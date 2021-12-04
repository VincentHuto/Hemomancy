package com.vincenthuto.hemomancy.capa.player.vascular;

import java.util.HashMap;
import java.util.Map;

public class VascularSystem implements IVascularSystem {
	@SuppressWarnings("serial")
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<EnumVeinSections, Float>() {
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

	public Map<EnumVeinSections, Float> getVascularSystem() {
		return vascularSystem;
	}

	public void setVascularSystem(Map<EnumVeinSections, Float> vascularSystem) {
		this.vascularSystem = vascularSystem;
	}

	public void setVascularSectionHealth(EnumVeinSections sectionIn, float value) {
		if (vascularSystem != null) {
			Map<EnumVeinSections, Float> newVasc = vascularSystem;
			newVasc.put(sectionIn, getHealthBySection(sectionIn) + value);
			setVascularSystem(newVasc);
		}
	}

	public float getHealthBySection(EnumVeinSections sectionIn) {
		if (vascularSystem != null && vascularSystem.get(sectionIn) != null) {
			return vascularSystem.get(sectionIn);
		} else {
			return 100;
		}
	}

	public EnumBloodFlow getBloodFlowBySection(EnumVeinSections sectionIn) {
		if (vascularSystem.get(sectionIn) >= 90) {
			return EnumBloodFlow.RAGING;
		} else if (vascularSystem.get(sectionIn) < 90 && vascularSystem.get(sectionIn) >= 75) {
			return EnumBloodFlow.FLOWING;

		} else if (vascularSystem.get(sectionIn) < 75 && vascularSystem.get(sectionIn) >= 50) {
			return EnumBloodFlow.STABLE;

		} else if (vascularSystem.get(sectionIn) < 50 && vascularSystem.get(sectionIn) >= 15) {
			return EnumBloodFlow.ClOTTED;

		} else if (vascularSystem.get(sectionIn) < 15 && vascularSystem.get(sectionIn) > 0) {
			return EnumBloodFlow.VARICOSE;
		} else if (vascularSystem.get(sectionIn) < 0) {
			return EnumBloodFlow.DEAD;
		}
		return EnumBloodFlow.STABLE;
	}

}
