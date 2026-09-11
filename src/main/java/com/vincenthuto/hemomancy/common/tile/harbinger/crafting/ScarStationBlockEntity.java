package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.List;

public class ScarStationBlockEntity extends BaseContainerBlockEntity implements MenuProvider, IMultiBlockEntity {
	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);

	public int numPlayersUsing = 0;
	public float lidAngle, prevLidAngle;
	public final String TAG_scarList = "scarList";
	public byte[][] scarsList = ScarRecipe.blank();
	public byte[][] clientScarList;

	ScarRecipe currentRecipe;

	public ScarStationBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.scar_station.get(), pos, state);
	}

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}


	public void clearScarList() {
		scarsList = ScarRecipe.blank();
		this.sendUpdates();
	}

	public void setScarList(byte[][] bs) {
		scarsList = bs;
		this.sendUpdates();

	}

	@Override
	public int getContainerSize() {
		return 5;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.contents) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	public boolean areScarsMatching() {
		return this.getCurrentRecipe() != null && Arrays.deepEquals(currentRecipe.getPattern(), scarsList);

	}

	public ScarRecipe getCurrentRecipe() {
		if (level == null) {
			currentRecipe = null;
			return null;
		}

		for (ScarRecipe recipe : ScarRecipe.getAllRecipes(level)) {
			if (recipe.getIngredients().size() == 1) {
				if (recipe.getIngredients().get(0).test(this.getItems().get(0))) {
					currentRecipe = recipe;
					return currentRecipe;
				}
			}
			if (recipe.getIngredients().size() == 2) {
				if (recipe.getIngredients().get(0).test(this.getItems().get(0))
						&& recipe.getIngredients().get(1).test(this.getItems().get(1))) {
					currentRecipe = recipe;
					return currentRecipe;
				}
			}
		}
		// Clear stale recipe when no match is found
		currentRecipe = null;
		return null;
	}

	public boolean hasValidRecipe() {
		return getCurrentRecipe() != null;
	}

	/**
	 * Attempts to auto-fill the scar grid from an ItemScarPattern in slot 4.
	 * Returns true if a pattern was loaded (so the screen can refresh buttons).
	 */
	public boolean tryLoadPatternFromSlot() {
		ItemStack patternStack = this.getItem(4);
		if (!patternStack.isEmpty() && patternStack.getItem() instanceof ItemScarPattern) {
			List<ItemScarPattern.TemplateEntry> entries = ItemScarPattern.getTemplateEntries(patternStack, level);
			if (!entries.isEmpty() && entries.get(0).pattern() != null) {
				byte[][] recipePattern = entries.get(0).pattern();
				// Deep copy so we don't modify the recipe's array
				byte[][] copy = new byte[recipePattern.length][];
				for (int i = 0; i < recipePattern.length; i++) {
					copy[i] = recipePattern[i].clone();
				}
				this.setScarList(copy);
				return true;
			}
		}

		return false;
	}

	// NBT
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.contents, registries);
		readScarList(compound);
	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);
		ContainerHelper.saveAllItems(compound, this.contents, registries);
		if (compound != null) {
			ListTag tagList = new ListTag();
			if (scarsList != null) {
				for (int i = 0; i < scarsList.length; i++) {
					ByteArrayTag bye = new ByteArrayTag(scarsList[i]);
					tagList.add(bye);
				}
				compound.put(TAG_scarList, tagList);
			}
		}

	}

	private void readScarList(CompoundTag tag) {
		scarsList = ScarRecipe.blank();
		ListTag rows = tag.getList(TAG_scarList, Tag.TAG_BYTE_ARRAY);
		for (int i = 0; i < Math.min(rows.size(), scarsList.length); i++) {
			byte[] row = ((ByteArrayTag) rows.get(i)).getAsByteArray();
			System.arraycopy(row, 0, scarsList[i], 0, Math.min(row.length, scarsList[i].length));
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		readScarList(tag);
	}

	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.contents, registries);
		ListTag tagList = new ListTag();
		if (scarsList != null) {
			for (int i = 0; i < scarsList.length; i++) {
				ByteArrayTag bye = new ByteArrayTag(scarsList[i]);
				tagList.add(bye);
			}
			tag.put(TAG_scarList, tagList);
		}
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		if (pkt.getTag() != null) readScarList(pkt.getTag());
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == 1) {
			this.numPlayersUsing = type;
			return true;
		} else {
			return super.triggerEvent(id, type);
		}
	}

	public static int getPlayersUsing(BlockGetter reader, BlockPos pos) {
		BlockState blockstate = reader.getBlockState(pos);
		if (blockstate.hasBlockEntity()) {
			BlockEntity BlockEntity = reader.getBlockEntity(pos);
			if (BlockEntity instanceof ScarStationBlockEntity) {
				return ((ScarStationBlockEntity) BlockEntity).numPlayersUsing;
			}
		}
		return 0;
	}

	protected void onOpenOrClose() {

	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.chisel_station");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new ScarStationMenu(id, player, this);
	}

	/** Null means the current server inventory and trace are ready to carve. */
	public Component craftingFailure() {
		if (!contents.get(2).isEmpty()) return Component.literal("Take the finished scar from the output first.");
		if (!(contents.get(3).getItem() instanceof ItemKnapper)) return Component.literal("Place a knapper in the tool slot.");
		ScarRecipe recipe = getCurrentRecipe();
		if (recipe == null) return Component.literal("Place a scar blank and the pattern's catalyst in the ingredient slots.");
		if (!Arrays.deepEquals(recipe.getPattern(), scarsList)) return Component.literal("Trace the purple stencil cells in red before carving. Remove any extra marks.");
		return null;
	}

	public boolean canCraft() {
		return craftingFailure() == null;
	}

	public void craftEvent() {
		if (!canCraft()) return;
		ScarRecipe recipe = currentRecipe;
		contents.set(0, ItemStack.EMPTY);
		if (recipe.getIngredients().size() > 1) contents.set(1, ItemStack.EMPTY);
		contents.set(2, recipe.getResultItem().copy());
		ItemStack knapper = contents.get(3).copy();
		int damage = knapper.getDamageValue() + recipe.getPattern().length;
		if (damage >= knapper.getMaxDamage()) contents.set(3, ItemStack.EMPTY);
		else {
			knapper.setDamageValue(damage);
			contents.set(3, knapper);
		}
		currentRecipe = null;
		scarsList = ScarRecipe.blank();
		sendUpdates();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, worldPosition);
	}

	@Override
	public void setItem(int pIndex, ItemStack pStack) {
		this.contents.set(pIndex, pStack);
	}

	@Override
	public ItemStack getItem(int p_58328_) {
		return this.contents.get(p_58328_);
	}

	@Override
	public ItemStack removeItem(int pIndex, int pCount) {
		return ContainerHelper.removeItem(this.contents, pIndex, pCount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pIndex) {
		return ContainerHelper.takeItem(this.contents, pIndex);
	}

	@Override
	public boolean stillValid(Player p_58340_) {
		return (p_58340_.level() != level || this.level.getBlockEntity(this.worldPosition) != this
				|| (!level.isClientSide && !com.vincenthuto.hemomancy.common.event.MachineAccessEvents.canUseStation(p_58340_, level, worldPosition))) ? false
				: p_58340_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void clearContent() {
		this.contents.clear();
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.contents = items;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.contents;
	}

}
