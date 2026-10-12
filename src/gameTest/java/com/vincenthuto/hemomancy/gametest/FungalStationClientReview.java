package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MorphlingIncubatorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialCrucibleBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialLanternBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialCrucibleBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.fungalStationReview, places a stocked Morphling Incubator, two Mycelial Crucibles and two
 * Mycelial Lanterns (north- and east-facing) in a disposable world and captures them from fixed poses into
 * build/fungal-station-client-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class FungalStationClientReview {
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    /** Eye offset from the north crucible, then the point looked at; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {.5, 2.6, -6.5, .5, 1.0, .5}, {-3.5, 1.5, -2.4, -3.5, .8, .5}, {-5.3, 2.7, -1.4, -3.5, .8, .5},
            {.5, 1.9, -3.7, .5, 1.0, .5}, {-1.2, 3.3, -2.4, .5, .7, .5}, {9.8, 2.2, .5, 5.5, 1.0, .5},
            {.5, 2.3, -1.0, .5, .62, .5}, {-3.5, 2.0, -1.3, -3.5, .7, .5},
            {-7.0, 1.4, -2.0, -6.5, .75, .5}, {-8.6, 2.1, -1.2, -6.5, .8, .5}, {-4.3, 1.3, 3.5, -6.5, .5, 3.5},
            {3.0, 1.6, -5.0, .5, 1.0, .5}};

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.fungalStationReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("fungal-station-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = shot < SHOTS.length - 1;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 6);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-8, -1, -8), row.offset(9, 4, 6)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);

                var incubatorPos = row.offset(-4, 0, 0);
                var incubator = (MorphlingIncubatorBlock) BlockInit.morphling_incubator.get();
                var incubatorState = incubator.defaultBlockState().setValue(MorphlingIncubatorBlock.FACING, Direction.NORTH);
                level.setBlock(incubatorPos, incubatorState, 3);
                incubator.placeFillers(level, incubatorPos, incubatorState);
                var brood = (MorphlingIncubatorBlockEntity) level.getBlockEntity(incubatorPos);
                brood.setItem(MorphlingIncubatorBlockEntity.SLOT_CENTER, new ItemStack(ItemInit.morphling_polyp.get()));
                var enzymes = new ItemStack[]{new ItemStack(ItemInit.vivacious_enzyme.get()), new ItemStack(ItemInit.ruinous_enzyme.get()),
                        new ItemStack(ItemInit.neurotic_enzyme.get()), new ItemStack(ItemInit.ferric_enzyme.get())};
                for (int i = 0; i < enzymes.length; i++)
                    brood.setItem(MorphlingIncubatorBlockEntity.SLOT_CATALYST_START + i, enzymes[i].copy());
                if (brood.getBloodCapability() != null) brood.getBloodCapability().fill((float) (brood.getMaxBloodVolume() * .65));
                brood.sendUpdates();

                var crucible = (MycelialCrucibleBlock) BlockInit.mycelial_crucible.get();
                for (var placement : new Object[][]{{row, Direction.NORTH}, {row.offset(5, 0, 0), Direction.EAST}}) {
                    var pos = (BlockPos) placement[0];
                    var state = crucible.defaultBlockState().setValue(MycelialCrucibleBlock.FACING, (Direction) placement[1]);
                    level.setBlock(pos, state, 3);
                    crucible.placeFillers(level, pos, state);
                    var vat = (MycelialCrucibleBlockEntity) level.getBlockEntity(pos);
                    vat.setItem(MycelialCrucibleBlockEntity.SLOT_CENTER, new ItemStack(ItemInit.immature_fungal_scar.get()));
                    if (vat.getBloodCapability() != null) vat.getBloodCapability().fill((float) (vat.getMaxBloodVolume() * .7));
                    vat.sendUpdates();
                }
                var lantern = (MycelialLanternBlock) BlockInit.mycelial_lantern.get();
                for (var placement : new Object[][]{{row.offset(-7, 0, 0), Direction.NORTH}, {row.offset(-7, 0, 3), Direction.EAST}}) {
                    var pos = (BlockPos) placement[0];
                    var state = lantern.defaultBlockState().setValue(MycelialLanternBlock.FACING, (Direction) placement[1]);
                    level.setBlock(pos, state, 3);
                    lantern.placeFillers(level, pos, state);
                    var cloche = (MycelialLanternBlockEntity) level.getBlockEntity(pos);
                    cloche.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(ItemInit.vivacious_spores.get()));
                    if (cloche.getBloodCapability() != null) cloche.getBloodCapability().fill(cloche.getMaxBloodVolume() * .6);
                    cloche.sendUpdates();
                }
                Hemomancy.LOGGER.info("FUNGAL_STATION_REVIEW placed incubator blood {}/{}, crucible blood {}/{}",
                        brood.getBloodVolume(), brood.getMaxBloodVolume(),
                        ((MycelialCrucibleBlockEntity) level.getBlockEntity(row)).getBloodVolume(),
                        ((MycelialCrucibleBlockEntity) level.getBlockEntity(row)).getMaxBloodVolume());
            });
        }
        if (ticks < 80 || row == null) return;
        int local = (ticks - 80) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            boolean hotbar = shot == SHOTS.length - 1;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
                if (hotbar) {
                    player.setGameMode(GameType.CREATIVE);
                    player.getInventory().setItem(0, new ItemStack(BlockInit.morphling_incubator.get()));
                    player.getInventory().setItem(1, new ItemStack(BlockInit.mycelial_crucible.get()));
                    player.getInventory().setItem(2, new ItemStack(BlockInit.mycelial_lantern.get()));
                    player.getInventory().setItem(3, new ItemStack(BlockInit.puppeteers_spindle.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 1;
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "fungal-station-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("FUNGAL_STATION_REVIEW captured {}", shot);
            shot++;
        }
    }
}
