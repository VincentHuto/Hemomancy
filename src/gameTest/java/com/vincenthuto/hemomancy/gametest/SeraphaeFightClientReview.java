package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Runs a natural state cycle, with a real server player receiving the growth attacks. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SeraphaeFightClientReview {
    private static MinecraftServer server;
    private static SeraphaeEntity boss;
    private static ServerPlayer victim;
    private static EmbeddedChannel channel;
    private static Path report;
    private static volatile int tick;
    private static volatile boolean done;
    private static boolean launched, healed, convulsed, dispersed, condensed;
    private static boolean lanceSeen,knotSeen;
    private static int captured=-1, closing;
    private static ParticleStatus particles;
    private static int fov;
    private static boolean pause;
    private SeraphaeFightClientReview() {}

    @SubscribeEvent public static void client(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.seraphaeFightReview"))return;
        Minecraft mc=Minecraft.getInstance();
        if(!launched && mc.player!=null && mc.getSingleplayerServer()!=null) {
            launched=true;server=mc.getSingleplayerServer();
            particles=mc.options.particles().get();fov=mc.options.fov().get();pause=mc.options.pauseOnLostFocus;
            mc.options.particles().set(ParticleStatus.MINIMAL);mc.options.fov().set(45);mc.options.pauseOnLostFocus=false;
            report=mc.gameDirectory.toPath().resolve("operator/seraphae-fight-review.json");
            UUID player=mc.player.getUUID();
            server.execute(()->{try{setup(server.getPlayerList().getPlayer(player));}catch(Exception e){finish(e);}});
        }
        int frame=tick;
        if(boss!=null && frame%16==0 && frame!=captured && !done) {
            captured=frame;
            Screenshot.grab(mc.gameDirectory,"seraphae-fight-"+frame+".png",mc.getMainRenderTarget(),message->{});
        }
        if(done && ++closing>24) {
            mc.options.particles().set(particles);mc.options.fov().set(fov);mc.options.pauseOnLostFocus=pause;mc.stop();
        }
    }
    private static void setup(ServerPlayer observer) {
        var level=observer.serverLevel();
        var source=observer.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        for(String command:new String[]{"gamemode spectator","time set 6000","gamerule doDaylightCycle false","gamerule naturalRegeneration false","weather clear","gamerule doMobSpawning false","difficulty normal"})
            server.getCommands().performPrefixedCommand(source,command);
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(1780,198,540,1820,220,580),
                e->!(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
        for(BlockPos pos:BlockPos.betweenClosed(1780,200,540,1820,215,580))level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        for(BlockPos pos:BlockPos.betweenClosed(1780,199,540,1820,199,580))level.setBlock(pos,Blocks.POLISHED_DEEPSLATE.defaultBlockState(),2);
        observer.teleportTo(level,1800.5,201,548.5,0,5);
        boss=EntityInit.seraphae.get().create(level);boss.setNoAi(true);boss.setPersistenceRequired();
        boss.moveTo(1800.5,200,560.5,180,0);boss.yBodyRot=boss.yBodyRotO=180;level.addFreshEntity(boss);
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"SeraphaeTarget"),false);
        victim=new ServerPlayer(server,level,cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);channel=new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(server,connection,victim,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        victim.setGameMode(GameType.SURVIVAL);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(990);
        victim.moveTo(1803.5,200,559.5,0,0);level.addNewPlayer(victim);
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.seraphaeFightReview") || event.getServer()!=server || boss==null || done)return;
        try {
            victim.doTick();
            if(!healed && victim.getHealth()>990) {healed=true;victim.setHealth(1000);}
            convulsed|=boss.getSeraphaeState()==SeraphaeState.DISPERSED && victim.hasEffect(MobEffects.WEAKNESS);
            var secondary=com.vincenthuto.hemomancy.common.entity.boss.saint.SaintAttackPattern.byId(boss.getSecondaryMove());
            if(secondary!=null && boss.getSecondaryTicks()<=secondary.duration()-secondary.windup()) {
                lanceSeen|=secondary.id()==3;knotSeen|=secondary.id()==4;
            }
            dispersed|=boss.getSeraphaeState()==SeraphaeState.DISPERSED;
            condensed|=boss.getSeraphaeState()==SeraphaeState.CONDENSING;
            if(++tick==780)for(int i=0;i<30;i++)boss.onFragmentContained(true);
            if(tick>=784) {
                if(!healed || !convulsed || !dispersed || !condensed || !boss.hasTrialEnded() || !lanceSeen || !knotSeen)
                    throw new IllegalStateException("Missing healing, convulsion, dispersal, condensation or containment");
                finish(null);
            }
        }catch(Exception e){finish(e);}
    }
    private static void finish(Exception error) {
        var result=new JsonObject();result.addProperty("healed",healed);result.addProperty("convulsed",convulsed);
        result.addProperty("dispersed",dispersed);result.addProperty("condensed",condensed);
        result.addProperty("contained",boss!=null && boss.hasTrialEnded());result.addProperty("passed",error==null);
        result.addProperty("nerve_lance",lanceSeen);result.addProperty("regrowth_knot",knotSeen);
        if(error!=null){result.addProperty("error",error.toString());Hemomancy.LOGGER.error("SERAPHAE_FIGHT_REVIEW_FAILED",error);}
        else Hemomancy.LOGGER.info("SERAPHAE_FIGHT_REVIEW_OK {}",result);
        if(boss!=null && !boss.isRemoved())boss.discard();
        if(victim!=null)victim.discard();if(channel!=null)channel.finishAndReleaseAll();
        try{Files.createDirectories(report.getParent());Files.writeString(report,new GsonBuilder().setPrettyPrinting().create().toJson(result));}
        catch(Exception e){Hemomancy.LOGGER.error("Could not write Seraphae review",e);}
        done=true;
    }
}
