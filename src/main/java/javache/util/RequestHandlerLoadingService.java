package javache.util;

import javache.WebConstants;
import javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class RequestHandlerLoadingService {

    private static final String LIB_FOLDER_PATH =
            WebConstants.SERVER_ROOT_PATH + "lib";

    private Set<RequestHandler> requestHandlers;

    public RequestHandlerLoadingService() {
    }

    public void loadRequestHandlers(Set<String> requestHandlerPriority) throws IOException {
        this.requestHandlers = new LinkedHashSet<>();
        try {
            this.loadLibraryFile(requestHandlerPriority);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private String getFileNameWithoutExtension(File file) {
        return file.getName()
                .substring(0, file.getName().indexOf("."));
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }

    private void loadRequestHandler(Class<?> currentClassFile) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RequestHandler requestHandlerObject =
                (RequestHandler)
                        currentClassFile.getDeclaredConstructor()
                                .newInstance();
        this.requestHandlers.add(requestHandlerObject);
    }

    private void loadLibraryFile(Set<String> requestHandlerPriority) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        File libraryFolder = new File(LIB_FOLDER_PATH);
        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException
                    ("Library folder does not a folder or library folder is not exist");
        }
        List<File> allJarFiles = Arrays.stream(libraryFolder.listFiles())
                .filter(x -> isJarFile(x))
                .collect(Collectors.toList());

        for (String currentRequestHandlerName : requestHandlerPriority) {
            File jarFile = allJarFiles.stream()
                    .filter(x -> this.getFileNameWithoutExtension(x)
                            .equals(currentRequestHandlerName))
                    .findFirst()
                    .orElse(null);
            if (jarFile != null) {
                JarFile fileIsJar = new JarFile(jarFile.getCanonicalPath());
                this.loadJarFile(fileIsJar, jarFile.getCanonicalPath());
            }
        }
    }

    private void loadJarFile(JarFile jarFile, String canonicalPath) throws MalformedURLException,
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();
            if (!currentEntry.isDirectory() && currentEntry.getRealName()
                    .endsWith(".class")) {
                URL[] urls = new URL[]{new URL("jar:file:" + canonicalPath
                        + "!/")};
                URLClassLoader ucl = new URLClassLoader(urls);

                String className = currentEntry
                        .getRealName()
                        .replace(".class", "")
                        .replace("/", ".");
                Class currentClassFile = ucl.loadClass(className);
                if (RequestHandler.class.isAssignableFrom(currentClassFile)){
                     this.loadRequestHandler(currentClassFile);
                }
            }
        }
    }

    public Set<RequestHandler> getRequestHandlers() {

        return Collections.unmodifiableSet(this.requestHandlers);
    }

}




