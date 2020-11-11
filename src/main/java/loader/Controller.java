package loader;

import javafx.concurrent.Service;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import loader.models.Cache;
import loader.services.Loader;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public Label labelDownload;
    public ProgressBar progressBarDownload;

    String oneDriveURL = "https://1drv.ms/u/s!AiQM7sVIv7faluI23hd4FbCaSdE3SA?e=PfVayy";
    String cacheDir = "USERLIB/ParticleTrieur";
    String mainJar = "ParticleTrieur.jar";
    String mainClass = "ordervschaos.particletrieur.app.App";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Cache cache = new Cache(oneDriveURL, cacheDir, mainJar, mainClass);
        Service service = Loader.syncService(cache);

        labelDownload.textProperty().bind(service.messageProperty());
        progressBarDownload.progressProperty().bind(service.progressProperty());

        service.setOnFailed(event -> {
            (new Exception(service.getException())).printStackTrace();
//            labelDownload.setText((new Exception(service.getException())).getLocalizedMessage());
        });

        service.start();
    }
}
