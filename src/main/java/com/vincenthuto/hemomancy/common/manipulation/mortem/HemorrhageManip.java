package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Optional;

/**
 * Hemorrhage — a T1 (HUMILIS) MORTEM quick manipulation that tears open
 * the veins of the closest enemy within 8 blocks and primes the shared Mortem
 * status economy with Blood Loss.
 */
public class HemorrhageManip extends BloodManipulation {

	private static final double RADIUS = 8.0;
	private static final int BLOOD_LOSS_TICKS = 240;

	public HemorrhageManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	private static Optional<LivingEntity> target(Player player) {
		return player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(RADIUS),
						e -> ManipulationCombatHelper.canHarm(player, e)).stream()
				.filter(e -> ManipulationCombatHelper.visible(player, e))
				.filter(e -> e.distanceTo(player) <= RADIUS)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.hemorrhage_manip.no_living_vessel_within_reach"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		Optional<LivingEntity> closest = target(player);
		if (closest.isEmpty()) return;

		LivingEntity target = closest.get();
		target.addEffect(new MobEffectInstance(EffectInit.blood_loss,
				BLOOD_LOSS_TICKS, 1, false, true));
		// The open wound begins to rot, seeding Necrosis debt for Funeral Bell and Carrion Communion.
		com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(player, target,
				com.vincenthuto.hemomancy.common.damage.SchoolState.NECROSIS, BLOOD_LOSS_TICKS);
		HemomancyTendrilEffects.hemorrhage(player, target);
        ManipulationVisuals.attached(target, ManipulationVisuals.Form.WOUND, 1, BLOOD_LOSS_TICKS, 1);

		world.playSound(null, center, SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.7f, 1.6f);

	        }
    }
}
