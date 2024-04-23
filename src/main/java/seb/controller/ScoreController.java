package seb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import seb.dal.UnitOfWork;
import seb.dal.repository.StatsRepository;
import seb.dal.repository.UserRepository;
import seb.model.Stats;
import seb.model.User;

import java.util.*;

public class ScoreController extends Controller{
    private final StatsRepository statsRepository;
    private final UserRepository userRepository;
    private final UnitOfWork unitOfWork;
    public ScoreController(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        this.userRepository = new UserRepository(unitOfWork);
        this.statsRepository = new StatsRepository(unitOfWork);
    }

    public Response getScore(Request request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            Map<String, Object> otherUserStatsMap = new HashMap<>();
            Map<String, Object> currentUserStats = new HashMap<>();
            List<User> users = this.userRepository.getUser();
            for (User currentUser: users) {
                boolean isAdded = false;
                Map<String, Object> userStatsDetails = new HashMap<>();
                String username = currentUser.getUsername();
                int user_id = currentUser.getId();
                int elo = currentUser.getElo();
                int overAllPushUpCount = this.statsRepository.getOverAllPushupsPerUser(user_id);
                userStatsDetails.put("Elo", elo);
                userStatsDetails.put("Pushup-count", overAllPushUpCount);


                if (request.getAuthorizationToken() != null) {
                    String[] parts = request.getAuthorizationToken().split("-sebToken", 2);
                    if (parts[0].equals(username)) {
                        currentUserStats.put(username, userStatsDetails);
                        isAdded = true;
                    }
                }
                if (!isAdded) {
                    otherUserStatsMap.put(username, userStatsDetails);
                }
            }

            String userStatsJson = objectMapper.writeValueAsString(currentUserStats);
            String otherUsersStatsJson = objectMapper.writeValueAsString(otherUserStatsMap);

            String responseBody = String.format(
                    "{ \"currentUser\": %s, \"otherUsers\": %s }",
                    userStatsJson,
                    otherUsersStatsJson
            );

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    responseBody
            );

        } catch (Exception e) {
            unitOfWork.rollbackTransaction();
            throw new RuntimeException(e);
        }

    }
}
