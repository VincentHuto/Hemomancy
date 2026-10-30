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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Each Apotheos's sanctum on the Gardens level, one per owner: the node, its synapses, its settings and when it was
 * planted, and the fruiting-body caches each owner has waiting. A save from before synapses existed holds only owner
 * and position, and loads as a node with none; one from before caches were recorded loads with none waiting.
 */
public final class HeartNodeSavedData extends SavedData {
	private static final String NAME = "hemomancy_heart_nodes";
	private final Map<UUID, HeartNodeEntry> nodes = new HashMap<>();
	/** The last node each owner lost, so their next login clears the spawn it held and nothing else. */
	private final Map<UUID, BlockPos> removed = new HashMap<>();
	/** Each owner's unopened fruiting-body caches, oldest first. They outlive the node: a cache is the owner's gear. */
	private final Map<UUID, List<BlockPos>> caches = new HashMap<>();

	public static HeartNodeSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(HeartNodeSavedData::new, HeartNodeSavedData::load, null), NAME);
	}

	@Nullable
	public HeartNodeEntry entry(UUID owner) {
		return nodes.get(owner);
	}

	@Nullable
	public BlockPos nodeOf(UUID owner) {
		HeartNodeEntry entry = nodes.get(owner);
		return entry == null ? null : entry.heart();
	}

	@Nullable
	public HeartNodeFootprint footprint(UUID owner) {
		HeartNodeEntry entry = nodes.get(owner);
		return entry == null ? null : entry.footprint();
	}

	/** Whose sanctum {@code pos} lies in, if anyone's. Envelopes never overlap, so there is at most one. */
	public Optional<HeartNodeEntry> footprintAt(BlockPos pos) {
		for (HeartNodeEntry entry : nodes.values()) {
			if (entry.footprint().contains(pos)) return Optional.of(entry);
		}
		return Optional.empty();
	}

	public Collection<HeartNodeEntry> envelopes() {
		return Collections.unmodifiableCollection(nodes.values());
	}

	/**
	 * Records {@code pos} as {@code owner}'s node, planted at {@code gameTime}; returns the node it replaces, if any.
	 * The replaced node's synapse records go with it.
	 */
	public Optional<HeartNodeEntry> plant(UUID owner, BlockPos pos, long gameTime) {
		HeartNodeEntry previous = nodes.put(owner, HeartNodeEntry.planted(owner, pos, gameTime));
		setDirty();
		return Optional.ofNullable(previous).filter(old -> !old.heart().equals(pos));
	}

	/** Forgets {@code owner}'s node at {@code pos} and its synapses; returns the entry that was removed. */
	public Optional<HeartNodeEntry> remove(UUID owner, BlockPos pos) {
		HeartNodeEntry entry = nodes.get(owner);
		if (entry == null || !entry.heart().equals(pos)) return Optional.empty();
		nodes.remove(owner);
		removed.put(owner, pos.immutable());
		setDirty();
		return Optional.of(entry);
	}

	/** Whether a lobe of {@code radius} at {@code pos} would touch any sanctum not {@code planter}'s. */
	public boolean wouldOverlap(UUID planter, BlockPos pos, double radius) {
		for (HeartNodeEntry entry : nodes.values()) {
			if (!entry.owner().equals(planter) && entry.footprint().wouldOverlap(pos, radius)) return true;
		}
		return false;
	}

	/** Whether {@code pos} lies in any sanctum that is not {@code actor}'s. */
	public boolean strangerTo(UUID actor, BlockPos pos) {
		return footprintAt(pos).map(entry -> !entry.owner().equals(actor)).orElse(false);
	}

	/**
	 * How strongly {@code player}'s own sanctum holds {@code pos}: 0 anywhere outside it, and 0 for everyone who is
	 * not its planter, wherever they stand. There are no members in a sanctum.
	 */
	public double ownerStrength(UUID player, BlockPos pos) {
		HeartNodeEntry entry = nodes.get(player);
		if (entry == null || !entry.owner().equals(player)) return 0.0D;
		return entry.footprint().strength(pos);
	}

	/** Whether {@code pos} lies in {@code player}'s own sanctum. */
	public boolean insideOwnEnvelope(UUID player, BlockPos pos) {
		HeartNodeEntry entry = nodes.get(player);
		return entry != null && entry.footprint().contains(pos);
	}

	/** Adds a synapse to {@code owner}'s envelope if it chains, fits the budget and touches no one else's. */
	public boolean addSynapse(UUID owner, BlockPos pos) {
		HeartNodeEntry entry = nodes.get(owner);
		if (entry == null || !entry.footprint().canAddSynapse(pos)
				|| wouldOverlap(owner, pos, HeartNodeRules.SYNAPSE_RADIUS)) return false;
		nodes.put(owner, entry.withSynapse(pos));
		setDirty();
		return true;
	}

	public boolean removeSynapse(UUID owner, BlockPos pos) {
		HeartNodeEntry entry = nodes.get(owner);
		if (entry == null || !entry.synapses().contains(pos)) return false;
		nodes.put(owner, entry.withoutSynapse(pos));
		setDirty();
		return true;
	}

	public void setSettings(UUID owner, HeartNodeSettings settings) {
		HeartNodeEntry entry = nodes.get(owner);
		if (entry == null || entry.settings().equals(settings)) return;
		nodes.put(owner, entry.withSettings(settings));
		setDirty();
	}

	public void addCache(UUID owner, BlockPos pos) {
		List<BlockPos> waiting = caches.computeIfAbsent(owner, key -> new ArrayList<>());
		if (waiting.contains(pos)) return;
		waiting.add(pos.immutable());
		setDirty();
	}

	/** Forgets the cache at {@code pos}, whoever's it was. */
	public boolean removeCache(BlockPos pos) {
		for (var iterator = caches.values().iterator(); iterator.hasNext(); ) {
			List<BlockPos> waiting = iterator.next();
			if (!waiting.remove(pos)) continue;
			if (waiting.isEmpty()) iterator.remove();
			setDirty();
			return true;
		}
		return false;
	}

	public List<BlockPos> caches(UUID owner) {
		return List.copyOf(caches.getOrDefault(owner, List.of()));
	}

	/** True once when {@code respawn} is the spawn of the node {@code owner} last lost; that record is then spent. */
	public boolean takeRemovedSpawn(UUID owner, BlockPos respawn) {
		BlockPos lost = removed.get(owner);
		if (lost == null || !lost.above().equals(respawn)) return false;
		removed.remove(owner);
		setDirty();
		return true;
	}

	static HeartNodeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		HeartNodeSavedData data = new HeartNodeSavedData();
		for (Tag element : tag.getList("Nodes", Tag.TAG_COMPOUND)) {
			CompoundTag node = (CompoundTag) element;
			UUID owner = node.getUUID("Owner");
			NbtUtils.readBlockPos(node, "Pos").ifPresent(pos -> {
				List<BlockPos> synapses = new ArrayList<>();
				for (long packed : node.getLongArray("Synapses")) synapses.add(BlockPos.of(packed));
				data.nodes.put(owner, new HeartNodeEntry(owner, pos, synapses,
						HeartNodeSettings.load(node.getCompound("Settings")), node.contains("PlantedAt") ? node.getLong("PlantedAt") : HeartNodeRules.LONG_GROWN));
			});
		}
		for (Tag element : tag.getList("Removed", Tag.TAG_COMPOUND)) {
			CompoundTag node = (CompoundTag) element;
			NbtUtils.readBlockPos(node, "Pos").ifPresent(pos -> data.removed.put(node.getUUID("Owner"), pos));
		}
		for (Tag element : tag.getList("Caches", Tag.TAG_COMPOUND)) {
			CompoundTag owner = (CompoundTag) element;
			List<BlockPos> waiting = new ArrayList<>();
			for (long packed : owner.getLongArray("Positions")) waiting.add(BlockPos.of(packed));
			if (!waiting.isEmpty()) data.caches.put(owner.getUUID("Owner"), waiting);
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		nodes.values().forEach(entry -> {
			CompoundTag node = new CompoundTag();
			node.putUUID("Owner", entry.owner());
			node.put("Pos", NbtUtils.writeBlockPos(entry.heart()));
			node.putLongArray("Synapses", entry.synapses().stream().mapToLong(BlockPos::asLong).toArray());
			node.put("Settings", entry.settings().save());
			node.putLong("PlantedAt", entry.plantedAt());
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
		ListTag waiting = new ListTag();
		caches.forEach((owner, positions) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("Owner", owner);
			entry.putLongArray("Positions", positions.stream().mapToLong(BlockPos::asLong).toArray());
			waiting.add(entry);
		});
		tag.put("Caches", waiting);
		return tag;
	}
}
