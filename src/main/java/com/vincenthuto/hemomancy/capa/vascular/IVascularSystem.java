package com.vincenthuto.hemomancy.capa.vascular;

import java.util.Map;

public interface IVascularSystem {

	public Map<EnumVeinSections, Float> getVascularSystem();

	public void setVascularSystem(Map<EnumVeinSections, Float> vascularSystem);

	public void setVascularSectionHealth(EnumVeinSections sectionIn, float value);

	public float getHealthBySection(EnumVeinSections sectionIn);

	public EnumBloodFlow getBloodFlowBySection(EnumVeinSections sectionIn);

}
