package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundInit {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT,
			Hemomancy.MOD_ID);

	private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
		return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Hemomancy.rloc(name)));
	}


	//Items
	//Qliphoph
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_QLIPHOPH_POME_EAT = registerSoundEvent(
			"item.qliphoth_pome.eat");
	public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_QLIPHOPH_POME_COMMUNION= registerSoundEvent(
			"item.qliphoth_pome.communion");

	// ===== Abhorent Thought (psychic nightmare) =====
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABHORENT_THOUGHT_AMBIENT = registerSoundEvent(
			"entity.abhorent_thought.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABHORENT_THOUGHT_HURT = registerSoundEvent(
			"entity.abhorent_thought.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABHORENT_THOUGHT_DEATH = registerSoundEvent(
			"entity.abhorent_thought.death");

	// ===== Animals =====

	// Crimson Doe (graceful blood-veined deer)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRIMSON_DOE_AMBIENT = registerSoundEvent(
			"entity.crimson_doe.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRIMSON_DOE_HURT = registerSoundEvent(
			"entity.crimson_doe.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRIMSON_DOE_DEATH = registerSoundEvent(
			"entity.crimson_doe.death");

	// Venous Strider (elegant wading bird with visible veins)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VENOUS_STRIDER_AMBIENT = registerSoundEvent(
			"entity.venous_strider.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VENOUS_STRIDER_HURT = registerSoundEvent(
			"entity.venous_strider.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VENOUS_STRIDER_DEATH = registerSoundEvent(
			"entity.venous_strider.death");

	// Leech (small aquatic blood-feeder)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LEECH_AMBIENT = registerSoundEvent(
			"entity.leech.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LEECH_HURT = registerSoundEvent(
			"entity.leech.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LEECH_DEATH = registerSoundEvent(
			"entity.leech.death");

	// Fungling (small aggressive fungal creature)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FUNGLING_AMBIENT = registerSoundEvent(
			"entity.fungling.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FUNGLING_HURT = registerSoundEvent(
			"entity.fungling.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FUNGLING_DEATH = registerSoundEvent(
			"entity.fungling.death");

	// ===== Aquatic =====

	// Hemojelly (bioluminescent blood jellyfish)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOJELLY_AMBIENT = registerSoundEvent(
			"entity.hemojelly.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOJELLY_HURT = registerSoundEvent(
			"entity.hemojelly.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOJELLY_DEATH = registerSoundEvent(
			"entity.hemojelly.death");

	// Blood Lantern Jelly (warm-ocean reef drifter)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_LANTERN_JELLY_AMBIENT = registerSoundEvent(
			"entity.blood_lantern_jelly.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_LANTERN_JELLY_HURT = registerSoundEvent(
			"entity.blood_lantern_jelly.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_LANTERN_JELLY_DEATH = registerSoundEvent(
			"entity.blood_lantern_jelly.death");

	// Mnemonic Whale (reef megafauna)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MNEMONIC_WHALE_AMBIENT = registerSoundEvent(
			"entity.mnemonic_whale.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MNEMONIC_WHALE_HURT = registerSoundEvent(
			"entity.mnemonic_whale.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MNEMONIC_WHALE_DEATH = registerSoundEvent(
			"entity.mnemonic_whale.death");

	// Barbed Urchin (spiny aquatic creature)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BARBED_URCHIN_AMBIENT = registerSoundEvent(
			"entity.barbed_urchin.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BARBED_URCHIN_HURT = registerSoundEvent(
			"entity.barbed_urchin.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BARBED_URCHIN_DEATH = registerSoundEvent(
			"entity.barbed_urchin.death");

	// Chalybeate Snail (iron-sulfide vent grazer)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHALYBEATE_SNAIL_AMBIENT = registerSoundEvent(
			"entity.chalybeate_snail.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHALYBEATE_SNAIL_HURT = registerSoundEvent(
			"entity.chalybeate_snail.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHALYBEATE_SNAIL_DEATH = registerSoundEvent(
			"entity.chalybeate_snail.death");

	// Brined Votary (drowned Harbinger ship remnant)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BRINED_VOTARY_AMBIENT = registerSoundEvent(
			"entity.brined_votary.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BRINED_VOTARY_HURT = registerSoundEvent(
			"entity.brined_votary.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BRINED_VOTARY_DEATH = registerSoundEvent(
			"entity.brined_votary.death");

	// ===== Arthropods =====

	// Chitinite (armored insectoid, rolls into a ball)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHITINITE_AMBIENT = registerSoundEvent(
			"entity.chitinite.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHITINITE_HURT = registerSoundEvent(
			"entity.chitinite.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHITINITE_DEATH = registerSoundEvent(
			"entity.chitinite.death");

	// Fervent Chitinite (heated variant of chitinite)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FERVENT_CHITINITE_AMBIENT = registerSoundEvent(
			"entity.fervent_chitinite.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FERVENT_CHITINITE_HURT = registerSoundEvent(
			"entity.fervent_chitinite.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FERVENT_CHITINITE_DEATH = registerSoundEvent(
			"entity.fervent_chitinite.death");

	// Chthonian (cave-dwelling spider variant)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_AMBIENT = registerSoundEvent(
			"entity.chthonian.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_HURT = registerSoundEvent(
			"entity.chthonian.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_DEATH = registerSoundEvent(
			"entity.chthonian.death");

	// Chthonian Queen (matriarch spider)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_QUEEN_AMBIENT = registerSoundEvent(
			"entity.chthonian_queen.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_QUEEN_HURT = registerSoundEvent(
			"entity.chthonian_queen.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CHTHONIAN_QUEEN_DEATH = registerSoundEvent(
			"entity.chthonian_queen.death");

	// Hemolymphopoda (aquatic centipede-like arthropod)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOLYMPHOPODA_AMBIENT = registerSoundEvent(
			"entity.hemolymphopoda.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOLYMPHOPODA_HURT = registerSoundEvent(
			"entity.hemolymphopoda.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_HEMOLYMPHOPODA_DEATH = registerSoundEvent(
			"entity.hemolymphopoda.death");

	// Myelin Borer (nerve-burrowing cave grub)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYELIN_BORER_AMBIENT = registerSoundEvent(
			"entity.myelin_borer.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYELIN_BORER_HURT = registerSoundEvent(
			"entity.myelin_borer.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYELIN_BORER_DEATH = registerSoundEvent(
			"entity.myelin_borer.death");

	// Fargone (aggressive cave arthropod)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FARGONE_AMBIENT = registerSoundEvent(
			"entity.fargone.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FARGONE_HURT = registerSoundEvent(
			"entity.fargone.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FARGONE_DEATH = registerSoundEvent(
			"entity.fargone.death");

	// ===== Monsters =====

	// Thirster (blood-draining predator)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_THIRSTER_AMBIENT = registerSoundEvent(
			"entity.thirster.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_THIRSTER_HURT = registerSoundEvent(
			"entity.thirster.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_THIRSTER_DEATH = registerSoundEvent(
			"entity.thirster.death");

	// Lump of Thought (amorphous psychic horror)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LUMP_OF_THOUGHT_AMBIENT = registerSoundEvent(
			"entity.lump_of_thought.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LUMP_OF_THOUGHT_HURT = registerSoundEvent(
			"entity.lump_of_thought.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LUMP_OF_THOUGHT_DEATH = registerSoundEvent(
			"entity.lump_of_thought.death");

	// Erythromycelium Eruptus (erupting fungal horror)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ERYTHROMYCELIUM_ERUPTUS_AMBIENT = registerSoundEvent(
			"entity.erythromycelium_eruptus.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ERYTHROMYCELIUM_ERUPTUS_HURT = registerSoundEvent(
			"entity.erythromycelium_eruptus.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ERYTHROMYCELIUM_ERUPTUS_DEATH = registerSoundEvent(
			"entity.erythromycelium_eruptus.death");

	// Blood Drunk Puppeteer (deranged puppet-master)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_DRUNK_PUPPETEER_AMBIENT = registerSoundEvent(
			"entity.blood_drunk_puppeteer.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_DRUNK_PUPPETEER_HURT = registerSoundEvent(
			"entity.blood_drunk_puppeteer.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_DRUNK_PUPPETEER_DEATH = registerSoundEvent(
			"entity.blood_drunk_puppeteer.death");

	// Enthralled Doll (puppet construct)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ENTHRALLED_DOLL_AMBIENT = registerSoundEvent(
			"entity.enthralled_doll.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ENTHRALLED_DOLL_HURT = registerSoundEvent(
			"entity.enthralled_doll.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ENTHRALLED_DOLL_DEATH = registerSoundEvent(
			"entity.enthralled_doll.death");

	// Cruor Fiend (Nether blood fiend)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRUOR_FIEND_AMBIENT = registerSoundEvent(
			"entity.cruor_fiend.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRUOR_FIEND_HURT = registerSoundEvent(
			"entity.cruor_fiend.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_CRUOR_FIEND_DEATH = registerSoundEvent(
			"entity.cruor_fiend.death");

	// Void Drinker (End dimension blood siphoner)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VOID_DRINKER_AMBIENT = registerSoundEvent(
			"entity.void_drinker.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VOID_DRINKER_HURT = registerSoundEvent(
			"entity.void_drinker.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VOID_DRINKER_DEATH = registerSoundEvent(
			"entity.void_drinker.death");

	// Desiccant (desert scorpion blood-drainer)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_DESICCANT_AMBIENT = registerSoundEvent(
			"entity.desiccant.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_DESICCANT_HURT = registerSoundEvent(
			"entity.desiccant.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_DESICCANT_DEATH = registerSoundEvent(
			"entity.desiccant.death");

	// Synapse Hound (neural pack predator)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SYNAPSE_HOUND_AMBIENT = registerSoundEvent(
			"entity.synapse_hound.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SYNAPSE_HOUND_HURT = registerSoundEvent(
			"entity.synapse_hound.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_SYNAPSE_HOUND_DEATH = registerSoundEvent(
			"entity.synapse_hound.death");

	// Frozen Clot (animated frozen blood mass)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROZEN_CLOT_AMBIENT = registerSoundEvent(
			"entity.frozen_clot.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROZEN_CLOT_HURT = registerSoundEvent(
			"entity.frozen_clot.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_FROZEN_CLOT_DEATH = registerSoundEvent(
			"entity.frozen_clot.death");

	// Tooth Pecks (fungus-mimic spider-tooth creature)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_TOOTH_PECKS_AMBIENT = registerSoundEvent(
			"entity.tooth_pecks.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_TOOTH_PECKS_HURT = registerSoundEvent(
			"entity.tooth_pecks.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_TOOTH_PECKS_DEATH = registerSoundEvent(
			"entity.tooth_pecks.death");

	// Abyssal Siphon (deep dark blood horror)
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABYSSAL_SIPHON_AMBIENT = registerSoundEvent(
			"entity.abyssal_siphon.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABYSSAL_SIPHON_HURT = registerSoundEvent(
			"entity.abyssal_siphon.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ABYSSAL_SIPHON_DEATH = registerSoundEvent(
			"entity.abyssal_siphon.death");

	// ===== Endgame Bosses =====

	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VESPER_AMBIENT = registerSoundEvent(
			"entity.vesper.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VESPER_HURT = registerSoundEvent(
			"entity.vesper.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VESPER_DEATH = registerSoundEvent(
			"entity.vesper.death");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VESPER_HIT = registerSoundEvent(
			"entity.vesper.hit");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_VESPER_MUSIC = registerSoundEvent(
			"entity.vesper.music");

	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_AMBIENT = registerSoundEvent(
			"entity.mycophant.ambient");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_HURT = registerSoundEvent(
			"entity.mycophant.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_HURT_OTHER = registerSoundEvent(
			"entity.mycophant.hurtother");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_DEATH = registerSoundEvent(
			"entity.mycophant.death");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_SUMMON = registerSoundEvent(
			"entity.mycophant.summon");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_TELEPORT = registerSoundEvent(
			"entity.mycophant.teleport");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MYCOPHANT_MUSIC = registerSoundEvent(
			"entity.mycophant.music");

}
