package com.vincenthuto.hemomancy.compat.mna.ritual;

import com.mna.Registries;
import com.mna.api.tools.RLoc;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraftforge.registries.RegisterEvent;

public class MnAPluginRitualInit {

	public static void registerRitualEffects(RegisterEvent event) {
		event.register(Registries.RitualEffect.get().getRegistryKey(), (helper) -> {
			helper.register(RLoc.create("weeping_wound"),
					new RitualEffectBloodMote(Hemomancy.rloc("rituals/weeping_wound")));
		});
	}

}
