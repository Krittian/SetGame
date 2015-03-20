/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author EliFriedman
 */
class Properties {
    /* the web server's virtual root */
    private File root = new File(System.getProperty("user.dir"));
    public File getRoot() { return root; }
    
    /* timeout on client connections */
    private int timeout = 10000;
    public int getTimeout() { return timeout; }
    
    /* max # worker threads */
    private int workers = 5;
    public int getWorkers() { return workers; }
    
    private int port = 8080;
    public int getPort() { return port; }

    private PrintStream log = System.out;

    public Properties() throws IOException {
        File f = new File("props" + File.separator + "www-server.properties");
        if (f.exists()) {
            java.util.Properties props = null;
            try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
                props = new java.util.Properties();
                props.load(is);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            }
            String r = props.getProperty("root");
            if (r != null) {
                root = new File(r);
            }

            r = props.getProperty("timeout", "5000");
            if (r != null) {
                timeout = Integer.parseInt(r);
            }
            r = props.getProperty("workers", "5");
            if (r != null) {
                workers = Integer.parseInt(r);
            }
            r = props.getProperty("port", "8080");
            if (r != null) {
                port = Integer.parseInt(r);
            }
            r = props.getProperty("log");
            if (r != null) {
                r = File.separator + r;
                File logfile = new File(root, r);
                if (logfile.exists()) {
                    p("opening log file: " + logfile);
                    log = new PrintStream(new BufferedOutputStream(
                            new FileOutputStream(logfile)));
                } else {
                    p("logging to standard out");
                    log = System.out;
                }
            }
        }
    }
    

    void printProps() {
        p("root=" + root);
        p("timeout=" + timeout);
        p("workers=" + workers);
        p("port=" + port);
    }

    /* print to stdout */
    protected void p(String s) {
        System.out.println(s);
    }

    /* print to the log file */
    protected void log(String s) {
        synchronized (log) {
            log.println(s);
            log.flush();
        }
    }
}
