package loader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    private static Main instance;
    public static Main getInstance() {
        return instance;
    }

    public Stage stage;



    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        this.stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/loader.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
