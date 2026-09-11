package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Second client for the opt-in local multiplayer cast review. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class ThermalObserverReview {
    private static boolean connected;
    private static int ticks,frames;
    private ThermalObserverReview() {}

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.thermalObserver"))return;
        var mc=Minecraft.getInstance();
        mc.options.pauseOnLostFocus=false;
        if(mc.level==null || mc.player==null) {
            if(connected) {
                org.slf4j.LoggerFactory.getLogger(ThermalObserverReview.class).info("THERMAL OBSERVER COMPLETE frames={}",frames);
                mc.stop();
            }
            return;
        }
        connected=true;mc.options.hideGui=true;mc.options.setCameraType(CameraType.FIRST_PERSON);
        if(mc.screen!=null)mc.setScreen(null);
        if(ticks++%4==0)Screenshot.grab(mc.gameDirectory,
                String.format(java.util.Locale.ROOT,"thermal-observer-%s-%05d.png",
                        System.getProperty("hemomancy.thermalObserverLabel","casts"),frames++),mc.getMainRenderTarget(),message->{});
    }
}
