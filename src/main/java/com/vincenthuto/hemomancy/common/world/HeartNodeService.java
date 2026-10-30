package com.vincenthuto.hemomancy.common.world;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.event.HeartNodeEnvelopeSync;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeMomentPacket;
import com.vincenthuto.hemomancy.common.network.heartnode.PacketOpenHeartNodeScreen;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * What the Heart-node screen reads and writes. The owner opens their node and sets how the sanctum looks; a creative
 * operator may look at anyone's without changing it. Settings are presentation only, so saving them touches nothing
 * the sanctum's rules read. It also announces the node's moments (planting, withering, a cache opened) to whoever
 * is near enough to see them.
 */
public final class HeartNodeService {
	/** Blocks from a moment within which players are shown it. */
	public static final double MOMENT_RANGE = 64.0D;

	private HeartNodeService() {}

	/** Shows {@code moment} at {@code pos} to the players near it; {@code stage} is the node's maturity, or 0. */
	public static void announce(ServerLevel level, BlockPos pos, HeartNodeMoment moment, int stage) {
		PacketHandler.sendNear(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, MOMENT_RANGE,
				new HeartNodeMomentPacket(pos, moment, stage));
	}

	/** Opens the node's screen for {@code viewer}, or tells them why it stays shut. */
	public static void open(ServerPlayer viewer, HeartNodeBlockEntity node) {
		ServerLevel level = viewer.serverLevel();
		BlockPos pos = node.getBlockPos();
		boolean owner = HeartNodeRules.mayOpen(node.owner(), viewer.getUUID());
		boolean operator = viewer.isCreative() && viewer.hasPermissions(2);
		if (!owner && !operator) {
			tell(viewer, "message.hemomancy.heart_node.not_yours");
			return;
		}
		HeartNodeEntry entry = node.owner() == null ? null : HeartNodeSavedData.get(level).entry(node.owner());
		if (entry == null || !entry.heart().equals(pos)) {
			tell(viewer, "message.hemomancy.heart_node.no_sanctum");
			return;
		}
		PacketHandler.sendToPlayer(viewer, snapshot(level, entry, !owner));
	}

	/** Everything the screen shows that the client cannot work out from the envelope sync. */
	public static PacketOpenHeartNodeScreen snapshot(ServerLevel level, HeartNodeEntry entry, boolean readOnly) {
		List<BlockPos> connected = entry.footprint().connectedSynapses();
		List<PacketOpenHeartNodeScreen.Synapse> synapses = new ArrayList<>();
		for (BlockPos pos : entry.synapses()) {
			int facing = PacketOpenHeartNodeScreen.Synapse.UNKNOWN_FACING;
			if (level.isLoaded(pos)) {
				BlockState state = level.getBlockState(pos);
				if (state.hasProperty(MycelialSynapseBlock.FACING)) facing = state.getValue(MycelialSynapseBlock.FACING).ordinal();
			}
			synapses.add(new PacketOpenHeartNodeScreen.Synapse(pos, facing, connected.contains(pos)));
		}
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(entry.owner());
		boolean respawnHere = owner != null && level.dimension().equals(owner.getRespawnDimension())
				&& entry.heart().above().equals(owner.getRespawnPosition());
		return new PacketOpenHeartNodeScreen(entry.heart(), entry.owner(), ownerName(level, entry.owner()), synapses,
				entry.settings(), entry.plantedAt(), respawnHere, waitingCaches(level, entry.owner()), readOnly);
	}

	/**
	 * Saves {@code settings} on {@code player}'s own node at {@code pos} and resends the envelope to everyone near it,
	 * so the world behind the screen changes at once. False when {@code pos} is not the player's recorded node.
	 */
	public static boolean updateSettings(ServerPlayer player, BlockPos pos, HeartNodeSettings settings) {
		ServerLevel level = player.serverLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		HeartNodeEntry entry = data.entry(player.getUUID());
		if (entry == null || !entry.heart().equals(pos)) return false;
		data.setSettings(player.getUUID(), settings);
		HeartNodeEnvelopeSync.syncAround(level, pos);
		return true;
	}

	/** The owner's recorded caches, forgetting any whose loaded block is no longer a cache. */
	private static List<BlockPos> waitingCaches(ServerLevel level, UUID owner) {
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		List<BlockPos> waiting = new ArrayList<>();
		for (BlockPos pos : data.caches(owner)) {
			if (level.isLoaded(pos) && !level.getBlockState(pos).is(BlockInit.fruiting_body_cache.get())) {
				data.removeCache(pos);
			} else {
				waiting.add(pos);
			}
		}
		return waiting;
	}

	private static String ownerName(ServerLevel level, UUID owner) {
		ServerPlayer online = level.getServer().getPlayerList().getPlayer(owner);
		if (online != null) return online.getGameProfile().getName();
		var cache = level.getServer().getProfileCache();
		return cache == null ? owner.toString().substring(0, 8)
				: cache.get(owner).map(profile -> profile.getName()).orElse(owner.toString().substring(0, 8));
	}

	private static void tell(ServerPlayer player, String key) {
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE,
				ChatFormatting.ITALIC), true);
	}
}
