package org.example;

import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;

        ServerSocket serverSocket = new ServerSocket(port);
        while(true) {
            Socket socket = serverSocket.accept();
            HttpRequest request = new HttpRequest(socket);
            Thread thread = new Thread(request);
            thread.start();
        }
    }
}
