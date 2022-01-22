package com.vincenthuto.hemomancy.entity.drudge;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.tool.ItemDrudgeElectrode;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.Vec3;

public class EntityDrudge extends TamableAnimal {

	private final SimpleContainer drudgeInventory = new SimpleContainer(1);
	public String roleTitle = "";
	private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(EntityDrudge.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> RANK = SynchedEntityData.defineId(EntityDrudge.class,
			EntityDataSerializers.INT);
	public static final Map<Integer, ResourceLocation> TEXTURE_BY_ID = Util.make(Maps.newHashMap(), (p_213410_0_) -> {
		p_213410_0_.put(0, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_red.png"));
		p_213410_0_.put(1, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_blue.png"));
		p_213410_0_.put(2, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_purple.png"));
		p_213410_0_.put(3, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_green.png"));
		p_213410_0_.put(4, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_yellow.png"));
		p_213410_0_.put(5, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_grey.png"));
		p_213410_0_.put(6, new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/drudge/model_drudge_brown.png"));

	});

	public EntityDrudge(EntityType<? extends EntityDrudge> type, Level worldIn) {
		super(type, worldIn);
	}

	public EnumDrudgeRoles getRoleTitle() {
		switch (this.getDrudgeRole()) {
		case 0:
			return EnumDrudgeRoles.ATTACK;
		case 1:
			return EnumDrudgeRoles.SEA;
		case 2:
			return EnumDrudgeRoles.PET;
		case 3:
			return EnumDrudgeRoles.HARVEST;
		case 4:
			return EnumDrudgeRoles.COLLECTOR;
		case 5:
			return EnumDrudgeRoles.BASE;
		case 6:
			return EnumDrudgeRoles.PLACER;
		default:
			return EnumDrudgeRoles.NOROLE;
		}
	}

	@Override
	public boolean canPickUpLoot() {
		return getRoleTitle() == EnumDrudgeRoles.COLLECTOR;
	}

	@Override
	public boolean canTakeItem(ItemStack itemstackIn) {
		return getRoleTitle() == EnumDrudgeRoles.COLLECTOR;
	}

	@Override
	public EntityDrudge getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
		EntityDrudge wolfentity = EntityInit.drudge.get().create(p_241840_1_);
		UUID uuid = this.getOwnerUUID();
		if (uuid != null) {
			wolfentity.setOwnerUUID(uuid);
			wolfentity.setTame(true);
		}

		return wolfentity;
	}

	@Override
	protected void setItemSlotAndDropWhenKilled(EquipmentSlot p_233657_1_, ItemStack p_233657_2_) {
		super.setItemSlotAndDropWhenKilled(p_233657_1_, p_233657_2_);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.put("Inventory", this.getDrudgeInventory().createTag());
		compound.putInt("Role", this.getDrudgeRole());
		compound.putInt("Rank", this.getDrudgeRank());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.getDrudgeInventory().fromTag(compound.getList("Inventory", 10));
		this.setDrudgeRole(compound.getInt("Role"));
		this.setDrudgeRank(compound.getInt("Rank"));
	}

	public SimpleContainer getDrudgeInventory() {
		return this.drudgeInventory;
	}

	public boolean wantsToPickUp(ItemStack p_230293_1_) {
		return (this.getDrudgeInventory().canAddItem(p_230293_1_));
	}

	public ResourceLocation getDrudgeRoleReLoc() {
		return TEXTURE_BY_ID.getOrDefault(this.getDrudgeRole(), TEXTURE_BY_ID.get(0));
	}

	public int getDrudgeRank() {
		return this.entityData.get(RANK);
	}

	public void setDrudgeRank(int type) {
		this.entityData.set(RANK, type);
	}

	public int getDrudgeRole() {
		return this.entityData.get(ROLE);
	}

	public void setDrudgeRole(int type) {
		if (type <= 0 || type >= 7) {
			type = this.random.nextInt(8);
		}

		this.entityData.set(ROLE, type);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return false;
	}

	@Override
	public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
		if (!player.level.isClientSide) {
			if (player.getItemInHand(hand).getItem() instanceof ItemDrudgeElectrode) {
				player.displayClientMessage(new TextComponent("Switching Roles").withStyle(ChatFormatting.RED), true);
				this.setDrudgeRole(this.random.nextInt(8));
			}
		}
		return super.interactAt(player, vec, hand);
	}

	@Override
	public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isTame()) {

		}
	}

	@Override
	protected void registerGoals() {
		this.targetSelector.addGoal(0, new DrudgeNearestAttackableTargetGoal<>(this, Villager.class, false));
		this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 2.5D, true));
		this.goalSelector.addGoal(0, new DrudgeExtractFromChestGoal(this, 2.5D, 10));
		this.goalSelector.addGoal(0, new DrudgeCollectItemGoal(this, 2.5D, 25));
		this.goalSelector.addGoal(0, new DrudgeEmptyToChestGoal(this, 2.5D, 25));
		this.goalSelector.addGoal(0, new DrudgeHarvestCropGoal(this, 2.5D, 25));
		this.goalSelector.addGoal(0, new DrudgeReplantCropGoal(this, 2.5D, 10));

	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0D).add(Attributes.MOVEMENT_SPEED, (double) 0.15F)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);

	}

