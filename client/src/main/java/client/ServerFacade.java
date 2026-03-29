package client;

import com.google.gson.Gson;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public String getAuthToken() { return authToken; }

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null);
        sendRequest(request);
    }

    public RegisterResult register(RegisterRequest requestBody) throws ResponseException {
        var request = buildRequest("POST", "/user", requestBody);
        var response = sendRequest(request);

        RegisterResult result = handleResponse(response, RegisterResult.class);
        this.authToken = result.authToken();

        return result;
    }

    public LoginResult login(LoginRequest requestBody) throws ResponseException {
        var request = buildRequest("POST", "/session", requestBody);
        var response = sendRequest(request);

        LoginResult result = handleResponse(response, LoginResult.class);
        this.authToken = result.authToken();

        return result;
    }

    public void logout() throws ResponseException {
        var request = buildRequest("DELETE", "/session", null);
        var response = sendRequest(request);
        handleResponse(response, null);
        this.authToken = null;
    }

    public ListGamesResult listGames() throws ResponseException {
        var request = buildRequest("GET", "/game", null);
        var response = sendRequest(request);

        return handleResponse(response, ListGamesResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest requestBody) throws ResponseException {
        var request = buildRequest("POST", "/game", requestBody);
        var response = sendRequest(request);

        return handleResponse(response, CreateGameResult.class);
    }

    public void joinGame(JoinGameRequest requestBody) throws ResponseException {
        var request = buildRequest("PUT", "/game", requestBody);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (this.authToken != null) {
            request.header("authorization", this.authToken);
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                Map errorMap = new Gson().fromJson(body, Map.class);
                throw new ResponseException(status, (String) errorMap.get("message"));
            }

            throw new ResponseException(status, "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}