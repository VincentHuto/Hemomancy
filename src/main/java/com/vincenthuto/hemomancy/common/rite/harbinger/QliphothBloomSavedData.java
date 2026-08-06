package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * World-level persistence for Qliphoth Blooms established via the Bloom of the
 * Qliphoth rite. Each bloom has an owner, center position, dimension,
 * chunk radius, and creation timestamp.
 * <p>
 * Effects within a bloom's radius:
 * <ul>
 *   <li>All blood manipulations cost 25% less blood</li>
 *   <li>Passive health regeneration (Regeneration I) every 2 seconds</li>
 *   <li>Enhanced blood regeneration rate</li>
 * </ul>
 * <p>
 * Each bloom tracks how many Qliphoth Pomes have ripened from it and whether
 * one ripe pome is still waiting to be consumed. A tree's lifecycle produces
 * exactly nine pomes (one per Qliphoth husk); after the ninth ripening the
 * tree ceases producing fruit until re-summoned.
 */
public class QliphothBloomSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_qliphoth_blooms";
	private static final SavedData.Factory<QliphothBloomSavedData> FACTORY =
			new SavedData.Factory<>(QliphothBloomSavedData::new, QliphothBloomSavedData::load, null);
	/** Maximum pomes a single bloom produces before its lifecycle is exhausted. */
	public static final int MAX_POMES_PER_BLOOM = 9;

	private final List<BloomEntry> blooms = new ArrayList<>();

	/**
	 * Tracks how many pomes have been dropped by each bloom, keyed by the
	 * bloom center's {@link BlockPos#asLong()} value. Cleared when a bloom is
	 * pruned or removed.
	 */
	private final Map<Long, Integer> pomesDroppedByBloom = new HashMap<>();
	private final Map<Long, SeveredQliphothState> bloomStates = new HashMap<>();

	/**
	 * Tracks the currently ripe or claimed-but-unconsumed pome by bloom center.
	 * The value is the husk index (0-8). A bloom may not ripen another pome
	 * while this entry exists.
	 */
	private final Map<Long, Integer> pendingPomeByBloom = new HashMap<>();
	private final Set<Long> claimedPendingPomes = new HashSet<>();
	private final Map<UUID, List<ItemStack>> pendingBoundPomeReturns = new HashMap<>();

	public QliphothBloomSavedData() {}

	public static QliphothBloomSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static QliphothBloomSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		QliphothBloomSavedData data = new QliphothBloomSavedData();
		if (tag.contains("blooms", Tag.TAG_LIST)) {
			ListTag list = tag.getList("blooms", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long createdTick = entry.getLong("CreatedTick");
				data.blooms.add(new BloomEntry(ownerUUID, center, dimension, chunkRadius, createdTick));
			}
		}
		if (tag.contains("pomesDropped", Tag.TAG_LIST)) {
			ListTag pdList = tag.getList("pomesDropped", Tag.TAG_COMPOUND);
			for (int i = 0; i < pdList.size(); i++) {
				CompoundTag entry = pdList.getCompound(i);
				long centerLong = entry.getLong("Center");
				int count = entry.getInt("Count");
				data.pomesDroppedByBloom.put(centerLong, count);
			}
		}
		if (tag.contains("bloomStates", Tag.TAG_LIST)) {
			ListTag states = tag.getList("bloomStates", Tag.TAG_COMPOUND);
			for (int i = 0; i < states.size(); i++) {
				CompoundTag entry = states.getCompound(i);
				data.bloomStates.put(entry.getLong("Center"),
						SeveredQliphothState.byName(entry.getString("State")));
			}
		}
		if (tag.contains("pendingPomes", Tag.TAG_LIST)) {
			ListTag pendingList = tag.getList("pendingPomes", Tag.TAG_COMPOUND);
			for (int i = 0; i < pendingList.size(); i++) {
				CompoundTag entry = pendingList.getCompound(i);
				data.pendingPomeByBloom.put(entry.getLong("Center"), entry.getInt("HuskIndex"));
				if (entry.getBoolean("Claimed")) {
					data.claimedPendingPomes.add(entry.getLong("Center"));
				}
			}
		}
		if (tag.contains("pendingBoundPomeReturns", Tag.TAG_LIST)) {
			ListTag owners = tag.getList("pendingBoundPomeReturns", Tag.TAG_COMPOUND);
			for (int i = 0; i < owners.size(); i++) {
				CompoundTag ownerTag = owners.getCompound(i);
				UUID ownerUUID = ownerTag.getUUID("Owner");
				ListTag stacks = ownerTag.getList("Stacks", Tag.TAG_COMPOUND);
				List<ItemStack> owedStacks = new ArrayList<>();
				for (int stackIndex = 0; stackIndex < stacks.size(); stackIndex++) {
					ItemStack stack = ItemStack.parseOptional(provider, stacks.getCompound(stackIndex));
					if (!stack.isEmpty()) {
						owedStacks.add(stack);
					}
				}
				if (!owedStacks.isEmpty()) {
					data.pendingBoundPomeReturns.put(ownerUUID, owedStacks);
				}
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (BloomEntry entry : blooms) {
			CompoundTag bloomTag = new CompoundTag();
			bloomTag.putUUID("Owner", entry.ownerUUID());
			bloomTag.putLong("Center", entry.center().asLong());
			bloomTag.putString("Dimension", entry.dimension());
			bloomTag.putInt("ChunkRadius", entry.chunkRadius());
			bloomTag.putLong("CreatedTick", entry.createdTick());
			list.add(bloomTag);
		}
		tag.put("blooms", list);

		ListTag pdList = new ListTag();
		for (Map.Entry<Long, Integer> pd : pomesDroppedByBloom.entrySet()) {
			CompoundTag pdTag = new CompoundTag();
			pdTag.putLong("Center", pd.getKey());
			pdTag.putInt("Count", pd.getValue());
			pdList.add(pdTag);
		}
		tag.put("pomesDropped", pdList);
		ListTag stateList = new ListTag();
		for (Map.Entry<Long, SeveredQliphothState> state : bloomStates.entrySet()) {
			CompoundTag stateTag = new CompoundTag();
			stateTag.putLong("Center", state.getKey());
			stateTag.putString("State", state.getValue().name());
			stateList.add(stateTag);
		}
		tag.put("bloomStates", stateList);

		ListTag pendingList = new ListTag();
		for (Map.Entry<Long, Integer> pending : pendingPomeByBloom.entrySet()) {
			CompoundTag pendingTag = new CompoundTag();
			pendingTag.putLong("Center", pending.getKey());
			pendingTag.putInt("HuskIndex", pending.getValue());
			pendingTag.putBoolean("Claimed", claimedPendingPomes.contains(pending.getKey()));
			pendingList.add(pendingTag);
		}
		tag.put("pendingPomes", pendingList);

		ListTag pendingReturnList = new ListTag();
		for (Map.Entry<UUID, List<ItemStack>> pendingReturn : pendingBoundPomeReturns.entrySet()) {
			ListTag stacks = new ListTag();
			for (ItemStack stack : pendingReturn.getValue()) {
				if (!stack.isEmpty()) {
					stacks.add(stack.save(provider));
				}
			}
			if (!stacks.isEmpty()) {
				CompoundTag ownerTag = new CompoundTag();
				ownerTag.putUUID("Owner", pendingReturn.getKey());
				ownerTag.put("Stacks", stacks);
				pendingReturnList.add(ownerTag);
			}
		}
		tag.put("pendingBoundPomeReturns", pendingReturnList);


		return tag;
	}

	public void addBloom(BloomEntry entry) {
		blooms.add(entry);
		bloomStates.put(entry.center().asLong(), SeveredQliphothState.LIVING);
		setDirty();
	}

	public List<BloomEntry> getBlooms() {
		return blooms;
	}

	/**
	 * Returns the number of pomes already dropped from the bloom at the given
	 * center position. Returns 0 if no pomes have been dropped yet.
	 */
	public int getPomesDropped(BlockPos center) {
		return pomesDroppedByBloom.getOrDefault(center.asLong(), 0);
	}

	/**
	 * Increments the pome-drop counter for the given bloom center and marks
	 * the data as dirty. Returns the new count after incrementing.
	 */
	public int incrementPomesDropped(BlockPos center) {
		long key = center.asLong();
		int next = pomesDroppedByBloom.getOrDefault(key, 0) + 1;
		pomesDroppedByBloom.put(key, next);
		setDirty();
		return next;
	}

	public SeveredQliphothState getState(BlockPos center) {
		return bloomStates.getOrDefault(center.asLong(), SeveredQliphothState.LIVING);
	}

	public boolean severBloom(BlockPos center) {
		long key = center.asLong();
		if (blooms.stream().noneMatch(bloom -> bloom.center().equals(center))) return false;
		SeveredQliphothState next = getState(center).sever();
		bloomStates.put(key, next);
		pendingPomeByBloom.remove(key);
		claimedPendingPomes.remove(key);
		setDirty();
		return next.isPortalOpen();
	}

	public boolean sealBloom(BlockPos center) {
		SeveredQliphothState next = getState(center).seal();
		bloomStates.put(center.asLong(), next);
		setDirty();
		return next.isSealedTrophy();
	}

	public boolean hasPendingPome(BlockPos center) {
		return pendingPomeByBloom.containsKey(center.asLong());
	}

	public int getPendingPomeHuskIndex(BlockPos center) {
		return pendingPomeByBloom.getOrDefault(center.asLong(), -1);
	}

	public void setPendingPome(BlockPos center, int huskIndex) {
		long key = center.asLong();
		pendingPomeByBloom.put(key, huskIndex);
		claimedPendingPomes.remove(key);
		setDirty();
	}

	public void clearPendingPome(long bloomOrigin) {
		if (pendingPomeByBloom.remove(bloomOrigin) != null) {
			claimedPendingPomes.remove(bloomOrigin);
			setDirty();
		}
	}

	public boolean isPendingPomeClaimed(BlockPos center) {
		return claimedPendingPomes.contains(center.asLong());
	}

	public void markPendingPomeClaimed(BlockPos center) {
		if (pendingPomeByBloom.containsKey(center.asLong())) {
			claimedPendingPomes.add(center.asLong());
			setDirty();
		}
	}

	public void queueBoundPomeReturn(UUID ownerUUID, ItemStack stack) {
		if (ownerUUID == null || stack.isEmpty()) {
			return;
		}
		pendingBoundPomeReturns.computeIfAbsent(ownerUUID, ignored -> new ArrayList<>()).add(stack.copy());
		setDirty();
	}

	public List<ItemStack> takePendingBoundPomeReturns(UUID ownerUUID) {
		List<ItemStack> stacks = pendingBoundPomeReturns.remove(ownerUUID);
		if (stacks != null) {
			setDirty();
			return stacks;
		}
		return List.of();
	}

	/**
	 * Returns the remaining number of pomes this bloom can still drop,
	 * i.e. {@link #MAX_POMES_PER_BLOOM} minus the number already dropped.
	 * Returns 0 once the lifecycle is exhausted.
	 */
	public int getRemainingPomes(BlockPos center) {
		return Math.max(0, MAX_POMES_PER_BLOOM - getPomesDropped(center));
	}

	/**
	 * Find the bloom that contains a given block position in a given dimension.
	 * Returns null if no bloom covers that location.
	 */
	public BloomEntry getBloomAt(BlockPos pos, String dimension) {
		for (BloomEntry entry : blooms) {
			if (!entry.dimension().equals(dimension)) continue;
			int blockRadius = entry.chunkRadius() * 16;
			BlockPos center = entry.center();
			if (Math.abs(pos.getX() - center.getX()) <= blockRadius
					&& Math.abs(pos.getZ() - center.getZ()) <= blockRadius) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Check if any bloom covers the given position in the given dimension.
	 */
	public boolean isInBloomRange(BlockPos pos, String dimension) {
		return getBloomAt(pos, dimension) != null;
	}

	/**
	 * Check if placing a new bloom at the given position would overlap with any
	 * existing bloom's radius in the same dimension. Two blooms overlap if
	 * either center falls within the other's chunk radius.
	 *
	 * @return the existing bloom that would overlap, or null if placement is clear
	 */
	public BloomEntry getOverlappingBloom(BlockPos newCenter, String dimension, int newChunkRadius) {
		int newBlockRadius = newChunkRadius * 16;
		for (BloomEntry existing : blooms) {
			if (!existing.dimension().equals(dimension)) continue;
			int existingBlockRadius = existing.chunkRadius() * 16;
			int dx = Math.abs(newCenter.getX() - existing.center().getX());
			int dz = Math.abs(newCenter.getZ() - existing.center().getZ());
			// Overlap if the new center is inside the existing radius
			// or the existing center is inside the new radius
			if ((dx <= existingBlockRadius && dz <= existingBlockRadius)
					|| (dx <= newBlockRadius && dz <= newBlockRadius)) {
				return existing;
			}
		}
		return null;
	}

	/**
	 * Remove the bloom whose center is in the same chunk as the given position
	 * in the given dimension. Returns the removed entry, or null if none found.
	 * Also clears the pomes-dropped counter for that bloom.
	 */
	public BloomEntry removeBloomInChunk(BlockPos pos, String dimension) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		for (int i = 0; i < blooms.size(); i++) {
			BloomEntry entry = blooms.get(i);
			if (!entry.dimension().equals(dimension)) continue;
			int bloomChunkX = entry.center().getX() >> 4;
			int bloomChunkZ = entry.center().getZ() >> 4;
			if (bloomChunkX == chunkX && bloomChunkZ == chunkZ) {
				blooms.remove(i);
				bloomStates.remove(entry.center().asLong());
				pomesDroppedByBloom.remove(entry.center().asLong());
				pendingPomeByBloom.remove(entry.center().asLong());
				claimedPendingPomes.remove(entry.center().asLong());
				setDirty();
				return entry;
			}
		}
		return null;
	}

	/**
	 * A persistent Qliphoth Bloom entry.
	 */
	public record BloomEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long createdTick) {}
}
