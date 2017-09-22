package levelup2.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Shamelessly lifted from AgriCore, modified because we're only extracting like five files and not searching for any.
 * @author RlonRyan
 */
public class JsonTransfer {
    public static Set<String> findResources(String filepath, Set<String> fileNames) {
        Set<String> files = new HashSet<>();
        for (String name : fileNames) {
            String str = filepath + "/" + name + ".json";
            files.add(str);
        }
        return files;
    }

    public static void copyResource(String from, Path to, boolean overwrite) {
        try {
            if (overwrite || !Files.exists(to)) {
                Files.createDirectories(to.getParent());
                Files.copy(getResourcesAsStream(from), to, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream getResourcesAsStream(String location) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + location);
        return in != null ? in : JsonTransfer.class.getResourceAsStream("/" + location);
    }
}
