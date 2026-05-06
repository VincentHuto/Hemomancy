package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Unclosing Eye — Canon Memory of Saint Seraphae.
 * Doctrine: Witness / Light
 *
 * <p>Seraphae's gaze strips concealment from every living thing within range —
 * invisibility is dissolved, all entities glow, and the caster is revealed too.
 * There is no hiding from a witness this complete.
 *
 * <p>Unlike Crimson Sight (which scouts enemies while the caster stays dark),
 * the Unclosing Eye creates total mutual exposure. It is an anti-stealth weapon,
 * not a scouting tool.
 */
public class UnclosingEyeManip extends BloodManipulation {

	private static final int GLOWING_DURATION = 600;  // 30 seconds
	private static final int NIGHT_VISION_DURATION = 600;
	private static final double SCAN_RADIUS = 32.0;

	public UnclosingEyeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		// Apply Glowing to ALL living entities in range, including players — and strip
		// Invisibility from any target that currently has it.
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(SCAN_RADIUS), e -> e != player && e.isAlive());

		int stripped = 0;
		for (LivingEntity target : targets) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION, 0, false, false));
			if (target.hasEffect(MobEffects.INVISIBILITY)) {
				target.removeEffect(MobEffects.INVISIBILITY);
				stripped++;
			}
		}

		// The caster is also revealed — this is not a tool for hiding
		player.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION, 0, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, true));

		String msg = stripped > 0
				? "Seraphae's eye opens. Nothing is hidden. " + stripped + " concealment(s) dissolved."
				: "Seraphae's eye opens. All is laid bare — including yourself.";
		player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);

		world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);

		if (world instanceof ServerLevel sLevel) {
			// Expanding ring of gold-white light from the caster
			BlockPos pos = player.blockPosition();
			for (int i = 0; i < 50; i++) {
				double angle = (i / 50.0) * Math.PI * 2;
				double dist = 1.5 + (i % 3) * 0.4;
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(255, 240, 180)),
						pos.getX() + 0.5 + Math.cos(angle) * dist,
						pos.getY() + 1.8,
						pos.getZ() + 0.5 + Math.sin(angle) * dist,
						1, 0f, 0.08f, 0f, 0.02f);
			}
		}
	}
}
