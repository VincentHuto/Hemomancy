package com.vincenthuto.hemomancy.common.unstained.stillarts;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StillArtRewardTable {
	private static final String ADV_CLARITY_AWAKENED = "hemomancy/clarity_awakened";
	private static final String ADV_DISCERNING = "hemomancy/discerning";
	private static final String ADV_VIGILANT = "hemomancy/vigilant";
	private static final String ADV_RESOLUTE_STAGE = "hemomancy/resolute_stage";
	private static final String ADV_ENLIGHTENED_SEEKER = "hemomancy/enlightened_seeker";

	private static final List<StageReward> REWARDS = List.of(
			new StageReward(EnumClarityStage.AWAKENED, "silver_rebuke"),
			new StageReward(EnumClarityStage.AWAKENED, "lethean_mute"),
			new StageReward(EnumClarityStage.DISCERNING, "still_pulse"),
			new StageReward(EnumClarityStage.DISCERNING, "pale_diagnosis"),
			new StageReward(EnumClarityStage.VIGILANT, "memory_shear"),
			new StageReward(EnumClarityStage.VIGILANT, "absolving_step"),
			new StageReward(EnumClarityStage.RESOLUTE, "quietus_bell"),
			new StageReward(EnumClarityStage.ENLIGHTENED, "autoimmune_edge"));

	private StillArtRewardTable() {
	}

	public static List<String> eligibleArtNames(EnumClarityStage stage) {
		List<String> names = new ArrayList<>();
		for (StageReward reward : REWARDS) {
			if (stage.getLevel() >= reward.stage().getLevel()) {
				names.add(reward.artName());
			}
		}
		return names;
	}

	public static Optional<EnumClarityStage> stageForAdvancement(ResourceLocation advancementId) {
		return stageForAdvancementPath(advancementId.getPath());
	}

	public static Optional<EnumClarityStage> stageForAdvancementPath(String advancementPath) {
		if (ADV_CLARITY_AWAKENED.equals(advancementPath)) {
			return Optional.of(EnumClarityStage.AWAKENED);
		}
		if (ADV_DISCERNING.equals(advancementPath)) {
			return Optional.of(EnumClarityStage.DISCERNING);
		}
		if (ADV_VIGILANT.equals(advancementPath)) {
			return Optional.of(EnumClarityStage.VIGILANT);
		}
		if (ADV_RESOLUTE_STAGE.equals(advancementPath)) {
			return Optional.of(EnumClarityStage.RESOLUTE);
		}
		if (ADV_ENLIGHTENED_SEEKER.equals(advancementPath)) {
			return Optional.of(EnumClarityStage.ENLIGHTENED);
		}
		return Optional.empty();
	}

	private record StageReward(EnumClarityStage stage, String artName) {
	}
}
