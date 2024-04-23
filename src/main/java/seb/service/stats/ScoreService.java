package seb.service.stats;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.controller.ScoreController;
import seb.dal.UnitOfWork;

public class ScoreService implements Service {
    private final ScoreController scoreController;
    private final UnitOfWork unitOfWork;

    public ScoreService() {
        this.unitOfWork = new UnitOfWork();
        this.scoreController = new ScoreController(unitOfWork);
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println(request.getPathParts());

        if(request.getMethod() == Method.GET) {
            return this.scoreController.getScore(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }

}
