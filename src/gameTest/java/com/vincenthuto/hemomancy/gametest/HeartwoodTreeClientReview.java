package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthHandler;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodCoreRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodTreeRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartwoodCoreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * With hemomancy.heartwoodTreeReview (run heartwoodTreeReviewClient, then drop {"op":"create","name":"HeartwoodTreeReview"}
 * into the operator inbox), grows two Heartwood trees and a Blood Wood tree by real projection on a dirt
 * platform in the sky, lays out both wood sets piece by piece, and captures the trees by day and night, the core
 * at each of its five stages, and every piece, to build/heartwood-tree-review/screenshots. Logs HEARTWOOD_REVIEW
 * PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HeartwoodTreeClientReview {
	private static final int FLOOR = 150;
	private static final BlockPos TREE = new BlockPos(0, FLOOR + 1, 0);
	private static final BlockPos SECOND_TREE = new BlockPos(34, FLOOR + 1, 7);
	private static final BlockPos BLOOD_WOOD_TREE = new BlockPos(-22, FLOOR + 1, 4);
	private static final BlockPos SECOND_BLOOD_WOOD_TREE = new BlockPos(-44, FLOOR + 1, 9);
	private static final BlockPos THIRD_BLOOD_WOOD_TREE = new BlockPos(-30, FLOOR + 1, 28);
	private static final BlockPos CORE = TREE.above(HeartwoodTreeRules.CORE_HEIGHT);
	private static final int STAGE = 40;
	private static final int BUILD = STAGE + 60;
	private static final int FIRST_SHOT = 220;
	private static final int GAP = 50;
	private static final int ROW_Z = -34;
	/** Each shot: name, camera x, y, z, yaw, pitch, night, core stage to feed up to (-1 leaves it). */
	private static final Shot[] SHOTS = {
			new Shot("01-tree-day", 0.5, FLOOR + 12.0, 34.5, 180f, -2f, false, -1),
			new Shot("02-tree-night", 0.5, FLOOR + 12.0, 34.5, 180f, -2f, true, -1),
			new Shot("03-tree-below", 7.5, FLOOR + 2.0, 13.5, 165f, -48f, false, -1),
			new Shot("04-tree-above", 0.5, FLOOR + 48.0, 0.5, 0f, 89f, false, -1),
			new Shot("05-second-tree", 34.5, FLOOR + 14.0, 40.5, 180f, -4f, false, -1),
			new Shot("05b-blood-wood-grove", -33.5, FLOOR + 9.0, 55.5, 180f, 4f, false, -1),
			new Shot("05c-blood-wood-close", -22.5, FLOOR + 7.0, 22.5, 180f, -4f, false, -1),
			new Shot("05d-blood-wood-under", -19.5, FLOOR + 2.0, 9.5, 150f, -32f, false, -1),
			new Shot("05e-blood-wood-above", -33.5, FLOOR + 38.0, 15.5, 0f, 89f, false, -1),
			new Shot("06-trees-side-by-side", -30.5, FLOOR + 10.0, 40.5, 145f, -2f, false, -1),
			new Shot("07-core-stage-0", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, true, 0),
			new Shot("08-core-stage-1", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, true, 1),
			new Shot("09-core-stage-2", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, true, 2),
			new Shot("10-core-stage-3", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, true, 3),
			new Shot("11-core-stage-4-night", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, true, 4),
			new Shot("12-core-stage-4-day", 0.5, CORE.getY() + 0.4, CORE.getZ() + 4.5, 180f, 0f, false, 4),
			new Shot("13-tree-full-night", 0.5, FLOOR + 12.0, 34.5, 180f, -2f, true, 4),
			new Shot("14-blood-wood-set-a", -10.5, FLOOR + 3.5, ROW_Z - 8.5, 0f, 12f, false, -1),
			new Shot("15-blood-wood-set-b", 4.5, FLOOR + 3.5, ROW_Z - 8.5, 0f, 12f, false, -1),
			new Shot("16-blood-wood-set-c", 15.5, FLOOR + 3.5, ROW_Z - 8.5, 0f, 12f, false, -1),
			new Shot("17-heartwood-set-a", -10.5, FLOOR + 3.5, ROW_Z + 8 - 5.5, 0f, 16f, false, -1),
			new Shot("18-heartwood-set-b", 4.5, FLOOR + 3.5, ROW_Z + 8 - 5.5, 0f, 16f, false, -1),
			new Shot("19-heartwood-set-c", 15.5, FLOOR + 3.5, ROW_Z + 8 - 5.5, 0f, 16f, false, -1),
			new Shot("20-heartwood-set-night", 4.5, FLOOR + 3.5, ROW_Z + 8 - 5.5, 0f, 16f, true, -1),
	};
	private static final int DONE = FIRST_SHOT + SHOTS.length * GAP + 20;
	private static int ticks;

	private HeartwoodTreeClientReview() {}

	private record Shot(String name, double x, double y, double z, float yaw, float pitch, boolean night, int coreStage) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.heartwoodTreeReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("heartwood-tree-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (ticks == STAGE) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		if (ticks == BUILD) server.execute(() -> build(server.getPlayerList().getPlayer(uuid)));
		for (int i = 0; i < SHOTS.length; i++) {
			Shot shot = SHOTS[i];
			int at = FIRST_SHOT + i * GAP;
			if (ticks == at - 35) server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), shot));
			if (ticks == at) capture(mc, "heartwood-" + shot.name() + ".png");
		}
		if (ticks == DONE) {
			Hemomancy.LOGGER.info("HEARTWOOD_REVIEW captured {}", SHOTS.length);
			mc.stop();
		}
	}

	private static void stage(ServerPlayer player) {
		if (player == null) return;
		run(player, "gamemode creative", "difficulty peaceful", "gamerule doMobSpawning false", "gamerule doDaylightCycle false",
				"weather clear", "time set noon", "forceload add -64 -64 64 64");
		player.teleportTo(player.serverLevel(), 0.5, FLOOR + 10.0, 30.5, 180f, 0f);
	}

	private static void build(ServerPlayer player) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		run(player, "fill -60 " + FLOOR + " -60 60 " + FLOOR + " 60 minecraft:coarse_dirt");
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(10000);
		volume.fill(10000);
		level.setBlockAndUpdate(TREE, Blocks.DEAD_BUSH.defaultBlockState());
		level.setBlockAndUpdate(SECOND_TREE, Blocks.DEAD_BUSH.defaultBlockState());
		level.setBlockAndUpdate(BLOOD_WOOD_TREE.below(), Blocks.DIRT.defaultBlockState());
		level.setBlockAndUpdate(BLOOD_WOOD_TREE, Blocks.CHERRY_SAPLING.defaultBlockState());
		level.setBlockAndUpdate(SECOND_BLOOD_WOOD_TREE.below(), Blocks.DIRT.defaultBlockState());
		level.setBlockAndUpdate(SECOND_BLOOD_WOOD_TREE, Blocks.OAK_SAPLING.defaultBlockState());
		level.setBlockAndUpdate(THIRD_BLOOD_WOOD_TREE.below(), Blocks.DIRT.defaultBlockState());
		level.setBlockAndUpdate(THIRD_BLOOD_WOOD_TREE, Blocks.SPRUCE_SAPLING.defaultBlockState());
		BloodwoodGrowthHandler.tryGrowFromProjection(level, TREE, player, HeartwoodTreeRules.PROJECTION_BLOOD_COST);
		BloodwoodGrowthHandler.tryGrowFromProjection(level, SECOND_TREE, player, HeartwoodTreeRules.PROJECTION_BLOOD_COST);
		BloodwoodGrowthHandler.tryGrowFromProjection(level, BLOOD_WOOD_TREE, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
		BloodwoodGrowthHandler.tryGrowFromProjection(level, SECOND_BLOOD_WOOD_TREE, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
		BloodwoodGrowthHandler.tryGrowFromProjection(level, THIRD_BLOOD_WOOD_TREE, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
		report(level.getBlockEntity(CORE) instanceof HeartwoodCoreBlockEntity, "the dead bush grew a tree with a core");
		report(level.getBlockState(BLOOD_WOOD_TREE).getBlock() != Blocks.CHERRY_SAPLING, "the cherry sapling grew Blood Wood");
		showcase(player, "blood_wood", ROW_Z);
		showcase(player, "heartwood", ROW_Z + 8);
		run(player, "gamemode spectator");
	}

	/** Every piece of one set in a row along x, with a backing wall of its planks. */
	private static void showcase(ServerPlayer player, String p, int z) {
		String id = "hemomancy:" + p;
		String stripped = "hemomancy:stripped_" + p;
		int y = FLOOR + 1;
		List<String> commands = new ArrayList<>();
		commands.add("fill -20 " + y + " " + (z + 2) + " 30 " + (y + 6) + " " + (z + 2) + " " + id + "_planks");
		String[] columns = { id + "_log", id + "_bark", stripped + "_log", stripped + "_bark", id + "_planks" };
		int x = -18;
		for (String column : columns) {
			commands.add("fill " + x + " " + y + " " + z + " " + x + " " + (y + 2) + " " + z + " " + column);
			x += 2;
		}
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_slab");
		commands.add("setblock " + x + " " + (y + 1) + " " + z + " " + id + "_slab[type=top]");
		x += 2;
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_stairs[facing=south]");
		commands.add("setblock " + (x + 1) + " " + y + " " + z + " " + id + "_stairs[facing=north]");
		x += 3;
		commands.add("fill " + x + " " + y + " " + z + " " + (x + 2) + " " + y + " " + z + " " + id + "_fence[east=true,west=true]");
		x += 4;
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_fence_gate[facing=north]");
		commands.add("setblock " + (x + 1) + " " + y + " " + z + " " + id + "_fence_gate[facing=north,open=true]");
		x += 3;
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_door[facing=south,half=lower]");
		commands.add("setblock " + x + " " + (y + 1) + " " + z + " " + id + "_door[facing=south,half=upper]");
		x += 2;
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_door[facing=south,half=lower,open=true]");
		commands.add("setblock " + x + " " + (y + 1) + " " + z + " " + id + "_door[facing=south,half=upper,open=true]");
		x += 2;
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_trapdoor[half=top]");
		commands.add("setblock " + (x + 1) + " " + y + " " + z + " " + id + "_trapdoor[facing=north,open=true]");
		x += 3;
		commands.add("setblock " + x + " " + y + " " + (z + 1) + " " + id + "_button[face=wall,facing=north]");
		commands.add("setblock " + (x + 1) + " " + y + " " + z + " " + id + "_pressure_plate");
		x += 3;
		String words = "{front_text:{messages:['\"Bloodwood\"','\"sign\"','\"\"','\"\"']}}";
		commands.add("setblock " + x + " " + y + " " + z + " " + id + "_sign[rotation=8]" + words);
		commands.add("setblock " + (x + 1) + " " + (y + 1) + " " + (z + 1) + " " + id + "_wall_sign[facing=north]" + words);
		x += 3;
		commands.add("setblock " + x + " " + (y + 3) + " " + z + " " + id + "_planks");
		commands.add("setblock " + x + " " + (y + 2) + " " + z + " " + id + "_hanging_sign[rotation=8,attached=true]" + words);
		commands.add("setblock " + (x + 2) + " " + (y + 1) + " " + (z + 1) + " " + id + "_wall_hanging_sign[facing=north]" + words);
		String[] framed = { "_door", "_sign", "_hanging_sign", "_log", "_trapdoor", "_fence" };
		for (int i = 0; i < framed.length; i++) {
			commands.add("summon minecraft:item_frame " + (-18 + i * 2) + " " + (y + 5) + " " + (z + 1)
					+ " {Facing:2b,Fixed:1b,Item:{id:\"" + id + framed[i] + "\",count:1}}");
		}
		run(player, commands.toArray(String[]::new));
	}

	private static void run(ServerPlayer player, String... commands) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : commands) player.server.getCommands().performPrefixedCommand(source, command);
	}

	private static void frame(ServerPlayer player, Shot shot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		run(player, shot.night() ? "time set midnight" : "time set noon");
		player.teleportTo(level, shot.x(), shot.y(), shot.z(), shot.yaw(), shot.pitch());
		if (shot.coreStage() >= 0 && level.getBlockEntity(CORE) instanceof HeartwoodCoreBlockEntity core) {
			double wanted = HeartwoodCoreRules.FULL * shot.coreStage() / HeartwoodCoreRules.RAVENOUS_STAGE;
			if (wanted > core.fed()) core.feed(level, wanted - core.fed());
			report(HeartwoodCoreRules.stage(core.fed()) == shot.coreStage(), "the core is at stage " + shot.coreStage());
		}
	}

	private static void capture(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("HEARTWOOD_REVIEW captured {}", name);
	}

	private static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("HEARTWOOD_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
