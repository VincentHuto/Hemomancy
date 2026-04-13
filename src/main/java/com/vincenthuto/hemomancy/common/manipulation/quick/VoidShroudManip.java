package com.vincenthuto.hemomancy.common.manipulation.quick;

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
 * Void Shroud — a T1 (HUMILIS) TENEBRIS quick manipulation that bends the
 * caster's blood into a light-absorbing veil, rendering them invisible for
 * 5 seconds.
 *
 * <p>The shroud is dispelled immediately if the player attacks, uses an item
 * that interacts with the world, or receives damage (all standard Invisibility
 * behaviour). Subtle dark particles mark where the caster stands, giving
 * attentive opponents a faint clue.
 */
public class VoidShroudManip extends BloodManipulation {

	/** Duration of invisibility in ticks (5 seconds). */
	private static final int INVISIBILITY_TICKS = 100;

	public VoidShroudManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		// Refresh (or apply) the Invisibility effect
		player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
				INVISIBILITY_TICKS, 0, false, false));

		world.playSound(null, player.blockPosition(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.8f);

		RandomSource random = world.random;
		BlockPos pos = player.blockPosition();
		for (int i = 0; i < 20; i++) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(
							40 + random.nextFloat() * 30,
							0,
							60 + random.nextFloat() * 50)),
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
					pos.getY() + random.nextDouble() * 2.0,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
					1, 0f, -0.05f, 0f, 0.01f);
		}
	}
}
