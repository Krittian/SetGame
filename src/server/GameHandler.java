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
        ret.put("stateNum", g.getStateNum());
        return ret;
    }

    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
    	
        //System.out.println("\nGameHandler received: " + jsonMap.toString());
               
    	JSONObject ret = new JSONObject();
    	if (jsonMap.has("leaveGame")){
            

        	String gID = jsonMap.getString("gameId");
            String uid = jsonMap.getString("uid");
            System.out.println( uid + " LEAVING "  + gID);
            Game g = gameList.get(gID);
            g.removePlayer(uid);
        	
        }
    	else if (jsonMap.has("hasUpdate") && jsonMap.has("stateNum") && jsonMap.has("gameId")) {
    		//System.out.println("here: hasUPdate and statenum and gameId ");
            String gID = jsonMap.getString("gameId");
            int state = jsonMap.getInt("stateNum");
            String uid = jsonMap.getString("uid");
            Game g = gameList.get(gID);
            g.addPlayer(uid);
            if(state < g.getStateNum()) {
            	System.out.println("state < server state" + state + " g.->  " + g.getStateNum());
                ret = sendGame(g);
                ret.put("hasUpdate", true);
            } else if(jsonMap.getBoolean("hasUpdate")) {
            	System.out.println("has update 2... checking set");
                JSONArray set = jsonMap.getJSONArray("set");
                if(set.length() == 3 && g.checkSet(uid, set.getInt(0), set.getInt(1), set.getInt(2))) {
                	System.out.println("valid SET!!");
                    ret = sendGame(g);
                    ret.put("hasUpdate", true);
                }
                else{
                	System.out.println("dumb SET");
                	ret.put("hasUpdate", false);
                }
            } else {
            	//System.out.println("has no update");
                ret.put("hasUpdate", false);
            }
        } 
        // send an empty json string {} until we do something else.
        this.sendJSON(ret, he);
    }
}
