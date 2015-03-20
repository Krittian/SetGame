/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

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
        if(jsonMap.has(LOGIN_STRING)) {
//          format of login_string {login: {name: ______, password: _____}}
//          therefore we need to unpack to JSONObject
            JSONObject login = jsonMap.getJSONObject(LOGIN_STRING);
            String name = login.getString(NAME_STRING);
            String pwd = login.getString(PWD_STRING);
            System.out.println(name+": " +pwd);
        }
        this.sendJSON((new JSONObject()).toString(), he);
    }

}
