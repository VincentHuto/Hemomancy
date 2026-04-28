package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Sanguine Alloy — a T3 (SUMMA) FERRIC quick manipulation that saturates the
 * caster's blood with ferrous compounds, sharpening their body as a weapon.
 *
 * <p>For 90 seconds the caster gains:
 * <ul>
 *   <li>Strength II — iron-enriched blood drives harder hits</li>
 *   <li>Sanguine Siphon II — accelerated blood production as the alloy
 *       metabolises from combat energy back into volume</li>
 * </ul>
 * The effect cannot be refreshed while already active — casting again
 * while the buff is running is a wasted cost.
 */
public class FerricTransmutationManip extends BloodManipulation {

	private static final int DURATION_TICKS = 1800; // 90 seconds
	private static final int STRENGTH_AMPLIFIER = 1; // Strength II
	private static final int SIPHON_AMPLIFIER = 1;   // Sanguine Siphon II

	public FerricTransmutationManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
				DURATION_TICKS, STRENGTH_AMPLIFIER, false, true));
		player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon.get(),
				DURATION_TICKS, SIPHON_AMPLIFIER, false, true));

		BlockPos pos = player.blockPosition();
		RandomSource random = world.random;

		// Red-tinged iron-grey particle burst around player
		for (int i = 0; i < 40; i++) {
			float r = 140 + random.nextFloat() * 80;
			float g = 60 + random.nextFloat() * 40;
			float b = 60 + random.nextFloat() * 40;
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(r, g, b)),
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.2,
					pos.getY() + 0.5 + random.nextDouble() * 1.8,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.2,
					1, (random.nextDouble() - 0.5) * 0.15, 0.15, (random.nextDouble() - 0.5) * 0.15, 0.02f);
		}

		world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7f, 0.6f);
		world.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 1.4f);
	}
}
