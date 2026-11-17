package com.vincenthuto.hemomancy.common.world.answer;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Blood spilled, counted (core-loop Track 10): a level's decaying total per region ({@link WorldAnswerRules}). A
 * region settles lazily against the level clock, so nothing ticks; it is forgotten when it fades out and holds no ward.
 * Wards are the Suspended Vivianite blocks standing in a region, recorded when placed and removed, and checked against
 * the world whenever their chunk is loaded. Saved with the level as {@code hemomancy_blood_spilled}.
 */
public final class BloodSpilledLedger extends SavedData {
	private static final String DATA_NAME = "hemomancy_blood_spilled";
	private static final SavedData.Factory<BloodSpilledLedger> FACTORY =
			new SavedData.Factory<>(BloodSpilledLedger::new, BloodSpilledLedger::load, null);

	private static final class Region {
		double ml;
		long settledAt;
		final Set<Long> wards = new HashSet<>();

		Region(double ml, long settledAt) {
			this.ml = ml;
			this.settledAt = settledAt;
		}
	}

	private final Map<Long, Region> regions = new HashMap<>();

	public static BloodSpilledLedger get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	// ── Reading ──

	/** The blood counted in the region holding {@code pos}, settled to now. */
	public static double total(ServerLevel level, BlockPos pos) {
		Region region = get(level).settled(level, pos, false);
		return region == null ? 0.0D : region.ml;
	}

	public static int band(ServerLevel level, BlockPos pos) {
		return WorldAnswerRules.band(total(level, pos));
	}

	public static double pressure(ServerLevel level, BlockPos pos) {
		return WorldAnswerRules.pressure(total(level, pos));
	}

	/** The wards standing in the region holding {@code pos}. */
	public static int wards(ServerLevel level, BlockPos pos) {
		Region region = get(level).settled(level, pos, false);
		return region == null ? 0 : region.wards.size();
	}

	// ── Writing ──

	/** Counts {@code ml} of blood left at {@code pos}, halved per ward in its region. Returns what was counted. */
	public static double spill(ServerLevel level, BlockPos pos, double ml) {
		if (!(ml > 0.0D)) return 0.0D;
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, true);
		double counted = WorldAnswerRules.counted(ml, region.wards.size());
		region.ml = WorldAnswerRules.add(region.ml, counted);
		data.setDirty();
		return counted;
	}

	/**
	 * A kill's spill of {@code ml} at {@code pos}, halved per ward, counted only up to the kill share's cap
	 * ({@link WorldAnswerRules#killCounted}). Returns what was counted.
	 */
	public static double spillKill(ServerLevel level, BlockPos pos, double ml) {
		if (!(ml > 0.0D)) return 0.0D;
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, true);
		double counted = WorldAnswerRules.killCounted(region.ml, WorldAnswerRules.counted(ml, region.wards.size()));
		if (counted <= 0.0D) return 0.0D;
		region.ml = WorldAnswerRules.add(region.ml, counted);
		data.setDirty();
		return counted;
	}

	/** Takes {@code ml} back off the count where blood was cleaned up at {@code pos}. */
	public static void clean(ServerLevel level, BlockPos pos, double ml) {
		if (!(ml > 0.0D)) return;
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, false);
		if (region == null) return;
		region.ml = WorldAnswerRules.add(region.ml, -ml);
		data.forgetIfEmpty(pos, region);
		data.setDirty();
	}

	/** Sets the region holding {@code pos} to {@code ml} (fixtures and review scenes). */
	public static void set(ServerLevel level, BlockPos pos, double ml) {
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, true);
		region.ml = WorldAnswerRules.add(0.0D, ml);
		data.forgetIfEmpty(pos, region);
		data.setDirty();
	}

	/** Moves the region's last settling back {@code ticks}, as if that long had passed (tests). */
	public static void backdate(ServerLevel level, BlockPos pos, long ticks) {
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, false);
		if (region == null) return;
		region.settledAt -= Math.max(0L, ticks);
		data.setDirty();
	}

	public static void addWard(ServerLevel level, BlockPos pos) {
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, true);
		if (region.wards.add(pos.asLong())) data.setDirty();
	}

	public static void removeWard(ServerLevel level, BlockPos pos) {
		BloodSpilledLedger data = get(level);
		Region region = data.settled(level, pos, false);
		if (region != null && region.wards.remove(pos.asLong())) {
			data.forgetIfEmpty(pos, region);
			data.setDirty();
		}
	}

	// ── Settling ──

	private Region settled(ServerLevel level, BlockPos pos, boolean create) {
		long key = WorldAnswerRules.regionKey(pos.getX(), pos.getZ());
		long now = level.getGameTime();
		Region region = regions.get(key);
		if (region == null) {
			if (!create) return null;
			region = new Region(0.0D, now);
			regions.put(key, region);
			setDirty();
			return region;
		}
		if (dropFallenWards(level, region)) setDirty();
		if (now != region.settledAt) {
			region.ml = WorldAnswerRules.decay(region.ml, now - region.settledAt, region.wards.size());
			region.settledAt = now;
			setDirty();
		}
		if (!create && region.ml <= 0.0D && region.wards.isEmpty()) {
			regions.remove(key);
			return null;
		}
		return region;
	}

	/** Forgets a recorded ward whose loaded chunk no longer holds one. */
	private static boolean dropFallenWards(ServerLevel level, Region region) {
		boolean changed = false;
		for (Iterator<Long> it = region.wards.iterator(); it.hasNext(); ) {
			BlockPos ward = BlockPos.of(it.next());
			if (level.isLoaded(ward) && !level.getBlockState(ward).is(BlockInit.suspended_vivianite.get())) {
				it.remove();
				changed = true;
			}
		}
		return changed;
	}

	private void forgetIfEmpty(BlockPos pos, Region region) {
		if (region.ml <= 0.0D && region.wards.isEmpty()) regions.remove(WorldAnswerRules.regionKey(pos.getX(), pos.getZ()));
	}

	// ── Saving ──

	public static BloodSpilledLedger load(CompoundTag tag, HolderLookup.Provider provider) {
		BloodSpilledLedger data = new BloodSpilledLedger();
		ListTag list = tag.getList("regions", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			Region region = new Region(entry.getDouble("ml"), entry.getLong("settled_at"));
			for (long ward : entry.getLongArray("wards")) region.wards.add(ward);
			data.regions.put(entry.getLong("key"), region);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		regions.forEach((key, region) -> {
			CompoundTag entry = new CompoundTag();
			entry.putLong("key", key);
			entry.putDouble("ml", region.ml);
			entry.putLong("settled_at", region.settledAt);
			if (!region.wards.isEmpty()) {
				entry.put("wards", new LongArrayTag(region.wards.stream().mapToLong(Long::longValue).toArray()));
			}
			list.add(entry);
		});
		tag.put("regions", list);
		return tag;
	}
}
