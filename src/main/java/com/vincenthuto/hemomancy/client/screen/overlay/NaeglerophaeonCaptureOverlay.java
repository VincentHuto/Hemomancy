package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Local victim view grade. Server-synced grab state determines when it can appear. */
public final class NaeglerophaeonCaptureOverlay {
    private static ClientLevel trackedLevel;
    private static int bossId=-1;
    private static long lastScanTick=Long.MIN_VALUE;
    private static double lastRenderTime=Double.NaN;
    private static float intensity;

    private NaeglerophaeonCaptureOverlay() {}

    public static void clear() {
        trackedLevel=null;
        bossId=-1;
        lastScanTick=Long.MIN_VALUE;
        lastRenderTime=Double.NaN;
        intensity=0;
    }

    public static boolean markEyes(NaeglerophaeonEntity boss) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level!=boss.level() || mc.player==null
                || ShaderInit.SANGUINE_OMEN_WORLD.getInstance().get()==null) return false;
        return capturedBy(boss,mc.player);
    }

    public static boolean renderWorldGrade(GuiGraphics graphics,int width,int height,float partialTick) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null || width<=0 || height<=0) {
            clear();
            return false;
        }
        if(trackedLevel!=mc.level) {
            clear();
            trackedLevel=mc.level;
        }

        NaeglerophaeonEntity boss=findCapturingBoss(mc.level,mc.player);
        if(boss==null) {
            bossId=-1;
            intensity=0;
            lastRenderTime=Double.NaN;
            return false;
        }
        double distance=mc.player.getEyePosition(partialTick)
                .distanceTo(boss.getPosition(partialTick).add(0,.8,0));
        float proximity=Mth.clamp((float)((12-distance)/10.2),0,1);
        float target=.06F+.94F*(float)Math.pow(proximity,1.25);
        double time=mc.level.getGameTime()+partialTick;
        double elapsed=Double.isNaN(lastRenderTime)?1:Math.clamp(time-lastRenderTime,0,5);
        intensity+=(target-intensity)*(float)(1-Math.exp(-.3F*elapsed));
        lastRenderTime=time;

        if(intensity<=0.001F || ShaderInit.SANGUINE_OMEN_WORLD.getInstance().get()==null
                || SanguineOmenOverlay.instance==null) {
            return false;
        }
        SanguineOmenOverlay.instance.renderWorldGradeAtIntensity(graphics,width,height,partialTick,
                intensity,true);
        return true;
    }

    private static NaeglerophaeonEntity findCapturingBoss(ClientLevel level,Player player) {
        Entity cached=bossId<0?null:level.getEntity(bossId);
        if(cached instanceof NaeglerophaeonEntity boss && capturedBy(boss,player)) return boss;
        long tick=level.getGameTime();
        if(lastScanTick==tick) return null;
        lastScanTick=tick;
        for(NaeglerophaeonEntity boss:level.getEntitiesOfClass(NaeglerophaeonEntity.class,
                player.getBoundingBox().inflate(20),candidate->capturedBy(candidate,player))) {
            bossId=boss.getId();
            return boss;
        }
        return null;
    }

    private static boolean capturedBy(NaeglerophaeonEntity boss,Player player) {
        return boss.isAlive() && boss.isGrabbing() && boss.grabbedEntityId()==player.getId();
    }
}
