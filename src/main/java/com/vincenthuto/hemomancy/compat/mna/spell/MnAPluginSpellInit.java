package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.Registries;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraftforge.registries.RegisterEvent;

public class MnAPluginSpellInit {

	public static void registerSpellBits(RegisterEvent event) {
		//Shapes
		event.register(Registries.Shape.get().getRegistryKey(), (helper) -> {
			helper.register(Hemomancy.rloc("shapes/pork_pulse"),
					new ShapePorkPulse(Hemomancy.rloc("textures/mna/pork_pulse.png")));
		});
		//Components
		event.register(Registries.SpellEffect.get().getRegistryKey(), (helper) -> {
			helper.register(Hemomancy.rloc("components/dodo"), new ComponentDodo(Hemomancy.rloc("textures/mna/dodo.png")));
			helper.register(Hemomancy.rloc("components/pork_warrior"),
					new ComponentPorkWarrior(Hemomancy.rloc("textures/mna/pork_warrior.png")));
			helper.register(Hemomancy.rloc("components/sanguine_fertility"),
					new ComponentSanguineFertility(Hemomancy.rloc("textures/mna/sanguine_fertility.png")));
			helper.register(Hemomancy.rloc("components/mana_to_blood"),
					new ComponentManaToBlood(Hemomancy.rloc("textures/mna/mana_to_blood.png")));
			helper.register(Hemomancy.rloc("components/blood_binding"),
					new ComponentBloodBinding(Hemomancy.rloc("textures/mna/blood_binding.png")));
		});
	}
}
