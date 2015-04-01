/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import java.util.HashMap;
import org.json.JSONArray;
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

    public boolean addGame(String ID, String name) {
        if (gameList.containsKey(ID)) {
            return false;
        }
        Game game = new Game(ID, name);
        gameList.put(ID, game);
        return true;
    }

    public HashMap<String, Game> getGameList() {
        return gameList;
    }
    private JSONObject sendGame(String gID) {
        if(gameList.containsKey(gID)) {
            return sendGame(gameList.get(gID));
        }
        return new JSONObject();
    }
    private JSONObject sendGame(Game g) {
        JSONObject ret = new JSONObject();
        
        JSONArray outcards = new JSONArray(g.getOutCards(false));
        ret.put("cards", outcards);
        ret.put("statenum", g.getStateNum());
        return ret;
    }

    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        System.out.println("\nGameHandler received: " + jsonMap.toString());
        JSONObject ret = new JSONObject();
        if (jsonMap.has("hasUpdate") 
                && jsonMap.has("stateNum") 
                && jsonMap.has("gameId")) {
            String gId = jsonMap.getString("gameId");
            int state = jsonMap.getInt("stateNum");
            Game g = gameList.get("gId");
            if(state < g.getStateNum()) {
                ret = sendGame(gId);
                ret.put("hasUpdate", true);
            } else if(jsonMap.getBoolean("hasUpdate")) {
                JSONArray set = jsonMap.getJSONArray("set");
                String uid = jsonMap.getString("uid");
                if(g.checkSet(uid, set.getString(0), set.getString(1), set.getString(2))) {
                    ret = sendGame(g);
                    ret.put("hasUpdate", true);
                }
                ret.put("hasUpdate", false);
            } else {
                ret.put("hasUpdate", false);
            }
        }
        // send an empty json string {} until we do something else.
        this.sendJSON(ret, he);
    }
}
