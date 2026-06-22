package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MorphlingIncubatorMenu;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MorphlingIncubatorBlockEntity extends BaseContainerBlockEntity implements IBloodReservoir, IBloodContainerSlotAccess {

	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	private static final int CRAFT_TIME = 200;
	private static final int ENZYME_TIME_BASE = 100;
	private static final int ENZYME_TIME_PER_ITEM = 60;
	private static final float BLOOD_COST_PER_TICK = 0.5f;

	// Slot layout:
	// 0 = center (polyp or morphling)
	// 1-4 = catalysts / enzymes
	// 5 = output
	// 6 = blood flask/gourd
	public static final int SLOT_CENTER = 0;
	public static final int SLOT_CATALYST_START = 1;
	public static final int SLOT_CATALYST_END = 4;
	public static final int SLOT_OUTPUT = 5;
	public static final int SLOT_BLOOD = 6;
	public static final int SLOT_FLASK_OUTPUT = 7;
	public static final int INVENTORY_SIZE = 8;

	public NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

	int craftingProgress;
	int craftingTotalTime;
	int mode; // 0 = idle, 1 = polyp crafting, 2 = enzyme feeding

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> craftingProgress;
				case 1 -> craftingTotalTime;
				case 2 -> mode;
				default -> 0;
			};
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public void set(int index, int val) {
			switch (index) {
				case 0 -> craftingProgress = val;
				case 1 -> craftingTotalTime = val;
				case 2 -> mode = val;
			}
		}
	};

	public MorphlingIncubatorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.morphling_incubator.get(), pos, state);
	}

	// ---- Lazy capability access ----

	@Nullable
	private IBloodVolume resolveVolume() {
		return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
	}

	// ---- Ticking ----

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			MorphlingIncubatorBlockEntity self) {
	}

	@Override
	public void onLoad() {
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setActive(true);
			vol.setMaxBloodVolume(2500f);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState blockState, MorphlingIncubatorBlockEntity te) {
		// Process blood flask / gourd in the blood slot
		te.processBloodSlot();

		boolean changed = false;

		if (te.craftingProgress > 0) {
			// Check blood — consume per tick
			IBloodVolume vol = te.resolveVolume();
			if (vol != null && vol.getBloodVolume() >= BLOOD_COST_PER_TICK) {
				vol.subtractBloodVolume(BLOOD_COST_PER_TICK);
				te.craftingProgress--;
				changed = true;

				HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.MYCELIUM);

				if (te.craftingProgress <= 0) {
					HLParticleUtils.spawnPoof((ServerLevel) level, pos, ParticleTypes.REVERSE_PORTAL);
					te.finishCrafting();
				}
			}
			// Not enough blood — pause (don't reset progress)
		} else {
			// Try to start a new craft
			if (te.tryStartPolypCraft()) {
				changed = true;
			} else if (te.tryStartEnzymeFeed()) {
				changed = true;
			} else {
				if (te.mode != 0) {
					te.mode = 0;
					changed = true;
				}
			}
		}

		if (changed) {
			te.sendUpdates();
			setChanged(level, pos, blockState);
		}
	}

	// ---- Blood slot processing ----

	private void processBloodSlot() {
		if (processBloodContainerInputSlot(this, this)) sendUpdates();
	}

	@Override
	public int getBloodContainerInputSlot() {
		return SLOT_BLOOD;
	}

	@Override
	public int getEmptyBloodContainerOutputSlot() {
		return SLOT_FLASK_OUTPUT;
	}

	@Override
	public boolean acceptsBloodGourdInput() {
		return true;
	}

	// ---- Polyp crafting ----

	private boolean tryStartPolypCraft() {
		IncubatorRecipe recipe = findMatchingRecipe();
		if (recipe != null && inventory.get(SLOT_OUTPUT).isEmpty()) {
			craftingProgress = CRAFT_TIME;
			craftingTotalTime = CRAFT_TIME;
			mode = 1;
			return true;
		}
		return false;
	}

	@Nullable
	public IncubatorRecipe findMatchingRecipe() {
		ItemStack center = inventory.get(SLOT_CENTER);
		if (center.isEmpty() || center.getItem() != ItemInit.morphling_polyp.get()) {
			return null;
		}
		List<Item> catalysts = collectCatalystItems();
		if (catalysts.isEmpty()) {
			return null;
		}
		if (level == null) return null;
		for (var recipeHolder : level.getRecipeManager().getAllRecipesFor(RecipeInit.incubator_recipe_type.get())) {
			if (recipeHolder.value().matchesCatalysts(catalysts)) {
				return recipeHolder.value();
			}
		}
		return null;
	}

	private List<Item> collectCatalystItems() {
		List<Item> items = new ArrayList<>();
		for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
			ItemStack stack = inventory.get(i);
			if (!stack.isEmpty()) {
				items.add(stack.getItem());
			}
		}
		return items;
	}

	// ---- Enzyme feeding ----

	private boolean tryStartEnzymeFeed() {
		ItemStack center = inventory.get(SLOT_CENTER);
		if (center.isEmpty() || !(center.getItem() instanceof IMorphling)) {
			return false;
		}
		// Need at least one enzyme in catalyst slots
		int enzymeCount = 0;
		for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
			if (inventory.get(i).getItem() instanceof EnzymeItem || inventory.get(i).getItem() instanceof RecycledEnzymeItem) {
				enzymeCount++;
			}
		}
		if (enzymeCount == 0) return false;
		if (!inventory.get(SLOT_OUTPUT).isEmpty()) return false;

		// More enzymes = proportionally longer craft time
		int totalTime = ENZYME_TIME_BASE + (enzymeCount * ENZYME_TIME_PER_ITEM);
		craftingProgress = totalTime;
		craftingTotalTime = totalTime;
		mode = 2;
		return true;
	}

	// ---- Finish crafting ----

	private void finishCrafting() {
		if (mode == 1) {
			finishPolypCraft();
		} else if (mode == 2) {
			finishEnzymeFeed();
		}
		craftingProgress = 0;
		craftingTotalTime = 0;
		mode = 0;
		sendUpdates();
	}

	private void finishPolypCraft() {
		IncubatorRecipe recipe = findMatchingRecipe();
		if (recipe == null) {
			return;
		}

		// Consume center polyp
		inventory.get(SLOT_CENTER).shrink(1);

		// Consume catalysts
		for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
			inventory.get(i).shrink(1);
		}

		// Place result in output
		inventory.set(SLOT_OUTPUT, recipe.getResultItemStack().copy());
	}

	private void finishEnzymeFeed() {
		ItemStack center = inventory.get(SLOT_CENTER);
		if (center.isEmpty() || !(center.getItem() instanceof IMorphling morphling)) return;

		// Gather the morphling's enzyme preferences
		EnumBloodTendency preferred = morphling.getPreferredTendency();
		EnumBloodTendency secondary = morphling.getSecondaryTendency();

		// Count enzymes and compute total effective strength with preference scaling
		int enzymeCount = 0;
		float totalStrength = 0;
		for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
			ItemStack stack = inventory.get(i);
			if (stack.getItem() instanceof EnzymeItem enzyme) {
				enzymeCount++;
				totalStrength += MorphlingItem.calculateEffectivePower(
						enzyme.getAmount(), enzyme.getTend(), preferred, secondary);
			} else if (stack.getItem() instanceof RecycledEnzymeItem recycled) {
				enzymeCount++;
				totalStrength += MorphlingItem.calculateEffectivePower(
						recycled.getAmount(), recycled.getTend(), preferred, secondary);
			}
		}

		if (enzymeCount == 0) return;

		// Consume enzymes
		for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
			ItemStack stack = inventory.get(i);
			if (stack.getItem() instanceof EnzymeItem || stack.getItem() instanceof RecycledEnzymeItem) {
				stack.shrink(1);
			}
		}

		// Remove center morphling and create enhanced copy in output
		ItemStack result = center.copy();

		// Store enzyme power as NBT on the morphling
		CompoundTag tag = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		float existingPower = tag.getFloat("EnzymePower");
		tag.putFloat("EnzymePower", existingPower + totalStrength);
		int existingFeedings = tag.getInt("EnzymeFeedings");
		tag.putInt("EnzymeFeedings", existingFeedings + enzymeCount);
		result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

		// Move center to output
		inventory.set(SLOT_CENTER, ItemStack.EMPTY);
		inventory.set(SLOT_OUTPUT, result);
	}

	// ---- Inventory helpers (for renderer) ----

	public ItemStack getCenterStack() {
		return inventory.get(SLOT_CENTER);
	}

	public List<ItemStack> getCatalystStacks() {
		return inventory.subList(SLOT_CATALYST_START, SLOT_CATALYST_END + 1);
	}

	// ---- Container implementation ----

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : inventory) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ContainerHelper.removeItem(inventory, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(inventory, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		ItemStack oldStack = inventory.get(slot);
		boolean isSameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, oldStack);
		inventory.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		// Reset crafting progress when an input slot changes during active crafting
		if (!isSameItem && (slot == SLOT_CENTER || (slot >= SLOT_CATALYST_START && slot <= SLOT_CATALYST_END))) {
			if (craftingProgress > 0) {
				craftingProgress = 0;
				craftingTotalTime = 0;
				mode = 0;
				setChanged();
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.level.getBlockEntity(this.worldPosition) != this) return false;
		return player.distanceToSqr(this.worldPosition.getX() + 0.5D,
				this.worldPosition.getY() + 0.5D,
				this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void clearContent() {
		inventory.clear();
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.inventory = items;
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
		return new MorphlingIncubatorMenu(containerId, playerInventory, this, this.dataAccess);
	}

	@Override
	protected Component getDefaultName() {
		return Component.literal("Morphling Incubator");
	}

	// ---- Serialization ----

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.inventory, registries);
		this.craftingProgress = tag.getInt("CraftProgress");
		this.craftingTotalTime = tag.getInt("CraftTotalTime");
		this.mode = tag.getInt("Mode");
		IBloodVolume vol = resolveVolume();
		if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
			vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ContainerHelper.saveAllItems(tag, this.inventory, registries);
		tag.putInt("CraftProgress", this.craftingProgress);
		tag.putInt("CraftTotalTime", this.craftingTotalTime);
		tag.putInt("Mode", this.mode);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.inventory, registries);
		tag.putInt("CraftProgress", this.craftingProgress);
		tag.putInt("CraftTotalTime", this.craftingTotalTime);
		tag.putInt("Mode", this.mode);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.inventory, registries);
		this.craftingProgress = tag.getInt("CraftProgress");
		this.craftingTotalTime = tag.getInt("CraftTotalTime");
		this.mode = tag.getInt("Mode");
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			IBloodVolume vol = resolveVolume();
			if (vol != null && tag.contains(TAG_BLOOD_LEVEL)) {
				vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			}
		}
	}

	// ---- Blood access ----

	@Nullable
	public IBloodVolume getBloodCapability() {
		return resolveVolume();
	}

	public double getBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getBloodVolume() : 0;
	}

	public double getMaxBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getMaxBloodVolume() : 0;
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

}
