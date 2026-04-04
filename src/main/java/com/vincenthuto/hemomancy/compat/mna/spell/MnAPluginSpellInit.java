package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.Registries;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.HemoSpellIconCompositor;

import net.minecraftforge.registries.RegisterEvent;

import static com.vincenthuto.hemomancy.compat.mna.HemoSpellIconCompositor.borderedIcon;

public class MnAPluginSpellInit {

	public static void registerSpellBits(RegisterEvent event) {
		//Shapes
		event.register(Registries.Shape.get().getRegistryKey(), (helper) -> {
//			helper.register(Hemomancy.rloc("shapes/pork_pulse"),
//					new ShapePorkPulse(borderedIcon(Hemomancy.rloc("textures/mna/pork_pulse.png"))));
		});
		//Components
		event.register(Registries.SpellEffect.get().getRegistryKey(), (helper) -> {
//			helper.register(Hemomancy.rloc("components/dodo"),
//					new ComponentDodo(borderedIcon(Hemomancy.rloc("textures/mna/dodo.png"))));
//			helper.register(Hemomancy.rloc("components/pork_warrior"),
//					new ComponentPorkWarrior(borderedIcon(Hemomancy.rloc("textures/mna/pork_warrior.png"))));
			helper.register(Hemomancy.rloc("components/sanguine_fertility"),
					new ComponentSanguineFertility(borderedIcon(Hemomancy.rloc("textures/mna/sanguine_fertility.png"))));
			helper.register(Hemomancy.rloc("components/mana_to_blood"),
					new ComponentManaToBlood(borderedIcon(Hemomancy.rloc("textures/mna/mana_to_blood.png"))));
			helper.register(Hemomancy.rloc("components/blood_binding"),
					new ComponentBloodBinding(borderedIcon(Hemomancy.rloc("textures/mna/blood_binding.png"))));
		});
	}
}
