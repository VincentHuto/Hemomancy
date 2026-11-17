package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodTemperament implements IBloodTemperament, INBTSerializable<CompoundTag> {
	private Map<EnumBloodTemper, Float> temper = new HashMap<>(Map.of(
			EnumBloodTemper.ANIMUS, 0f,
			EnumBloodTemper.MORTEM, 0f,
			EnumBloodTemper.DUCTILIS, 0f,
			EnumBloodTemper.FERRIC, 0f,
			EnumBloodTemper.LUX, 0f,
			EnumBloodTemper.TENEBRIS, 0f,
			EnumBloodTemper.FLAMMEUS, 0f,
			EnumBloodTemper.CONGEATIO, 0f));
	private final Map<EnumBloodTemper, Float> transientAlignment = new HashMap<>();

	@Override
	public void addTemperAlignment(EnumBloodTemper temperIn, float value) {
		if (temperIn != null) {
			temper.put(temperIn, temper.getOrDefault(temperIn, 0F) + value);
		}
	}

	@Override
	public void addTransientAlignment(EnumBloodTemper temperIn, float value) {
		transientAlignment.merge(temperIn, value, Float::sum);
		if (Math.abs(transientAlignment.get(temperIn)) < 0.0001F) {
			transientAlignment.remove(temperIn);
		}
	}

	@Override
	public void setTransientAlignments(Map<EnumBloodTemper, Float> scars) {
		transientAlignment.clear();
		if (scars == null) return;
		scars.forEach((school, value) -> {
			if (school != null && value != null && Math.abs(value) >= 0.0001F) transientAlignment.put(school, value);
		});
	}

	@Override
	public float getAlignmentByTemper(EnumBloodTemper temperIn) {
		if (temper == null) return 0;
		Float alignment = temper.get(temperIn);
		return (alignment == null ? 0 : alignment) + transientAlignment.getOrDefault(temperIn, 0F);
	}

	@Override
	public float getBaseAlignment(EnumBloodTemper temperIn) {
		if (temper == null) return 0;
		Float alignment = temper.get(temperIn);
		return alignment == null ? 0 : alignment;
	}

	@Override
	public ParticleColor getAvgBloodColor() {
		List<ParticleColor> colors = new ArrayList<>();
		for (EnumBloodTemper tend : EnumBloodTemper.values()) {
			float percent = getPercentByTemper(tend);
			colors.add(new ParticleColor(tend.getColor().getRed() * percent, tend.getColor().getBlue() * percent,
					tend.getColor().getGreen() * percent));
		}
		return ParticleColor.averageFromList(colors);

	}

	@Override
	public float getPercentByTemper(EnumBloodTemper temperIn) {
		float total = getTotalAlignment();
		if (Math.abs(total) < 0.0001F) return 0.0F;
		return getAlignmentByTemper(temperIn) / total * 100.0F;
	}

	@Override
	public Map<EnumBloodTemper, Float> getTemper() {
		Map<EnumBloodTemper, Float> effective = new HashMap<>();
		for (EnumBloodTemper value : EnumBloodTemper.values()) {
			effective.put(value, getAlignmentByTemper(value));
		}
		return effective;
	}

	@Override
	public float getTotalAlignment() {
		float runningTotal = 0;
		for (EnumBloodTemper key : EnumBloodTemper.values()) {
			runningTotal += getAlignmentByTemper(key);
		}
		return runningTotal;

	}

	@Override
	public void setTemper(Map<EnumBloodTemper, Float> temper) {
		// Copied into a map of its own with every temper present: the caller's map may be immutable or partial.
		Map<EnumBloodTemper, Float> copy = new EnumMap<>(EnumBloodTemper.class);
		for (EnumBloodTemper value : EnumBloodTemper.values()) {
			Float given = temper == null ? null : temper.get(value);
			copy.put(value, given == null ? 0.0F : given);
		}
		this.temper = copy;
	}

	@Override
	public void setTemperAlignment(EnumBloodTemper temperIn, float value) {
		if (temperIn != null) {
			temper.put(temperIn, value);
		}
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag covenTag = new CompoundTag();
		temper.keySet().forEach(key -> {
			Float val = temper.get(key);
			covenTag.putFloat(key.toString(), val != null ? val : 0f);
		});
		return covenTag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		for (EnumBloodTemper coven : EnumBloodTemper.values()) {
			temper.put(coven, nbt.getFloat(coven.toString()));
		}
	}

}
