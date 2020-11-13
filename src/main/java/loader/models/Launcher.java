package loader.models;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;

public class Launcher {

    public String jarFilename;
    public String mainClass;

    public Launcher(String jarFilename, String mainClass) {
        this.jarFilename = jarFilename;
        this.mainClass = mainClass;
    }

    public void startApplication(SyncFileInfoCollection resourceCollection, Path cacheDir, Stage primaryStage) throws Exception {
        ArrayList<URL> libs = new ArrayList<>();
        libs.add(cacheDir.resolve(this.jarFilename).toUri().toURL());
        ClassLoader classLoader = new URLClassLoader(libs.toArray(new URL[libs.size()]));
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
