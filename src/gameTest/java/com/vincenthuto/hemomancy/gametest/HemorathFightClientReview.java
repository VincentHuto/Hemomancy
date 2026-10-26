package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.common.entity.boss.goal.EmptyPulseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.goal.HematicCollapseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
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

/** Opt-in native review: real attack goals and overload, with scripted targets and Minimal particles. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HemorathFightClientReview {
    private static final String[] SCENES = {"idle", "collapse", "empty_pulse", "phase_two", "fast_collapse", "melee", "stored_blood", "overload"};
    private static final int[] LENGTHS = {50, 150, 90, 55, 90, 35, 55, 140};
    private static final JsonObject RESULTS = new JsonObject();
    private static volatile int scene = -1, tick;
    private static volatile boolean done;
    private static boolean launched, stopped;
    private static int capturedScene = -1, capturedTick = -1, closing, rewardsBefore, overloadAt = -1;
    private static MinecraftServer server;
    private static ServerPlayer player;
    private static HemorathEntity boss;
    private static Husk target;
    private static Goal goal;
    private static ParticleStatus originalParticles;
    private static Path report;

    private HemorathFightClientReview() {}

    @SubscribeEvent
    public static void client(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.hemorathFightReview")) return;
        Minecraft mc = Minecraft.getInstance();
        if (!launched && mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
            launched = true;
            server = mc.getSingleplayerServer();
            originalParticles = mc.options.particles().get();
            mc.options.particles().set(ParticleStatus.MINIMAL);
            mc.options.pauseOnLostFocus = false;
            report = mc.gameDirectory.toPath().resolve("operator/hemorath-fight-review.json");
            var id = mc.player.getUUID();
            server.execute(() -> {
                try { player = server.getPlayerList().getPlayer(id); setup(); }
                catch (Exception error) { fail(error); }
            });
        }
        if (!launched) return;
        // Phase cards are reviewed elsewhere; this fixture keeps its fixed camera on the body and effect geometry.
        BossSceneDirector.INSTANCE.clear();
        int current = scene, frame = tick;
        if (current >= 0 && current < SCENES.length && frame % 6 == 0
                && (capturedScene != current || capturedTick != frame)) {
            capturedScene = current; capturedTick = frame;
            String name = String.format("hemorath-fight-%02d-%s-%03d.png", current, SCENES[current], frame);
            Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
        }
        if (done && ++closing > 24) {
            mc.options.particles().set(originalParticles);
            mc.stop();
        }
    }

    private static void setup() {
        if (player == null) throw new IllegalStateException("Review player is missing");
        ServerLevel level = player.serverLevel();
        for (int x = 1680; x <= 1720; x += 8) for (int z = 500; z <= 536; z += 8)
            level.getChunkAt(new BlockPos(x, 200, z));
        // Retire only stale actors in this opt-in review stage after an interrupted run.
        AABB stage = new AABB(1682, 198, 502, 1719, 215, 538);
        level.getEntitiesOfClass(HemorathEntity.class, stage).forEach(actor -> actor.discard());
        level.getEntitiesOfClass(Husk.class, stage).forEach(actor -> actor.discard());
        for (BlockPos pos : BlockPos.betweenClosed(1682,199,502,1718,199,537))
            level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
        for (BlockPos pos : BlockPos.betweenClosed(1686,200,528,1714,213,528))
            level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(1698,199,519), Blocks.OCHRE_FROGLIGHT.defaultBlockState(), 2);
        level.setBlock(new BlockPos(1702,199,519), Blocks.OCHRE_FROGLIGHT.defaultBlockState(), 2);
        level.setDayTime(18000);
        player.setGameMode(GameType.SPECTATOR);
        player.teleportTo(level, 1700.5, 201.5, 511.0, 0, 0);
        boss = EntityInit.hemorath.get().create(level);
        target = EntityType.HUSK.create(level);
        if (boss == null || target == null) throw new IllegalStateException("Review entities failed to create");
        boss.setNoAi(true); boss.setInvulnerable(true); boss.setPersistenceRequired();
        boss.moveTo(1700.5, 200, 520.5, 180, 0);
        boss.yBodyRot = boss.yBodyRotO = boss.yHeadRot = boss.yHeadRotO = 180;
        target.setNoAi(true); target.setPersistenceRequired();
        target.moveTo(1697.5, 200, 516.5, 0, 0);
        level.addFreshEntity(boss); level.addFreshEntity(target);
        scene = 0; tick = 0;
        Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW started with Minimal particles");
    }

    @SubscribeEvent
    public static void server(ServerTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.hemorathFightReview") || event.getServer() != server || scene < 0 || done) return;
        try {
            if (tick == 0) enter();
            if (goal != null) {
                if (goal.canContinueToUse()) { if (tick % 2 == 0) goal.tick(); }
                else if (!stopped) { goal.stop(); stopped = true; }
            }
            if (scene == 5 && tick == 4) {
                boss.swing(InteractionHand.MAIN_HAND, true);
                boss.doHurtTarget(target);
            }
            if (scene == 7 && overloadAt < 0 && boss.getVisualState() == HemorathEntity.VISUAL_EMPTY_PULSE
                    && boss.getVisualTicks() <= 5 && boss.getVisualTicks() > 0) {
                if (target.hasEffect(EffectInit.hemophagy))
                    throw new IllegalStateException("AI pulse fired before overload setup");
                overloadAt = tick;
                boss.setCollapseCharging(true);
                boss.addAbsorbedBlood(player, 1000);
            }
            if (scene == 7 && overloadAt >= 0 && !boss.isRemoved()) {
                if (boss.getVisualState() != HemorathEntity.VISUAL_OVERLOAD || !boss.isNoAi()
                        || boss.isCollapseCharging() || boss.getTarget() != null || target.hasEffect(EffectInit.hemophagy))
                    throw new IllegalStateException("Combat AI interrupted terminal overload");
            }
            if (++tick >= LENGTHS[scene]) {
                verifyScene();
                if (goal != null && !stopped) goal.stop();
                goal = null; stopped = false;
                if (++scene >= SCENES.length) {
                    target.discard();
                    RESULTS.addProperty("passed", true);
                    writeReport(); done = true;
                    Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW_OK {}", RESULTS);
                } else tick = 0;
            }
        } catch (Exception error) { fail(error); }
    }

    private static void enter() {
        target.moveTo(1697.5, 200, 516.5, 0, 0);
        target.setDeltaMovement(0,0,0);
        target.setHealth(target.getMaxHealth());
        target.removeEffect(EffectInit.hemophagy);
        boss.setTarget(target);
        switch (scene) {
            case 1, 4 -> { goal = new HematicCollapseGoal(boss); goal.start(); }
            case 2 -> { goal = new EmptyPulseGoal(boss); goal.start(); }
            case 3 -> boss.setHealth(100);
            case 6 -> boss.addAbsorbedBlood(player, 2100);
            case 7 -> {
                rewardsBefore = residuumCount();
                // The normal selector must start and tick a real pulse before overload interrupts it.
                boss.goalSelector.removeAllGoals(candidate -> true);
                boss.goalSelector.addGoal(0, new EmptyPulseGoal(boss));
                boss.setNoAi(false);
            }
            default -> {}
        }
    }

    private static void verifyScene() {
        boolean pass = switch (scene) {
            case 1, 4, 5 -> target.getHealth() < target.getMaxHealth();
            case 2 -> target.hasEffect(EffectInit.hemophagy);
            case 3 -> boss.isInPhase2();
            case 6 -> boss.getAbsorbedBlood() > 2800 && boss.getAbsorbedBlood() < 3000;
            case 7 -> overloadAt >= 0 && boss.isRemoved() && !target.hasEffect(EffectInit.hemophagy)
                    && residuumCount() == rewardsBefore + 1;
            default -> true;
        };
        RESULTS.addProperty(SCENES[scene], pass);
        if (!pass) throw new IllegalStateException("Failed review contract: " + SCENES[scene]);
    }

    private static int residuumCount() {
        return player.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hallowed_residuum_hemorath.get()))
                .mapToInt(stack -> stack.getCount()).sum();
    }

    private static void fail(Exception error) {
        RESULTS.addProperty("passed", false); RESULTS.addProperty("error", error.toString());
        Hemomancy.LOGGER.error("HEMORATH_FIGHT_REVIEW_FAILED", error);
        if (boss != null) boss.discard();
        if (target != null) target.discard();
        writeReport(); done = true;
    }

    private static void writeReport() {
        try {
            Files.createDirectories(report.getParent());
            Files.writeString(report, new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));
        } catch (Exception error) { Hemomancy.LOGGER.error("Could not save Hemorath review report", error); }
    }
}
