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
	 * bloom lifecycle ID. Cleared when a bloom is
	 * pruned or removed.
	 */
	private final Map<UUID, Integer> pomesDroppedByBloom = new HashMap<>();
	private final Map<UUID, SeveredQliphothState> bloomStates = new HashMap<>();

	/**
	 * Tracks the currently ripe or claimed-but-unconsumed pome by bloom lifecycle.
	 * The value is the husk index (0-8). A bloom may not ripen another pome
	 * while this entry exists.
	 */
	private final Map<UUID, Integer> pendingPomeByBloom = new HashMap<>();
	private final Set<UUID> claimedPendingPomes = new HashSet<>();
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
				UUID bloomId = entry.hasUUID("BloomId") ? entry.getUUID("BloomId") : UUID.randomUUID();
				boolean migratesLegacyProgress = !entry.hasUUID("BloomId") || entry.getBoolean("MigratesLegacyProgress");
				data.blooms.add(new BloomEntry(ownerUUID, center, dimension, chunkRadius, createdTick,
						bloomId, migratesLegacyProgress));
				if (!entry.hasUUID("BloomId")) data.setDirty();
			}
		}
		if (tag.contains("pomesDropped", Tag.TAG_LIST)) {
			ListTag pdList = tag.getList("pomesDropped", Tag.TAG_COMPOUND);
			for (int i = 0; i < pdList.size(); i++) {
				CompoundTag entry = pdList.getCompound(i);
				UUID bloomId = data.bloomIdFromTag(entry);
				if (bloomId != null) data.pomesDroppedByBloom.put(bloomId, entry.getInt("Count"));
			}
		}
		if (tag.contains("bloomStates", Tag.TAG_LIST)) {
			ListTag states = tag.getList("bloomStates", Tag.TAG_COMPOUND);
			for (int i = 0; i < states.size(); i++) {
				CompoundTag entry = states.getCompound(i);
				UUID bloomId = data.bloomIdFromTag(entry);
				if (bloomId != null) data.bloomStates.put(bloomId,
						SeveredQliphothState.byName(entry.getString("State")));
			}
		}
		if (tag.contains("pendingPomes", Tag.TAG_LIST)) {
			ListTag pendingList = tag.getList("pendingPomes", Tag.TAG_COMPOUND);
			for (int i = 0; i < pendingList.size(); i++) {
				CompoundTag entry = pendingList.getCompound(i);
				UUID bloomId = data.bloomIdFromTag(entry);
				if (bloomId != null) {
					data.pendingPomeByBloom.put(bloomId, entry.getInt("HuskIndex"));
					if (entry.getBoolean("Claimed")) data.claimedPendingPomes.add(bloomId);
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

	private UUID bloomIdFromTag(CompoundTag entry) {
		if (entry.hasUUID("BloomId")) return entry.getUUID("BloomId");
		long center = entry.getLong("Center");
		setDirty();
		return blooms.stream().filter(bloom -> bloom.migratesLegacyProgress()
				&& bloom.center().asLong() == center)
				.map(BloomEntry::bloomId).findFirst().orElse(null);
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
			bloomTag.putUUID("BloomId", entry.bloomId());
			if (entry.migratesLegacyProgress()) bloomTag.putBoolean("MigratesLegacyProgress", true);
			list.add(bloomTag);
		}
		tag.put("blooms", list);

		ListTag pdList = new ListTag();
		for (Map.Entry<UUID, Integer> pd : pomesDroppedByBloom.entrySet()) {
			CompoundTag pdTag = new CompoundTag();
			pdTag.putUUID("BloomId", pd.getKey());
			pdTag.putInt("Count", pd.getValue());
			pdList.add(pdTag);
		}
		tag.put("pomesDropped", pdList);
		ListTag stateList = new ListTag();
		for (Map.Entry<UUID, SeveredQliphothState> state : bloomStates.entrySet()) {
			CompoundTag stateTag = new CompoundTag();
			stateTag.putUUID("BloomId", state.getKey());
			stateTag.putString("State", state.getValue().name());
			stateList.add(stateTag);
		}
		tag.put("bloomStates", stateList);

		ListTag pendingList = new ListTag();
		for (Map.Entry<UUID, Integer> pending : pendingPomeByBloom.entrySet()) {
			CompoundTag pendingTag = new CompoundTag();
			pendingTag.putUUID("BloomId", pending.getKey());
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
		bloomStates.put(entry.bloomId(), SeveredQliphothState.LIVING);
		setDirty();
	}

	public List<BloomEntry> getBlooms() {
		return blooms;
	}

	/**
	 * Returns the number of pomes already dropped from the bloom at the given
	 * center position. Returns 0 if no pomes have been dropped yet.
	 */
	public int getPomesDropped(BloomEntry bloom) {
		return pomesDroppedByBloom.getOrDefault(bloom.bloomId(), 0);
	}

	/**
	 * Increments the pome-drop counter for the given bloom center and marks
	 * the data as dirty. Returns the new count after incrementing.
	 */
	public int incrementPomesDropped(BloomEntry bloom) {
		UUID key = bloom.bloomId();
		int next = pomesDroppedByBloom.getOrDefault(key, 0) + 1;
		pomesDroppedByBloom.put(key, next);
		setDirty();
		return next;
	}

	public SeveredQliphothState getState(BloomEntry bloom) {
		return bloomStates.getOrDefault(bloom.bloomId(), SeveredQliphothState.LIVING);
	}

	public boolean severBloom(BloomEntry bloom) {
		UUID key = bloom.bloomId();
		if (!blooms.contains(bloom)) return false;
		SeveredQliphothState next = getState(bloom).sever();
		bloomStates.put(key, next);
		pendingPomeByBloom.remove(key);
		claimedPendingPomes.remove(key);
		setDirty();
		return next.isPortalOpen();
	}

	public boolean sealBloom(BloomEntry bloom) {
		SeveredQliphothState next = getState(bloom).seal();
		bloomStates.put(bloom.bloomId(), next);
		setDirty();
		return next.isSealedTrophy();
	}

	public boolean hasPendingPome(BloomEntry bloom) {
		return pendingPomeByBloom.containsKey(bloom.bloomId());
	}

	public int getPendingPomeHuskIndex(BloomEntry bloom) {
		return pendingPomeByBloom.getOrDefault(bloom.bloomId(), -1);
	}

	public void setPendingPome(BloomEntry bloom, int huskIndex) {
		UUID key = bloom.bloomId();
		pendingPomeByBloom.put(key, huskIndex);
		claimedPendingPomes.remove(key);
		setDirty();
	}

	public void clearPendingPome(BloomEntry bloom) {
		UUID key = bloom.bloomId();
		if (pendingPomeByBloom.remove(key) != null) {
			claimedPendingPomes.remove(key);
			setDirty();
		}
	}

	public boolean isPendingPomeClaimed(BloomEntry bloom) {
		return claimedPendingPomes.contains(bloom.bloomId());
	}

	public void markPendingPomeClaimed(BloomEntry bloom) {
		if (pendingPomeByBloom.containsKey(bloom.bloomId())) {
			claimedPendingPomes.add(bloom.bloomId());
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
	public int getRemainingPomes(BloomEntry bloom) {
		return Math.max(0, MAX_POMES_PER_BLOOM - getPomesDropped(bloom));
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

	public BloomEntry getBloomById(UUID bloomId) {
		for (BloomEntry entry : blooms) if (entry.bloomId().equals(bloomId)) return entry;
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
				bloomStates.remove(entry.bloomId());
				pomesDroppedByBloom.remove(entry.bloomId());
				pendingPomeByBloom.remove(entry.bloomId());
				claimedPendingPomes.remove(entry.bloomId());
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
			int chunkRadius, long createdTick, UUID bloomId, boolean migratesLegacyProgress) {
		public BloomEntry(UUID ownerUUID, BlockPos center, String dimension, int chunkRadius, long createdTick) {
			this(ownerUUID, center, dimension, chunkRadius, createdTick, UUID.randomUUID(), false);
		}

		public BloomEntry(UUID ownerUUID, BlockPos center, String dimension, int chunkRadius,
				long createdTick, UUID bloomId) {
			this(ownerUUID, center, dimension, chunkRadius, createdTick, bloomId, false);
		}
	}
}
