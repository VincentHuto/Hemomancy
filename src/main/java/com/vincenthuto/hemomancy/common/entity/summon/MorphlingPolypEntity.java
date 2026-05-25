package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class MorphlingPolypEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_LAYER_MASK = SynchedEntityData.defineId(
			MorphlingPolypEntity.class, EntityDataSerializers.INT);
	private static final String TAG_MORPHLING_LAYERS = "MorphlingLayers";
	private static final double WATER_DRIFT_SPEED = 0.025D;

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D)
				.add(Attributes.FOLLOW_RANGE, 16.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
	}

	public MorphlingPolypEntity(EntityType<? extends MorphlingPolypEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public int getLayerMask() {
		return this.entityData.get(DATA_LAYER_MASK);
	}

	public void setLayerMask(int mask) {
		this.entityData.set(DATA_LAYER_MASK, MorphlingPolypLayerRules.sanitizeMask(mask));
	}

	public boolean hasLayer(MorphlingPolypLayer layer) {
		return MorphlingPolypLayerRules.hasLayer(this.getLayerMask(), layer);
	}

	public List<MorphlingPolypLayer> getLayers() {
		return MorphlingPolypLayerRules.layersFromMask(this.getLayerMask());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_LAYER_MASK, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MorphlingPolypHopGoal(this));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static boolean canSpawnHere(EntityType<? extends MorphlingPolypEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		boolean water = level.getFluidState(pos).is(FluidTags.WATER);
		if (water) {
			return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
					&& (level.getFluidState(pos.above()).is(FluidTags.WATER)
					|| level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty());
		}
		BlockPos below = pos.below();
		return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
				&& level.getBlockState(below).isSolidRender(level, below);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		if (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) {
			this.assignNaturalLayers(level, this.blockPosition());
		} else if (this.getLayerMask() == 0) {
			this.assignNaturalLayers(level, this.blockPosition());
		}
		return super.finalizeSpawn(level, difficulty, reason, spawnData);
	}

	private void assignNaturalLayers(ServerLevelAccessor level, BlockPos pos) {
		EnumSet<MorphlingPolypLayer> candidates = MorphlingPolypLayerRules.candidateLayers(
				this.buildLayerContext(level, pos));
		this.setLayerMask(MorphlingPolypLayerRules.selectLayerMask(candidates,
				new java.util.Random(this.random.nextLong()), MorphlingPolypLayerRules.MAX_NATURAL_LAYERS));
	}

	private MorphlingPolypLayerRules.SpawnContext buildLayerContext(ServerLevelAccessor level, BlockPos pos) {
		MorphlingPolypLayerRules.SpawnContext context = MorphlingPolypLayerRules.context();
		String biomePath = level.getBiome(pos).unwrapKey()
				.map(key -> key.location().getPath())
				.orElse("");
		if (level.getFluidState(pos).is(FluidTags.WATER)) {
			context.inWater();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_OCEAN) || biomePath.contains("ocean")) {
			context.ocean();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_RIVER) || biomePath.contains("river")
				|| biomePath.contains("swamp")) {
			context.riverOrSwamp();
		}
		if (biomePath.contains("swamp")) {
			context.swamp();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_BADLANDS) || biomePath.contains("desert")
				|| biomePath.contains("badlands")) {
			context.desertOrBadlands();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_JUNGLE) || biomePath.contains("jungle")) {
			context.jungle();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_FOREST) || biomePath.contains("forest")) {
			context.forest();
		}
		if (biomePath.contains("dark_forest")) {
			context.darkForest();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_TAIGA) || biomePath.contains("old_growth")) {
			context.taigaOrOldGrowth();
		}
		if (biomePath.contains("mushroom")) {
			context.mushroom();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_SAVANNA) || biomePath.contains("plains")
				|| biomePath.contains("meadow") || biomePath.contains("savanna")) {
			context.openLand();
		}
		if (level.getBiome(pos).is(BiomeTags.IS_MOUNTAIN) || biomePath.contains("stony")
				|| biomePath.contains("grove") || biomePath.contains("slope")) {
			context.mountainOrStony();
		}
		if (!level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() - 8) {
			context.underground();
		}
		if (level.getMaxLocalRawBrightness(pos) <= 7) {
			context.lowLight();
		}
		if (nearbyMatches(level, pos, state -> state.is(BlockTags.BASE_STONE_OVERWORLD)
				|| state.is(BlockTags.STONE_ORE_REPLACEABLES))) {
			context.stoneNearby();
		}
		if (nearbyMatches(level, pos, state -> state.is(BlockTags.DIRT))) {
			context.dirtNearby();
		}
		if (nearbyMatches(level, pos, MorphlingPolypEntity::isFungalBlock)) {
			context.fungalNearby();
		}
		return context;
	}

	private static boolean nearbyMatches(LevelAccessor level, BlockPos center, Predicate<BlockState> predicate) {
		for (BlockPos candidate : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
			if (predicate.test(level.getBlockState(candidate))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isFungalBlock(BlockState state) {
		return state.is(BlockTags.MUSHROOM_GROW_BLOCK)
				|| state.is(Blocks.BROWN_MUSHROOM)
				|| state.is(Blocks.RED_MUSHROOM)
				|| state.is(Blocks.MUSHROOM_STEM)
				|| state.is(Blocks.BROWN_MUSHROOM_BLOCK)
				|| state.is(Blocks.RED_MUSHROOM_BLOCK)
				|| state.is(Blocks.MYCELIUM);
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isAlive() && this.isInWater()) {
			Vec3 drift = this.waterDriftVector();
			this.setDeltaMovement(this.getDeltaMovement().scale(0.82D).add(drift));
			this.move(MoverType.SELF, this.getDeltaMovement());
			return;
		}
		super.travel(travelVector);
	}

	private Vec3 waterDriftVector() {
		LivingEntity target = this.getTarget();
		if (target != null) {
			Vec3 toTarget = target.position().subtract(this.position());
			if (toTarget.lengthSqr() > 0.01D) {
				return toTarget.normalize().scale(WATER_DRIFT_SPEED);
			}
		}
		double turn = (this.tickCount + this.getId()) * 0.071D;
		double y = this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER) ? 0.006D : -0.004D;
		return new Vec3(Math.cos(turn) * WATER_DRIFT_SPEED, y, Math.sin(turn) * WATER_DRIFT_SPEED);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		this.dropLayerHint(source);
	}

	private void dropLayerHint(DamageSource source) {
		if (!(source.getEntity() instanceof Player player)) {
			return;
		}
		List<MorphlingPolypLayer> layers = this.getLayers();
		if (layers.isEmpty()) {
			return;
		}
		int chance = Math.max(1, 5 - player.getMainHandItem().getEnchantmentLevel(this.level().holderOrThrow(
				net.minecraft.world.item.enchantment.Enchantments.LOOTING)));
		if (this.random.nextInt(chance) != 0) {
			return;
		}
		MorphlingPolypLayer layer = layers.get(this.random.nextInt(layers.size()));
		Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(layer.hintItemId()));
		if (item != Items.AIR) {
			this.spawnAtLocation(new ItemStack(item));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_MORPHLING_LAYERS, this.getLayerMask());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setLayerMask(tag.getInt(TAG_MORPHLING_LAYERS));
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		return false;
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SLIME_SQUISH_SMALL;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SLIME_DEATH_SMALL;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.SLIME_HURT_SMALL;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3F;
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hurt = super.doHurtTarget(target);
		if (hurt) {
			this.playSound(SoundEvents.SLIME_ATTACK, 0.45F, 0.7F + this.random.nextFloat() * 0.25F);
		}
		return hurt;
	}

	private static final class MorphlingPolypHopGoal extends Goal {
		private final MorphlingPolypEntity polyp;
		private int hopCooldown;
		private int attackCooldown;

		private MorphlingPolypHopGoal(MorphlingPolypEntity polyp) {
			this.polyp = polyp;
		}

		@Override
		public boolean canUse() {
			return true;
		}

		@Override
		public void tick() {
			if (this.attackCooldown > 0) {
				this.attackCooldown--;
			}
			LivingEntity target = this.polyp.getTarget();
			if (target != null && target.isAlive() && this.polyp.distanceToSqr(target) <= 1.8D
					&& this.attackCooldown <= 0) {
				this.polyp.doHurtTarget(target);
				this.attackCooldown = 20;
			}
			if (this.polyp.isInWater() || !this.polyp.onGround()) {
				return;
			}
			if (this.hopCooldown-- > 0) {
				return;
			}
			this.hopCooldown = target == null ? 28 + this.polyp.random.nextInt(28)
					: 12 + this.polyp.random.nextInt(12);
			Vec3 direction = this.pickDirection(target);
			double speed = target == null ? 0.12D : 0.22D;
			this.polyp.setDeltaMovement(direction.x * speed, 0.42D, direction.z * speed);
			this.polyp.hasImpulse = true;
			this.polyp.playSound(SoundEvents.SLIME_JUMP_SMALL, 0.3F,
					0.75F + this.polyp.random.nextFloat() * 0.2F);
			if (direction.horizontalDistanceSqr() > 1.0E-4D) {
				this.polyp.setYRot((float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F);
			}
		}

		private Vec3 pickDirection(@Nullable LivingEntity target) {
			if (target != null && target.isAlive()) {
				Vec3 toTarget = target.position().subtract(this.polyp.position());
				if (toTarget.horizontalDistanceSqr() > 0.01D) {
					return new Vec3(toTarget.x, 0.0D, toTarget.z).normalize();
				}
			}
			float angle = this.polyp.random.nextFloat() * Mth.TWO_PI;
			return new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
		}
	}
}
