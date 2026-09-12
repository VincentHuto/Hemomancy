package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Bloom of Rot — Canon Memory of Saint Putriciel.
 * Doctrine: Absolution / Witness
 *
 * Spreads hematic fungus in a radius — enemies decay (wither + poison),
 * and flora blooms. However, the terrain becomes semi-hostile and
 * the player also takes minor poison.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class BloomOfRotManip extends BloodManipulation {

	private static final double RADIUS = 8.0;
	private static final int ENEMY_EFFECT_DURATION = 200; // 10 seconds
	private static final int SELF_POISON_DURATION = 100; // 5 seconds

	public BloomOfRotManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		world.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(RADIUS), e -> ManipulationCombatHelper.canHarm(player, e))
				.forEach(entity -> {
					com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(player, entity,
                            com.vincenthuto.hemomancy.common.damage.SchoolState.NECROSIS, ENEMY_EFFECT_DURATION);
                    com.vincenthuto.hemomancy.common.manipulation.MortemStatusVisuals.infect(entity,ENEMY_EFFECT_DURATION);
				});

		player.addEffect(new MobEffectInstance(MobEffects.POISON, SELF_POISON_DURATION, 0, false, true));
        com.vincenthuto.hemomancy.common.manipulation.MortemStatusVisuals.infect(player,SELF_POISON_DURATION);

		player.displayClientMessage(
				net.minecraft.network.chat.Component.literal(
						"Putriciel's bloom erupts. Decay and life intertwine — the rot spreads to all, including you.")
						.withStyle(net.minecraft.ChatFormatting.DARK_GREEN, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.PLAYERS, 1.0f, 0.7f);

		if (world instanceof ServerLevel sLevel) {
            ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.BLOOM, player.position(), player.position(), RADIUS, 36);
		}
	        }
    }
}
