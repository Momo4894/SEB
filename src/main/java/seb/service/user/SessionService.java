package seb.service.user;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.controller.UserController;
import seb.dal.UnitOfWork;

public class SessionService implements Service {
    private final UserController userController;
    private final UnitOfWork unitOfWork;

    public SessionService() {
        unitOfWork = new UnitOfWork();
        this.userController = new UserController(unitOfWork);
    }

    @Override
    public Response handleRequest(Request request) {
        if(request.getMethod() == Method.POST) {
            return this.userController.loginUser(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
