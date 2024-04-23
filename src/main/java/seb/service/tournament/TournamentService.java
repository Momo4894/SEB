package seb.service.tournament;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.controller.TournamentController;
import seb.dal.UnitOfWork;

public class TournamentService implements Service {
    private final TournamentController tournamentController;
    private final UnitOfWork unitOfWork;

    public TournamentService () {
        this.unitOfWork = new UnitOfWork();
        this.tournamentController = new TournamentController(unitOfWork);

    }

    @Override
    public Response handleRequest (Request request) {
        if (request.getMethod() == Method.GET) {
            return this.tournamentController.getActiveTournament(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }

}
