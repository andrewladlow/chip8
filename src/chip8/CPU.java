package chip8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class CPU {
    // unsigned   
    private int opcode; // 2 bytes
    private int[] memory = new int[4096];
    
    /*     
    0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
    0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
    0x200-0xFFF - Program ROM and work RAM
    */
    
    // registers V0 - VE, VF carry
    private int[] V = new int[16];
    // index register
    private int index;
    // program counter
    private int pc;   
    private int[][] gfx = new int[64][32];
    private int delayTimer;
    private int soundTimer;
    private int[] stack = new int[16];
    // stack pointer
    private int sp;
    // hex input keypad
    private int[] key = new int[16];

    private int fontSet[] = new int[] {
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
        0xF0, 0x80, 0xF0, 0x80, 0x80, // F
    };
    
    private boolean drawFlag = false;

    public void init() {
        // clear memory
        for (int i = 0; i < 4096; i++) {
            memory[i] = 0;
        }
        // clear stack, keys, and registers
        for (int i = 0; i < 16; i++) {
            stack[i] = 0;
            key[i] = 0;
            V[i] = 0;
        }
        // load fonts
        for (int i = 0; i < 80; i++) {
            memory[i] = fontSet[i];
        }
        
        // reset vars
        pc = 0x200;
        opcode = 0;
        index = 0;
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        
        // refresh screen
        drawFlag = true;
    }
    
    public void cycle() {
        opcode = fetch(pc);
        decode(opcode);
    }
    
    private int fetch(int pc) {
        // combine memory bytes pc and pc+1 to form 2 byte opcode
        int opcode = (memory[pc] & 0xFF) << 8 | (memory[pc+1] & 0xFF);
        return opcode;
    }
    
    public void decode(int opcode) {
        //System.out.println("Opcode: " + Integer.toHexString(opcode));
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
        case 0x1000: // 0x1NNN
            jump();
            break;
        case 0x2000: // 0x2NNN
            callSub();
            break;
        case 0x3000: // 0x3NNN
            skipIfEqualN();
            break;
        case 0x4000: // 0x4NNN
            skipIfNotEqualN();
            break;
        case 0x5000: // 0x5XY0
            skipIfEqualV();
            break;
        case 0x6000: // 0x6XNN
            setAddress();
            break;
        case 0x7000: // 0x7XNN
            add();
            break;
        case 0x8000:
            switch (opcode & 0x000F) {
            case 0x0000: // 0x8XY0
                setV();
                break;
            case 0x0001: // 0x8XY1
                or();
                break;
            case 0x0002: // 0x8XY2
                and();
                break;
            case 0x0003: // 0x8XY3
                xor();
                break;
            case 0x0004: // 0x8XY4
                addCarry();
                break;
            case 0x0005: // 0x8XY5
                subBorrow();
                break;
            case 0x0006: // 0x8XY6
                shiftRight();
                break;
            case 0x0007: // 0x8XY7
                setSubBorrow();
                break;
            case 0x000E: // 0x8XYE
                shiftLeft();
                break;
            }
            break;
        case 0x9000: // 0x9XY0
            skip();
            break;
        case 0xA000: // 0xANNN
            setIndex();
            break;
        case 0xB000: // 0xBNNN
            jumpV();
            break;
        case 0xC000: // 0xCXNN
            RandAnd();
            break;
        case 0xD000: // 0xDXYN
            drawSprite();
            break;
        case 0xE000:
            switch (opcode & 0x000F) {
            case 0x000E: // 0xEX9E
                skipKeyPressed();
                break;
            case 0x0001: // 0xEXA1
                skipKeyNotPressed();
                break;
            }
            break;
        case 0xF000:
            switch (opcode & 0x00FF) {
            case 0x0007: // 0xFX07
                setXDelay();
                break;
            case 0x000A: // 0xFX0A
                keyWait();
                break;
            case 0x0015: // 0xFX15
                setDelayX();
                break;
            case 0x0018: // 0xFX18
                setSoundX();
                break;
            case 0x001E: // 0xFX1E
                addToIndex();
                break;
            case 0x0029: // 0xFX29
                setSpriteIndex();
                break;
            case 0x0033: // 0xFX33
                storeBCD();
                break;
            case 0x0055: // 0xFX55
                memStore();
                break;
            case 0x0065: // 0xFX65
                memFill();
                break;
            }
            break;        
        default:
            System.out.println("Unknown opcode: " + opcode);
        }
    }
        
    // 0x00E0 - clear screen
    public void clear() {
        for (int xLine = 0; xLine < 64; xLine++) {
            for (int yLine = 0; yLine < 32; yLine++) {
                gfx[xLine][yLine] = 0;
            }
        }
        pc += 2;
        drawFlag = true;
    }
    
    // 0x00EE - return from subroutine
    public void subReturn() {
        sp--;
        pc = stack[sp];
        pc += 2;
        drawFlag = true;
    }
    
    // 0x1NNN - jump to address NNN
    public void jump() {
        pc = opcode & 0x0FFF;
    }
    
    // 0x2NNN - call subroutine at address NNN
    public void callSub() {
        stack[sp] = pc;
        sp++;
        pc = opcode & 0x0FFF;
    }
    
    // 0x3XNN - skip ins. if VX == NN
    public void skipIfEqualN() {
        if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x4XNN - skip ins. if VX != NN
    public void skipIfNotEqualN() {
        if (V[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF)) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x5XY0 - skip ins. if VX == VY
    public void skipIfEqualV() {
        if (V[(opcode & 0x0F00) >> 8] == V[(opcode & 0x00F0) >> 4]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x6XNN - set VX to NN
    public void setAddress() {
        V[(opcode & 0x0F00) >> 8] = (opcode & 0x00FF);
        //System.out.println("Set");
        pc += 2;
    }
    
    // 0x7XNN - add NN to VX
    public void add() {
        int X = (opcode & 0x0F00) >> 8;
        int NN = (opcode & 0x00FF);
        int result = V[X] + NN;
        // resolve overflow
        if (result >= 256) {
            V[X] = result - 256;
        } else {
            V[X] = result;
        }
        pc += 2;
    }
    
    // 0x8XY0 - set VX to VY
    public void setV() {
        V[(opcode & 0x0F00) >> 8] = V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY1 - VX = VX OR VY
    public void or() {
        V[(opcode & 0x0F00) >> 8] |= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY2 - VX = VX AND VY
    public void and() {
        V[(opcode & 0x0F00) >> 8] &= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY3 - VX = VX XOR VY
    public void xor() {
        V[(opcode & 0x0F00) >> 8] ^= V[(opcode & 0x00F0) >> 4];
        pc += 2;
    }
    
    // 0x8XY4 - VX += VY, VF=1 if > 255, else VF=0
    public void addCarry() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int result = V[X] + V[Y];
        // if V[X] += V[Y] > 255, set carry
        if (result > 255) {
            V[0xF] = 1;
            V[X] = (result - 256) & 0xFF;
        } else {
            V[0xF] = 0;
            V[X] = result & 0xFF;
        }
        pc += 2;
    }
    // 0x8XY5 - VX -= VY, VF = 0 if < 0, else VF = 1
    public void subBorrow() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        // if V[Y] > V[X], set !borrow
        if (V[Y] > V[X]) {
            // wrap around instead of below 0
            V[X] = (256 + (V[X] - V[Y])) & 0xFF;
            V[0xF] = 0;
        } else {
            V[X] = (V[X] - V[Y]) & 0xFF;
            V[0xF] = 1;
        }
        pc += 2;
    }
    
    // 0x8XY6 - VF = LSB VX, VX >> 1
    public void shiftRight() {
        int X = (opcode & 0x0F00) >> 8;
        V[0xF] = V[X] & 0x01;
        V[X] >>= 1;
        pc += 2;
    }
    
    // 0x8XY7 - VX = VY - VX, VF = 0 if < 0, else VF = 1
    public void setSubBorrow() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        int result = V[Y] - V[X];
        if (V[X] > V[Y]) {
            V[0xF] = 0;
            V[X] = 256 + result;
        } else {
            V[0xF] = 1;
            V[X] = result;
        }
        pc += 2;
    }
    
    // 0x8XYE - VF = MSB VX, VX << 1
    public void shiftLeft() {
        int X = (opcode & 0x0F00) >> 8;
        V[0xF] = V[X] & 0x80;
        V[X] <<= 1;
        pc += 2;
    }
    
    // 0x9XY0 - skip ins. if VX != VY
    public void skip() {
        if (V[(opcode & 0x0F00) >> 8] != V[(opcode & 0x00F0) >> 4]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
      
    // 0xANNN - set index to address NNN
    public void setIndex() {
        index = (opcode & 0x0FFF);
        pc += 2;
    }
    
    // 0xBNNN - jump to NNN + V0
    public void jumpV() {
        pc = (opcode & 0x0FFF) + V[0];
    }
    
    // 0xCXNN - VX = rand & NN
    public void RandAnd() {
        Random rand = new Random();
        int i = rand.nextInt(256);
        V[(opcode & 0x0F00) >> 8] = i & (opcode & 0x00FF); 
        pc += 2;
    }
    
    // 0xDXYN - draw sprite at X,Y with N rows
    public void drawSprite() {
        int x = V[(opcode & 0x0F00) >> 8];
        int y = V[(opcode & 0x00F0) >> 4];
        int height = opcode & 0x000F;
        V[0xF] = 0;
        for (int yLine = 0; yLine < height; yLine++) {
            int pixel = memory[index + yLine];

            for (int xLine = 0; xLine < 8; xLine++) {
                // check each bit (pixel) in the 8 bit row
                if ((pixel & (0x80 >> xLine)) != 0) {

                    // wrap pixels if they're drawn off screen
                    //int xCoord = (x+xLine) % 64;
                    //int yCoord = (y+yLine) % 32;
                    int xCoord = x+xLine;
                    int yCoord = y+yLine; 
                    
                    if (xCoord < 64 && yCoord < 32) {
                        // if pixel already exists, set carry (collision)
                        if (gfx[xCoord][yCoord] == 1) {
                            V[0xF] = 1;
                        }
                        // draw via xor
                        gfx[xCoord][yCoord] ^= 1;
                    }
                }
            }
        }       
        drawFlag = true;
        pc += 2;
    }
    
    // 0xEX9E - skip ins. if key in VX is pressed
    public void skipKeyPressed() {
        if (key[V[(opcode & 0x0F00) >> 8]] == 1) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0xEXA1 - skip ins. if key in VX is not pressed
    public void skipKeyNotPressed() {
        if (key[V[(opcode & 0x0F00) >> 8]] == 0) {
            pc += 4;
        } else {
            pc += 2;
        }        
    }
    
    // 0xFX07 - VX = delay timer
    public void setXDelay() {
        V[(opcode & 0x0F00) >> 8] = delayTimer;
        pc += 2;
    }
    
    // 0xFX0A - Check for key press, store in VX
    public void keyWait() {     
        int X = (opcode & 0x0F00) >> 8;
        boolean keyPressed = false;
           
        for (int i = 0; i < 16; i++) {
            if (key[i] == 1) {
                V[X] = i;
                keyPressed = true;
                key[i] = 0;
            }
        }
        
        if (keyPressed) {
            pc += 2;
        }
    }
    
    // 0xFX15 - delay timer = VX
    public void setDelayX() {
        delayTimer = V[(opcode & 0x0F00) >> 8];
        pc += 2;
    }
    
    // 0xFX18 - sound timer = VX
    public void setSoundX() {
        soundTimer = V[(opcode & 0x0F00) >> 8];
        pc += 2;
    }
    
    // 0xFX1E - index += VX
    public void addToIndex() {
        index += V[(opcode & 0x0F00) >> 8];
        pc += 2;
    }
    
    // 0xFX29 - index = sprite loc. for char in VX
    public void setSpriteIndex() {
        index = V[(opcode & 0x0F00) >> 8] * 5;
        pc += 2;
        drawFlag = true;
    }
    
    // 0xFX33 - store BCD of VX in M[i]->M[i+2]
    public void storeBCD() {
        int X = (opcode & 0x0F00) >> 8;
        memory[index] = V[X] / 100;
        memory[index+1] = (V[X] % 100) / 10;
        memory[index+2] = (V[X] % 100) % 10;
        pc += 2;
    }
    
    // 0xFX55 - store V0 -> VX in memory from point index
    public void memStore() {
        int X = (opcode & 0x0F00) >> 8;
        for (int i = 0; i <= X; i++) {
            memory[index + i] = V[i];
        }
        //index += X + 1;
        pc += 2;
    }
    
    // 0xFX65 - fill V0 -> VX with values from memory point index
    public void memFill() {
        int X = (opcode & 0x0F00) >> 8;
        //System.out.println("X: " + X);
        //System.out.println("Index: " + index);
        for (int i = 0; i <= X; i++) {
            V[i] = (memory[index + i]) & 0xFF;
        }
        //index += X + 1;
        pc += 2;
    }
    
    public void updateTimers() {
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

    public int[][] getGfx() {
        return gfx;
    }
    
    public void setKey(int index, int val) {
        key[index] = val;
    }
    
    public void debug() {
        System.out.println("Opcode: " + Integer.toHexString(opcode));
        System.out.println("I: " + Integer.toHexString(index));
        System.out.println("PC: " + Integer.toHexString(pc));
        System.out.println("Delay: " + delayTimer);
        System.out.println("Sound: " + soundTimer);
        System.out.println("Draw: " + drawFlag);
        for (int i = 0; i < 16; i++) {
            System.out.println("V[" + Integer.toHexString(i) + "]: " + Integer.toHexString(V[i]));
        }
        System.out.printf("%n%n%n");
    }
      
    // package private getters / setters for junit
          
    void setGfx(int x, int y) {
        gfx[x][y] ^= 1;
    }
    
    int getStack(int stackp) {
        return stack[stackp];
    }
    
    void setStack(int stackp, int val) {
        stack[stackp] = val;
    }
    
    int getSp() {
        return sp;
    }
    
    void setSp(int val) {
        sp = val;
    }
    
    int getPc() {
        return pc;
    }
    
    void setPc(int val) {
        pc = val;
    }
    
    int getV(int i) {
        return V[i];
    }
    
    void setV(int i, int val) {
        V[i] = val;
    }
    
    void setOpcode(int val) {
        opcode = val;
    }
    
    int getIndex() {
        return index;
    }
    
    int getDelayTimer() {
        return delayTimer;
    }
    
    void setDelayTimer(int val) {
        delayTimer = val;
    }
    
    int getSoundTimer() {
        return soundTimer;
    }
    
    int getMemoryVal(int index) {
        return memory[index];
    }
    
    void setMemoryVal(int index, int val) {
        memory[index] = val;
    }
}
