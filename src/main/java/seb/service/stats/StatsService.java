package seb.service.stats;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.controller.StatsController;
import seb.dal.UnitOfWork;

public class StatsService implements Service {
    private final StatsController statsController;
    private final UnitOfWork unitOfWork;

    public StatsService() {
        unitOfWork = new UnitOfWork();
        this.statsController = new StatsController(unitOfWork);
    }

    @Override
    public Response handleRequest(Request request) {
        if(request.getPathParts().get(0).equals("history")) {
            if(request.getMethod() == Method.GET) {
                return this.statsController.getHistory(request);
            } else if(request.getMethod() == Method.POST) {
                return this.statsController.addHistory(request);
            }
        }

        if(request.getMethod() == Method.GET) {
            return this.statsController.getStats(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
