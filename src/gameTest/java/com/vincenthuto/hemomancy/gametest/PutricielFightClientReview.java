package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
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

/** Opt-in native review of the real nova and two full, naturally timed absolution windows. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PutricielFightClientReview {
    private static final String[] SCENES={"idle","rot_nova","escalated_nova","melee","offering_opens","absolution_hits","offering_closes","next_window","saint_rests"};
    private static final int[] LENGTHS={40,60,60,40,120,60,40,280,70};
    private static final AABB STAGE=new AABB(1780,198,540,1821,215,581);
    private static final JsonObject RESULTS=new JsonObject();
    private static MinecraftServer server;
    private static ServerPlayer observer,victim;
    private static PutricielEntity boss;
    private static Husk husk;
    private static EmbeddedChannel channel;
    private static volatile int scene=-1,tick;
    private static volatile boolean done;
    private static boolean launched,hitDuringNova,novaDuringHit;
    private static int capturedScene=-1,capturedTick=-1,closing,rewardsBefore,originalFov;
    private static boolean originalPause;
    private static ParticleStatus originalParticles;
    private static Path report;
    private PutricielFightClientReview() {}

    @SubscribeEvent public static void client(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.putricielFightReview"))return;
        Minecraft mc=Minecraft.getInstance();
        if(!launched && mc.player!=null && mc.level!=null && mc.getSingleplayerServer()!=null) {
            launched=true;server=mc.getSingleplayerServer();
            originalParticles=mc.options.particles().get();originalFov=mc.options.fov().get();originalPause=mc.options.pauseOnLostFocus;
            mc.options.particles().set(ParticleStatus.MINIMAL);mc.options.fov().set(45);mc.options.pauseOnLostFocus=false;
            report=mc.gameDirectory.toPath().resolve("operator/putriciel-fight-review.json");
            UUID id=mc.player.getUUID();
            server.execute(()->{try{observer=server.getPlayerList().getPlayer(id);setup();}catch(Exception error){fail(error);}});
        }
        int current=scene,frame=tick;
        if(launched && !done)mc.options.fov().set(current==1 || current==2?45:30);
        if(current>=0 && current<SCENES.length && frame%6==0 && (capturedScene!=current || capturedTick!=frame)) {
            capturedScene=current;capturedTick=frame;
            Screenshot.grab(mc.gameDirectory,String.format("putriciel-fight-%02d-%s-%03d.png",current,SCENES[current],frame),mc.getMainRenderTarget(),message->{});
        }
        if(done && ++closing>24) {
            mc.options.particles().set(originalParticles);mc.options.fov().set(originalFov);mc.options.pauseOnLostFocus=originalPause;mc.stop();
        }
    }
    private static void setup() {
        if(observer==null)throw new IllegalStateException("Missing observer");
        ServerLevel level=observer.serverLevel();
        for(int x=1780;x<=1820;x+=8)for(int z=540;z<=580;z+=8)level.getChunkAt(new BlockPos(x,200,z));
        level.getEntitiesOfClass(PutricielEntity.class,STAGE).forEach(actor->actor.discard());
        level.getEntitiesOfClass(Husk.class,STAGE).forEach(actor->actor.discard());
        for(BlockPos pos:BlockPos.betweenClosed(1780,199,540,1820,199,580))level.setBlock(pos,Blocks.POLISHED_DEEPSLATE.defaultBlockState(),2);
        for(BlockPos pos:BlockPos.betweenClosed(1786,200,570,1814,212,570))level.setBlock(pos,Blocks.BLACK_CONCRETE.defaultBlockState(),2);
        level.setBlock(new BlockPos(1798,199,559),Blocks.OCHRE_FROGLIGHT.defaultBlockState(),2);
        level.setBlock(new BlockPos(1802,199,559),Blocks.OCHRE_FROGLIGHT.defaultBlockState(),2);
        level.setDayTime(18000);observer.setGameMode(GameType.SPECTATOR);
        observer.teleportTo(level,1800.5,201.3,544.5,0,0);
        boss=EntityInit.putriciel.get().create(level);husk=EntityType.HUSK.create(level);
        if(boss==null || husk==null)throw new IllegalStateException("Missing actors");
        boss.setNoAi(true);boss.setPersistenceRequired();boss.moveTo(1800.5,200,560.5,180,0);
        boss.yBodyRot=boss.yBodyRotO=boss.yHeadRot=boss.yHeadRotO=180;
        husk.setNoAi(true);husk.setPersistenceRequired();husk.moveTo(1798.5,200,558.5,0,0);
        level.addFreshEntity(boss);level.addFreshEntity(husk);
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"PutricielTarget"),false);
        victim=new ServerPlayer(server,level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);channel=new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(server,connection,victim,cookie){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
        };
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);victim.setHealth(100);
        victim.moveTo(1808.4,200,568.4,0,0);level.addNewPlayer(victim);
        rewardsBefore=residuumCount();scene=0;tick=0;
        Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW started with Minimal particles");
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.putricielFightReview") || event.getServer()!=server || scene<0 || done)return;
        try {
            if(tick==0)enter();
            if(scene==3 && tick==6){boss.swing(InteractionHand.MAIN_HAND,true);boss.doHurtTarget(husk);}
            if(scene==5 && boss.getNovaReleaseTicks()==18 && boss.getVisualState()==PutricielEntity.VISUAL_ABSOLUTION_HIT)
                novaDuringHit=true;
            if(scene==5 && (tick==5 || tick==41 || tick==47)) {
                if(boss.getNovaReleaseTicks()>0)hitDuringNova=true;
                absolve();
            }
            if(scene==8 && (tick==5 || tick==25))absolve();
            if(scene==8 && tick==30 && boss.hurt(boss.damageSources().playerAttack(observer),1))
                throw new IllegalStateException("Completed trial accepted another absolution");
            if(++tick>=LENGTHS[scene]) {
                verify();
                if(++scene>=SCENES.length) {
                    cleanup();RESULTS.addProperty("passed",true);write();done=true;
                    Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW_OK {}",RESULTS);
                }else tick=0;
            }
        }catch(Exception error){fail(error);}
    }
    private static void enter() {
        if(scene==1 || scene==2) {
            boss.tickCount=96;
            victim.removeAllEffects();victim.clearFire();victim.setHealth(100);victim.invulnerableTime=0;
            double offset=scene==1?7.9:11.9;
            victim.teleportTo(boss.getX()+offset,200,boss.getZ()+offset);
            if(scene==2)boss.setHealth(160);
        }
        if(scene==3){victim.discard();husk.setHealth(husk.getMaxHealth());}
    }
    private static void absolve() {
        if(!boss.isAbsolved())throw new IllegalStateException("Expected a naturally open absolution window");
        boss.invulnerableTime=0;
        boss.hurt(boss.damageSources().playerAttack(observer),1);
    }
    private static void verify() {
        boolean pass=switch(scene) {
            case 1,2 -> victim.getHealth()<100 && victim.hasEffect(MobEffects.WITHER)
                    && !victim.getEffect(MobEffects.WITHER).isVisible()
                    && victim.getEffect(MobEffects.WITHER).getAmplifier()==(scene==2?1:0)
                    && victim.getRemainingFireTicks()>0;
            case 3 -> husk.getHealth()<husk.getMaxHealth();
            case 4,7 -> boss.isAbsolved();
            case 5 -> boss.getAbsolutionCount()==3 && !boss.isRemoved() && hitDuringNova && novaDuringHit;
            case 6 -> !boss.isAbsolved() && boss.getAbsolutionCount()==3;
            case 8 -> boss.isRemoved() && boss.getAbsolutionCount()==5 && residuumCount()==rewardsBefore+1;
            default -> true;
        };
        RESULTS.addProperty(SCENES[scene],pass);
        if(!pass)throw new IllegalStateException("Failed review contract: "+SCENES[scene]);
    }
    private static int residuumCount() {
        return observer.serverLevel().getEntitiesOfClass(ItemEntity.class,STAGE).stream()
                .filter(item->item.getItem().is(ItemInit.hallowed_residuum_putriciel.get())).mapToInt(item->item.getItem().getCount()).sum();
    }
    private static void cleanup() {
        if(boss!=null && !boss.isRemoved())boss.discard();
        if(husk!=null)husk.discard();if(victim!=null && !victim.isRemoved())victim.discard();
        if(channel!=null)channel.finishAndReleaseAll();
    }
    private static void fail(Exception error) {
        RESULTS.addProperty("passed",false);RESULTS.addProperty("error",error.toString());
        Hemomancy.LOGGER.error("PUTRICIEL_FIGHT_REVIEW_FAILED",error);cleanup();write();done=true;
    }
    private static void write() {
        try{Files.createDirectories(report.getParent());Files.writeString(report,new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));}
        catch(Exception error){Hemomancy.LOGGER.error("Could not save Putriciel review",error);}
    }
}
