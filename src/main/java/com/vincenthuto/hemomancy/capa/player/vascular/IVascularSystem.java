package com.vincenthuto.hemomancy.capa.player.vascular;

import java.util.Map;

public interface IVascularSystem {

	public EnumBloodFlow getBloodFlowBySection(EnumVeinSections sectionIn);

	public float getHealthBySection(EnumVeinSections sectionIn);

	public Map<EnumVeinSections, Float> getVascularSystem();

	public void setVascularSectionHealth(EnumVeinSections sectionIn, float value);

	public void setVascularSystem(Map<EnumVeinSections, Float> vascularSystem);

}
