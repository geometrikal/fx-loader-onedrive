package loader.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cache {

    // Cache code
    public String cacheCode;
    // Path to the local cache directory
    public Path cacheDir;
    // Local resources
    public String manifest;
    public SyncFileInfoCollection resourceCollection;

    public Cache(String cacheCode) {
        this.cacheCode = cacheCode;
    }

    public void initialise() throws IOException {
        createOrResolveCacheDir();
        getManifest();
        parseManifest();
    }

    private void createOrResolveCacheDir() {
        Path path;
        if (cacheCode.contains("USERLIB")) {
            String replacement;
            switch (OS.current) {
                case mac:
                    replacement = Paths.get(System.getProperty("user.home"))
                            .resolve("Library")
                            .resolve("Application Support")
                            .resolve(cacheCode.substring(8))
                            .toString();
                    break;
                case win:
                    replacement = Paths.get(System.getProperty("user.home"))
                            .resolve("AppData")
                            .resolve("Local")
                            .resolve(cacheCode.substring(8))
                            .toString();
                    break;
                default:
                    replacement = Paths.get(System.getProperty("user.home"))
                            .resolve("." + cacheCode.substring(8))
                            .toString();
            }
            path = Paths.get(replacement);
        } else if (cacheCode.startsWith("ALLUSERS")) {
            switch (OS.current) {
                case mac:
                    path = Paths.get("/Library/Application Support")
                            .resolve(cacheCode.substring(9));
                    break;
                case win:
                    path = Paths.get(System.getenv("ALLUSERSPROFILE"))
                            .resolve(cacheCode.substring(9));
                    break;
                default:
                    path = Paths.get("/usr/local/share")
                            .resolve(cacheCode.substring(9));
            }
        } else {
            path = Paths.get(cacheCode);
        }
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.cacheDir = path;
    }

    public void parseManifest() {
        if (this.manifest == null) {
            resourceCollection = new SyncFileInfoCollection();
        }
        else {
            try {
                resourceCollection = SyncFileInfoCollection.parseManifest(this.manifest);
            } catch (ParseException e) {
                e.printStackTrace();
                resourceCollection = new SyncFileInfoCollection();
            }
        }
    }

    public String getManifest() throws IOException {
        Path localPath = this.cacheDir.resolve("manifest.json");
        if (Files.exists(localPath)) {
            String content = new String(Files.readAllBytes(localPath));
            return content;
        }
        return null;
    }

    public void writeManifest(String localManifest) throws IOException {
        Path localPath = this.cacheDir.resolve("manifest.json");
        Files.write(localPath, localManifest.getBytes());
    }
}
