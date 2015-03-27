/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author EliFriedman
 */
public class WebServer2 {

    private HttpServer server;
    private HttpHandler fileHandler;

    public void initializeServer() throws IOException {
        Properties props = new Properties();
        ExecutorService executors = Executors.newCachedThreadPool();
        try {
            System.out.println(InetAddress.getLocalHost());
            InetSocketAddress address = new InetSocketAddress(props.getPort());
            server = HttpServer.create(address, props.getWorkers());
//            server.setExecutor(executors);
            fileHandler = new FileHandler(props);
            server.createContext("/", fileHandler);
        } catch (UnknownHostException ex) {
            props.log(ex.toString());
        }
    }

    public void start() {
        server.start();
    }

    public void addService(String path, HttpHandler service) {
        server.createContext(path, service);
    }

    public static void main(String args[]) throws IOException {
        WebServer2 ws = new WebServer2();
        ws.initializeServer();
        GameHandler g = new GameHandler();
        ws.addService("/gameDATA", g);
        ws.addService("/lobbyDATA", new LobbyHandler(g));
        ws.start();
//        Game g = new Game();
    }
}

class FileHandler implements HttpHandler {

    private byte[] buf;
    private final Properties props;

    FileHandler(Properties props) {
        this.props = props;
        props.printProps();
        this.buf = new byte[256];
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        String root = props.getRoot().getAbsolutePath();
        if (he.getRequestMethod().equalsIgnoreCase("GET")
                || he.getRequestMethod().equalsIgnoreCase("POST")) {
            String path = he.getRequestURI().getPath().replaceAll("\\.\\.", "");
            File f = new File(root + path);

            if (f.isDirectory()) {
                File ind = new File(f, "index.html");
                f = ind;
            }
            props.log(f.getAbsolutePath());

            printFileHeaders(f, he);
            try (OutputStream responseBody = he.getResponseBody()) {
                sendFile(f, responseBody);
                props.log("sent file");
            }
        }
    }

    void sendFile(File f, OutputStream os) throws IOException {
        try (FileInputStream is = new FileInputStream(f.getAbsolutePath())) {
            int n;
            while ((n = is.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
        }
    }

    private boolean printFileHeaders(File targ, HttpExchange he) throws IOException {
        Headers header = he.getResponseHeaders();
        boolean ret;
        int rCode;
        if (!targ.exists()) {
            rCode = HttpURLConnection.HTTP_NOT_FOUND;
            props.log(targ + " not found.");
            ret = false;
        } else {
            rCode = HttpURLConnection.HTTP_OK;
            ret = true;
        }
        header.set("Date", (new Date()).toString());
        if (ret) {
            if (!targ.isDirectory()) {
                header.set("Content-length", Long.toString(targ.length()));
                header.set("Last-Modified", (new Date(targ.lastModified())).toString());
                String name = targ.getName();
                int ind = name.lastIndexOf('.');

                String ct = null;
                if (ind > 0 && name.length() >= ind+1) {
                    String ext = name.substring(ind+1);
                    switch(ext) {
                        case "pdf":
                            ct = "application/pdf";
                            break;
                        case "bmp":
                            ct = "image/bmp";
                            break;
                        case "css":
                            ct = "text/css";
                            break;
                        case "csv":
                            ct = "text/csv";
                            break;
                        case "html":
                            ct = "text/html";
                            break;
                        case "js":
                            ct = "application/javascript";
                            break;
                        case "json":
                            ct = "application/json";
                            break;
                        case "jpg":
                        case "jpeg":
                            ct = "image/jpeg";
                            break;
                        case "doc":
                            ct = "application/msword";
                            break;
                        case "mpeg":
                            ct = "video/mpeg";
                            break;
                        case "mp4":
                            ct = "application/mp4";
                            break;
                        case "png":
                            ct = "image/png";
                            break;
                        case "xhtml":
                            ct = "application/xhtml+xml";
                            break;
                        case "xml": 
                            ct = "application/xml";
                            break;
                        case "zip":
                            ct = "application/zip";
                            break;
                    }
                }
                if (ct == null) {
                    ct = "unknown/unknown";
                }
                header.set("Content-type", ct);
            } else {
                header.set("Content-type", "text/html");
            }
        }
        he.sendResponseHeaders(rCode, 0);
        if (!ret) {
            he.getResponseBody().write(("<html><body><h1>404 Not Found</h1> "
                    + "The requested resource was not found.</body></html>").getBytes());
            he.getResponseBody().close();
        }
        return ret;
    }
}
