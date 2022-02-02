package com.vincenthuto.hemomancy.entity.brain;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.NetworkHooks;

public class EntityBrainTest extends PathfinderMob implements InventoryCarrier {

	private final SimpleContainer inventory = new SimpleContainer(8);
	private static final EntityDataAccessor<EntityBrainData> DATA_VILLAGER_DATA = (EntityDataAccessor<EntityBrainData>) SynchedEntityData
			.defineId(EntityBrainTest.class, SensorInit.brain_data.get().getSerializer());
	/** Mapping between valid food items and their respective efficiency values. */
	public static final Map<Item, Integer> FOOD_POINTS = ImmutableMap.of(Items.BREAD, 4, Items.POTATO, 1, Items.CARROT,
			1, Items.BEETROOT, 1);
	private static final Set<Item> WANTED_ITEMS = ImmutableSet.of(Items.BREAD, Items.POTATO, Items.CARROT, Items.WHEAT,
			Items.WHEAT_SEEDS, Items.BEETROOT, Items.BEETROOT_SEEDS);
	private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME,
			MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT,
			MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
			MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE,
			MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY,
			MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_WORKED_AT_POI);
	private static final ImmutableList<SensorType<? extends Sensor<? super EntityBrainTest>>> SENSOR_TYPES = ImmutableList
			.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS,
					SensorType.HURT_BY, SensorInit.secondary_brain_poi.get());

	public static final Map<MemoryModuleType<GlobalPos>, BiPredicate<EntityBrainTest, PoiType>> POI_MEMORIES = ImmutableMap
			.of(MemoryModuleType.HOME, (p_35493_, p_35494_) -> {
				return p_35494_ == PoiType.HOME;
			}, MemoryModuleType.JOB_SITE, (p_35486_, p_35487_) -> {
				return p_35486_.getEntityBrainTestData().getProfession().getJobPoiType() == p_35487_;
			}, MemoryModuleType.POTENTIAL_JOB_SITE, (p_35469_, p_35470_) -> {
				return PoiType.ALL_JOBS.test(p_35470_);
			}, MemoryModuleType.MEETING_POINT, (p_35434_, p_35435_) -> {
				return p_35435_ == PoiType.MEETING;
			});

	public EntityBrainTest(EntityType<? extends EntityBrainTest> type, Level worldIn) {
		super(type, worldIn);
		((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
		this.getNavigation().setCanFloat(true);
		this.setCanPickUpLoot(true);
		this.setEntityBrainTestData(this.getEntityBrainTestData().setProfession(VillagerProfession.NONE));

	}

	public Brain<?> getBrain() {
		return  super.getBrain();
	}

	protected Brain.Provider<EntityBrainTest> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
		Brain<EntityBrainTest> brain = this.brainProvider().makeBrain(pDynamic);
		this.registerBrainGoals(brain);
		return brain;
	}

	public void refreshBrain(ServerLevel pServerLevel) {
		Brain<EntityBrainTest> brain = (Brain<EntityBrainTest>) this.getBrain();
		brain.stopAll(pServerLevel, this);
		this.brain = brain.copyWithoutBehaviors();
		this.registerBrainGoals(this.getBrain());
	}

	private void registerBrainGoals(Brain<?> brain) {

	}

	public void setEntityBrainTestData(EntityBrainData entityBrainData) {
		EntityBrainData villagerdata = this.getEntityBrainTestData();
		if (villagerdata.getProfession() != entityBrainData.getProfession()) {
		}

		this.entityData.set(DATA_VILLAGER_DATA, entityBrainData);
	}

	public EntityBrainData getEntityBrainTestData() {
		return this.entityData.get(DATA_VILLAGER_DATA);
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.populateDefaultEquipmentSlots(difficultyIn);

		return spawnDataIn;

	}

	@Override
	public void tick() {
		super.tick();

	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
	}

	@Override
	protected void registerGoals() {

	}
	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.15D).add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
	}

	@Override
	public SimpleContainer getInventory() {
		return this.inventory;
	}

}
