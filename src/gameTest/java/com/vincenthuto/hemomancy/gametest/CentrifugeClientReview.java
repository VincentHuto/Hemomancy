package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.VialCentrifugeScreen;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Captures the three centrifuge menus in a disposable client, or with hemomancy.centrifugeWorldReview the
 * three stages side by side at rest and mid-spin. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CentrifugeClientReview {
    private static int ticks;
    private static int stage;
    private static boolean opening;
    private static BlockPos reviewPos;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.centrifugeReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("centrifuge-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        if (Boolean.getBoolean("hemomancy.centrifugeWorldReview")) { worldTick(mc); return; }
        mc.options.pauseOnLostFocus = false;
        if (++ticks < 60) return;
        if (!opening) {
            opening = true;
            reviewPos = mc.player.blockPosition().offset(0, 0, 2);
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var level = player.serverLevel();
                level.setBlockAndUpdate(reviewPos, BlockInit.vial_centrifuge.get().defaultBlockState()
                        .setValue(StationTierProperty.STAGE, stage));
            });
            ticks = 0;
            return;
        }
        if (!(mc.screen instanceof VialCentrifugeScreen)) {
            if (mc.level.getBlockEntity(reviewPos) instanceof VialCentrifugeBlockEntity) {
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    player.openMenu((VialCentrifugeBlockEntity) player.serverLevel().getBlockEntity(reviewPos), reviewPos);
                });
                ticks = 0;
            }
            return;
        }
        Screenshot.grab(mc.gameDirectory, "centrifuge-stage-" + stage + ".png", mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("CENTRIFUGE_CLIENT_REVIEW captured stage {}, menu slots {}", stage,
                mc.player.containerMenu.slots.size());
        if (++stage == 3) { mc.stop(); return; }
        mc.player.closeContainer();
        opening = false;
        ticks = 0;
    }

    private static int worldTicks;
    private static int shot;
    private static BlockPos row;
    /** Camera pose per capture: eye offset from the middle centrifuge, then whether the batch is spinning. */
    private static final double[][] SHOTS = {{-1.5, 1.6, -4.2, 0}, {-1.5, 1.6, -4.2, 1}, {-2.2, 1.9, -1.8, 1}, {2.6, 1.7, 2.2, 1}, {0, 1.6, -3.2, 1}};

    private static void worldTick(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = shot < SHOTS.length - 1;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        worldTicks++;
        if (worldTicks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 6);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-6, -1, -6), row.offset(6, 4, 6)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                for (int stage = 0; stage < 3; stage++) {
                    var pos = row.offset((stage - 1) * 2, 0, 0);
                    level.setBlock(pos, BlockInit.vial_centrifuge.get().defaultBlockState()
                            .setValue(StationTierProperty.STAGE, stage)
                            .setValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.VialCentrifugeBlock.FACING,
                                    net.minecraft.core.Direction.NORTH), 3);
                    var machine = (VialCentrifugeBlockEntity) level.getBlockEntity(pos);
                    for (int slot = 2; slot <= 9; slot++) {
                        boolean spent = stage == 0 && (slot == 4 || slot == 8);
                        machine.setItem(slot, spent ? new net.minecraft.world.item.ItemStack(
                                com.vincenthuto.hemomancy.common.init.ItemInit.bloody_vial.get())
                                : CentrifugeUpgradeGameTests.sample("minecraft:cow"));
                    }
                    var blood = machine.getBloodCapability();
                    if (blood != null) blood.fill(500 + stage * 700);
                    machine.sendUpdates();
                }
            });
        }
        if (worldTicks < 80 || row == null) return;
        int local = (worldTicks - 80) % 70;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            boolean spin = at[3] > 0;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                if (spin) for (int stage = 0; stage < 3; stage++)
                    if (level.getBlockEntity(row.offset((stage - 1) * 2, 0, 0)) instanceof VialCentrifugeBlockEntity machine
                            && !machine.isProcessing()) {
                        for (int slot = 2; slot <= 9; slot++) machine.setItem(slot, CentrifugeUpgradeGameTests.sample("minecraft:cow"));
                        Hemomancy.LOGGER.info("CENTRIFUGE_WORLD_REVIEW stage {} startup {}", stage, machine.attemptStartup(null));
                    }
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + .5, fy = row.getY() + .6, fz = row.getZ() + .5;
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(level, x, y - player.getEyeHeight(), z, yaw, pitch);
                if (shot == SHOTS.length - 1) {
                    // Last capture checks the item renderer: each saved stage in the hotbar, stage 2 in hand.
                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                    for (int stage = 0; stage < 3; stage++) {
                        var item = new net.minecraft.world.item.ItemStack(BlockInit.vial_centrifuge.get());
                        item.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                                net.minecraft.world.item.component.BlockItemStateProperties.EMPTY.with(StationTierProperty.STAGE, stage));
                        player.getInventory().setItem(stage, item);
                    }
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 2;
        if (local == 45) {
            Screenshot.grab(mc.gameDirectory, "centrifuge-world-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("CENTRIFUGE_WORLD_REVIEW captured {}", shot);
            shot++;
        }
    }
}
