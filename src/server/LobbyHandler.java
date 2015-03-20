/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 * @author EliFriedman
 */
public class LobbyHandler extends ResponseHandler {

    private GameHandler gamehandler;
    public static final String NEW_GAME_STRING = "new_game";
    public static final String LOGIN_STRING = "login";
    public static final String NAME_STRING = "name";
    public static final String PWD_STRING = "password";
    public static final String ADD_PLAYER_STRING = "addplayer";

    public LobbyHandler(GameHandler g) {
        this.gamehandler = g;
    }

    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        
        this.sendJSON("{\"Response\": \"recieved\"}", he);
    }

}
