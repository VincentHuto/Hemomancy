package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantClaimRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantHoldKind;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BroodTumorBlock;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

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
            MycophantAttack.PLUCK, MycophantAttack.RAKE_STRING, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE,
            MycophantAttack.BROOD_SETTLE, MycophantAttack.CLAW_PIN, MycophantAttack.TAIL_LASH, MycophantAttack.VENT_BLOOM,
            MycophantAttack.HAND_SWEEP, MycophantAttack.SIPHON_SPIT, MycophantAttack.SPORE_MORTAR, MycophantAttack.NURSING};
    // the joining also replays the phase-1 moves that phase 2 lengthens (the four-swing rake, the double lash, five globs)
    private static final MycophantAttack[] PHASE_TWO = {MycophantAttack.FUSION_EMBRACE, MycophantAttack.WING_GUARD,
            MycophantAttack.WING_SWEEP, MycophantAttack.PULP_BLOOM, MycophantAttack.RAKE_STRING, MycophantAttack.TAIL_LASH,
            MycophantAttack.SIPHON_SPIT, MycophantAttack.HYPHAE_LANCES, MycophantAttack.LATTICE_NET, MycophantAttack.GAZE};
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
            // the expanded moveset: the claws, belly and tail from the side they work on, the wing and pulp moves opened
            new Scene("pose_rake_l", "rake_l", 15.0, 1.5, -0.6), new Scene("pose_rake_r", "rake_r", 15.0, 1.5, 0.6),
            new Scene("pose_slam", "slam", 15.0, 2.0, 0.5),
            // low and close on the striking claw, where a reach that sinks into the floor shows
            new Scene("pose_rake_l_close", "rake_l", 8.0, -0.5, -0.9, -1.6, new int[]{30}, 34),
            new Scene("pose_rake_r_close", "rake_r", 8.0, -0.5, 0.9, -1.6, new int[]{30}, 34),
            new Scene("pose_settle_rise", "settle_rise", 16.0, 1.0, 1.5708), new Scene("pose_settle_drop", "settle_drop", 16.0, 1.0, 1.5708),
            new Scene("pose_pin", "pin", 14.0, 1.5, 0.8),
            new Scene("pose_tail_coil", "tail_coil", 15.0, 3.0, 2.6), new Scene("pose_tail_lash", "tail_lash", 15.0, 3.0, 2.6),
            new Scene("pose_vents_open", "vents_open", 15.0, 4.5, 2.4),
            new Scene("pose_hand_wind", "hand_wind", 15.0, 1.5, -0.5), new Scene("pose_hand_sweep", "hand_sweep", 15.0, 1.5, 0.5),
            new Scene("pose_spit", "spit", 15.0, 1.5, -0.6), new Scene("pose_mortar", "mortar", 16.0, 3.0, 2.0),
            new Scene("pose_nurse", "nurse", 15.0, 1.5, -0.6),
            new Scene("pose_lances", "lances", 16.0, 2.0, 0.5), new Scene("pose_net", "net", 16.0, 2.0, 0.3),
            new Scene("pose_gaze", "gaze", 15.0, 1.5, 0.2),
            new Scene("pose_last_brood_rear", "last_brood_rear", 16.0, 2.0, 0.5),
            new Scene("pose_last_brood_pull", "last_brood_pull", 15.0, 1.5, 0.4),
            new Scene("pose_last_brood_collapse", "last_brood_collapse", 16.0, 2.5, 0.7),
            new Scene("drag_cycle", "drag", 17.0, 1.0, 1.2, 0.0, everyOther(10, 46), 48),
            new Scene("live_phase_one", null, 18.0, 4.0, 1.0, 0.0, attackShots(PHASE_ONE, 1), attackLength(PHASE_ONE, 1)),
            new Scene("live_phase_two", null, 18.0, 4.0, 1.0, 0.0, attackShots(PHASE_TWO, 2), attackLength(PHASE_TWO, 2)),
            // phase 2 below a quarter: the tell, the pull on the dummy, then the probe cuts the four tendrils and she collapses
            new Scene("live_last_brood", null, 18.0, 4.0, 1.0, 0.0, new int[]{28, 40, 49, 60, 90, 130, 152, 160, 175, 200, 225}, 245),
            new Scene("split", null, 15.0, 2.0, 0.5, 0.0, new int[]{12, 20, 28, 36, 44, 52, 60, 76, 92}, 96),
            // close on the face as it cracks and opens, then behind the shoulder as the wings grow (split tick = tick - 5)
            new Scene("split_head", null, 7.0, 0.2, 0.3, 3.6, everyOther(6, 60), 64),
            new Scene("split_wings", null, 7.5, 2.0, 2.2, 1.6, everyOther(36, 92), 94),
            new Scene("yield", null, 15.0, 2.0, 0.6, 0.0, new int[]{10, 25, 40, 55, 70, 90, 110, 128}, 130),
            new Scene("intro_pov", null, 0.0, 0.0, 0.0, 0.0, new int[]{4, 20, 30, 44, 60, 76, 80, 84, 90, 110, 125, 140, 155, 165, 175, 195, 205}, 210),
            // the rematch through the boss scene director: the shot circling her turn, the card over the roar, the view back before the rush
            new Scene("rematch_pov", null, 0.0, 0.0, 0.0, 0.0, new int[]{2, 8, 14, 22, 30, 38, 44, 52, 60}, 64),
            new Scene("nursery_wide", "tending", 17.0, 2.0, 0.5), new Scene("nursery_high", "tending", 12.0, 9.0, 2.6),
            new Scene("nursery_rear", "tending", 15.0, 0.5, 3.1416),
            new Scene("capture_pov", "tending", 0.0, 0.0, 0.0, 0.0, new int[]{10, 30, 50, 70, 84, 96, 104}, 110),
            new Scene("glimpse", "tending", 0.0, 0.0, 0.0, 0.0, new int[]{10, 18, 30, 44, 60}, 64),
            new Scene("hold_live", null, 0.0, 0.0, 0.0, 0.0, new int[0], 4800),
    };
    private static final BlockPos CENTER = new BlockPos(512, 100, 512);

    private static int[] everyOther(int from, int to) {
        return java.util.stream.IntStream.iterate(from, i -> i <= to, i -> i + 2).toArray();
    }

    /** The live scenes play each string in its longest form: the delayed slam (phase 1), four swings and the double lash (phase 2). */
    private static int variantFor(MycophantAttack attack, int phase) {
        return switch (attack) {
            case RAKE_STRING -> phase >= 2 ? 2 : 1;
            case TAIL_LASH -> phase >= 2 ? 1 : 0;
            default -> 0;
        };
    }

    private static BossMoveTimeline liveTimeline(MycophantAttack attack, int phase) {
        return MycophantControllerRules.timeline(attack, phase, variantFor(attack, phase));
    }

    private static int start(MycophantAttack[] attacks, int index, int phase) {
        int at = ATTACK_START;
        for (int i = 0; i < index; i++) at += liveTimeline(attacks[i], phase).total() + 16;
        return at;
    }

    private static int[] attackShots(MycophantAttack[] attacks, int phase) {
        List<Integer> shots = new ArrayList<>();
        for (int i = 0; i < attacks.length; i++) {
            var timeline = liveTimeline(attacks[i], phase);
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
                boolean screen = current.name().equals("intro_pov") || current.name().equals("capture_pov")
                        || current.name().equals("rematch_pov");
                client.options.hideGui = !screen;
                MycophantModel.reviewPose = current.name().equals("capture_pov") || current.name().equals("glimpse") ? null : current.pose();
                MycophantEntity.reviewHoldPose = MycophantModel.reviewPose;
                MycophantGlimpse.clear();
                BossSceneDirector.INSTANCE.clear();
                if (current.name().equals("intro_pov")) {
                    MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.WAKING, 11);
                    BossSceneDirector.INSTANCE.start(BossPresentationIds.SCENE_MYCOPHANT_WAKING, -1, 0);
                } else if (current.name().equals("capture_pov")) {
                    MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.CAPTURE, 11);
                    BossSceneDirector.INSTANCE.start(BossPresentationIds.SCENE_MYCOPHANT_CAPTURE, -1, 0);
                } else MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.CLEAR, 0);
                server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), current));
                return;
            }
            if (!ready) return;
            tick++;
            int at = tick;
            if (current.name().equals("rematch_pov") && tick == 1 && actor != null) {
                MycophantIntroClientState.INSTANCE.start(MycophantIntroClientState.REMATCH, 11);
                BossSceneDirector.INSTANCE.start(BossPresentationIds.SCENE_MYCOPHANT_REMATCH, actor.getId(), 0);
            }
            if (current.name().equals("hold_live")) watchHeldClient(client.player);
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

    private static int clientHeldTicks;
    private static double clientStep;
    private static Vec3 clientLast;
    private static MycophantHoldKind clientKind = MycophantHoldKind.SAC;

    /**
     * The held player's own client: once drawn in, it must sit still at the hold point. Logs the largest per-tick move
     * after the pull for each hold of every kind (the sac, the claw pin, the cocoon); a client that falls and is snapped
     * back shows here as steps of a tenth of a block.
     */
    private static void watchHeldClient(LocalPlayer player) {
        List<MycophantEntity> holders = player.level().getEntitiesOfClass(MycophantEntity.class, player.getBoundingBox().inflate(12.0D),
                boss -> boss.getClaimedId() == player.getId());
        boolean held = !holders.isEmpty();
        Vec3 at = player.position();
        if (held) {
            clientKind = holders.get(0).getHoldKind();
            if (++clientHeldTicks > MycophantClaimRules.holdPullTicks() && clientLast != null)
                clientStep = Math.max(clientStep, at.distanceTo(clientLast));
        } else if (clientHeldTicks > 0) {
            LogUtils.getLogger().info("MYCOPHANT_HOLD_JITTER kind={} held={} maxStepAfterPull={}", clientKind, clientHeldTicks,
                    String.format(java.util.Locale.ROOT, "%.4f", clientStep));
            clientHeldTicks = 0;
            clientStep = 0.0D;
        }
        clientLast = at;
    }

    /** What the Gaze dealt the probed player (the beam's blows come from her while the Gaze is her move). */
    @SubscribeEvent public static void gazeDamage(LivingDamageEvent.Post event) {
        HoldProbe watching = probe;
        if (watching == null || !(event.getEntity() instanceof ServerPlayer)) return;
        if (event.getSource().getEntity() instanceof MycophantEntity boss && boss.getAttack() == MycophantAttack.GAZE)
            watching.gazeDamage += event.getNewDamage();
    }

    private static void setup(ServerPlayer player, Scene scene) {
        ServerLevel level = player.server.getLevel(Level.OVERWORLD);
        var source = player.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"gamemode spectator", "gamerule doMobSpawning false", "gamerule doDaylightCycle false",
                scene.name().equals("intro_pov") || scene.name().equals("rematch_pov") ? "time set 18000" : "time set 1000", "weather clear", "difficulty normal", "kill @e[type=minecraft:item]"})
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
        boolean pov = name.equals("intro_pov") || name.equals("rematch_pov");
        if (pov || name.startsWith("nursery")) MycophantEncounterManager.buildNursery(level, CENTER.below(2));
        boolean live = scene.pose() == null;
        boolean holdLive = name.equals("hold_live");
        dummy = null;
        if (live && !pov && !holdLive) {
            dummy = EntityType.VILLAGER.create(level);
            dummy.setNoAi(true);
            dummy.setInvulnerable(true);
            dummy.setPersistenceRequired();
            dummy.moveTo(Vec3.atBottomCenterOf(CENTER.offset(0, 0, -9)), 0.0F, 0.0F);
            level.addFreshEntity(dummy);
        }
        MycophantEntity boss = EntityInit.mycophant.get().create(level);
        boss.setPersistenceRequired();
        float facing = pov ? 0.0F : 180.0F;
        boss.moveTo(Vec3.atBottomCenterOf(CENTER), facing, 0.0F);
        boss.setYHeadRot(facing);
        boss.yBodyRot = facing;
        if (pov) boss.setEncounterOwner(player.getUUID());
        if (name.equals("rematch_pov")) boss.setRematchIntro(true);
        if (dummy != null) boss.setTarget(dummy);
        if (name.equals("live_phase_two") || name.equals("live_last_brood")) forcePhaseTwo(boss);
        level.addFreshEntity(boss);
        actor = boss;

        if (holdLive) {
            // the real candidate, in survival, nine blocks in front of her
            player.server.getCommands().performPrefixedCommand(source, "gamemode survival");
            player.setHealth(player.getMaxHealth());
            player.teleportTo(level, CENTER.getX() + 0.5, CENTER.getY(), CENTER.getZ() - 9.0 + 0.5, 0.0F, -12.0F);
            boss.setTarget(player);
            probe = new HoldProbe();
        } else if (pov) {
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
            for (int i = 0; i < attacks.length; i++) {
                if (tick != start(attacks, i, phase)) continue;
                // nursing needs a young one of hers close by: a still, unharmed brood fungling at her right
                if (attacks[i] == MycophantAttack.NURSING) spawnBrood(actor, new Vec3(5.0D, 0.0D, -2.0D));
                // beginNow ends whatever follow-up she chained in the gap, so every move starts clean
                beginNow(actor, attacks[i]);
                invoke(actor, "setAttackVariant", new Class<?>[]{int.class}, variantFor(attacks[i], phase));
            }
            if (dummy != null) actor.setTarget(dummy);
        } else if (name.equals("live_last_brood")) {
            if (dummy != null) actor.setTarget(dummy);
            // below a quarter in phase 2: she starts it herself on her next tick
            if (tick == ATTACK_START) actor.setHealth(actor.getMaxHealth() * 0.24F);
            // late in the pull, cut all four binding tendrils (two blows each) for the cut-free collapse
            if (tick == ATTACK_START + MycophantLastBroodRules.TELL + 100 || tick == ATTACK_START + MycophantLastBroodRules.TELL + 104)
                for (MycophantHazardEntity tendril : MycophantHazardEntity.of((ServerLevel) actor.level(), actor, MycophantHazardEntity.Kind.BINDING_TENDRIL))
                    cutTendril(player, tendril);
            if (tick % 10 == 0) LogUtils.getLogger().info("MYCOPHANT_LIVE last_brood t={} outcome={} tendrils={} health={}", tick,
                    actor.getLastBroodOutcome(), MycophantHazardEntity.of((ServerLevel) actor.level(), actor, MycophantHazardEntity.Kind.BINDING_TENDRIL).size(),
                    String.format(java.util.Locale.ROOT, "%.1f", actor.getHealth()));
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
        private static final int RESULT = 10;
        private int step, elapsed, heldTicks, nextHit, markTick = -1, landedTick = -1, deathsAtCatch;
        private boolean begun, wasHolding, walled, splitSeen;
        private Vec3 caughtAt;
        private double drift;
        private float healthAtBreak = -1.0F;
        private final List<String> results = new ArrayList<>();
        // the expanded moveset's checks
        private MycophantHoldKind caughtKind = MycophantHoldKind.SAC;
        private int attempts, heldFor, seenTick = -1, gazeRun;
        private boolean seen, flagged, netCaught;
        float gazeDamage;
        private BlockPos tumor;

        void tick(ServerPlayer player, MycophantEntity boss, int tick) {
            elapsed++;
            // her other moves land for real; keep the candidate standing except where a step measures their health
            boolean measuring = step == 3 || (step == 9 && flagged);
            if (!measuring && player.isAlive()) player.setHealth(player.getMaxHealth());
            boolean holding = boss.isHolding();
            if (holding && !wasHolding) {
                caughtAt = boss.position();
                caughtKind = boss.getHoldKind();
                heldTicks = 0;
                nextHit = tick + 30;
                LogUtils.getLogger().info("MYCOPHANT_HOLD step={} caught t={} attack={} kind={}", step, tick, boss.getAttack(), caughtKind);
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
                case 5 -> { // the Claw Pin: pinned under the claw for its twenty ticks, then let go with no stagger
                    if (!begun && ready(boss) && elapsed > 3) {
                        boss.setHealth(boss.getMaxHealth() * 0.45F);
                        stage(player, boss, 5.5D);
                        start(boss, MycophantAttack.CLAW_PIN);
                        attempts++;
                    }
                    if (wasHolding && !holding && markTick < 0) {
                        markTick = tick;
                        heldFor = heldTicks;
                    }
                    if (markTick >= 0 && tick - markTick <= 10 && boss.getState() == MycophantState.STAGGER) flagged = true;
                    if (markTick >= 0 && tick - markTick == 10) {
                        record("claw pin kind=" + caughtKind + " held=" + heldFor + " staggered=" + flagged,
                                caughtKind == MycophantHoldKind.PIN && heldFor >= 16 && heldFor <= 24 && !flagged);
                        next();
                    }
                    // a stab that missed: stage it again (up to three times)
                    if (begun && markTick < 0 && !holding && boss.getAttack() != MycophantAttack.CLAW_PIN && ready(boss) && attempts < 3)
                        begun = false;
                }
                case 6 -> { // the Lattice Net: caught, then the Fusion Embrace follows
                    if (!begun && ready(boss) && elapsed > 3) {
                        boss.setHealth(boss.getMaxHealth() * 0.45F);
                        stage(player, boss, 9.0D);
                        start(boss, MycophantAttack.LATTICE_NET);
                        // the 85% follow-up roll, pinned so the check reads the rule and not the dice
                        setField(invoke(boss, "controller", new Class<?>[0]), "followRoll", 0.0D);
                    }
                    // the net lands and the follow-up is queued on the same tick, so its catch is read through the link gap too
                    // (the controller keeps it until the next move begins), and off the net itself
                    if (begun && (seen || boss.getAttack() == MycophantAttack.LATTICE_NET) && markTick < 0
                            && (boss.getAttack() == MycophantAttack.LATTICE_NET || boss.getAttack() == MycophantAttack.NONE)) {
                        flagged |= (Boolean) invoke(invoke(boss, "controller", new Class<?>[0]), "netted", new Class<?>[0]);
                        for (MycophantHazardEntity net : MycophantHazardEntity.of((ServerLevel) boss.level(), boss, MycophantHazardEntity.Kind.LATTICE_NET))
                            if (net.landed() && net.caught(player)) netCaught = true;
                    }
                    if (begun && boss.getAttack() == MycophantAttack.LATTICE_NET) {
                        seen = true;
                        if (boss.getAttackTick() == MycophantControllerRules.timeline(MycophantAttack.LATTICE_NET, 2).windup() + 17)
                            pendingShot = "net-landed";
                    }
                    if (begun && seen && seenTick < 0 && boss.getAttack() != MycophantAttack.LATTICE_NET) seenTick = tick;
                    if (seenTick >= 0 && markTick < 0 && boss.getAttack() != MycophantAttack.NONE
                            && boss.getAttack() != MycophantAttack.LATTICE_NET) {
                        record("lattice net netted=" + flagged + " caughtByNet=" + netCaught + " next=" + boss.getAttack(),
                                flagged && netCaught && boss.getAttack() == MycophantAttack.FUSION_EMBRACE);
                        markTick = tick;
                    }
                    if (seenTick >= 0 && markTick < 0 && tick - seenTick > 40) {
                        record("lattice net netted=" + flagged + " caughtByNet=" + netCaught + " next=none state=" + boss.getState(), false);
                        next();
                    }
                    if (markTick >= 0 && holding && heldTicks == 12) pendingShot = "net-fusion";
                    // once the netted player is in the fusion (or it missed), let them go and move on
                    if (markTick >= 0 && ((holding && heldTicks >= 14) || boss.getAttack() != MycophantAttack.FUSION_EMBRACE)) {
                        if (boss.isHolding()) boss.releaseHold();
                        next();
                    }
                }
                case 7 -> { // the Gaze: a tumor close in front of the player shelters them; in the open the beam lands
                    if (!begun && ready(boss) && elapsed > 3) {
                        boss.setHealth(boss.getMaxHealth() * 0.45F);
                        stage(player, boss, 10.0D);
                        tumor = CENTER.offset(0, 0, -9);
                        if (gazeRun == 0) {
                            boolean placed = ((BroodTumorBlock) BlockInit.brood_tumor.get()).place(boss.level(), tumor);
                            LogUtils.getLogger().info("MYCOPHANT_HOLD gaze tumor placed={} at={}", placed, tumor);
                        }
                        gazeDamage = 0.0F;
                        seen = false;
                        start(boss, MycophantAttack.GAZE);
                    }
                    int sweepAt = MycophantControllerRules.timeline(MycophantAttack.GAZE, 2).windup() + 20;
                    if (begun && boss.getAttack() == MycophantAttack.GAZE) {
                        seen = true;
                        if (boss.getAttackTick() == sweepAt) pendingShot = gazeRun == 0 ? "gaze-cover" : "gaze-open";
                    }
                    if (begun && seen && boss.getAttack() != MycophantAttack.GAZE) {
                        if (gazeRun == 0) {
                            record("gaze behind a tumor damage=" + fmt(gazeDamage), gazeDamage <= 0.0F);
                            ((BroodTumorBlock) BlockInit.brood_tumor.get()).burst(boss.level(), tumor);
                            gazeRun = 1;
                            begun = false;
                            seen = false;
                            elapsed = 0;
                        } else {
                            record("gaze in the open damage=" + fmt(gazeDamage), gazeDamage > 0.0F);
                            next();
                        }
                    }
                }
                case 8 -> { // the Last Brood, cut free: four tendrils cut twice each and she collapses
                    if (!begun && ready(boss) && elapsed > 3) {
                        stage(player, boss, 12.0D);
                        // below a quarter in phase 2: she starts it herself
                        boss.setHealth(boss.getMaxHealth() * 0.24F);
                        begun = true;
                    }
                    List<MycophantHazardEntity> tendrils = MycophantHazardEntity.of((ServerLevel) boss.level(), boss,
                            MycophantHazardEntity.Kind.BINDING_TENDRIL);
                    if (begun && seenTick < 0 && boss.getAttack() == MycophantAttack.LAST_BROOD
                            && tendrils.size() == MycophantLastBroodRules.TENDRILS) {
                        seenTick = tick;
                    }
                    // a few ticks on, once the client has drawn the tendrils, and before the first cut
                    if (seenTick >= 0 && tick == seenTick + 3) pendingShot = "last-brood-bound";
                    if (seenTick >= 0 && (tick == seenTick + 4 || tick == seenTick + 10)) {
                        LogUtils.getLogger().info("MYCOPHANT_HOLD cutting {} tendrils", tendrils.size());
                        for (MycophantHazardEntity tendril : tendrils) cutTendril(player, tendril);
                    }
                    if (seenTick >= 0 && tick == seenTick + 14) pendingShot = "last-brood-collapse";
                    if (seenTick >= 0 && tick == seenTick + 16) {
                        record("last brood cut free state=" + boss.getState() + " outcome=" + boss.getLastBroodOutcome()
                                        + " tendrils=" + tendrils.size(),
                                boss.getState() == MycophantState.STAGGER
                                        && boss.getLastBroodOutcome() == MycophantLastBroodRules.Outcome.CUT_FREE.ordinal());
                        next();
                    }
                }
                case 9 -> { // the Last Brood, cocooned: left alone the player is reeled in, sealed and let go alive
                    if (!begun && ready(boss) && elapsed > 3) {
                        stage(player, boss, 9.0D);
                        deathsAtCatch = deaths(player);
                        Object controller = invoke(boss, "controller", new Class<?>[0]);
                        invoke(controller, "beginNow", new Class<?>[]{MycophantAttack.class}, MycophantAttack.LAST_BROOD);
                        begun = true;
                    }
                    if (holding && heldTicks == 1 && boss.getHoldKind() == MycophantHoldKind.COCOON) {
                        // low enough that the cocoon's full toll would kill: the floor must hold
                        flagged = true;
                        player.setHealth(10.0F);
                    }
                    if (holding && heldTicks == 30) pendingShot = "cocoon";
                    if (flagged && wasHolding && !holding && markTick < 0) {
                        markTick = tick;
                        heldFor = heldTicks;
                    }
                    if (markTick >= 0 && tick - markTick == 3) {
                        boolean sameLife = deaths(player) == deathsAtCatch;
                        record("last brood cocooned kind=" + caughtKind + " held=" + heldFor + " outcome=" + boss.getLastBroodOutcome()
                                        + " health=" + fmt(player.getHealth()) + " sameLife=" + sameLife,
                                caughtKind == MycophantHoldKind.COCOON
                                        && boss.getLastBroodOutcome() == MycophantLastBroodRules.Outcome.COCOONED.ordinal()
                                        && sameLife && player.isAlive() && player.getHealth() >= 4.0F - 0.01F);
                        if (player.isAlive()) player.setHealth(player.getMaxHealth());
                        next();
                    }
                }
                case RESULT -> {
                    boolean pass = !results.isEmpty() && results.stream().allMatch(line -> line.startsWith("PASS"));
                    LogUtils.getLogger().info("MYCOPHANT_HOLD_RESULT all={} {}", pass ? "PASS" : "FAIL", results);
                    command(player, "difficulty normal");
                    step++;
                    sceneFinished = true;
                }
                default -> { }
            }
            if (step < RESULT && elapsed > 420) {
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
            attempts = 0;
            heldFor = 0;
            seenTick = -1;
            seen = false;
            flagged = false;
            netCaught = false;
        }

        /** Free to start a staged move: roaming (still or dragging), holding nobody, between moves. */
        private static boolean ready(MycophantEntity boss) {
            MycophantState state = boss.getState();
            return (state == MycophantState.IDLE || state == MycophantState.DRAG) && !boss.isHolding()
                    && boss.getAttack() == MycophantAttack.NONE;
        }

        /**
         * Puts her back on the platform's middle facing north and the player {@code distance} blocks straight in front
         * of her, facing her, so each staged move starts from the same, readable spot.
         */
        private static void stage(ServerPlayer player, MycophantEntity boss, double distance) {
            Vec3 middle = Vec3.atBottomCenterOf(CENTER);
            boss.moveTo(middle.x, middle.y, middle.z, 180.0F, 0.0F);
            boss.setYHeadRot(180.0F);
            boss.yBodyRot = 180.0F;
            boss.setDeltaMovement(Vec3.ZERO);
            player.teleportTo((ServerLevel) boss.level(), middle.x, CENTER.getY(), middle.z - distance, 0.0F, -12.0F);
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
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

    /** Starts {@code attack} at once, ending any move or follow-up in flight (the controller's own beginNow). */
    private static void beginNow(MycophantEntity boss, MycophantAttack attack) {
        Object controller = invoke(boss, "controller", new Class<?>[0]);
        invoke(controller, "beginNow", new Class<?>[]{MycophantAttack.class}, attack);
    }

    /** One blow on a binding tendril, as an arrow from {@code player}: a projectile needs no aim along its length. */
    private static void cutTendril(ServerPlayer player, MycophantHazardEntity tendril) {
        Arrow arrow = EntityType.ARROW.create(tendril.level());
        if (arrow == null) return;
        arrow.setOwner(player);
        tendril.hurt(player.damageSources().arrow(arrow, player), 4.0F);
    }

    /** A brood fungling of hers at {@code offset} from her feet, held still and unharmed so the review can watch it. */
    private static FunglingEntity spawnBrood(MycophantEntity boss, Vec3 offset) {
        FunglingEntity fungling = EntityInit.fungling.get().create(boss.level());
        Vec3 at = boss.position().add(offset);
        fungling.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        fungling.setBrood(boss.getUUID());
        fungling.setNoAi(true);
        fungling.setInvulnerable(true);
        fungling.setPersistenceRequired();
        boss.level().addFreshEntity(fungling);
        return fungling;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Mycophant review could not set " + name, exception);
        }
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
