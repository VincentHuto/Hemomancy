package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.init.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("naeglerophaeon_validation")
@PrefixGameTestTemplate(false)
public final class NaeglerophaeonAnimationGameTests {
    private record Encounter(NaeglerophaeonEntity boss,ServerPlayer player) {}
    private static Encounter encounter(GameTestHelper h,double range) {
        var boss=h.spawn(EntityInit.naeglerophaeon.get(),new BlockPos(8,6,8));
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(),"axon-victim"),false);
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if(boss.isGrabbing() && packet instanceof net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket move) {
                    h.assertTrue(move.getRelativeArguments().containsAll(java.util.Set.of(
                            net.minecraft.world.entity.RelativeMovement.X_ROT,net.minecraft.world.entity.RelativeMovement.Y_ROT))
                            && move.getXRot()==0 && move.getYRot()==0,"reel must send zero relative camera rotation");
                }
            }
        };
        player.setGameMode(GameType.SURVIVAL); player.setPos(boss.position().add(range,0,0)); player.setNoGravity(true);
        h.getLevel().addNewPlayer(player);
        h.runAfterDelay(175,player::discard);
        return new Encounter(boss,player);
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void captureReelsGraduallyThenDischarges(GameTestHelper h) {
        var e=encounter(h,6); var initial=e.player.position();
        h.runAfterDelay(25,()->{
            h.assertTrue(e.boss.isGrabbing(),"capture windup must enter reel");
            h.assertTrue(e.player.position().distanceTo(initial)<1,"capture must not instantly snap to core");
            h.assertTrue(e.boss.getParts()[0].isPickable(),"gripping weak point must be hittable");
        });
        h.runAfterDelay(70,()->{
            h.assertTrue(e.player.position().distanceTo(initial)>1,"victim must move gradually");
            var observer=EntityInit.naeglerophaeon.get().create(h.getLevel());
            observer.getEntityData().assignValues(e.boss.getEntityData().getNonDefaultValues());
            h.assertTrue(observer.isGrabbing() && observer.grabbedEntityId()==e.player.getId(),"late observer must receive hold");
        });
        h.runAfterDelay(128,()->{
            h.assertTrue(!e.boss.isGrabbing(),"completed reel releases");
            h.assertTrue(e.player.getHealth()<20,"terminal discharge damages player");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void strikingGripBreaksHoldAndDamagesCore(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(30,()->{
            float before=e.boss.getHealth();
            e.boss.getParts()[0].hurt(h.getLevel().damageSources().playerAttack(e.player),12);
            h.assertTrue(e.boss.getHealth()<before,"weak point forwards damage");
            h.assertTrue(!e.boss.isGrabbing(),"eight actual damage breaks hold");
            h.assertTrue(!e.boss.getParts()[0].isPickable(),"released grip cannot be hit");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void disappearingVictimCancelsReel(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(30,e.player::discard);
        h.runAfterDelay(33,()->{ h.assertTrue(!e.boss.isGrabbing() && e.boss.grabbedEntityId()==-1,"removed victim releases"); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void captureMissesWhenTargetLeavesRange(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(10,()->e.player.setPos(e.boss.position().add(20,0,0)));
        h.runAfterDelay(25,()->{h.assertTrue(!e.boss.isGrabbing(),"out of range capture must miss"); e.player.discard(); h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void wallBlocksLightningRelease(GameTestHelper h) {
        var e=encounter(h,14);
        h.runAfterDelay(10,()->wall(h,15));
        h.runAfterDelay(30,()->{h.assertTrue(!e.boss.isGrabbing() && e.player.getHealth()==20,"wall must block lightning"); e.player.discard(); h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void blockedReelReleasesInsteadOfPullingThroughWall(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(25,()->wall(h,12));
        h.runAfterDelay(100,()->{
            h.assertTrue(!e.boss.isGrabbing(),"blocked reel must release");
            h.assertTrue(e.player.getX()>h.absolutePos(new BlockPos(12,6,8)).getX(),"player stays on near side of wall");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void swimmerLeavesIsolatedPlatform(GameTestHelper h) {
        var e=encounter(h,20);
        e.player.setPos(e.boss.position().add(14,-2,0));
        for(int x=7;x<=9;x++) for(int z=7;z<=9;z++) h.setBlock(new BlockPos(x,5,z),BlockInit.synaptic_node.get());
        var start=e.boss.position();
        h.runAfterDelay(160,()->{
            h.assertTrue(e.boss.position().distanceTo(start)>2,"free swimmer must leave island");
            e.player.discard(); h.succeed();
        });
    }
    private static void wall(GameTestHelper h,int x) {
        for(int y=4;y<11;y++) for(int z=5;z<12;z++) h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void lightningDamagesAfterWindup(GameTestHelper h) {
        var e=encounter(h,14);
        e.boss.setNoAi(true);
        field(e.boss,"grabCooldown",200); field(e.boss,"zapCooldown",65);
        h.runAfterDelay(72,()->h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.CHARGE,"lightning has a visible windup"));
        h.runAfterDelay(95,()->{
            h.assertTrue(e.player.getHealth()<20,"unobstructed lightning must damage");
            h.assertTrue(e.boss.grabbedEntityId()==-1,"completed lightning clears victim reference");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void lightningRechecksObstructionAfterWindup(GameTestHelper h) {
        var e=encounter(h,14); e.boss.setNoAi(true);
        field(e.boss,"grabCooldown",200); field(e.boss,"zapCooldown",65);
        h.runAfterDelay(72,()->{
            h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.CHARGE,"attack starts before wall appears");
            wall(h,15);
        });
        h.runAfterDelay(95,()->{
            h.assertTrue(e.player.getHealth()==20,"wall blocks release after spawn immunity expires");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void saveDuringHoldRestoresIdleAndKeepsOwnership(GameTestHelper h) {
        var e=encounter(h,6);
        e.boss.bindMind(e.boss.blockPosition(),37,java.util.List.of());
        h.runAfterDelay(30,()->{
            h.assertTrue(e.boss.isGrabbing(),"fixture enters hold");
            var tag=new net.minecraft.nbt.CompoundTag(); e.boss.addAdditionalSaveData(tag);
            var restored=EntityInit.naeglerophaeon.get().create(h.getLevel()); restored.readAdditionalSaveData(tag);
            h.assertTrue(restored.animationPhase()==NaeglerophaeonEntity.IDLE && restored.grabbedEntityId()==-1,"transient hold is not saved");
            h.assertTrue(restored.mindOrigin().equals(e.boss.mindOrigin()),"encounter ownership survives");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void coreDamageBreaksCapture(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(30,()->{
            e.boss.hurt(h.getLevel().damageSources().playerAttack(e.player),12);
            h.assertTrue(!e.boss.isGrabbing(),"core damage also breaks hold");
            e.player.discard(); h.succeed();
        });
    }
    private static void field(Object target,String name,int value) {
        try { var f=target.getClass().getDeclaredField(name); f.setAccessible(true); f.setInt(target,value); }
        catch(ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void victimCanTurnWhileBeingReeled(GameTestHelper h) {
        var e=encounter(h,6);
        h.runAfterDelay(30,()->{e.player.setYRot(73); e.player.setXRot(37);});
        h.runAfterDelay(34,()->{
            h.assertTrue(e.boss.isGrabbing(),"fixture remains in reel");
            h.assertTrue(e.player.getYRot()==73 && e.player.getXRot()==37,"reel preserves aiming angles");
            e.player.discard(); h.succeed();
        });
    }
}
