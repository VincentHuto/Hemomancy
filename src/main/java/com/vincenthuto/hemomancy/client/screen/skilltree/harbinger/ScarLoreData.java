package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

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
				"The Anvil does not merely harden — it fixes. The blood's iron locks into the joints, "
				+ "the joints lock into the earth, and the practitioner becomes immovable. "
				+ "They also become slow. This is not a coincidence.");

		LORE.put("scar_blight",
				"The Blight scar turns the practitioner's own toxins outward. What the body would purge "
				+ "is redirected — but redirection is not mastery. Occasionally the venom reads its bearer "
				+ "as the wound, and corrects accordingly.");

		LORE.put("scar_chimera",
				"Two bodies, one motion. The Chimera scar accelerates everything — the strike, the step, "
				+ "the heartbeat. The vessel does not always appreciate the pace. Armour becomes a "
				+ "liability; the body eats itself to sustain the speed.");

		LORE.put("scar_corona",
				"The blood runs so hot that the armour begins to blister. The Corona scar is the end "
				+ "stage of Flammeus discipline: total offence, absolute exposure. The veins burn "
				+ "continuously. Most practitioners report they barely notice.");

		LORE.put("scar_crucible",
				"The Crucible demands everything and gives back iron. The practitioner becomes "
				+ "a wall — impenetrable, immovable, and very, very slow. Even the attack slows. "
				+ "The Crucible does not care about speed. It has endured long enough.");

		LORE.put("scar_descendence",
				"Some practitioners sink willingly. The Descendence scar aligns the blood with the "
				+ "deep cold — extraordinary speed, extraordinary control over the frozen field — "
				+ "but the hands grow distant and the strikes grow weak. You cannot be everywhere at once.");

		LORE.put("scar_eye",
				"Vision carved into the hemomantic lattice at tremendous cost. The Eye scar opens "
				+ "channels that perceive what the eyes were never designed to see — and the blood "
				+ "pays for the privilege, tick by tick. The night holds no secrets. "
				+ "The blood holds fewer reserves.");

		LORE.put("scar_feral",
				"Before the Rites were codified, there was only hunger. The Feral scar strips away "
				+ "doctrine and returns the practitioner to the primal state — faster, more ravenous. "
				+ "The skin, which once had the good sense to stop a blade, becomes an afterthought.");

		LORE.put("scar_flux",
				"The blood has no fixed form. The Flux scar reminds the practitioner of this truth "
				+ "constantly — the strike quickens, the kill-chain accelerates — but the armour "
				+ "loosens with the form. Fluidity has a cost.");

		LORE.put("scar_glacier",
				"Patience carved in cold blood. The Glacier scar slows the practitioner's own pulse "
				+ "deliberately, trading the speed of attack for the ability to command the frozen "
				+ "field around them. The monsters slow; the hands slow with them.");

		LORE.put("scar_halo",
				"The oldest practitioners spoke of a ring of light that follows those who have endured. "
				+ "The Halo scar does not grant holiness — it hardens the body and turns the strike "
				+ "back on those who dare. Movement costs more than it did before.");

		LORE.put("scar_heart",
				"The first scar, and the most honest. The Heart scar grants more life — but the blood "
				+ "pool constricts. The practitioner lives longer and spends less. "
				+ "Whether this is mercy or cruelty depends on what follows.");

		LORE.put("scar_marrow",
				"Power runs deeper than the blood. The Marrow scar taps the reserves buried within "
				+ "bone — more life, a kill that heals — but the deep draw constricts the blood pool "
				+ "and weighs on the step. Deep things are not moved quickly.");

		LORE.put("scar_moon",
				"The tides answer to no practitioner, yet the Moon scar aligns the blood with their "
				+ "rhythm. What ebbs empowers the step. What flows weakens the fist. "
				+ "The shadow grows swift and the blow grows quiet.");

		LORE.put("scar_oblivion",
				"The Oblivion scar does not merely reduce — it erases. The targets wither and bleed; "
				+ "the practitioner's own health erodes; the blood drains continuously. "
				+ "Nothing is taken without something being surrendered first.");

		LORE.put("scar_phoenix",
				"To burn completely is to be reborn without remainder. The Phoenix scar carries "
				+ "tremendous vitality — healing on every kill, emergency regeneration when wounded — "
				+ "but the blood pool constricts severely and the body slows. Life and blood "
				+ "are not the same resource.");

		LORE.put("scar_pyre",
				"Sacrifice is not merely symbolic in hemomancy. The Pyre scar sharpens the edge "
				+ "by stripping away protection. The practitioner strikes harder. "
				+ "They also bleed more easily. This is, apparently, acceptable.");

		LORE.put("scar_rime",
				"Cold enough to quicken the step but not enough to stop the hands — almost. "
				+ "The Rime scar accelerates movement and slows foes, but the ice in the wrists "
				+ "costs time off every strike. The body cannot freeze and flow at once.");

		LORE.put("scar_shade",
				"The Shade scar quickens the step and darkens the presence — at the cost of force. "
				+ "The practitioner moves through darkness like thought, and strikes like a thought "
				+ "that has been politely reconsidered.");

		LORE.put("scar_sol",
				"The Sol scar was carved by sun-worshippers who found that the blood responds to "
				+ "heat long before the mind does. The strike sharpens; the guard drops. "
				+ "The warmth it channels is not comforting — it is relentless, and careless.");

		LORE.put("scar_thorn",
				"Every wound teaches something. The Thorn scar ensures that those who strike the "
				+ "practitioner also learn — but iron in the blood is weight in the legs. "
				+ "The lesson is mutual.");

		LORE.put("scar_transcendence",
				"There are limits etched into every practitioner's blood — inherited ceilings, "
				+ "ancestral walls. The Transcendence scar burns them away, leaving toughness "
				+ "and searing brightness. The blood pool thins. The feet slow. "
				+ "What lies beyond is the practitioner's to discover — slowly, with less to spend.");

		LORE.put("scar_veil",
				"The Veil scar does not make things invisible. It makes the practitioner "
				+ "unconvincing — too radiant to approach, too blinding to pursue. "
				+ "The blood learns to hide in plain light. The body learns to stop moving quickly.");

		LORE.put("scar_wither",
				"The Wither scar is not destruction — it is reduction. It drains the vital excess "
				+ "from a target, leaving just enough for the practitioner to claim what remains. "
				+ "The scar claims something in return: a portion of the vessel's own health.");

		// ── Fungal scars — cultivated rather than carved ──

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
