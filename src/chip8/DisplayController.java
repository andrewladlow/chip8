package chip8;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<String, Integer> keyMap = new HashMap<String, Integer>();
    
    public DisplayController() {
        /* 
         * initialise key input
         * "1", "2", "3", "4"
         * "Q", "W", "E", "R"
         * "A", "S", "D", "F"
         * "Z", "X", "C", "V" 
        */
        keyMap.put("1", 1);
        keyMap.put("2", 2);
        keyMap.put("3", 3);
        keyMap.put("4", 12);
        keyMap.put("q", 4);
        keyMap.put("w", 5);
        keyMap.put("e", 6);
        keyMap.put("r", 13);
        keyMap.put("a", 7);
        keyMap.put("s", 8);
        keyMap.put("d", 9);
        keyMap.put("f", 14);
        keyMap.put("z", 10);
        keyMap.put("x", 0);
        keyMap.put("c", 11);
        keyMap.put("v", 15);
    }
 
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
        System.out.println("Pressed: " + ke.getText());

        if (keyMap.containsKey(ke.getText())) {
            chip8CPU.setKey(keyMap.get(ke.getText()), 1);
        }
    }
    
    @FXML
    private void handleKeyReleased(KeyEvent ke) {
        System.out.println("Released: " + ke.getText());
        
        if (keyMap.containsKey(ke.getText())) {
            chip8CPU.setKey(keyMap.get(ke.getText()), 0);
        }
        
    }
    
    public void init() {
        stage = (Stage) canvas.getScene().getWindow();
        canvas.setFocusTraversable(true);
        borderPane.setStyle("-fx-background-color: black");
        borderPane.setCenter(canvas);
        gc = canvas.getGraphicsContext2D();
        chip8CPU = new CPU();
        chip8CPU.init();
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
            //chip8CPU.debug();
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
        if (filePath != null) {
            stopThreads();
            chip8CPU = new CPU();
            chip8CPU.init();
            chip8CPU.loadROM(filePath);
            startThreads();
        }
    }
}
