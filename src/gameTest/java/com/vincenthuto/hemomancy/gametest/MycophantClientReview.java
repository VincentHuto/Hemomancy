package com.vincenthuto.hemomancy.gametest;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.boss.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Explicit opt-in visual fixture for the Mycophant (the Spawn Mother): held stills of every pose from every side and
 * with the head opened, the drag cycle, then live scenes against an invulnerable dummy with each move forced in turn,
 * the split, the yielding and the waking seen through the player's own eyes. {@code hold_live} fights the real
 * survival player instead: it is caught, breaks free, crosses half health while held, breaks a fusion, lets a meter
 * fill at Hard and is siphoned, logging a MYCOPHANT_HOLD_RESULT line with each check.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MycophantClientReview {
    private record Scene(String name, String pose, double distance, double height, double yaw, double lift, int[] shots, int length) {
        Scene(String name, String pose, double distance, double height, double yaw) {
            this(name, pose, distance, height, yaw, 0.0D, new int[]{30}, 34);
        }
    }

    private static final MycophantAttack[] PHASE_ONE = {MycophantAttack.RUSHING_EMBRACE, MycophantAttack.SIPHON_FEED,
            MycophantAttack.PLUCK, MycophantAttack.CLAW_RAKE, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE};
    private static final MycophantAttack[] PHASE_TWO = {MycophantAttack.FUSION_EMBRACE, MycophantAttack.WING_GUARD,
            MycophantAttack.WING_SWEEP, MycophantAttack.PULP_BLOOM};
    private static final int ATTACK_START = 20;

    private static final Scene[] SCENES = {
            new Scene("front", "stalk", 15.0, 1.5, 0.0), new Scene("side", "stalk", 17.0, 1.0, 1.5708),
            new Scene("rear", "stalk", 16.0, 3.5, 3.1416), new Scene("three_quarter", "stalk", 16.0, 3.0, 0.7),
            new Scene("top", "stalk", 8.0, 16.0, 0.4),
            new Scene("head", "stalk", 5.0, 0.6, 0.45, 2.6, new int[]{30}, 34),
            new Scene("pose_tending", "tending", 15.0, 2.0, 0.6), new Scene("pose_roar", "roar", 16.0, 1.5, 0.5),
            new Scene("pose_embrace", "embrace", 15.0, 1.5, 0.5), new Scene("pose_hold", "hold", 14.0, 1.5, 0.6),
            new Scene("pose_spawn", "spawn", 16.0, 3.0, 2.4), new Scene("pose_siphon", "siphon", 15.0, 1.5, -0.6),
            new Scene("pose_pluck", "pluck", 15.0, 1.5, 0.7), new Scene("pose_yield", "yield", 15.0, 1.5, 0.5),
            new Scene("p2_front", "stalk_p2", 16.0, 2.0, 0.0), new Scene("p2_head", "stalk_p2", 5.0, 0.6, 0.2, 2.6, new int[]{30}, 34),
            new Scene("p2_guard", "guard_p2", 16.0, 2.0, 0.5), new Scene("p2_sweep", "sweep_p2", 17.0, 2.0, 0.4),
            new Scene("p2_bloom", "bloom_p2", 15.0, 2.0, 0.5),
            new Scene("drag_cycle", "drag", 17.0, 1.0, 1.2, 0.0, everyOther(10, 46), 48),
            new Scene("live_phase_one", null, 18.0, 4.0, 1.0, 0.0, attackShots(PHASE_ONE, 1), attackLength(PHASE_ONE, 1)),
            new Scene("live_phase_two", null, 18.0, 4.0, 1.0, 0.0, attackShots(PHASE_TWO, 2), attackLength(PHASE_TWO, 2)),
            new Scene("split", null, 15.0, 2.0, 0.5, 0.0, new int[]{12, 20, 28, 36, 44, 52, 60, 76, 92}, 96),
            // close on the face as it cracks and opens, then behind the shoulder as the wings grow (split tick = tick - 5)
            new Scene("split_head", null, 7.0, 0.2, 0.3, 3.6, everyOther(6, 60), 64),
            new Scene("split_wings", null, 7.5, 2.0, 2.2, 1.6, everyOther(36, 92), 94),
            new Scene("yield", null, 15.0, 2.0, 0.6, 0.0, new int[]{10, 25, 40, 55, 70, 90, 110, 128}, 130),
            new Scene("intro_pov", null, 0.0, 0.0, 0.0, 0.0, new int[]{4, 20, 30, 44, 60, 76, 80, 84, 90, 110, 125, 140, 155, 175, 195, 205}, 210),
            new Scene("nursery_wide", "tending", 17.0, 2.0, 0.5), new Scene("nursery_high", "tending", 12.0, 9.0, 2.6),
            new Scene("nursery_rear", "tending", 15.0, 0.5, 3.1416),
            new Scene("capture_pov", "tending", 0.0, 0.0, 0.0, 0.0, new int[]{10, 30, 50, 70, 84, 96, 104}, 110),
            new Scene("glimpse", "tending", 0.0, 0.0, 0.0, 0.0, new int[]{10, 18, 30, 44, 60}, 64),
            new Scene("hold_live", null, 0.0, 0.0, 0.0, 0.0, new int[0], 2400),
    };
    private static final BlockPos CENTER = new BlockPos(512, 100, 512);

    private static int[] everyOther(int from, int to) {
        return java.util.stream.IntStream.iterate(from, i -> i <= to, i -> i + 2).toArray();
    }

    private static int start(MycophantAttack[] attacks, int index, int phase) {
        int at = ATTACK_START;
        for (int i = 0; i < index; i++) at += MycophantControllerRules.timeline(attacks[i], phase).total() + 16;
        return at;
    }

    private static int[] attackShots(MycophantAttack[] attacks, int phase) {
        List<Integer> shots = new ArrayList<>();
        for (int i = 0; i < attacks.length; i++) {
            var timeline = MycophantControllerRules.timeline(attacks[i], phase);
            int s = start(attacks, i, phase);
            for (int at : new int[]{timeline.windup() / 2, timeline.windup() - 1, timeline.windup() + timeline.active() / 2,
                    timeline.windup() + timeline.active() + timeline.recovery() / 2}) shots.add(s + at);
        }
        return shots.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int attackLength(MycophantAttack[] attacks, int phase) {
        return start(attacks, attacks.length, phase);
    }

    private static boolean wanted(Scene scene) {
        String only = System.getProperty("hemomancy.mycophantReview.only", "");
        if (only.isBlank()) return true;
        for (String name : only.split(",")) if (scene.name().equals(name.trim())) return true;
        return false;
    }

    private static int scene, tick, captured;
    private static boolean started;
    private static volatile boolean ready;
    private static MycophantEntity actor;
    private static Villager dummy;
    private static volatile String pendingShot;
    private static volatile boolean sceneFinished;
    private static HoldProbe probe;

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.mycophantReview")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (client.player == null || client.getSingleplayerServer() == null) return;
        if (client.player.isDeadOrDying()) {
            // a save left by a fight the candidate lost: stand them back up before any scene
            client.player.respawn();
            return;
        }
        try {
            if (scene == SCENES.length) {
                MycophantModel.reviewPose = null;
                MycophantEntity.reviewHoldPose = null;
                Files.writeString(client.gameDirectory.toPath().resolve("mycophant-visuals.txt"),
                        "Native fixtures for every Spawn Mother pose and side, the opened head, the drag, each move live, "
                                + "the split, the yielding and the waking from the player's eyes.\n");
                LogUtils.getLogger().info("MYCOPHANT_REVIEW captured {}", captured);
                client.stop();
                return;
            }
            var server = client.getSingleplayerServer();
            var uuid = client.player.getUUID();
            if (!started && !wanted(SCENES[scene])) {
                scene++;
                return;
            }
            Scene current = SCENES[scene];
            if (!started) {
                started = true;
                ready = false;
                boolean screen = current.name().equals("intro_pov") || current.name().equals("capture_pov");
                client.options.hideGui = !screen;
                MycophantModel.reviewPose = current.name().equals("capture_pov") || current.name().equals("glimpse") ? null : current.pose();
                MycophantEntity.reviewHoldPose = MycophantModel.reviewPose;
                MycophantGlimpse.clear();
                if (current.name().equals("intro_pov")) MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.WAKING, 11);
                else if (current.name().equals("capture_pov")) MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.CAPTURE, 11);
                else MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.CLEAR, 0);
                server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), current));
                return;
            }
            if (!ready) return;
            tick++;
            int at = tick;
            if (current.name().equals("glimpse") && tick == 2) MycophantGlimpse.start(0x5EED);
            server.execute(() -> drive(server.getPlayerList().getPlayer(uuid), current, at));
            String requested = pendingShot;
            if (requested != null) {
                pendingShot = null;
                Screenshot.grab(client.gameDirectory, "mycophant-" + current.name() + "-" + requested + ".png",
                        client.getMainRenderTarget(), message -> LogUtils.getLogger().info("MYCOPHANT_SCREENSHOT {}", message.getString()));
                captured++;
            }
            for (int shot : current.shots()) {
                if (tick == shot) {
                    Screenshot.grab(client.gameDirectory, "mycophant-" + current.name() + (current.shots().length > 1 ? "-" + shot : "") + ".png",
                            client.getMainRenderTarget(), message -> LogUtils.getLogger().info("MYCOPHANT_SCREENSHOT {}", message.getString()));
                    captured++;
                }
            }
            if (tick > current.length() || sceneFinished) {
                scene++;
                tick = 0;
                started = false;
                sceneFinished = false;
            }
        } catch (Exception exception) {
            LogUtils.getLogger().error("Mycophant native review failed", exception);
            client.stop();
        }
    }

    private static void setup(ServerPlayer player, Scene scene) {
        ServerLevel level = player.server.getLevel(Level.OVERWORLD);
        var source = player.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"gamemode spectator", "gamerule doMobSpawning false", "gamerule doDaylightCycle false",
                scene.name().equals("intro_pov") ? "time set 18000" : "time set 1000", "weather clear", "difficulty normal", "kill @e[type=minecraft:item]"})
            player.server.getCommands().performPrefixedCommand(source, cmd);
        // keep the platform loaded however far away the save left the player, so a fresh boss is never unloaded to disk
        for (int cx = (CENTER.getX() - 32) >> 4; cx <= (CENTER.getX() + 32) >> 4; cx++)
            for (int cz = (CENTER.getZ() - 32) >> 4; cz <= (CENTER.getZ() + 32) >> 4; cz++) level.setChunkForced(cx, cz, true);
        level.getEntities((Entity) null, new AABB(CENTER).inflate(56), e -> !(e instanceof net.minecraft.world.entity.player.Player))
                .forEach(Entity::discard);
        for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-28, -2, -28), CENTER.offset(28, 22, 28))) {
            if (pos.getY() == CENTER.getY() - 2) {
                // a solid bed under the floor, so the nectar pool has a bottom like the Nursery's barrier floor
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
                continue;
            }
            int dx = pos.getX() - CENTER.getX(), dz = pos.getZ() - CENTER.getZ();
            boolean floor = pos.getY() == CENTER.getY() - 1;
            if (!floor && (Math.abs(pos.getX() - CENTER.getX()) == 28 || Math.abs(pos.getZ() - CENTER.getZ()) == 28)) {
                // a barrier ring like the Nursery's, so a push near the edge never drops the player off the platform
                level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                continue;
            }
            boolean pool = pos.getY() == CENTER.getY() - 1 && dx * dx + dz * dz <= 16;
            level.setBlock(pos, pool ? FluidInit.MORPHIC_NECTAR.get().defaultFluidState().createLegacyBlock()
                    : floor ? Blocks.MYCELIUM.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        }
        for (int x = -10; x <= 10; x++)
            for (int y = 0; y < 8; y++)
                level.setBlock(CENTER.offset(x, y, 20), (x + y) % 3 == 0 ? Blocks.WARPED_WART_BLOCK.defaultBlockState()
                        : Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);

        String name = scene.name();
        // the real Morphic Nursery, its floor surface where the platform's is, for the waking and the arena stills
        if (name.equals("intro_pov") || name.startsWith("nursery")) MycophantEncounterManager.buildNursery(level, CENTER.below(2));
        boolean live = scene.pose() == null;
        boolean holdLive = name.equals("hold_live");
        dummy = null;
        if (live && !name.equals("intro_pov") && !holdLive) {
            dummy = EntityType.VILLAGER.create(level);
            dummy.setNoAi(true);
            dummy.setInvulnerable(true);
            dummy.setPersistenceRequired();
            dummy.moveTo(Vec3.atBottomCenterOf(CENTER.offset(0, 0, -9)), 0.0F, 0.0F);
            level.addFreshEntity(dummy);
        }
        MycophantEntity boss = EntityInit.mycophant.get().create(level);
        boss.setPersistenceRequired();
        float facing = name.equals("intro_pov") ? 0.0F : 180.0F;
        boss.moveTo(Vec3.atBottomCenterOf(CENTER), facing, 0.0F);
        boss.setYHeadRot(facing);
        boss.yBodyRot = facing;
        if (name.equals("intro_pov")) boss.setEncounterOwner(player.getUUID());
        if (dummy != null) boss.setTarget(dummy);
        if (name.equals("live_phase_two")) forcePhaseTwo(boss);
        level.addFreshEntity(boss);
        actor = boss;

        if (holdLive) {
            // the real candidate, in survival, nine blocks in front of her
            player.server.getCommands().performPrefixedCommand(source, "gamemode survival");
            player.setHealth(player.getMaxHealth());
            player.teleportTo(level, CENTER.getX() + 0.5, CENTER.getY(), CENTER.getZ() - 9.0 + 0.5, 0.0F, -12.0F);
            boss.setTarget(player);
            probe = new HoldProbe();
        } else if (name.equals("intro_pov")) {
            // the candidate's own eyes, where the Nursery delivers them: behind her, eleven blocks out
            player.teleportTo(level, CENTER.getX() + 0.5, CENTER.getY(), CENTER.getZ() - 11.0, 0.0F, -8.0F);
        } else if (name.equals("capture_pov")) {
            // taken in the open, facing away from her
            boss.discard();
            player.teleportTo(level, CENTER.getX() + 0.5, CENTER.getY(), CENTER.getZ() - 8.0, 180.0F, 0.0F);
        } else if (name.equals("glimpse")) {
            // she is glimpsed behind the player; they turn to look at tick 14
            boss.discard();
            player.teleportTo(level, CENTER.getX() + 0.5, CENTER.getY(), CENTER.getZ() + 12.0, 0.0F, 0.0F);
        } else {
            double eye = 2.4 + scene.lift();
            Vec3 focus = boss.position().add(0, eye, 0);
            double x = focus.x + Math.sin(scene.yaw()) * scene.distance();
            double z = focus.z - Math.cos(scene.yaw()) * scene.distance();
            double y = focus.y + scene.height();
            float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - x), focus.z - z));
            float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - y, Math.hypot(focus.x - x, focus.z - z)));
            player.teleportTo(level, x, y - player.getEyeHeight(), z, yaw, pitch);
        }
        LogUtils.getLogger().info("MYCOPHANT_CLIENT_REVIEW {}", name);
        ready = true;
    }

    private static void drive(ServerPlayer player, Scene scene, int tick) {
        if (scene.name().equals("glimpse")) {
            // no boss here: the glimpse is client-only; the candidate turns round to look at tick 14
            if (tick == 14) player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 180.0F, -4.0F);
            return;
        }
        if (actor == null || !actor.isAlive() && !scene.name().equals("yield")) {
            if (tick % 100 == 0) LogUtils.getLogger().info("MYCOPHANT_LIVE {} t={} no live actor ({})", scene.name(), tick,
                    actor == null ? "null" : actor.getRemovalReason());
            return;
        }
        String name = scene.name();
        if (name.startsWith("live_phase")) {
            MycophantAttack[] attacks = name.equals("live_phase_one") ? PHASE_ONE : PHASE_TWO;
            int phase = name.equals("live_phase_one") ? 1 : 2;
            for (int i = 0; i < attacks.length; i++) if (tick == start(attacks, i, phase)) begin(actor, attacks[i]);
            if (dummy != null) actor.setTarget(dummy);
        } else if (name.startsWith("split") && tick == 4) {
            Object controller = invoke(actor, "controller", new Class<?>[0]);
            invoke(controller, "requestSplit", new Class<?>[0]);
        } else if (name.equals("yield") && tick == 4) {
            actor.kill();
        } else if (name.equals("hold_live") && probe != null) {
            actor.setTarget(player);
            probe.tick(player, actor, tick);
        }
        if (tick % 20 == 0) LogUtils.getLogger().info("MYCOPHANT_LIVE {} t={} state={} attack={} tick={} phase={} split={}",
                name, tick, actor.getState(), actor.getAttack(), actor.getAttackTick(), actor.getPhase(), actor.getSplitTick());
    }

    /**
     * Drives the hold checks one step at a time against the real player. Each step waits for her to be idle, starts the
     * move it needs and watches what the review fixes promised: she stays put while holding and the player is never
     * pushed into a wall (C1); a split crossed mid-hold really splits (C2); a broken fusion lands its refusal wound (I1);
     * a filled meter at Hard leaves the player on the floor (I2); the siphon latches onto the player.
     */
    private static final class HoldProbe {
        private int step, elapsed, heldTicks, nextHit, markTick = -1, landedTick = -1, deathsAtCatch;
        private boolean begun, wasHolding, walled, splitSeen;
        private Vec3 caughtAt;
        private double drift;
        private float healthAtBreak = -1.0F;
        private final List<String> results = new ArrayList<>();

        void tick(ServerPlayer player, MycophantEntity boss, int tick) {
            elapsed++;
            // her other moves land for real; keep the candidate standing except where a step measures their health
            if (step != 3 && player.isAlive()) player.setHealth(player.getMaxHealth());
            boolean holding = boss.isHolding();
            if (holding && !wasHolding) {
                caughtAt = boss.position();
                heldTicks = 0;
                nextHit = tick + 30;
                LogUtils.getLogger().info("MYCOPHANT_HOLD step={} caught t={} attack={}", step, tick, boss.getAttack());
                pendingShot = "caught-" + step;
            }
            if (holding) {
                heldTicks++;
                drift = Math.max(drift, boss.position().distanceTo(caughtAt));
                walled |= player.isInWall();
                if (heldTicks == 20) pendingShot = "held-" + step;
            }
            if (boss.getState() == MycophantState.SPLIT) splitSeen = true;
            boolean idle = boss.getState() == MycophantState.IDLE && !holding;
            switch (step) {
                case 0 -> { // phase one: caught, then five hits tear the player free
                    if (!begun && elapsed > 20) start(boss, MycophantAttack.RUSHING_EMBRACE);
                    if (holding && tick >= nextHit) {
                        hit(player, boss);
                        nextHit = tick + 12;
                    }
                    if (wasHolding && !holding) {
                        record("C1 hold drift=" + fmt(drift) + " inWall=" + walled, drift < 1.0D && !walled);
                        record("break by hits state=" + boss.getState(), boss.getState() == MycophantState.STAGGER);
                        next();
                    }
                }
                case 1 -> { // caught again, then she crosses half health while still holding
                    if (!begun && idle && elapsed > 3) start(boss, MycophantAttack.RUSHING_EMBRACE);
                    if (holding && heldTicks == 25) {
                        // her armor (20) takes a 20-point blow to about 12, so this crosses half health while held
                        boss.setHealth(boss.getMaxHealth() * 0.5F + 4.0F);
                        boss.hurt(player.damageSources().playerAttack(player), 20.0F);
                        LogUtils.getLogger().info("MYCOPHANT_HOLD split hit health={} holding={}", fmt(boss.getHealth()), boss.isHolding());
                    }
                    if (wasHolding && !holding && markTick < 0) markTick = tick;
                    if (markTick >= 0 && tick - markTick == 120) {
                        record("C2 split while held splitSeen=" + splitSeen + " phase=" + boss.getPhase() + " state=" + boss.getState()
                                + " holding=" + boss.isHolding(), splitSeen && boss.getPhase() == 2 && !boss.isHolding());
                        next();
                    }
                }
                case 2 -> { // phase two: a fusion broken by six hits lands its refusal wound on the next tick
                    if (!begun && idle && elapsed > 3) start(boss, MycophantAttack.FUSION_EMBRACE);
                    if (holding && tick >= nextHit) {
                        float before = boss.getHealth();
                        hit(player, boss);
                        LogUtils.getLogger().info("MYCOPHANT_HOLD fusion hit dealt={} holding={}", fmt(before - boss.getHealth()), boss.isHolding());
                        nextHit = tick + 12;
                        if (!boss.isHolding()) {
                            healthAtBreak = boss.getHealth();
                            markTick = tick;
                            pendingShot = "fusion-broken";
                        }
                    }
                    if (healthAtBreak > 0.0F && tick - markTick >= 1 && tick - markTick <= 3)
                        LogUtils.getLogger().info("MYCOPHANT_HOLD after break +{} health={} state={}", tick - markTick, fmt(boss.getHealth()), boss.getState());
                    if (healthAtBreak > 0.0F && tick - markTick == 3) {
                        float drop = healthAtBreak - boss.getHealth();
                        record("I1 refusal drop=" + fmt(drop) + " want>=" + fmt(boss.getMaxHealth() * 0.08F),
                                drop >= boss.getMaxHealth() * 0.08F - 0.01F);
                        // the same wound dealt by hand, to tell a missing refusal from a refused one
                        float before = boss.getHealth();
                        boolean landed = boss.hurt(HemoDamageTypes.mycophantRefusal(boss.level(), player), 57.6F);
                        LogUtils.getLogger().info("MYCOPHANT_HOLD manual refusal landed={} dealt={}", landed, fmt(before - boss.getHealth()));
                        next();
                    }
                }
                case 3 -> { // a meter left to fill at Hard leaves the player on the floor, not below it
                    if (elapsed == 1) command(player, "difficulty hard");
                    if (!begun && idle && elapsed > 3) start(boss, MycophantAttack.FUSION_EMBRACE);
                    if (holding && heldTicks == 1) {
                        player.setHealth(8.0F);
                        deathsAtCatch = deaths(player);
                    }
                    if ((holding && heldTicks % 20 == 0) || (markTick >= 0 && tick - markTick <= 40))
                        LogUtils.getLogger().info("MYCOPHANT_HOLD_FALL t={} held={} y={} fall={} ground={} health={}", tick, holding,
                                fmt(player.getY()), fmt(player.fallDistance), player.onGround(), fmt(player.getHealth()));
                    if (wasHolding && !holding && markTick < 0) {
                        record("I2 meter at hard health=" + fmt(player.getHealth()), player.isAlive() && player.getHealth() >= 4.0F - 0.01F);
                        command(player, "difficulty normal");
                        markTick = tick;
                    }
                    // two ticks after they first touch ground again: the hold must not have stored a fall
                    if (markTick >= 0 && landedTick < 0 && tick > markTick && player.onGround()) landedTick = tick;
                    if (landedTick >= 0 && tick - landedTick == 2) {
                        boolean sameLife = deaths(player) == deathsAtCatch;
                        record("landing after the meter health=" + fmt(player.getHealth()) + " sameLife=" + sameLife,
                                sameLife && player.isAlive() && player.getHealth() >= 4.0F - 0.01F);
                        if (player.isAlive()) player.setHealth(player.getMaxHealth());
                        next();
                    }
                }
                case 4 -> { // the siphon latches onto the player standing in front of her
                    if (!begun && idle && elapsed > 3) {
                        // seven blocks from her toward the middle of the platform, so the fixture never drops the player off its edge
                        Vec3 middle = Vec3.atBottomCenterOf(CENTER);
                        Vec3 toward = middle.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
                        if (toward.lengthSqr() < 1.0D) toward = new Vec3(0.0D, 0.0D, -1.0D);
                        Vec3 spot = boss.position().add(toward.normalize().scale(7.0D));
                        // facing her, so the shot shows the tether leaving her right side
                        float yaw = (float) Math.toDegrees(Math.atan2(-(boss.getX() - spot.x), boss.getZ() - spot.z));
                        player.teleportTo((ServerLevel) boss.level(), spot.x, CENTER.getY(), spot.z, yaw, -18.0F);
                        start(boss, MycophantAttack.SIPHON_FEED);
                    }
                    if (boss.getSiphonTargetId() == player.getId() && markTick < 0) markTick = tick;
                    if (markTick >= 0 && tick - markTick == 8) pendingShot = "siphon";
                    if (markTick >= 0 && tick - markTick == 10) {
                        record("siphon latched on player", true);
                        next();
                    }
                }
                case 5 -> {
                    boolean pass = !results.isEmpty() && results.stream().allMatch(line -> line.startsWith("PASS"));
                    LogUtils.getLogger().info("MYCOPHANT_HOLD_RESULT all={} {}", pass ? "PASS" : "FAIL", results);
                    command(player, "difficulty normal");
                    step++;
                    sceneFinished = true;
                }
                default -> { }
            }
            if (step < 5 && elapsed > 420) {
                record("step " + step + " timed out state=" + boss.getState() + " attack=" + boss.getAttack(), false);
                next();
            }
            wasHolding = holding;
        }

        private void start(MycophantEntity boss, MycophantAttack attack) {
            begin(boss, attack);
            begun = true;
        }

        private static int deaths(ServerPlayer player) {
            return player.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.DEATHS));
        }

        private static void hit(ServerPlayer player, MycophantEntity boss) {
            boss.hurt(player.damageSources().playerAttack(player), 4.0F);
        }

        private static void command(ServerPlayer player, String command) {
            player.server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), command);
        }

        private void record(String line, boolean pass) {
            String entry = (pass ? "PASS " : "FAIL ") + line;
            results.add(entry);
            LogUtils.getLogger().info("MYCOPHANT_HOLD {}", entry);
        }

        private void next() {
            step++;
            elapsed = 0;
            begun = false;
            drift = 0.0D;
            walled = false;
            markTick = -1;
            landedTick = -1;
            healthAtBreak = -1.0F;
        }

        private static String fmt(double value) {
            return String.format(java.util.Locale.ROOT, "%.2f", value);
        }
    }

    private static void forcePhaseTwo(MycophantEntity boss) {
        invoke(boss, "setPhase", new Class<?>[]{int.class}, 2);
        invoke(boss, "finishSplit", new Class<?>[0]);
        boss.setHealth(boss.getMaxHealth() * 0.45F);
    }

    private static void begin(MycophantEntity boss, MycophantAttack attack) {
        Object controller = invoke(boss, "controller", new Class<?>[0]);
        invoke(controller, "begin", new Class<?>[]{MycophantAttack.class}, attack);
    }

    private static Object invoke(Object target, String method, Class<?>[] types, Object... args) {
        try {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    Method found = type.getDeclaredMethod(method, types);
                    found.setAccessible(true);
                    return found.invoke(target, args);
                } catch (NoSuchMethodException ignored) {
                    type = type.getSuperclass();
                }
            }
            throw new NoSuchMethodException(method);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Mycophant review could not call " + method, exception);
        }
    }
}
