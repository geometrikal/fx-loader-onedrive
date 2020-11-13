package loader.models;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class Remote {

    public String remoteURL;
    public String manifest;
    public SyncFileInfoCollection resourceCollection;

    public Remote(String remoteURL) {
        this.remoteURL = remoteURL;
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

    public String getManifest() throws IOException {
        URL url = new URL(convertShareURLtoAPIURL(this.remoteURL));
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
        manifest = inline;
    }

    public void downloadFile(SyncFileInfo resource, Path cacheDir, BiConsumer<Long, Long> updateProgress) throws IOException, URISyntaxException {
        Path target = cacheDir.resolve(resource.name).toAbsolutePath();
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
}
