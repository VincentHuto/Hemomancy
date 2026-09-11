package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.List;

/** Exercises paid casts and channel cancellation in an opt-in disposable world. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class ThermalCastReview {
    private static final String[] SPELLS={"sanguine_ignition","furnace_veins","vitric_combustion","scalding_updraft",
            "soaring_updraft","suspended_updraft","expulsive_updraft","pyretic_forge","cauterizing_rebuke",
            "crimson_flame_conjuration","cryogenic_pulse","glacial_grasp","glacial_rampart","glacial_rampart",
            "osseous_bloom","rimebound_sentence","absolute_stillness","endless_hour","conjure_torch","conjure_flail","phoenix_debt"};
    private static final String[] NPC_SPELLS={"sanguine_ignition","vitric_combustion","scalding_updraft","pyretic_forge",
            "crimson_flame_conjuration","cryogenic_pulse","glacial_grasp","glacial_rampart"};
    private static int ticks,frame;
    private static boolean published,started;
    private static volatile int targetId=-1;
    private static volatile int npcId=-1;
    private ThermalCastReview() {}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.thermalCastReview"))return;
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null)return;
        if(!mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))throw new IllegalStateException("Thermal casts require the disposable world");
        mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;if(mc.screen!=null)mc.setScreen(null);
        if(Boolean.getBoolean("hemomancy.thermalMultiplayerReview") && !started) {
            if(!published) {
                // Development clients use offline profiles in this disposable review.
                mc.getSingleplayerServer().setUsesAuthentication(false);
                published=mc.getSingleplayerServer().publishServer(GameType.CREATIVE,true,25579);
                if(!published)throw new IllegalStateException("Could not start the disposable observer server");
                org.slf4j.LoggerFactory.getLogger(ThermalCastReview.class).info("THERMAL OBSERVER SERVER READY port=25579");
            }
            if(mc.getSingleplayerServer().getPlayerList().getPlayerCount()<2)return;
            started=true;
        }
        boolean npcOnly=Boolean.getBoolean("hemomancy.thermalNpcReview");
        String[] roster=npcOnly?NPC_SPELLS:SPELLS;
        int tick=ticks++,scene=tick/120,phase=tick%120;
        if(scene>=roster.length){org.slf4j.LoggerFactory.getLogger(ThermalCastReview.class).info("THERMAL {} REVIEW COMPLETE frames={}",npcOnly?"NPC":"CAST",frame);mc.stop();return;}
        String name=roster[scene];double x=9000+scene*40+.5;
        if(phase==0) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
                var level=player.serverLevel();
                var previousNpc=level.getEntity(npcId);if(previousNpc!=null)previousNpc.discard();npcId=-1;
                ManipulationChannelManager.stop(player);BloodManipulation.clearSessionState();
                player.removeAllEffects();player.clearFire();player.setTicksFrozen(0);player.setHealth(20);player.setAbsorptionAmount(0);
                player.getInventory().clearContent();player.setGameMode(GameType.CREATIVE);player.setShiftKeyDown(scene==13);
                player.teleportTo(level,x,100,0,0,name.equals("vitric_combustion")||name.equals("crimson_flame_conjuration")||name.equals("glacial_rampart")?15:0);
                for(var observer:level.getServer().getPlayerList().getPlayers())if(observer!=player)
                    observer.teleportTo(level,x+(npcOnly?6:5),100,-1,npcOnly?40:35,14);
                level.setDayTime(scene%2==0?6000:18000);level.setWeatherParameters(6000,0,false,false);
                for(int dx=-8;dx<=8;dx++)for(int dz=-6;dz<=16;dz++)level.setBlockAndUpdate(new BlockPos((int)x+dx,99,dz),Blocks.STONE.defaultBlockState());
                for(int dx=-3;dx<=3;dx++)for(int y=100;y<=104;y++)level.setBlockAndUpdate(new BlockPos((int)x+dx,y,12),Blocks.DEEPSLATE.defaultBlockState());
                if(name.equals("glacial_grasp"))for(int dx=-2;dx<=2;dx++)for(int dz=2;dz<=8;dz++)level.setBlockAndUpdate(new BlockPos((int)x+dx,99,dz),Blocks.WATER.defaultBlockState());
                var target=EntityType.HUSK.create(level);target.setPos(x,name.equals("glacial_grasp")?99.4:100,3);target.setNoAi(true);
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);level.addFreshEntity(target);targetId=target.getId();
                if(EntityManipulationEffects.isSupported(ManipulationInit.getByName(name))) {
                    var npc=EntityType.HUSK.create(level);npc.setPos(x+1,100,0);npc.setNoAi(true);npc.setTarget(target);
                    level.addFreshEntity(npc);npcId=npc.getId();
                }
                if(npcOnly)player.teleportTo(level,x+5,100,-1,35,14);
                var blood=HemoCapabilityAccess.requireBloodVolume(player);blood.setActive(!npcOnly);
                if(!npcOnly) {
                    blood.setBloodVolume(10000);
                    for(var tendency:EnumBloodTendency.values())HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tendency,100);
                    var manipulation=ManipulationInit.getByName(name);var known=HemoCapabilityAccess.requireKnownManipulations(player);
                    known.getKnownManips().put(manipulation,new ManipLevel(4,185));known.setSelectedManip(manipulation);known.setEquippedManipNames(List.of(name));
                    if(name.equals("phoenix_debt")) {
                        if(!known.isPassiveActive(name))known.togglePassive(name);
                        HemoCapabilityAccess.getPowerGuardrails(player).setLastRiteCooldownUntil(0);
                    }
                }
                if(name.equals("pyretic_forge"))player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.RAW_IRON,3));
                if(name.startsWith("conjure_"))player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ItemInit.living_staff.get()));
                if(name.equals("cauterizing_rebuke"))player.addEffect(new MobEffectInstance(MobEffects.POISON,160));
                if(scene==0){ThermalVisualGameTests.verify(level,new Vec3(x+15,104,0));org.slf4j.LoggerFactory.getLogger(ThermalCastReview.class).info("THERMAL PACKET OBSERVER CHECKS PASSED");}
            });
        }
        if(phase==18)mc.getSingleplayerServer().execute(()->{
            ServerPlayer player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            var spell=ManipulationInit.getByName(name);double before=HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
            boolean cast;
            if(npcOnly)cast=castNpc(player,name);
            else if(spell.getType()==EnumManipulationType.CONTINUOUS){ManipulationChannelManager.start(player);cast=ManipulationChannelManager.isChanneling(player.getUUID());}
            else if(name.equals("phoenix_debt")) {
                com.vincenthuto.hemomancy.common.event.LastRiteHelper.arm(player,com.vincenthuto.hemomancy.common.event.LastRiteHelper.PHOENIX_DEBT_ID);
                var damage=new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre(player,
                        new net.neoforged.neoforge.common.damagesource.DamageContainer(player.damageSources().generic(),40));
                ManipulationReactiveEvents.onFinalDamage(damage);cast=player.getHealth()==1;
            } else cast=spell.tryPerformAction(player,player.level(),player.getMainHandItem(),player.blockPosition(),spell.getRequiredChargeTicks());
            if(!cast)throw new IllegalStateException("Thermal review cast rejected: "+name);
            org.slf4j.LoggerFactory.getLogger(ThermalCastReview.class).info("THERMAL CAST {} blood {} -> {}",name,before,HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume());
        });
        if(phase==60)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            if(name.equals("endless_hour")) {
                com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip.accumulateDeferredDamage(player,8);
                com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip.settleDebt(player);
            }
            if(name.equals("furnace_veins") || name.equals("absolute_stillness"))ManipulationChannelManager.stop(player);
            var target=player.level().getEntity(targetId);if(target!=null)target.setDeltaMovement(.06,0,0);
            if(!npcOnly && EntityManipulationEffects.isSupported(ManipulationInit.getByName(name)))castNpc(player,name);
        });
        if(phase>=6 && phase<=112 && phase%2==0)Screenshot.grab(mc.gameDirectory,String.format(java.util.Locale.ROOT,npcOnly?"thermal-npc-%05d.png":"thermal-casts-%05d.png",frame++),mc.getMainRenderTarget(),message->{});
    }
    private static boolean castNpc(ServerPlayer player,String name) {
        if(!(player.level().getEntity(npcId) instanceof net.minecraft.world.entity.Mob npc)
                || !(player.level().getEntity(targetId) instanceof net.minecraft.world.entity.LivingEntity target))return false;
        return EntityManipulationEffects.cast(ManipulationInit.getByName(name),
                ManipulationCastContext.forWill(npc,target,player.getMainHandItem()));
    }
}
