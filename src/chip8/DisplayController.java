package chip8;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

public class DisplayController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private Canvas canvas;
    private Stage stage;
    private GraphicsContext gc;
    private double pixelSize = 8;  
    private CPU chip8CPU;
    private FileChooser fileChooser = new FileChooser();
    private String filePath;
    private ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(2); 
    private ScheduledFuture<?> cpuThread;
    private ScheduledFuture<?> displayThread;
 
    @FXML
    private void handleLoad() {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            filePath = file.toString();
            restartCPU();
        }
    }
    
    @FXML
    private void handleRestartAction(ActionEvent ae) {
        restartCPU();
    } 
    
    @FXML
    private void handleKeyPressed(KeyEvent ke) {
        System.out.println("Pressed: " + ke.getCode().toString());
        switch (ke.getCode().toString()) {
        case "DIGIT1":
            chip8CPU.setKey(1, 1);
            break;
        case "DIGIT2":
            chip8CPU.setKey(2, 1);        
            break;
        case "DIGIT3":
            chip8CPU.setKey(3, 1);  
            break;
        case "DIGIT4":
            chip8CPU.setKey(12, 1); 
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
            chip8CPU.setKey(13, 1); 
            break;
        case "A":
            chip8CPU.setKey(7, 1); 
            break;
        case "S":
            chip8CPU.setKey(8, 1); 
            break;
        case "D":
            chip8CPU.setKey(9, 1);  
            break;
        case "F":
            chip8CPU.setKey(14, 1);
            break;
        case "Z":
            chip8CPU.setKey(10, 1);  
            break;
        case "X":
            chip8CPU.setKey(0, 1);  
            break;
        case "C":
            chip8CPU.setKey(11, 1);
            break;
        case "V":
            chip8CPU.setKey(15, 1); 
            break;
        }
    }
    
    @FXML
    private void handleKeyReleased(KeyEvent ke) {
        System.out.println("Released: " + ke.getCode().toString());
        switch (ke.getCode().toString()) {
        case "DIGIT1":
            chip8CPU.setKey(1, 0);
            break;
        case "DIGIT2":
            chip8CPU.setKey(2, 0);         
            break;
        case "DIGIT3":
            chip8CPU.setKey(3, 0);  
            break;
        case "DIGIT4":
            chip8CPU.setKey(12, 0); 
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
            chip8CPU.setKey(13, 0); 
            break;
        case "A":
            chip8CPU.setKey(7, 0); 
            break;
        case "S":
            chip8CPU.setKey(8, 0); 
            break;
        case "D":
            chip8CPU.setKey(9, 0);  
            break;
        case "F":
            chip8CPU.setKey(14, 0);  
            break;
        case "Z":
            chip8CPU.setKey(10, 0);  
            break;
        case "X":
            chip8CPU.setKey(0, 0);  
            break;
        case "C":
            chip8CPU.setKey(11, 0);
            break;
        case "V":
            chip8CPU.setKey(15, 0); 
            break;
        }
    }
    
    public void init() {
        stage = (Stage) canvas.getScene().getWindow();
        canvas.setFocusTraversable(true);
        borderPane.setStyle("-fx-background-color: black");
        borderPane.setCenter(canvas);
        gc = canvas.getGraphicsContext2D();
    }
    
    public void updateDisplay() {
        int[][]gfx = chip8CPU.getGfx();
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                if (gfx[x][y] == 1) {
                    gc.setFill(Color.WHITE);
                } else {
                    gc.setFill(Color.BLACK);
                }
                gc.fillRect(x*pixelSize, y*pixelSize, pixelSize, pixelSize);
            }
        }
    }
    
    public void startThreads() {        
        // 500 operations/s
        cpuThread = threadPool.scheduleWithFixedDelay(() -> {
            chip8CPU.cycle();
            chip8CPU.debug();
        }, 2, 2, TimeUnit.MILLISECONDS);
        
        // ~60Hz
        displayThread = threadPool.scheduleWithFixedDelay(() -> {
            chip8CPU.updateTimers();
            if (chip8CPU.isDrawFlag()) {
                Platform.runLater(() -> {
                    updateDisplay();  
                    chip8CPU.setDrawFlag(false);
                });
            }
        }, 17, 17, TimeUnit.MILLISECONDS);
    }
    
    public void stopThreads() {
        if (cpuThread != null) {
            cpuThread.cancel(true);
            displayThread.cancel(true);
        }
    }
    
    public void stopPool() {
        threadPool.shutdownNow();
    }
    
    public void restartCPU() {
        stopThreads();
        chip8CPU = new CPU();
        chip8CPU.init();
        chip8CPU.loadROM(filePath);
        startThreads();
    }
}
