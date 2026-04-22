package com.vincenthuto.hemomancy.common.capability.player.vascular;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class VascularSystem implements IVascularSystem, INBTSerializable<CompoundTag> {
	@SuppressWarnings("serial")
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>() {
		{
			put(EnumVeinSections.HEAD, 100f);
			put(EnumVeinSections.HEART, 100f);
			put(EnumVeinSections.BODY, 100f);
			put(EnumVeinSections.LEFTARM, 100f);
			put(EnumVeinSections.RIGHTARM, 100f);
			put(EnumVeinSections.LEFTLEG, 100f);
			put(EnumVeinSections.RIGHTLEG, 100f);
		}
	};

	@Override
	public EnumBloodFlow getBloodFlowBySection(EnumVeinSections sectionIn) {
		if (vascularSystem.get(sectionIn) >= 90) {
			return EnumBloodFlow.RAGING;
		} else if (vascularSystem.get(sectionIn) < 90 && vascularSystem.get(sectionIn) >= 75) {
			return EnumBloodFlow.FLOWING;

		} else if (vascularSystem.get(sectionIn) < 75 && vascularSystem.get(sectionIn) >= 50) {
			return EnumBloodFlow.STABLE;

		} else if (vascularSystem.get(sectionIn) < 50 && vascularSystem.get(sectionIn) >= 15) {
			return EnumBloodFlow.VARICOSE;

		} else if (vascularSystem.get(sectionIn) < 15 && vascularSystem.get(sectionIn) > 0) {
			return EnumBloodFlow.ClOTTED;
		} else if (vascularSystem.get(sectionIn) < 0) {
			return EnumBloodFlow.DEAD;
		}
		return EnumBloodFlow.STABLE;
	}

	@Override
	public float getHealthBySection(EnumVeinSections sectionIn) {
		if (vascularSystem != null && vascularSystem.get(sectionIn) != null) {
			return vascularSystem.get(sectionIn);
		} else {
			return 100;
		}
	}

	@Override
	public Map<EnumVeinSections, Float> getVascularSystem() {
		return vascularSystem;
	}

	@Override
	public void setVascularSectionHealth(EnumVeinSections sectionIn, float value) {
		if (vascularSystem != null) {
			Map<EnumVeinSections, Float> newVasc = vascularSystem;
			newVasc.put(sectionIn, getHealthBySection(sectionIn) + value);
			setVascularSystem(newVasc);
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
			vascularSystem.put(key, nbt.getFloat(key.toString()));
		}
	}

}
