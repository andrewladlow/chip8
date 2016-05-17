package chip8;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private Object lock = new Object();
    
    @FXML
    private void handleKeyPressed(KeyEvent ke) {
        System.out.println("Key Pressed: " + ke.getCode());
    }
    
    @FXML
    private void handleKeyReleased(KeyEvent ke) {
        System.out.println("Key Released: " + ke.getCode());
    }
    
    public void initialize(URL location, ResourceBundle resources) { 
        canvas.setFocusTraversable(true);
        borderPane.setStyle("-fx-background-color: black");      
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1.0);
        pw = canvas.getGraphicsContext2D().getPixelWriter();

        chip8CPU = new CPU(lock);         
        chip8CPU.init();
        chip8CPU.loadROM("roms/TICTAC");
        
        //chip8CPU.startThread();
        
        executor.execute(() -> {
            while (true) {

                chip8CPU.cycle();
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        

        executor.execute(() -> {
            while(true) {

                if (chip8CPU.isDrawFlag()) {
                    updateDisplay();              
                    chip8CPU.setDrawFlag(false);
                }
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }
    
    public void updateDisplay() {
        System.out.println("UPDATE");
        //TODO    
        int[][]gfx = chip8CPU.getGfx();
        
        
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (gfx[x][y] == 1) {
                    gc.setStroke(Color.WHITE);
                    gc.setFill(Color.WHITE);
                    //pw.setColor(x, y, Color.WHITE);
                } else {
                    gc.setStroke(Color.BLACK);
                    gc.setFill(Color.BLACK);
                    //pw.setColor(x, y, Color.BLACK);
                }
                //gc.setStroke(Color.WHITE);
                drawPixel(x, y);
            }
        }
    }
    
    private void drawPixel(int x, int y) {
        gc.strokeLine((x*modifier), (y*modifier), (x*modifier), (y*modifier)+modifier);
        gc.strokeLine((x*modifier)+modifier, (y*modifier)+modifier, (x*modifier)+modifier, (y*modifier)+modifier);
        //gc.fillOval(x, y, modifier, modifier);
        //gc.fillRect(x, y, 3, 3);
    }
}
