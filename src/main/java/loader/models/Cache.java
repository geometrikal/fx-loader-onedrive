package loader.models;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;

public class Cache {

    // Path to the OneDrive folder
    public String oneDriveURL;
    // Cache code
    public String cacheCode;
    //
    public String mainJar;
    public String mainClass;
    // Path to the local cache directory
    Path cacheDir;
    // Get local resources
    OnedriveResourceCollection localResources;
    // Get remote resources
    OnedriveResourceCollection remoteResources;

    public Cache(String oneDriveURL, String cacheCode, String mainJar, String mainClass) {
        this.oneDriveURL = oneDriveURL;
        this.cacheCode = cacheCode;
        this.mainJar = mainJar;
        this.mainClass = mainClass;
    }

    public void createOrResolveCacheDir() {
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

    public OnedriveResourceCollection parseResourceManifest(String manifest) throws ParseException {
        OnedriveResourceCollection resources = new OnedriveResourceCollection();
        JSONParser parse = new JSONParser();
        JSONObject base = (JSONObject) parse.parse(manifest);
        JSONArray arr = (JSONArray) base.get("children");
        for (Object a : arr) {
            JSONObject o = (JSONObject) a;
            OnedriveResource resource = new OnedriveResource(
                    (String) o.get("eTag"),
                    (String) o.get("name"),
                    (long) o.get("size"),
                    (String) o.get("webUrl"));
            resources.addResource(resource);
        }
        return resources;
    }

    public String convertShareURLtoDownloadURL(String shareURL) {
        shareURL = shareURL.replace("https://1drv.ms/", "https://1drv.ws/");
        shareURL = shareURL.split("\\?")[0];
        return shareURL;
    }


    public String convertShareURLtoAPIURL(String shareURL) {
        shareURL = shareURL.split("\\?")[0];
        shareURL = Base64.getEncoder().encodeToString(shareURL.getBytes());
        String APIURL = "https://api.onedrive.com/v1.0/shares/u!" + shareURL + "/root?expand=children";
        return APIURL;
    }

    public String getLocalManifest() throws IOException {
        Path localPath = this.cacheDir.resolve("manifest.json");
        if (Files.exists(localPath)) {
            String content = new String(Files.readAllBytes(localPath));
            return content;
        }
        return null;
    }

    public void writeLocalManifest(String localManifest) throws IOException {
        Path localPath = this.cacheDir.resolve("manifest.json");
        Files.write(localPath, localManifest.getBytes());
    }

    public String getRemoteManifest() {
        try {
            URL url = new URL(convertShareURLtoAPIURL(this.oneDriveURL));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responsecode = conn.getResponseCode();

            String inline = "";
            Scanner scanner = new Scanner(url.openStream());

            //Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }
            //Close the scanner
            scanner.close();
            return inline;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void downloadResource(OnedriveResource resource, BiConsumer<Long, Long> updateProgress) throws IOException, URISyntaxException {
        Path target = this.cacheDir.resolve(resource.name).toAbsolutePath();
        URI uri = new URI(convertShareURLtoDownloadURL(resource.shareURL));
        try (InputStream input = openDownloadStream(uri); OutputStream output = Files.newOutputStream(target)) {
            byte[] buf = new byte[65536];
            int read;
            long totalWritten = 0;
            System.out.println(uri);
            while ((read = input.read(buf)) > -1) {
                output.write(buf, 0, read);
                totalWritten += read;
                updateProgress.accept(totalWritten, resource.size);
            }
        }
    }

    public InputStream openDownloadStream(URI uri) throws IOException {
        if (uri.getScheme().equals("file")) return Files.newInputStream(new File(uri.getPath()).toPath());
        URLConnection connection = uri.toURL().openConnection();
        if (uri.getUserInfo() != null) {
            byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(payload);
            connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
        }
        return connection.getInputStream();
    }

    public void startApplication(OnedriveResourceCollection resourceCollection, Stage primaryStage) throws Exception {
        ArrayList<URL> libs = new ArrayList<>();
        libs.add(this.cacheDir.resolve(this.mainJar).toUri().toURL());
//        libs.add(this.cacheDir.resolve("controlsfx.jar").toUri().toURL());
        ClassLoader classLoader = new URLClassLoader(libs.toArray(new URL[libs.size()]));
//        Thread.currentThread().setContextClassLoader(classLoader);
        FXMLLoader.setDefaultClassLoader(classLoader);
        Platform.runLater(() -> Thread.currentThread().setContextClassLoader(classLoader));
        Class<Application> appclass = (Class<Application>) classLoader.loadClass(this.mainClass);
        Application app = appclass.newInstance();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                app.init();
                app.start(stage);
                primaryStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
