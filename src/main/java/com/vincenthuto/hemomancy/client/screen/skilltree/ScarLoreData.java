package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-scar flavour text displayed in the Scars tab of the Skill Tree.
 * <p>
 * Keys are the trailing segment of the chisel recipe's ResourceLocation path
 * (e.g. {@code "scar_heart"} for {@code hemomancy:chisel/scar_heart}).
 */
public final class ScarLoreData {

	private ScarLoreData() {}

	private static final Map<String, String> LORE = new HashMap<>();

	static {
		LORE.put("scar_anvil",
				"Shaped by blood spilled upon iron, the Anvil scar tempers the practitioner's will "
				+ "into an unyielding edge. What bends in others remains rigid in you.");

		LORE.put("scar_blight",
				"The Blight scar was first conceived in a fevered vein — a record of infection that "
				+ "the body never truly forgets. It turns the practitioner's own toxins outward.");

		LORE.put("scar_chimera",
				"Two bloods, one vessel. The Chimera scar fuses contradictions within the circulatory "
				+ "system, allowing tendencies that should never coexist to occupy the same body.");

		LORE.put("scar_corona",
				"The blood remembers light. The Corona scar awakens the photosensitive proteins "
				+ "sleeping in the practitioner's veins, drawing power from radiance itself.");

		LORE.put("scar_crucible",
				"Pain is the forge and blood is the ore. The Crucible scar refines suffering into "
				+ "strength, ensuring nothing the practitioner endures is wasted.");

		LORE.put("scar_descendence",
				"Some practitioners sink willingly. The Descendence scar aligns the blood with the "
				+ "deep currents of the earth, granting stability at the cost of upward motion.");

		LORE.put("scar_eye",
				"Vision carved directly into the hemomantic lattice. The Eye scar does not sharpen "
				+ "sight — it opens channels that perceive what the eyes were never designed to see.");

		LORE.put("scar_feral",
				"Before the Rites were codified, there was only hunger. The Feral scar strips away "
				+ "doctrine and returns the practitioner to the primal state — untamed, ravenous, alive.");

		LORE.put("scar_flux",
				"The blood has no fixed form. The Flux scar reminds the practitioner of this truth, "
				+ "allowing the circulatory system to shift and redistribute power moment to moment.");

		LORE.put("scar_glacier",
				"Patience carved in cold blood. The Glacier scar slows the pulse deliberately, "
				+ "trading speed for endurance and heat for crystalline permanence.");

		LORE.put("scar_halo",
				"The oldest practitioners spoke of a ring of light that follows those who have mastered "
				+ "their blood. The Halo scar does not grant holiness — it merely signals that something "
				+ "holy has been sacrificed.");

		LORE.put("scar_heart",
				"The first scar, and the most feared. The Heart scar speaks directly to the organ at "
				+ "the centre of all hemomantic practice — awakening its full, terrible potential.");

		LORE.put("scar_ichor",
				"Not all blood is mortal. The Ichor scar coaxes the divine trace elements latent in "
				+ "every living vein, briefly elevating the practitioner above their station.");

		LORE.put("scar_marrow",
				"Power runs deeper than the blood. The Marrow scar taps the reserves buried within "
				+ "bone, drawing on strength the practitioner did not know they had.");

		LORE.put("scar_moon",
				"The tides answer to no practitioner, yet the Moon scar aligns the blood with their "
				+ "rhythm. What ebbs will flood again; what floods can be directed.");

		LORE.put("scar_oblivion",
				"The Oblivion scar does not destroy — it erases. Impressions carved into the "
				+ "hemomantic lattice by this mark leave no echo, no memory, no trace.");

		LORE.put("scar_phoenix",
				"To burn completely is to be reborn without remainder. The Phoenix scar encodes the "
				+ "pattern of the practitioner's blood so perfectly that even catastrophic loss cannot "
				+ "extinguish it.");

		LORE.put("scar_pyre",
				"Sacrifice is not merely symbolic in hemomancy. The Pyre scar channels offerings "
				+ "directly into the blood, converting loss into momentum.");

		LORE.put("scar_rime",
				"Cold enough to stop a pulse but not to end one. The Rime scar encases specific "
				+ "functions of the circulatory system in frost, preserving them perfectly for use "
				+ "at a later moment.");

		LORE.put("scar_shade",
				"The Shade scar does not darken the world — it darkens the practitioner. The blood "
				+ "grows quiet, the presence grows thin, and what remains is harder for anything to find.");

		LORE.put("scar_sol",
				"The Sol scar was carved by sun-worshippers who found that the blood responds to heat "
				+ "long before the mind does. The warmth it channels is not comforting — it is relentless.");

		LORE.put("scar_thorn",
				"Every wound teaches something. The Thorn scar ensures that those who strike the "
				+ "practitioner also learn, turning the act of harm into a two-edged lesson.");

		LORE.put("scar_transcendence",
				"There are limits etched into every practitioner's blood — inherited ceilings, "
				+ "ancestral walls. The Transcendence scar burns them away. What lies beyond is the "
				+ "practitioner's to discover.");

		LORE.put("scar_veil",
				"The Veil scar does not make things invisible. It makes them unconvincing — too "
				+ "ordinary to investigate, too forgettable to pursue. The blood learns to hide in plain sight.");

		LORE.put("scar_wither",
				"The Wither scar is not destruction — it is reduction. It drains the vital excess "
				+ "from a target, leaving just enough for the practitioner to claim what remains.");

		// ── Fungal scars — these are not carved so much as cultivated ──

		LORE.put("respergillus",
				"Named for a mould that colonises grain stores, the Respergillus scar draws moisture "
				+ "from the practitioner's blood and redistributes it. You may breathe underwater — "
				+ "but something in the gills feels borrowed, not given.");

		LORE.put("talaromyces_minus",
				"Talaromyces Minus is a soil-dweller, a quiet decomposer of dead matter. Etched into "
				+ "the hemomantic lattice it accelerates the hands, quickens the pulse of labour — "
				+ "as if the scar is impatient for you to break things down.");

		LORE.put("lumina_devorans",
				"Noctilumina Devorans. 'The light-eater.' This scar does not merely grant vision in "
				+ "the dark — it feeds on illumination, growing stronger in the absence of light. "
				+ "Those who wear it too long begin to find daylight... irritating.");

		LORE.put("noctifly_agaric",
				"The Noctifly Agaric cap unfurls wings of pale mycelium in response to the wearer's "
				+ "will. Flight granted by fungus. The practitioner hovers effortlessly, but the "
				+ "sensation is less like flying and more like being carried.");
	}

	/**
	 * Returns the lore description for the given chisel recipe path segment,
	 * or a generic fallback if none is registered.
	 *
	 * @param recipePathKey the last segment of the chisel recipe's ResourceLocation path
	 *                      (e.g. {@code "scar_heart"})
	 */
	public static String getLore(String recipePathKey) {
		return LORE.getOrDefault(recipePathKey,
				"Scars carve new venous and nervous pathways in the mind, "
				+ "opening the practitioner to tendencies once sealed away.");
	}
}
