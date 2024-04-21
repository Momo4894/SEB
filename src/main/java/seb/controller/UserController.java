package seb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.dal.repository.UserRepository;
import seb.model.User;

import java.util.Collection;
import java.util.List;

public class UserController extends Controller{
    private final UserRepository userRepository;
    private final UnitOfWork unitOfWork;

    public UserController(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.userRepository = new UserRepository(unitOfWork);
    }

    // GET /user(:username)
    public Response getUser(String username)
    {
        try {
            User userData = this.userRepository.getUser(username);
            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\" : \"success\" }" + userDataJSON
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }
    // GET /user
    public Response getUser() {
        try {
            List userData = this.userRepository.getUser();
            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    // POST /user

    public Response loginUser(Request request) {
        try {
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            this.userRepository.loginUser(user);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\": \"success\" }"
            );

        } catch (DataAccessException e) {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\": \"failed\" }"
            );
        }  catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    // POST /user
    public Response addUser(Request request) {
        try {
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            this.userRepository.addUser(user);

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"message\": \"Success\" }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (DataAccessException e) {
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"message\": \"User already exists\" }"
            );
        }

        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
        );
    }

    // PUT /user(:username)
    public Response addUserData(Request request) {
        try {
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            user.setUsername(request.getPathParts().get(1));
            this.userRepository.addUserData(user);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\": \"Success\" }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
        );
    }

    // GET /user
    public Response getUserPerRepository() {
        try (unitOfWork) {
            Collection<User> userData = new UserRepository(unitOfWork).findAllUsers();

            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
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
