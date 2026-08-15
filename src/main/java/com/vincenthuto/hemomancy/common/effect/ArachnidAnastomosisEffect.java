package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

/**
 * A beneficial effect that slowly repairs vascular damage across all vein
 * sections. Applied by the Bootlace Morphling while it is attached to the player.
 * Each tick, every damaged section is healed by a small amount that scales with
 * the amplifier.
 */
public class ArachnidAnastomosisEffect extends MobEffect {
	private final String displayKey;

	public ArachnidAnastomosisEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.arachnid_anastomosis");
	}

	public ArachnidAnastomosisEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;
		if (!(entity instanceof Player player)) return true;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			float healAmount = 0.5f + amplifier * 0.25f;
			boolean changed = false;

			for (EnumVeinSections section : EnumVeinSections.values()) {
				float health = vascular.getHealthBySection(section);
				if (health < 100f) {
					float newHealth = Math.min(100f, health + healAmount);
					Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
					sys.put(section, newHealth);
					vascular.setVascularSystem(sys);
					changed = true;
				}
			}

			if (changed) {
				VascularSystemEvents.syncVascular((ServerPlayer) player, vascular);
			}
		});
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(displayKey);
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		// Tick every 2 seconds (40 ticks) to match the vascular system check cadence
		return duration % 40 == 0;
	}

}

