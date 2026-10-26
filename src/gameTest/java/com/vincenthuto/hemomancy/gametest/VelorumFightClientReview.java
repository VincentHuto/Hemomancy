package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Opt-in native review of the real nova and nova, veil, resistance and blood drain, including concurrent casts. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VelorumFightClientReview {
    private static final String[] SCENES={"idle","frost_nova","veil_of_darkness","melee","martyrdom","silence_drain","concurrent_casts","rest"};
    private static final int[] LENGTHS={40,60,60,35,55,60,60,70};
    private static final AABB STAGE=new AABB(1880,198,580,1921,215,621);
    private static final JsonObject RESULTS=new JsonObject();
    private static MinecraftServer server;
    private static ServerPlayer observer,victim,inactive;
    private static VelorumEntity boss;
    private static Husk husk;
    private static EmbeddedChannel channel,inactiveChannel;
    private static volatile int scene=-1,tick;
    private static volatile boolean done;
    private static boolean launched,rootChecked,drainChecked,concurrentChecked,martyrChecked;
    private static int capturedScene=-1,capturedTick=-1,closing,rewardsBefore,originalFov;
    private static boolean originalPause;
    private static ParticleStatus originalParticles;
    private static Path report;
    private VelorumFightClientReview() {}

    @SubscribeEvent public static void client(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.velorumFightReview"))return;
        Minecraft mc=Minecraft.getInstance();
        if(!launched && mc.player!=null && mc.level!=null && mc.getSingleplayerServer()!=null) {
            launched=true;server=mc.getSingleplayerServer();
            originalParticles=mc.options.particles().get();originalFov=mc.options.fov().get();originalPause=mc.options.pauseOnLostFocus;
            mc.options.particles().set(ParticleStatus.MINIMAL);mc.options.fov().set(45);mc.options.pauseOnLostFocus=false;
            report=mc.gameDirectory.toPath().resolve("operator/velorum-fight-review.json");
            UUID id=mc.player.getUUID();
            server.execute(()->{try{observer=server.getPlayerList().getPlayer(id);setup();}catch(Exception error){fail(error);}});
        }
        int current=scene,frame=tick;
        if(launched && !done)mc.options.fov().set(current==1 || current==2?45:30);
        if(current>=0 && current<SCENES.length && frame%6==0 && (capturedScene!=current || capturedTick!=frame)) {
            capturedScene=current;capturedTick=frame;
            Screenshot.grab(mc.gameDirectory,String.format("velorum-fight-%02d-%s-%03d.png",current,SCENES[current],frame),mc.getMainRenderTarget(),message->{});
        }
        if(done && ++closing>24) {
            mc.options.particles().set(originalParticles);mc.options.fov().set(originalFov);mc.options.pauseOnLostFocus=originalPause;mc.stop();
        }
    }
    private static void setup() {
        if(observer==null)throw new IllegalStateException("Missing observer");
        ServerLevel level=observer.serverLevel();
        for(int x=1880;x<=1920;x+=8)for(int z=580;z<=620;z+=8)level.getChunkAt(new BlockPos(x,200,z));
        level.getEntitiesOfClass(VelorumEntity.class,STAGE).forEach(actor->actor.discard());
        level.getEntitiesOfClass(Husk.class,STAGE).forEach(actor->actor.discard());
        for(BlockPos pos:BlockPos.betweenClosed(1880,199,580,1920,199,620))level.setBlock(pos,Blocks.POLISHED_DEEPSLATE.defaultBlockState(),2);
        for(BlockPos pos:BlockPos.betweenClosed(1886,200,610,1914,212,610))level.setBlock(pos,Blocks.BLACK_CONCRETE.defaultBlockState(),2);
        level.setBlock(new BlockPos(1898,199,599),Blocks.OCHRE_FROGLIGHT.defaultBlockState(),2);
        level.setBlock(new BlockPos(1902,199,599),Blocks.OCHRE_FROGLIGHT.defaultBlockState(),2);
        level.setDayTime(18000);observer.setGameMode(GameType.SPECTATOR);
        observer.teleportTo(level,1900.5,201.3,584.5,0,0);
        boss=EntityInit.velorum.get().create(level);husk=EntityType.HUSK.create(level);
        if(boss==null || husk==null)throw new IllegalStateException("Missing actors");
        boss.setNoAi(true);boss.setPersistenceRequired();boss.moveTo(1900.5,200,600.5,180,0);
        boss.yBodyRot=boss.yBodyRotO=boss.yHeadRot=boss.yHeadRotO=180;
        husk.setNoAi(true);husk.setPersistenceRequired();husk.moveTo(1898.5,200,598.5,0,0);
        level.addFreshEntity(boss);level.addFreshEntity(husk);
        victim=player(level,"VelorumActive",false);
        inactive=player(level,"VelorumDormant",true);
        var activeBlood=HemoCapabilityAccess.getBloodVolume(victim);activeBlood.setActive(true);activeBlood.setBloodVolume(5000);
        var dormantBlood=HemoCapabilityAccess.getBloodVolume(inactive);dormantBlood.setActive(false);dormantBlood.setBloodVolume(5000);
        rewardsBefore=residuumCount();scene=0;tick=0;
        Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW started with Minimal particles");
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.velorumFightReview") || event.getServer()!=server || scene<0 || done)return;
        try {
            // Real connections tick doTick(); these two embedded listeners have no connection loop.
            if(victim!=null && !victim.isRemoved())victim.doTick();
            if(inactive!=null && !inactive.isRemoved())inactive.doTick();
            if(tick==0)enter();
            if(scene==3 && tick==6){boss.swing(InteractionHand.MAIN_HAND,true);boss.doHurtTarget(husk);}
            if(scene==4 && tick==6)hit();
            if(scene==6 && tick==20)hit();
            if(scene==1 && boss.getFrostRelease()==18) {
                var slow=victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                rootChecked=slow!=null && slow.getAmplifier()==6 && slow.getDuration()>=19 && slow.getDuration()<=20 && !slow.isVisible();
            }
            if((scene==5 || scene==6) && boss.getDrainRelease()==20) {
                var paid=BloodFlowLedger.collect(victim,HemoCapabilityAccess.getBloodVolume(victim)).contribution("velorum_silence");
                boolean dormantUntouched=BloodFlowLedger.collect(inactive,HemoCapabilityAccess.getBloodVolume(inactive)).contribution("velorum_silence").isEmpty();
                drainChecked=paid.isPresent() && paid.get().appliedPerTick()==-200 && dormantUntouched;
                if(!drainChecked)throw new IllegalStateException("Drain did not charge exactly 200 active blood only");
            }
            if(scene==4 && tick==8)martyrChecked=boss.isMartyrdom() && boss.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && boss.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==0 && !boss.getEffect(MobEffects.DAMAGE_RESISTANCE).isVisible();
            if(scene==6 && boss.getFrostRelease()==18)
                concurrentChecked=boss.getVeilRelease()==22 && boss.getDrainRelease()==20 && boss.getMartyrdomTicks()>0;
            if(scene==7 && tick==6){boss.setHealth(1);boss.invulnerableTime=0;boss.hurt(boss.damageSources().playerAttack(observer),10000);}
            if(++tick>=LENGTHS[scene]) {
                verify();
                if(++scene>=SCENES.length) {
                    cleanup();RESULTS.addProperty("passed",true);write();done=true;
                    Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW_OK {}",RESULTS);
                }else tick=0;
            }
        }catch(Exception error){fail(error);}
    }
    private static void enter() {
        victim.removeAllEffects();victim.clearFire();victim.setHealth(100);victim.invulnerableTime=0;
        inactive.removeAllEffects();inactive.setHealth(100);inactive.invulnerableTime=0;
        switch(scene) {
            case 1 -> {boss.tickCount=76;positionTargets(9.9);}
            case 2 -> {boss.tickCount=140;positionTargets(11.9);}
            case 3 -> husk.setHealth(husk.getMaxHealth());
            case 5 -> {boss.setHealth(100);boss.tickCount=60;positionTargets(7.9);}
            case 6 -> {boss.tickCount=776;positionTargets(7.9);}
            default -> {}
        }
        if(scene==5 || scene==6) {
            HemoCapabilityAccess.getBloodVolume(victim).setBloodVolume(5000);
            HemoCapabilityAccess.getBloodVolume(inactive).setBloodVolume(5000);
            BloodFlowLedger.forget(victim,"velorum_silence");BloodFlowLedger.forget(inactive,"velorum_silence");
        }
    }
    private static void positionTargets(double offset) {
        victim.teleportTo(boss.getX()+offset,200,boss.getZ()+offset);
        inactive.teleportTo(boss.getX()-offset,200,boss.getZ()+offset);
    }
    private static void hit(){boss.invulnerableTime=0;boss.hurt(boss.damageSources().playerAttack(observer),1);}
    private static void verify() {
        boolean pass=switch(scene) {
            case 1 -> rootChecked && victim.getHealth()<100 && victim.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                    && victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==2 && !victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN).isVisible();
            case 2 -> victim.hasEffect(MobEffects.BLINDNESS) && inactive.hasEffect(MobEffects.BLINDNESS)
                    && !victim.getEffect(MobEffects.BLINDNESS).isVisible() && victim.hasEffect(MobEffects.CONFUSION)
                    && !victim.getEffect(MobEffects.CONFUSION).isVisible() && !inactive.hasEffect(MobEffects.CONFUSION);
            case 3 -> husk.getHealth()<husk.getMaxHealth();
            case 4 -> martyrChecked && !boss.isMartyrdom() && !boss.hasEffect(MobEffects.DAMAGE_RESISTANCE);
            case 5 -> drainChecked;
            case 6 -> concurrentChecked && drainChecked;
            case 7 -> boss.isRemoved() && residuumCount()==rewardsBefore+1;
            default -> true;
        };
        RESULTS.addProperty(SCENES[scene],pass);
        if(!pass)throw new IllegalStateException("Failed review contract: "+SCENES[scene]);
    }
    private static ServerPlayer player(ServerLevel level,String name,boolean second) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),name),false);
        var actor=new ServerPlayer(server,level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);
        var embedded=new EmbeddedChannel(connection);if(second)inactiveChannel=embedded;else channel=embedded;
        new ServerGamePacketListenerImpl(server,connection,actor,cookie){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
        };
        actor.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);actor.setHealth(100);actor.setInvisible(true);
        actor.moveTo(1908.4,200,608.4,0,0);level.addNewPlayer(actor);return actor;
    }
    private static int residuumCount() {
        return observer.serverLevel().getEntitiesOfClass(ItemEntity.class,STAGE).stream()
                .filter(item->item.getItem().is(ItemInit.hallowed_residuum_velorum.get())).mapToInt(item->item.getItem().getCount()).sum();
    }
    private static void cleanup() {
        if(boss!=null && !boss.isRemoved())boss.discard();
        if(husk!=null)husk.discard();if(victim!=null && !victim.isRemoved())victim.discard();
        if(inactive!=null && !inactive.isRemoved())inactive.discard();
        if(victim!=null)BloodFlowLedger.logout(victim);if(inactive!=null)BloodFlowLedger.logout(inactive);
        if(channel!=null)channel.finishAndReleaseAll();if(inactiveChannel!=null)inactiveChannel.finishAndReleaseAll();
    }
    private static void fail(Exception error) {
        RESULTS.addProperty("passed",false);RESULTS.addProperty("error",error.toString());
        Hemomancy.LOGGER.error("VELORUM_FIGHT_REVIEW_FAILED",error);cleanup();write();done=true;
    }
    private static void write() {
        try{Files.createDirectories(report.getParent());Files.writeString(report,new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));}
        catch(Exception error){Hemomancy.LOGGER.error("Could not save Velorum review",error);}
    }
}
