package chip8;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CPUTest {
    
    private CPU target = new CPU();
    
    @Before
    public void init() {
        // clear all registers, memory etc
        target.init();
    }
    
    @Test
    public void testClear() { // 00E0
        // add pixel
        target.setGfx(15, 24);
 
        target.clear();
        
        int[][] gfx = target.getGfx();
        
        // confirm pixel was removed
        boolean valid = true;
        for (int x = 0; x < 64; x ++) {
            for (int y = 0; y < 32; y ++) {
                if (gfx[x][y] == 1) {
                    valid = false;
                }
            }
        }
        // 'valid' should still be true if no pixel found
        assertEquals(valid, true);
    }

    @Test
    public void testSubReturn() { // 00EE
        // create an example sub call
        target.setStack(0, 50);
        target.setSp(1);
        // pc = opcode (0x2NNN sub call) & 0x0FFF
        target.setPc((0x2FE7 & 0x0FFF));
        
        // test the sub return
        target.subReturn();
        // sp-- = 0
        assertEquals(target.getSp(), 0);
        // pc = stack[sp] + 2 = 50 + 2
        assertEquals(target.getPc(), 52);
    }

    @Test
    public void testJump() { // 1NNN
        target.setOpcode(0x2659);
        
        target.jump();
        
        // pc = opcode & 0x0FFF = 0x659
        assertEquals(target.getPc(), 0x659);
    }

    @Test
    public void testCallSub() { // 2NNN
        target.setOpcode(0x2ABC);
        target.setPc(0x509);
        
        target.callSub();

        // stack[sp] = pc, stack[0] = 0x509
        assertEquals(target.getStack(target.getSp()-1), 0x509);
        // sp++, sp = 1
        assertEquals(target.getSp(), 1);
        // pc = opcode & 0x0FFF = 0xABC
        assertEquals(target.getPc(), 0xABC);
    }

    @Test
    public void testSkipIfEqualN() { // 0x3XNN
        target.setOpcode(0x3298);
        target.setV(2, 152);
        
        target.skipIfEqualN();
        
        // V[2] = 152 = 0x98, pc = 0x200 + 4 = 516
        assertEquals(target.getPc(), 516);
    }

    @Test
    public void testSkipIfNotEqualN() { // 0x4XNN
        target.setOpcode(0x45DA);
        target.setV(5, 43);
        
        target.skipIfNotEqualN();
        
        // V[5] = 43 != 0xDA, pc += 4 = 516
        assertEquals(target.getPc(), 516);
    }

    @Test
    public void testSkipIfEqualV() { // 5XY0
        target.setOpcode(0x5C80);
        target.setV(0xC, 5);
        target.setV(8, 5);
        
        target.skipIfEqualV();
        
        // V[C] = V[8] hence pc +4, = 516
        assertEquals(target.getPc(), 516);
    }

    @Test
    public void testSetAddress() { // 6XNN
        target.setOpcode(0x67ED);
        
        target.setAddress();
        
        // V[7] = 0xED = 237
        assertEquals(target.getV(7), 237);
    }

    @Test
    public void testAdd() { // 7XNN
        target.setOpcode(0x7455);
        target.setV(4, 39);
        
        target.add();
        
        // V[4] = 39, NN = 0x55 = 85
        // 85 + 39 = 124
        assertEquals(target.getV(4), 124);
    }

    @Test
    public void testSetV() { // 8XY0
        target.setOpcode(0x8750);
        target.setV(0x7, 3);
        target.setV(0x5, 9);
        
        target.setV();
        
        // V[7] = V[5] = 9
        assertEquals(target.getV(7), 9);
    }

    @Test
    public void testOr() { // 8XY1
        target.setOpcode(0x84e1);
        target.setV(4, 7);
        target.setV(0xE, 42);
        
        target.or();
        
        // 7 OR 42 = 47
        assertEquals(target.getV(4), 47);
    }
    
    @Test
    public void testAnd() { // 8XY2
        target.setOpcode(0x8c02);
        target.setV(0xC, 3);
        target.setV(0, 5);
        
        target.and();
        
        // 3 AND 5 = 1
        assertEquals(target.getV(0xC), 1);
    }

    @Test
    public void testXor() { // 8XY3
        target.setOpcode(0x8483);
        target.setV(4, 14);
        target.setV(8, 31);
        
        target.xor();
        
        // 14 XOR 31 = 17 
        assertEquals(target.getV(0x4), 17);
    }

    @Test
    public void testAddCarry() { // 8XY4
        target.setOpcode(0x8394);
        target.setV(3, 180);
        target.setV(9, 155);
       
        target.addCarry();
        
        // 180 + 155 = 79
        assertEquals(target.getV(3), 79);
        // overflow therefore V[F] = 1
        assertEquals(target.getV(0xF), 1);
    }

    @Test
    public void testSubBorrow() { // 8XY5
        target.setOpcode(0x8015);
        target.setV(0, 0);
        target.setV(1, 2);
       
        target.subBorrow();
        
        // V[0] -= V[1] = -2 = -254
        assertEquals(target.getV(0), 254);
    }

    @Test
    public void testShiftRight() { // 8XY6
        target.setOpcode(0x8A56);
        target.setV(0xA, 187);
        
        target.shiftRight();
        
        // V[F] = LSB V[A] = 1
        assertEquals(target.getV(0xF), 1);
        // V[A] = 187 >> 1 = 93
        assertEquals(target.getV(0xA), 93);      
    }

    @Test
    public void testSetSubBorrow() { // 8XY7
        target.setOpcode(0x8357);
        target.setV(3, 124);
        target.setV(5, 40);
        
        target.setSubBorrow();
        
        // 40 - 124 = -84 = 172 underflow, V[F] = 0
        assertEquals(target.getV(3), 172);
        assertEquals(target.getV(0xF), 0);     
    }

    @Test
    public void testShiftLeft() { // 8XYE
        target.setOpcode(0x89EE);      
        target.setV(9, 212);
        
        target.shiftLeft();
        
        // V[F] = MSB V[9] = 128
        assertEquals(target.getV(0xF), 128);
        // V[9] = 212 << 1 = 424
        assertEquals(target.getV(9), 424);    
    }

    @Test
    public void testSkip() { // 9XY0
        target.setOpcode(0x9340);
        target.setV(3, 50);
        target.setV(4, 100);
        
        target.skip();
        // V[3] != V[4], pc += 4 = 516
        assertEquals(target.getPc(), 516);
    }

    @Test
    public void testSetIndex() { // ANNN
        target.setOpcode(0xA932);
        
        target.setIndex();
        
        // index = 0x932 = 2354
        assertEquals(target.getIndex(), 2354);
    }

    @Test
    public void testJumpV() { // BNNN
        target.setOpcode(0xB932);
        target.setV(0, 356);
        
        target.jumpV();
        
        // pc = opcode + V[0] = 0x932 + 356 = 2710
        assertEquals(target.getPc(), 2710);
    }

    @Test
    public void testDrawSprite() { // DXYN
        target.setOpcode(0xDAE1);
        target.setV(0xA, 3);
        target.setV(0xE, 4);
        
        // create sprite
        int index = target.getIndex();
        target.setMemoryVal(index, 0x3C);
        
        target.drawSprite();
        
        int[][] gfx = target.getGfx();
        // 0x3C = 00111100, start at 3,4 with 1 height
        assertEquals(gfx[3][4], 0);
        assertEquals(gfx[4][4], 0);
        assertEquals(gfx[5][4], 1);
        assertEquals(gfx[6][4], 1);
        assertEquals(gfx[7][4], 1);
        assertEquals(gfx[8][4], 1);
        assertEquals(gfx[9][4], 0);
        assertEquals(gfx[10][4], 0);
    }

    @Test
    public void testSkipKeyPressed() { // EX9E
        target.setOpcode(0xE49E);
        target.setV(4, 5);
        // simulate key press
        target.setKey(5, 1);
        
        target.skipKeyPressed();
        // key pressed, pc += 4 = 516
        assertEquals(target.getPc(), 516);
    }

    @Test
    public void testSkipKeyNotPressed() { // EXA1
        target.setOpcode(0xE49E);
        target.setV(4, 5);
        
        target.skipKeyPressed();
        // key not pressed, pc += 2 = 514
        assertEquals(target.getPc(), 514);
    }

    @Test
    public void testSetXDelay() { // FX07
        target.setOpcode(0xF407);
        target.setDelayTimer(35);
        
        target.setXDelay();
        
        // V[4] = delay = 35
        assertEquals(target.getV(4), 35);
    }

    @Test
    public void testKeyWait() { // FX0A
        target.setOpcode(0xF30A);       
        target.setKey(9, 1);
        
        target.keyWait();
        
        // V[3] = 9, pc +=2 = 514
        assertEquals(target.getV(3), 9);
        assertEquals(target.getPc(), 514);
    }

    @Test
    public void testSetDelayX() { // FX15
        target.setOpcode(0xF915);
        target.setV(9, 23);
        
        target.setDelayX();
        
        // delay = V[9] = 23
        assertEquals(target.getDelayTimer(), 23);
    }

    @Test
    public void testSetSoundX() { // FX18
        target.setOpcode(0xF318);
        target.setV(3, 42);
        
        target.setSoundX();
        
        // sound = V[3] = 42
        assertEquals(target.getSoundTimer(), 42);
    }

    @Test
    public void testAddToIndex() { // FX1E
        target.setOpcode(0xF51E);
        target.setV(5, 32);
        
        target.addToIndex();
        
        // index += V[5] = 32
        assertEquals(target.getIndex(), 32);
    }

    @Test
    public void testSetSpriteIndex() { // FX29
        target.setOpcode(0xF229);
        target.setV(2, 4);
        
        target.setSpriteIndex();
        
        // index = V[2] * 5 = 20
        assertEquals(target.getIndex(), 20);       
    }

    @Test
    public void testStoreBCD() { // FX33
        target.setOpcode(0xF633);
        target.setV(6, 243);
        
        target.storeBCD();
        int index = target.getIndex();
        // 243 = 2, 4, 3
        assertEquals(target.getMemoryVal(index), 2);
        assertEquals(target.getMemoryVal(index+1), 4);
        assertEquals(target.getMemoryVal(index+2), 3);      
    }

    @Test
    public void testMemStore() { // FX55
        target.setOpcode(0xF255);
        target.setV(0, 43);
        target.setV(1, 132);
        target.setV(2, 14);
        
        target.memStore();
        int index = target.getIndex();
        
        // M[i] = V[0], M[i+1] = V[2], M[i+2] = V[3]
        assertEquals(target.getMemoryVal(index), 43);
        assertEquals(target.getMemoryVal(index+1), 132);
        assertEquals(target.getMemoryVal(index+2), 14);  
    }

    @Test
    public void testMemFill() { // FX65
        target.setOpcode(0xF265);
        int index = target.getIndex();
        target.setMemoryVal(index, 213);
        target.setMemoryVal(index+1, 112);
        target.setMemoryVal(index+2, 453);
        
        target.memFill();
        
        // V[0] = M[i], V[1] = M[i+1], V[2] = M[i+2]
        // 453 -> 197 as V regs are 8bit
        assertEquals(target.getV(0), 213);
        assertEquals(target.getV(1), 112);
        assertEquals(target.getV(2), 197);  
    }
}
