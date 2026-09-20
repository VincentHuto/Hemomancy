package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.client.sound.ClairaudiographSounds;
import com.vincenthuto.hemomancy.client.sound.SpectrogramAnalysis;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Audio and unresolved displacement deliberately have separate scales and display surfaces. */
final class ClairaudiographSpectrograph implements AutoCloseable {
    private final NativeImage pixels = new NativeImage(116,64,false);
    private final DynamicTexture texture = new DynamicTexture(pixels);
    private final ResourceLocation location = Minecraft.getInstance().getTextureManager().register("clairaudiograph_spectrum",texture);
    private int lastFrame=Integer.MIN_VALUE;
    private SpectrogramAnalysis lastAnalysis;
    void render(GuiGraphics graphics,int x,int y,ClairaudiographSounds.View view) {
        var mc=Minecraft.getInstance();
        graphics.fill(x,y,x+144,y+226,0xFF0B0708);
        graphics.renderOutline(x,y,144,226,0xFF663B2A);
        label(graphics,"spectrograph",x+7,y+5,0xFFD7B88A);
        graphics.drawString(mc.font,"12k",x+3,y+24,0xFF96846D,false);
        graphics.drawString(mc.font,"50",x+3,y+81,0xFF96846D,false);
        int current=(int)(view.seconds()*30);
        if(current!=lastFrame || view.analysis()!=lastAnalysis) {
            lastFrame=current; lastAnalysis=view.analysis();
            for(int col=0;col<116;col++) for(int row=0;row<64;row++) {
                double time=view.seconds()-8+col*8.0/115;
                int band=SpectrogramAnalysis.bandFor(SpectrogramAnalysis.frequency(63-row)/view.pitch());
                float energy=view.analysis()==null || time<0?0:view.analysis().energy(view.analysis().frameAt(time,view.pitch()),band);
                int r=(int)(12+energy*243),g=(int)(9+energy*180),b=(int)(9+energy*65);
                pixels.setPixelRGBA(col,row,0xFF000000 | b<<16 | g<<8 | r);
            }
            texture.upload();
        }
        graphics.blit(location,x+26,y+25,0,0,116,64,116,64);
        graphics.drawString(mc.font,"-8s",x+26,y+91,0xFF96846D,false);
        graphics.drawString(mc.font,"0s",x+128,y+91,0xFF96846D,false);
        graphics.drawString(mc.font,"-80 ... 0 dBFS",x+28,y+102,0xFF96846D,false);
        label(graphics,"waveform",x+7,y+115,0xFFD7B88A);
        graphics.hLine(x+7,x+136,y+139,0xFF42332A);
        if(view.analysis()!=null) for(int col=0;col<130;col++) {
            double time=view.seconds()-8+col*8.0/129;
            int height=(int)(view.analysis().amplitude(view.analysis().frameAt(time,view.pitch()))*9);
            graphics.vLine(x+7+col,y+139-height,y+139+height,0xFFDCA35B);
        }
        boolean anomalous=view.program()!=null && view.program().id().equals("severed_record");
        if(anomalous) {
            boolean known=mc.player!=null && HemoCapabilityAccess.antecedent(mc.player).has(AntecedentResearch.Evidence.CONTROLLED_REPLAY);
            label(graphics,known?"unresolved":"displacement",x+7,y+153,0xFF65A8AC);
            for(int col=0;col<130;col++) {
                double time=view.seconds()-8+col*8.0/129;
                boolean strong=time>=55 && time<68;
                double magnitude=strong?9:time>=0 && time<55?2:0;
                int dy=(int)(Math.sin(time*11)*Math.cos(time*3.4)*magnitude);
                graphics.fill(x+7+col,y+178+dy,x+8+col,y+179+dy,0xFF6CCEC4);
            }
        } else label(graphics,view.status(),x+7,y+157,0xFF96846D);
        if(view.program()!=null) {
            String caption=view.program().caption((int)(view.seconds()*20));
            if(!caption.isEmpty()) {
                graphics.pose().pushPose();graphics.pose().translate(x+7,y+191,0);graphics.pose().scale(.65F,.65F,1);
                graphics.drawWordWrap(mc.font,Component.translatable(caption),0,0,200,0xFFD6D0C3);graphics.pose().popPose();
            }
            if(!view.status().equals("playing")) label(graphics,view.status(),x+7,y+13,0xFF96846D);
        }
    }
    private static void label(GuiGraphics graphics,String key,int x,int y,int color) {
        var font=Minecraft.getInstance().font;
        graphics.drawString(font,font.plainSubstrByWidth(Component.translatable("gui.hemomancy.clairaudiograph."+key).getString(),130),x,y,color,false);
    }
    @Override public void close() { Minecraft.getInstance().getTextureManager().release(location); }
}
