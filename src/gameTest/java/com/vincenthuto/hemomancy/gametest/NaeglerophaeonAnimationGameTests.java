package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
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
        return new Encounter(boss,victim(h,boss,boss.position().add(range,0,0)));
    }
    private static ServerPlayer victim(GameTestHelper h,NaeglerophaeonEntity boss,net.minecraft.world.phys.Vec3 at) {
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
        player.setGameMode(GameType.SURVIVAL); player.setPos(at); player.setNoGravity(true);
        h.getLevel().addNewPlayer(player);
        h.runAfterDelay(175,player::discard);
        return player;
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

    // ---- Overhaul coverage: phase-one/two moves, Overload and Drained ----
    private static final String[] COOLDOWNS={"zapCooldown","grabCooldown","lashCooldown","lungeCooldown",
            "volleyCooldown","conductCooldown","novaCooldown","blinkCooldown"};
    /** Leaves only the named moves ready so a fixture exercises one attack. */
    private static void only(NaeglerophaeonEntity boss,String... ready) {
        var allowed=java.util.Set.of(ready);
        for(String name:COOLDOWNS) field(boss,name,allowed.contains(name)?0:1000);
    }
    private static void set(Object target,String name,Object value) {
        try { var f=target.getClass().getDeclaredField(name); f.setAccessible(true); f.set(target,value); }
        catch(ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static Object get(Object target,String name) {
        try { var f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target); }
        catch(ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    /** New players ignore damage for 60 ticks; these fixtures strike sooner. */
    private static void vulnerable(ServerPlayer player) {
        try { var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime"); f.setAccessible(true); f.setInt(player,0); }
        catch(ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static java.util.List<BlockPos> placeNodes(GameTestHelper h,NaeglerophaeonEntity boss,BlockPos... relative) {
        var absolute=new java.util.ArrayList<BlockPos>();
        for(BlockPos pos:relative) { h.setBlock(pos,BlockInit.synaptic_node.get()); absolute.add(h.absolutePos(pos)); }
        set(boss,"nodes",java.util.List.copyOf(absolute));
        return absolute;
    }
    private static NaeglerophaeonEntity loneBoss(GameTestHelper h) {
        return h.spawn(EntityInit.naeglerophaeon.get(),new BlockPos(8,6,8));
    }

    @GameTest(template="empty",timeoutTicks=180)
    public static void lungeStrikesPlayerInItsPath(GameTestHelper h) {
        var e=encounter(h,10); only(e.boss,"lungeCooldown"); vulnerable(e.player);
        h.runAfterDelay(8,()->h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.LUNGE_WINDUP,"lunge flares before it dashes"));
        h.runAfterDelay(28,()->{
            h.assertTrue(e.player.getHealth()<20,"the dash damages a player in its path");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void lungeIntoWallStunsTheBoss(GameTestHelper h) {
        var e=encounter(h,10); only(e.boss,"lungeCooldown"); vulnerable(e.player);
        h.runAfterDelay(3,()->wall(h,13));
        h.runAfterDelay(24,()->{
            h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.RECOVERY,"a blocked dash ends in a stun");
            h.assertTrue(e.boss.getX()<h.absolutePos(new BlockPos(13,6,8)).getX(),"the dash does not pass through terrain");
            h.assertTrue(e.player.getHealth()==20,"the wall shields the player");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void lashPunishesHuggingTheCore(GameTestHelper h) {
        var e=encounter(h,4); only(e.boss,"lashCooldown"); vulnerable(e.player);
        h.runAfterDelay(14,()->{
            h.assertTrue(e.player.getHealth()<20,"the sweep hits a player within five blocks");
            h.assertTrue(e.player.hasEffect(EffectInit.neural_overload),"the sweep overloads the victim's nerves");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void lashMissesPlayerWhoBacksOff(GameTestHelper h) {
        var e=encounter(h,4); only(e.boss,"lashCooldown"); vulnerable(e.player);
        h.runAfterDelay(4,()->e.player.setPos(e.boss.position().add(7,0,0)));
        h.runAfterDelay(14,()->{
            h.assertTrue(e.player.getHealth()==20,"stepping outside the sweep during the windup avoids it");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void volleySparksStrikeWhereThePlayerStood(GameTestHelper h) {
        var e=encounter(h,14); only(e.boss,"volleyCooldown"); vulnerable(e.player);
        boolean[] moved={false}, dodged={false};
        h.onEachTick(()->{
            int pending=((java.util.List<?>)get(e.boss,"pendingSparks")).size();
            int launched=NaeglerophaeonCombatRules.volleyCount(false)-(int)get(e.boss,"sparksLeft");
            if(!moved[0] && pending>0) { e.player.setPos(e.player.position().add(0,0,3)); moved[0]=true; }
            if(moved[0] && !dodged[0] && launched-pending>=1 && e.boss.animationPhase()==NaeglerophaeonEntity.VOLLEY) {
                h.assertTrue(e.player.getHealth()==20,"moving during a spark's travel dodges it");
                dodged[0]=true;
            }
        });
        h.runAfterDelay(40,()->{
            h.assertTrue(moved[0] && dodged[0],"the volley fired and its first spark resolved");
            h.assertTrue(e.player.getHealth()<20,"sparks aimed at a stationary player land");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void grabDrainHealsTheBoss(GameTestHelper h) {
        var e=encounter(h,6); only(e.boss,"grabCooldown"); vulnerable(e.player);
        e.boss.setHealth(130);
        h.runAfterDelay(46,()->{
            h.assertTrue(e.boss.isGrabbing(),"fixture remains in reel");
            h.assertTrue(e.boss.getHealth()>130,"the hold siphons health back into the core");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void transitionFiresOnceAndSurvivesReload(GameTestHelper h) {
        var e=encounter(h,14); only(e.boss);
        e.boss.setHealth(70);
        h.runAfterDelay(2,()->h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.TRANSITION && e.boss.isEnraged(),
                "half health starts the phase-two transition"));
        h.runAfterDelay(5,()->h.assertTrue(!e.boss.hurt(h.getLevel().damageSources().playerAttack(e.player),5),
                "the transition cannot be damaged"));
        h.runAfterDelay(6,()->{
            var tag=new net.minecraft.nbt.CompoundTag(); e.boss.addAdditionalSaveData(tag);
            var restored=EntityInit.naeglerophaeon.get().create(h.getLevel()); restored.readAdditionalSaveData(tag);
            h.assertTrue(restored.isEnraged() && restored.animationPhase()==NaeglerophaeonEntity.IDLE,"phase two persists without replaying");
        });
        h.runAfterDelay(60,()->{
            h.assertTrue(e.boss.isEnraged() && e.boss.animationPhase()!=NaeglerophaeonEntity.TRANSITION,"the transition fires only once");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void nerveBlinkTravelsBetweenNodes(GameTestHelper h) {
        var e=encounter(h,3); only(e.boss,"blinkCooldown"); set(e.boss,"enraged",true);
        // The template floor tops out at y5; both nodes stay inside the barrier-encased test volume.
        var nodes=placeNodes(h,e.boss,new BlockPos(8,5,8),new BlockPos(24,6,14));
        h.runAfterDelay(18,()->{
            h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.NERVE_TRANSIT && e.boss.isInvisible(),"the boss travels inside the nerve");
            h.assertTrue(!e.boss.hurt(h.getLevel().damageSources().playerAttack(e.player),6),"a travelling signal cannot be struck");
        });
        h.runAfterDelay(40,()->{
            h.assertTrue(!e.boss.isInvisible(),"the boss re-forms");
            h.assertTrue(e.boss.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(nodes.get(1)))<3,"it emerges at the far node");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void noNodesMeansNoBlink(GameTestHelper h) {
        var e=encounter(h,3); only(e.boss,"blinkCooldown"); set(e.boss,"enraged",true);
        h.onEachTick(()->h.assertTrue(e.boss.animationPhase()!=NaeglerophaeonEntity.NERVE_DIVE
                && e.boss.animationPhase()!=NaeglerophaeonEntity.NERVE_TRANSIT,"a boss without nodes cannot dive"));
        h.runAfterDelay(30,()->{ e.player.discard(); h.succeed(); });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void conductionRunsAlongTheFiber(GameTestHelper h) {
        var e=encounter(h,8); only(e.boss,"conductCooldown"); vulnerable(e.player);
        var far=victim(h,e.boss,e.boss.position().add(8,0,4)); vulnerable(far);
        placeNodes(h,e.boss,new BlockPos(12,5,8));
        for(int x=13;x<=20;x++) h.setBlock(new BlockPos(x,5,8),BlockInit.nerve_fiber.get());
        h.runAfterDelay(32,()->{
            h.assertTrue(e.player.getHealth()<20,"charge running beneath a player shocks them");
            h.assertTrue(far.getHealth()==20,"a player four blocks off the fiber is safe");
            e.player.discard(); far.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void novaRingDamagesInTheOpen(GameTestHelper h) {
        var e=encounter(h,7); only(e.boss,"novaCooldown"); set(e.boss,"enraged",true); vulnerable(e.player);
        h.runAfterDelay(10,()->h.assertTrue(e.boss.animationPhase()==NaeglerophaeonEntity.NOVA,"the nova charges visibly"));
        h.runAfterDelay(45,()->{
            h.assertTrue(e.player.getHealth()<20,"the expanding ring reaches an exposed player");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void novaRingIsBlockedByCover(GameTestHelper h) {
        var e=encounter(h,7); only(e.boss,"novaCooldown"); set(e.boss,"enraged",true); vulnerable(e.player);
        h.runAfterDelay(5,()->wall(h,12));
        h.runAfterDelay(45,()->{
            h.assertTrue(e.player.getHealth()==20,"terrain between core and player blocks the ring");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void fiberCutKeepsHoldAndPunishesCutter(GameTestHelper h) {
        var e=encounter(h,6); only(e.boss,"grabCooldown"); vulnerable(e.player);
        h.runAfterDelay(30,()->{
            h.assertTrue(e.boss.isGrabbing(),"fixture enters hold");
            com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonNerveEvents.announceGap(
                    h.getLevel(),h.absolutePos(new BlockPos(3,6,3)),e.player);
        });
        h.runAfterDelay(45,()->{
            h.assertTrue(e.boss.isGrabbing(),"a cut fiber no longer frees the victim");
            h.assertTrue(e.player.hasEffect(EffectInit.neural_overload),"the cutter is struck by retaliating lightning");
            e.player.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void overloadCannotBeSkippedOrKilledByOrdinaryDamage(GameTestHelper h) {
        var boss=loneBoss(h);
        placeNodes(h,boss,new BlockPos(8,2,8));
        boss.hurt(h.getLevel().damageSources().generic(),1000);
        h.assertTrue(Math.abs(boss.getHealth()-15)<1e-4,"one huge hit stops at the ten percent line");
        h.runAfterDelay(2,()->h.assertTrue(boss.isOverloading(),"crossing the line starts Overload"));
        h.runAfterDelay(15,()->{
            boss.hurt(h.getLevel().damageSources().generic(),1000);
            h.assertTrue(boss.isAlive() && Math.abs(boss.getHealth()-1.5F)<1e-4,"Overload holds the boss at one percent");
            boss.kill();
            h.assertTrue(!boss.isAlive(),"kill damage still removes an overloaded boss");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=200)
    public static void overloadBurnsNodesThenDrains(GameTestHelper h) {
        var boss=loneBoss(h);
        var nodes=placeNodes(h,boss,new BlockPos(4,6,8),new BlockPos(12,6,8));
        var watcher=victim(h,boss,boss.position().add(0,0,6)); watcher.setInvulnerable(true);
        int[] firstBurst={-1};
        h.onEachTick(()->{
            if(firstBurst[0]<0 && nodes.stream().anyMatch(n->h.getLevel().getBlockState(n).isAir()))
                firstBurst[0]=((java.util.List<?>)get(boss,"homing")).size();
        });
        boss.hurt(h.getLevel().damageSources().generic(),1000);
        h.runAfterDelay(5,()->h.assertTrue(boss.animationPhase()==NaeglerophaeonEntity.OVERLOAD,"the boss settles at the web's centre"));
        h.runAfterDelay(105,()->{
            for(BlockPos node:nodes) h.assertTrue(h.getLevel().getBlockState(node).isAir(),"every node is burned out");
            h.assertTrue(firstBurst[0]>=2 && firstBurst[0]<=6,"a broken node releases two to six sparks, saw "+firstBurst[0]);
            h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(h.absolutePos(new BlockPos(8,6,8))).inflate(16)).isEmpty(),"burned nodes drop nothing");
            h.assertTrue(boss.isDrained() && boss.animationPhase()==NaeglerophaeonEntity.DRAINED,"the spent boss becomes Drained");
            boss.hurt(h.getLevel().damageSources().playerAttack(watcher),1000);
            h.assertTrue(!boss.isAlive(),"a Drained boss can be finished");
            watcher.discard(); h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void overloadWithoutNodesDrainsAtOnce(GameTestHelper h) {
        var boss=loneBoss(h);
        boss.hurt(h.getLevel().damageSources().generic(),1000);
        h.runAfterDelay(5,()->{ h.assertTrue(boss.isDrained(),"no nodes left to burn means Drained"); h.succeed(); });
    }
    /** Its own batch: neighbouring fixtures' players would otherwise count as an audience. */
    @GameTest(template="empty",timeoutTicks=180,batch="naeglerophaeon_solo")
    public static void overloadPausesWithoutAudience(GameTestHelper h) {
        var boss=loneBoss(h);
        var nodes=placeNodes(h,boss,new BlockPos(4,6,8),new BlockPos(12,6,8));
        boss.hurt(h.getLevel().damageSources().generic(),1000);
        h.runAfterDelay(90,()->{
            for(BlockPos node:nodes) h.assertTrue(h.getLevel().getBlockState(node).is(BlockInit.synaptic_node.get()),"no pulses fire with nobody nearby");
            h.assertTrue(boss.isOverloading(),"the boss waits in Overload");
            h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=180)
    public static void overloadAndDrainedSurviveReload(GameTestHelper h) {
        var overloaded=loneBoss(h);
        placeNodes(h,overloaded,new BlockPos(8,2,8));
        overloaded.hurt(h.getLevel().damageSources().generic(),1000);
        var drained=h.spawn(EntityInit.naeglerophaeon.get(),new BlockPos(20,6,8));
        drained.hurt(h.getLevel().damageSources().generic(),1000);
        h.runAfterDelay(15,()->{
            drained.hurt(h.getLevel().damageSources().generic(),10);
            var tag=new net.minecraft.nbt.CompoundTag(); overloaded.addAdditionalSaveData(tag);
            var restored=EntityInit.naeglerophaeon.get().create(h.getLevel()); restored.readAdditionalSaveData(tag);
            h.assertTrue(restored.isOverloading() && Math.abs(restored.getHealth()-15)<1e-4,"Overload resumes after reload");
            tag=new net.minecraft.nbt.CompoundTag(); drained.addAdditionalSaveData(tag);
            restored=EntityInit.naeglerophaeon.get().create(h.getLevel()); restored.readAdditionalSaveData(tag);
            h.assertTrue(restored.isDrained() && Math.abs(restored.getHealth()-5)<1e-4,"Drained health is not clamped back up on load");
            h.succeed();
        });
    }
}
