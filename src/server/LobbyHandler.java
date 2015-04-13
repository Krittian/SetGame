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
import java.util.ArrayList;
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
	public static final String USER_SIGNUP = "user_signup_request";
	public static final int maxNameSize = 30;
	private static final String GAME_DATA = "gameData";
	private static final String JOIN_GAME_STRING = "!!!!!";

	public LobbyHandler(GameHandler g) {
		this.gamehandler = g;
	}

	@Override
	public void handleRequest(JSONObject jsonMap, HttpExchange he) {
		boolean sent = false;
		getConnection();
		JSONObject ret = new JSONObject();
		//createTables();
		if (jsonMap.has(USER_SIGNUP)) {
			JSONObject signup = jsonMap.getJSONObject(USER_SIGNUP);
			String name = signup.getString(NAME_STRING);
			String pwd = signup.getString(PWD_STRING);
			System.out.println("lobbyHandler received (signup): " + name + ", " + pwd);
			if(addUser(name, pwd) == 1){
				ret.put(AUTH_STRING, true);
			}
			else
				ret.put(AUTH_STRING, false);
		}
		else if (jsonMap.has(LOGIN_STRING)) {
			//          format of login_string {login: {name: ______, password: _____}}
			//          therefore we need to unpack to JSONObject
			JSONObject login = jsonMap.getJSONObject(LOGIN_STRING);
			String name = login.getString(NAME_STRING);
			String pwd = login.getString(PWD_STRING);
			System.out.println("lobbyHandler received: " + name + ", " + pwd);
			boolean checkUserBool = checkUser(name, pwd);// (name.equals("Eli") && pwd.equals("b"));
			if (checkUserBool) {
				ret.put(AUTH_STRING, true);
				int gameListSize = gamehandler.getGameList().size();
				System.out.println("gameListSize: " + gameListSize);
				Object[] gameIdArray = new Object[gameListSize];
				Object[] gameNameArray = new Object[gameListSize];
				Integer [] gamePlayerCountsArray = new Integer [gameListSize];
				int i = 0;
				for (Game g : gamehandler.getGameList().values()) {
					gameIdArray[i] = (String) g.getID();
					gameNameArray[i] = g.getName();
					gamePlayerCountsArray[i] = g.getPlayers().length;
					System.out.print("_ABC:__" + g.getPlayers().length);
					i++;
				}
				ret.put("gameIds", gameIdArray);
				ret.put("gameNames", gameNameArray);
				ret.put("gamePlayerCounts", gamePlayerCountsArray);
				String uuid = UUID.randomUUID().toString();
				ret.put("uid", uuid);
			} else {
				ret.put(AUTH_STRING, false);
			}
		} else if (jsonMap.has(GAME_DATA)) {
			System.out.println("lobbyHandler received In GAME_DATA: ");
			JSONObject gameData = jsonMap.getJSONObject(GAME_DATA);
			String uid = gameData.getString("uid");
			String gameName = gameData.getString("gameName");
			String requestType = gameData.getString("request");

			if (requestType.equals(NEW_GAME_STRING)) {
				System.out.println("lobbyHandler received In NEW_GAME_STRING: ");
				String gameId = UUID.randomUUID().toString();
				System.out.println("addding game: " + gamehandler.addGame(gameId, gameName) + " ___" + gamehandler.getGameList().size());
				
				ret.put("gameId", gameId);

			} else if (requestType.equals(JOIN_GAME_STRING)) {
				System.out.println("lobbyHandler received In JOIN_GAME_STRING: ");
				ret.put("gameState", "you've joined a game!!");
			}
			System.out.println("lobbyHandler received In GAME_DATA: " + uid + ", " + gameName + ", " + requestType);

		}
		this.sendJSON(ret, he);
	}

	private Connection getConnection(){

		Connection con = null;
		try{
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			Class.forName("com.mysql.jdbc.Driver");
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

	private boolean checkUser(String name, String password) {
		Connection c = getConnection();
		PreparedStatement  stmt =null;
		try{
			String sql = "SELECT * FROM Users WHERE name=? AND password=?;";	
			stmt = c.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();
			int count = 0;
			boolean isGood = false;
			while ( rs.next() )
			{
				count++;
				if(name.equals(rs.getString("name")))
					isGood = true;
			}
			rs.close();
			stmt.close();
			c.close();
			if(count ==1 && isGood)//count is 1 to help make sure there's no funny bussiness of getting all the rows 
				return true;
		}catch(Exception e){
			e.printStackTrace();			
		}
		return false;
	}


	/**
	 * returns 1 on success and -1 on fail (b/c the name is already in there), or -2 for other fail
	 */
	private int addUser(String name, String password){
		//password = MD5;
		Connection c = getConnection();
		PreparedStatement  stmt =null;
		try{
			String sql = "INSERT INTO Users(name, password) VALUES(?,?);";	
			stmt = c.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, password);
			int returnVal = stmt.executeUpdate();
			stmt.close();
			c.close();
			return returnVal;
		}catch(Exception e){
			e.printStackTrace();			
			return -2;
		}

	}

	private void createTables(){
		Connection c = getConnection();
		Statement stmt =null;
		ResultSet rs = null;
		String sql = null;
		System.out.println("Creating Tables");
		try{
			stmt = c.createStatement();
			sql = "DROP TABLE IF EXISTS Users; "//	 +			" DROP TABLE Games IF EXISTS;"
					//+ "DROP TABLE IF EXISTS Games; "
					;
			stmt.execute(sql);
			sql = "CREATE TABLE Users (" +
					//"uid int NOT NULL AUTO_INCREMENT," +
					"name CHAR(" + maxNameSize + ")," +
					"password TEXT," +
					//"ssn CHAR(10)," +//don't need it, but if they give us it, why wouldn't we store it.
					"PRIMARY KEY(name)" +
					//"UNIQUE (name)" +
					");";

			stmt.execute(sql);
			stmt.close();
		}catch(Exception e){
			System.err.println(e);
		}
		System.out.println("FINISHED Creating Tables");

	}
}
