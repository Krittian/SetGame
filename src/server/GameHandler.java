/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author EliFriedman
 */
public class GameHandler extends ResponseHandler {

    private HashMap<String, Game> gameList;

    public GameHandler() {
        gameList = new HashMap<>();
    }

    public boolean addGame(String name) {
        if (gameList.containsKey(name)) {
            return false;
        }
        Game game = new Game(name);
        gameList.put(name, game);
        return true;
    }

    public Integer[] getPlayers(String game_name) {
        return gameList.get(game_name).getPlayers();
    }

    public String[] getOutCards(String game_name) {
        return gameList.get(game_name).getOutCards();
    }

    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        System.out.println("\nGameHandler received: " + jsonMap.toString());

        // send an empty json string {} until we do something else.
        this.sendJSON((new JSONObject()).toString(), he);
    }
}
