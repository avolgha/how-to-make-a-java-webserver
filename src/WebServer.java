import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer implements Runnable {
    static final String REGEX_URL_SPLIT = "/";

    // The port of the local HTTP Server. Change it to "80" if you want to connect to the server without specifying the port everytime
    static final int PORT = 8080;

    // Should output logs
    static final boolean verbose = true;

    // The server socket. Don't change this if you don't know what you're doing
    private final Socket socket;

    public WebServer(Socket socket) {
        this.socket = socket;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        try {
            // Make a server socket that listens on the specified port
            ServerSocket serverSocket = new ServerSocket(WebServer.PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                // Create a web server that listens on a request that is going into the server socket
                WebServer server = new WebServer(serverSocket.accept());

                // If any connection is there, print the date of the connection
                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                // Now create the runnable that executes the good code
                new Thread(server).start();
            }
        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    static void sendHtml(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String content) throws IOException {
        // send HTML code as response
        write(headerWriter, contentWriter, statusCode, "text/html", content.getBytes(StandardCharsets.UTF_8), content.length());
    }

    static void sendPlain(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String content) throws IOException {
        // send plain text as response
        write(headerWriter, contentWriter, statusCode, "text/plain", content.getBytes(StandardCharsets.UTF_8), content.length());
    }

    // TODO: Check if JSON is valid. If not print error message
    static void sendJson(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String json) throws IOException {
        // send json as response
        write(headerWriter, contentWriter, statusCode, "application/json", json.getBytes(StandardCharsets.UTF_8), json.length());
    }

    static void write(PrintWriter headerWriter, BufferedOutputStream contentWriter, int statusCode, String contentType, byte[] response, int responseLength) throws IOException {
        // write a plain request as response. All have to be setup in the methods parameters

        // Fetch the StatusCode from the integer
        HttpStatusCode httpStatusCode = HttpStatusCode.getByResult(statusCode);

        // Write the headers of the response
        headerWriter.println(String.format("HTTP/1.1 %d %s", statusCode, httpStatusCode == null ? "Unknown" : httpStatusCode.name()));
        headerWriter.println("Server: HTTP Server : 1.0");
        headerWriter.println("Date: " + new Date());
        headerWriter.println("Content-type: " + contentType);
        headerWriter.println("Content-length: " + responseLength);
        headerWriter.println();
        headerWriter.flush();

        // Write the content of the response
        contentWriter.write(response, 0, responseLength);
        contentWriter.flush();
    }

    @Override
    public void run() {
        // Get all important Streams for outputting the data
        try (BufferedReader       requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter          headerWriter  = new PrintWriter(socket.getOutputStream());
             BufferedOutputStream contentWriter = new BufferedOutputStream(socket.getOutputStream())) {

            // Fetch the response
            StringTokenizer parse = new StringTokenizer(requestReader.readLine());
            // Get the HTTP Method that was used to call the server
            String method = parse.nextToken().toUpperCase();
            // Get the requested path that was used at calling the server
            String requested = parse.nextToken().toLowerCase();

            // Check if the incoming request is a GET request
            if (!method.equals("GET")) {
                if (verbose) {
                    System.out.println("501 Not implemented : " + method + " method.");
                }

                // Send a message that the user uses the wrong method at this server
                sendJson(headerWriter, contentWriter, 501, "{\"error\":\"Method not implemented. Please use GET instead\"}");
            } else {
                // Get the path that was called
                // If "https://example.com/any/thing" gets called, returns an array of "any" and "thing"
                String[] urlSplit = requested.split(WebServer.REGEX_URL_SPLIT);
                // Here goes your code
                // [...]
            }
        } catch (IOException exception) {
            // Any error on this server occurred. That is bad...
            System.err.println("Server error : " + exception);
        } finally {
            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }
    }
}
