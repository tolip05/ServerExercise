package javache;

public final class WebConstants {

    public static final Integer DEFAULT_SERVER_PORT = 8000;

    public static final String SERVER_HTTP_VERSION = "HTTP/1.1";


    public static final String SERVER_ROOT_PATH =
            Server.class.getResource("").toString()
                    .replace("javache/","")
                    .replace("file:/","");

    private WebConstants() { }
}
