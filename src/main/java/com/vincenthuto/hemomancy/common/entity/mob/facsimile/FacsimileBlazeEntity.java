package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import com.vincenthuto.hemomancy.common.entity.projectile.CrimsonFireballEntity;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.mixin.core.BlazeChargeInvoker;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * The Gardens' copy of a blaze: the hive learned its fire before it learned its body, and learned it crimson. It
 * smoulders embers instead of smoke and throws {@link CrimsonFireballEntity crimson fireballs}.
 */
public class FacsimileBlazeEntity extends Blaze {
	private static final ParticleColor EMBER = new ParticleColor(178, 18, 30);
	private static final ParticleColor SOOT = new ParticleColor(52, 10, 14);

	public FacsimileBlazeEntity(EntityType<? extends FacsimileBlazeEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Blaze.createAttributes();
	}

	/** What rises off the body each tick in place of vanilla's smoke: embers, and now and then a dark wisp of soot. */
	public ParticleOptions smoulder() {
		return random.nextInt(3) == 0 ? HemoParticleData.darkGlow(SOOT)
				: HemoParticleData.emberSpark(EMBER, 0.16f + random.nextFloat() * 0.08f, 0.7f, 18 + random.nextInt(8));
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(4, new CrimsonVolleyGoal(this));
		goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
		goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	private void setCharged(boolean charged) {
		((BlazeChargeInvoker) this).hemomancy$setCharged(charged);
	}

	/** Vanilla's blaze volley, beat for beat: a charge, three shots, a long rest; the shots are crimson. */
	static class CrimsonVolleyGoal extends Goal {
		private final FacsimileBlazeEntity blaze;
		private int attackStep;
		private int attackTime;
		private int lastSeen;

		CrimsonVolleyGoal(FacsimileBlazeEntity blaze) {
			this.blaze = blaze;
			setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = blaze.getTarget();
			return target != null && target.isAlive() && blaze.canAttack(target);
		}

		@Override
		public void start() {
			attackStep = 0;
		}

		@Override
		public void stop() {
			blaze.setCharged(false);
			lastSeen = 0;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			attackTime--;
			LivingEntity target = blaze.getTarget();
			if (target == null) return;
			boolean seen = blaze.getSensing().hasLineOfSight(target);
			lastSeen = seen ? 0 : lastSeen + 1;
			double distanceSqr = blaze.distanceToSqr(target);
			if (distanceSqr < 4.0) {
				if (!seen) return;
				if (attackTime <= 0) {
					attackTime = 20;
					blaze.doHurtTarget(target);
				}
				blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
			} else if (distanceSqr < followDistance() * followDistance() && seen) {
				double dx = target.getX() - blaze.getX();
				double dy = target.getY(0.5) - blaze.getY(0.5);
				double dz = target.getZ() - blaze.getZ();
				if (attackTime <= 0) {
					attackStep++;
					if (attackStep == 1) {
						attackTime = 60;
						blaze.setCharged(true);
					} else if (attackStep <= 4) {
						attackTime = 6;
					} else {
						attackTime = 100;
						attackStep = 0;
						blaze.setCharged(false);
					}
					if (attackStep > 1) {
						double spread = Math.sqrt(Math.sqrt(distanceSqr)) * 0.5;
						if (!blaze.isSilent()) blaze.playSound(SoundEvents.BLAZE_SHOOT, 1f, 0.8f);
						Vec3 aim = new Vec3(blaze.getRandom().triangle(dx, 2.297 * spread), dy,
								blaze.getRandom().triangle(dz, 2.297 * spread));
						CrimsonFireballEntity shot = new CrimsonFireballEntity(blaze.level(), blaze, aim.normalize());
						shot.setPos(shot.getX(), blaze.getY(0.5) + 0.5, shot.getZ());
						blaze.level().addFreshEntity(shot);
					}
				}
				blaze.getLookControl().setLookAt(target, 10.0F, 10.0F);
			} else if (lastSeen < 5) {
				blaze.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
			}
		}

		private double followDistance() {
			return blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
		}
	}
}
