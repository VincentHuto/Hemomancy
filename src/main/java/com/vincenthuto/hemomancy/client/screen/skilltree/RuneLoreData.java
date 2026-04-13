package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-rune flavour text displayed in the Runes tab of the Skill Tree.
 * <p>
 * Keys are the trailing segment of the chisel recipe's ResourceLocation path
 * (e.g. {@code "rune_heart"} for {@code hemomancy:chisel/rune_heart}).
 */
public final class RuneLoreData {

	private RuneLoreData() {}

	private static final Map<String, String> LORE = new HashMap<>();

	static {
		LORE.put("rune_anvil",
				"Shaped by blood spilled upon iron, the Anvil rune tempers the practitioner's will "
				+ "into an unyielding edge. What bends in others remains rigid in you.");

		LORE.put("rune_blight",
				"The Blight rune was first conceived in a fevered vein — a record of infection that "
				+ "the body never truly forgets. It turns the practitioner's own toxins outward.");

		LORE.put("rune_chimera",
				"Two bloods, one vessel. The Chimera rune fuses contradictions within the circulatory "
				+ "system, allowing tendencies that should never coexist to occupy the same body.");

		LORE.put("rune_corona",
				"The blood remembers light. The Corona rune awakens the photosensitive proteins "
				+ "sleeping in the practitioner's veins, drawing power from radiance itself.");

		LORE.put("rune_crucible",
				"Pain is the forge and blood is the ore. The Crucible rune refines suffering into "
				+ "strength, ensuring nothing the practitioner endures is wasted.");

		LORE.put("rune_descendence",
				"Some practitioners sink willingly. The Descendence rune aligns the blood with the "
				+ "deep currents of the earth, granting stability at the cost of upward motion.");

		LORE.put("rune_eye",
				"Vision carved directly into the hemomantic lattice. The Eye rune does not sharpen "
				+ "sight — it opens channels that perceive what the eyes were never designed to see.");

		LORE.put("rune_feral",
				"Before the Rites were codified, there was only hunger. The Feral rune strips away "
				+ "doctrine and returns the practitioner to the primal state — untamed, ravenous, alive.");

		LORE.put("rune_flux",
				"The blood has no fixed form. The Flux rune reminds the practitioner of this truth, "
				+ "allowing the circulatory system to shift and redistribute power moment to moment.");

		LORE.put("rune_glacier",
				"Patience carved in cold blood. The Glacier rune slows the pulse deliberately, "
				+ "trading speed for endurance and heat for crystalline permanence.");

		LORE.put("rune_halo",
				"The oldest practitioners spoke of a ring of light that follows those who have mastered "
				+ "their blood. The Halo rune does not grant holiness — it merely signals that something "
				+ "holy has been sacrificed.");

		LORE.put("rune_heart",
				"The first rune, and the most feared. The Heart rune speaks directly to the organ at "
				+ "the centre of all hemomantic practice — awakening its full, terrible potential.");

		LORE.put("rune_ichor",
				"Not all blood is mortal. The Ichor rune coaxes the divine trace elements latent in "
				+ "every living vein, briefly elevating the practitioner above their station.");

		LORE.put("rune_marrow",
				"Power runs deeper than the blood. The Marrow rune taps the reserves buried within "
				+ "bone, drawing on strength the practitioner did not know they had.");

		LORE.put("rune_moon",
				"The tides answer to no practitioner, yet the Moon rune aligns the blood with their "
				+ "rhythm. What ebbs will flood again; what floods can be directed.");

		LORE.put("rune_oblivion",
				"The Oblivion rune does not destroy — it erases. Impressions carved into the "
				+ "hemomantic lattice by this mark leave no echo, no memory, no trace.");

		LORE.put("rune_phoenix",
				"To burn completely is to be reborn without remainder. The Phoenix rune encodes the "
				+ "pattern of the practitioner's blood so perfectly that even catastrophic loss cannot "
				+ "extinguish it.");

		LORE.put("rune_pyre",
				"Sacrifice is not merely symbolic in hemomancy. The Pyre rune channels offerings "
				+ "directly into the blood, converting loss into momentum.");

		LORE.put("rune_rime",
				"Cold enough to stop a pulse but not to end one. The Rime rune encases specific "
				+ "functions of the circulatory system in frost, preserving them perfectly for use "
				+ "at a later moment.");

		LORE.put("rune_shade",
				"The Shade rune does not darken the world — it darkens the practitioner. The blood "
				+ "grows quiet, the presence grows thin, and what remains is harder for anything to find.");

		LORE.put("rune_sol",
				"The Sol rune was carved by sun-worshippers who found that the blood responds to heat "
				+ "long before the mind does. The warmth it channels is not comforting — it is relentless.");

		LORE.put("rune_thorn",
				"Every wound teaches something. The Thorn rune ensures that those who strike the "
				+ "practitioner also learn, turning the act of harm into a two-edged lesson.");

		LORE.put("rune_transcendence",
				"There are limits etched into every practitioner's blood — inherited ceilings, "
				+ "ancestral walls. The Transcendence rune burns them away. What lies beyond is the "
				+ "practitioner's to discover.");

		LORE.put("rune_veil",
				"The Veil rune does not make things invisible. It makes them unconvincing — too "
				+ "ordinary to investigate, too forgettable to pursue. The blood learns to hide in plain sight.");

		LORE.put("rune_wither",
				"The Wither rune is not destruction — it is reduction. It drains the vital excess "
				+ "from a target, leaving just enough for the practitioner to claim what remains.");
	}

	/**
	 * Returns the lore description for the given chisel recipe path segment,
	 * or a generic fallback if none is registered.
	 *
	 * @param recipePathKey the last segment of the chisel recipe's ResourceLocation path
	 *                      (e.g. {@code "rune_heart"})
	 */
	public static String getLore(String recipePathKey) {
		return LORE.getOrDefault(recipePathKey,
				"Runes carve new venous and nervous pathways in the mind, "
				+ "opening the practitioner to tendencies once sealed away.");
	}
}
