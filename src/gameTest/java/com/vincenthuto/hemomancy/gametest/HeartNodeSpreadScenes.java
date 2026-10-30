package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.world.HeartNodeSpreadRenderer;
import com.vincenthuto.hemomancy.common.event.HeartNodeEnvelopeSync;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.HeartNodeEntry;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Track 4's scenes in the heart-node review: the mycelial spread over the ground. The node is replanted in place with
 * an older planting time (its synapses raised again in order) so the growth front shows part grown, then mature by day
 * and in the dark; the three-synapse chain from above; intensity 0 and 4 through the real settings path; a
 * neighbour's sanctum seen as a stranger with its strangers setting off (nothing) and on (dim, no threads); and the
 * ceiling synapse broken so its lobe recedes and greys.
 */
final class HeartNodeSpreadScenes {
	/** The ceiling synapse of Track 2's chain, the one that is broken. */
	private static final BlockPos CEILING = new BlockPos(28, 4, 0);
	private static final BlockPos NEIGHBOUR_HEART = new BlockPos(-38, 0, 0);

	private HeartNodeSpreadScenes() {}

	/**
	 * Replants the owner's node where it stands, planted {@code age} ticks ago, raises its synapses again and puts its
	 * settings back to the defaults.
	 */
	static void age(ServerPlayer player, BlockPos node, long age) {
		if (player == null || node == null) return;
		ServerLevel level = player.serverLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		HeartNodeEntry entry = data.entry(player.getUUID());
		if (entry == null) return;
		List<BlockPos> synapses = entry.synapses();
		data.plant(player.getUUID(), node, level.getGameTime() - age);
		for (BlockPos synapse : synapses) data.addSynapse(player.getUUID(), synapse);
		// Track 3's toggle scene leaves the spread off; the spread scenes start from the defaults.
		data.setSettings(player.getUUID(), HeartNodeSettings.DEFAULT);
		HeartNodeEnvelopeSync.syncAround(level, node);
		HeartNodeClientReview.report(data.entry(player.getUUID()).synapses().equals(synapses),
				"the node is replanted " + age + " ticks old with its synapses");
	}

	/** Over the heart lobe from the south, high enough to clear the hill beside the review node. */
	static void frameAbove(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		hover(player, node.getX() + 0.5, node.getY() + 14, node.getZ() + 11.5, 180f, 52f);
	}

	/**
	 * Low in the cell Track 2 cleared round the floor synapse, looking back west along its thread to the node.
	 */
	static void frameGround(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		hover(player, node.getX() + 11.5, node.getY() + 0.6, node.getZ() + 0.5, 90f, 14f);
	}

	/** High over the chain, the heart at one end and the ceiling synapse at the other (Track 2's chain framing). */
	static void frameChain(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		hover(player, node.getX() + 14.5, node.getY() + 22, node.getZ() + 18.5, 180f, 55f);
	}

	/** Puts the reviewer at a framing point and keeps them flying there, so they don't fall before the capture. */
	private static void hover(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		player.teleportTo(player.serverLevel(), x, y, z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
	}

	static void daylight(ServerPlayer player, boolean day) {
		if (player == null) return;
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		player.server.getCommands().performPrefixedCommand(source, day ? "time set noon" : "time set midnight");
		if (day) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0, false, false));
		} else {
			player.removeEffect(MobEffects.NIGHT_VISION);
		}
	}

	static void intensity(ServerPlayer player, BlockPos node, int intensity) {
		if (player == null || node == null) return;
		HeartNodeEntry entry = HeartNodeSavedData.get(player.serverLevel()).entry(player.getUUID());
		if (entry == null) return;
		HeartNodeClientReview.report(HeartNodeService.updateSettings(player, node, entry.settings().withIntensity(intensity)),
				"intensity " + intensity + " is saved through the settings path");
	}

	/** The owner's spread is being drawn, with faces found, at the intensity the server last sent. */
	static void checkOwn(Minecraft mc, int intensity, String scene) {
		if (mc.player == null) return;
		var own = HeartNodeClientState.own();
		int faces = HeartNodeSpreadRenderer.keptFaces(mc.player.getUUID());
		HeartNodeClientReview.report(own.isPresent() && own.get().settings().intensity() == intensity && faces > 0,
				"the owner's spread draws " + faces + " faces (" + scene + ")");
	}

	/**
	 * The neighbour's sanctum (planted by Track 2's foreign scene) made mature on a floor of its own, with its strangers
	 * setting {@code shown}, and the reviewer framed over it.
	 */
	static void neighbour(ServerPlayer player, BlockPos node, boolean shown) {
		if (player == null || node == null) return;
		ServerLevel level = player.serverLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		BlockPos heart = node.offset(NEIGHBOUR_HEART);
		if (!shown) {
			int reach = (int) HeartNodeRules.HEART_RADIUS + 1;
			for (BlockPos pos : BlockPos.betweenClosed(heart.offset(-reach, -1, -reach), heart.offset(reach, -1, reach)))
				level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
			for (BlockPos pos : BlockPos.betweenClosed(heart.offset(-reach, 0, -reach), heart.offset(reach, 4, reach)))
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			data.plant(SynapseScenes.NEIGHBOUR, heart, level.getGameTime() - HeartNodeRules.MATURE_TICKS);
		}
		data.setSettings(SynapseScenes.NEIGHBOUR, HeartNodeSettings.DEFAULT.with(HeartNodeSettings.SHOW_STRANGERS, shown));
		HeartNodeEnvelopeSync.syncAround(level, heart);
		hover(player, heart.getX() + 0.5, heart.getY() + 9.5, heart.getZ() + 11.5, 180f, 45f);
	}

	static void checkStranger(Minecraft mc, boolean shown) {
		UUID neighbour = SynapseScenes.NEIGHBOUR;
		boolean told = HeartNodeClientState.envelopes().stream().anyMatch(envelope ->
				envelope.footprint().owner().equals(neighbour) && envelope.relation() == HeartNodeRelation.STRANGER
						&& envelope.settings().has(HeartNodeSettings.SHOW_STRANGERS) == shown);
		int faces = HeartNodeSpreadRenderer.keptFaces(neighbour);
		HeartNodeClientReview.report(told && (shown ? faces > 0 : faces == -1), shown
				? "a stranger sees the shown sanctum (" + faces + " faces)"
				: "a stranger sees nothing of a sanctum with strangers off");
	}

	/** Over the ceiling synapse's lobe, then the synapse broken by its owner so the lobe recedes. */
	static void frameWither(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		BlockPos synapse = node.offset(CEILING);
		hover(player, synapse.getX() + 0.5, synapse.getY() + 14, synapse.getZ() + 10.5, 180f,
				55f);
	}

	static void wither(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		ServerLevel level = player.serverLevel();
		BlockPos synapse = node.offset(CEILING);
		boolean was = level.getBlockState(synapse).is(BlockInit.mycelial_synapse.get());
		level.destroyBlock(synapse, false, player);
		HeartNodeClientReview.report(was && !HeartNodeSavedData.get(level).entry(player.getUUID()).synapses().contains(synapse),
				"the ceiling synapse is broken and its record gone");
	}

	static void checkReceding(Minecraft mc, boolean receding) {
		if (mc.player == null) return;
		int count = HeartNodeSpreadRenderer.receding(mc.player.getUUID());
		HeartNodeClientReview.report(receding ? count == 1 : count == 0, receding
				? "the withered synapse's lobe is receding" : "the receded lobe is gone after three seconds");
	}
}
