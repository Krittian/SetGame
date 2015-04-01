/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.util.UUID;

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
    public static final String AUTH_STRING = "authentication";
    public static final String USER_COOKIE = "user_cookie";
    public static final String REQ_GAMELIST_STRING = "gamelist_request";
    private static final String GAME_DATA = "!!!!!!!";
    private static final String JOIN_GAME_STRING = "!!!!!";

    public LobbyHandler(GameHandler g) {
        this.gamehandler = g;
    }

    @Override
    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        boolean sent = false;
        getConnection();
        JSONObject ret = new JSONObject();
        createTables();
        if (jsonMap.has(LOGIN_STRING)) {
//          format of login_string {login: {name: ______, password: _____}}
//          therefore we need to unpack to JSONObject
            JSONObject login = jsonMap.getJSONObject(LOGIN_STRING);
            String name = login.getString(NAME_STRING);
            String pwd = login.getString(PWD_STRING);
            System.out.println("lobbyHandler received: " + name + ", " + pwd);
            if (name.equals("Eli") && pwd.equals("b")) {
                ret.put(AUTH_STRING, true);
                Object[] gameIdArray = new Object[gamehandler.getGameList().size()];
                Object[] gameNameArray = new Object[gamehandler.getGameList().size()];
                int i = 0;
                for (Game g : gamehandler.getGameList().values()) {
                    gameIdArray[i] = (String) g.getID();
                    gameNameArray[i] = g.getName();
                    i++;
                }
                ret.put("gameIds", gameIdArray);
                ret.put("gameNames", gameNameArray);
                String uuid = UUID.randomUUID().toString();
                ret.put("uid", uuid);
            } else {
                ret.put(AUTH_STRING, false);
            }
        } else if (jsonMap.has(GAME_DATA)) {
            JSONObject gameData = jsonMap.getJSONObject(GAME_DATA);
            String uid = gameData.getString("uid");
            String gameName = gameData.getString("gameName");
            String requestType = gameData.getString("request");

            if (requestType.equals(NEW_GAME_STRING)) {
                String gameId = UUID.randomUUID().toString();
                gamehandler.addGame(gameId, gameName);
                ret.put("gameId", gameId);

            } else if (requestType.equals(JOIN_GAME_STRING)) {
                ret.put("gameState", "you've joined a game!!");
            }
            System.out.println("lobbyHandler received: " + uid + ", " + gameName + ", " + requestType);

        }
        this.sendJSON(ret, he);
    }

	private Connection getConnection(){
		
		Connection con = null;
		try{
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("got here.");
			con = DriverManager.getConnection("jdbc:mysql://localhost/setgame", "root", "betamobile");
			if(con.isClosed()){
				System.out.println("mySQL is closed");
			}
		}catch(Exception e){
			System.out.println("could not connect to mySQL");
			System.err.println(e);

		}
		return con;
	}
	 
	private void createTables(){
		Connection c = getConnection();
		Statement stmt =null;
		ResultSet rs = null;
		String sql = null;
		
		try{
		stmt = c.createStatement();
		sql = "DROP TABLE IF EXISTS Users; "//	 +			" DROP TABLE Games IF EXISTS;"
			//+ "DROP TABLE IF EXISTS Games; "
				;
		stmt.execute(sql);
		sql = "CREATE TABLE Users (" +
				"uid int NOT NULL AUTO_INCREMENT," +
				"name TEXT," +
				"password TEXT," +
				"ssn CHAR(10)," +//don't need it, but if they give us it, why wouldn't we store it.
				"PRIMARY KEY(uid)" +
				");";
	    
	    stmt.execute(sql);
	    sql = "";
	    stmt.close();
		}catch(Exception e){
			System.err.print(e);
		}
	}
}
