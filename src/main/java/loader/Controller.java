package loader;

import javafx.concurrent.Service;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import loader.models.Cache;
import loader.models.Remote;
import loader.services.Synchronise;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Label labelDownload;
    public ProgressBar progressBarDownload;

    String remoteURL = "https://1drv.ms/u/s!AiQM7sVIv7faluI23hd4FbCaSdE3SA?e=PfVayy";
    String cacheDir = "USERLIB/ParticleTrieur";
    String mainJar = "ParticleTrieur.jar";
    String mainClass = "ordervschaos.particletrieur.app.App";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Cache cache = new Cache(oneDriveURL, cacheDir, mainJar, mainClass);

    }

    public void synchroniseAndRun() {
        // Start cache
        Cache cache = new Cache(this.cacheDir);
        try {
            cache.initialise();
        } catch (IOException e) {
            e.printStackTrace();
            showError("IO error opening local cache", e.getLocalizedMessage());
            return;
        }

        // Synchronise
        Remote remote = new Remote(remoteURL);
        Service service = Synchronise.syncService(cache, remote);
        labelDownload.textProperty().bind(service.messageProperty());
        progressBarDownload.progressProperty().bind(service.progressProperty());
        // Fatal error
        service.setOnFailed(event -> {
            Exception e = new Exception(service.getException());
            showError("Could not synchronise with remote", e.getLocalizedMessage());
        });
        service.setOnSucceeded(event -> {

        });
        service.start();
    }

    public void showError(String description, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(description);
        alert.getButtonTypes().addAll(ButtonType.CLOSE);
        TextArea textArea = new TextArea();
        textArea.setText(message);
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }
}
