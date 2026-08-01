package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.Objects;
import java.util.UUID;

public final class PuppeteerTrialRiteRules {
	public static final int MISSING_ENTITY_GRACE_TICKS = 40;

	public enum MediumStatus {
		READY,
		UNATTUNED,
		FOREIGN,
		ACTIVE_BODIES
	}

	private PuppeteerTrialRiteRules() {
	}

	public static MediumStatus mediumStatus(UUID boundOwner, UUID caster, boolean hasActiveBodies) {
		if (boundOwner == null) return MediumStatus.UNATTUNED;
		if (!boundOwner.equals(caster)) return MediumStatus.FOREIGN;
		return hasActiveBodies ? MediumStatus.ACTIVE_BODIES : MediumStatus.READY;
	}

	public static boolean matchesDeath(UUID expectedCaster, UUID expectedEntity, String expectedSummon,
			UUID actualCaster, UUID actualEntity, String actualSummon) {
		return Objects.equals(expectedCaster, actualCaster)
				&& Objects.equals(expectedEntity, actualEntity)
				&& Objects.equals(expectedSummon, actualSummon);
	}

	public static boolean missingEntityExpired(int missingTicks) {
		return missingTicks >= MISSING_ENTITY_GRACE_TICKS;
	}
}
