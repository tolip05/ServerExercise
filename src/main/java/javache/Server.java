package javache;

import javache.api.RequestHandler;
import javache.util.InputStreamCachingService;
import javache.util.JavacheConfigService;
import javache.util.RequestHandlerLoadingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.FutureTask;

public class Server {
    private static final String LISTENING_MESSAGE = "Listening on port: ";

    private static final String TIMEOUT_DETECTION_MESSAGE = "Timeout detected!";

    private static final Integer SOCKET_TIMEOUT_MILLISECONDS = 5000;

    private int port;

    private int timeouts;

    private ServerSocket server;

    private JavacheConfigService javacheConfigService;

    private InputStreamCachingService cachingService;

    private RequestHandlerLoadingService requestHandlerLoadingService;



    public Server(int port) throws IOException {
        this.port = port;
        this.timeouts = 0;
        this.javacheConfigService = new JavacheConfigService();
        this.requestHandlerLoadingService = new RequestHandlerLoadingService();
        this.initRequestHandlers();
    }

    private void initRequestHandlers() throws IOException {
        this.requestHandlerLoadingService.loadRequestHandlers(
                this.javacheConfigService.getRequestHandlerPriority()
        );
    }

    public void run() throws IOException {
        this.server = new ServerSocket(this.port);
        System.out.println(LISTENING_MESSAGE + this.port);

        this.server.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

        while(true) {
            try(Socket clientSocket = this.server.accept()) {
                clientSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

                ConnectionHandler connectionHandler
                        = new ConnectionHandler(clientSocket,
                        this.requestHandlerLoadingService
                                .getRequestHandlers(),
                new InputStreamCachingService());

                FutureTask<?> task = new FutureTask<>(connectionHandler, null);
                task.run();
            } catch(SocketTimeoutException e) {
                System.out.println(TIMEOUT_DETECTION_MESSAGE);
                this.timeouts++;
            }
        }
    }
}