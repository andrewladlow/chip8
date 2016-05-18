package chip8;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class DisplayController implements Initializable {
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Canvas canvas;
    
    private GraphicsContext gc;
    private PixelWriter pw;
    private CPU chip8CPU;
    private ExecutorService executor = Executors.newFixedThreadPool(2); 
    private int modifier = 8;  
    private boolean running = false;
    
    @FXML
    private void handleRestartAction(ActionEvent ae) {
        System.out.println("Clicked restart");
    }
    
    @FXML
    private void handleAboutAction(ActionEvent ae) {
        System.out.println("Clicked about");
    }   
    
    @FXML
    private void handleKeyPressed(KeyEvent ke) {
        System.out.println(ke.getCode().toString());
        switch (ke.getCode().toString()) {
        case "DIGIT1":
            chip8CPU.setKey(0, 1);
            break;
        case "DIGIT2":
            chip8CPU.setKey(1, 1);        
            break;
        case "DIGIT3":
            chip8CPU.setKey(2, 1);  
            break;
        case "DIGIT4":
            chip8CPU.setKey(3, 1); 
            break;
        case "Q":
            chip8CPU.setKey(4, 1);
            break;
        case "W":
            chip8CPU.setKey(5, 1); 
            break;
        case "E":
            chip8CPU.setKey(6, 1); 
            break;
        case "R":
            chip8CPU.setKey(7, 1); 
            break;
        case "A":
            chip8CPU.setKey(8, 1); 
            break;
        case "S":
            chip8CPU.setKey(9, 1); 
            break;
        case "D":
            chip8CPU.setKey(10, 1);  
            break;
        case "F":
            chip8CPU.setKey(11, 1);
            break;
        case "Z":
            chip8CPU.setKey(12, 1);  
            break;
        case "X":
            chip8CPU.setKey(13, 1);  
            break;
        case "C":
            chip8CPU.setKey(14, 1);
            break;
        case "V":
            chip8CPU.setKey(15, 1); 
            break;
        }
    }
    
    @FXML
    private void handleKeyReleased(KeyEvent ke) {
        
        switch (ke.getCode().toString()) {
        case "DIGIT1":
            chip8CPU.setKey(0, 0);
            break;
        case "DIGIT2":
            chip8CPU.setKey(1, 0);         
            break;
        case "DIGIT3":
            chip8CPU.setKey(2, 0);  
            break;
        case "DIGIT4":
            chip8CPU.setKey(3, 0); 
            break;
        case "Q":
            chip8CPU.setKey(4, 0);
            break;
        case "W":
            chip8CPU.setKey(5, 0); 
            break;
        case "E":
            chip8CPU.setKey(6, 0); 
            break;
        case "R":
            chip8CPU.setKey(7, 0); 
            break;
        case "A":
            chip8CPU.setKey(8, 0); 
            break;
        case "S":
            chip8CPU.setKey(9, 0); 
            break;
        case "D":
            chip8CPU.setKey(10, 0);  
            break;
        case "F":
            chip8CPU.setKey(11, 0);  
            break;
        case "Z":
            chip8CPU.setKey(12, 0);  
            break;
        case "X":
            chip8CPU.setKey(13, 0);  
            break;
        case "C":
            chip8CPU.setKey(14, 0);
            break;
        case "V":
            chip8CPU.setKey(15, 0); 
            break;
        }
    }
    
    public void initialize(URL location, ResourceBundle resources) {
        
        canvas.setFocusTraversable(true);
        borderPane.setStyle("-fx-background-color: black");
        borderPane.setCenter(canvas);
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1.0);
        pw = canvas.getGraphicsContext2D().getPixelWriter();

        chip8CPU = new CPU();         
        chip8CPU.init();
        chip8CPU.loadROM("roms/UFO");
        
        //chip8CPU.startThread();
                 
        running = true;
        
        executor.execute(() -> {
            while (running) {
                chip8CPU.cycle();
                try {
                    Thread.sleep(4);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        executor.execute(() -> {
            while(running) {
                if (chip8CPU.isDrawFlag()) {
                    updateDisplay();              
                    chip8CPU.setDrawFlag(false);
                }
                try {
                   Thread.sleep(16);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void updateDisplay() {
        //System.out.println("UPDATE");
        
        int[][]gfx = chip8CPU.getGfx();
        
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (gfx[x][y] == 1) {
                    gc.setFill(Color.WHITE);
                } else {
                    gc.setFill(Color.BLACK);
                }
                gc.fillRect(x*modifier, y*modifier, modifier, modifier);
            }
        }
    }
    
    public void stop() {
        running = false;
        executor.shutdown();
    }
}
