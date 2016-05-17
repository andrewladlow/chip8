package chip8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class CPU implements Runnable {
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
    private int[] key = new int[] {
        '1','2','3','4',
        'Q','W','E','R',
        'A','B','D','F',
        'Z','X','C','V',
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
        0xF0, 0x80, 0xF0, 0x80, 0x80, // F
    };
    
    private boolean drawFlag = false;
    
    private Thread CPUThread;
    
    private boolean running = false;
    
    
    public CPU() {
    }
    
    public void startThread() {
        if (CPUThread == null) {
            running = true;
            CPUThread = new Thread(this);
            CPUThread.start();
        }
    }

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
               
        pc = 0x200;
        opcode = 0;
        index = 0;
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        
        // refresh screen
        drawFlag = true;

    }
    
    public void run() {
        while (running) {
            cycle();
        }
    }
    
    public void cycle() {
        opcode = fetch(pc);
        decode(opcode);
        updateTimers();
    }
    
    private int fetch(int pc) {
        //System.out.println("Opcode upper: " + Integer.toHexString(memory[pc] & 0xFF));
        //System.out.println("Opcode lower: " + Integer.toHexString(memory[pc+1] & 0xFF));
        // combine memory bytes pc and pc+1 to form 2 byte opcode
        int opcode = (memory[pc] & 0xFF) << 8 | (memory[pc+1] & 0xFF);
        return opcode;
    }
    
    private void decode(int opcode) {
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
            set();
            break;
        case 0x7000: // 0x7XNN
            add();
            break;
        case 0x8000:
            switch (opcode & 0x000F) {
            case 0x0000: // 0x8XY0
                setVal();
                break;
            case 0x0001: // 0x8XY1
                and();
                break;
            case 0x0002: // 0x8XY2
                or();
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
    
    // opcode execution
    
    // 0x00E0 - clear screen
    private void clear() {
        for (int xLine = 0; xLine < 64; xLine++) {
            for (int yLine = 0; yLine < 32; yLine++) {
                gfx[xLine][yLine] = 0;
            }
        }
        pc += 2;
    }
    
    // 0x00EE - return from subroutine
    private void subReturn() {
        sp--;
        pc = stack[sp];
        pc += 2;
    }
    
    // 0x1NNN - jump to address NNN
    private void jump() {
        pc = opcode & 0x0FFF;
        //System.out.println("New PC: " + pc);
        drawFlag = true;
    }
    
    // 0x2NNN - call subroutine at address NNN
    private void callSub() {
        stack[sp] = pc;
        sp++;
        pc = opcode & 0x0FFF;
    }
    
    // 0x3XNN - skip ins. if VX == NN
    private void skipIfEqualN() {
        if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x4XNN - skip ins. if VX != NN
    private void skipIfNotEqualN() {
        if (V[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF)) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x5XY0 - skip ins. if VX == VY
    private void skipIfEqualV() {
        if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0x6XNN - set VX to NN
    private void set() {
        V[(opcode & 0x0F00) >> 8] = opcode & 0x00FF;
        //System.out.println("Set");
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
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;   
        // if V[X] += V[Y] > 255, set carry
        if (V[X] > (0xFF - V[Y])) {
            V[0xF] = 1;
        } else {
            V[0xF] = 0;
        }
        // mask with lower byte - necessary?
        V[X] += V[Y];
        pc += 2;
    }
    
    // 0x8XY5 - VX -= VY, VF = 0 if < 0, else VF = 1
    private void subBorrow() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        // if V[Y] > V[X], set borrow
        if (V[Y] > V[X]) {
            V[0xF] = 0;
        } else {
            V[0xF] = 1;
        }
        // again mask with lower byte
        V[X] -= V[Y] & 0xFF;
        pc += 2;
    }
    
    // 0x8XY6 - VF = LSB VX, VX >> 1
    private void shiftRight() {
        int X = (opcode & 0x0F00) >> 8;
        V[0xF] = V[X] & 0x01;
        V[X] >>= 1;
        pc += 2;
    }
    
    // 0x8XY7 - VX = VY - VX, VF = 0 if < 0, else VF = 1
    private void setSubBorrow() {
        int X = (opcode & 0x0F00) >> 8;
        int Y = (opcode & 0x00F0) >> 4;
        if (V[X] > V[Y]) {
            V[0xF] = 0;
        } else {
            V[0xF] = 1;
        }
        V[X] = V[Y] - V[X];
        pc += 2;
    }
    
    // 0x8XYE - VF = MSB VX, VX << 1
    private void shiftLeft() {
        int X = (opcode & 0x0F00) >> 8;
        V[0xF] = V[X] & 0x80;
        V[X] <<= 1;
        pc += 2;
    }
    
    // 0x9XY0 - skip ins. if VX != VY
    private void skip() {
        if (V[(opcode & 0x0F00) >> 8] != V[(opcode & 0x00F0) >> 4]) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
      
    // 0xANNN - set index to address NNN
    private void setIndex() {
        index = (opcode & 0x0FFF);
        pc += 2;
        //System.out.println("Index: " + index);
    }
    
    // 0xBNNN - jump to NNN + V0
    private void jumpV() {
        pc = (opcode & 0x0FFF) + V[0x0];
    }
    
    // 0xCXNN - VX = rand & NN
    private void RandAnd() {
        Random rand = new Random();
        int i = rand.nextInt() % 0xFF;
        V[(opcode & 0x0F00) >> 8] = i & (opcode & 0x00FF); 
        pc += 2;
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
                // check each bit (pixel) in the 1 byte row
                if ((pixel & (0x80 >> xLine)) != 0) {

                    // wrap pixels if they're drawn off screen
                    int xCoord = (x+xLine) % 64;
                    int yCoord = (y+yLine) % 32;

                    // if pixel already exists, set carry (collision)
                    if (gfx[xCoord][yCoord] == 1) {
                        V[0xF] = 1;
                    }
                    // draw via xor
                    gfx[xCoord][yCoord] ^= 1;
                }
            }
        }       
        //drawFlag = true;
        pc += 2;
    }
    
    // 0xEX9E - skip ins. if key in VX is pressed
    private void skipKeyPressed() {
        System.out.println("TEST: " + key[V[(opcode & 0x0F00) >> 8]]);
        if (key[V[(opcode & 0x0F00) >> 8]] != 0) {
            pc += 4;
        } else {
            pc += 2;
        }
    }
    
    // 0xEXA1 - skip ins. if key in VX is not pressed
    private void skipKeyNotPressed() {
        if (key[V[(opcode & 0x0F00) >> 8]] != 1) {
            pc += 4;
        } else {
            pc += 2;
        }        
    }
    
    // 0xFX07 - VX = delay timer
    private void setXDelay() {
        V[(opcode & 0x0F00) >> 8] = delayTimer;
        pc += 2;
    }
    
    // 0xFX0A - Check for key press, store in VX
    private void keyWait() {
        int X = (opcode & 0x0F00);
        boolean keyPressed = false;
           
        for (int i = 0; i < 16; i++) {
            if (key[i] == 1) {
                //V[X] = key[i];
                V[X] = i;
                System.out.println("KEY1: " + key[i]);
                System.out.println("KEY2: " + V[X]);
                keyPressed = true;
            }
        }
        
        if (keyPressed) {
            pc += 2;
        }
    }
    
    // 0xFX15 - delay timer = VX
    private void setDelayX() {
        delayTimer = (((opcode & 0x0F00) >> 8) & 0x00FF);
        pc += 2;
    }
    
    // 0xFX18 - sound timer = VX
    private void setSoundX() {
        soundTimer = (((opcode & 0x0F00) >> 8) & 0x00FF);
        pc += 2;
    }
    
    // 0xFX1E - index += VX
    private void addToIndex() {
        int X = (opcode & 0x0F00) >> 8;
        // V[F] set to 1 if I+VX overflows
        if (index + V[X] > 0xFFF) {
            V[0xF] = 1;
        } else {
            V[0xF] = 0;
        }
        index += V[(opcode & 0x0F00) >> 8];
        System.out.println("Index: " + index);
        pc += 2;
    }
    
    // 0xFX29 - index = sprite loc. for char in VX
    private void setSpriteIndex() {
        index = V[(opcode & 0x0F00) >> 8] * 5;
        System.out.println("SPRITE INDEX: " + index);
        pc += 2;
    }
    
    // 0xFX33 - store BCD of VX in M[i]->M[i+2]
    private void storeBCD() {
        int X = (opcode & 0x0F00) >> 8;
        memory[index] = V[X] / 100;
        memory[index+1] = (V[X] % 100) / 10;
        memory[index+2] = (V[X] % 100) % 10;
        pc += 2;
    }
    
    // 0xFX55 - store V0 -> VX in memory from point index
    private void memStore() {
        int X = (opcode & 0x0F00) >> 8;
        for (int i = 0; i <= X; i++) {
            memory[index + i] = V[i];
        }
        //index += X + 1;
        pc += 2;
    }
    
    // 0xFX65 - fill V0 -> VX with values from memory point index
    private void memFill() {
        int X = (opcode & 0x0F00) >> 8;
        System.out.println("X: " + X);
        System.out.println("Index: " + index);
        for (int i = 0; i <= X; i++) {
            V[i] = memory[index + i];
        }
        //index += X + 1;
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
    
}
