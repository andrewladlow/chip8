package chip8;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Display extends Application {
    private FXMLLoader fxmlLoader;
    private DisplayController controller;
    
    public void start(Stage stage) throws Exception {  
        URL location = this.getClass().getResource("Display.fxml");
        fxmlLoader = new FXMLLoader();  
        fxmlLoader.setLocation(location);
        BorderPane pane = fxmlLoader.load(location.openStream());
        controller = (DisplayController) fxmlLoader.getController();

        Scene scene = new Scene(pane);
        stage.setTitle("CHIP-8");
        stage.setScene(scene);
        stage.show();
        
        controller.init();
    }

    public void stop() {
        controller.stopPool(); 
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
