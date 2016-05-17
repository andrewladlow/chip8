package chip8;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Display extends Application {
    private DisplayController displayController;
       

    public void start(Stage stage) throws Exception {    
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Root.fxml"));
        
        displayController = new DisplayController();
        loader.setController(displayController);
        
        BorderPane root = (BorderPane) loader.load();
        displayController.setup();

        Scene scene = new Scene(root);
        stage.setTitle("CHIP8");
        stage.setScene(scene);
        stage.show();  
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
