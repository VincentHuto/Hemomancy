package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class VascularSystem implements IVascularSystem, INBTSerializable<CompoundTag> {
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>(Map.of(
			EnumVeinSections.HEAD, 100f,
			EnumVeinSections.HEART, 100f,
			EnumVeinSections.BODY, 100f,
			EnumVeinSections.ARMS, 100f,
			EnumVeinSections.LEGS, 100f));

	@Override
	public EnumBloodFlow getBloodFlowBySection(EnumVeinSections sectionIn) {
		float health = getHealthBySection(sectionIn);
		if (health >= 90) {
			return EnumBloodFlow.RAGING;
		} else if (health < 90 && health >= 75) {
			return EnumBloodFlow.FLOWING;

		} else if (health < 75 && health >= 50) {
			return EnumBloodFlow.STABLE;

		} else if (health < 50 && health >= 15) {
			return EnumBloodFlow.VARICOSE;

		} else if (health < 15 && health > 0) {
			return EnumBloodFlow.ClOTTED;
		} else if (health <= 0) {
			return EnumBloodFlow.DEAD;
		}
		return EnumBloodFlow.STABLE;
	}

	@Override
	public float getHealthBySection(EnumVeinSections sectionIn) {
		if (vascularSystem == null) return 100;
		Float health = vascularSystem.get(sectionIn);
		return health == null ? 100 : health;
	}

	@Override
	public Map<EnumVeinSections, Float> getVascularSystem() {
		return vascularSystem;
	}

	@Override
	public void setVascularSectionHealth(EnumVeinSections sectionIn, float value) {
		if (vascularSystem != null) {
			vascularSystem.put(sectionIn, getHealthBySection(sectionIn) + value);
		}
	}

	@Override
	public void setVascularSystem(Map<EnumVeinSections, Float> vascularSystem) {
		this.vascularSystem = vascularSystem;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		for (EnumVeinSections key : vascularSystem.keySet()) {
			Float val = vascularSystem.get(key);
			tag.putFloat(key.toString(), val != null ? val : 100f);
		}
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		for (EnumVeinSections key : EnumVeinSections.values()) {
			if (nbt.contains(key.toString())) {
				vascularSystem.put(key, nbt.getFloat(key.toString()));
			}
		}

		boolean hasLeftArm = nbt.contains("LEFTARM");
		boolean hasRightArm = nbt.contains("RIGHTARM");
		if (hasLeftArm || hasRightArm) {
			float leftArm = hasLeftArm ? nbt.getFloat("LEFTARM") : 100f;
			float rightArm = hasRightArm ? nbt.getFloat("RIGHTARM") : 100f;
			vascularSystem.put(EnumVeinSections.ARMS, Math.min(leftArm, rightArm));
		}

		boolean hasLeftLeg = nbt.contains("LEFTLEG");
		boolean hasRightLeg = nbt.contains("RIGHTLEG");
		if (hasLeftLeg || hasRightLeg) {
			float leftLeg = hasLeftLeg ? nbt.getFloat("LEFTLEG") : 100f;
			float rightLeg = hasRightLeg ? nbt.getFloat("RIGHTLEG") : 100f;
			vascularSystem.put(EnumVeinSections.LEGS, Math.min(leftLeg, rightLeg));
		}
	}

}
