/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

/**
 *
 * @author EliFriedman
 */
public abstract class ResponseHandler implements HttpHandler {

    private byte buf[];
    private final int BUF_SIZE = 2048;
    private final JSONObject nullJSON = new JSONObject();

    public ResponseHandler() {
        buf = new byte[BUF_SIZE];
    }

    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
        System.out.println("super");
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        JSONObject jsonMap;
        String data = "{}";
        switch (he.getRequestMethod()) {
            case "GET": {
                data = he.getRequestURI().toString();
                int ind = data.indexOf('?') + 1;
                if (ind > 0) {
                    data = data.substring(ind);
                } else {
                    this.sendJSON(nullJSON, he);
                    return;
                }
            }
            case "POST": {
                data = readRequest(he);
            }
        }
        try {
            jsonMap = new JSONObject(data);
            this.handleRequest(jsonMap, he);
        } catch (JSONException jse) {
            this.sendJSON(nullJSON, he);
            jse.printStackTrace();
        }
    }

    public String readRequest(HttpExchange he) throws IOException {
        int len = he.getRequestBody().read(buf);
        String request = "";
        if (len > 0 && len < BUF_SIZE) {
            request = new String(buf, 0, len);
        }
        return request;
    }

    public void sendJSON(JSONObject json,HttpExchange he)  {
        this.sendJSON(json.toString(), he);
    }
    public void sendJSON(String json, HttpExchange he) {
        try {
            he.getResponseHeaders().set("Date", (new Date()).toString());
            he.getResponseHeaders().set("Content-type", "application/json");
            he.getResponseHeaders().set("Content-length", Long.toString(json.length()));
            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.length());
            he.getResponseBody().write(json.getBytes());
            he.getResponseBody().close();
        } catch (IOException ex) {
            Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendText(String text, HttpExchange he) {
        try {
            he.getResponseHeaders().set("Date", (new Date()).toString());
            he.getResponseHeaders().set("Content-type", "text/html");
            he.getResponseHeaders().set("Content-length", Long.toString(text.length()));
            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, text.length());
            he.getResponseBody().write(text.getBytes());
            he.getResponseBody().close();
        } catch (IOException ex) {
            Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public byte[] sha1(String input) {
	try { 
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		return mDigest.digest(input.getBytes());
	} catch (NoSuchAlgorithmException nsae) {
		Logger.getLogger(ResponseHandler.class.getName()).log(Level.SEVERE, null, nsae);
	}
        return new byte[0]; 
    }
}
