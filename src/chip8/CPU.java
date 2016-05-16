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
    private int[] memory = new int[4096];
    // registers V0 - VE, VF carry
    private int[] V = new int[16];
    // index register
    private int index;
    // program counter
    private int pc;   
/*    0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
    0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
    0x200-0xFFF - Program ROM and work RAM*/
    private char[][] gfx = new char[64][32];
    private char delayTimer;
    private char soundTimer;
    private int[] stack = new int[16];
    // stack pointer
    private int sp;
    // hex input keypad
    private char[] key = new char[] {
        '1', '2', '3', '4',
        'Q', 'W', 'E', 'R',
        'A', 'S', 'D', 'F',
        'Z', 'X', 'C', 'V'
    };

    private char fontSet[] = new char[] {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80 // F
    };
    
    private boolean drawFlag = false;
    
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
        System.out.println("Opcode upper: " + Integer.toHexString(memory[pc]));
        System.out.println("Opcode lower: " + Integer.toHexString(memory[pc+1]));
        // combine memory bytes pc and pc+1 to form 2 byte opcode
        int opcode = memory[pc] << 8 | memory[pc+1];
        return opcode;
    }
    
    private void decode(int opcode) {
        System.out.println("Opcode: " + Integer.toHexString(opcode));
        switch (opcode & 0xF000) {
        case 0x0000:
            switch (opcode & 0x000F) {
            case 0x0000: // 0x00E0
                clear();
                break;
            case 0x000E: // 0x00EE
                subReturn();
                break;
            }
            break;
        case 0x1000:
            
        case 0x2000: // 0x2NNN
            callSub();
            break;
        case 0x3000:
            
        case 0x4000:
            
        case 0x5000:
            
        case 0x6000: // 0x6XNN
            set();
            break;
        case 0x7000:
            add();
            break;
        case 0x8000:
            switch (opcode & 0x000F) {
            case 0x0000:
                
            case 0x0001:
                
            case 0x0002:
                
            case 0x0003:
                
            case 0x0004: // 0x8XY4
                addCarry();
                break;
            case 0x0005:
                
            case 0x0006:
                
            case 0x0007:
                
            case 0x000E:
                     
            }
            break;
        case 0x9000:
            
        case 0xA000: // 0xANNN
            setIndex();
            break;
        case 0xB000:
        case 0xC000:
        case 0xD000: // 0xDXYN
            drawSprite();
            break;
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
            case 0x0033: // 0xFX33
                storeBCD();
            case 0x0055:
            case 0x0065:
            }
            break;        
        default:
            System.out.println("Unknown opcode: " + opcode);
        }
    }
    
    // opcode execution
    
    // 0x00E0
    private void clear() {
        
    }
    
    // 0x00EE
    private void subReturn() {
        
    }
    
    // 0x1NNN
    private void jump() {
        
    }
    
    // 0x2NNN - call subroutine at address NNN
    private void callSub() {
        stack[sp] = pc;
        sp++;
        pc = opcode & 0x0FFF;
    }
    
    // 0x3XNN
    private void skipIfEqualN() {
        
    }
    
    // 0x4XNN
    private void skipIfNotEqual() {
        
    }
    
    // 0x5XY0
    private void skipIfEqualV() {
        
    }
    
    // 0x6XNN - set VX to NN
    private void set() {
        V[(opcode & 0x0F00) >> 8] = opcode & 0x00FF;
        System.out.println("Set");
        pc += 2;
    }
    
    // 0x7XNN - add NN to VX
    private void add() {
        V[(opcode & 0x0F00) >> 8] += opcode & 0x00FF;
        pc += 2;
    }
    
    // 0x8XY0 - set VX to VY
    private void setVal() {
        V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY1 - VX = VX OR VY
    private void or() {
        V[(opcode & 0x0F00) >> 8] |= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY2 - VX = VX AND VY
    private void and() {
        V[(opcode & 0x0F00) >> 8] &= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY3 - VX = VX XOR VY
    private void xor() {
        V[(opcode & 0x0F00) >> 8] ^= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY4 - VX += VY, VF=1 if > 255, else VF=0
    private void addCarry() {
        // if V[X] += V[Y] > 255, set carry
        if (V[(opcode & 0x00F0) >> 4] > (0xFF - V[(opcode & 0x0F00) >> 8])) {
            V[0xF] = 1;
        } else {
            V[0xF] = 0;
        }
        V[(opcode & 0x0F00) >> 8] += V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY5
    private void subBorrow() {
        
    }
    
    // 0x8XY6
    private void shiftRight() {
        
    }
    
    // 0x8XY7
    private void setSubBorrow() {
        
    }
    
    // 0x8XYE
    private void shiftLeft() {
        
    }
    
    // 0x9XY0
    private void skip() {
        
    }
      
    // 0xANNN - set index to address NNN
    private void setIndex() {
        index = opcode & 0x0FFF;
        pc += 2; 
    }
    
    // 0xBNNN
    private void jumpV0() {
        
    }
    
    // 0xCXNN
    private void bitwiseAnd() {
        
    }
    
    // 0xDXYN - draw sprite at X,Y with N rows
    private void drawSprite() {
        int x = V[(opcode & 0x0F00) >> 8];
        int y = V[(opcode & 0x00F0) >> 4];
        int height = opcode & 0x000F;
        int pixel;
        
        V[0xF] = 0;
        for (int yLine = 0; yLine < height; yLine++) {
            pixel = memory[index + yLine];
            for (int xLine = 0; xLine < 8; xLine++) {
                if ((pixel & (0x80 >> xLine)) != 0) {
                    // if pixel already exists, set carry (collision)
                    if (gfx[x+xLine][y+yLine] == 1) {
                        V[0xF] = 1;
                    }
                    // draw via xor
                    gfx[x+xLine][y+yLine] ^= 1;
                }
            }
        }
        
        drawFlag = true;
        pc += 2;
    }
    
    // 0xEX9E - skip ins. if key in VX is pressed
    private void skipKeyPressed() {
        if (key[V[(opcode & 0x0F00) >> 8]] != 0) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0xEXA1
    private void skipKeyNotPressed() {
        
    }
    
    // 0xFX07
    private void setXDelay() {
        
    }
    
    // 0xFX0A
    private void KeyWait() {
        
    }
    
    // 0xFX15
    private void setDelayX() {
        
    }
    
    // 0xFX18
    private void setSoundX() {
        
    }
    
    // 0xFX1E
    private void addToIndex() {
        
    }
    
    // 0xFX29
    private void setSpriteIndex() {
        
    }
    
    // 0xFX33 - store BCD of VX in M[i]->M[i+2]
    private void storeBCD() {
        memory[index] = V[(opcode & 0x0F00) >> 8] / 100;
        memory[index+1] = (V[(opcode & 0x0F00) >> 8] / 100) % 10;
        memory[index+2] = (V[(opcode & 0x0F00) >> 8] % 100) % 10;
        pc += 2;
    }
    
    // 0xFX55
    private void MemStore() {
        
    }
    
    // 0xFX65
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

    public boolean isDrawFlag() {
        return drawFlag;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }
    
}
