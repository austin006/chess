package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.request.*;
import service.result.*;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws ResponseException{
        facade.clear();
    }

    // Register tests
    @Test
    void registerSuccess() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = facade.register(request);

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    void registerFail() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = facade.register(request);

        ResponseException exception = assertThrows(ResponseException.class, () -> {
            facade.register(request);
        });
        assertEquals(403, exception.statusCode());
    }

}
