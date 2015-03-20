/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

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
        System.out.println("\nGameHandler received: ");
        String json = "{\"a\": \"b\"}";
        
        this.sendJSON(json, he);
    }
}
