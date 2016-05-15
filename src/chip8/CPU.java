package chip8;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CPU {
    // unsigned   
    private int opcode; // 2 bytes
    private byte[] memory = new byte[4096];
    // registers V0 - VE, VF carry
    private int[] V = new int[16];
    // index register
    private int index;
    // program counter
    private int pc;
    // i & pc range 0x000 - 0xFFF
    /*    
     * 0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
     * 0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
     * 0x200-0xFFF - Program ROM and work RAM
     */
    private char[][] gfx = new char[64][32];
    private char delayTimer;
    private char soundTimer;
    private char[] stack = new char[16];
    // stack pointer
    private char sp;
    // hex input keypad
    private char[] key = new char[16];
    
    public void init() {
        // set program start, clear memory
        pc = 0x200;
        opcode = 0;
        index = 0;
        sp = 0;
    }
    
    public void cycle() {
        opcode = fetch(pc);
        decode(opcode);
        updateTimers();
    }
    
    private int fetch(int pc) {
        System.out.println("Opcode upper: " + memory[pc]);
        System.out.println("Opcode lower: " + memory[pc+1]);
        // combine memory bytes pc and pc+1 to form 2 byte opcode
        int opcode = memory[pc] << 8 | memory[pc++];
        return opcode;
    }
    
    private void decode(int opcode) {
        System.out.println("Opcode: " + opcode);
        switch (opcode & 0xF000) {
        case 0xA000:
            setIndex();
            break;
        default:
            System.out.println("Unknown opcode: " + opcode);
        }
    }
    
    // opcode execution
    private void setIndex() {
        index = opcode & 0x0FFF;
        pc += 2; 
    }
    
    private void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            if (soundTimer == 1) {
                System.out.println("BEEP");
            }
            soundTimer--;
        }
    }
    
    public void loadROM(String filePath) {
        Path path = Paths.get(filePath);
        try {
            byte[] data = Files.readAllBytes(path);
            for (int i = 0; i < data.length; i++) {
                // program starts at 0x200 (512)
                memory[i+512] = data[i];
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    
}