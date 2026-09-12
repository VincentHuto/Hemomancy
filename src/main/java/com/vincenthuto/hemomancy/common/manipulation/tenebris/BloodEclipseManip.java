package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Blood Eclipse — a T2 (MEDIOCRITAS) TENEBRIS quick manipulation that erupts
 * a cone of void-drenched blood ahead of the caster, afflicting all enemies
 * caught in a 60-degree arc with Blindness and Weakness.
 *
 * <p>Cone range scales with Sanguine Reach. Each affected entity receives:
 * <ul>
 *   <li>Blindness II for 5 seconds</li>
 *   <li>Weakness I for 6 seconds</li>
 *   <li>1.5 hearts of shadow damage</li>
 * </ul>
 * Best used to reduce pressure from ranged enemies or to open a gap in melee.
 */
public class BloodEclipseManip extends BloodManipulation {

	private static final double BASE_RANGE = 18.0;
	/** Half-angle of the forward cone in degrees. */
	private static final double CONE_HALF_ANGLE_DEG = 30.0;
	private static final int BLINDNESS_TICKS = 100; // 5 s
	private static final int BLINDNESS_AMP = 1;     // Blindness II
	private static final int WEAKNESS_TICKS = 120;  // 6 s
	private static final int WEAKNESS_AMP = 0;      // Weakness I
	private static final float SHADOW_DAMAGE = 3.0f; // 1.5 hearts

	public BloodEclipseManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 look = player.getViewVector(1.0F).normalize();
		double cosThreshold = Math.cos(Math.toRadians(CONE_HALF_ANGLE_DEG));

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(range);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> ManipulationCombatHelper.canHarm(player, e));

		int hit = 0;
		for (LivingEntity target : targets) {
			if (!ManipulationCombatHelper.visible(player, target)) continue;
			double dist = target.distanceTo(player);
			if (dist > range) continue;

			Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0)
					.subtract(player.getEyePosition(1.0F)).normalize();
			double dot = look.dot(toTarget);
			if (dot < cosThreshold) continue;

			float damage = (float) (SHADOW_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
			ManipulationCombatHelper.hurt(this, player, target, sLevel, damage, BLINDNESS_TICKS, 1);
			if (hit < 6) {
				HemomancyTendrilEffects.bloodEclipse(player, target, hit);
			}
			hit++;
		}

		world.playSound(null, center, SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 2.0f);
		world.playSound(null, center, SoundEvents.ENDERMAN_AMBIENT, SoundSource.PLAYERS, 0.4f, 0.5f);

		RandomSource random = world.random;
		Vec3 perp1 = look.cross(new Vec3(0, 1, 0)).normalize();
		if (perp1.lengthSqr() < 1e-6) {
			perp1 = look.cross(new Vec3(1, 0, 0)).normalize();
		}
		Vec3 perp2 = look.cross(perp1).normalize();
		double halfAngleRad = Math.toRadians(CONE_HALF_ANGLE_DEG);
		for (int i = 0; i < 8; i++) {
			double theta = random.nextDouble() * 2 * Math.PI;
			double phi = random.nextDouble() * halfAngleRad;
			double sinPhi = Math.sin(phi);
			Vec3 dir = look.scale(Math.cos(phi))
					.add(perp1.scale(sinPhi * Math.cos(theta)))
					.add(perp2.scale(sinPhi * Math.sin(theta)));
			double dist = 2.0 + random.nextDouble() * (range * 0.4);
			Vec3 pPos = ManipulationCombatHelper.clipToGeometry(player, player.getEyePosition(1.0F).add(dir.scale(dist)));
            ManipulationParticles.accent(sLevel, EnumBloodTendency.TENEBRIS, pPos, dir.scale(.02));
		}
	        }
    }
}
