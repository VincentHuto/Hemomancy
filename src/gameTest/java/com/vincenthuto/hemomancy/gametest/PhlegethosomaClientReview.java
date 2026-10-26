package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.UUID;

/** Disposable dry-profile and ichor-swimming review. Fixture assists are not Survival evidence. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PhlegethosomaClientReview {
    private static boolean started;
    private static UUID observer;
    private static volatile PhlegethosomaEntity boss;
    private static Cow victim;
    private static volatile int ticks;
    private static int captured=-1;
    @SubscribeEvent public static void client(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.phlegethosomaReview"))return;
        var mc=Minecraft.getInstance();
        if(!started && mc.player!=null && mc.getSingleplayerServer()!=null) {
            started=true;observer=mc.player.getUUID();mc.options.pauseOnLostFocus=false;
            mc.options.fov().set(45);mc.options.hideGui=true;
            mc.getSingleplayerServer().execute(()-> {
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(observer);
                var level=player.serverLevel();player.setGameMode(GameType.CREATIVE);
                player.getAbilities().flying=true;player.onUpdateAbilities();player.setNoGravity(true);
                for(int x=30;x<=33;x++)for(int z=30;z<=33;z++)level.setChunkForced(x,z,true);
                for(int x=488;x<=532;x++)for(int z=488;z<=532;z++) {
                    level.setBlock(new BlockPos(x,199,z),Blocks.BLACKSTONE.defaultBlockState(),3);
                    for(int y=200;y<=210;y++) {
                        boolean pool=x>=490 && x<=530 && z>=490 && z<=530;
                        var state=y<=203?(pool?BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState():Blocks.BLACKSTONE.defaultBlockState()):Blocks.AIR.defaultBlockState();
                        level.setBlock(new BlockPos(x,y,z),state,3);
                    }
                }
                for(int x=502;x<=508;x++)for(int z=492;z<=507;z++)level.setBlock(new BlockPos(x,203,z),Blocks.BLACKSTONE.defaultBlockState(),3);
                var fluke=EntityInit.phlegethosoma.get().create(level);
                fluke.setPos(505,204,504);fluke.setNoAi(true);level.addFreshEntity(fluke);
                victim=EntityType.COW.create(level);victim.setPos(515,204.05,504);victim.setNoAi(true);
                victim.setNoGravity(true);victim.setInvulnerable(true);level.addFreshEntity(victim);
                level.setDayTime(6000);player.teleportTo(level,516,207,516,145,16);
                boss=fluke;
            });
        }
        if(started && mc.player!=null) {
            mc.player.getAbilities().flying=true;mc.player.setNoGravity(true);
            mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
        if(started && boss!=null && ticks%2==0 && ticks!=captured && ticks<900) {
            captured=ticks;
            Screenshot.grab(mc.gameDirectory,"phlegethosoma-breach-"+String.format("%04d",ticks)+".png",mc.getMainRenderTarget(),message->{});
        }
        if(ticks>940)mc.stop();
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.phlegethosomaReview") || boss==null)return;
        var level=(net.minecraft.server.level.ServerLevel)boss.level();
        if(level.getEntity(boss.getUUID()) instanceof PhlegethosomaEntity live)boss=live;
        else if(ticks<880)return;
        if(level.getEntity(victim.getUUID()) instanceof Cow liveVictim)victim=liveVictim;
        var player=level.getServer().getPlayerList().getPlayer(observer);
        if(player!=null && ticks%2==0)player.teleportTo(level,516,207,516,145,16);
        ticks++;
        if(ticks%10==0)Hemomancy.LOGGER.info("FLUKE_BREACH_REVIEW tick={} id={} noAi={} action={} pos={} speed={}",ticks,boss.getId(),boss.isNoAi(),boss.action(),boss.position(),boss.getDeltaMovement().length());
        if(ticks==140) {
            for(int x=502;x<=508;x++)for(int z=492;z<=507;z++)boss.level().setBlock(new BlockPos(x,203,z),BlockInit.PHLEGETHONTIC_ICHOR_BLOCK.get().defaultBlockState(),3);
            boss.setPos(505,203.7,504);boss.setNoAi(false);boss.setTarget(victim);
        }
        if(ticks==340)victim.setPos(518,204.05,522);
        if(ticks==380)boss.setHealth(70);
        if(ticks==500)victim.setPos(493,204.05,514);
        if(ticks==600)boss.setHealth(28);
        if(ticks==700)victim.setPos(519,204.05,496);
        if(ticks==880)boss.kill();
        if(ticks==920) {
            for(int x=30;x<=33;x++)for(int z=30;z<=33;z++)level.setChunkForced(x,z,false);
            Hemomancy.LOGGER.info("PHLEGETHOSOMA_BREACH_REVIEW_FINISHED");
        }
    }
}
