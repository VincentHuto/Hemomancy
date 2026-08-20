package com.vincenthuto.hemomancy.common.mission.alchemist;

import java.util.UUID;

public final class FirstSeparationSpinProof {
	private FirstSeparationSpinProof() {
	}

	public static boolean matches(UUID expectedPlayer, UUID expectedSpin, UUID outputPlayer, UUID outputSpin) {
		return expectedPlayer != null && expectedSpin != null
				&& expectedPlayer.equals(outputPlayer) && expectedSpin.equals(outputSpin);
	}
}
