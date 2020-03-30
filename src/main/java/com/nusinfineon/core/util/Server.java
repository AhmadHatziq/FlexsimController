package com.nusinfineon.core.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.nusinfineon.core.RunCore;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(RunCore.class.getName());

    //initialize socket and input stream
    private Socket socket = null;
    private ServerSocket server = null;
    private int port;

    // constructor with port
    public Server(int port) {
        this.port = port;
    }

    public void checkForConnection() {
        try {
            // starts server and waits for a connection
            server = new ServerSocket(port);
            LOGGER.info("Server started");

            LOGGER.info("Waiting for FlexSim to finish and connect ...");

            socket = server.accept();
            LOGGER.info("FlexSim connected! Starting next run");

            // close connection
            socket.close();
            server.close();

            TimeUnit.SECONDS.sleep(5);
        } catch(IOException | InterruptedException i) {
            System.out.println(i);
        }
    }
}
