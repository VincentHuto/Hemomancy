package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationScalingRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Lodestone Collapse — Ferric Perfectus. The caster magnetises a point and every hostile body nearby is
 * dragged into it, the iron already fixed in their blood collapsing inward. Lodestone and three-level Rime
 * are cashed in by the shared school payoff in {@code SchoolCombatEvents}.
 */
public class LodestoneCollapseManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 70;
	private static final double AIM_RANGE = 16.0D;
	private static final double MIN_RADIUS = 3.0D;
	private static final double MAX_RADIUS = 7.0D;
	private static final float MIN_DAMAGE = 3.0F;
	private static final float MAX_DAMAGE = 10.0F;
	private static final double PULL = 0.9D;

	public LodestoneCollapseManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	/** Iron answers the lodestone only through open air: a wall between the point and a body shields it. */
	private static boolean reaches(Level level, Vec3 origin, LivingEntity target, Player player) {
		return level.clip(new ClipContext(origin, target.getEyePosition(), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player,
				ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS))) {
			if (!(world instanceof ServerLevel level)) return;
			float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
			double radius = ManipulationScalingRules.scaled(MIN_RADIUS, MAX_RADIUS, heldTicks, CHARGE_TICKS);
			float damage = (float) (ManipulationScalingRules.scaled(MIN_DAMAGE, MAX_DAMAGE, heldTicks, CHARGE_TICKS)
					* SkillPointHelper.getCrimsonMasteryMultiplier(player));
			Vec3 center = ManipulationCombatHelper.clipToGeometry(player,
					player.getEyePosition().add(player.getLookAngle().scale(AIM_RANGE)));
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.LODESTONE_COLLAPSE, center, center, radius,
					LodestoneCollapseRules.VISUAL_TICKS, 1 + level.random.nextInt(97));
			// The point is clipped onto a surface; trace from just off it so the struck block cannot hide everyone.
			Vec3 back = player.getEyePosition().subtract(center);
			Vec3 origin = back.lengthSqr() > 0.01D ? center.add(back.normalize().scale(0.1D)) : center;
			for (LivingEntity target : ManipulationCombatHelper.hostileTargets(player, level, AIM_RANGE + radius)) {
				Vec3 toCenter = center.subtract(target.position());
				if (toCenter.length() > radius || !reaches(level, origin, target, player)) continue;
				if (toCenter.lengthSqr() > 0.01D) {
					Vec3 pull = toCenter.normalize().scale(PULL * charge);
					target.setDeltaMovement(pull.x, Math.max(0.15D, pull.y), pull.z);
					target.hurtMarked = true;
				}
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT,
						target.position().add(0, target.getBbHeight() * 0.5D, 0), center, 0.6D, 14);
				if (ManipulationCombatHelper.hurt(this, player, target, level, damage) && charge >= 1.0F) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
				}
			}
			BlockPos sound = BlockPos.containing(center);
			level.playSound(null, sound, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.9F, 0.5F);
			level.playSound(null, sound, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 0.8F, 0.6F);
		}
	}
}
