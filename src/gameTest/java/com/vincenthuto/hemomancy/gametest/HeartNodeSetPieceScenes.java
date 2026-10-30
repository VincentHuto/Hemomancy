package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.HeartNodeBeat;
import com.vincenthuto.hemomancy.client.render.world.HeartNodeMomentRenderer;
import com.vincenthuto.hemomancy.client.render.world.HeartNodeSpreadGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HeartNodeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Track 5's scenes in the heart-node review: the set pieces. Each maturity stage up close (the node's block entity
 * replanted older, so its block state and model follow), the heart at the top of a beat and at rest, the floor synapse
 * at the moment the pulse reaches it, a fruiting-body cache closed and then splitting open frame by frame, a planting
 * frame by frame (on fresh ground to the north, which withers the first node and its synapses off camera), the
 * floor and wall synapses withered, and the new node withering frame by frame as the first spot is planted again.
 * Runs on its own clock, called each client tick by {@link HeartNodeClientReview} once the spread scenes are done.
 */
final class HeartNodeSetPieceScenes {
	/** Ticks this sequence runs for, from its own zero. */
	static final int LENGTH = 720;
	private static final int STAGE_GAP = 40;
	private static final int STAGE_SHOT = 25;
	private static final int BEAT = 160;
	private static final int BEAT_END = 250;
	private static final int FLARE = 260;
	private static final int FLARE_END = 400;
	private static final int CACHE = 410;
	private static final int CACHE_SHOT = 435;
	private static final int CACHE_OPEN = 450;
	private static final int[] CACHE_FRAMES = { 2, 5, 9, 14, 20 };
	private static final int PLANTING = 490;
	private static final int PLANT = 520;
	private static final int[] PLANT_FRAMES = { 2, 6, 10, 14, 20, 28, 36 };
	private static final int WITHERED_FLOOR = 570;
	private static final int WITHERED_WALL = 600;
	private static final int WITHERING = 640;
	private static final int WITHER = 660;
	private static final int[] WITHER_FRAMES = { 2, 5, 9, 14, 20, 28 };
	/** Track 2's floor and wall synapses, from the first node. */
	private static final BlockPos FLOOR_SYNAPSE = new BlockPos(10, 0, 0);
	private static final BlockPos WALL_SYNAPSE = new BlockPos(17, 1, 0);
	/** The fresh ground the second node is planted on, clear of the neighbour's sanctum to the west. */
	private static final BlockPos SECOND = new BlockPos(0, 0, -30);
	private static final BlockPos CACHE_SPOT = new BlockPos(-2, 0, 0);

	private static boolean peakShot;
	private static boolean restShot;
	private static boolean flareShot;

	private HeartNodeSetPieceScenes() {}

