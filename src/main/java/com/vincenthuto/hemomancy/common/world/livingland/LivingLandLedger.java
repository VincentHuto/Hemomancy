package com.vincenthuto.hemomancy.common.world.livingland;

import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandTerrainRules;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Every real block Living Land has taken out of its place in this level and not yet put back (core-loop Track 9).
 * This, not the strike entity or the player's session, is the truth about a lifted block: a strike carries a record,
 * a raise holds one, and each touches it every tick. A record nothing has touched for {@link #ORPHAN_TICKS} (its
 * strike unloaded or was removed, the server restarted, the player logged out) is settled by the sweep. Nothing is
 * ever dropped as an item: a block goes back to the cell it came from, or the nearest free cell when that is taken,
 * and waits for its chunk when the chunk is not loaded.
 *
 * <p>Settling is a restoration, so it ends in a plain {@code setBlock} when the owner is away or their place check
 * is refused: the block was the world's before the lift and must not be lost to a claim that would also refuse the
 * return. When the owner is here it goes through {@link PlayerBlockEdits#settle} first, so the place event fires.
 */
public final class LivingLandLedger extends SavedData {
	private static final String DATA_NAME = "hemomancy_living_land";
	private static final SavedData.Factory<LivingLandLedger> FACTORY =
			new SavedData.Factory<>(LivingLandLedger::new, LivingLandLedger::load, null);
	/** A record untouched this long is an orphan and the sweep settles it. */
	public static final int ORPHAN_TICKS = 40;
	/** How far a block looks for a free cell when its own is taken. */
	public static final int DISPLACED_RADIUS = 4;

	/**
	 * One lifted block: the cell it came from, its state, and where it stands now when it stands in the world (a raised
	 * step), or null while a strike carries it.
	 */
	public record Lifted(BlockPos origin, BlockState state, @Nullable BlockPos at) {
		public Lifted {
			origin = origin.immutable();
			at = at == null ? null : at.immutable();
		}
	}

	private static final class Record {
		final UUID owner;
		final List<Lifted> blocks = new ArrayList<>();

		Record(UUID owner) {
			this.owner = owner;
		}
	}

	private final Map<UUID, Record> records = new LinkedHashMap<>();
	/** Last game time each record was touched; transient, so a restart leaves every record an orphan. */
	private final Map<UUID, Long> touched = new HashMap<>();

	public static LivingLandLedger get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	/** Opens a record for {@code owner} and returns its id. */
	public UUID open(UUID owner, long now) {
		UUID id = UUID.randomUUID();
		records.put(id, new Record(owner));
		touched.put(id, now);
		setDirty();
		return id;
	}

	public void add(UUID id, Lifted lifted) {
		Record record = records.get(id);
		if (record == null) return;
		record.blocks.add(lifted);
		setDirty();
	}

	/** Whether {@code id} still holds blocks (or is open and empty). */
	public boolean holds(UUID id) {
		return id != null && records.containsKey(id);
	}

	public void touch(UUID id, long now) {
		if (records.containsKey(id)) touched.put(id, now);
	}

	/** The blocks a record holds, in lift order. */
	public List<Lifted> blocks(UUID id) {
		Record record = records.get(id);
		return record == null ? List.of() : List.copyOf(record.blocks);
	}

	public int size() {
		return records.size();
	}

	/** How many blocks every record in this level holds; for tests. */
	public int heldBlocks() {
		return records.values().stream().mapToInt(record -> record.blocks.size()).sum();
	}

	/**
	 * Puts every block of {@code id} back: a raised block is taken from where it stands (only if it is still there:
	 * one someone mined is theirs) and set at its origin, or the nearest free cell. Returns true when the record is
	 * gone; false when some block waits for its chunk, in which case those blocks stay recorded.
	 */
	public boolean settle(ServerLevel level, UUID id, @Nullable ServerPlayer owner) {
		Record record = records.get(id);
		if (record == null) return true;
		List<Lifted> waiting = new ArrayList<>();
		for (int i = record.blocks.size() - 1; i >= 0; i--) {
			Lifted left = settleOne(level, record.blocks.get(i), owner);
			if (left != null) waiting.add(0, left);
		}
		record.blocks.clear();
		record.blocks.addAll(waiting);
		if (waiting.isEmpty()) {
			records.remove(id);
			touched.remove(id);
		}
		setDirty();
		return waiting.isEmpty();
	}

	/** Settles one block; returns what is still owed (null when done). A raised block taken down but not yet set is carried. */
	@Nullable
	private static Lifted settleOne(ServerLevel level, Lifted lifted, @Nullable ServerPlayer owner) {
		if (!level.isLoaded(lifted.origin()) || (lifted.at() != null && !level.isLoaded(lifted.at()))) return lifted;
		if (lifted.at() != null) {
			if (!level.getBlockState(lifted.at()).equals(lifted.state())) return null;
			level.setBlock(lifted.at(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
		return restore(level, lifted.origin(), lifted.state(), owner) ? null
				: new Lifted(lifted.origin(), lifted.state(), null);
	}

	/**
	 * Sets {@code state} at {@code origin}, or the nearest free cell; false when nowhere loaded is free yet. Also the way
	 * a lift that could not be placed goes straight back.
	 */
	public static boolean restore(ServerLevel level, BlockPos origin, BlockState state, @Nullable ServerPlayer owner) {
		Optional<BlockPos> cell = level.getBlockState(origin).canBeReplaced() ? Optional.of(origin)
				: LivingLandTerrainRules.nearestFree(origin, DISPLACED_RADIUS,
						pos -> level.isLoaded(pos) && !level.isOutsideBuildHeight(pos)
								&& level.getBlockState(pos).canBeReplaced());
		if (cell.isEmpty()) return false;
		BlockPos at = cell.get();
		if (owner != null && !owner.isRemoved() && PlayerBlockEdits.settle(owner, at, state)) return true;
		level.setBlock(at, state, Block.UPDATE_ALL);
		return true;
	}

	/** Settles every record nothing has touched for {@link #ORPHAN_TICKS}; the owner is found if online. */
	public void sweep(ServerLevel level, long now) {
		for (UUID id : List.copyOf(records.keySet())) {
			long last = touched.getOrDefault(id, Long.MIN_VALUE);
			if (last != Long.MIN_VALUE && now - last < ORPHAN_TICKS) continue;
			Record record = records.get(id);
			ServerPlayer owner = level.getServer().getPlayerList().getPlayer(record.owner);
			settle(level, id, owner);
		}
	}

	public static LivingLandLedger load(CompoundTag tag, HolderLookup.Provider provider) {
		LivingLandLedger ledger = new LivingLandLedger();
		HolderLookup<Block> blocks = provider.lookupOrThrow(Registries.BLOCK);
		ListTag list = tag.getList("records", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag saved = list.getCompound(i);
			Record record = new Record(saved.getUUID("owner"));
			ListTag entries = saved.getList("blocks", Tag.TAG_COMPOUND);
			for (int j = 0; j < entries.size(); j++) {
				CompoundTag entry = entries.getCompound(j);
				BlockState state = NbtUtils.readBlockState(blocks, entry.getCompound("state"));
				BlockPos at = entry.contains("at") ? BlockPos.of(entry.getLong("at")) : null;
				record.blocks.add(new Lifted(BlockPos.of(entry.getLong("origin")), state, at));
			}
			ledger.records.put(saved.getUUID("id"), record);
		}
		return ledger;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		records.forEach((id, record) -> {
			CompoundTag saved = new CompoundTag();
			saved.putUUID("id", id);
			saved.putUUID("owner", record.owner);
			ListTag entries = new ListTag();
			for (Lifted lifted : record.blocks) {
				CompoundTag entry = new CompoundTag();
				entry.putLong("origin", lifted.origin().asLong());
				entry.put("state", NbtUtils.writeBlockState(lifted.state()));
				if (lifted.at() != null) entry.putLong("at", lifted.at().asLong());
				entries.add(entry);
			}
			saved.put("blocks", entries);
			list.add(saved);
		});
		tag.put("records", list);
		return tag;
	}
}
