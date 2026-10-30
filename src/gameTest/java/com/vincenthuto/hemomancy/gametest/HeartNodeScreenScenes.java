package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.screen.tile.functional.HeartNodeScreen;
import com.vincenthuto.hemomancy.common.network.heartnode.PacketOpenHeartNodeScreen;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Track 3's scenes for the heart-node review: the "you leave the sanctum" line on stepping out, then the screen opened
 * the way a player opens it, each toggle flipped in turn with the synced bits checked a quarter second later and the
 * screen plus the world behind it captured.
 */
final class HeartNodeScreenScenes {
	static final String[] TOGGLE_NAMES = {"spread", "edge", "threads", "pulse", "motes", "show-strangers", "edge-warning"};
	private static final int[] TOGGLE_BITS = {HeartNodeSettings.SPREAD, HeartNodeSettings.EDGE,
			HeartNodeSettings.THREADS, HeartNodeSettings.PULSE, HeartNodeSettings.MOTES,
			HeartNodeSettings.SHOW_STRANGERS, HeartNodeSettings.EDGE_WARNING};

	private HeartNodeScreenScenes() {}

	/** Stands the owner just inside their heart lobe. */
	static void stepInside(ServerPlayer player, BlockPos node) {
		if (node == null) return;
		player.teleportTo(player.serverLevel(), node.getX() + 0.5, node.getY(), node.getZ() + 4.5, 180f, 20f);
	}

	/** And then well past the edge of everything they hold. */
	static void stepOutside(ServerPlayer player, BlockPos node) {
		if (node == null) return;
		HeartNodeSavedData data = HeartNodeSavedData.get(player.serverLevel());
		int out = (int) Math.ceil(data.footprint(player.getUUID()).maxRadius()) + 6;
		player.teleportTo(player.serverLevel(), node.getX() - out + 0.5, node.getY() + 4, node.getZ() + 0.5, 90f, 20f);
	}

	static void checkLeaveLine(Minecraft mc) {
		String shown = overlay(mc);
		HeartNodeClientReview.report(shown != null && shown.contains("leave the sanctum"),
				"stepping out of the envelope shows the leave line (" + shown + ")");
	}

	/** Frames the node from a few blocks off and right-clicks it, through the block's own use action. */
	static void open(ServerPlayer player, BlockPos node) {
		if (node == null) return;
		ServerLevel level = player.serverLevel();
		player.teleportTo(level, node.getX() + 0.5, node.getY() + 0.2, node.getZ() + 3.5, 180f, 14f);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(node), Direction.SOUTH, node, false);
		level.getBlockState(node).useWithoutItem(level, player, hit);
	}

	static boolean checkOpen(Minecraft mc) {
		boolean open = mc.screen instanceof HeartNodeScreen;
		HeartNodeClientReview.report(open, "right-clicking the node opens the Heart-node screen for its owner");
		return open;
	}

	/** Flips toggle {@code index} as its button would. */
	static void flip(Minecraft mc, int index) {
		if (mc.screen instanceof HeartNodeScreen screen) screen.flip(index);
	}

	/** A quarter second after the flip, the envelope the server sent back carries the new bit. */
	static void checkFlip(Minecraft mc, int index) {
		if (!(mc.screen instanceof HeartNodeScreen screen)) {
			HeartNodeClientReview.report(false, "the screen is still open for toggle " + TOGGLE_NAMES[index]);
			return;
		}
		boolean wanted = screen.settings().has(TOGGLE_BITS[index]);
		var own = HeartNodeClientState.own();
		HeartNodeClientReview.report(own.isPresent() && own.get().settings().has(TOGGLE_BITS[index]) == wanted
						&& own.get().settings().equals(screen.settings()),
				"toggle " + TOGGLE_NAMES[index] + " " + (wanted ? "on" : "off") + " is synced back within a quarter second");
	}

	/** After every toggle has been flipped once, the server's record holds the inverse of the defaults. */
	static void checkSaved(ServerPlayer player) {
		var entry = HeartNodeSavedData.get(player.serverLevel()).entry(player.getUUID());
		int expected = HeartNodeSettings.ALL & ~HeartNodeSettings.DEFAULT.bits();
		HeartNodeClientReview.report(entry != null && entry.settings().bits() == expected,
				"the saved settings hold every flip (" + (entry == null ? "no entry" : Integer.toBinaryString(entry.settings().bits())) + ")");
	}

	static void close(Minecraft mc) {
		if (mc.screen instanceof HeartNodeScreen) mc.setScreen(null);
	}

	/** A sanctum with a chain of three synapses, one cut off, and two caches, for the screen-chrome capture. */
	static PacketOpenHeartNodeScreen sampleScreenState(BlockPos heart, UUID owner, boolean readOnly) {
		return new PacketOpenHeartNodeScreen(heart, owner, "Apotheos", List.of(
				new PacketOpenHeartNodeScreen.Synapse(heart.offset(17, 0, -4), Direction.UP.get3DDataValue(), true),
				new PacketOpenHeartNodeScreen.Synapse(heart.offset(29, 2, 6), Direction.WEST.get3DDataValue(), true),
				new PacketOpenHeartNodeScreen.Synapse(heart.offset(-6, 1, 30),
						PacketOpenHeartNodeScreen.Synapse.UNKNOWN_FACING, false)),
				HeartNodeSettings.DEFAULT, 0L, true, List.of(heart.offset(5, 0, 3), heart.offset(22, 1, 2)), readOnly);
	}

	private static String overlay(Minecraft mc) {
		try {
			var field = Gui.class.getDeclaredField("overlayMessageString");
			field.setAccessible(true);
			Object text = field.get(mc.gui);
			return text instanceof net.minecraft.network.chat.Component component ? component.getString() : null;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
}
