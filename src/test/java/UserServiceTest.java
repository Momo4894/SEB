import httpserver.server.Request;
import httpserver.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import seb.controller.UserController;
import seb.dal.repository.UserRepository;
import seb.model.User;
import seb.service.user.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        Request request = new Request();
        request.setBody("{\"Username\":\"Bert\", \"Password\":\"myPassword\"}");

        Response response = userController.addUser(request);

        assertEquals(201, response.getStatus());
    }


}
