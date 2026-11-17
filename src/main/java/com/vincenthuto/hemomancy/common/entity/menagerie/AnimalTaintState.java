package com.vincenthuto.hemomancy.common.entity.menagerie;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * What a Harbinger's blood has done to an animal (core-loop Track 5): how many doses of plain Projection it has
 * taken and whose school they carried (its taint), when it was last dosed, how long it stays feral, and, for the
 * offspring of a tainted parent, the weak second school it was born with. {@link AnimalTaint} owns every write.
 */
public final class AnimalTaintState implements INBTSerializable<CompoundTag> {
	public int doses;
	@Nullable
	public EnumBloodTemper school;
	public long lastDose = Long.MIN_VALUE;
	public long feralUntil = Long.MIN_VALUE;
	@Nullable
	public EnumBloodTemper inherited;

	public boolean tainted() {
		return doses > 0;
	}

	public boolean feral(long now) {
		return feralUntil != Long.MIN_VALUE && now < feralUntil;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (doses > 0) tag.putInt("doses", doses);
		if (school != null) tag.putString("school", school.name().toLowerCase(Locale.ROOT));
		if (lastDose != Long.MIN_VALUE) tag.putLong("last_dose", lastDose);
		if (feralUntil != Long.MIN_VALUE) tag.putLong("feral_until", feralUntil);
		if (inherited != null) tag.putString("inherited", inherited.name().toLowerCase(Locale.ROOT));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		doses = tag.getInt("doses");
		school = temper(tag.getString("school"));
		lastDose = tag.contains("last_dose") ? tag.getLong("last_dose") : Long.MIN_VALUE;
		feralUntil = tag.contains("feral_until") ? tag.getLong("feral_until") : Long.MIN_VALUE;
		inherited = temper(tag.getString("inherited"));
	}

	@Nullable
	private static EnumBloodTemper temper(String name) {
		if (name == null || name.isBlank()) return null;
		try {
			return EnumBloodTemper.valueOf(name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException unknown) {
			return null;
		}
	}
}
