/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.HttpExchange;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author EliFriedman
 */
public class SocketHandler extends ResponseHandler {

    public SocketHandler() {

    }

    @Override
    public void handle(HttpExchange he) throws IOException {
	String key = he.getRequestHeaders().getFirst("Sec-WebSocket-Key");
	key = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	key = DatatypeConverter.printBase64Binary(sha1(key));
        he.getResponseHeaders().set("Sec-WebSocket-Accept", key);
        he.getResponseHeaders().set("Upgrade", "WebSocket");
        he.getResponseHeaders().set("Connection", "Upgrade");
        he.sendResponseHeaders(101, -1);

	while(he.getRequestBody().available() < 0);
        String data1 = readRequest(he);
	System.out.println("WebSocket read:" + Arrays.toString(data1.getBytes()) + "]]");
	he.getResponseBody().write("Hello".getBytes());
	he.getResponseBody().close();
    }
}
