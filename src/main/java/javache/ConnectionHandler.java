package javache;



import javache.api.RequestHandler;
import javache.io.Reader;
import javache.io.Writer;
import javache.util.InputStreamCachingService;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

public class ConnectionHandler extends Thread {
    private static final int CONNECTION_KILL_LIMIT = 5000;

    private static final String REQUEST_CONTENT_LOADING_FAILURE_EXCEPTION_MESSAGE = "Failed loading request content.";

    private Socket clientSocket;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private InputStreamCachingService cachingService;

    private Set<RequestHandler> requestHandlers;

    public ConnectionHandler(Socket clientSocket, Set<RequestHandler> requestHandlers
            ,InputStreamCachingService
                                     cachingService) {
        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
        this.cachingService = cachingService;
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClientConnection() throws IOException {
        for (RequestHandler requestHandler : this.requestHandlers) {
            requestHandler.handleRequest(this.cachingService
                    .getOrCacheInputStream(this.clientSocketInputStream),this.clientSocketOutputStream);
            if (requestHandler.hasIntercepted())break;
        }
    }
    @Override
    public void run() {
        try {
            this.processClientConnection();
            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
            this.cachingService.evictCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}






