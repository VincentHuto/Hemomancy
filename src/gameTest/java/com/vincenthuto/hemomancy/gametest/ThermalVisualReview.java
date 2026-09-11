package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;

/** Paired material studies in the existing disposable save; actual casts have a separate review. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID, value=Dist.CLIENT)
public final class ThermalVisualReview {
    private static final Form[] FORMS = {Form.FURNACE, Form.GLASS_CHARGE, Form.GLASS, Form.UPDRAFT,
            Form.FORGE, Form.CAUTERIZE, Form.PHOENIX, Form.PHOENIX_READY, Form.ICE, Form.ICE_CHARGE,
            Form.BONE, Form.STILLNESS, Form.HOUR};
    private static int ticks=Integer.getInteger("hemomancy.thermalReviewStartMode",0)*FORMS.length*100;
    private static int frame=Integer.getInteger("hemomancy.thermalReviewStartMode",0)*FORMS.length*44;
    private static volatile int source = -1;
    private static long lastFrame;
    private static final List<Double> active = new ArrayList<>(), idle = new ArrayList<>();
    private ThermalVisualReview() {}

    @SubscribeEvent public static void frame(RenderLevelStageEvent event) {
        if (!Boolean.getBoolean("hemomancy.thermalReview") || event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        long now=System.nanoTime();
        int phase=ticks%100;
        // Measurements have their own pass: screenshot readback is excluded entirely.
        if (Boolean.getBoolean("hemomancy.thermalBenchmark") && lastFrame!=0 && ticks>100) {
            if (phase>=20 && phase<=65) active.add((now-lastFrame)/1e6);
            if (phase>=85) idle.add((now-lastFrame)/1e6);
        }
        lastFrame=now;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.thermalReview")) return;
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null)return;
        if(!mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Thermal review requires the disposable VisceralReview save");
        mc.options.pauseOnLostFocus=false; mc.options.hideGui=true;
        if(Boolean.getBoolean("hemomancy.thermalBenchmark")) {
            mc.options.enableVsync().set(false);
            mc.options.framerateLimit().set(260);
        }
        if(mc.screen!=null)mc.setScreen(null);
        int tick=ticks++,scene=tick/100,phase=tick%100;
        String label=System.getProperty("hemomancy.thermalReviewLabel","after");
        int modes=Integer.getInteger("hemomancy.thermalReviewModes",3);
        if(scene>=FORMS.length*modes) {
            org.slf4j.LoggerFactory.getLogger(ThermalVisualReview.class).info("THERMAL REVIEW COMPLETE label={} frames={} active={} idle={}",label,frame,stats(active),stats(idle));
            mc.stop();return;
        }
        int mode=scene/FORMS.length;
        Form form=FORMS[scene%FORMS.length];
        double x=6000+scene*32+.5;
        if(phase==0) {
            mc.options.setCameraType(mode==2?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);
            mc.options.particles().set(mode==2?ParticleStatus.MINIMAL:mode==1?ParticleStatus.DECREASED:ParticleStatus.ALL);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if(player==null)return;
                var level=player.serverLevel();
                level.setDayTime(mode==1?18000:6000);level.setWeatherParameters(6000,0,false,false);
                for(int dx=-9;dx<=(mode==3?17:9);dx++)for(int dz=mode==3?-17:-5;dz<=16;dz++)
                    level.setBlockAndUpdate(new BlockPos((int)x+dx,99,dz),((dx+dz)%2==0?Blocks.STONE:Blocks.POLISHED_DEEPSLATE).defaultBlockState());
                for(int y=100;y<=103;y++)for(int dx=-4;dx<=4;dx++)level.setBlockAndUpdate(new BlockPos((int)x+dx,y,12),Blocks.STONE.defaultBlockState());
                if(mode==1)for(int dx=1;dx<=4;dx++)level.setBlockAndUpdate(new BlockPos((int)x+dx,100,5),Blocks.STONE_SLAB.defaultBlockState());
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level,x+(mode==3?15:mode==4?2:5),100,mode==3?-15:mode==4?2:-1,35,mode==3?8:mode==4?20:14);
                var target=EntityType.HUSK.create(level);target.setPos(x,100,5);target.setNoAi(true);level.addFreshEntity(target);source=target.getId();
                org.slf4j.LoggerFactory.getLogger(ThermalVisualReview.class).info("THERMAL SCENE {} {} {}",label,mode,form);
            });
            if(mode==1 && scene==FORMS.length)mc.reloadResourcePacks();
        }
        if(phase==10 || phase==30 || phase==50 || phase==70)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            Vec3 at=new Vec3(x,100,5),end=at.add(0,1.5,6);
            boolean burst=form==Form.GLASS || form==Form.PHOENIX || form==Form.ICE;
            if(burst && phase!=10)return;
            float radius=form.name().endsWith("_CHARGE")?.85f:form==Form.HOUR?8:3;
            PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(form,burst?-1:source,at,end,radius,phase==70?0:burst?58:28,form==Form.UPDRAFT?4:1));
            if(mode==2 && phase==10)PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(Form.LUX_MIST,-1,at.add(1,1,0),at,1,55,1));
        });
        if(!Boolean.getBoolean("hemomancy.thermalBenchmark") && phase>=6 && phase<=92 && phase%2==0) {
            Screenshot.grab(mc.gameDirectory,String.format(java.util.Locale.ROOT,"thermal-%s-%05d.png",label,frame++),mc.getMainRenderTarget(),message->{});
        }
    }
    private static String stats(List<Double> samples) {
        if(samples.isEmpty())return "none";
        var sorted=samples.stream().sorted().toList();
        return String.format(java.util.Locale.ROOT,"n=%d mean=%.2f p95=%.2f",sorted.size(),sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0),sorted.get((int)(sorted.size()*.95)));
    }
}
