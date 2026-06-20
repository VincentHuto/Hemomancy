package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoirContainer;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HematicArmatureBlockEntity extends BaseContainerBlockEntity implements IBloodReservoirContainer {
	public static final int SLOT_HEAD_REAGENT = 0;
	public static final int SLOT_CHEST_REAGENT = 1;
	public static final int SLOT_LEGS_REAGENT = 2;
	public static final int SLOT_FEET_REAGENT = 3;
	public static final int SLOT_BLOOD_INPUT = 4;
	public static final int SLOT_EMPTY_OUTPUT = 5;
	public static final int INVENTORY_SIZE = 6;
	public static final int MAX_BLOOD = 8_000;
	public static final int PROCESS_INTERVAL_TICKS = 100;
	private static final double HAND_GOURD_TRANSFER_TICKS = 20.0D;
	private static final DustParticleOptions CRIMSON_FLASH =
			new DustParticleOptions(new Vector3f(0.85F, 0.02F, 0.02F), 2.4F);
	private static final double[][] BOWL_PARTICLE_OFFSETS = new double[][] {
			{ -2.625D, 1.56D, 0.25D },
			{ 2.625D, 1.56D, 0.25D },
			{ -1.8125D, 1.38D, 1.0625D },
			{ 1.8125D, 1.38D, 1.0625D }
	};

	private static final String TAG_RESTRAINED_PLAYER = "RestrainedPlayer";
	private static final String TAG_BLOOD_LEVEL = "BloodLevel";
	private static final String TAG_ARMATURE_TIER = "ArmatureTier";

	private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	@Nullable
	private UUID restrainedPlayer;
	@Nullable
	private ArmatureUpgradeRules.ArmatureSlot pendingSlot;
	private int pendingBowlSlot = -1;
	private long craftStartTick = -1L;
	private ArmatureUpgradeRules.ArmatureTier armatureTier = ArmatureUpgradeRules.ArmatureTier.BASE;

	public HematicArmatureBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.hematic_armature.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, HematicArmatureBlockEntity armature) {
		boolean changed = armature.processBloodContainerInputSlot();
		changed |= armature.tryProcessRestrainedPlayer(level);
		if (changed) {
			armature.sendUpdates();
			setChanged(level, pos, state);
		}
	}

	private boolean tryProcessRestrainedPlayer(Level level) {
		if (restrainedPlayer == null || !(level instanceof ServerLevel serverLevel)) {
			return false;
		}
		long now = level.getGameTime();

		ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(restrainedPlayer);
		if (!isValidRestrainedPlayer(player)) {
			clearRestrainedPlayer(null);
			return true;
		}

		IBloodVolume reservoir = getBloodCapability();
		if (reservoir == null) {
			return false;
		}

		List<ArmatureUpgradeRecipe> recipes = ArmatureUpgradeRecipe.getAllRecipes(level);
		for (ArmatureUpgradeRules.ArmatureSlot slot : ArmatureUpgradeRules.processingOrder()) {
			EquipmentSlot equipmentSlot = toEquipmentSlot(slot);
			ItemStack worn = player.getItemBySlot(equipmentSlot);
			if (worn.isEmpty()) {
				continue;
			}
			MatchedUpgrade match = findMatchingUpgrade(recipes, slot, worn, player);
			if (match == null) {
				continue;
			}
			ArmatureUpgradeRecipe recipe = match.recipe();
			if (reservoir.getBloodVolume() < recipe.getBloodCost()) {
				if (!isPendingCraft(slot, match.bowlSlot())
						|| now - craftStartTick >= PROCESS_INTERVAL_TICKS) {
					startPendingCraft(slot, match.bowlSlot(), now);
					player.sendSystemMessage(Component.translatable(
							"block.hemomancy.hematic_armature.insufficient_blood",
							(int) recipe.getBloodCost()).withStyle(ChatFormatting.DARK_RED));
				}
				return false;
			}

			if (!isPendingCraft(slot, match.bowlSlot())) {
				startPendingCraft(slot, match.bowlSlot(), now);
				spawnWindupEffects(serverLevel, player, match.bowlSlot(), 0.0D);
				return false;
			}

			long elapsed = now - craftStartTick;
			if (elapsed < PROCESS_INTERVAL_TICKS) {
				spawnWindupEffects(serverLevel, player, match.bowlSlot(),
						(double) elapsed / (double) PROCESS_INTERVAL_TICKS);
				return false;
			}

			ItemStack upgraded = recipe.createResult(level.registryAccess());
			mergeCustomData(worn, upgraded);
			copyDamage(worn, upgraded);
			player.setItemSlot(equipmentSlot, upgraded);
			ItemStack reagent = match.reagent();
			reagent.shrink(1);
			if (reagent.isEmpty()) {
				setItem(match.bowlSlot(), ItemStack.EMPTY);
			}
			reservoir.drain(recipe.getBloodCost());
			spawnUpgradeEffects(serverLevel, player, match.bowlSlot());
			clearPendingCraft();
			player.sendSystemMessage(Component.translatable(
					"block.hemomancy.hematic_armature.upgraded",
					upgraded.getHoverName()).withStyle(ChatFormatting.DARK_RED));
			return true;
		}
		clearPendingCraft();
		return false;
	}

	@Nullable
	private MatchedUpgrade findMatchingUpgrade(List<ArmatureUpgradeRecipe> recipes,
			ArmatureUpgradeRules.ArmatureSlot slot, ItemStack worn, Player player) {
		for (int bowlSlot : ArmatureUpgradeRules.bowlSlotsInInsertionOrder()) {
			ItemStack reagent = getItem(bowlSlot);
			if (reagent.isEmpty()) {
				continue;
			}
			ArmatureUpgradeRecipe recipe = findMatchingRecipe(recipes, slot, worn, reagent, player, armatureTier);
			if (recipe != null) {
				return new MatchedUpgrade(recipe, bowlSlot, reagent);
			}
		}
		return null;
	}

	private boolean isValidRestrainedPlayer(@Nullable ServerPlayer player) {
		return player != null
				&& player.isAlive()
				&& player.getVehicle() instanceof ArmatureRestraintEntity restraint
				&& restraint.isForArmature(worldPosition);
	}

	@Nullable
	private static ArmatureUpgradeRecipe findMatchingRecipe(List<ArmatureUpgradeRecipe> recipes,
			ArmatureUpgradeRules.ArmatureSlot slot, ItemStack worn, ItemStack reagent, Player player,
			ArmatureUpgradeRules.ArmatureTier armatureTier) {
		for (ArmatureUpgradeRecipe recipe : recipes) {
			if (recipe.matchesUpgrade(slot, worn, reagent, player, armatureTier)) {
				return recipe;
			}
		}
		return null;
	}

	private void spawnUpgradeEffects(ServerLevel serverLevel, ServerPlayer player, int bowlSlot) {
		Vec3 bowl = bowlParticlePosition(bowlSlot);
		serverLevel.sendParticles(ParticleTypes.FLAME,
				bowl.x, bowl.y, bowl.z,
				14, 0.14D, 0.08D, 0.14D, 0.01D);
		serverLevel.sendParticles(ParticleTypes.SMOKE,
				bowl.x, bowl.y + 0.04D, bowl.z,
				6, 0.10D, 0.04D, 0.10D, 0.01D);

		double playerY = player.getY() + player.getBbHeight() * 0.55D;
		serverLevel.sendParticles(CRIMSON_FLASH,
				player.getX(), playerY, player.getZ(),
				48, 0.62D, 0.86D, 0.62D, 0.0D);
		serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
				player.getX(), playerY, player.getZ(),
				26, 0.50D, 0.68D, 0.50D, 0.02D);
	}

	private void spawnWindupEffects(ServerLevel serverLevel, ServerPlayer player, int bowlSlot, double progress) {
		if (serverLevel.getGameTime() % 5L != 0L) {
			return;
		}
		Vec3 bowl = bowlParticlePosition(bowlSlot);
		double spread = 0.04D + progress * 0.10D;
		serverLevel.sendParticles(ParticleTypes.FLAME,
				bowl.x, bowl.y, bowl.z,
				2, spread, 0.04D, spread, 0.005D);
		serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
				player.getX(), player.getY() + player.getBbHeight() * 0.55D, player.getZ(),
				2, 0.25D + progress * 0.20D, 0.35D + progress * 0.20D,
				0.25D + progress * 0.20D, 0.01D);
	}

	private Vec3 bowlParticlePosition(int bowlSlot) {
		int index = Math.max(0, Math.min(BOWL_PARTICLE_OFFSETS.length - 1, bowlSlot));
		double[] offset = BOWL_PARTICLE_OFFSETS[index];
		BlockState state = getBlockState();
		Direction facing = state.hasProperty(HematicArmatureBlock.FACING)
				? state.getValue(HematicArmatureBlock.FACING)
				: Direction.SOUTH;
		Vec3 rotated = rotateBowlOffset(offset[0], offset[2], facing);
		return new Vec3(
				worldPosition.getX() + 0.5D + rotated.x,
				worldPosition.getY() + offset[1],
				worldPosition.getZ() + 0.5D + rotated.z);
	}

	private static Vec3 rotateBowlOffset(double x, double z, Direction facing) {
		return switch (facing) {
			case NORTH -> new Vec3(-x, 0.0D, -z);
			case EAST -> new Vec3(-z, 0.0D, x);
			case WEST -> new Vec3(z, 0.0D, -x);
			default -> new Vec3(x, 0.0D, z);
		};
	}

	private static EquipmentSlot toEquipmentSlot(ArmatureUpgradeRules.ArmatureSlot slot) {
		return switch (slot) {
			case HEAD -> EquipmentSlot.HEAD;
			case CHEST -> EquipmentSlot.CHEST;
			case LEGS -> EquipmentSlot.LEGS;
			case FEET -> EquipmentSlot.FEET;
		};
	}

	private boolean isPendingCraft(ArmatureUpgradeRules.ArmatureSlot slot, int bowlSlot) {
		return pendingSlot == slot && pendingBowlSlot == bowlSlot && craftStartTick >= 0L;
	}

	private void startPendingCraft(ArmatureUpgradeRules.ArmatureSlot slot, int bowlSlot, long now) {
		pendingSlot = slot;
		pendingBowlSlot = bowlSlot;
		craftStartTick = now;
	}

	private void clearPendingCraft() {
		pendingSlot = null;
		pendingBowlSlot = -1;
		craftStartTick = -1L;
	}

	private record MatchedUpgrade(ArmatureUpgradeRecipe recipe, int bowlSlot, ItemStack reagent) {
	}

	private static void copyDamage(ItemStack from, ItemStack to) {
		if (!from.isDamageableItem() || !to.isDamageableItem() || from.getMaxDamage() <= 0) {
			return;
		}
		float damageRatio = (float) from.getDamageValue() / (float) from.getMaxDamage();
		to.setDamageValue(Math.min(to.getMaxDamage() - 1, Math.round(damageRatio * to.getMaxDamage())));
	}

	private static void mergeCustomData(ItemStack from, ItemStack to) {
		CustomData baseData = from.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		CustomData resultData = to.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		CompoundTag merged = baseData.copyTag();
		merged.merge(resultData.copyTag());
		if (!merged.isEmpty()) {
			to.set(DataComponents.CUSTOM_DATA, CustomData.of(merged));
		}
	}

	public boolean insertHeldBowlItems(ServerPlayer player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty() || BloodContainerTransfer.isFilledBloodContainer(held)
				|| BloodContainerTransfer.isBloodGourd(held)) {
			return false;
		}

		int slot = ArmatureUpgradeRules.nextInsertableBowlSlot(occupiedBowlSlots());
		if (slot < 0) {
			return false;
		}
		int moved = ArmatureUpgradeRules.insertedCountForBowl(getItem(slot).isEmpty(), held.getCount());
		if (moved <= 0) {
			return false;
		}
		ItemStack inserted = held.copy();
		inserted.setCount(moved);
		setItem(slot, inserted);
		if (!player.getAbilities().instabuild) {
			held.shrink(moved);
		}
		sendUpdates();
		return true;
	}

	private List<Boolean> occupiedBowlSlots() {
		return List.of(
				!getItem(SLOT_HEAD_REAGENT).isEmpty(),
				!getItem(SLOT_CHEST_REAGENT).isEmpty(),
				!getItem(SLOT_LEGS_REAGENT).isEmpty(),
				!getItem(SLOT_FEET_REAGENT).isEmpty());
	}

	public boolean extractMostRecentBowlItem(ServerPlayer player) {
		for (int slot : ArmatureUpgradeRules.bowlSlotsInWithdrawalOrder()) {
			ItemStack removed = removeItemNoUpdate(slot);
			if (removed.isEmpty()) {
				continue;
			}
			giveOrDrop(player, removed);
			sendUpdates();
			return true;
		}
		return false;
	}

	public boolean useBloodContainerInHand(ServerPlayer player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty()) {
			return false;
		}

		IBloodVolume reservoir = getBloodCapability();
		if (reservoir == null || reservoir.isFull()) {
			return false;
		}

		if (held.getItem() instanceof BloodyFlaskItem flask) {
			double amount = flask.getAmount();
			if (!BloodContainerTransfer.canAcceptFilledContainer(
					reservoir.getBloodVolume(), reservoir.getMaxBloodVolume(), amount)) {
				return false;
			}
			if (!player.getAbilities().instabuild) {
				held.shrink(1);
				giveOrDrop(player, new ItemStack(HLItemInit.cured_clay_flask.get()));
			}
			reservoir.addBloodVolume(amount);
			sendUpdates();
			return true;
		}

		if (held.getItem() instanceof BloodGourdItem) {
			IBloodVolume gourdVolume = HemoCapabilityAccess.getBloodVolume(held).orElse(null);
			if (gourdVolume == null) {
				return false;
			}
			double transfer = BloodContainerTransfer.calculateGourdTransfer(
					gourdVolume.getBloodVolume(),
					reservoir.getBloodVolume(),
					reservoir.getMaxBloodVolume(),
					getBloodGourdInputTransferRate() * HAND_GOURD_TRANSFER_TICKS);
			if (transfer <= 0.0D) {
				return false;
			}
			gourdVolume.subtractBloodVolume(transfer);
			reservoir.addBloodVolume(transfer);
			sendUpdates();
			return true;
		}

		return false;
	}

	public boolean applyArmatureUpgradeItem(ServerPlayer player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty()) {
			return false;
		}

		ArmatureUpgradeRules.ArmatureTier targetTier = null;
		int requiredDegree = 0;
		if (held.is(ItemInit.vicars_consecration_kit.get())) {
			targetTier = ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED;
			requiredDegree = 5;
		} else if (held.is(ItemInit.monolithic_cornerstone.get())) {
			targetTier = ArmatureUpgradeRules.ArmatureTier.MONOLITHIC;
			requiredDegree = 7;
		}
		if (targetTier == null) {
			return false;
		}
		if (armatureTier.id() >= targetTier.id()) {
			player.sendSystemMessage(Component.translatable(
					"block.hemomancy.hematic_armature.upgrade_already_applied").withStyle(ChatFormatting.DARK_RED));
			return true;
		}
		if (targetTier == ArmatureUpgradeRules.ArmatureTier.MONOLITHIC
				&& armatureTier != ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED) {
			player.sendSystemMessage(Component.translatable(
					"block.hemomancy.hematic_armature.cornerstone_requires_vicar").withStyle(ChatFormatting.DARK_RED));
			return true;
		}
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < requiredDegree) {
			player.sendSystemMessage(Component.translatable(
					"block.hemomancy.hematic_armature.upgrade_requires_degree",
					requiredDegree).withStyle(ChatFormatting.DARK_RED));
			return true;
		}

		armatureTier = targetTier;
		clearPendingCraft();
		if (!player.getAbilities().instabuild) {
			held.shrink(1);
		}
		sendUpdates();
		player.sendSystemMessage(Component.translatable(
				"block.hemomancy.hematic_armature.upgrade_tier_applied",
				Component.translatable("block.hemomancy.hematic_armature.tier."
						+ targetTier.serializedName())).withStyle(ChatFormatting.DARK_RED));
		return true;
	}

	private static void giveOrDrop(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	public void setRestrainedPlayer(UUID playerId) {
		this.restrainedPlayer = playerId;
		clearPendingCraft();
		sendUpdates();
	}

	@Nullable
	public UUID getRestrainedPlayer() {
		return restrainedPlayer;
	}

	public boolean hasRestrainedPlayer() {
		return restrainedPlayer != null;
	}

	public ArmatureUpgradeRules.ArmatureTier getArmatureTier() {
		return armatureTier;
	}

	public void dropAppliedUpgradeItems(Level level, BlockPos pos) {
		if (armatureTier.id() >= ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED.id()) {
			Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					new ItemStack(ItemInit.vicars_consecration_kit.get()));
		}
		if (armatureTier.id() >= ArmatureUpgradeRules.ArmatureTier.MONOLITHIC.id()) {
			Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					new ItemStack(ItemInit.monolithic_cornerstone.get()));
		}
	}

	public void clearRestrainedPlayer(@Nullable UUID playerId) {
		if (restrainedPlayer == null) return;
		if (playerId == null || restrainedPlayer.equals(playerId)) {
			restrainedPlayer = null;
			clearPendingCraft();
			sendUpdates();
		}
	}

	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

	@Nullable
	@Override
	public IBloodVolume getBloodCapability() {
		return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
	}

	@Override
	public int getBloodContainerInputSlot() {
		return SLOT_BLOOD_INPUT;
	}

	@Override
	public int getEmptyBloodContainerOutputSlot() {
		return SLOT_EMPTY_OUTPUT;
	}

	@Override
	public boolean acceptsBloodGourdInput() {
		return true;
	}

	@Override
	public double getBloodGourdInputTransferRate() {
		return 40D;
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
		if (!removed.isEmpty()) {
			setChanged();
		}
		return removed;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == SLOT_EMPTY_OUTPUT) {
			return false;
		}
		if (slot == SLOT_BLOOD_INPUT) {
			return BloodContainerTransfer.isFilledBloodContainer(stack)
					|| BloodContainerTransfer.isBloodGourd(stack);
		}
		return slot >= SLOT_HEAD_REAGENT && slot <= SLOT_FEET_REAGENT;
	}

	@Override
	public boolean stillValid(Player player) {
		return level != null && level.getBlockEntity(worldPosition) == this
				&& player.distanceToSqr(worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public void clearContent() {
		items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		for (int i = 0; i < Math.min(items.size(), INVENTORY_SIZE); i++) {
			this.items.set(i, items.get(i));
		}
	}

	@Override
	@Nullable
	protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
		return null;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.hematic_armature");
	}

	@Override
	public void onLoad() {
		IBloodVolume volume = getBloodCapability();
		if (volume != null) {
			volume.setActive(true);
			volume.setMaxBloodVolume(MAX_BLOOD);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, items, registries);
		restrainedPlayer = tag.hasUUID(TAG_RESTRAINED_PLAYER) ? tag.getUUID(TAG_RESTRAINED_PLAYER) : null;
		armatureTier = tag.contains(TAG_ARMATURE_TIER)
				? ArmatureUpgradeRules.ArmatureTier.byName(tag.getString(TAG_ARMATURE_TIER))
				: ArmatureUpgradeRules.ArmatureTier.BASE;
		IBloodVolume volume = getBloodCapability();
		if (volume != null) {
			volume.setActive(true);
			volume.setMaxBloodVolume(MAX_BLOOD);
			volume.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ContainerHelper.saveAllItems(tag, items, registries);
		if (restrainedPlayer != null) {
			tag.putUUID(TAG_RESTRAINED_PLAYER, restrainedPlayer);
		}
		tag.putString(TAG_ARMATURE_TIER, armatureTier.serializedName());
		IBloodVolume volume = getBloodCapability();
		if (volume != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, items, registries);
		if (restrainedPlayer != null) {
			tag.putUUID(TAG_RESTRAINED_PLAYER, restrainedPlayer);
		}
		tag.putString(TAG_ARMATURE_TIER, armatureTier.serializedName());
		IBloodVolume volume = getBloodCapability();
		if (volume != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, items, registries);
		restrainedPlayer = tag.hasUUID(TAG_RESTRAINED_PLAYER) ? tag.getUUID(TAG_RESTRAINED_PLAYER) : null;
		armatureTier = tag.contains(TAG_ARMATURE_TIER)
				? ArmatureUpgradeRules.ArmatureTier.byName(tag.getString(TAG_ARMATURE_TIER))
				: ArmatureUpgradeRules.ArmatureTier.BASE;
		IBloodVolume volume = getBloodCapability();
		if (volume != null) {
			volume.setActive(true);
			volume.setMaxBloodVolume(MAX_BLOOD);
			volume.setBloodVolume(tag.getDouble(TAG_BLOOD_LEVEL));
		}
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
			HolderLookup.Provider registries) {
		if (pkt.getTag() != null) {
			handleUpdateTag(pkt.getTag(), registries);
		}
	}

	@Override
	public void sendUpdates() {
		if (level == null) return;
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}
}
