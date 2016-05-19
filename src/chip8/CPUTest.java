package chip8;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CPUTest {
    
    private CPU target;
    
    @Before
    public void init() {
        target = new CPU();
        target.init();
    }
    
    @Test
    public void testClear() { // 00E0
        // add pixel
        target.setGfx(15, 24);
 
        // call method
        target.clear();
        
        // retrieve result
        int[][] gfx = target.getGfx();
        
        // confirm action
        boolean valid = true;
        for (int x = 0; x < 64; x ++) {
            for (int y = 0; y < 32; y ++) {
                if (gfx[x][y] == 1) {
                    valid = false;
                }
            }
        }
        
        assertEquals(valid, true);
    }

    @Test
    public void testSubReturn() { // 00EE
        // create an example sub call
        // stack[0] = 50
        target.setStack(0, 50);
        // sp++
        target.setSp(1);
        // pc = opcode (0x2NNN sub call) & 0x0FFF
        target.setPc((0x2FE7 & 0x0FFF));
        
        // test the sub return
        target.subReturn();
        // sp--
        assertEquals(0, target.getSp());
        // pc = stack[sp] + 2
        assertEquals(52, target.getPc());
    }
/*
    @Test
    public void testJump() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testCallSub() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkipIfEqualN() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkipIfNotEqualN() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkipIfEqualV() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSet() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testAdd() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetVal() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testOr() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testAnd() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testXor() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testAddCarry() {
        fail("Not yet implemented"); // TODO
    }
*/
    @Test
    public void testSubBorrow() { // 8XY5
        // V[0] = 0, V[1] = 2
        target.setV(0, 0);
        target.setV(1, 2);
        target.setOpcode(0x8015);
       
        target.subBorrow();
        
        // V[0] -= V[1] = -2 = -254
        assertEquals(254, target.getV(0));
    }
/*
    @Test
    public void testShiftRight() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetSubBorrow() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testShiftLeft() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkip() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetIndex() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testJumpV() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testRandAnd() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testDrawSprite() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkipKeyPressed() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSkipKeyNotPressed() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetXDelay() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testKeyWait() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetDelayX() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetSoundX() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testAddToIndex() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetSpriteIndex() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testStoreBCD() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testMemStore() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testMemFill() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testUpdateTimers() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testLoadROM() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testIsDrawFlag() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetDrawFlag() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testGetGfx() {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public void testSetKey() {
        fail("Not yet implemented"); // TODO
    }
*/
}
