package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneRegistry;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;

/** Every boss scene the client knows, under the ids the server plays. */
public final class BossSceneCatalog {
	private BossSceneCatalog() {
	}

	public static void registerAll() {
        BossSceneRegistry.register(BossPresentationIds.SCENE_PHLEGETHOSOMA, PhlegethosomaScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_MYCOPHANT_WAKING, MycophantWakingScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_MYCOPHANT_REMATCH, MycophantRematchScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_MYCOPHANT_CAPTURE, MycophantCaptureScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_ANNETTA_KNOWLES, AnnettaKnowlesScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_STAINED_PRIESTESS, StainedPriestessScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_LATENT_INFECTION, LatentInfectionScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_NAEGLEROPHAEON, NaeglerophaeonScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_VESPER_CROWNED_REFUSAL, VesperCrownedRefusalScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_VESPER_EVENING_STAR, VesperEveningStarScene::new);
		BossSceneRegistry.register(BossPresentationIds.SCENE_SAINT_RISE, SaintRiseScene::new);

		BossSceneRegistry.register(BossPresentationIds.SCENE_ANNETTA_BREAK_CARD, () -> new PhaseCardScene("annetta_break", 0xD6EAF6));
		BossSceneRegistry.register(BossPresentationIds.SCENE_MYCOPHANT_SPLIT_CARD, () -> new PhaseCardScene("mycophant_split", 0xFFA828));
		BossSceneRegistry.register(BossPresentationIds.SCENE_NAEGLEROPHAEON_TRANSITION_CARD,
				() -> new PhaseCardScene("naeglerophaeon_transition", 0xAA35FF));
		BossSceneRegistry.register(BossPresentationIds.SCENE_VESPER_COCOON_CARD, () -> new PhaseCardScene("vesper_cocoon", 0xE00018));
		BossSceneRegistry.register(BossPresentationIds.SCENE_HEMORATH_EMPTIES_CARD, () -> new PhaseCardScene("hemorath_empties", 0xB080E0));
	}

	/** Whether a scene is one of the Mycophant's taking: a CLEAR from the encounter ends those and nothing else. */
	public static boolean isMycophantTaking(BossScene scene) {
		return scene instanceof MycophantWakingScene || scene instanceof MycophantRematchScene
				|| scene instanceof MycophantCaptureScene;
	}
}
