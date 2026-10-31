package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.*;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import net.minecraft.resources.ResourceLocation;

/** The art for every frame id the server can name. */
public final class BossBarFrames {
	private static final Map<ResourceLocation, BossBarFrame> FRAMES = new HashMap<>();

	static {
        FRAMES.put(BossPresentationIds.FRAME_PHLEGETHOSOMA, new PhlegethosomaFrame());
		FRAMES.put(BossPresentationIds.FRAME_MYCOPHANT, new MycophantFrame());
		FRAMES.put(BossPresentationIds.FRAME_ANNETTA_KNOWLES, new AnnettaKnowlesFrame());
		FRAMES.put(BossPresentationIds.FRAME_STAINED_PRIESTESS, new StainedPriestessFrame());
		FRAMES.put(BossPresentationIds.FRAME_LATENT_INFECTION, new LatentInfectionFrame());
		FRAMES.put(BossPresentationIds.FRAME_NAEGLEROPHAEON, new NaeglerophaeonFrame());
		FRAMES.put(BossPresentationIds.FRAME_VESPER_CROWNED_REFUSAL, new VesperCrownedRefusalFrame());
		FRAMES.put(BossPresentationIds.FRAME_VESPER_EVENING_STAR, new VesperEveningStarFrame());
		FRAMES.put(BossPresentationIds.FRAME_HEMORATH, new HemorathFrame());
		FRAMES.put(BossPresentationIds.FRAME_PUTRICIEL, new PutricielFrame());
		FRAMES.put(BossPresentationIds.FRAME_VELORUM, new VelorumFrame());
		FRAMES.put(BossPresentationIds.FRAME_SERAPHAE, new SeraphaeFrame());
		FRAMES.put(BossPresentationIds.FRAME_BLOOM_WARDEN, new BloomWardenFrame());
		FRAMES.put(BossPresentationIds.FRAME_KINDLED_CHAMPION, new KindledChampionFrame());
	}

	private BossBarFrames() {
	}

	@Nullable
	public static BossBarFrame get(ResourceLocation id) {
		return FRAMES.get(id);
	}
}
