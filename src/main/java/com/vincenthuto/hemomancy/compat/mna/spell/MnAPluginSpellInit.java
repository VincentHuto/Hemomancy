package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.Registries;
import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.vincenthuto.hemomancy.compat.mna.HemoSpellIconCompositor.borderedIcon;

public class MnAPluginSpellInit {

	public static void registerSpellBits(RegisterEvent event) {
		event.register(Registries.Shape.get().getRegistryKey(), (helper) -> {
		});
		event.register(Registries.SpellEffect.get().getRegistryKey(), (helper) -> {
			helper.register(Hemomancy.rloc("components/sanguine_fertility"),
					new ComponentSanguineFertility(borderedIcon(Hemomancy.rloc("textures/mna/sanguine_fertility.png"))));
			helper.register(Hemomancy.rloc("components/mana_to_blood"),
					new ComponentManaToBlood(borderedIcon(Hemomancy.rloc("textures/mna/mana_to_blood.png"))));
			helper.register(Hemomancy.rloc("components/blood_binding"),
					new ComponentBloodBinding(borderedIcon(Hemomancy.rloc("textures/mna/blood_binding.png"))));
			helper.register(Hemomancy.rloc("components/blood_loss"),
					new ComponentBloodLoss(borderedIcon(Hemomancy.rloc("textures/mna/blood_loss.png"))));
			helper.register(Hemomancy.rloc("components/blood_rush"),
					new ComponentBloodRush(borderedIcon(Hemomancy.rloc("textures/mna/blood_rush.png"))));
			helper.register(Hemomancy.rloc("components/hemolysis"),
					new ComponentHemolysis(borderedIcon(Hemomancy.rloc("textures/mna/hemolysis.png"))));
			helper.register(Hemomancy.rloc("components/summon_sanguilith"),
					new ComponentSummonSanguilith(borderedIcon(Hemomancy.rloc("textures/mna/summon_sanguilith.png"))));
		});
	}
}