	@Override
	public boolean canAttackType(EntityType<?> typeIn) {
		return (this.getRoleTitle() == EnumDrudgeRoles.ATTACK)
				&& !(typeIn == EntityInit.drudge.get() || typeIn == EntityType.PLAYER);
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.BAT_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.BAT_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.BAT_DEATH;
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume() {
		return 0.2F;
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemstack = itemEntity.getItem();
		if (this.wantsToPickUp(itemstack)) {
			SimpleContainer inventory = this.getDrudgeInventory();
			boolean flag = inventory.canAddItem(itemstack);
			if (!flag) {
				return;
			}

			this.onItemPickup(itemEntity);
			this.take(itemEntity, itemstack.getCount());
			ItemStack itemstack1 = inventory.addItem(itemstack);
			if (itemstack1.isEmpty()) {
				itemEntity.remove(RemovalReason.KILLED);
			} else {
				itemstack.setCount(itemstack1.getCount());
			}
			inventory.setChanged();
		}
	}

	// This is on right click with a certain item not neccisairly by player
	@Override
	public InteractionResult mobInteract(Player p_230254_1_, InteractionHand p_230254_2_) {
		ItemStack itemstack = p_230254_1_.getItemInHand(p_230254_2_);
		Item item = itemstack.getItem();
		if (item == ItemInit.drudge_submission_device.get() && !this.isTame()) {
			if (!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, p_230254_1_)) {
				this.tame(p_230254_1_);
				this.navigation.stop();
				this.setTarget((LivingEntity) null);
				this.setOrderedToSit(true);
				this.level.broadcastEntityEvent(this, (byte) 7);
			} else {
				this.level.broadcastEntityEvent(this, (byte) 6);
			}

			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(p_230254_1_, p_230254_2_);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ROLE, 0);
		this.entityData.define(RANK, 0);

	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.setDrudgeRole(this.random.nextInt(8));
		Level world = worldIn.getLevel();
		if (world instanceof ServerLevel && ((ServerLevel) world).structureFeatureManager()
				.getStructureAt(this.blockPosition(), StructureFeature.SWAMP_HUT).isValid()) {
			this.setDrudgeRole(1);
			this.setPersistenceRequired();
		}

		return spawnDataIn;

	}

	protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return this.isBaby() ? sizeIn.height * 0.1F : 1F;
	}

	public boolean isFarmItemInInventory() {
		return this.getDrudgeInventory()
				.hasAnyOf(ImmutableSet.of(Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS));
	}

}