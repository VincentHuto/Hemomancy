package com.vincenthuto.hemomancy.common.manipulation.family;

import java.util.List;

public record ManipulationFamilyDefinition(String baselineId, List<ManipulationFormDefinition> forms) {
	public ManipulationFamilyDefinition {
		forms = List.copyOf(forms);
	}
}
