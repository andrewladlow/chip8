package chip8;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class DisplayController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Canvas canvas;
    
    private CPU chip8CPU;
    
    public void drawGraphics(PixelWriter pw) {
        //TODO
        int[][]gfx = chip8CPU.getGfx();
        
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (gfx[x][y] == 1) {
                    pw.setColor(x, y, Color.WHITE);
                }
            }
        }
    }
   
    public void setup() {
        borderPane.setStyle("-fx-background-color: black");
        
        PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
        
        chip8CPU = new CPU();            
        chip8CPU.init();
        chip8CPU.loadROM("roms/PONG");
        
        int i = 0;
        while(i < 50) {
            chip8CPU.cycle();
            
            if (chip8CPU.isDrawFlag()) {
                drawGraphics(pw);              
                chip8CPU.setDrawFlag(false);
            }         
            i++;
        }
    }
}
