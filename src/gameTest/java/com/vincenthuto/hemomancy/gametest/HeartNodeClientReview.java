package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.event.HeartNodeEnvelopeSync;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.UUID;

/**
 * With hemomancy.heartNodeReview (run heartNodeReviewClient, then drop {"op":"create","name":"HeartNodeReview"} into
 * the operator inbox), makes the reviewer an Apotheos, carries them into the Gardens, plants a heart-node on network
 * ground the way a player does (so the placement events run) and checks the personal-Fane rules: the planter's
 * envelope packet arrives as OWNER, a stranger is told STRANGER, a stranger standing on the heart gets no benefit, no
 * cache and cannot break inside, and the owner is fed. Captures the node close up and the envelope from above to
 * build/heart-node-review/screenshots. Track 2 raises a synapse chain ({@link SynapseScenes}); Track 3 steps out
 * for the leave line, opens the Heart-node screen and flips each toggle ({@link HeartNodeScreenScenes}); Track 4
 * captures the mycelial spread part grown, mature by day and dark, at both intensity ends, as a stranger and as a
 * synapse's lobe recedes ({@link HeartNodeSpreadScenes}); Track 5 captures each node stage, the beat, a synapse
 * flare, a cache splitting open, and a planting and a withering frame by frame ({@link HeartNodeSetPieceScenes}). Logs
 * HEART_NODE_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HeartNodeClientReview {
	private static final int STAGE = 40;
	private static final int PLANT = 120;
	private static final int CHECK = 220;
	private static final int CLOSE = 260;
	private static final int WIDE = 320;
	/** Track 2: a floor, a wall and a ceiling synapse raised in a chain, then a foreign raise refused. */
	private static final int RAISE = 360;
	private static final int FLOOR_SHOT = 440;
	private static final int WALL_SHOT = 480;
	private static final int CEILING_SHOT = 520;
	private static final int FOREIGN = 560;
	private static final int FOREIGN_SHOT = 580;
	private static final int CHAIN_SHOT = 640;
	/** Track 3: the leave line on stepping out, then the screen opened and each toggle flipped and captured. */
	private static final int STEP_IN = 680;
	/** Long enough inside that a leave line from the earlier framing is past its ten-second gap. */
	private static final int STEP_OUT = STEP_IN + 220;
	private static final int LEAVE_CHECK = STEP_OUT + 20;
	private static final int SCREEN = LEAVE_CHECK + 40;
	private static final int SCREEN_SHOT = SCREEN + 30;
	private static final int FLIPS = SCREEN_SHOT + 30;
	private static final int FLIP_GAP = 30;
	/** A quarter second: the bit must be back from the server by then. */
	private static final int FLIP_CHECK = 5;
	private static final int SAVED = FLIPS + FLIP_GAP * HeartNodeScreenScenes.TOGGLE_NAMES.length + 20;
	/** Track 4: the spread part grown, mature by day and dark, the chain, intensities, a stranger, a withering lobe. */
	private static final int SPREAD = SAVED + 40;
	private static final int GROWING_SHOT = SPREAD + 40;
	private static final int MATURE = SPREAD + 60;
	private static final int DAY_SHOT = SPREAD + 100;
	private static final int DARK = SPREAD + 110;
	private static final int DARK_SHOT = SPREAD + 150;
	private static final int CHAIN_ABOVE = SPREAD + 160;
	private static final int CHAIN_ABOVE_SHOT = SPREAD + 190;
	private static final int FAINT = SPREAD + 200;
	private static final int FAINT_SHOT = SPREAD + 220;
	private static final int THICK = SPREAD + 230;
	private static final int THICK_SHOT = SPREAD + 250;
	private static final int HIDDEN = SPREAD + 260;
	private static final int HIDDEN_SHOT = SPREAD + 300;
	private static final int SHOWN = SPREAD + 310;
	private static final int SHOWN_SHOT = SPREAD + 340;
	private static final int WITHER = SPREAD + 370;
	/** Halfway through the three-second recede. */
	private static final int WITHER_SHOT = WITHER + 30;
	private static final int WITHER_GONE = WITHER + 90;
	/** Track 5's set-piece scenes run on their own clock from here ({@link HeartNodeSetPieceScenes}). */
	private static final int SET_PIECES = WITHER_GONE + 20;
	private static final int DONE = SET_PIECES + HeartNodeSetPieceScenes.LENGTH;
	private static final UUID STRANGER = UUID.nameUUIDFromBytes("hemomancy:heart_node_review_stranger".getBytes());
	private static int ticks;
	private static BlockPos node;

	private HeartNodeClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.heartNodeReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("heart-node-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (ticks == STAGE) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		if (ticks == PLANT) server.execute(() -> plant(server.getPlayerList().getPlayer(uuid)));
		if (ticks == CHECK) {
			checkClient(uuid);
			server.execute(() -> checkServer(server.getPlayerList().getPlayer(uuid)));
		}
		if (ticks == CLOSE - 20) server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), false));
		if (ticks == CLOSE) capture(mc, "heart-node-1-close.png");
		if (ticks == WIDE - 20) server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), true));
		if (ticks == WIDE) capture(mc, "heart-node-2-envelope.png");
		if (ticks == RAISE) server.execute(() -> SynapseScenes.raiseChain(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == RAISE + 30) server.execute(() -> SynapseScenes.raiseChain(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == RAISE + 60) server.execute(() -> SynapseScenes.raiseChain(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == FLOOR_SHOT - 20) server.execute(() -> SynapseScenes.frame(server.getPlayerList().getPlayer(uuid), node, 0));
		if (ticks == FLOOR_SHOT) capture(mc, "heart-node-3-synapse-floor.png");
		if (ticks == WALL_SHOT - 20) server.execute(() -> SynapseScenes.frame(server.getPlayerList().getPlayer(uuid), node, 1));
		if (ticks == WALL_SHOT) capture(mc, "heart-node-4-synapse-wall.png");
		if (ticks == CEILING_SHOT - 20) server.execute(() -> SynapseScenes.frame(server.getPlayerList().getPlayer(uuid), node, 2));
		if (ticks == CEILING_SHOT) capture(mc, "heart-node-5-synapse-ceiling.png");
		if (ticks == FOREIGN) server.execute(() -> SynapseScenes.foreign(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == FOREIGN_SHOT) capture(mc, "heart-node-6-synapse-foreign.png");
		if (ticks == CHAIN_SHOT - 20) server.execute(() -> SynapseScenes.frame(server.getPlayerList().getPlayer(uuid), node, 3));
		if (ticks == CHAIN_SHOT) capture(mc, "heart-node-7-synapse-chain.png");
		if (ticks == STEP_IN) server.execute(() -> HeartNodeScreenScenes.stepInside(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == STEP_OUT) server.execute(() -> HeartNodeScreenScenes.stepOutside(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == LEAVE_CHECK) HeartNodeScreenScenes.checkLeaveLine(mc);
		if (ticks == SCREEN) server.execute(() -> HeartNodeScreenScenes.open(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == SCREEN_SHOT && HeartNodeScreenScenes.checkOpen(mc)) capture(mc, "heart-node-8-screen.png");
		for (int i = 0; i < HeartNodeScreenScenes.TOGGLE_NAMES.length; i++) {
			int at = FLIPS + i * FLIP_GAP;
			if (ticks == at) HeartNodeScreenScenes.flip(mc, i);
			if (ticks == at + FLIP_CHECK) {
				HeartNodeScreenScenes.checkFlip(mc, i);
				capture(mc, "heart-node-9-" + (i + 1) + "-" + HeartNodeScreenScenes.TOGGLE_NAMES[i] + ".png");
			}
		}
		if (ticks == SAVED) {
			HeartNodeScreenScenes.close(mc);
			server.execute(() -> HeartNodeScreenScenes.checkSaved(server.getPlayerList().getPlayer(uuid)));
		}
		if (ticks == SPREAD) server.execute(() -> HeartNodeSpreadScenes.age(server.getPlayerList().getPlayer(uuid), node,
				HeartNodeRules.MATURE_TICKS / 3));
		if (ticks == SPREAD + 20) server.execute(() -> HeartNodeSpreadScenes.frameAbove(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == GROWING_SHOT) {
			HeartNodeSpreadScenes.checkOwn(mc, 2, "growing");
			capture(mc, "heart-node-10-spread-growing.png");
		}
		if (ticks == MATURE) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			HeartNodeSpreadScenes.age(player, node, HeartNodeRules.MATURE_TICKS);
			HeartNodeSpreadScenes.daylight(player, true);
			HeartNodeSpreadScenes.frameGround(player, node);
		});
		if (ticks == DAY_SHOT) capture(mc, "heart-node-11-spread-day.png");
		if (ticks == DARK) server.execute(() -> HeartNodeSpreadScenes.daylight(server.getPlayerList().getPlayer(uuid), false));
		if (ticks == DARK_SHOT) capture(mc, "heart-node-12-spread-dark.png");
		if (ticks == CHAIN_ABOVE) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			HeartNodeSpreadScenes.daylight(player, true);
			HeartNodeSpreadScenes.frameChain(player, node);
		});
		if (ticks == CHAIN_ABOVE_SHOT) capture(mc, "heart-node-13-spread-chain.png");
		if (ticks == FAINT) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			HeartNodeSpreadScenes.intensity(player, node, 0);
			HeartNodeSpreadScenes.frameAbove(player, node);
		});
		if (ticks == FAINT_SHOT) {
			HeartNodeSpreadScenes.checkOwn(mc, 0, "intensity 0");
			capture(mc, "heart-node-14-spread-intensity-0.png");
		}
		if (ticks == THICK) server.execute(() -> HeartNodeSpreadScenes.intensity(server.getPlayerList().getPlayer(uuid), node, 4));
		if (ticks == THICK_SHOT) {
			HeartNodeSpreadScenes.checkOwn(mc, 4, "intensity 4");
			capture(mc, "heart-node-15-spread-intensity-4.png");
		}
		if (ticks == HIDDEN) server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			HeartNodeSpreadScenes.intensity(player, node, 2);
			HeartNodeSpreadScenes.neighbour(player, node, false);
		});
		if (ticks == HIDDEN_SHOT) {
			HeartNodeSpreadScenes.checkStranger(mc, false);
			capture(mc, "heart-node-16-spread-stranger-hidden.png");
		}
		if (ticks == SHOWN) server.execute(() -> HeartNodeSpreadScenes.neighbour(server.getPlayerList().getPlayer(uuid), node, true));
		if (ticks == SHOWN_SHOT) {
			HeartNodeSpreadScenes.checkStranger(mc, true);
			capture(mc, "heart-node-17-spread-stranger-shown.png");
		}
		if (ticks == WITHER - 20) server.execute(() -> HeartNodeSpreadScenes.frameWither(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == WITHER) server.execute(() -> HeartNodeSpreadScenes.wither(server.getPlayerList().getPlayer(uuid), node));
		if (ticks == WITHER_SHOT) {
			HeartNodeSpreadScenes.checkReceding(mc, true);
			capture(mc, "heart-node-18-spread-withering.png");
		}
		if (ticks == WITHER_GONE) HeartNodeSpreadScenes.checkReceding(mc, false);
		if (ticks >= SET_PIECES) HeartNodeSetPieceScenes.tick(mc, server, uuid, node, ticks - SET_PIECES);
		if (ticks == DONE) mc.stop();
	}

	private static void stage(ServerPlayer player) {
		MinecraftServer server = player.server;
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setDegreeNumber(8);
		degree.setArchonPath(EnumArchonPath.APOTHEOS);
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] {"gamemode creative", "difficulty peaceful", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		ServerLevel gardens = server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		if (gardens == null) {
			Hemomancy.LOGGER.info("HEART_NODE_REVIEW FAIL the Gardens level is missing");
			return;
		}
		player.teleportTo(gardens, 0.5, 120, 0.5, 0f, 30f);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0, false, false));
		Hemomancy.LOGGER.info("HEART_NODE_REVIEW staged in {}", gardens.dimension().location());
	}

	/** Finds open network ground near the reviewer (laying a patch if there is none) and plants the node on it. */
	private static void plant(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		BlockPos ground = findGround(level, player.blockPosition());
		if (ground == null) {
			ground = player.blockPosition().below(2);
			for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-3, 0, -3), ground.offset(3, 0, 3)))
				level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
			for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-3, 1, -3), ground.offset(3, 4, 3)))
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			Hemomancy.LOGGER.info("HEART_NODE_REVIEW laid a network patch at {}", ground);
		}
		player.teleportTo(level, ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 3.5, 180f, 20f);
		ItemStack seed = new ItemStack(BlockInit.heart_node.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, seed);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false);
		seed.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		node = ground.above().immutable();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		report(node.equals(data.nodeOf(player.getUUID())) && level.getBlockState(node).is(BlockInit.heart_node.get()),
				"the node is planted and recorded at " + node);
		report(node.above().equals(player.getRespawnPosition()), "the respawn is bound to the node");
	}

	private static BlockPos findGround(ServerLevel level, BlockPos around) {
		for (int r = 0; r <= 48; r += 2) {
			for (int dx = -r; dx <= r; dx += 2) {
				for (int dz = -r; dz <= r; dz += 2) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
					for (int y = around.getY() + 20; y > level.getMinBuildHeight() + 4; y--) {
						BlockPos pos = new BlockPos(around.getX() + dx, y, around.getZ() + dz);
						if (level.getBlockState(pos).is(RootedStateHelper.FUNGAL_NETWORK_GROUND)
								&& level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.above(2)).isAir()
								&& level.getBlockState(pos.above(3)).isAir()) return pos.immutable();
					}
				}
			}
		}
		return null;
	}

	private static void checkClient(UUID uuid) {
		var own = HeartNodeClientState.own();
		report(own.isPresent() && own.get().footprint().owner().equals(uuid) && own.get().relation() == HeartNodeRelation.OWNER
				&& own.get().footprint().heart().equals(node), "the planter's envelope packet arrived as OWNER");
	}

	private static void checkServer(ServerPlayer player) {
		if (node == null) return;
		ServerLevel level = player.serverLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		var told = HeartNodeEnvelopeSync.envelopesFor(data.envelopes(), STRANGER, node, level.getGameTime());
		report(told.size() == 1 && told.getFirst().relation() == HeartNodeRelation.STRANGER,
				"a stranger nearby is told STRANGER");
		report(data.ownerStrength(STRANGER, node) == 0.0D && !data.insideOwnEnvelope(STRANGER, node),
				"a stranger on the heart gets no benefit and no cache");
		ServerPlayer stranger = FakePlayerFactory.get(level, new GameProfile(STRANGER, "HeartNodeStranger"));
		stranger.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		stranger.moveTo(Vec3.atBottomCenterOf(node));
		BlockPos inside = node.offset(4, -1, 0);
		var refused = NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, inside, level.getBlockState(inside), stranger));
		report(refused.isCanceled(), "a stranger cannot break inside the envelope");
		BlockPos outside = node.offset((int) HeartNodeRules.HEART_RADIUS + 2, -1, 0);
		var allowed = NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, outside, level.getBlockState(outside), stranger));
		report(!allowed.isCanceled(), "a stranger breaks freely past the edge");
		report(player.hasEffect(MobEffects.REGENERATION), "the owner standing in the heart lobe is fed");
	}

	private static void frame(ServerPlayer player, boolean wide) {
		if (node == null) return;
		ServerLevel level = player.serverLevel();
		if (wide) {
			player.teleportTo(level, node.getX() + 0.5, node.getY() + 18, node.getZ() + 14.5, 180f, 52f);
		} else {
			player.teleportTo(level, node.getX() + 0.5, node.getY() + 0.4, node.getZ() + 2.6, 180f, 18f);
		}
	}

	static void capture(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("HEART_NODE_REVIEW captured {}", name);
	}

	static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("HEART_NODE_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
