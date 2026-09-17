package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Only enabled by tools/casting-animation-review.init.gradle, in a disposable build directory. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class CastingClientAcceptance {
    private static int ticks,waiting;
    private static boolean initialized;
    private static long started;
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.castingReview"))return;
        var server=event.getServer();
        if(++waiting>12000) { server.halt(false);return; }
        var caster=server.getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().startsWith("CastCaster")).findFirst().orElse(null);
        var observer=server.getPlayerList().getPlayerByName("CastObserver");
        if(caster==null || observer==null)return;
        if(!initialized) {
            initialized=true;
            var level=caster.serverLevel();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            level.setDayTime(6000);level.setWeatherParameters(100000,0,false,false);
            for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++) {
                var floor=new BlockPos(x,99,z);
                level.setBlockAndUpdate(floor,Blocks.SMOOTH_QUARTZ.defaultBlockState());
                for(int y=1;y<9;y++)level.setBlockAndUpdate(floor.above(y),Blocks.AIR.defaultBlockState());
            }
            caster.teleportTo(level,.5,100,.5,0,0);
            observer.teleportTo(level,5.5,100,7.5,144,0);
            for(var p:server.getPlayerList().getPlayers()) {
                p.setGameMode(GameType.CREATIVE);
                p.removeAllEffects();
                p.getInventory().clearContent();
            }
            observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,caster.getEyePosition());
            Hemomancy.LOGGER.info("CAST_REVIEW two real clients connected");
        }
        int age=ticks++;
        if(age<80) { if(age==60)shot(server,"baseline",caster); return; }
        int scene=(age-80)/40+(Boolean.getBoolean("hemomancy.castingReviewIntegrationOnly")?46:0),frame=(age-80)%40;
        if(scene<40) {
            var school=EnumBloodTendency.values()[scene/5];
            var rank=EnumManipulationRank.values()[scene%5];
            long now=caster.level().getGameTime();
            if(frame==0)started=now;
            if(frame==0 || frame==16)send(caster,new CastingAnimationPacket(caster.getId(),caster.getUUID(),"review",
                    -1000+scene,school,null,rank,CastPurpose.AREA,CastStyle.SCHOOL,frame==0?CastPhase.OPENING:CastPhase.HOLD,started,now,20,40,InteractionHand.MAIN_HAND));
            if(frame==10)shot(server,school.name().toLowerCase()+"-"+rank.name().toLowerCase()+"-hold",caster);
            if(frame==20)shot(server,school.name().toLowerCase()+"-"+rank.name().toLowerCase()+"-held",caster);
            if(frame==22)send(caster,new CastingAnimationPacket(caster.getId(),caster.getUUID(),"review",-1000+scene,
                    school,null,rank,CastPurpose.AREA,CastStyle.SCHOOL,CastPhase.RELEASE,now,now,40,40,InteractionHand.MAIN_HAND));
            if(frame==25 && rank==EnumManipulationRank.SUMMA)shot(server,school.name().toLowerCase()+"-release",caster);
            return;
        }
        // Real channel packets plus repeated tracking entry exercise server-owned loop snapshots.
        if(scene==40) {
            if(frame==0) {
                var m=ManipulationInit.sanguine_ward.get();
                ready(caster,m);ManipulationChannelManager.start(caster);
                observer.teleportTo(observer.serverLevel(),200,100,200,0,0);
            }
            if(frame==20) {
                observer.teleportTo(observer.serverLevel(),5.5,100,7.5,144,0);
                observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,caster.getEyePosition());
            }
            if(frame==32)shot(server,"late-tracking-channel",caster);
            return;
        }
        if(scene==41) {
            if(frame==0)ManipulationChannelManager.stop(caster,false);
            if(frame==18)shot(server,"channel-cleanup",caster);
            return;
        }
        if(scene<46) {
            // Pair of simultaneous animated players, then the same scene with reduced effects.
            var school=EnumBloodTendency.FERRIC;
            for(var p:server.getPlayerList().getPlayers()) {
                long now=p.level().getGameTime();
                if(frame==0 || frame==16)send(p,new CastingAnimationPacket(p.getId(),p.getUUID(),"review-pair",
                        20000+scene,school,null,EnumManipulationRank.PERFECTUS,CastPurpose.AREA,CastStyle.SCHOOL,
                        CastPhase.HOLD,now-frame,now,20,40,InteractionHand.MAIN_HAND));
            }
            if(frame==20)shot(server,"pair-ferric-"+(scene<44?"full":"minimal")+"-"+scene,caster);
            return;
        }
        if(scene<55) {
            String name=switch(scene) {
                case 46 -> "armor"; case 47 -> "occupied-hands"; case 48 -> "shield-priority";
                case 49 -> "left-handed"; case 50 -> "walking"; case 51 -> "crouching";
                case 52 -> "cancel"; case 53 -> "release"; default -> "replace-recovery";
            };
            if(frame==0) {
                caster.stopUsingItem();
                if(scene==46) {
                    caster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
                    caster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
                }
                if(scene==47) {
                    caster.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
                    caster.setItemInHand(InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));
                }
                if(scene==48) {
                    caster.setItemInHand(InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
                    caster.startUsingItem(InteractionHand.OFF_HAND);
                }
                if(scene==49)caster.getInventory().clearContent();
                for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_MODE "+name));
            }
            long now=caster.level().getGameTime();
            var phase=scene==52?CastPhase.CANCEL:scene==53?CastPhase.RELEASE:CastPhase.HOLD;
            if(frame==0 || frame==16 && phase.sustained())send(caster,new CastingAnimationPacket(caster.getId(),caster.getUUID(),"review-integration",
                    30000+scene,EnumBloodTendency.LUX,null,EnumManipulationRank.SUMMA,CastPurpose.AREA,CastStyle.SCHOOL,
                    phase,now-frame,now,20,40,InteractionHand.MAIN_HAND));
            if(frame==(scene==52?5:20))shot(server,name,caster);
            return;
        }
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_DONE"));
        Hemomancy.LOGGER.info("CAST_REVIEW complete: 40 profiles, release frames, late tracking, cleanup and simultaneous casters");
        server.halt(false);
    }
    private static void ready(ServerPlayer p,BloodManipulation m) {
        var volume=HemoCapabilityAccess.requireBloodVolume(p);volume.setActive(true);volume.fill(5000);
        var known=HemoCapabilityAccess.requireKnownManipulations(p);known.getKnownManips().put(m,new ManipLevel(0,0));
        known.setSelectedManip(m);known.setEquippedManipNames(java.util.List.of(m.getName()));
        HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(m.getTend(),100);
    }
    private static void send(ServerPlayer p,CastingAnimationPacket packet) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,packet);
    }
    private static void shot(net.minecraft.server.MinecraftServer server,String name,ServerPlayer caster) {
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_SHOT "+name+" "+caster.getId()));
    }

    @EventBusSubscriber(modid=Hemomancy.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
    public static final class Client {
        private static long previousFrame;
        private static double sum,max;
        private static int frames;
        private static boolean reduced;
        private static String mode="";
        @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient") || event.getStage()!=net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
            long now=System.nanoTime();
            if(previousFrame!=0) {
                double ms=(now-previousFrame)/1_000_000.0;
                sum+=ms;max=Math.max(max,ms);frames++;
            }
            previousFrame=now;
        }
        @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient"))return;
            var mc=net.minecraft.client.Minecraft.getInstance();
            mc.options.pauseOnLostFocus=false;
            mc.options.framerateLimit().set(120);
            mc.options.renderDistance().set(5);
            mc.options.particles().set(reduced?net.minecraft.client.ParticleStatus.MINIMAL:net.minecraft.client.ParticleStatus.ALL);
            mc.options.hideGui=false;
            if(mc.player!=null && mc.getUser().getName().startsWith("CastCaster")) {
                mc.options.mainHand().set(mode.equals("left-handed")?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT);
                mc.options.keyUp.setDown(mode.equals("walking"));
                mc.options.keyShift.setDown(mode.equals("crouching"));
                mc.options.keyUse.setDown(mode.equals("shield-priority"));
            }
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        }
        @SubscribeEvent public static void chat(net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient"))return;
            String text=event.getMessage().getString();
            var mc=net.minecraft.client.Minecraft.getInstance();
            if(text.equals("CAST_REVIEW_DONE")) { event.setCanceled(true);mc.stop();return; }
            if(text.startsWith("CAST_REVIEW_MODE ")) {
                mode=text.substring("CAST_REVIEW_MODE ".length()); reduced=false;
                if(mode.equals("left-handed")) { mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.LEFT);mc.options.broadcastOptions(); }
                event.setCanceled(true);return;
            }
            if(!text.startsWith("CAST_REVIEW_SHOT ") || mc.level==null)return;
            String[] parts=text.split(" ");
            if(parts[1].equals("pair-ferric-full-43"))reduced=true;
            var entity=mc.level.getEntity(Integer.parseInt(parts[2]));
            var animation=entity instanceof net.minecraft.world.entity.player.Player p
                    ?com.vincenthuto.hemomancy.client.player.CastingAnimationClientState.animation(p):null;
            if(parts[1].equals("baseline")) {
                for(var p:mc.level.players())Hemomancy.LOGGER.info("CAST_REVIEW_MODEL {} {}",p.getName().getString(),p.getSkin().model());
            }
            Hemomancy.LOGGER.info("CAST_REVIEW_FRAME {} {} state={} age={} frames={} meanMs={} maxMs={}",
                    mc.getUser().getName(),parts[1],animation==null?"none":animation.packet().phase(),
                    animation==null?0:animation.playback.elapsed(mc.level.getGameTime(),0),
                    frames,frames==0?0:sum/frames,max);
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,"casting-"+parts[1]+".png",mc.getMainRenderTarget(),ignored -> {});
            frames=0;sum=0;max=0;previousFrame=0;
            event.setCanceled(true);
        }
    }
}
