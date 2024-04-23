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
import seb.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatsController extends Controller{
    private final StatsRepository statsRepository;
    private final UserRepository userRepository;
    private TournamentRepository tournamentRepository;
    private T_ParticipantRepository t_participantRepository;
    private final UnitOfWork unitOfWork;
    private final ObjectMapper objectMapper;

    public StatsController(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.userRepository = new UserRepository(unitOfWork);
        this.statsRepository = new StatsRepository(unitOfWork);
        this.tournamentRepository = new TournamentRepository(unitOfWork);
        this.t_participantRepository = new T_ParticipantRepository(unitOfWork);
        this.objectMapper = new ObjectMapper();
    }

    public Response getStats(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            int user_id = this.userRepository.getUserId(username);
                System.out.println("user_id: " + user_id);

                int user_elo = this.userRepository.getElo(username);
                System.out.println("user_elo: " + user_elo);

                int overallPushupCount = this.statsRepository.getOverAllPushupsPerUser(user_id);
                System.out.println("overall pushups: " + overallPushupCount);

                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"message\" : \"success\" } elo: " + user_elo + " overall pushup count: " + overallPushupCount
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

            Collection<Stats> stats = this.statsRepository.getAllStatsByUser(user_id); //getStats()

            Map<String, Object> map = new HashMap<>();
            Map<Object, Object> historyMap = new HashMap<>();

            if (!objectMapper.writeValueAsString(stats).equals("[]")) {
                for (Stats currentStats: stats) {
                    Map<String, Object> historyMapDetails = new HashMap<>();
                    historyMapDetails.put("duration", currentStats.getDuration());
                    historyMapDetails.put("count", currentStats.getCount());
                    historyMap.put(currentStats.getId(), historyMapDetails);
                }
                map.put(username, historyMap);
            } else { map.put(username, "no entry"); }

            String historyJSON = objectMapper.writeValueAsString(map);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    historyJSON
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
                }
                //addTournament Participant
                this.t_participantRepository.addTournamentParticipant(user_id, tournament.getId());
            }
            tournament_id = tournament.getId();
            //addHistory with user_id and tournament_id
            Stats stats = this.getObjectMapper().readValue(request.getBody(), Stats.class);
            stats.addIds(user_id, tournament_id);
            this.statsRepository.addHistory(stats);
            new Response(
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
