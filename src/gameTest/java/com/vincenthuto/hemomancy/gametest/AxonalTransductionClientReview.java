package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in native rendering and input check, restricted to the disposable axonal-client world. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class AxonalTransductionClientReview {
    private static int ticks;
    private static boolean setupQueued;
    private static volatile boolean prepared;
    @SubscribeEvent public static void input(ClientTickEvent.Pre event) {
        if(!Boolean.getBoolean("hemomancy.axonalReview")||!prepared) return;
        var mc=Minecraft.getInstance();
        mc.options.keyUp.setDown((ticks>=40&&ticks<62)||(ticks>=72&&ticks<80));
        mc.options.keyDown.setDown(ticks>=88&&ticks<93);
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.axonalReview")) return;
        Minecraft mc=Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("axonal-client")
                || mc.player==null || mc.getSingleplayerServer()==null) return;
        mc.options.pauseOnLostFocus=false;
        if(mc.screen instanceof PauseScreen) mc.setScreen(null);
        if(!setupQueued) {
            setupQueued=true;
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            var playerId=mc.player.getUUID();
            mc.getSingleplayerServer().execute(()->setup(mc.getSingleplayerServer().getPlayerList().getPlayer(playerId)));
            return;
        }
        if(!prepared) return;
        int tick=ticks++;
        if(tick>=40&&tick<62) mc.options.keyUp.setDown(true);
        if(tick==62) mc.options.keyUp.setDown(false);
        if(tick==67) capture(mc,"axonal-camera-start");
        if(tick==68) mc.player.setYRot(90);
        if(tick>=72&&tick<80) mc.options.keyUp.setDown(true);
        if(tick==80) mc.options.keyUp.setDown(false);
        if(tick==84) capture(mc,"axonal-camera-forward");
        if(tick>=88&&tick<93) mc.options.keyDown.setDown(true);
        if(tick==93) mc.options.keyDown.setDown(false);
        if(tick==96) capture(mc,"axonal-camera-backward");
        if(tick==97) mc.player.setYRot(34);
        if(tick==100) capture(mc,"axonal-camera-branch");
        if(tick==104) mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            HemoCapabilityAccess.requireKnownManipulations(player).togglePassive("axonal_transduction");
        });
        if(tick==120) {
            capture(mc,"axonal-camera-reformed");
            mc.getSingleplayerServer().execute(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                Hemomancy.LOGGER.info("AXONAL_REVIEW recovery active={} noPhysics={} noGravity={} position={}",
                        AxonalTransductionManager.isTraveling(p),p.noPhysics,p.isNoGravity(),p.position());
            });
        }
        if(tick==140) mc.stop();
    }
    private static void capture(Minecraft mc,String name) {
        Hemomancy.LOGGER.info("AXONAL_REVIEW {} active={} camera={} yaw={} position={}",name,
                AxonalTransductionManager.isTraveling(mc.player),mc.options.getCameraType(),mc.player.getYRot(),mc.player.position());
        var playerId=mc.player.getUUID();
        mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            Hemomancy.LOGGER.info("AXONAL_REVIEW server {} active={} yaw={} position={}",name,
                    AxonalTransductionManager.isTraveling(p),p.getYRot(),p.position());
        });
        Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),message->Hemomancy.LOGGER.info("AXONAL_REVIEW {}",message.getString()));
    }
    private static void setup(ServerPlayer p) {
        var level=p.serverLevel();
        for(BlockPos b:BlockPos.betweenClosed(new BlockPos(-3,100,-3),new BlockPos(51,106,8)))
            level.setBlock(b,Blocks.STONE.defaultBlockState(),3);
        for(int x=0;x<=48;x++) level.setBlock(new BlockPos(x,102,0),
                BlockInit.nerve_fiber.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X),3);
        for(int z=1;z<6;z++) level.setBlock(new BlockPos(20,102,z),
                BlockInit.nerve_fiber.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z),3);
        for(BlockPos node:java.util.List.of(new BlockPos(0,102,0),new BlockPos(48,102,0),new BlockPos(20,102,6)))
            level.setBlock(node,BlockInit.synaptic_node.get().defaultBlockState(),3);
        for(int x:java.util.List.of(0,48)) for(BlockPos b:BlockPos.betweenClosed(new BlockPos(x-2,103,-2),new BlockPos(x+2,105,2)))
            level.setBlock(b,Blocks.AIR.defaultBlockState(),3);
        HemoCapabilityAccess.getInitiatoryDegree(p).orElseThrow().setDegreeNumber(6);
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(10000);
        HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(EnumBloodTendency.DUCTILIS,100);
        var known=HemoCapabilityAccess.requireKnownManipulations(p);
        known.getKnownManips().put(ManipulationInit.axonal_transduction.get(),new ManipLevel(4,185));
        known.setEquippedManipNames(java.util.List.of("axonal_transduction"));
        if(!known.isPassiveActive("axonal_transduction"))known.togglePassive("axonal_transduction");
        p.setPos(.5,103,3.5);AxonalTransductionManager.tick(p);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.memory_axonal_transduction.get()));
        p.teleportTo(level,.5,103,.5,-90,0);
        Hemomancy.LOGGER.info("AXONAL_REVIEW setup");
        prepared=true;
    }
}
