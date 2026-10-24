package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ScaldingUpdraftManip extends BloodManipulation {
	private static final double RADIUS = 4.0;
	private static final float BURST_DAMAGE = 1.5F;
	/** Expulsive trades height for a heavier blast that throws everything nearby outward. */
	public static final float EXPULSIVE_BURST_DAMAGE = 3.0F;
	public static final double EXPULSIVE_LAUNCH = 0.6D;
	private static final int SEARING_TICKS = 60;
	private final Mode mode;

	public ScaldingUpdraftManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.BASELINE);
	}

	public ScaldingUpdraftManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	/** The updraft kicks off something: ground, water or a ladder. It cannot be chained in mid-air. */
	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.scalding_updraft_manip.the_updraft_needs_footing"), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		Vec3 look = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
		double horizontal = mode == Mode.SOARING ? 0.65D : 0.45D;
		double vertical = switch (mode) {
			case SOARING -> 1.6D;
			case SUSPENDED -> 1.2D;
			case EXPULSIVE -> EXPULSIVE_LAUNCH;
			default -> 1.1D;
		};
		if (look.lengthSqr() > 0.01) look = look.normalize().scale(horizontal);
		player.push(look.x, vertical, look.z);
		player.hurtMarked = true;
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.UPDRAFT, mode == Mode.EXPULSIVE ? 2 : mode == Mode.SOARING ? .6 : 1, mode == Mode.SUSPENDED ? 25 : 16, mode.ordinal()+1);
		player.fallDistance = 0;
		player.resetFallDistance();
		int slowFalling = switch (mode) {
			case SOARING -> 120;
			case SUSPENDED -> 160;
			default -> 100;
		};
		if (mode == Mode.SUSPENDED) {
			player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, false, true));
		}
		player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, slowFalling, 0, false, true));

		float burst = (mode == Mode.EXPULSIVE ? EXPULSIVE_BURST_DAMAGE : BURST_DAMAGE)
				* (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player);
		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(RADIUS),
				e -> ManipulationCombatHelper.canHarm(player, e))) {
			ManipulationParticles.hurt(this, target, world.damageSources().onFire(),
					burst, SEARING_TICKS, 1);
			if (mode == Mode.EXPULSIVE) {
				Vec3 outward = new Vec3(target.getX() - player.getX(), 0, target.getZ() - player.getZ());
				if (outward.lengthSqr() > 0.001D) outward = outward.normalize().scale(0.8D);
				target.push(outward.x, 1.0D, outward.z);
				target.hurtMarked = true;
			}
		}
		if (mode == Mode.EXPULSIVE)
			ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.IGNITION, player.position(), player.position(), RADIUS, 18);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 0.6F);

	        }
    }

	public enum Mode {
		BASELINE,
		SOARING,
		SUSPENDED,
		EXPULSIVE
	}
}
