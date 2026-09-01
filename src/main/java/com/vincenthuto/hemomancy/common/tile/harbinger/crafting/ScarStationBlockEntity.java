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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScarStationBlockEntity extends BaseContainerBlockEntity implements MenuProvider, IMultiBlockEntity {
	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);

	public int numPlayersUsing = 0;
	public float lidAngle, prevLidAngle;
	public final String TAG_scarList = "scarList";
	public byte[][] scarsList;
	public byte[][] clientScarList;

	ScarRecipe currentRecipe;

	public ScarStationBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.scar_station.get(), pos, state);
	}

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}


	@Override
	public void onLoad() {
		this.scarsList = ScarRecipe.blank();
	}

	public void clearScarList() {
		this.sendUpdates();
		if (scarsList != null) {
			scarsList = ScarRecipe.blank();
		}
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
		ListTag tagList = compound.getList(TAG_scarList, Tag.TAG_BYTE_ARRAY);
		if (this.scarsList != null) {
			for (int i = 0; i < tagList.size(); i++) {
				ByteArrayTag arr = (ByteArrayTag) tagList.get(i);
				scarsList[i] = arr.getAsByteArray();
			}
		}
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

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		if (tag != null) {
			ListTag tagList = tag.getList(TAG_scarList, Tag.TAG_BYTE_ARRAY);
			if (this.scarsList != null) {
				for (int i = 0; i < tagList.size(); i++) {
					ByteArrayTag arr = (ByteArrayTag) tagList.get(i);
					scarsList[i] = arr.getAsByteArray();
				}
			}
		}
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
		if (pkt.getTag() != null) {
			ListTag tagList = pkt.getTag().getList(TAG_scarList, Tag.TAG_BYTE_ARRAY);
			if (this.scarsList != null) {
				for (int i = 0; i < tagList.size(); i++) {
					ByteArrayTag arr = (ByteArrayTag) tagList.get(i);
					scarsList[i] = arr.getAsByteArray();
				}
			}
		}
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

	public boolean canCraft() {

		return false;
	}

	public void craftEvent() {
		List<ItemStack> chestStuff = new ArrayList<ItemStack>();
		chestStuff.add(contents.get(0));
		chestStuff.add(contents.get(1));
		ScarRecipe recipe = currentRecipe;
		if (recipe != null && contents.get(2).isEmpty()) {
			List<Ingredient> recipieInObj = recipe.getIngredients();
			boolean matcher = false;
			if (recipieInObj.get(0).test(chestStuff.get(0)) && recipieInObj.size() == 1) {
				matcher = true;
			} else if (recipieInObj.get(0).test(chestStuff.get(0)) && recipieInObj.get(1).test(chestStuff.get(1))
					&& recipieInObj.size() == 2) {
				matcher = true;
			}
			if (Arrays.deepEquals(scarsList, currentRecipe.getPattern()) && matcher) {
				ItemStack output = recipe.getResultItem().copy();
				contents.set(0, ItemStack.EMPTY);
				contents.set(1, ItemStack.EMPTY);
				contents.set(2, output);
				ItemStack knapperIn = contents.get(3);
				if (knapperIn.getItem() instanceof ItemKnapper) {
					ItemStack newKnapper = knapperIn.copy();
					int newDamage = newKnapper.getDamageValue() + recipe.getPattern().length;
					if (newDamage >= newKnapper.getMaxDamage()) {
						contents.set(3, ItemStack.EMPTY);
					} else {
						newKnapper.setDamageValue(newDamage);
						contents.set(3, newKnapper);
					}
				}
				currentRecipe = null;
				scarsList = ScarRecipe.blank();
				this.sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, worldPosition);
			}
		}
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
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
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
