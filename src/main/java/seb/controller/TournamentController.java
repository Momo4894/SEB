package seb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.UnitOfWork;
import seb.dal.repository.T_ParticipantRepository;
import seb.dal.repository.TournamentRepository;
import seb.dal.repository.UserRepository;
import seb.model.Status;
import seb.model.Tournament;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TournamentController {
    private UnitOfWork unitOfWork;
    private TournamentRepository tournamentRepository;
    private T_ParticipantRepository t_participantRepository;
    private ObjectMapper objectMapper;
    private UserRepository userRepository;

    public TournamentController (UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.tournamentRepository = new TournamentRepository(unitOfWork);
        this.t_participantRepository = new T_ParticipantRepository(unitOfWork);
        this.userRepository = new UserRepository(unitOfWork);
        this.objectMapper = new ObjectMapper();
    }

    public Response getTournamentsByUser(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            System.out.println("TournamentController.getTournamentsByUser(Request request): " + username);
            List<Tournament> tournaments = this.tournamentRepository.getTournamentsByUsername(username);
            System.out.println("TournamentController.getTournamentsByUser(Request request): after getTournamentsByUsername");
            if (tournaments == null) {
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"no tournament found\" }"
                );
            }
            System.out.println("TournamentController.getTournamentsByUser(Request request): after not null");
            System.out.println(tournaments);

            List<String> formattedTournaments = new ArrayList<>();
            System.out.println("TournamentController.getTournamentsByUser(Request request): after new array");
            for (Tournament currentTournament: tournaments) {
                System.out.println("TournamentController.getTournamentsByUser(Request request): in currentTournament");
                String tournamentFormat;
                if(!currentTournament.getStatusString().equals("pending")) {
                    System.out.println("TournamentController.getTournamentsByUser(Request request): in status != pending");
                    int participantAmount = this.t_participantRepository.getParticipantAmountByTournamentId(currentTournament.getId());

                    //get 1st place
                    List<String> firstPlaceList = this.t_participantRepository.getFirstPlaceUsernameByTournamentId(currentTournament.getId());
                    //create 1st place String
                    String firstPlace;
                    if (firstPlaceList.size() > 1) {
                        firstPlace = String.format(
                                "{ \"tied first place\": %s }",
                                firstPlaceList
                        );
                    } else if (firstPlaceList.size() == 1) {
                        firstPlace = firstPlaceList.getFirst();
                    } else { firstPlace = "no first place"; }
                    System.out.println("TournamentController.getTournamentsByUser(Request request): before timestamp was made readable");
                    //retrieve time and make readable
                    Timestamp startTime = currentTournament.getStartTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String formattedStartTime = simpleDateFormat.format(startTime);
                    System.out.println("TournamentController.getTournamentsByUser(Request request): after timestamp was made readable");

                    tournamentFormat = String.format(
                            "{ \"tournament-status\": %s, \"participants\": %s, \"1st Place\": %s, \"start-time\": %s }",
                            currentTournament.getStatusString(),
                            participantAmount,
                            firstPlace,
                            formattedStartTime
                    );
                } else {
                    tournamentFormat = String.format(
                            "{ \"tournament-status\": %s }",
                            currentTournament.getStatusString()
                    );
                }

                formattedTournaments.add(tournamentFormat);
            }
            String responseJSON = objectMapper.writeValueAsString(formattedTournaments);
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    responseJSON
            );


        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    //(tournament started; 2 participants; altenhof in front; write start-time)
    public Response getActiveTournament(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            Tournament tournament = this.tournamentRepository.getTournamentByStatusAndUsername(username, Status.ACTIVE);
            if (tournament == null) {
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"no active tournament\" }"
                );
            }
            //get participant amount
            int participantAmount = this.t_participantRepository.getParticipantAmountByTournamentId(tournament.getId());

            //get 1st place
            List<String> firstPlaceList = this.t_participantRepository.getFirstPlaceUsernameByTournamentId(tournament.getId());
            //create 1st place String
            String firstPlace;
            if (firstPlaceList.size() > 1) {
                firstPlace = String.format(
                        "{ \"tied first place\": %s }",
                        firstPlaceList
                );
            } else if (firstPlaceList.size() == 1) {
                firstPlace = firstPlaceList.getFirst();
            } else { firstPlace = "no first place"; }

            //retrieve time and make readable
            Timestamp startTime = tournament.getStartTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String formattedStartTime = simpleDateFormat.format(startTime);

            String responseJSON = String.format(
                    "{ \"tournament-status\": %s, \"participants\": %s, \"1st Place\": %s, \"start-time\": %s }",
                    tournament.getStatusString(),
                    participantAmount,
                    firstPlace,
                    formattedStartTime
            );

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    responseJSON
            );
        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }

    }

    public void endTournament(int tournament_id) {
        try {
            this.tournamentRepository.endTournament(tournament_id);
            Map<Integer, Integer> userIdPlacement = this.t_participantRepository.getPlacementsByTournamentId(tournament_id);
            List<Integer> firstPlace = new ArrayList();
            List<Integer> otherPlaces = new ArrayList();
            for (Map.Entry<Integer, Integer> currentUser : userIdPlacement.entrySet()) {
                if(currentUser.getValue() == 1) {
                    firstPlace.add(currentUser.getKey());
                } else {
                    otherPlaces.add(currentUser.getKey());
                }
            }
            if (firstPlace.size() < 1) {
                for (Integer currentFirstPlace: firstPlace) {
                    this.userRepository.changeEloByUserId(currentFirstPlace, 1);
                }
            } else {
                this.userRepository.changeEloByUserId(firstPlace.get(0), 2);
            }
            for (Integer currentOtherPlace: otherPlaces) {
                this.userRepository.changeEloByUserId(currentOtherPlace, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
        }
    }
}
