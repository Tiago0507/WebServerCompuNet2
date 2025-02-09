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
        //var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //var out = new BufferedOutputStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

        String lineRequest = in.readLine();
        if (lineRequest == null) {
            return;
        }

        StringTokenizer tokens = new StringTokenizer(lineRequest);
        String method = tokens.nextToken(); // GET, POST, etc.
        String fileName = tokens.nextToken();

        if (!method.equals("GET")) {
            sendString("HTTP/1.0 405 Method Not Allowed" + CRLF, out);
            out.flush();
            socket.close();
            return;
        }

        // Si el archivo solicitado es "/", cargar index.html
        if (fileName.equals("/")) {
            fileName = "index.html";
        } else {
            fileName = fileName.substring(1); // Eliminar la barra inicial "/"
        }

        // Imprimir headers
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            System.out.println("Header: " + line);
        }

        // Obtener el archivo desde resources
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(fileName);
        if (inputStream != null) {
            File file = new File(ClassLoader.getSystemResource(fileName).toURI());
            long fileSize = file.length();

            sendString("HTTP/1.0 200 OK" + CRLF, out);
            sendString("Content-Type: " + contentType(fileName) + CRLF, out);
            sendString("Content-Length: " + fileSize + CRLF, out);
            sendString(CRLF, out); // Línea vacía entre headers y cuerpo

            // Enviar contenido del archivo
            sendBytes(inputStream, out);
        } else {
            InputStream errorPage = ClassLoader.getSystemResourceAsStream("404.html");
            sendString("HTTP/1.0 404 Not Found" + CRLF, out);
            sendString("Content-Type: text/html" + CRLF, out);
            sendString(CRLF, out);

            sendBytes(errorPage, out);
        }

        out.flush();
        in.close();
        out.close();
        socket.close();
    }

    private static void sendString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
        System.out.println(line);
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

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
