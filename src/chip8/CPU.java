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
        int opcode = memory[pc] << 8 | memory[pc+1];
        return opcode;
    }
    
    private void decode(int opcode) {
        System.out.println("Opcode: " + opcode);
        switch (opcode & 0xF000) {
        case 0x0000:
            switch (opcode & 0x000F) {
            case 0x0000:
                clear();
            case 0x000E:
                returnSub();
            }
            break;
        case 0x1000:
            
        case 0x2000:
            
        case 0x3000:
            
        case 0x4000:
            
        case 0x5000:
            
        case 0x6000:
            
        case 0x7000:
            
        case 0x8000:
            switch (opcode & 0x000F) {
            case 0x0000:
                
            case 0x0001:
                
            case 0x0002:
                
            case 0x0003:
                
            case 0x0004:
                
            case 0x0005:
                
            case 0x0006:
                
            case 0x0007:
                
            case 0x000E:
                     
            }
            break;
        case 0x9000:
            
        case 0xA000:
            setIndex();
            break;
        case 0xB000:
        case 0xC000:
        case 0xD000:
        case 0xE000:
            switch (opcode & 0x000F) {
            case 0x000E:
            case 0x0001:
            }
            break;
        case 0xF000:
            switch (opcode & 0x00FF) {
            case 0x0007:
            case 0x000A:
            case 0x0015:
            case 0x0018:
            case 0x001E:
            case 0x0029:
            case 0x0033:
            case 0x0055:
            case 0x0065:
            }
            break;        
        default:
            System.out.println("Unknown opcode: " + opcode);
        }
    }
    
    // opcode execution
    
    private void clear() {
        
    }
    
    private void returnSub() {
        
    }
    
    private void jump() {
        
    }
    
    private void call() {
        
    }
    
    private void skipIfEqual() {
        
    }
    
    private void skipIfNotEqual() {
        
    }
    
    private void set() {
        
    }
    
    private void add() {
        
    }
    
    private void setVal() {
        
    }
    
    private void or() {
        
    }
    
    private void and() {
        
    }
    
    private void xor() {
        
    }
    
    private void addCarry() {
        
    }
    
    private void subBorrow() {
        
    }
    
    private void shiftRight() {
        
    }
    
    private void setSubBorrow() {
        
    }
    
    private void shiftLeft() {
        
    }
    
    private void skip() {
        
    }
    
    private void setAddress() {
        
    }
    
    private void jumpAddress() {
        
    }
    
    private void setIndex() {
        index = opcode & 0x0FFF;
        pc += 2; 
    }
    
    private void bitwiseAnd() {
        
    }
    
    private void dxyn() {
        
    }
    
    private void skipPressed() {
        
    }
    
    private void skipNotPressed() {
        
    }
    
    private void setDelay() {
        
    }
    
    private void setSound() {
        
    }
    
    private void addToIndex() {
        
    }
    
    private void setSpriteIndex() {
        
    }
    
    private void storeBCD() {
        
    }
    
    private void MemStore() {
        
    }
    
    private void MemFill() {
        
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
