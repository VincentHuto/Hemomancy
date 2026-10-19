package com.vincenthuto.hemomancy.gametest;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Explicit opt-in visual fixture for the Annetta encounter: still looks, then live scenes with real AI against an
 * invulnerable dummy (Phase 1 combat, the forced break and rise, Priestess flight, the purge and the ally fight).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AnnettaClientReview {
    private record Scene(String name, double distance, double height, double yaw, double lift, int[] shots, int length) {
        Scene(String name, double distance, double height, double yaw) {
            this(name, distance, height, yaw, 0.0D, new int[]{50}, 60);
        }
    }

    private static final int LATENT_ATTACK_START = 10;
    private static final int LATENT_ATTACK_SPACING = 44;

    /** One held pose: Annetta pinned at an action (or a break scene tick) for a few ticks, then photographed. */
    private record Pose(int action, int tick) {}
    private static final int POSE_HOLD = 6;
    private static final Pose[] PHASE_ONE_POSES = poses(
            AnnettaKnowlesEntity.ACTION_NONE, new int[]{0},
            AnnettaKnowlesEntity.ACTION_SCREAM, new int[]{6, 14},
            AnnettaKnowlesEntity.ACTION_DAGGER_COMBO, new int[]{3, 5, 7, 11, 14, 16, 19, 22, 25},
            AnnettaKnowlesEntity.ACTION_SHIELD_GUARD, new int[]{10},
            AnnettaKnowlesEntity.ACTION_SHIELD_BASH, new int[]{1, 4, 7, 10},
            AnnettaKnowlesEntity.ACTION_STILL_DASH, new int[]{5},
            AnnettaKnowlesEntity.ACTION_STILL_BOLT, new int[]{7, 10},
            AnnettaKnowlesEntity.ACTION_SILVER_AURA, new int[]{5, 11});
    private static final Pose[] ALLY_POSES = poses(
            AnnettaKnowlesEntity.ACTION_NONE, new int[]{0},
            AnnettaKnowlesEntity.ACTION_HEAL, new int[]{5, 12},
            AnnettaKnowlesEntity.ACTION_ALLY_BOLT, new int[]{5, 9},
            AnnettaKnowlesEntity.ACTION_ALLY_STRIKE, new int[]{4, 7, 11, 14},
            AnnettaKnowlesEntity.ACTION_STILL_DASH, new int[]{5});
    /** Break scene ticks: falter, clutch, grip, raise, the stab, the swell, the pop, headless, the fall, lying. */
    private static final int[] BREAK_TICKS = {8, 12, 18, 31, 37, 41, 47, 52, 55, 58, 70, 95, 120};

    private static final int MOVE_COMBO = 20, MOVE_JAVELIN = 100, MOVE_SWOOP = 160, MOVE_PLUMMET = 220, PRIESTESS_MOVES_END = 310;

    private static final Scene[] SCENES = {
            new Scene("cowering", 3.4, 0.9, 0.35), new Scene("phase_one_fresh", 3.6, 1.0, 0.55),
            new Scene("purged_resolved", 3.6, 1.0, -0.35),
            new Scene("priestess_spread", 13.0, 1.6, 0.25), new Scene("priestess_head", 2.4, 0.2, 0.35, 1.25, new int[]{50}, 60),
            new Scene("latent_front", 10.0, 2.5, 0.3),
            new Scene("latent_close", 6.5, 1.2, 0.75, 0.6, new int[]{30, 52}, 60),
            // A burst of frames two ticks apart, stitched into a clip to judge how the idle moves.
            new Scene("latent_motion", 8.0, 1.0, 0.9, 0.4, java.util.stream.IntStream.rangeClosed(1, 45).map(i -> 10 + i * 2).toArray(), 102),
            new Scene("live_latent", 11.0, 2.6, 1.6, 0.4, new int[]{30, 60, 90, 120, 150, 180, 210, 240}, 250),
            // Every attack forced in turn, side on, with frames at its wind-up, blow and recovery.
            new Scene("latent_attacks", 11.0, 1.4, 1.25, 0.2, latentAttackShots(), LATENT_ATTACK_START + 5 * LATENT_ATTACK_SPACING),
            new Scene("latent_emerge", 10.0, 1.2, 1.0, 0.2, new int[]{14, 22, 30, 38, 46, 60}, 62),
            new Scene("live_phase_one", 7.0, 2.2, 0.6, 0.0, new int[]{40, 70, 100, 130, 160, 190}, 200),
            // Held poses, side on and three-quarter, for every Phase 1 move and every ally move.
            new Scene("poses_side", 3.2, 0.3, 1.5708, 0.0, poseShots(PHASE_ONE_POSES.length), 12 + PHASE_ONE_POSES.length * POSE_HOLD),
            new Scene("poses_front", 3.2, 0.5, 0.55, 0.0, poseShots(PHASE_ONE_POSES.length), 12 + PHASE_ONE_POSES.length * POSE_HOLD),
            new Scene("ally_poses", 3.2, 0.4, 0.9, 0.0, poseShots(ALLY_POSES.length), 12 + ALLY_POSES.length * POSE_HOLD),
            new Scene("break_poses", 3.0, 0.3, -0.8, 0.15, poseShots(BREAK_TICKS.length), 12 + BREAK_TICKS.length * POSE_HOLD),
            // The combo and the shield counter run live at full speed, one frame per tick, to judge how they flow.
            new Scene("combo_motion", 4.6, 0.4, 1.1, 0.0, everyTick(10, 66), 68),
            new Scene("live_break", 5.0, 1.8, 0.5, 0.0, new int[]{14, 30, 44, 60, 92, 140, 250, 330, 360, 390, 405}, 410),
            new Scene("live_priestess", 15.0, 5.0, 0.4, 2.0, new int[]{40, 80, 120, 160, 200, 240, 280}, 290),
            // Each Priestess move forced in turn, side on: the spear combo, the javelin, the swoop and the plummet.
            new Scene("live_priestess_moves", 8.5, 1.4, 1.25, 0.8, priestessMoveShots(), PRIESTESS_MOVES_END),
            // Her guard close up, every other tick, to judge the joints and the grip.
            new Scene("priestess_guard", 4.2, 0.4, 1.0, 0.0, java.util.stream.IntStream.rangeClosed(0, 15).map(i -> 20 + i * 2).toArray(), 52),
            new Scene("live_purge", 9.0, 2.6, 0.45, 1.0, new int[]{15, 55, 92, 100, 115, 135, 180, 230, 280, 330}, 340),
    };
    private static final BlockPos CENTER = new BlockPos(512, 100, 512);

    private static Pose[] poses(Object... spec) {
        java.util.List<Pose> out = new java.util.ArrayList<>();
        for (int i = 0; i < spec.length; i += 2) for (int tick : (int[]) spec[i + 1]) out.add(new Pose((Integer) spec[i], tick));
        return out.toArray(Pose[]::new);
    }

    private static int[] poseShots(int count) {
        int[] shots = new int[count];
        for (int i = 0; i < count; i++) shots[i] = 10 + i * POSE_HOLD + POSE_HOLD - 1;
        return shots;
    }

    private static int[] priestessMoveShots() {
        java.util.List<Integer> shots = new java.util.ArrayList<>();
        for (int at : new int[]{4, 9, 11, 13, 20, 23, 25, 27, 29, 37, 40, 44, 46, 50, 58}) shots.add(MOVE_COMBO + at);
        for (int at : new int[]{6, 12, 15, 17, 20, 30, 38}) shots.add(MOVE_JAVELIN + at);
        for (int at : new int[]{6, 13, 17, 22}) shots.add(MOVE_SWOOP + at);
        for (int at : new int[]{14, 26, 33, 36, 40, 46, 60}) shots.add(MOVE_PLUMMET + at);
        return shots.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Forces the Priestess through her moves on a fixed clock; her own AI fills the gaps between. */
    private static void drivePriestessMoves(StainedPriestessEntity priestess, int tick) {
        Class<?>[] mode = {com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.class};
        Class<?>[] action = {int.class};
        if (tick == 1) {
            invoke(priestess, "setMode", mode, com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.LANDED);
            invoke(priestess, "setFlying", new Class<?>[]{boolean.class}, false);
        }
        if (tick < MOVE_JAVELIN) setField(priestess, "comboCooldown", 1000);
        if (tick == MOVE_COMBO) invoke(priestess, "setAction", action, StainedPriestessEntity.ACTION_COMBO);
        if (tick == MOVE_JAVELIN) {
            invoke(priestess, "setMode", mode, com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.HOVER);
            invoke(priestess, "setAction", action, StainedPriestessEntity.ACTION_LANCE);
        }
        if (tick == MOVE_SWOOP) invoke(priestess, "setMode", mode, com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.SWOOP);
        if (tick == MOVE_PLUMMET) invoke(priestess, "setMode", mode, com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.PLUMMET);
    }

    private static int[] everyTick(int from, int to) {
        return java.util.stream.IntStream.rangeClosed(from, to).toArray();
    }


    private static int[] latentAttackShots() {
        java.util.List<Integer> shots = new java.util.ArrayList<>();
        for (LatentCombatRules.Attack attack : LatentCombatRules.Attack.values()) {
            int start = LATENT_ATTACK_START + attack.ordinal() * LATENT_ATTACK_SPACING;
            int hit = LatentCombatRules.hitTick(attack);
            for (int at : new int[]{hit / 2, hit - 2, hit + 1, hit + 6}) shots.add(start + at);
        }
        return shots.stream().mapToInt(Integer::intValue).toArray();
    }

    private static boolean wanted(Scene scene) {
        String only = System.getProperty("hemomancy.annettaReview.only", "");
        if (only.isBlank()) return true;
        for (String name : only.split(",")) if (scene.name().equals(name.trim())) return true;
        return false;
    }
    private static int scene, tick, captured;
    private static boolean started;
    private static volatile boolean ready;
    private static Entity actor;
    private static Villager dummy;

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.annettaReview")) return;
        var client = Minecraft.getInstance();
        client.options.pauseOnLostFocus = false;
        if (client.player == null || client.getSingleplayerServer() == null) return;
        try {
            if (scene == SCENES.length) {
                Files.writeString(client.gameDirectory.toPath().resolve("annetta-visuals.txt"),
                        "Native fixtures for every Annetta look plus live AI scenes against an invulnerable dummy.\n");
                LogUtils.getLogger().info("ANNETTA_REVIEW captured {}", captured);
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
                client.options.hideGui = true;
                StainedPriestessModel.reviewWingPose = current.name().equals("priestess_spread")
                        || current.name().equals("priestess_head") ? StainedPriestessModel.WingPose.SPREAD : null;
                server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), current));
                return;
            }
            if (!ready) return;
            tick++;
            int at = tick;
            server.execute(() -> drive(server.getPlayerList().getPlayer(uuid), current, at));
            for (int shot : current.shots()) {
                if (tick == shot) {
                    Screenshot.grab(client.gameDirectory, "annetta-" + current.name() + (current.shots().length > 1 ? "-" + shot : "") + ".png",
                            client.getMainRenderTarget(), message -> LogUtils.getLogger().info("ANNETTA_SCREENSHOT {}", message.getString()));
                    captured++;
                }
            }
            if (tick > current.length()) {
                scene++;
                tick = 0;
                started = false;
            }
        } catch (Exception exception) {
            LogUtils.getLogger().error("Annetta native review failed", exception);
            client.stop();
        }
    }

    private static void setup(ServerPlayer player, Scene scene) {
        ServerLevel level = player.server.getLevel(Level.OVERWORLD);
        var source = player.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"gamemode spectator", "gamerule doMobSpawning false", "gamerule doDaylightCycle false",
                "time set 6000", "weather clear", "kill @e[type=hemomancy:annetta_pool]", "kill @e[type=minecraft:item]"})
            player.server.getCommands().performPrefixedCommand(source, cmd);
        level.getEntities((Entity) null, new AABB(CENTER).inflate(48), e -> !(e instanceof net.minecraft.world.entity.player.Player))
                .forEach(Entity::discard);
        for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-22, -1, -22), CENTER.offset(22, 16, 22)))
            level.setBlock(pos, pos.getY() == CENTER.getY() - 1 ? Blocks.POLISHED_DIORITE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        for (int x = -6; x <= 6; x++)
            for (int y = 0; y < 5; y++)
                level.setBlock(CENTER.offset(x, y, 12), (x + y) % 3 == 0 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                        : Blocks.CALCITE.defaultBlockState(), 2);

        String name = scene.name();
        AnnettaKnowlesEntity.reviewHoldClock = name.contains("poses");
        // The break runs on Peaceful: Annetta stays there, so the Priestess rising out of her must too.
        player.server.getCommands().performPrefixedCommand(source,
                name.equals("live_break") ? "difficulty peaceful" : "difficulty normal");
        if (name.equals("latent_close")) {
            // At night, so its glow and the infection under its skin can be judged.
            player.server.getCommands().performPrefixedCommand(source, "time set 18000");
        }
        boolean live = name.startsWith("live");
        dummy = null;
        if (live) {
            dummy = EntityType.VILLAGER.create(level);
            dummy.setNoAi(true);
            dummy.setInvulnerable(true);
            dummy.setPersistenceRequired();
            dummy.moveTo(Vec3.atBottomCenterOf(CENTER.offset(0, 0, -4)), 0.0F, 0.0F);
            level.addFreshEntity(dummy);
        }
        Mob mob;
        if (name.contains("priestess")) {
            mob = EntityInit.stained_priestess.get().create(level);
        } else if (name.startsWith("latent") || name.equals("live_latent")) {
            mob = EntityInit.latent_annetta_infection.get().create(level);
        } else {
            AnnettaKnowlesEntity annetta = EntityInit.annetta_knowles.get().create(level);
            AnnettaKnowlesEntity.EncounterState state = name.equals("cowering") || name.equals("live_purge")
                    ? AnnettaKnowlesEntity.EncounterState.COWERING
                    : name.equals("purged_resolved") ? AnnettaKnowlesEntity.EncounterState.RESOLVED
                    : name.equals("ally_poses") ? AnnettaKnowlesEntity.EncounterState.CURED_SUPPORT
                    : name.equals("break_poses") ? AnnettaKnowlesEntity.EncounterState.BREAKING
                    : AnnettaKnowlesEntity.EncounterState.PHASE_ONE;
            invoke(annetta, "setEncounterState", new Class<?>[]{AnnettaKnowlesEntity.EncounterState.class}, state);
            if (state == AnnettaKnowlesEntity.EncounterState.PHASE_ONE || state == AnnettaKnowlesEntity.EncounterState.BREAKING) {
                annetta.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.annettas_absolution_dagger.get()));
                annetta.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.unstained_shield.get()));
            }
            if (state == AnnettaKnowlesEntity.EncounterState.CURED_SUPPORT) {
                annetta.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.annettas_absolution_dagger.get()));
            }
            if (name.equals("live_break")) {
                // Both break conditions already met: the break begins on her first tick.
                setField(annetta, "phaseOneTicks", 2400);
                setField(annetta, "damageTaken", 60.0F);
            }
            mob = annetta;
        }
        mob.setNoAi(!live);
        mob.setPersistenceRequired();
        mob.moveTo(Vec3.atBottomCenterOf(CENTER), 180.0F, 0.0F);
        mob.setYHeadRot(180.0F);
        mob.yBodyRot = 180.0F;
        if (dummy != null) mob.setTarget(dummy);
        level.addFreshEntity(mob);
        actor = mob;
        if (name.equals("live_purge") && mob instanceof AnnettaKnowlesEntity annetta) {
            invoke(annetta, "startCureRoute", new Class<?>[]{ServerPlayer.class, ItemStack.class}, player,
                    new ItemStack(ItemInit.draught_of_still_mercy.get()));
        }

        double eye = mob.getBbHeight() * 0.55 + scene.lift();
        Vec3 focus = mob.position().add(0, eye, 0);
        double x = focus.x + Math.sin(scene.yaw()) * scene.distance();
        double z = focus.z - Math.cos(scene.yaw()) * scene.distance();
        double y = focus.y + scene.height();
        float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - x), focus.z - z));
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - y, Math.hypot(focus.x - x, focus.z - z)));
        player.teleportTo(level, x, y - player.getEyeHeight(), z, yaw, pitch);
        LogUtils.getLogger().info("ANNETTA_CLIENT_REVIEW {} difficulty={}", name, level.getDifficulty());
        ready = true;
    }

    /** Keeps the live fights pointed at the dummy as bodies change: the rising Priestess and the torn-out infection. */
    private static void drive(ServerPlayer player, Scene scene, int tick) {
        if (actor instanceof AnnettaKnowlesEntity annetta && scene.name().contains("poses")) {
            int index = Math.max(0, (tick - 10) / POSE_HOLD);
            if (scene.name().equals("break_poses")) {
                setAnnettaData(annetta, "DATA_SCENE_TICK", BREAK_TICKS[Math.min(index, BREAK_TICKS.length - 1)] - 1);
            } else {
                Pose[] poses = scene.name().equals("ally_poses") ? ALLY_POSES : PHASE_ONE_POSES;
                Pose pose = poses[Math.min(index, poses.length - 1)];
                invoke(annetta, "setAction", new Class<?>[]{int.class, int.class}, pose.action(), Math.max(0, pose.tick() - 1));
            }
            return;
        }
        if (actor instanceof AnnettaKnowlesEntity annetta && scene.name().equals("combo_motion")) {
            // Combo from tick 10, then a blocked blow at 42 sets off the shield counter.
            if (tick == 10) invoke(annetta, "setAction", new Class<?>[]{int.class, int.class}, AnnettaKnowlesEntity.ACTION_DAGGER_COMBO, 0);
            if (tick == 44) invoke(annetta, "setAction", new Class<?>[]{int.class, int.class}, AnnettaKnowlesEntity.ACTION_SHIELD_BASH, 0);
            return;
        }
        if (actor instanceof LatentAnnettaInfectionEntity latent && scene.name().equals("latent_attacks")) {
            int index = (tick - LATENT_ATTACK_START) / LATENT_ATTACK_SPACING;
            int at = (tick - LATENT_ATTACK_START) % LATENT_ATTACK_SPACING;
            if (tick >= LATENT_ATTACK_START && index < LatentCombatRules.Attack.values().length) {
                LatentCombatRules.Attack attack = LatentCombatRules.Attack.values()[index];
                invoke(latent, "setAction", new Class<?>[]{LatentCombatRules.Attack.class}, at < LatentCombatRules.duration(attack) ? attack : null);
                setData(latent, "DATA_ACTION_TICK", at);
            }
            return;
        }
        if (actor instanceof LatentAnnettaInfectionEntity latent && scene.name().equals("latent_emerge")) {
            setData(latent, "DATA_EMERGE_TICK", Math.min(LatentCombatRules.EMERGE_END, LatentCombatRules.EMERGE_HIDDEN - 10 + tick));
            return;
        }
        if (actor instanceof StainedPriestessEntity priestess && scene.name().equals("priestess_guard")) {
            // Grounded in guard, held still.
            invoke(priestess, "setMode", new Class<?>[]{com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.class},
                    com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode.LANDED);
            invoke(priestess, "setFlying", new Class<?>[]{boolean.class}, false);
            return;
        }
        if (actor instanceof StainedPriestessEntity priestess && scene.name().equals("live_priestess_moves")) {
            drivePriestessMoves(priestess, tick);
        }
        if (dummy == null || player == null) return;
        ServerLevel level = player.serverLevel();
        AABB area = new AABB(CENTER).inflate(40);
        for (StainedPriestessEntity priestess : level.getEntitiesOfClass(StainedPriestessEntity.class, area)) priestess.setTarget(dummy);
        for (LatentAnnettaInfectionEntity latent : level.getEntitiesOfClass(LatentAnnettaInfectionEntity.class, area)) latent.setTarget(dummy);
        if (tick % 20 == 0) {
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e instanceof AnnettaKnowlesEntity || e instanceof StainedPriestessEntity || e instanceof LatentAnnettaInfectionEntity)) {
                LogUtils.getLogger().info("ANNETTA_LIVE {} t={} {}", scene.name(), tick, describe(living));
            }
        }
    }

    private static String describe(LivingEntity entity) {
        if (entity instanceof AnnettaKnowlesEntity a) return "annetta " + a.getEncounterState() + " action=" + a.getAction() + " scene=" + a.getSceneTick()
                + " yaw=" + Math.round(a.yBodyRot) + " head=" + Math.round(a.getYHeadRot());
        if (entity instanceof StainedPriestessEntity p) return "priestess " + p.getMode() + " action=" + p.getAction() + " y=" + String.format("%.1f", p.getY());
        if (entity instanceof LatentAnnettaInfectionEntity l) return "latent attack=" + l.getAttack() + " emerge=" + l.getEmergeTick();
        return entity.toString();
    }

    @SuppressWarnings("unchecked")
    private static void setAnnettaData(AnnettaKnowlesEntity annetta, String accessor, int value) {
        try {
            Field field = AnnettaKnowlesEntity.class.getDeclaredField(accessor);
            field.setAccessible(true);
            annetta.getEntityData().set((net.minecraft.network.syncher.EntityDataAccessor<Integer>) field.get(null), value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Review data " + accessor + " unavailable", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setData(LatentAnnettaInfectionEntity latent, String accessor, int value) {
        try {
            Field field = LatentAnnettaInfectionEntity.class.getDeclaredField(accessor);
            field.setAccessible(true);
            latent.getEntityData().set((net.minecraft.network.syncher.EntityDataAccessor<Integer>) field.get(null), value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Review data " + accessor + " unavailable", exception);
        }
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Review field " + name + " unavailable", exception);
        }
    }

    private static void invoke(Object target, String name, Class<?>[] types, Object... values) {
        try {
            Method method = target.getClass().getDeclaredMethod(name, types);
            method.setAccessible(true);
            method.invoke(target, values);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Review hook " + name + " unavailable", exception);
        }
    }
}
