package com.vincenthuto.hemomancy.common.entity.boss.presentation;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules;
import net.minecraft.resources.ResourceLocation;

/**
 * The names the server and client share for boss presentation: each boss bar's frame, and each scene the director
 * plays. The client registers art and timing under these ids; the server only ever sends an id.
 */
public final class BossPresentationIds {
    public static final ResourceLocation FRAME_PHLEGETHOSOMA = Hemomancy.rloc("phlegethosoma");
    public static final ResourceLocation SCENE_PHLEGETHOSOMA = Hemomancy.rloc("phlegethosoma");
	public static final ResourceLocation FRAME_MYCOPHANT = Hemomancy.rloc("mycophant");
	public static final ResourceLocation FRAME_ANNETTA_KNOWLES = Hemomancy.rloc("annetta_knowles");
	public static final ResourceLocation FRAME_STAINED_PRIESTESS = Hemomancy.rloc("stained_priestess");
	public static final ResourceLocation FRAME_LATENT_INFECTION = Hemomancy.rloc("latent_annetta_infection");
	public static final ResourceLocation FRAME_NAEGLEROPHAEON = Hemomancy.rloc("naeglerophaeon");
	public static final ResourceLocation FRAME_VESPER_CROWNED_REFUSAL = Hemomancy.rloc("vesper_crowned_refusal");
	public static final ResourceLocation FRAME_VESPER_EVENING_STAR = Hemomancy.rloc("vesper_evening_star");
	public static final ResourceLocation FRAME_HEMORATH = Hemomancy.rloc("hemorath");
	public static final ResourceLocation FRAME_PUTRICIEL = Hemomancy.rloc("putriciel");
	public static final ResourceLocation FRAME_VELORUM = Hemomancy.rloc("velorum");
	public static final ResourceLocation FRAME_SERAPHAE = Hemomancy.rloc("seraphae");

	public static final ResourceLocation SCENE_MYCOPHANT_WAKING = Hemomancy.rloc("mycophant_waking");
	public static final ResourceLocation SCENE_MYCOPHANT_REMATCH = Hemomancy.rloc("mycophant_rematch");
	public static final ResourceLocation SCENE_MYCOPHANT_CAPTURE = Hemomancy.rloc("mycophant_capture");
	public static final ResourceLocation SCENE_MYCOPHANT_SPLIT_CARD = Hemomancy.rloc("mycophant_split");
	public static final ResourceLocation SCENE_ANNETTA_KNOWLES = Hemomancy.rloc("annetta_knowles");
	public static final ResourceLocation SCENE_ANNETTA_BREAK_CARD = Hemomancy.rloc("annetta_break");
	public static final ResourceLocation SCENE_STAINED_PRIESTESS = Hemomancy.rloc("stained_priestess");
	public static final ResourceLocation SCENE_LATENT_INFECTION = Hemomancy.rloc("latent_annetta_infection");
	public static final ResourceLocation SCENE_NAEGLEROPHAEON = Hemomancy.rloc("naeglerophaeon");
	public static final ResourceLocation SCENE_NAEGLEROPHAEON_TRANSITION_CARD = Hemomancy.rloc("naeglerophaeon_transition");
	public static final ResourceLocation SCENE_VESPER_CROWNED_REFUSAL = Hemomancy.rloc("vesper_crowned_refusal");
	public static final ResourceLocation SCENE_VESPER_COCOON_CARD = Hemomancy.rloc("vesper_cocoon");
	public static final ResourceLocation SCENE_VESPER_EVENING_STAR = Hemomancy.rloc("vesper_evening_star");
	public static final ResourceLocation SCENE_SAINT_RISE = Hemomancy.rloc("saint_rise");
	public static final ResourceLocation SCENE_HEMORATH_EMPTIES_CARD = Hemomancy.rloc("hemorath_empties");
	public static final ResourceLocation SCENE_POSSESSION_DEPARTURE = Hemomancy.rloc("possession_departure");
	public static final ResourceLocation SCENE_POSSESSION_ARRIVAL = Hemomancy.rloc("possession_arrival");
	public static final ResourceLocation SCENE_POSSESSION_BROKEN_GARDENS = Hemomancy.rloc("possession_broken_gardens");
	public static final ResourceLocation SCENE_POSSESSION_BROKEN_RITE = Hemomancy.rloc("possession_broken_rite");
	public static final ResourceLocation SCENE_POSSESSION_CLEAN_GARDENS = Hemomancy.rloc("possession_clean_gardens");
	public static final ResourceLocation SCENE_POSSESSION_CLEAN_RITE = Hemomancy.rloc("possession_clean_rite");

	public static final List<ResourceLocation> FRAMES = List.of(FRAME_PHLEGETHOSOMA, FRAME_MYCOPHANT, FRAME_ANNETTA_KNOWLES,
			FRAME_STAINED_PRIESTESS, FRAME_LATENT_INFECTION, FRAME_NAEGLEROPHAEON, FRAME_VESPER_CROWNED_REFUSAL,
			FRAME_VESPER_EVENING_STAR, FRAME_HEMORATH, FRAME_PUTRICIEL, FRAME_VELORUM, FRAME_SERAPHAE);

	public static final List<ResourceLocation> SCENES = List.of(SCENE_PHLEGETHOSOMA, SCENE_MYCOPHANT_WAKING, SCENE_MYCOPHANT_REMATCH,
			SCENE_MYCOPHANT_CAPTURE, SCENE_MYCOPHANT_SPLIT_CARD, SCENE_ANNETTA_KNOWLES, SCENE_ANNETTA_BREAK_CARD,
			SCENE_STAINED_PRIESTESS, SCENE_LATENT_INFECTION, SCENE_NAEGLEROPHAEON, SCENE_NAEGLEROPHAEON_TRANSITION_CARD,
			SCENE_VESPER_CROWNED_REFUSAL, SCENE_VESPER_COCOON_CARD, SCENE_VESPER_EVENING_STAR, SCENE_SAINT_RISE,
			SCENE_HEMORATH_EMPTIES_CARD, SCENE_POSSESSION_DEPARTURE, SCENE_POSSESSION_ARRIVAL, SCENE_POSSESSION_BROKEN_GARDENS,
			SCENE_POSSESSION_BROKEN_RITE, SCENE_POSSESSION_CLEAN_GARDENS, SCENE_POSSESSION_CLEAN_RITE);

	/** The director scene for one half of a possession's cinematic. */
	public static ResourceLocation possessionScene(PossessionSceneRules.Scene scene) {
		return switch (scene) {
			case DEPARTURE -> SCENE_POSSESSION_DEPARTURE;
			case ARRIVAL -> SCENE_POSSESSION_ARRIVAL;
			case BROKEN_GARDENS -> SCENE_POSSESSION_BROKEN_GARDENS;
			case BROKEN_RITE -> SCENE_POSSESSION_BROKEN_RITE;
			case CLEAN_GARDENS -> SCENE_POSSESSION_CLEAN_GARDENS;
			case CLEAN_RITE -> SCENE_POSSESSION_CLEAN_RITE;
		};
	}

	private BossPresentationIds() {
	}
}
