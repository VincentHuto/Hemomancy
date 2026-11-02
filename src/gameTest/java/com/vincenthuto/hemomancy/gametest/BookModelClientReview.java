package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.common.block.entity.DictationTableBlockEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * With hemomancy.bookModelReview, shows the five book models on a Dictation Table, a Harbinger Escritoire and the
 * Enzymatic Scriptorium, in item frames and on the ground, then held shut and open in first and third person, then
 * in the inventory, into build/book-model-review/screenshots. Run with bookModelReviewClient. Excluded from release
 * jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BookModelClientReview {
    private static final List<Supplier<Item>> BOOKS = List.of(ItemInit.liber_sanguinum::get,
            ItemInit.liber_immaculatus::get, ItemInit.book_of_observances::get, ItemInit.harbinger_assignment_ledger::get,
            ItemInit.mnemonic_folio::get);
    /** Eye offset from the row, then the point looked at. */
    private static final double[][] VIEWS = {
            {0, 2.6, -4.5, 0, 1, 1.5}, {-3, 2.4, -1.4, -3, 1.2, .5}, {0, 2.4, -1.4, 0, 1.2, .5},
            {3, 2.6, -1.6, 3, 1.1, .5}, {0, 2.5, .6, 0, 2.5, 2.9}, {0, 1.0, -2.6, 0, .3, -1.1},
            {0, 1.7, -1.2, 0, 1.05, .2}, {5.9, 2.3, -4, 4, 1.1, -4}};
    private static final int SCENE = 60, HELD = 130;
    private static int ticks;
    private static int step;
    private static BlockPos row;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.bookModelReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("book-model-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        var server = mc.getSingleplayerServer();
        if (++ticks == 40) server.execute(() -> build(server, mc.player.getUUID()));
        if (ticks < 80 || row == null) return;
        int t = ticks - 80;
        int views = VIEWS.length, held = BOOKS.size() * 2;
        if (t < views * SCENE) {
            int view = t / SCENE, local = t % SCENE;
            mc.options.hideGui = true;
            if (local == 0) look(server, mc, VIEWS[view], GameType.SPECTATOR);
            if (local == 40) grab(mc, "scene-" + view);
            return;
        }
        t -= views * SCENE;
        if (t < held * HELD) {
            int index = t / HELD, local = t % HELD;
            int book = index % BOOKS.size();
            boolean thirdPerson = index >= BOOKS.size();
            mc.options.hideGui = thirdPerson;
            if (local == 0) {
                // Look at a bare wall so the book reads against it; empty the hand first so the next book opens fresh.
                mc.options.setCameraType(thirdPerson ? CameraType.THIRD_PERSON_FRONT : CameraType.FIRST_PERSON);
                look(server, mc, new double[]{0, 1.6, -2.5, 0, 1.6, -8}, GameType.CREATIVE);
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                    for (int i = 0; i < BOOKS.size(); i++) player.getInventory().setItem(i, new ItemStack(BOOKS.get(i).get()));
                    player.getInventory().setItem(8, ItemStack.EMPTY);
                });
                mc.player.getInventory().selected = 8;
            }
            if (local == 20) mc.player.getInventory().selected = book;
            if (local == 60 && !thirdPerson) grab(mc, "held-" + book + "-opening");
            if (local == 120) grab(mc, (thirdPerson ? "third-" : "held-") + book + "-open");
            return;
        }
        t -= held * HELD;
        if (t == 0) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.hideGui = false;
            mc.setScreen(new InventoryScreen(mc.player));
        }
        if (t == 30) grab(mc, "inventory");
        if (t == 40) mc.stop();
    }

    private static void build(MinecraftServer server, java.util.UUID uuid) {
        var player = server.getPlayerList().getPlayer(uuid);
        var level = player.serverLevel();
        row = player.blockPosition().offset(0, 0, 6);
        var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                "weather clear", "gamerule doMobSpawning false"})
            server.getCommands().performPrefixedCommand(source, cmd);
        for (BlockPos pos : BlockPos.betweenClosed(row.offset(-6, -1, -14), row.offset(6, 5, 4)))
            level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        // A wall behind the row carrying a frame per book, and a bare wall behind the player for the held shots.
        for (BlockPos pos : BlockPos.betweenClosed(row.offset(-5, 0, 3), row.offset(5, 4, 3)))
            level.setBlock(pos, BlockInit.polished_venous_stone_bricks.get().defaultBlockState(), 2);
        for (BlockPos pos : BlockPos.betweenClosed(row.offset(-5, 0, -9), row.offset(5, 4, -9)))
            level.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
        level.setBlock(row.offset(-3, 0, 0), BlockInit.dictation_table.get().defaultBlockState(), 3);
        if (level.getBlockEntity(row.offset(-3, 0, 0)) instanceof DictationTableBlockEntity table)
            table.setBook(new ItemStack(ItemInit.liber_sanguinum.get()));
        level.setBlock(row, BlockInit.harbinger_escritoire.get().defaultBlockState(), 3);
        if (level.getBlockEntity(row) instanceof DictationTableBlockEntity table)
            table.setBook(new ItemStack(ItemInit.liber_immaculatus.get()));
        // A second Escritoire facing east, for the desk pose under rotation.
        var east = row.offset(4, 0, -4);
        level.setBlock(east, BlockInit.harbinger_escritoire.get().defaultBlockState()
                .setValue(com.vincenthuto.hutoslib.common.block.DictationTableBlock.FACING, Direction.EAST), 3);
        if (level.getBlockEntity(east) instanceof DictationTableBlockEntity table)
            table.setBook(new ItemStack(ItemInit.liber_sanguinum.get()));
        level.setBlock(row.offset(3, 0, 0), BlockInit.enzymatic_scriptorium.get().defaultBlockState()
                .setValue(EnzymaticScriptoriumBlock.FACING, Direction.NORTH), 3);
        for (int i = 0; i < BOOKS.size(); i++) {
            var frame = new ItemFrame(level, row.offset(-3 + 2 * i, 2, 2), Direction.NORTH);
            frame.setItem(new ItemStack(BOOKS.get(i).get()));
            level.addFreshEntity(frame);
            var dropped = new ItemEntity(level, row.getX() - 0.4 + 0.6 * i, row.getY() + 0.1, row.getZ() - 0.6,
                    new ItemStack(BOOKS.get(i).get()), 0, 0, 0);
            dropped.setNeverPickUp();
            dropped.setUnlimitedLifetime();
            level.addFreshEntity(dropped);
        }
        Hemomancy.LOGGER.info("BOOK_MODEL_REVIEW placed at {}", row);
    }

    private static void look(MinecraftServer server, Minecraft mc, double[] at, GameType mode) {
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(uuid);
            player.setGameMode(mode);
            double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
            double fx = row.getX() + .5 + at[3], fy = row.getY() + at[4], fz = row.getZ() + .5 + at[5];
            float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
            float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
            player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
        });
    }

    private static void grab(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "book-" + name + ".png", mc.getMainRenderTarget(), message -> {});
        Hemomancy.LOGGER.info("BOOK_MODEL_REVIEW captured {}", name);
    }
}
