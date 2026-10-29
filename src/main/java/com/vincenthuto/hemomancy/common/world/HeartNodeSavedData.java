package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Where each Apotheos's heart-node grows, on the Gardens level: one node per owner. */
public final class HeartNodeSavedData extends SavedData {
	private static final String NAME = "hemomancy_heart_nodes";
	private final Map<UUID, BlockPos> nodes = new HashMap<>();
	/** The last node each owner lost, so their next login clears the spawn it held and nothing else. */
	private final Map<UUID, BlockPos> removed = new HashMap<>();

	public static HeartNodeSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(HeartNodeSavedData::new, HeartNodeSavedData::load, null), NAME);
	}

	@Nullable
	public BlockPos nodeOf(UUID owner) {
		return nodes.get(owner);
	}

	/** Records {@code pos} as {@code owner}'s node; returns the node it replaces, if any. */
	public Optional<BlockPos> plant(UUID owner, BlockPos pos) {
		BlockPos previous = nodes.put(owner, pos.immutable());
		setDirty();
		return Optional.ofNullable(previous).filter(old -> !old.equals(pos));
	}

	public void remove(UUID owner, BlockPos pos) {
		if (pos.equals(nodes.get(owner))) {
			nodes.remove(owner);
			removed.put(owner, pos.immutable());
			setDirty();
		}
	}

	/** Whether a node at {@code pos} would put its sanctum over another owner's: footprints never overlap. */
	public boolean crowds(UUID planter, BlockPos pos) {
		int reach = HeartNodeRules.FOOTPRINT_RADIUS * 2;
		for (Map.Entry<UUID, BlockPos> entry : nodes.entrySet()) {
			if (entry.getKey().equals(planter)) continue;
			BlockPos other = entry.getValue();
			int dx = other.getX() - pos.getX();
			int dz = other.getZ() - pos.getZ();
			if (dx * dx + dz * dz <= reach * reach
					&& Math.abs(other.getY() - pos.getY()) <= HeartNodeRules.FOOTPRINT_HEIGHT * 2) return true;
		}
		return false;
	}

	/** Whether {@code pos} lies in any sanctum that is not {@code actor}'s. */
	public boolean strangerTo(UUID actor, BlockPos pos) {
		for (Map.Entry<UUID, BlockPos> entry : nodes.entrySet()) {
			if (!entry.getKey().equals(actor) && HeartNodeRules.inFootprint(entry.getValue(), pos)) return true;
		}
		return false;
	}

	/** True once when {@code respawn} is the spawn of the node {@code owner} last lost; that record is then spent. */
	public boolean takeRemovedSpawn(UUID owner, BlockPos respawn) {
		BlockPos lost = removed.get(owner);
		if (lost == null || !lost.above().equals(respawn)) return false;
		removed.remove(owner);
		setDirty();
		return true;
	}

	private static HeartNodeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		HeartNodeSavedData data = new HeartNodeSavedData();
		for (Tag element : tag.getList("Nodes", Tag.TAG_COMPOUND)) {
			CompoundTag node = (CompoundTag) element;
			NbtUtils.readBlockPos(node, "Pos").ifPresent(pos -> data.nodes.put(node.getUUID("Owner"), pos));
		}
		for (Tag element : tag.getList("Removed", Tag.TAG_COMPOUND)) {
			CompoundTag node = (CompoundTag) element;
			NbtUtils.readBlockPos(node, "Pos").ifPresent(pos -> data.removed.put(node.getUUID("Owner"), pos));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		nodes.forEach((owner, pos) -> {
			CompoundTag node = new CompoundTag();
			node.putUUID("Owner", owner);
			node.put("Pos", NbtUtils.writeBlockPos(pos));
			list.add(node);
		});
		tag.put("Nodes", list);
		ListTag lost = new ListTag();
		removed.forEach((owner, pos) -> {
			CompoundTag node = new CompoundTag();
			node.putUUID("Owner", owner);
			node.put("Pos", NbtUtils.writeBlockPos(pos));
			lost.add(node);
		});
		tag.put("Removed", lost);
		return tag;
	}
}
