package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import java.util.UUID;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("saint_power_validation")
@PrefixGameTestTemplate(false)
public final class SaintPowerGameTests {
    @GameTest(template="empty", timeoutTicks=40)
    public static void hemorathBothSecondaryAttacksRespectWindupAndRecovery(GameTestHelper h) {secondaryPair(h,0);}
    @GameTest(template="empty", timeoutTicks=40)
    public static void seraphaeBothSecondaryAttacksRespectWindupAndRecovery(GameTestHelper h) {secondaryPair(h,1);}
    @GameTest(template="empty", timeoutTicks=40)
    public static void putricielBothSecondaryAttacksRespectWindupAndRecovery(GameTestHelper h) {secondaryPair(h,2);}
    @GameTest(template="empty", timeoutTicks=40)
    public static void velorumBothSecondaryAttacksRespectWindupAndRecovery(GameTestHelper h) {secondaryPair(h,3);}

    private static SaintTrialEntity isolatedBoss(GameTestHelper h,int saint) {
        return switch(saint) {
            case 0 -> new HemorathEntity(EntityInit.hemorath.get(),h.getLevel()) {@Override public void aiStep(){tickSecondaryAttacks(true);}};
            case 1 -> new SeraphaeEntity(EntityInit.seraphae.get(),h.getLevel()) {@Override public void aiStep(){tickSecondaryAttacks(true);}};
            case 2 -> new PutricielEntity(EntityInit.putriciel.get(),h.getLevel()) {@Override public void aiStep(){tickSecondaryAttacks(true);}};
            default -> new VelorumEntity(EntityInit.velorum.get(),h.getLevel()) {@Override public void aiStep(){tickSecondaryAttacks(true);}};
        };
    }
    private static void secondaryPair(GameTestHelper h,int saint) {
        var boss=isolatedBoss(h,saint);
        try(var actors=new Actors(h,boss)) {
            actors.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
            var hits=new java.util.HashSet<Integer>();
            for(int i=0;i<600;i++) {
                actors.player.setHealth(1000);actors.player.invulnerableTime=0;boss.aiStep();
                int id=boss.getSecondaryMove();
                if(id==0)continue;
                var move=com.vincenthuto.hemomancy.common.entity.boss.saint.SaintAttackPattern.byId(id);
                int age=move.duration()-boss.getSecondaryTicks();
                if(age<move.windup())h.assertTrue(actors.player.getHealth()==1000,"Attack hit before its warning finished");
                if(actors.player.getHealth()<1000) {
                    h.assertTrue(move.damageTick(age),"Attack hit during recovery");hits.add(id);
                }
            }
            h.assertTrue(hits.size()==2,"Expected both secondary attacks to land; got "+hits);
            for(int i=0;i<200 && boss.getSecondaryMove()==0;i++)boss.aiStep();
            boss.beginRise();
            h.assertTrue(boss.getSecondaryMove()==0 && boss.getSecondaryTicks()==0,"A fresh rise did not cancel the queued attack");
            h.succeed();
        }
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void hemorathOverloadImmediatelyCancelsASecondaryWarning(GameTestHelper h) {
        var boss=EntityInit.hemorath.get().create(h.getLevel());
        try(var actors=new Actors(h,boss)) {
            actors.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
            actors.player.setHealth(1000);
            for(int i=0;i<140;i++)boss.aiStep();
            h.assertTrue(boss.getSecondaryMove()==1,"Expected an iron rake warning before overload");
            boss.addAbsorbedBlood(actors.player,HemorathEntity.OVERLOAD_THRESHOLD);
            h.assertTrue(boss.getSecondaryMove()==0 && boss.getSecondaryTicks()==0,
                    "Terminal overload retained a queued secondary warning until another tick");
            h.succeed();
        }
    }

    @GameTest(template="empty", timeoutTicks=40)
    public static void nerveLanceDoesNotFollowAPlayerWhoDodgesItsCommittedLine(GameTestHelper h) {
        var boss=isolatedBoss(h,1);
        try(var actors=new Actors(h,boss)) {
            actors.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
            actors.player.setHealth(1000);
            for(int i=0;i<140;i++)boss.aiStep();
            h.assertTrue(boss.getSecondaryMove()==3,"Expected the first nerve lance warning");
            actors.player.setPos(actors.player.position().add(0,0,3));
            for(int i=0;i<60;i++){actors.player.invulnerableTime=0;boss.aiStep();}
            h.assertTrue(actors.player.getHealth()==1000,"The lance tracked a dodged target instead of the warned line");
            h.succeed();
        }
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void hemorathChargesRealBloodExactlyOnceAndLeaksWhenAlone(GameTestHelper h) {
        var boss = EntityInit.hemorath.get().create(h.getLevel());
        try (var actors = new Actors(h, boss)) {
            var blood = HemoCapabilityAccess.getBloodVolume(actors.player);
            blood.setActive(true); blood.setBloodVolume(1000);
            for (int i=0;i<20;i++) boss.aiStep();
            h.assertTrue(blood.getBloodVolume()==980 && boss.getAbsorbedBlood()==20,
                    "Aura must charge 20 blood and absorb the actual payment exactly once");
            actors.player.setPos(boss.position().add(40,0,0));
            boss.aiStep();
            h.assertTrue(boss.getAbsorbedBlood()==19.75F,"Stored blood did not leak away from players");
            actors.player.setPos(boss.position().add(2,0,0));
            actors.player.setGameMode(GameType.CREATIVE);
            for(int i=0;i<100;i++)boss.aiStep();
            h.assertTrue(blood.getBloodVolume()==980,"Creative player was charged by the siphon");
            h.succeed();
        }
    }

    @GameTest(template="empty", timeoutTicks=40)
    public static void seraphaeHealsThenOvergrowsWithoutChangingContainmentVictory(GameTestHelper h) {
        var boss=EntityInit.seraphae.get().create(h.getLevel());
        try(var actors=new Actors(h,boss)) {
            actors.player.setHealth(10);
            for(int i=0;i<80;i++)boss.aiStep();
            h.assertTrue(actors.player.getHealth()==12,"Transfusion did not heal the wound");
            actors.player.setHealth(actors.player.getMaxHealth());
            // Remaining stable pulse warns; the first deterministic dispersed impulse convulses.
            for(int i=0;i<260;i++)boss.aiStep();
            h.assertTrue(actors.player.hasEffect(MobEffects.WEAKNESS) && actors.player.getHealth()<actors.player.getMaxHealth(),
                    "Excess growth did not cause a nerve convulsion");
            for(int i=0;i<30;i++)boss.onFragmentContained(true);
            boss.aiStep();
            h.assertTrue(boss.hasTrialEnded(),"New growth power bypassed or broke containment victory");
            h.succeed();
        }
    }

    @GameTest(template="empty", timeoutTicks=40)
    public static void putricielRotNeverBurnsAndCleansingWaitsForItsWindup(GameTestHelper h) {
        var boss=EntityInit.putriciel.get().create(h.getLevel());
        try(var actors=new Actors(h,boss)) {
            actors.player.setHealth(20); boss.tickCount=120; boss.aiStep();
            h.assertTrue(actors.player.hasEffect(MobEffects.WITHER) && !actors.player.isOnFire(),"Rot nova must wither without fire");
            actors.player.removeAllEffects();actors.player.invulnerableTime=0;
            boss.tickCount=121;
            for(int i=0;i<299;i++)boss.aiStep();
            h.assertTrue(boss.isAbsolved() && !actors.player.isOnFire(),"Opening cleansing did not preserve its warning");
            for(int i=0;i<19;i++)boss.aiStep();
            h.assertTrue(!actors.player.isOnFire(),"Cleansing burned during the windup");
            boss.aiStep();
            h.assertTrue(actors.player.isOnFire(),"Cleansing did not ignite the nearby player after its warning");
            h.succeed();
        }
    }

    @GameTest(template="empty", timeoutTicks=40)
    public static void velorumStillnessBuildsAndAHitOpensThenReleasesTheField(GameTestHelper h) {
        var boss=EntityInit.velorum.get().create(h.getLevel());
        try(var actors=new Actors(h,boss)) {
            boss.tickCount=1;
            for(int i=0;i<80;i++)boss.aiStep();
            h.assertTrue(boss.getStillnessPressure()==80,"Stillness did not accumulate on an unmoving player");
            boss.invulnerableTime=0;
            h.assertTrue(boss.hurt(boss.damageSources().playerAttack(actors.player),1),"Opening strike was rejected");
            h.assertTrue(boss.getStillnessBreakTicks()==60 && !boss.isMartyrdom(),"A strike did not interrupt stillness");
            for(int i=0;i<16;i++)boss.aiStep();
            h.assertTrue(boss.getStillnessPressure()==0,"Interrupted field did not release accumulated pressure");
            int opening=boss.getStillnessBreakTicks();boss.invulnerableTime=0;
            boss.hurt(boss.damageSources().playerAttack(actors.player),1);
            h.assertTrue(boss.getStillnessBreakTicks()==opening && !boss.isMartyrdom(),
                    "Repeated hits refreshed the opening or granted resistance during it");
            h.succeed();
        }
    }

    private static final class Actors implements AutoCloseable {
        final SaintTrialEntity boss;
        final ServerPlayer player;
        final EmbeddedChannel channel;
        Actors(GameTestHelper h,SaintTrialEntity entity) {
            boss=entity;
            for(var pos:net.minecraft.core.BlockPos.betweenClosed(h.absolutePos(new net.minecraft.core.BlockPos(1,1,1)),
                    h.absolutePos(new net.minecraft.core.BlockPos(14,12,14))))
                h.getLevel().setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
            boss.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(4,3,4)));
            boss.setNoAi(true);h.getLevel().addFreshEntity(boss);
            var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"SaintPowerTest"),false);
            player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
            var connection=new Connection(PacketFlow.SERVERBOUND);channel=new EmbeddedChannel(connection);
            new ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            };
            player.setGameMode(GameType.SURVIVAL);player.setPos(boss.position().add(2,0,0));
            h.getLevel().addNewPlayer(player);
            ((com.vincenthuto.hemomancy.mixin.core.ServerPlayerSpawnGraceAccessor)player).hemomancy$setSpawnInvulnerableTime(0);
            h.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(boss,player)
                    && boss.hasLineOfSight(player), "Invalid power fixture: eligible="
                    +com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(boss,player)
                    +", sight="+boss.hasLineOfSight(player)+", alive="+player.isAlive()+", spectator="+player.isSpectator()
                    +", creative="+player.isCreative()+", distance="+boss.distanceToSqr(player));
        }
        public void close() {
            if(boss instanceof SeraphaeEntity seraphae) {
                for(var actor:boss.level().getEntities(boss,boss.getBoundingBox().inflate(32),
                        e -> e instanceof com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity a && seraphae.getUUID().equals(a.getParentId())
                                || e instanceof com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity f && seraphae.getUUID().equals(f.getParentId())))actor.discard();
            }
            boss.discard();player.discard();
            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger.logout(player);
            channel.finishAndReleaseAll();
        }
    }
}
