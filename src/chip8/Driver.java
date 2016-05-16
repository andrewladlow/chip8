package chip8;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class Driver {

    public static void main(String[] args) {
        CPU chip8CPU = new CPU();
        Screen screen = new Screen();
        
        chip8CPU.init();
        chip8CPU.loadROM("roms/PONG");
        
        //while(true) {
            chip8CPU.cycle();
            
            if (chip8CPU.isDrawFlag()) {
                screen.drawGraphics();
            }
        //}
    }
}
