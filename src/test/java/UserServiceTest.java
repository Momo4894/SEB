import com.fasterxml.jackson.core.JsonProcessingException;
import httpserver.http.HttpStatus;
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
import seb.dal.DataAccessException;
import seb.dal.UnitOfWork;
import seb.dal.repository.UserRepository;
import seb.model.User;
import seb.service.user.UserService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private UserController spyUserController;

    private User newUser;


    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.userController = new UserController(new UnitOfWork());
        spyUserController = spy(userController);
        newUser = new User("Bert", "myPassword");
        //doReturn(userRepository).when(spyUserController).getUserRepository();

    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        doReturn(newUser).when(spyUserController).getUserFromBody(any(String.class));
        doNothing().when(userRepository).addUser(any(User.class));


        Request request = createMockRequestWithUser(newUser);


        Response response = spyUserController.addUser(request);

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }



    @Test
    public void testRegisterUser_UsernameExists() throws Exception {
        doReturn(newUser).when(spyUserController).getUserFromBody(any(String.class));
        doThrow(DataAccessException.class).when(userRepository).addUser(any(User.class));

        Request request = createMockRequestWithUser(newUser);

        Response response = spyUserController.addUser(request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void testLoginUSer_Success() throws Exception {
        doReturn(newUser).when(spyUserController).getUserFromBody(any(String.class));
        doNothing().when(userRepository).loginUser(any(User.class));

        Request request = createMockRequestWithUser(newUser);

        Response response = spyUserController.loginUser(request);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testLoginUSer_UserNotFound() throws Exception {
        doReturn(newUser).when(spyUserController).getUserFromBody(any(String.class));
        doThrow(DataAccessException.class).when(userRepository).loginUser(any(User.class));

        Request request = createMockRequestWithUser(newUser);

        Response response = spyUserController.loginUser(request);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void editUserData_Success() throws Exception {
        doReturn(newUser).when(spyUserController).getUserFromBody(any(String.class));
        doNothing().when(userRepository).addUserData(any(User.class));

        Request request = createMockRequestWithUser(newUser);

        Response response = spyUserController.addUserData(request);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

    private Request createMockRequestWithUser(User user) {
        Request request = mock(Request.class);
        String bodyString = String.format(
                "{\"Username\":\"%s\", \"Password\":\"%s\"}",
                user.getUsername(),
                user.getPassword()
        );
        when(request.getBody()).thenReturn(bodyString);
        when(request.getAuthorizationToken()).thenReturn(user.getUsername() + "-sebToken");
        when(request.getPathParts().get(0)).thenReturn(user.getUsername());
        return request;
    }




}
