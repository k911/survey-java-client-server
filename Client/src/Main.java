
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Fill the survey!");
        primaryStage.setScene(new Scene(root, 667.0, 625.0));
        primaryStage.show();

        primaryStage.setResizable(false);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
