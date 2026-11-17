package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodTemper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Temper: what a Harbinger is carrying of each school right now, beside {@link BloodTemperament} (what they have
 * done). Filled by drawing from living creatures and by dissolving catalysts, burned by Sculptings and casts, halved by
 * death. The amounts are bounded by {@link TemperRules#cap}, which reads alignment; the holder does not know
 * alignment, so callers pass the cap. {@code seen} records every school the player has held or run dry in, for the HUD.
 * {@code bandolierGiven} records the Alchemist's one Titration Bandolier (CQ-63); it survives death with the rest.
 */
public class BloodTemper implements INBTSerializable<CompoundTag> {
	private static final EnumBloodTemper[] SCHOOLS = EnumBloodTemper.values();
	private final float[] pool = new float[SCHOOLS.length];
	private int seen;
	private boolean bandolierGiven;
	private transient boolean syncDirty;
	private transient long lastNoticeTick = Long.MIN_VALUE;

	public float get(EnumBloodTemper school) {
		return school == null ? 0.0F : pool[school.ordinal()];
	}

	/** Sets a school's pool, no lower than 0 and no higher than {@code cap}. */
	public void set(EnumBloodTemper school, float value, float cap) {
		if (school == null) return;
		float clamped = Float.isFinite(value) ? Math.clamp(value, 0.0F, Math.max(0.0F, cap)) : 0.0F;
		int index = school.ordinal();
		if (clamped > 0.0F) markSeen(school);
		if (Math.abs(pool[index] - clamped) > 1.0E-5F) {
			pool[index] = clamped;
			syncDirty = true;
		}
	}

	/** Adds {@code amount} to a school, up to {@code cap}. Returns what went in. */
	public float add(EnumBloodTemper school, float amount, float cap) {
		if (school == null || !(amount > 0.0F)) return 0.0F;
		float before = get(school);
		set(school, before + amount, Math.max(cap, before));
		return get(school) - before;
	}

	public boolean hasSeen(EnumBloodTemper school) {
		return school != null && (seen & (1 << school.ordinal())) != 0;
	}

	/** A school the HUD shows from now on: one the player has held, or run dry in. */
	public void markSeen(EnumBloodTemper school) {
		if (school == null) return;
		int bit = 1 << school.ordinal();
		if ((seen & bit) == 0) {
			seen |= bit;
			syncDirty = true;
		}
	}

	public int seenMask() {
		return seen;
	}

	public boolean bandolierGiven() {
		return bandolierGiven;
	}

	public void markBandolierGiven() {
		bandolierGiven = true;
	}

	/** Every school at {@link TemperRules#afterDeath}. */
	public void afterDeath() {
		for (EnumBloodTemper school : SCHOOLS) {
			float kept = TemperRules.afterDeath(get(school));
			if (kept != get(school)) {
				pool[school.ordinal()] = kept;
				syncDirty = true;
			}
		}
	}

	/** Whether a notice (dry or catalyst) may show at {@code gameTime}; at most one every {@code gap} ticks. */
	public boolean takeNotice(long gameTime, int gap) {
		if (lastNoticeTick != Long.MIN_VALUE && gameTime - lastNoticeTick < gap && gameTime >= lastNoticeTick) return false;
		lastNoticeTick = gameTime;
		return true;
	}

	/** Client mirror of the owner's pool. */
	public void setFromSync(float[] amounts, int seenMask) {
		for (int i = 0; i < SCHOOLS.length && i < amounts.length; i++) {
			pool[i] = Float.isFinite(amounts[i]) ? Math.max(0.0F, amounts[i]) : 0.0F;
		}
		seen = seenMask;
	}

	public float[] snapshot() {
		return pool.clone();
	}

	public boolean isSyncDirty() {
		return syncDirty;
	}

	public void sync(ServerPlayer player, boolean force) {
		if (!force && !syncDirty) return;
		PacketHandler.sendToPlayer(player, new PacketSyncBloodTemper(snapshot(), seen));
		syncDirty = false;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		for (EnumBloodTemper school : SCHOOLS) {
			if (pool[school.ordinal()] > 0.0F) tag.putFloat(school.name(), pool[school.ordinal()]);
		}
		tag.putInt("seen", seen);
		if (bandolierGiven) tag.putBoolean("bandolier_given", true);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		for (EnumBloodTemper school : SCHOOLS) {
			float value = tag.getFloat(school.name());
			pool[school.ordinal()] = Float.isFinite(value) ? Math.max(0.0F, value) : 0.0F;
		}
		seen = tag.getInt("seen");
		bandolierGiven = tag.getBoolean("bandolier_given");
		syncDirty = true;
	}
}
