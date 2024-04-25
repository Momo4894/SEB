package seb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.dal.repository.UserRepository;
import seb.model.Tips;
import seb.model.User;

import java.util.Collection;
import java.util.List;

public class UserController extends Controller{
    private final UserRepository userRepository;
    private final UnitOfWork unitOfWork;

    private Tips tips;

    public UserController(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.userRepository = new UserRepository(unitOfWork);
        this.tips = new Tips();
    }

    public UserRepository getUserRepository() { return userRepository; }

    // GET /user(:username)
    public Response getUser(Request request)
    {
        try {
            String username = request.getPathParts().get(1);
            if(request.getAuthorizationToken().equals(username + "-sebToken")) {
                User userData = this.userRepository.getUser(username);
                String userDataJSON = this.getObjectMapper().writeValueAsString(userData);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"message\" : \"success\" }" + userDataJSON + "\n" + tips.getTip()
                );
            }
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"invalid token\" }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
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
                    userDataJSON + "\n" + tips.getTip()
            );
        } catch (DataAccessException e) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\": \"failed\" }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    public User getUserFromBody(String bodyString) throws Exception {
        try {
            User user = this.getObjectMapper().readValue(bodyString, User.class);
            return user;
        } catch (JsonProcessingException e) {
            throw new Exception(e.getMessage(), e);
        }

    }

    // POST /user
    public Response loginUser(Request request) {
        try {
            User user = getUserFromBody(request.getBody());
            this.userRepository.loginUser(user);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\": \"success\" }" + "\n" + tips.getTip()
            );

        } catch (DataAccessException e) {
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\": \"failed\" }"
            );
        }  catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
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
            User user = getUserFromBody(request.getBody());
            this.userRepository.addUser(user);

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"message\": \"Success\" }" + "\n" + tips.getTip()
            );
        } catch (DataAccessException e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"message\": \"User already exists\" }"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        unitOfWork.rollbackTransaction();
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
        );
    }

    // PUT /user(:username)
    public Response addUserData(Request request) {
        try {
            String username = request.getPathParts().get(1);
            if (request.getAuthorizationToken().equals(username + "-sebToken")) {
                User user = getUserFromBody(request.getBody());
                user.setUsername(username);
                this.userRepository.addUserData(user);

                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"message\": \"Success\" }" + "\n" + tips.getTip()
                );
            }

            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\": \"invalid token\" }"
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

    // GET /user
    public Response getUserPerRepository() {
        try (unitOfWork) {
            Collection<User> userData = new UserRepository(unitOfWork).findAllUsers();

            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON + "\n" + tips.getTip()
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
