import httpserver.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import seb.controller.TournamentController;
import seb.dal.UnitOfWork;
import seb.dal.repository.TournamentRepository;
import seb.model.User;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceTest {
    @Mock
    TournamentRepository tournamentRepository;

    @InjectMocks
    TournamentController tournamentController;
    TournamentController spyTournamentController;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.tournamentController = new TournamentController(new UnitOfWork());
        spyTournamentController = spy(tournamentController);
    }


    private Request createMockRequest() {
        Request request = mock(Request.class);
        String bodyString = String.format(
                "{\"Username\":\"%s\", \"Password\":\"%s\"}",
                "Bert",
                "MyPassword"
        );
        when(request.getBody()).thenReturn(bodyString);
        when(request.getAuthorizationToken()).thenReturn("Bert-sebToken");
        when(request.getPathParts().get(0)).thenReturn("Bert");
        return request;
    }
}
