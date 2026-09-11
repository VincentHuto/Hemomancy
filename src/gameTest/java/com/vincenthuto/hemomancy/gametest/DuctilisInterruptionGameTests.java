package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.network.ManipulationInterruptedPacket;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DuctilisInterruptionGameTests {
    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis")
    public static void paralysisBlocksStillArtsButPreservesAutomaticPassiveTriggers(GameTestHelper h) {
        var p=DuctilisGameTests.player(h);
        try {
            var phoenix=ManipulationInit.phoenix_debt.get();
            var known=HemoCapabilityAccess.requireKnownManipulations(p);
            known.getKnownManips().put(phoenix,new ManipLevel(4,185));
            known.setEquippedManipNames(java.util.List.of(phoenix.getName()));
            if(!known.isPassiveActive(phoenix.getName()))known.togglePassive(phoenix.getName());
            var blood=HemoCapabilityAccess.requireBloodVolume(p);blood.setActive(true);blood.setBloodVolume(10000);
            for(var tendency:EnumBloodTendency.values())HemoCapabilityAccess.getBloodTendency(p).orElseThrow().setTendencyAlignment(tendency,100);
            h.assertTrue(Paralysis.apply(p,20),"Passive fixture paralysis rejected");
            h.assertTrue(phoenix.tryPerformPassiveTrigger(p),"Paralysis disabled an automatic Phoenix Debt trigger");
            int[] casts={0};
            var art=new StillArt("ductilis_action_fixture",EnumClarityStage.AWAKENED,20,(player,level,item,pos,self)->{casts[0]++;return true;});
            HemoCapabilityAccess.getKnownStillArts(p).orElseThrow().learnArt(art);
            var clarity=HemoCapabilityAccess.getUnstainedProgress(p).orElseThrow();clarity.setClarityUnlocked(true);clarity.setClarity(100);
            art.performAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition());
            h.assertTrue(casts[0]==0&&!art.isOnCooldown(p),"Paralyzed Still Art ran or spent its cooldown");
            Paralysis.onLogin(new PlayerEvent.PlayerLoggedInEvent(p));
            art.performAction(p,h.getLevel(),ItemStack.EMPTY,p.blockPosition());
            h.assertTrue(casts[0]==1&&art.isOnCooldown(p),"Still Art did not resume after paralysis cleanup");
            h.succeed();
        } finally {p.discard();}
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis")
    public static void slimeAndMagmaCubeCannotDealContactDamageWhileParalyzed(GameTestHelper h) {
        for(EntityType<? extends Slime> type:java.util.List.of(EntityType.SLIME,EntityType.MAGMA_CUBE)) {
            var p=DuctilisGameTests.player(h);var slime=type.create(h.getLevel());
            try {
                // Newly constructed ServerPlayers reject mob damage for60 player ticks after login.
                for(int tick=0;tick<60;tick++)p.tick();
                // Vanilla contact damage requires effective AI; the synchronous fixture never advances a movement tick.
                slime.setSize(2,true);slime.setPos(p.position());h.getLevel().addFreshEntity(slime);
                h.assertTrue(slime.isEffectiveAi()&&slime.isWithinMeleeAttackRange(p)&&slime.hasLineOfSight(p)
                        &&slime.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)>0,
                        "Slime contact prerequisites missing: AI="+slime.isEffectiveAi()+", range="+slime.isWithinMeleeAttackRange(p)
                                +", LOS="+slime.hasLineOfSight(p)+", damage="+slime.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
                h.assertTrue(Paralysis.apply(slime,60),"Slime paralysis rejected");
                float health=p.getHealth();slime.playerTouch(p);
                h.assertTrue(p.getHealth()==health,"Paralyzed slime dealt contact damage");
                slime.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.paralysis);slime.playerTouch(p);
                if(p.getHealth()==health) {
                    boolean receiverAccepted=p.hurt(p.damageSources().mobAttack(slime),2);
                    h.fail("Unparalyzed contact failed: receiverAccepted="+receiverAccepted+", invulnerable="+p.isInvulnerable()
                            +", creative="+p.isCreative()+", remainingEffect="+Paralysis.isParalyzed(slime)
                            +", AI="+slime.isEffectiveAi()+", LOS="+slime.hasLineOfSight(p)+", range="+slime.isWithinMeleeAttackRange(p));
                }
            } finally {slime.discard();p.discard();}
        }
        h.succeed();
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis")
    public static void interruptionCancelsCreeperWindupButKeepsIgnitedFuse(GameTestHelper h) {
        var creeper=EntityType.CREEPER.create(h.getLevel());
        try {
            creeper.setSwellDir(1);Paralysis.interrupt(creeper);
            h.assertTrue(creeper.getSwellDir()==-1,"Unignited attack windup survived interruption");
            creeper.ignite();creeper.setSwellDir(1);Paralysis.interrupt(creeper);
            h.assertTrue(creeper.isIgnited()&&creeper.getSwellDir()==1,"Interruption extinguished a deliberately ignited fuse");
            h.succeed();
        } finally {creeper.discard();}
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis")
    public static void paralysisRejectsHeldMountJumpRelease(GameTestHelper h) {
        var p=DuctilisGameTests.player(h);int[] jumps={0};
        var horse=new Horse(EntityType.HORSE,h.getLevel()) {
            @Override public boolean canJump(){return true;}
            @Override public LivingEntity getControllingPassenger(){return p;}
            @Override public void handleStartJump(int strength){jumps[0]++;}
        };
        try {
            horse.setPos(p.position());h.getLevel().addFreshEntity(horse);p.startRiding(horse,true);
            h.assertTrue(Paralysis.apply(p,20),"Mount fixture paralysis rejected");
            var jump=new ServerboundPlayerCommandPacket(p,ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP,90);
            p.connection.handlePlayerCommand(jump);
            h.assertTrue(jumps[0]==0,"Paralyzed rider released a charged mount jump");
            Paralysis.onLogin(new PlayerEvent.PlayerLoggedInEvent(p));p.connection.handlePlayerCommand(jump);
            h.assertTrue(jumps[0]==1,"Mount jump did not resume after cleanup");h.succeed();
        } finally {p.stopRiding();horse.discard();p.discard();}
    }

    @GameTest(templateNamespace="hemomancy",template="ductilis_arena",batch="ductilis")
    public static void disruptionSendsChargeCancellationWithoutParalysisOrTickSpam(GameTestHelper h) {
        int[] cues={0};var p=DuctilisGameTests.player(h,packet->{
            if(packet instanceof ClientboundCustomPayloadPacket custom&&custom.payload() instanceof ManipulationInterruptedPacket)cues[0]++;
        });
        try {
            Paralysis.interrupt(p);
            h.assertTrue(cues[0]==1&&!Paralysis.isParalyzed(p),"Short disruption lacked its charge cancellation cue");
            Paralysis.enforceLock(p);
            h.assertTrue(cues[0]==1,"Maintaining paralysis resent its interruption cue every tick");h.succeed();
        } finally {p.discard();}
    }
}
