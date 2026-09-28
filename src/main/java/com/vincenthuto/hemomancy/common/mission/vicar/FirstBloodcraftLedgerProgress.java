package com.vincenthuto.hemomancy.common.mission.vicar;

public record FirstBloodcraftLedgerProgress(double absorbedMl, boolean formationProjected,
		boolean venousStoneProjected, boolean structureCrafted, boolean votaryReached) {
	public boolean readyForVicar() {
		return absorbedMl >= 500 && formationProjected && venousStoneProjected && structureCrafted;
	}

	public int completedProofs() {
		return (absorbedMl >= 500 ? 1 : 0) + (formationProjected ? 1 : 0)
				+ (venousStoneProjected ? 1 : 0) + (structureCrafted ? 1 : 0);
	}
}
