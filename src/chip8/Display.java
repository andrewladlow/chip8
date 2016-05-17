package chip8;


import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
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
    private FXMLLoader fxmlLoader; 
    
    public void start(Stage stage) throws Exception {  
        URL location = getClass().getResource("Display.fxml");
        fxmlLoader = new FXMLLoader();  
        fxmlLoader.setLocation(location);
        BorderPane pane = fxmlLoader.load(location.openStream());

        Scene scene = new Scene(pane);
        stage.setTitle("CHIP-8");
        stage.setScene(scene);
        stage.show();
    }

    public void stop() {
        ((DisplayController) fxmlLoader.getController()).stop();  
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
