package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.MathUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.NetworkHooks;

public class ChitiniteEntity extends PathfinderMob {

	private static final EntityDataAccessor<Boolean> ROLLED_UP =
			SynchedEntityData.defineId(ChitiniteEntity.class, EntityDataSerializers.BOOLEAN);

	private static final int ROLLUP_DURATION = 80;

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public int puffCooldown = 0;

	public ChitiniteEntity(EntityType<? extends ChitiniteEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public boolean isRolledUp() {
		return this.entityData.get(ROLLED_UP);
	}

	private void setRolledUp(boolean rolledUp) {
		this.entityData.set(ROLLED_UP, rolledUp);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (isRolledUp()) {
			return false;
		}
		return super.hurt(source, amount);
	}

	public void attackInBox(AABB box, int disabledShieldTime) {
		List<LivingEntity> attackables = level().getEntitiesOfClass(LivingEntity.class, box,
				entity -> entity != this && !hasPassenger(entity));
		for (LivingEntity attacking : attackables) {
			doHurtTarget(attacking);
			if (disabledShieldTime > 0 && attacking instanceof Player) {
				Player player = ((Player) attacking);
				if (player.isUsingItem()) {
					player.getCooldowns().addCooldown(Items.SHIELD, disabledShieldTime);
					player.stopUsingItem();
					level().broadcastEntityEvent(player, (byte) 9);
				}
			}
		}
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	public static boolean canSpawnInCave(EntityType<? extends ChitiniteEntity> type, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return world.getBlockState(below).isSolidRender(world, below)
				&& world.getRawBrightness(pos, 0) <= 7;
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
		return super.checkSpawnRules(pLevel, pSpawnReason);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ROLLED_UP, false);
	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.populateDefaultEquipmentSlots(random, difficultyIn);
		return spawnDataIn;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_CHITINITE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_CHITINITE_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_CHITINITE_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new RollupGoal(this));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public void sporePuff(Level world, AABB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntities(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof ChitiniteEntity)) {
				LivingEntity liv = (LivingEntity) ent;
				liv.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 200));
				for (int countparticles = 0; countparticles <= 10; ++countparticles) {
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 150, 0)),
							getX() + HLParticleUtils.inRange(-0.25, 0.25),
							getY() + HLParticleUtils.inRange(-0.25, 0.25),
							getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 250, 0)),
							getX() + HLParticleUtils.inRange(-0.25, 0.25), getY() + HLParticleUtils.inRange(-0.1, 0.1),
							getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		LivingEntity target = getTarget();
		if (target == null)
			return;
		double distFromTarget = distanceToSqr(target);

		getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
		boolean isClose = distFromTarget < 5;
		if (getNavigation().isDone())
			getNavigation().moveTo(target, 1.2);
		if (isClose) {
			yHeadRot = (float) MathUtils.getAngle(ChitiniteEntity.this, target) + 90f;
		}
	}

	// Rolls up into a defensive ball when hurt, becoming completely immune to
	// damage for ROLLUP_DURATION ticks before uncurling.
	private class RollupGoal extends Goal {
		private final ChitiniteEntity chitinite;
		private int rollupTimer;
		private int cooldown;

		public RollupGoal(ChitiniteEntity chitinite) {
			this.chitinite = chitinite;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (cooldown > 0) {
				cooldown--;
				return false;
			}
			return chitinite.getLastHurtByMob() != null;
		}

		@Override
		public boolean canContinueToUse() {
			return rollupTimer > 0;
		}

		@Override
		public void start() {
			rollupTimer = ROLLUP_DURATION;
			chitinite.setRolledUp(true);
			chitinite.getNavigation().stop();
		}

		@Override
		public void tick() {
			rollupTimer--;
		}

		@Override
		public void stop() {
			cooldown = 100;
			chitinite.setRolledUp(false);
		}
	}

}
