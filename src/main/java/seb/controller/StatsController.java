package seb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.dal.repository.StatsRepository;
import seb.dal.repository.T_ParticipantRepository;
import seb.dal.repository.TournamentRepository;
import seb.dal.repository.UserRepository;
import seb.model.Stats;
import seb.model.Status;
import seb.model.Tournament;
import seb.model.Tournament_Participant;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsController extends Controller{
    private final StatsRepository statsRepository;
    private final UserRepository userRepository;
    private TournamentRepository tournamentRepository;
    private T_ParticipantRepository t_participantRepository;
    private final UnitOfWork unitOfWork;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final TournamentController tournamentController;


    public StatsController(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.userRepository = new UserRepository(unitOfWork);
        this.statsRepository = new StatsRepository(unitOfWork);
        this.tournamentRepository = new TournamentRepository(unitOfWork);
        this.t_participantRepository = new T_ParticipantRepository(unitOfWork);
        this.tournamentController = new TournamentController(unitOfWork);
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public Response getStats(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            int user_id = this.userRepository.getUserId(username);

                int user_elo = this.userRepository.getElo(username);

                int overallPushupCount = this.statsRepository.getOverAllPushupsPerUser(user_id);

                String responseJSON = String.format(
                        "{ \"elo\": %s, \"overall PushUp count\": %s }",
                        user_elo,
                        overallPushupCount
                );

                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        responseJSON
                );

        } catch (Exception e) {
            unitOfWork.rollbackTransaction();
            throw new RuntimeException(e);
        }
    }

    public Response getHistory(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            int user_id = this.userRepository.getUserId(username);

            List<Stats> stats = this.statsRepository.getAllStatsByUser(user_id); //getStats()

            List<String> statsString = new ArrayList<>();
            if (!objectMapper.writeValueAsString(stats).equals("[]")) {
                for (Stats currentStats: stats) {
                    String tempString = String.format(
                            "{ \"history-ID\": %s: { \"duration\": %s, \"PushUp-Count\": %s }",
                            currentStats.getId(),
                            currentStats.getDuration(),
                            currentStats.getCount()
                    );
                    statsString.add(tempString);
                }
            } else {
                statsString.add("no entry");
            }


            String responseJSON = String.format(
                    "{ %s: %s }",
                    username,
                    statsString
            );

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    responseJSON
            );

        } catch (Exception e) {
            unitOfWork.rollbackTransaction();
            throw new RuntimeException(e);
        }
    }

    public Response addHistory(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];

            int tournament_id;
            int user_id = this.userRepository.getUserId(username);
            Tournament tournament = this.tournamentRepository.getTournamentByStatusAndUsername(username, Status.ACTIVE);
            if (tournament == null) {
                tournament = this.tournamentRepository.getTournamentByStatus(Status.ACTIVE);
                if (tournament == null) {
                    tournament = this.tournamentRepository.getTournamentByStatusAndUsername(username, Status.PENDING);
                    if (tournament == null) {
                        this.tournamentRepository.addTournament();
                        tournament = this.tournamentRepository.getTournamentByStatus(Status.PENDING);
                    }
                    this.tournamentRepository.startTournament(tournament.getId());
                    LocalDateTime startTime = this.tournamentRepository.getStartTimeById(tournament.getId());
                    LocalDateTime endTime = startTime.plusMinutes(2);
                    long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
                    final int tournamentIdTemp = tournament.getId();

                    scheduler.schedule(() -> this.tournamentController.endTournament(tournamentIdTemp), delay, TimeUnit.MILLISECONDS);
                }
                //addTournament Participant
                this.t_participantRepository.addTournamentParticipant(user_id, tournament.getId());
            }
            tournament_id = tournament.getId();
            Stats stats = this.getObjectMapper().readValue(request.getBody(), Stats.class);
            stats.addIds(user_id, tournament_id);
            this.statsRepository.addHistory(stats);
            int totalCountByUser = this.statsRepository.getOverAllCountByUserInTournament(user_id, tournament_id);
            this.t_participantRepository.addScoreById(tournament_id, user_id, totalCountByUser);
            this.t_participantRepository.updatePlacements(tournament_id);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\" : \"success\" }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (DataAccessException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"message\": \"History already exists\" }"
            );
        }
        unitOfWork.rollbackTransaction();
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
        );
    }


}
