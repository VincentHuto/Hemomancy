package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Map;

/**
 * With hemomancy.loomReview, stages Somatic Looms in a disposable world: empty; stocked with no weave chosen; live
 * weaves on a base, a Ganglionic and a Cortical loom, each hunted around a stand-in weaver at its front so every
 * weave stays live while the camera moves; a Cortical weave carried to its climax just before its capture; and an
 * east-facing Ganglionic weave. Captures by day and night into build/loom-client-review/screenshots.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LoomClientReview {
    private static final int[] LOOMS = {-18, -12, -6, 0, 6, 12, 18};
    private static final int CLIMAX_SHOT = 8;
    private static int ticks;
    private static int shot;
    private static BlockPos row;
    private static SomaticLoomBlockEntity climaxLoom;
    private static net.minecraft.world.entity.player.Player climaxWeaver;
    /** Eye offset from the row, the point looked at, then 1 for night; the last pose shows the hotbar. */
    private static final double[][] SHOTS = {
            {0, 7.5, -18.0, .5, 1.0, .5, 0},
            {-7.1, 2.0, -1.6, -5.5, 1.25, .8, 0},
            {-1.1, 2.0, -1.6, .5, 1.25, .8, 0}, {3.6, 3.4, -.6, .5, 1.2, .9, 0},
            {4.2, 2.6, -3.6, 6.5, 1.9, .8, 0}, {9.6, 3.6, -.6, 6.5, 1.6, .9, 1},
            {6.5, 4.5, -13.5, 6.5, 1.5, .5, 0}, {12.5, 5.0, -13.0, 12.5, 1.5, .5, 1},
            {10.2, 2.6, -3.8, 12.5, 1.7, .8, 0},
            {22.2, 2.6, -2.2, 18.5, 1.1, .5, 0},
            {3.6, 2.6, -3.8, 6.5, 1.7, .8, 1}, {-8.6, 2.6, -3.4, -5.5, 1.1, .8, 1},
            {0.5, 3.4, -37.5, 0.5, 0.3, -32.0, 0}, {-2.5, 2.0, -36.5, 0.5, 0.2, -32.5, 0},
            {0.5, 2.0, -28.0, 0.5, 0.2, -32.5, 0}, {4.0, 2.4, -36.5, 0.5, 0.2, -32.5, 1},
            {0, 3.0, -6.0, .5, 1.0, .5, 0}};
    /** The seam loom starts weaving partway through this shot, so its capture catches the ground tearing open. */
    private static final int TEAR_SHOT = 12;
    private static SomaticLoomBlockEntity tearLoom;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.loomReview")) return;
        var mc = Minecraft.getInstance();
        if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("loom-client-review")) return;
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        mc.options.hideGui = shot < SHOTS.length - 1;
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (++ticks == 40) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                row = player.blockPosition().offset(0, 0, 10);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
                        "weather clear", "gamerule doMobSpawning false", "kill @e[type=item]",
                        "kill @e[type=hemomancy:frayed_thread]"})
                    server.getCommands().performPrefixedCommand(source, cmd);
                for (BlockPos pos : BlockPos.betweenClosed(row.offset(-24, -1, -40), row.offset(24, 6, 6)))
                    level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                player.teleportTo(level, row.getX() + 0.5, row.getY(), row.getZ() - 3.5, 0, 20);
                stage(level, player);
                Hemomancy.LOGGER.info("LOOM_REVIEW staged {} looms at {}", LOOMS.length, row);
            });
        }
        if (ticks < 240 || row == null) return;
        int local = (ticks - 240) % 60;
        if (local == 0) {
            if (shot == SHOTS.length) { mc.stop(); return; }
            double[] at = SHOTS[shot];
            boolean hotbar = shot == SHOTS.length - 1;
            boolean climax = shot == CLIMAX_SHOT;
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
                server.getCommands().performPrefixedCommand(source, at[6] > 0 ? "time set 18000" : "time set 6000");
                double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
                double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
                float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
                float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
                player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
                if (climax && climaxLoom != null) weaveToClimax(player.serverLevel(), climaxLoom, climaxWeaver);
                if (hotbar) {
                    player.setGameMode(GameType.CREATIVE);
                    player.getInventory().setItem(0, new ItemStack(BlockInit.somatic_loom.get()));
                }
            });
        }
        if (local == 5 && shot == SHOTS.length - 1) mc.player.getInventory().selected = 0;
        if (local == 22 && shot == TEAR_SHOT && tearLoom != null) server.execute(() -> {
            var level = server.getPlayerList().getPlayer(uuid).serverLevel();
            charge(standIn(level, tearLoom), tearLoom, 600);
        });
        if (local == 40) {
            Screenshot.grab(mc.gameDirectory, "loom-" + shot + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("LOOM_REVIEW captured {}", shot);
            shot++;
        }
    }

    private static void stage(ServerLevel level, ServerPlayer player) {
        var tempest = Hemomancy.rloc("memory_weaving/memory_sanguine_tempest");
        var rush = Hemomancy.rloc("memory_weaving/memory_blood_rush");
        var axonal = Hemomancy.rloc("memory_weaving/memory_axonal_transduction");
        place(level, LOOMS[0], Direction.NORTH, 0);

        var stocked = place(level, LOOMS[1], Direction.NORTH, 0);
        stocked.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        enzymes(level, stocked, Map.of("ANIMUS", 8, "FLAMMEUS", 3, "DUCTILIS", 16, "LUX", 1, "MORTEM", 5,
                "CONGEATIO", 30, "FERRIC", 64, "TENEBRIS", 12));
        HemoCapabilityAccess.getBloodVolume(stocked).ifPresent(v -> v.setBloodVolume(v.getMaxBloodVolume() * .6D));
        stocked.sendUpdates();

        var base = weave(level, LOOMS[2], Direction.NORTH, 0, ItemInit.blood_crystal_shard.get(), rush,
                Map.of("ANIMUS", 6, "FLAMMEUS", 4, "DUCTILIS", 4, "MORTEM", 4, "CONGEATIO", 4, "FERRIC", 4, "TENEBRIS", 4));
        charge(standIn(level, base), base, 350);

        var ganglionic = weave(level, LOOMS[3], Direction.NORTH, 1, ItemInit.spore_sac.get(), tempest,
                Map.of("ANIMUS", 8, "DUCTILIS", 4, "CONGEATIO", 4));
        charge(standIn(level, ganglionic), ganglionic, 400);

        var cortical = weave(level, LOOMS[4], Direction.NORTH, 2, ItemInit.naeglerophaeon_ganglion.get(), axonal,
                Map.of("DUCTILIS", 8));
        charge(standIn(level, cortical), cortical, 600);

        climaxLoom = weave(level, LOOMS[5], Direction.NORTH, 2, ItemInit.naeglerophaeon_ganglion.get(), axonal,
                Map.of("DUCTILIS", 8));
        climaxWeaver = qliphothWeaver(level, climaxLoom);
        charge(climaxWeaver, climaxLoom, 600);

        tearLoom = weaveAt(level, 0, -20, Direction.NORTH, 2, ItemInit.naeglerophaeon_ganglion.get(), axonal,
                Map.of("DUCTILIS", 8));

        var east = weave(level, LOOMS[6], Direction.EAST, 1, ItemInit.spore_sac.get(), tempest,
                Map.of("ANIMUS", 8, "DUCTILIS", 4, "CONGEATIO", 4));
        charge(standIn(level, east), east, 400);
        Hemomancy.LOGGER.info("LOOM_REVIEW base={} ganglionic={} cortical={} east={}", base.isWeaving(),
                ganglionic.isWeaving(), cortical.isWeaving(), east.isWeaving());
    }

    private static net.neoforged.neoforge.common.util.FakePlayer qliphothWeaver(ServerLevel level, SomaticLoomBlockEntity te) {
        var weaver = standIn(level, te);
        com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.get(level.getServer()).setSkyThemeOverride(weaver,
                com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION);
        return weaver;
    }

    /** An invulnerable weaver at the loom's front, so its weave stays live wherever the camera is. */
    private static net.neoforged.neoforge.common.util.FakePlayer standIn(ServerLevel level, SomaticLoomBlockEntity te) {
        var weaver = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "loom_review_weaver"));
        Direction facing = te.getBlockState().getValue(SomaticLoomBlock.FACING);
        var front = te.echoAnchor().add(facing.getStepX() * 2.5D, 0, facing.getStepZ() * 2.5D);
        weaver.moveTo(front.x, te.getBlockPos().getY(), front.z);
        return weaver;
    }

    private static SomaticLoomBlockEntity place(ServerLevel level, int x, Direction facing, int stage) {
        var loom = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        var pos = row.offset(x, 0, 0);
        var state = loom.defaultBlockState().setValue(SomaticLoomBlock.FACING, facing).setValue(SomaticLoomBlock.STAGE, stage);
        level.setBlock(pos, state, 3);
        loom.placeFillers(level, pos, state);
        return (SomaticLoomBlockEntity) level.getBlockEntity(pos);
    }

    /** A loom set back from the row, so a 13-block dome's seam lies on cleared floor all round. */
    private static SomaticLoomBlockEntity weaveAt(ServerLevel level, int x, int z, Direction facing, int stage, Item catalyst,
            ResourceLocation recipe, Map<String, Integer> stock) {
        BlockPos saved = row;
        row = row.offset(0, 0, z);
        try {
            return weave(level, x, facing, stage, catalyst, recipe, stock);
        } finally {
            row = saved;
        }
    }

    private static SomaticLoomBlockEntity weave(ServerLevel level, int x, Direction facing, int stage, Item catalyst,
            ResourceLocation recipe, Map<String, Integer> stock) {
        var te = place(level, x, facing, stage);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(catalyst), null);
        enzymes(level, te, stock);
        te.selectRecipe(null, recipe);
        return te;
    }

    private static void enzymes(ServerLevel level, SomaticLoomBlockEntity te, Map<String, Integer> stock) {
        CompoundTag tag = te.saveWithoutMetadata(level.registryAccess());
        CompoundTag stored = new CompoundTag();
        stock.forEach(stored::putInt);
        tag.put("storedEnzymes", stored);
        te.loadWithComponents(tag, level.registryAccess());
        te.sendUpdates();
    }

    private static void charge(net.minecraft.world.entity.player.Player player, SomaticLoomBlockEntity te, double amount) {
        HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> {
            v.setActive(true);
            v.setBloodVolume(v.getMaxBloodVolume());
        });
        te.tryChargeRitualBlood(player, amount, false);
    }

    /** Pays the weave, then frays, spends and hooks every thread until the echo reaches its climax. */
    private static void weaveToClimax(ServerLevel level, SomaticLoomBlockEntity te, net.minecraft.world.entity.player.Player weaver) {
        if (!te.isWeaving()) charge(weaver, te, 600);
        for (int tick = 0; tick < 4000 && te.isWeaving() && !te.isInClimax(); tick++) {
            SomaticLoomBlockEntity.serverTick(level, te.getBlockPos(), level.getBlockState(te.getBlockPos()), te);
            for (var thread : te.looseThreadEntities(level)) {
                thread.addStrain(1f);
                thread.tryHook(weaver);
            }
        }
        Hemomancy.LOGGER.info("LOOM_REVIEW climax={}", te.isInClimax());
    }
}
