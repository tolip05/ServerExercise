package javache.util;

import javache.WebConstants;
import javache.io.Reader;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JavacheConfigService {

    private static final String CONFIG_FOLDER_PATH =
            WebConstants.SERVER_ROOT_PATH +"config";

    private static final String REQUEST_HANDLER_PRIORITY_FOLDER_PATH =
            CONFIG_FOLDER_PATH + "/config.ini";

    private Set<String> requestHandlerPriority;

    public JavacheConfigService() {
        this.initConfiguration();

    }

    private void initConfiguration() {
        try{
            this.loadRequestHandlerConfig();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadRequestHandlerConfig() throws IOException {
        File priorityConfigFile =
                new File(REQUEST_HANDLER_PRIORITY_FOLDER_PATH);

        if (!priorityConfigFile.exists() || !priorityConfigFile.isFile()){
            throw new IllegalArgumentException("Request Handler priority" +
                    " configuration file does not exist!");
        }
        String configFileContent =
                Reader.readAllLines(new BufferedInputStream(
                        new FileInputStream(priorityConfigFile))
                );
        this.requestHandlerPriority =
                Arrays.stream(configFileContent
                        .replace("request-handlers:","")
                        .split(","))
                .collect(Collectors.toSet());
    }

    public Set<String> getRequestHandlerPriority() {
        return Collections.unmodifiableSet(this.requestHandlerPriority);
    }
}
