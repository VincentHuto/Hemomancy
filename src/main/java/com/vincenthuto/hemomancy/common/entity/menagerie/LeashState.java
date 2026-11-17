package com.vincenthuto.hemomancy.common.entity.menagerie;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A creature on the Tendon Line (core-loop Track 5), kept on the creature as an attachment so the leash outlasts
 * the creature's chunk unloading, a relog and a restart: who leads it, whether its own targeting was already off
 * before the leash (so release puts back what was there), and since when its keeper has been out of reach.
 * {@link MenagerieLeash} owns every read and write.
 */
public final class LeashState implements INBTSerializable<CompoundTag> {
	public static final long NEVER = Long.MIN_VALUE;

	/** One leash. {@code awaySince} is {@link #NEVER} while the keeper is within reach. */
	public record Leash(UUID keeper, boolean targetDisabled, long awaySince) {
		public Leash away(long since) {
			return new Leash(keeper, targetDisabled, since);
		}
	}

	@Nullable
	private Leash leash;

	@Nullable
	public Leash leash() {
		return leash;
	}

	public void set(@Nullable Leash leash) {
		this.leash = leash;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (leash == null) return tag;
		tag.putUUID("keeper", leash.keeper());
		tag.putBoolean("target_disabled", leash.targetDisabled());
		if (leash.awaySince() != NEVER) tag.putLong("away_since", leash.awaySince());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		leash = tag.hasUUID("keeper") ? new Leash(tag.getUUID("keeper"), tag.getBoolean("target_disabled"),
				tag.contains("away_since") ? tag.getLong("away_since") : NEVER) : null;
	}
}
