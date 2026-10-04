package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SchoolAwarenessCombatGameTests {
    private static final class MeleeProbePlayer extends net.minecraft.server.level.ServerPlayer {
        private net.minecraft.world.entity.Entity watchedAttacker;
        private int hurtCalls;
        private int rejectedHurtCalls;

        private MeleeProbePlayer(GameTestHelper h, net.minecraft.server.network.CommonListenerCookie cookie) {
            super(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        }

        @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
            boolean watched = watchedAttacker != null && source.getEntity() == watchedAttacker;
            if (watched) hurtCalls++;
            boolean accepted = super.hurt(source, amount);
            if (watched && !accepted) rejectedHurtCalls++;
            return accepted;
        }
    }

    private static MeleeProbePlayer meleePlayer(GameTestHelper h) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "melee-probe"), false);
        var player = new MeleeProbePlayer(h, cookie);
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2, 2, 2)));
        h.getLevel().addNewPlayer(player);
        return player;
    }

    private static void floor(GameTestHelper h) {
        for (int x = 0; x < 18; x++) for (int z = 0; z < 18; z++) h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="school_awareness", timeoutTicks=110)
    public static void obscuredSkeletonStopsDistantBowFireButDefendsAtCloseRange(GameTestHelper h) {
        floor(h);
        var player = DuctilisGameTests.player(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        player.setHealth(1000);
        var skeleton = h.spawn(EntityType.SKELETON, new BlockPos(2, 2, 10));
        skeleton.setPersistenceRequired();
        skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        skeleton.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        skeleton.setTarget(player);
        SchoolStates.apply(player, skeleton, SchoolState.OBSCURED, 200);
        h.runAfterDelay(25, () -> {
            h.assertTrue(skeleton.getTarget() == null && !skeleton.isUsingItem() && player.getHealth() == 1000,
                    "Distant skeleton retained its target or fired through Obscured");
            player.setPos(skeleton.position().add(0, 0, -2.5));
            skeleton.setTarget(player);
        });
        h.runAfterDelay(95, () -> {
            try {
                h.assertTrue(player.getHealth() < 1000, "Close bow: health=" + player.getHealth() + ", target=" + skeleton.getTarget() + ", using=" + skeleton.isUsingItem());
                h.succeed();
            } finally { skeleton.discard(); player.discard(); }
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="school_awareness", timeoutTicks=80)
    public static void obscuredMeleeMobCanPursueAndHitANearbyEnemy(GameTestHelper h) {
        floor(h);
        var player = meleePlayer(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        player.setHealth(1000);
        var husk = h.spawn(EntityType.HUSK, new BlockPos(2, 2, 5));
        player.watchedAttacker = husk;
        husk.setPersistenceRequired();
        SchoolStates.apply(player, husk, SchoolState.OBSCURED, 160);
        husk.setTarget(player);
        var start = husk.position();
        int[] contactTicks = {0};
        int[] incomingDamageEvents = {0};
        java.util.function.Consumer<net.neoforged.neoforge.event.tick.EntityTickEvent.Post> contactProbe = event -> {
            if (event.getEntity() == husk && husk.isWithinMeleeAttackRange(player)
                    && husk.getSensing().hasLineOfSight(player)) contactTicks[0]++;
        };
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent> attackProbe = event -> {
            if (event.getEntity() == player && event.getSource().getEntity() == husk) incomingDamageEvents[0]++;
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(contactProbe);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(attackProbe);
        Runnable cleanup = () -> {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(contactProbe);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(attackProbe);
            husk.discard(); player.discard();
        };
        h.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo test) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo test,
                    net.minecraft.gametest.framework.GameTestRunner runner) { cleanup.run(); }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo test,
                    net.minecraft.gametest.framework.GameTestRunner runner) { cleanup.run(); }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo oldTest,
                    net.minecraft.gametest.framework.GameTestInfo newTest,
                    net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        // Navigation timing varies; require a real approach and hit within the declared 80-tick limit.
        h.succeedWhen(() -> {
            h.assertTrue(husk.position().distanceToSqr(start) > .25 && player.getHealth() < 1000,
                    "Close melee: moved=" + husk.position().distanceToSqr(start)
                            + ", health=" + player.getHealth() + ", target=" + husk.getTarget()
                            + ", huskPos=" + husk.position() + ", playerPos=" + player.position()
                            + ", huskTicks=" + husk.tickCount + ", playerTicks=" + player.tickCount
                            + ", alive=" + husk.isAlive() + "/" + player.isAlive()
                            + ", distanceSquared=" + husk.distanceToSqr(player)
                            + ", canAttack=" + husk.canAttack(player)
                            + ", inAttackRange=" + husk.isWithinMeleeAttackRange(player)
                            + ", cachedSight=" + husk.getSensing().hasLineOfSight(player)
                            + ", runningGoals=" + husk.goalSelector.getAvailableGoals().stream()
                                    .filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning)
                                    .map(goal -> goal.getGoal().getClass().getSimpleName()).toList()
                            + ", contactTicks=" + contactTicks[0] + ", incomingDamageEvents=" + incomingDamageEvents[0]
                            + ", hurtCalls=" + player.hurtCalls + ", rejectedHurtCalls=" + player.rejectedHurtCalls
                            + ", invulnerable=" + player.isInvulnerableTo(h.getLevel().damageSources().mobAttack(husk))
                            + ", attackDamage=" + husk.getAttributeValue(Attributes.ATTACK_DAMAGE)
                            + ", sight=" + husk.hasLineOfSight(player)
                            + ", navigationDone=" + husk.getNavigation().isDone()
                            + ", onGround=" + husk.onGround() + "/" + player.onGround()
                            + ", huskChunkLoaded=" + h.getLevel().areEntitiesLoaded(husk.chunkPosition().toLong())
                            + ", playerChunkLoaded=" + h.getLevel().areEntitiesLoaded(player.chunkPosition().toLong()));
        });
    }

    @GameTest(templateNamespace="hemomancy", template="ductilis_arena", batch="school_awareness", timeoutTicks=65)
    public static void runningPiglinBrainCannotReacquireAnUnseenDistantTarget(GameTestHelper h) {
        floor(h);
        var player = DuctilisGameTests.player(h);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (int tick = 0; tick < 61; tick++) player.tick();
        var piglin = h.spawn(EntityType.PIGLIN, new BlockPos(2, 2, 10));
        piglin.setImmuneToZombification(true);
        piglin.setPersistenceRequired();
        piglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        piglin.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
        SchoolStates.apply(player, piglin, SchoolState.OBSCURED, 160);
        h.runAfterDelay(25, () -> {
            h.assertTrue(piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty(),
                    "Brain reacquired a distant target while Obscured");
            player.setPos(piglin.position().add(0, 0, -2));
            piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, player.getUUID());
            piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
        });
        h.runAfterDelay(32, () -> {
            try {
                h.assertTrue(piglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == player,
                        "Brain lost a permitted close-range target");
                h.succeed();
            } finally { piglin.discard(); player.discard(); }
        });
    }

}
