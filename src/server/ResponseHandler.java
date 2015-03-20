/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.*;

/**
 *
 * @author EliFriedman
 */
public abstract class ResponseHandler implements HttpHandler {

    private byte buf[];
    private final int BUF_SIZE = 2048;
    private JSONParser parser = new JSONParser();

    private ContainerFactory containerFactory = new ContainerFactory() {
        @Override
        public List creatArrayContainer() {
            return new LinkedList();
        }

        @Override
        public Map createObjectContainer() {
            return new LinkedHashMap();
        }
    };

    public ResponseHandler() {
        buf = new byte[BUF_SIZE];
    }

    public void handleRequest(JSONObject jsonMap, HttpExchange he) {
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        JSONObject jsonMap = null;
        switch (he.getRequestMethod()) {
            case "GET": {
                String data = he.getRequestURI().toString();
                System.out.println(data);
                int ind = data.indexOf('?') + 1;
                if (ind > 0) {
                    data = data.substring(ind);
                } else {
                    return;
                }
                try {
                    jsonMap = (JSONObject)parser.parse(data,this.containerFactory);
                } catch (ParseException pe) {
                    
                }
            }
            case "POST": {
                String data = readRequest(he);
                try {
                    jsonMap = (JSONObject)parser.parse(data,this.containerFactory);
                } catch (ParseException pe) {
                    
                }
            }
        }
        handleRequest(jsonMap, he);
    }

    public String readRequest(HttpExchange he) throws IOException {
        int len = he.getRequestBody().read(buf);
        String request = "";
        if (len > 0 && len < BUF_SIZE) {
            request = new String(buf, 0, len);
        }
        return request;
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
}
