package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.sickleStrokeReview, holds the Living Sickle's attack stroke at fixed progress values and captures
 * each from first person, from behind and from the side into build/sickle-stroke-review/screenshots. Run with
 * sickleStrokeReviewClient and drop {"op":"create","name":"SickleReview","seed":1} into operator/inbox after startup.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SickleStrokeClientReview {
    private static final float[] PROGRESS = {0, .08F, .16F, .24F, .32F, .36F, .40F, .44F, .48F, .58F, .70F, .85F};
    private static final String[] VIEWS = {"first", "back", "side", "front"};
    private static final int SETTLE = 4;
    private static int ticks, shot;
    private static ArmorStand camera;
    /** Camera eye, then the point it looks at, as offsets from the player's feet (the player faces +z). */
    private static final double[][] CAMERAS = {{-1.3, 2.0, -2.4, -.3, 1.3, .5}, {-3.0, 1.5, .7, 0, 1.3, .7},
            {-2.2, 1.6, 2.6, -.2, 1.3, .3}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.sickleStrokeReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("sickle-stroke-review")) return;
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.bobView().set(false);
        if (mc.screen != null) mc.setScreen(null);
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) server.execute(() -> prepare(server.getPlayerList().getPlayer(uuid)));
        if (ticks < 80) return;
        if (shot == PROGRESS.length * VIEWS.length) {
            Hemomancy.LOGGER.info("SICKLE_REVIEW complete {}", shot);
            mc.stop();
            return;
        }
        String view = VIEWS[shot / PROGRESS.length];
        float progress = PROGRESS[shot % PROGRESS.length];
        int local = (ticks - 80) % SETTLE;
        if (local == 0) frame(mc, view);
        mc.options.hideGui = !view.equals("first");
        hold(mc, progress);
        if (local == SETTLE - 1) {
            Screenshot.grab(mc.gameDirectory, String.format("sickle-%s-%02d-%03d.png", view, shot % PROGRESS.length,
                    Math.round(progress * 100)), mc.getMainRenderTarget(), message -> {});
            var cam = mc.gameRenderer.getMainCamera();
            Hemomancy.LOGGER.info("SICKLE_REVIEW captured {} {} camera={} yaw={} pitch={}", view, progress,
                    cam.getPosition(), cam.getYRot(), cam.getXRot());
            shot++;
        }
    }

    /** Pins the swing at one progress value; the client tick has already advanced it, so the frame renders this. */
    private static void hold(Minecraft mc, float progress) {
        var player = mc.player;
        player.swingingArm = InteractionHand.MAIN_HAND;
        player.swinging = progress > 0;
        player.swingTime = 1;
        player.attackAnim = progress;
        player.oAttackAnim = progress;
        player.setYRot(0);
        player.setXRot(0);
        player.yRotO = 0;
        player.xRotO = 0;
        player.yBodyRot = player.yBodyRotO = player.yHeadRot = player.yHeadRotO = 0;
    }

    private static void frame(Minecraft mc, String view) {
        switch (view) {
            case "first" -> { mc.setCameraEntity(mc.player); mc.options.setCameraType(CameraType.FIRST_PERSON); }
            default -> {
                double[] at = CAMERAS[java.util.Arrays.asList(VIEWS).indexOf(view) - 1];
                if (camera == null) {
                    camera = EntityType.ARMOR_STAND.create(mc.level);
                    camera.setInvisible(true);
                }
                var feet = mc.player.position();
                double x = feet.x + at[0], y = feet.y + at[1] - camera.getEyeHeight(), z = feet.z + at[2];
                double dx = at[3] - at[0], dy = at[4] - at[1], dz = at[5] - at[2];
                camera.setPos(x, y, z);
                camera.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
                camera.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
                camera.setYHeadRot(camera.getYRot());
                camera.setOldPosAndRot();
                mc.setCameraEntity(camera);
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        }
    }

    private static void prepare(net.minecraft.server.level.ServerPlayer player) {
        var level = player.serverLevel();
        var server = level.getServer();
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
                "gamerule doMobSpawning false"})
            server.getCommands().performPrefixedCommand(source, cmd);
        BlockPos base = new BlockPos(0, 120, 0);
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-8, -1, -8), base.offset(8, 5, 10)))
            level.setBlock(pos, pos.getY() < base.getY() ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        level.getEntitiesOfClass(Entity.class, new AABB(base).inflate(12), e -> e != player).forEach(Entity::discard);
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        player.setMainArm(HumanoidArm.RIGHT);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.living_sickle.get()));
        player.teleportTo(level, .5, base.getY(), .5, 0, 0);
        var target = EntityType.HUSK.create(level);
        target.setPos(.5, base.getY(), 3.1);
        target.setNoAi(true);
        target.setYRot(180);
        target.setYHeadRot(180);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        level.addFreshEntity(target);
        Hemomancy.LOGGER.info("SICKLE_REVIEW prepared");
    }
}
