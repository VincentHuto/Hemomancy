package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.UUID;

public interface BoundPuppeteerSummon {
	UUID hemomancy$getOwnerUUID();

	void hemomancy$setOwnerUUID(UUID ownerUuid);

	UUID hemomancy$getCrossbarUUID();

	void hemomancy$setCrossbarUUID(UUID crossbarUuid);

	String hemomancy$getSummonName();

	void hemomancy$setSummonName(String summonName);

	int hemomancy$getDismissalTicks();

	void hemomancy$setDismissalTicks(int ticks);

	boolean hemomancy$isTrialSummon();

	void hemomancy$setTrialSummon(boolean trialSummon);

	UUID hemomancy$getTrialCasterUUID();

	void hemomancy$setTrialCasterUUID(UUID casterUuid);
}
