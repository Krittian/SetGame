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
    private static final String UID_SIGNIN = "uid_signin";

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
            ret = signup(jsonMap);
        } else if (jsonMap.has(LOGIN_STRING)) {
            ret = login(jsonMap);
        } else if (jsonMap.has(GAME_DATA)) {
            ret = getGameData(jsonMap);
        } else if (jsonMap.has(UID_SIGNIN)) {
            ret = uidSignin(jsonMap);
        }
        this.sendJSON(ret, he);
    }

    private JSONObject addGameInfo(JSONObject ret) {
        int gameListSize = gamehandler.getGameList().size();
        Object[] gameIdArray = new Object[gameListSize];
        Object[] gameNameArray = new Object[gameListSize];
        Integer[] gamePlayerCountsArray = new Integer[gameListSize];
        int i = 0;
        for (Game g : gamehandler.getGameList().values()) {
            gameIdArray[i] = (String) g.getID();
            gameNameArray[i] = g.getName();
            gamePlayerCountsArray[i] = g.getPlayers().length;
            i++;
        }
        ret.put("gameIds", gameIdArray);
        ret.put("gameNames", gameNameArray);
        ret.put("gamePlayerCounts", gamePlayerCountsArray);
        return ret;
    }

    private String getGameID(String gameName) {
        for (Game g : gamehandler.getGameList().values()) {
            if (gameName.equals(g.getName())) {
                return (String) g.getID();
            }
        }
        return "";
    }

    private JSONObject signup(JSONObject jsonMap) {
        JSONObject ret = new JSONObject();
        JSONObject signup = jsonMap.getJSONObject(USER_SIGNUP);
        String name = signup.getString(NAME_STRING);
        String pwd = signup.getString(PWD_STRING);
        System.out.println("lobbyHandler received (signup): " + name + ", " + pwd);
        if (addUser(name, pwd) == 1) {
            ret.put(AUTH_STRING, true);
        } else {
            ret.put(AUTH_STRING, false);
        }
        return ret;
    }

    private JSONObject login(JSONObject jsonMap) {
        JSONObject ret = new JSONObject();
        //          format of login_string {login: {name: ______, password: _____}}
        //          therefore we need to unpack to JSONObject
        JSONObject login = jsonMap.getJSONObject(LOGIN_STRING);
        String name = login.getString(NAME_STRING);
        String pwd = login.getString(PWD_STRING);
        System.out.println("lobbyHandler received: " + name + ", " + pwd);
        String uid = checkUser(name, pwd); // name
        if (!uid.equals("")) {
            ret.put(AUTH_STRING, true);
            ret = addGameInfo(ret);
            ret.put("uid", uid);
        } else {
            ret.put(AUTH_STRING, false);
        }
        return ret;
    }

    private JSONObject getGameData(JSONObject jsonMap) {
        JSONObject ret = new JSONObject();
        //System.out.println("lobbyHandler received In GAME_DATA: ");
        JSONObject gameData = jsonMap.getJSONObject(GAME_DATA);
        
        String uid = "";
        if (gameData.has("uid")) {
                uid = gameData.getString("uid");
        }
        String gameName = "";
        if (gameData.has("gameName")) {
            gameName = gameData.getString("gameName");
        }
        String requestType = gameData.getString("request");

        if (requestType.equals(NEW_GAME_STRING)) {
            //Check if there already is a game with that name (if so, just go to that game):
            String gameId = getGameID(gameName);
            if (gameId.equals("")) {//the game doesn't exist yet
                gameId = gameName;//UUID.randomUUID().toString();
                System.out.println("adding game: " + gamehandler.addGame(gameId, gameName));
            }
            ret.put("gameId", gameId);

        } else if (requestType.equals(JOIN_GAME_STRING)) {
            System.out.println("lobbyHandler received In JOIN_GAME_STRING: ");
            ret.put("gameState", "you've joined a game!!");
        } else if (requestType.equals(REQ_GAMELIST_STRING)) {
           // System.out.println("lobbyHandler received In REQ_GAMELIST_STRING: ");
            ret = addGameInfo(ret);
        }
        return ret;
    }

    private JSONObject uidSignin(JSONObject jsonMap) {
        JSONObject ret = new JSONObject();
        JSONObject login = jsonMap.getJSONObject(UID_SIGNIN);
        String uid = login.getString("uid");
        System.out.println("Cookie: " + uid);
        String name = checkUser(uid);// (name.equals("Eli") && pwd.equals("b"));
        if (!name.equals("")) {
            ret.put(AUTH_STRING, true);
            ret = addGameInfo(ret);
            ret.put("uid", uid);
        } else {
            ret.put(AUTH_STRING, false);
        }
        return ret;
    }

    private static Connection getConnection() {

        Connection con = null;
        try {
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost/setgame", "root", "betamobile");
            if (con.isClosed()) {
                System.out.println("mySQL is closed");
            }
        } catch (Exception e) {
            System.out.println("could not connect to mySQL");
            System.err.println(e);

        }
        return con;
    }

    private String checkUser(String name, String password) {
        Connection c = getConnection();
        PreparedStatement stmt = null;
        String uid = "";
        try {
            String sql = "SELECT * FROM Users WHERE name=? AND password=?;";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                if (name.equals(rs.getString("name"))) {
                    uid = rs.getString("uid");
                }
            }
            rs.close();
            stmt.close();
            c.close();
            if (count == 1)//count is 1 to help make sure there's no funny bussiness of getting all the rows 
            {
                return uid;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String checkUser(String uid) {
        Connection c = getConnection();
        PreparedStatement stmt = null;
        String name = "";
        try {
            String sql = "SELECT * FROM Users WHERE uid=?;";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, uid);
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                name = rs.getString("name");
            }
            rs.close();
            stmt.close();
            c.close();
            if (count == 1)//count is 1 to help make sure there's no funny bussiness of getting all the rows 
            {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @return 1 on success and -1 on fail (b/c the name is already in there),
     * or -2 for other fail, -3 on bad name/passwords
     */
    private int addUser(String name, String password) {
        if (name.length() < 1 || password.length() < 1) {
            return -3;
        }
        //password = MD5;
        Connection c = getConnection();
        PreparedStatement stmt = null;
        String uid = UUID.randomUUID().toString();
        try {
            String sql = "INSERT INTO Users(name, password,uid) VALUES(?,?,?);";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, uid);
            int returnVal = stmt.executeUpdate();
            stmt.close();
            c.close();
            return returnVal;
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }

    }

    private void createTables() {
        Connection c = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql = null;
        System.out.println("Creating Tables");
        try {
            stmt = c.createStatement();
            sql = "DROP TABLE IF EXISTS Users; "//	 +			" DROP TABLE Games IF EXISTS;"
                    //+ "DROP TABLE IF EXISTS Games; "
                    ;
            stmt.execute(sql);
            sql = "CREATE TABLE Users ("
                    + //"uid int NOT NULL AUTO_INCREMENT," +
                    "name CHAR(" + maxNameSize + "),"
                    + "password TEXT,"
                    + "uid TEXT,"
                    + //"ssn CHAR(10)," +//don't need it, but if they give us it, why wouldn't we store it.
                    "PRIMARY KEY(name)"
                    + //"UNIQUE (name)" +
                    ");";

            stmt.execute(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        System.out.println("FINISHED Creating Tables");

    }
}
