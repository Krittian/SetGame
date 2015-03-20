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

    private HashMap<Integer, Game> gameList;

    public GameHandler() {
        gameList = new HashMap<>();
    }

    public int addGame() {
        int game_id = 3;
        return game_id;
    }
    public Integer[] getPlayers(int game_id) {
        return gameList.get(game_id).getPlayers();
    }
    public String[] getOutCards(int game_id) {
        return gameList.get(game_id).getOutCards();
    }
    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        System.out.println("\nGameHandler received: " + jsonMap.toString());
        
        // send an empty json string {} until we do something else.
        this.sendJSON((new JSONObject()).toString(), he);
    }
}
