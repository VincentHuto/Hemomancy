package com.huto.hemomancy.entity.mob;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.hutoslib.client.models.Animation;
import com.hutoslib.client.models.AnimationPacket;
import com.hutoslib.client.models.IAnimatable;
import com.hutoslib.client.particle.ParticleColor;
import com.hutoslib.client.particle.ParticleUtil;
import com.hutoslib.client.particles.factory.GlowParticleFactory;
import com.hutoslib.math.MathUtil;

import net.minecraft.block.Blocks;
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
import net.minecraft.entity.ai.goal.BreakBlockGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityChitinite extends CreatureEntity implements IAnimatable {

	private Animation animation = NO_ANIMATION;
	public static final Animation ROLLUP_ANIMATION = new Animation(128);

	public int puffCooldown = 0;

	private int animationTick;

	public EntityChitinite(EntityType<? extends EntityChitinite> type, World worldIn) {
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
		if (world instanceof ServerWorld && ((ServerWorld) world).getStructureManager()
				.getStructureStart(this.getPosition(), true, Structure.SWAMP_HUT).isValid()) {
			this.enablePersistence();
		}

		return spawnDataIn;

	}

	public void sporePuff(World world, AxisAlignedBB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof EntityChitinite)) {
				LivingEntity liv = (LivingEntity) ent;
				liv.addPotionEffect(new EffectInstance(Effects.POISON, 200, 200));
				for (int countparticles = 0; countparticles <= 10; ++countparticles) {
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 150, 0)),
							getPosX() + ParticleUtil.inRange(-0.25, 0.25),
							getPosY() + ParticleUtil.inRange(-0.25, 0.25),
							getPosZ() + ParticleUtil.inRange(-0.25, 0.25), 0, 0.000, 0);
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 250, 0)),
							getPosX() + ParticleUtil.inRange(-0.25, 0.25), getPosY() + ParticleUtil.inRange(-0.1, 0.1),
							getPosZ() + ParticleUtil.inRange(-0.25, 0.25), 0, 0.000, 0);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void livingTick() {
		super.livingTick();
		Animation animation = getAnimation();
		int animTick = getAnimationTick();

		if (puffCooldown > 0) {
			--puffCooldown;
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
			rotationYaw = (float) MathUtil.getAngle(EntityChitinite.this, target) + 90f;
		}
		if (noActiveAnimation()) {
			if (isClose && MathHelper.degreesDifferenceAbs((float) MathUtil.getAngle(EntityChitinite.this, target) + 90,
					rotationYaw) < 30) {
				AnimationPacket.send(EntityChitinite.this, ROLLUP_ANIMATION);
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
		this.goalSelector.addGoal(1, new BreakBlockGoal(Blocks.OAK_WOOD, this, 1.5d, 10));
		this.goalSelector.addGoal(2, new RollupGoal(this, 1.0f));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(7, new LookRandomlyGoal(this));

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
		return new Animation[] { ROLLUP_ANIMATION };
	}

	public boolean isVulnerable() {
		return this.getHealth() <= this.getMaxHealth() / 4.0F;
	}

	// Roll Goal
	private class RollupGoal extends Goal {
		protected final CreatureEntity creature;
		protected boolean running;

		public RollupGoal(CreatureEntity creature, double speedIn) {
			this.creature = creature;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public void tick() {
			creature.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 10, 100, false, false));
			if (noActiveAnimation()) {
				AnimationPacket.send(EntityChitinite.this, ROLLUP_ANIMATION);
			}
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state
		 * necessary for execution in this method as well.
		 */
		public boolean shouldExecute() {
			if (this.creature.getRevengeTarget() == null && !this.creature.isBurning()) {
				return false;
			} else {
				if (this.creature.isBurning()) {
					BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 5, 4);
					if (blockpos != null) {
						return true;
					}
				}

				return this.findRandomPosition();
			}
		}

		protected boolean findRandomPosition() {
			Vector3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 5, 4);
			if (vector3d == null) {
				return false;
			} else {
				return true;
			}
		}

		@SuppressWarnings("unused")
		public boolean isRunning() {
			return this.running;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			// this.creature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY,
			// this.randPosZ, this.speed);
			this.running = true;
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		public void resetTask() {
			this.running = false;
		}

		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		public boolean shouldContinueExecuting() {
			return !this.creature.getNavigator().noPath();
		}

		@Nullable
		protected BlockPos getRandPos(IBlockReader worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
			BlockPos blockpos = entityIn.getPosition();
			int i = blockpos.getX();
			int j = blockpos.getY();
			int k = blockpos.getZ();
			float f = (float) (horizontalRange * horizontalRange * verticalRange * 2);
			BlockPos blockpos1 = null;
			BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

			for (int l = i - horizontalRange; l <= i + horizontalRange; ++l) {
				for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1) {
					for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1) {
						blockpos$mutable.setPos(l, i1, j1);
						if (worldIn.getFluidState(blockpos$mutable).isTagged(FluidTags.WATER)) {
							float f1 = (float) ((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));
							if (f1 < f) {
								f = f1;
								blockpos1 = new BlockPos(blockpos$mutable);

							}
						}
					}
				}
			}

			return blockpos1;
		}
	}

}
