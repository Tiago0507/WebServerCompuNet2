package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

final public class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        var out = new BufferedOutputStream(socket.getOutputStream());

        String lineRequest = in.readLine();
        System.out.println("\nSolicitud recibida:");
        System.out.println(lineRequest);
        if (lineRequest == null) {
            return;
        }

        StringTokenizer tokens = new StringTokenizer(lineRequest);
        String method = tokens.nextToken(); // Método (GET, POST, etc.)
        String fileName = tokens.nextToken();

        if (fileName.equals("/")) {
            fileName = "index.html";
        } else {
            fileName = fileName.substring(1); // Eliminar la barra inicial "/"
        }

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            System.out.println("Header: " + line);
        }

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        if(inputStream != null) {
            File file = new File(ClassLoader.getSystemResource(fileName).toURI());
            long fileSize = file.length();

        }else {

        }
        sendString("HTTP/1.0 200 OK" + CRLF, out);
        sendString("Content-Type: text/html" + CRLF, out);
        String response = "HTTP/1.0 200 OK" + CRLF +
                "Content-Length: 13" + CRLF +
                CRLF +
                "<html><head></head><body><h1>Hola</h1></body></html";

        in.close();
        out.close();
        socket.close();
    }

    private static void sendString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(InputStream fis, OutputStream os) throws Exception {
        // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copia el archivo solicitado hacia el output stream del socket.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }
}
