package com.vincenthuto.hemomancy.common.manipulation.saint;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Unclosing Eye — Canon Memory of Saint Seraphae.
 * Doctrine: Witness / Light
 *
 * See all entities and structures through walls (glowing effect on nearby entities).
 * However, the player is also revealed — enemies gain heightened awareness.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class UnclosingEyeManip extends BloodManipulation {

	private static final int DURATION_TICKS = 400; // 20 seconds

	public UnclosingEyeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		// Grant glowing to nearby entities so the player can see them through walls
		world.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
				player.getBoundingBox().inflate(32.0), e -> e != player)
				.forEach(entity -> {
					entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, DURATION_TICKS, 0, false, false));
				});

		// The player is also revealed — grant glowing to self
		player.addEffect(new MobEffectInstance(MobEffects.GLOWING, DURATION_TICKS, 0, false, true));

		// Night vision for the player to complement the sight
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_TICKS, 0, false, true));

		player.displayClientMessage(
				net.minecraft.network.chat.Component.literal(
						"Seraphae's eye opens within you. All is laid bare — including yourself.")
						.withStyle(net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);

		if (world instanceof ServerLevel sLevel) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(255, 255, 200)),
					player.getX(), player.getY() + 1.8, player.getZ(),
					30, 0.5, 0.3, 0.5, 0.05);
		}
	}
}