	static void tick(Minecraft mc, MinecraftServer server, UUID uuid, BlockPos node, int t) {
		if (node == null) return;
		// The set pieces are shot without the hotbar, the held seed or the tutorial toasts in the way.
		if (t == 0) mc.options.hideGui = true;
		for (int stage = 0; stage <= HeartNodeRules.MATURE_STAGE; stage++) {
			int at = stage * STAGE_GAP;
			int shown = stage;
			if (t == at) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), node, shown));
			if (t == at + STAGE_SHOT) {
				checkStage(mc, node, shown);
				HeartNodeClientReview.capture(mc, "heart-node-19-" + shown + "-stage.png");
			}
		}
		if (t > BEAT && t < BEAT_END) beat(mc, node);
		if (t == BEAT_END) HeartNodeClientReview.report(peakShot && restShot, "the heart captured at a thump and at rest");
		if (t == FLARE) server.execute(() -> frameSynapse(server.getPlayerList().getPlayer(uuid), node.offset(FLOOR_SYNAPSE)));
		if (t > FLARE + 20 && t < FLARE_END) flare(mc, node);
		if (t == FLARE_END) HeartNodeClientReview.report(flareShot, "the floor synapse flares as the pulse reaches it");
		if (t == CACHE) server.execute(() -> cache(server.getPlayerList().getPlayer(uuid), node.offset(CACHE_SPOT)));
		if (t == CACHE_SHOT) HeartNodeClientReview.capture(mc, "heart-node-21-cache-closed.png");
		if (t == CACHE_OPEN) server.execute(() -> openCache(server.getPlayerList().getPlayer(uuid), node.offset(CACHE_SPOT)));
		frames(mc, t, CACHE_OPEN, CACHE_FRAMES, "heart-node-22-cache-open-");
		if (t == CACHE_OPEN + 3) HeartNodeClientReview.report(HeartNodeMomentRenderer.running() > 0,
				"the cache's split plays on the client");
		BlockPos second = node.offset(SECOND);
		if (t == PLANTING) server.execute(() -> prepare(server.getPlayerList().getPlayer(uuid), second));
		if (t == PLANT) server.execute(() -> replant(server.getPlayerList().getPlayer(uuid), second, node));
		frames(mc, t, PLANT, PLANT_FRAMES, "heart-node-23-planting-");
		if (t == PLANT + 4) HeartNodeClientReview.report(HeartNodeMomentRenderer.plantingAge(second, 0.0F) >= 0.0F,
				"the planting plays on the client");
		if (t == WITHERED_FLOOR) server.execute(() -> withered(server.getPlayerList().getPlayer(uuid),
				node.offset(FLOOR_SYNAPSE), Direction.UP));
		if (t == WITHERED_FLOOR + 20) HeartNodeClientReview.capture(mc, "heart-node-24-synapse-floor-withered.png");
		if (t == WITHERED_WALL) server.execute(() -> withered(server.getPlayerList().getPlayer(uuid),
				node.offset(WALL_SYNAPSE), Direction.WEST));
		if (t == WITHERED_WALL + 20) HeartNodeClientReview.capture(mc, "heart-node-25-synapse-wall-withered.png");
		if (t == WITHERING) server.execute(() -> frameNode(server.getPlayerList().getPlayer(uuid), second));
		if (t == WITHERING + 15) HeartNodeClientReview.capture(mc, "heart-node-26-withering-0-before.png");
		if (t == WITHER) server.execute(() -> replant(server.getPlayerList().getPlayer(uuid), node, second));
		frames(mc, t, WITHER, WITHER_FRAMES, "heart-node-26-withering-");
		if (t == WITHER + 4) {
			HeartNodeClientReview.report(HeartNodeMomentRenderer.running() > 0, "the withering ghost plays on the client");
			server.execute(() -> checkGone(server.getPlayerList().getPlayer(uuid), second));
		}
	}

	private static void frames(Minecraft mc, int t, int from, int[] offsets, String prefix) {
		for (int i = 0; i < offsets.length; i++) {
			if (t == from + offsets[i]) HeartNodeClientReview.capture(mc, prefix + (i + 1) + ".png");
		}
	}

	/** The node's block entity replanted to sit at {@code stage}, and the reviewer framed close on it. */
	private static void stage(ServerPlayer player, BlockPos node, int stage) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		if (!(level.getBlockEntity(node) instanceof HeartNodeBlockEntity heart)) return;
		long into = stage == HeartNodeRules.MATURE_STAGE ? HeartNodeRules.MATURE_TICKS
				: (long) stage * HeartNodeRules.MATURE_TICKS / HeartNodeRules.MATURE_STAGE + 20L;
		heart.plant(player.getUUID(), level.getGameTime() - into);
		heart.refreshStage(level);
		frameNode(player, node);
		HeartNodeClientReview.report(level.getBlockState(node).getValue(HeartNodeBlock.STAGE) == stage,
				"the node's block state shows stage " + stage);
	}

	private static void checkStage(Minecraft mc, BlockPos node, int stage) {
		if (mc.level == null) return;
		var state = mc.level.getBlockState(node);
		HeartNodeClientReview.report(state.is(BlockInit.heart_node.get()) && state.getValue(HeartNodeBlock.STAGE) == stage,
				"the client draws stage " + stage + "'s model");
	}

	private static void frameNode(ServerPlayer player, BlockPos node) {
		if (player == null) return;
		// The eye sits 1.62 above the feet: this looks down on the heart from about two blocks off.
		hover(player, node.getX() + 0.5, node.getY() + 0.5, node.getZ() + 3.0, 180f, 33f);
	}

	/** The reviewer stands in the heart lobe, so the heart goes at its quick pace; one shot at a thump, one at rest. */
	private static void beat(Minecraft mc, BlockPos node) {
		if (mc.level == null) return;
		double phase = HeartNodeBeat.phase(AnimationTime.gameTime(mc.level, 0.0D), HeartNodeBeat.Pace.QUICK);
		if (!peakShot && Math.abs(phase - HeartNodeBeat.THUMP_PEAK) < 0.5D) {
			peakShot = true;
			HeartNodeClientReview.capture(mc, "heart-node-20-1-beat-thump.png");
		} else if (!restShot && phase > 15.0D && phase < 17.0D) {
			restShot = true;
			HeartNodeClientReview.capture(mc, "heart-node-20-2-beat-rest.png");
		}
	}

	private static void frameSynapse(ServerPlayer player, BlockPos synapse) {
		if (player == null) return;
		hover(player, synapse.getX() + 0.5, synapse.getY() + 1.2, synapse.getZ() + 2.6, 180f, 30f);
	}

	/** One shot of the floor synapse while the client's flare there is near its peak. */
	private static void flare(Minecraft mc, BlockPos node) {
		if (flareShot || mc.level == null) return;
		var own = HeartNodeClientState.own();
		if (own.isEmpty()) return;
		Double arrive = HeartNodeSpreadGeometry.arrivals(own.get().footprint()).get(node.offset(FLOOR_SYNAPSE));
		if (arrive == null) return;
		if (HeartNodeSpreadGeometry.flare(AnimationTime.gameTime(mc.level, 0.0D), arrive) > 0.85D) {
			flareShot = true;
			HeartNodeClientReview.capture(mc, "heart-node-20-3-synapse-flare.png");
		}
	}

	/** A cache of the reviewer's beside the node, holding a sword, on cleared ground, framed close. */
	private static void cache(ServerPlayer player, BlockPos spot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		// Unwalled: a wall would run through the node two blocks east.
		clearCell(level, spot, 1, 3, false);
		level.setBlockAndUpdate(spot, BlockInit.fruiting_body_cache.get().defaultBlockState());
		if (level.getBlockEntity(spot) instanceof FruitingBodyCacheBlockEntity cache) {
			cache.store(player.getUUID(), List.of(new ItemStack(Items.IRON_SWORD)));
		}
		hover(player, spot.getX() + 0.5, spot.getY() + 0.5, spot.getZ() + 3.0, 180f, 35f);
	}

	private static void openCache(ServerPlayer player, BlockPos spot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		player.getInventory().clearContent();
		var hit = new BlockHitResult(Vec3.atCenterOf(spot), Direction.UP, spot, false);
		level.getBlockState(spot).useWithoutItem(level, player, hit);
		HeartNodeClientReview.report(level.getBlockState(spot).isAir() && player.getInventory().contains(new ItemStack(Items.IRON_SWORD)),
				"opening the cache hands back what it held and leaves no block");
	}

	/** Fresh network ground for the second node, and the reviewer framed on it. */
	private static void prepare(ServerPlayer player, BlockPos spot) {
		if (player == null) return;
		clearCell(player.serverLevel(), spot, 4, 5, true);
		// Close enough to see the roots punch out about three blocks, high enough to see them all.
		hover(player, spot.getX() + 0.5, spot.getY() + 1.2, spot.getZ() + 3.8, 180f, 38f);
	}

	/** Plants a node on the ground under {@code spot} the way a player does; the planter's old node withers. */
	private static void replant(ServerPlayer player, BlockPos spot, BlockPos old) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		BlockPos ground = spot.below();
		trace(level, "before planting at " + spot, spot, old);
		ItemStack seed = new ItemStack(BlockInit.heart_node.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, seed);
		seed.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false)));
		HeartNodeClientReview.report(spot.equals(HeartNodeSavedData.get(level).nodeOf(player.getUUID()))
				&& !level.getBlockState(old).is(BlockInit.heart_node.get()),
				"planting at " + spot + " withers the node at " + old);
		trace(level, "after planting at " + spot, spot, old);
		player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 3,
				() -> trace(level, "3 ticks after planting at " + spot, spot, old)));
	}

	/** Logs the blocks at both sites and every heart-node item near either, for tracing a stray drop. */
	private static void trace(ServerLevel level, String when, BlockPos... sites) {
		for (BlockPos site : sites) {
			StringBuilder line = new StringBuilder("HEART_NODE_REVIEW TRACE " + when + ": " + site + " is "
					+ level.getBlockState(site));
			for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(site).inflate(8),
					found -> found.getItem().is(BlockInit.heart_node.get().asItem()))) {
				line.append(" | item x").append(item.getItem().getCount()).append(" at ").append(item.position())
						.append(" age ").append(item.getAge());
			}
			com.vincenthuto.hemomancy.Hemomancy.LOGGER.info(line.toString());
		}
	}

	/** A withered synapse framed close, after its node was replanted away. */
	private static void withered(ServerPlayer player, BlockPos synapse, Direction face) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		var state = level.getBlockState(synapse);
		HeartNodeClientReview.report(state.is(BlockInit.mycelial_synapse.get()) && state.getValue(MycelialSynapseBlock.WITHERED),
				"the " + face + " synapse went inert with its node");
		if (face == Direction.UP) {
			frameSynapse(player, synapse);
		} else {
			hover(player, synapse.getX() - 2.7, synapse.getY() - 0.4, synapse.getZ() + 0.5, -90f, 5f);
		}
	}

	/** The withered node left no block and no item: the presentation is the client's ghost alone. */
	private static void checkGone(ServerPlayer player, BlockPos spot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		List<ItemEntity> dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(spot).inflate(3),
				item -> item.getItem().is(BlockInit.heart_node.get().asItem()));
		HeartNodeClientReview.report(!level.getBlockState(spot).is(BlockInit.heart_node.get()),
				"the withered node leaves no block");
		HeartNodeClientReview.report(dropped.isEmpty(), "the withered node leaves no item" + (dropped.isEmpty() ? ""
				: dropped.stream().map(item -> " [" + item.getItem().getCount() + " at " + item.blockPosition() + ", age "
						+ item.getAge() + "]").reduce("", String::concat)));
	}

	/**
	 * Network ground under a cleared cell {@code radius} wide and {@code height} tall around {@code spot}; walled two
	 * blocks high when {@code walled}, so a nectar pool beside it can't flow back in.
	 */
	private static void clearCell(ServerLevel level, BlockPos spot, int radius, int height, boolean walled) {
		int wall = radius + 1;
		for (BlockPos pos : BlockPos.betweenClosed(spot.offset(-wall, -1, -wall), spot.offset(wall, 1, wall))) {
			if (walled && Math.max(Math.abs(pos.getX() - spot.getX()), Math.abs(pos.getZ() - spot.getZ())) == wall)
				level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
		}
		for (BlockPos pos : BlockPos.betweenClosed(spot.offset(-radius, -1, -radius), spot.offset(radius, -1, radius)))
			level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
		for (BlockPos pos : BlockPos.betweenClosed(spot.offset(-radius, 0, -radius), spot.offset(radius, height, radius)))
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	private static void hover(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		player.teleportTo(player.serverLevel(), x, y, z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
	}
}
