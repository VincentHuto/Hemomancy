package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.*;
public final class PhlegethosomaFrame implements BossBarFrame {
    @Override public int fillTop(int phase) {return 0xFFE54A32;}
    @Override public int fillBottom(int phase) {return 0xFF661226;}
    @Override public int nameColor(int phase) {return 0xE4B4A5;}
    @Override public float[] notches() {return new float[]{.25F,.6F};}
    @Override public void paintBack(BossBarFrameContext c) {
        BossBarFramePainter.rails(c,0,0xFF180D13,0xFF4C1721,0xFF9C4233);
        for(int i=0;i<9;i++)c.canvas().disc(c.x()+i*22,c.y(),3,2,8,0xFF37121B,0xFF6C2630);
    }
}
