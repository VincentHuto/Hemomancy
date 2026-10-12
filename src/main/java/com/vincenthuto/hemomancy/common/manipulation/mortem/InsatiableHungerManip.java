package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
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

public class InsatiableHungerManip extends BloodManipulation {
	private static final double BASE_RANGE = 10.0D;
	private static final int DURATION_TICKS = 220;
	/** The bite that opens the hunger, before Crimson Mastery; as a direct Mortem hit it seeds Necrosis. */
	public static final float CAST_DAMAGE = 2.0F;

	public InsatiableHungerManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	private static LivingEntity target(Player player) {
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		return player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range),
						entity -> ManipulationCombatHelper.canHarm(player, entity) && ManipulationCombatHelper.visible(player, entity)
								&& entity.distanceTo(player) <= range)
				.stream()
				.min(Comparator.comparingDouble(player::distanceToSqr))
				.orElse(null);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player) == null) {
			player.displayClientMessage(Component.literal("No vessel within reach to starve."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel serverLevel)) return;
		LivingEntity target = target(player);
		if (target == null) return;

		ManipulationCombatHelper.hurt(this, player, target, serverLevel,
				CAST_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player));
		if (!target.isAlive()) return;
		target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, DURATION_TICKS, 0, false, true, true));
		ManipulationVisuals.attached(target, ManipulationVisuals.Form.HUNGER, 1, DURATION_TICKS, 1); world.playSound(null, target.blockPosition(), SoundEvents.HUSK_AMBIENT, SoundSource.PLAYERS, 0.65F, 0.7F);
	        }
    }
}
