package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.List;

/** Destructive only inside the explicitly opt-in disposable acceptance server. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class ManipulationRemoteAcceptance {
    private static final String[] POWERS={"learn", "blood_shot", "hematic_flare", "hematic_flare",
            "white_verdict", "white_verdict", "crimson_coronation", "vitric_combustion",
            "eclipse_well", "absolute_stillness", "funeral_bell"};
    private static int tick, failures, elapsed;
    private static boolean started;
    private static Husk enemy;
    private static Wolf ally;
    private static double beforeBlood;

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.remoteAcceptance"))return;
        var server=event.getServer();
        if(++elapsed>12000){Hemomancy.LOGGER.error("AREA6 TIMEOUT before completion");server.halt(false);return;}
        if(started && tick>=POWERS.length*200+80){server.halt(false);return;}
        if(started && tick>=POWERS.length*200){tick++;return;}
        var caster=server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("Area6Caster")).findFirst().orElse(null);
        var observer=server.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("Area6Observer")).findFirst().orElse(null);
        if(caster==null || observer==null){if(!started)server.overworld().setDayTime(0);return;}
        if(!started){started=true;Hemomancy.LOGGER.info("AREA6 two real clients connected; beginning disposable fixtures");}
        int scene=tick/200, phase=tick++%200;
        if(phase==0)prepare(caster,observer,scene);
        if(scene==POWERS.length-1 && phase==199){
            Hemomancy.LOGGER.info("AREA6 COMPLETE failures={}",failures);
            for(var player:List.of(caster,observer))player.sendSystemMessage(net.minecraft.network.chat.Component.literal("AREA6_SCENE "+POWERS.length));
        }
        if(scene==6 && phase==85 && enemy!=null) {
            caster.hurt(caster.damageSources().mobAttack(enemy),2);
        }
        if(phase==150) {
            var known=HemoCapabilityAccess.requireKnownManipulations(caster);
            double blood=HemoCapabilityAccess.requireBloodVolume(caster).getBloodVolume();
            if(scene==0)check(known.getKnownManips().containsKey(ManipulationInit.blood_shot.get())
                    && caster.getMainHandItem().isEmpty() && blood<beforeBlood-490,"crude memory use learned Shot, consumed shard and paid 500 mL");
            if(scene==1)check(enemy!=null && enemy.getHealth()<20,"network Blood Shot damaged ordinary hostile");
			if(scene==2 || scene==3)check(ManipulationInit.getByName(POWERS[scene]).getRemainingCooldownTicks(caster) == 0L
                    && blood>=beforeBlood-1,"low blood / invalid target did not start cooldown or charge blood: "+scene);
            if(scene==4) {
                check(observer.getHealth()==20,"PvP-disabled remote player remained unharmed inside beam");
                check(ally!=null && ally.getHealth()==ally.getMaxHealth(),"owned ally remained unharmed inside beam");
                check(enemy!=null && enemy.getHealth()<20,"same beam hit hostile beyond protected targets");
            }
            if(scene>=5)check(observer.getHealth()==20,"remote observer unharmed: "+POWERS[scene]);
            if(scene>=5)check(blood<beforeBlood-10,"network cast paid and executed: "+POWERS[scene]);
            Hemomancy.LOGGER.info("AREA6 scene={} power={} bloodBefore={} bloodAfter={} enemyHP={} observerHP={}",
                    scene,POWERS[scene],beforeBlood,blood,enemy==null?-1:enemy.getHealth(),observer.getHealth());
        }
    }
    private static void check(boolean result,String label) {
        if(!result)failures++;
        Hemomancy.LOGGER.info("AREA6 {} {}",result?"PASS":"FAIL",label);
    }
    private static void prepare(ServerPlayer caster,ServerPlayer observer,int scene) {
        var level=caster.serverLevel();
        ManipulationChannelManager.stop(caster);
        BloodManipulation.clearSessionState();
        ManipulationVisuals.attached(caster,ManipulationVisuals.Form.CROWN,0,0,0);
        if(enemy!=null)enemy.discard();if(ally!=null)ally.discard();enemy=null;ally=null;
        int x=scene*40;
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,level.getServer());
        level.setWeatherParameters(10000,0,false,false);
        for(int dx=-10;dx<=10;dx++)for(int dz=-6;dz<=12;dz++) {
            var pos=new BlockPos(x+dx,99,dz);
            level.setBlockAndUpdate(pos,((dx+dz)%2==0?Blocks.STONE:Blocks.POLISHED_DEEPSLATE).defaultBlockState());
            for(int dy=1;dy<=5;dy++)level.setBlockAndUpdate(pos.above(dy),Blocks.AIR.defaultBlockState());
        }
        for(int y=0;y<4;y++)level.setBlockAndUpdate(new BlockPos(x,100+y,10),Blocks.STONE.defaultBlockState());
        caster.teleportTo(level,x+.5,100,.5,0,0);observer.teleportTo(level,x+5.5,100,4.5,90,0);
        caster.setGameMode(GameType.SURVIVAL);observer.setGameMode(GameType.SURVIVAL);
        caster.removeAllEffects();observer.removeAllEffects();caster.setHealth(20);observer.setHealth(20);
        caster.getFoodData().setFoodLevel(20);observer.getFoodData().setFoodLevel(20);
        caster.getInventory().clearContent();caster.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var volume=HemoCapabilityAccess.requireBloodVolume(caster);volume.setActive(true);volume.setMaxBloodVolume(6000);volume.setBloodVolume(scene==2?0:5000);
        var known=HemoCapabilityAccess.requireKnownManipulations(caster);
        HemoCapabilityAccess.getInitiatoryDegree(caster).orElseThrow().setDegreeNumber(scene==0?0:7);
        if(scene==0) {
            known.getKnownManips().clear();known.setEquippedManipNames(List.of());
            caster.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemInit.crude_memory_blood_shot.get()));
        } else {
            var power=ManipulationInit.getByName(POWERS[scene]);
            if(scene!=1)known.getKnownManips().put(power,new ManipLevel(0,0));
            known.setEquippedManipNames(List.of(power.getName()));known.setSelectedManip(power);
            HemoCapabilityAccess.getBloodTendency(caster).orElseThrow().setTendencyAlignment(power.getTend(),(float)power.getAlignLevel());
        }
        if(scene!=0 && scene!=3) {
            enemy=EntityType.HUSK.create(level);enemy.setPos(x+.5,100,7.5);enemy.setNoAi(true);level.addFreshEntity(enemy);
        }
        if(scene==4) {
            observer.teleportTo(level,x+.5,100,3.5,180,0);
            ally=EntityType.WOLF.create(level);ally.setPos(x+.5,100,5.5);ally.tame(caster);ally.setNoAi(true);level.addFreshEntity(ally);
        }
        PacketHandler.sendToPlayer(caster,new KnownManipulationServerPacket(known));
        PacketHandler.sendToPlayer(caster,new BloodVolumeServerPacket(volume));
        beforeBlood=volume.getBloodVolume();
        level.setDayTime(6000);
        for(var player:List.of(caster,observer))player.sendSystemMessage(net.minecraft.network.chat.Component.literal("AREA6_SCENE "+scene));
        Hemomancy.LOGGER.info("AREA6 START scene={} power={}",scene,POWERS[scene]);
    }

    @EventBusSubscriber(modid=Hemomancy.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
    public static final class Client {
        private static int scene=-1, ticks;
        @SubscribeEvent public static void message(net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) {
            if(!Boolean.getBoolean("hemomancy.remoteAcceptanceClient"))return;
            String message=event.getMessage().getString();
            for(int next=0;next<=POWERS.length;next++)if(message.equals("AREA6_SCENE "+next)) {
                scene=next;ticks=0;
                Hemomancy.LOGGER.info("AREA6 client phase={}",scene);
                event.setCanceled(true);return;
            }
        }
        @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            if(!Boolean.getBoolean("hemomancy.remoteAcceptanceClient"))return;
            var mc=net.minecraft.client.Minecraft.getInstance();
            if(mc.level==null || mc.player==null)return;
            mc.options.pauseOnLostFocus=false;
            if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
            if(scene<0)return;
            if(scene>=POWERS.length){mc.stop();return;}
            ticks++;
            boolean caster=mc.getUser().getName().equals("Area6Caster");
            if(caster && scene>0 && ticks>=20 && ticks<=32 && ticks%4==0) {
                var power=ManipulationInit.getByName(POWERS[scene]);
                if(power.getType()==EnumManipulationType.CHARGED)
                    PacketHandler.sendToServer(new ManipulationChargeVisualPacket((ticks-16)*power.getRequiredChargeTicks()/16));
            }
            if(caster && ticks==40) {
                if(scene==0)mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);
                else {
                    var power=ManipulationInit.getByName(POWERS[scene]);
                    if(power.getType()==EnumManipulationType.CONTINUOUS)PacketHandler.sendToServer(UseManipKeyPacket.startContinuous());
                    else {PacketHandler.sendToServer(new ManipulationChargeVisualPacket(0));PacketHandler.sendToServer(new UseManipKeyPacket(power.getRequiredChargeTicks()));}
                }
            }
            if(caster && ticks==100)PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
            if(ticks==30 || ticks==44 || ticks==48 || ticks==55 || ticks==87 || ticks==92 || ticks==160)
                net.minecraft.client.Screenshot.grab(mc.gameDirectory,"remote-"+scene+"-"+ticks+".png",mc.getMainRenderTarget(),message->{});
        }
    }
}
