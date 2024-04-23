package seb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.UnitOfWork;
import seb.dal.repository.TournamentRepository;
import seb.model.Status;
import seb.model.Tournament;

import java.util.HashMap;
import java.util.Map;

public class TournamentController {
    private UnitOfWork unitOfWork;
    private TournamentRepository tournamentRepository;
    private ObjectMapper objectMapper;

    public TournamentController (UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.tournamentRepository = new TournamentRepository(unitOfWork);
        this.objectMapper = new ObjectMapper();
    }

    public Response getActiveTournament(Request request) {
        try {
            String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
            String username = parts[0];
            Tournament tournament = this.tournamentRepository.getTournamentByStatusAndUsername(username, Status.ACTIVE);
            Map<String, String> mapResponseJSON = new HashMap<>();
            String response;
            if (tournament != null) {
                response = objectMapper.writeValueAsString(tournament);
            } else {
                response = "no entries";
            }
            mapResponseJSON.put(username, response);
            String responseJSON = objectMapper.writeValueAsString(mapResponseJSON);
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
}
