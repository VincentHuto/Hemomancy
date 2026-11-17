package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.client.screen.overlay.HudVisibilityRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Every strain's passive effect drops out of vanilla's on-screen effect row while the hematic HUD's jar shows the
 * Morphling; the inventory's effect list keeps it. Registered by {@link ClientExtensionsRegistrar}.
 */
final class MorphlingEffectClientExtensions {

	private MorphlingEffectClientExtensions() {
	}

	static void register(RegisterClientExtensionsEvent event) {
		event.registerMobEffect(new IClientMobEffectExtensions() {
			@Override
			public boolean isVisibleInGui(MobEffectInstance instance) {
				return HudVisibilityRules.showsMorphlingEffectIcon(
						BloodVolumeOverlay.showsMorphlingJar(Minecraft.getInstance().player));
			}
		}, MorphlingStrains.ALL.stream().map(strain -> strain.passiveEffect().value()).distinct().toArray(MobEffect[]::new));
	}
}
