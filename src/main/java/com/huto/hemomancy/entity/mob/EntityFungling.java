package com.huto.hemomancy.entity.mob;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.huto.hemomancy.entity.utils.Vector3;
import com.huto.hemomancy.model.animation.Animation;
import com.huto.hemomancy.model.animation.AnimationPacket;
import com.huto.hemomancy.model.animation.IAnimatable;
import com.huto.hemomancy.model.animation.Mafs;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.GlowParticleData;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityFungling extends CreatureEntity implements IAnimatable {

	private Animation animation = NO_ANIMATION;
	public static final Animation HEADBUTT_ANIMATION = new Animation(17);
	public static final Animation SPOREPUFF_ANIMATION = new Animation(17);
	public int puffCooldown = 0;

	private int animationTick;

	public EntityFungling(EntityType<? extends EntityFungling> type, World worldIn) {
		super(type, worldIn);

	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void registerData() {
		super.registerData();

	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Nullable
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			@Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setEquipmentBasedOnDifficulty(difficultyIn);
		World world = worldIn.getWorld();
		if (world instanceof ServerWorld && ((ServerWorld) world).func_241112_a_()
				.getStructureStart(this.getPosition(), true, Structure.SWAMP_HUT).isValid()) {
			this.enablePersistence();
		}

		return spawnDataIn;

	}

	public void sporePuff(World world, AxisAlignedBB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof EntityFungling)) {
				LivingEntity liv = (LivingEntity) ent;
				liv.addPotionEffect(new EffectInstance(Effects.POISON, 200, 200));
				for (int countparticles = 0; countparticles <= 10; ++countparticles) {
					world.addParticle(GlowParticleData.createData(new ParticleColor(0, 150, 0)),
							getPosX() + ParticleUtil.inRange(-0.25, 0.25),
							getPosY() + ParticleUtil.inRange(-0.25, 0.25),
							getPosZ() + ParticleUtil.inRange(-0.25, 0.25), 0, 0.000, 0);
					world.addParticle(GlowParticleData.createData(new ParticleColor(0, 250, 0)),
							getPosX() + ParticleUtil.inRange(-0.25, 0.25), getPosY() + ParticleUtil.inRange(-0.1, 0.1),
							getPosZ() + ParticleUtil.inRange(-0.25, 0.25), 0, 0.000, 0);
				}
			}
		}
	}

	@Override
	public void livingTick() {
		super.livingTick();
		Animation animation = getAnimation();
		int animTick = getAnimationTick();

		if (puffCooldown > 0) {
			--puffCooldown;
		}

		if (animation == SPOREPUFF_ANIMATION) {
			puffCooldown += 6;
			if (animTick == 10)
//				playSound(SoundHandler.ENTITY_DARK_YOUNG_HIT, .25F, 1f);
				if (!world.isRemote && animTick >= 10) {
					LivingEntity target = getAttackTarget();
					if (target != null) {
						if (animTick % 30 == 0) {
							sporePuff(world,
									new AxisAlignedBB(this.getPositionVec().add(-3, -3, -3),
											this.getPositionVec().add(3, 3, 3)),
									this.getPositionVec().getX() + 0.5, this.getPositionVec().getY(),
									this.getPositionVec().getZ() + 0.5);
						}
					}
				}

		} else if (animation == HEADBUTT_ANIMATION) {
			if (animTick == 0) {
				// playSound(SoundHandler.ENTITY_DARK_YOUNG_HIT, .25F, 1f);
			} else if (animTick == 6)
				attackInBox(getBoundingBox()
						.offset(Vector3d.fromPitchYaw(isInWater() ? rotationPitch : 0, rotationYawHead).scale(1f))
						.grow(0.85), 40);
		}
	}

	public void attackInBox(AxisAlignedBB box, int disabledShieldTime) {
		List<LivingEntity> attackables = world.getEntitiesWithinAABB(LivingEntity.class, box,
				entity -> entity != this && !isPassenger(entity));
		for (LivingEntity attacking : attackables) {
			attackEntityAsMob(attacking);
			if (disabledShieldTime > 0 && attacking instanceof PlayerEntity) {
				PlayerEntity player = ((PlayerEntity) attacking);
				if (player.isHandActive() && player.getActiveItemStack().isShield(player)) {
					player.getCooldownTracker().setCooldown(Items.SHIELD, disabledShieldTime);
					player.resetActiveHand();
					world.setEntityState(player, (byte) 9);
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		updateAnimations();
		LivingEntity target = getAttackTarget();
		if (target == null)
			return;
		double distFromTarget = getDistanceSq(target);

		getLookController().setLookPositionWithEntity(target, getHorizontalFaceSpeed(), getVerticalFaceSpeed());
		boolean isClose = distFromTarget < 5;
		if (getNavigator().noPath())
			getNavigator().tryMoveToEntityLiving(target, 1.2);
		if (isClose) {
			rotationYaw = (float) Mafs.getAngle(EntityFungling.this, target) + 90f;
		}
		if (noActiveAnimation()) {
			if (distFromTarget > 15 && distFromTarget < 30) {
				AnimationPacket.send(EntityFungling.this, SPOREPUFF_ANIMATION);

				if (!world.isRemote) {
					ParticleUtil.spawnPoof((ServerWorld) world, new BlockPos(Vector3.fromEntityCenter(this).x,
							Vector3.fromEntityCenter(this).y, Vector3.fromEntityCenter(this).z));
					sporePuff(world,
							new AxisAlignedBB(this.getPositionVec().add(-2, -2, -2),
									this.getPositionVec().add(2, 2, 2)),
							this.getPositionVec().getX() + 0.5, this.getPositionVec().getY(),
							this.getPositionVec().getZ() + 0.5);
				}

			} else if (isClose && MathHelper
					.degreesDifferenceAbs((float) Mafs.getAngle(EntityFungling.this, target) + 90, rotationYaw) < 30) {
				AnimationPacket.send(EntityFungling.this, HEADBUTT_ANIMATION);
			}
		}
	}

	@Override
	public void onCollideWithPlayer(PlayerEntity entityIn) {
		super.onCollideWithPlayer(entityIn);

	}

	@Override
	protected void collideWithEntity(Entity entityIn) {
		super.collideWithEntity(entityIn);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(2, new HeadButtGoal());
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(1, new MoveTowardsTargetGoal(this, 0.5d, 50));
		this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));

	}

	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 7.0D)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3D)
				.createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 1.15D)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_WOLF_HURT;
	}

	@Override
	public int getAnimationTick() {
		return animationTick;
	}

	@Override
	public void setAnimationTick(int tick) {
		animationTick = tick;

	}

	@Override
	public Animation getAnimation() {
		return animation;
	}

	@Override
	public void setAnimation(Animation animation) {
		if (animation == null)
			animation = NO_ANIMATION;
		setAnimationTick(0);
		this.animation = animation;
	}

	@Override
	public Animation[] getAnimations() {
		return new Animation[] { HEADBUTT_ANIMATION, SPOREPUFF_ANIMATION };
	}

	// Bite Goal
	private class HeadButtGoal extends Goal {
		public HeadButtGoal() {
			setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean shouldExecute() {
			return !canPassengerSteer() && getAttackTarget() != null;
		}

		@Override
		public void tick() {
			LivingEntity target = getAttackTarget();
			if (target == null)
				return;
			double distFromTarget = getDistanceSq(target);

			getLookController().setLookPositionWithEntity(target, getHorizontalFaceSpeed(), getVerticalFaceSpeed());

			boolean isClose = distFromTarget < 5;

			if (getNavigator().noPath())
				getNavigator().tryMoveToEntityLiving(target, 1);

			if (isClose)
				rotationYaw = (float) Mafs.getAngle(EntityFungling.this, target) + 90f;
			if (noActiveAnimation()) {
				if (isClose && MathHelper.degreesDifferenceAbs((float) Mafs.getAngle(EntityFungling.this, target) + 90,
						rotationYaw) < 30)
					AnimationPacket.send(EntityFungling.this, HEADBUTT_ANIMATION);
			}

		}
	}

	// Move Goal
	private class MoveTowardsTargetGoal extends Goal {
		private final CreatureEntity creature;
		private LivingEntity targetEntity;
		private double movePosX;
		private double movePosY;
		private double movePosZ;
		private final double speed;
		private final float maxTargetDistance;

		public MoveTowardsTargetGoal(CreatureEntity creature, double speedIn, float targetMaxDistance) {
			this.creature = creature;
			this.speed = speedIn;
			this.maxTargetDistance = targetMaxDistance;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public void tick() {
			LivingEntity target = getAttackTarget();
			if (target == null)
				return;
			double distFromTarget = getDistanceSq(target);

			getLookController().setLookPositionWithEntity(target, getHorizontalFaceSpeed(), getVerticalFaceSpeed());

			boolean isClose = distFromTarget < 40;

			if (getNavigator().noPath())
				getNavigator().tryMoveToEntityLiving(target, 1.2);

			if (isClose)
				rotationYaw = (float) Mafs.getAngle(EntityFungling.this, target) + 90f;

			if (noActiveAnimation()) {
				if (distFromTarget > 40) {
					AnimationPacket.send(EntityFungling.this, NO_ANIMATION);
				}
			}
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state
		 * necessary for execution in this method as well.
		 */
		public boolean shouldExecute() {
			this.targetEntity = this.creature.getAttackTarget();
			if (this.targetEntity == null) {
				return false;
			} else if (this.targetEntity
					.getDistanceSq(this.creature) > (double) (this.maxTargetDistance * this.maxTargetDistance)) {
				return false;
			} else {
				Vector3d vector3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 16, 7,
						this.targetEntity.getPositionVec());
				if (vector3d == null) {
					return false;

				} else {
					this.movePosX = vector3d.x;
					this.movePosY = vector3d.y;
					this.movePosZ = vector3d.z;
					return true;
				}
			}
		}

		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		public boolean shouldContinueExecuting() {
			return !this.creature.getNavigator().noPath() && this.targetEntity.isAlive() && this.targetEntity
					.getDistanceSq(this.creature) < (double) (this.maxTargetDistance * this.maxTargetDistance);
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		public void resetTask() {
			this.targetEntity = null;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			this.creature.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
		}
	}

}
