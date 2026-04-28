package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
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
 * Void Shroud — a T1 (HUMILIS) TENEBRIS quick manipulation that folds the
 * caster's blood into darkness, granting invisibility and shadow-speed for
 * 5 seconds.
 *
 * <p>Unlike a pure stealth effect, the Shroud is a repositioning tool:
 * Speed II lets the caster dash while hidden, and Night Vision lets them
 * read the terrain they're hiding in. The invisibility breaks on attack
 * or damage (standard behaviour), so the window is short.
 *
 * <p>Pairs with UmbralStep — shroud first to close distance unseen,
 * then step through shadows to appear at a new position.
 */
public class VoidShroudManip extends BloodManipulation {

	private static final int DURATION_TICKS = 100; // 5 seconds

	public VoidShroudManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, DURATION_TICKS, 0, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, 1, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_TICKS, 0, false, false));

		world.playSound(null, player.blockPosition(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.8f);

		RandomSource random = world.random;
		BlockPos pos = player.blockPosition();
		for (int i = 0; i < 25; i++) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(
							40 + random.nextFloat() * 30,
							0,
							60 + random.nextFloat() * 50)),
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
					pos.getY() + random.nextDouble() * 2.0,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
					1, (random.nextDouble() - 0.5) * 0.1, -0.03f, (random.nextDouble() - 0.5) * 0.1, 0.01f);
		}
	}
}
