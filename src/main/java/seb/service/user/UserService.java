package seb.service.user;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.Service;
import seb.controller.UserController;
import seb.dal.UnitOfWork;

public class UserService implements Service {
    private final UserController userController;
    private final UnitOfWork unitOfWork;

    public UserService() {
        unitOfWork = new UnitOfWork();
        this.userController = new UserController(unitOfWork);
    }

    @Override
    public Response handleRequest(Request request) {
        if(request.getMethod() == Method.GET && request.getPathParts().size() > 1) {
            return this.userController.getUser(request);
        } else if (request.getMethod() == Method.PUT && request.getPathParts().size() > 1) {
            return this.userController.addUserData(request);
        } else if (request.getMethod() == Method.GET) {
            return this.userController.getUserPerRepository();
        } else if (request.getMethod() == Method.POST) {
            return this.userController.addUser(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
