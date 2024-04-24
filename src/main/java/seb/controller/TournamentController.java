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
            List<Tournament> tournaments = this.tournamentRepository.getTournamentsByUsername(username);
            if (tournaments == null) {
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"no tournament found\" }"
                );
            }




            String formatTournaments;
            List<String> formattedTournaments = new ArrayList<>();
            for (Tournament currentTournament: tournaments) {
                String tournamentFormat;
                if(!currentTournament.getStatusString().equals("pending")) {
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

                    //retrieve time and make readable
                    Timestamp startTime = currentTournament.getStartTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String formattedStartTime = simpleDateFormat.format(startTime);

                    tournamentFormat = String.format(
                            "{ \"tournament-status\": %s, \"participants\": %s, \"1st Place\": %s, \"start-time\": %s }\n",
                            currentTournament.getStatusString(),
                            participantAmount,
                            firstPlace,
                            formattedStartTime
                    );
                } else {
                    tournamentFormat = String.format(
                            "{ \"tournament-status\": %s }\n",
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
            //get placements and user_ids from t_participants
            //sort out the elo changes
            //this.userRepository.changeEloById(user_id);

        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
        }
    }
}
