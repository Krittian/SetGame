/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 *
 * @author EliFriedman
 */
public class SocketHandler extends ResponseHandler {

    public SocketHandler() {

    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        String data = readRequest(he);
        System.out.println(data);
        he.getResponseHeaders().set("Upgrade:", "websocket");
        he.getResponseHeaders().set("Connection:", "Upgrade");
        he.sendResponseHeaders(101, 1);
    }
}
