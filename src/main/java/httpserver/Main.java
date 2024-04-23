package httpserver;

import httpserver.server.Server;
import httpserver.utils.Router;
import seb.service.stats.ScoreService;
import seb.service.stats.StatsService;
import seb.service.tournament.TournamentService;
import seb.service.user.SessionService;
import seb.service.user.UserService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter()
    {
        Router router = new Router();
        router.addService("/users", new UserService());
        router.addService("/sessions", new SessionService());
        router.addService("/stats", new StatsService());
        router.addService("/score", new ScoreService());
        router.addService("/history", new StatsService());
        router.addService("/tournament", new TournamentService());

        return router;
    }
}