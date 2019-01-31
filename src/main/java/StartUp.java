import javache.Server;
import javache.WebConstants;
import javache.util.JavacheConfigService;
import javache.util.RequestHandlerLoadingService;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class StartUp {
    public static void main(String[] args) throws IOException {
      start(args);

    }

    private static void start(String[] args) throws IOException {
        int port = WebConstants.DEFAULT_SERVER_PORT;

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        Server server = new Server(port);

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

