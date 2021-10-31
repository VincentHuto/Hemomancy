package com.vincenthuto.hemomancy.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.block.BlockChiselStation;
import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.recipe.ChiselRecipes;
import com.vincenthuto.hemomancy.recipe.RecipeChiselStation;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class BlockEntityChiselStation extends RandomizableContainerBlockEntity implements MenuProvider {
	public NonNullList<ItemStack> chestContents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	public SimpleItemStackHandler itemHandler = createItemHandler();

	public int numPlayersUsing = 0;
	public float lidAngle, prevLidAngle;
	public final String TAG_RUNELIST = "RUNELIST";
	public List<Integer> runesList;
	public List<Integer> clientRuneList;

	RecipeChiselStation currentRecipe;

	public BlockEntityChiselStation(BlockPos pos, BlockState state) {
		super(BlockEntityInit.runic_chisel_station.get(), pos, state);
	}

	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, true);
	}

	public IItemHandlerModifiable getItemHandler() {
		return itemHandler;
	}

	@Override
	public void onLoad() {
		this.runesList = new ArrayList<Integer>();
	}

	public List<Integer> getRuneList() {
		return runesList;
	}

	public void cleartRuneList() {
		this.sendUpdates();
		if (runesList != null) {
			runesList.clear();
		}
	}

	public void setRuneList(List<Integer> runesIn) {
		runesList = runesIn;
		this.sendUpdates();

	}

	@Override
	public int getContainerSize() {
		return 5;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.chestContents) {
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

	public boolean areRunesMatching() {
		if (this.getCurrentRecipe() != null) {
			List<Integer> currentList = clientRuneList;
			List<Integer> recipeList = currentRecipe.getActivatedRunes();
			Collections.sort(currentList);
			Collections.sort(recipeList);
			if (currentList.equals(recipeList)) {
				return true;
			} else {
				return false;
			}
		}

		return false;

	}

	public RecipeChiselStation getCurrentRecipe() {
		for (RecipeChiselStation recipe : ChiselRecipes.runeRecipies) {
			if (recipe.getInputs().size() == 1) {
				if (recipe.getInputs().get(0).test(this.getItems().get(0))) {
					currentRecipe = recipe;
					return currentRecipe;

				}
			}
			if (recipe.getInputs().size() == 2) {
				if (recipe.getInputs().get(0).test(this.getItems().get(0))
						&& recipe.getInputs().get(1).test(this.getItems().get(1))) {
					currentRecipe = recipe;
					return currentRecipe;

				}
			}
		}
		return currentRecipe;

	}

	public boolean hasValidRecipe() {
		for (RecipeChiselStation recipe : ChiselRecipes.runeRecipies) {
			if (recipe.getInputs().size() == 1) {
				if (recipe.getInputs().get(0).test(this.getItems().get(0))) {
					return true;

				}
			}
			if (recipe.getInputs().size() == 2) {
				if (recipe.getInputs().get(0).test(this.getItems().get(0))
						&& recipe.getInputs().get(1).test(this.getItems().get(1))) {
					return true;

				}
			}
		}
		return false;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.chestContents;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.chestContents = itemsIn;

	}

	// NBT
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		readPacketNBT(compound);
		this.chestContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compound)) {
			ContainerHelper.loadAllItems(compound, this.chestContents);
		}
		ListTag tagList = compound.getList(TAG_RUNELIST, Constants.NBT.TAG_COMPOUND);
		if (this.runesList != null) {
			for (int i = 0; i < tagList.size(); i++) {
				CompoundTag tag = tagList.getCompound(i);
				int s = tag.getInt("ListPos " + i);
				runesList.add(i, s);
				runesList.set(i, s);
			}
		}

	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		super.save(compound);
		writePacketNBT(compound);
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, this.chestContents);
		}

		ListTag tagList = new ListTag();
		if (runesList != null) {
			for (int i = 0; i < runesList.size(); i++) {
				Integer s = runesList.get(i);
				CompoundTag ss = new CompoundTag();
				if (s != null) {
					ss.putInt("Int", s);
					tagList.add(ss);
				}
			}

			compound.put(TAG_RUNELIST, tagList);
		}

		return compound;
	}

	public void readPacketNBT(CompoundTag par1CompoundTag) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(par1CompoundTag);
		this.chestContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(par1CompoundTag)) {
			ContainerHelper.loadAllItems(par1CompoundTag, this.chestContents);
		}
	}

	public void writePacketNBT(CompoundTag par1CompoundTag) {
		par1CompoundTag.merge(itemHandler.serializeNBT());

	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		this.chestContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(pkt.getTag())) {
			ContainerHelper.loadAllItems(pkt.getTag(), this.chestContents);
		}
		ListTag tagList = pkt.getTag().getList(TAG_RUNELIST, Constants.NBT.TAG_COMPOUND);
		List<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag tag = tagList.getCompound(i);
			int s = Integer.valueOf(tag.toString().substring(5).replace("}", ""));
			test.add(i, s);
			test.set(i, s);
		}
		this.runesList = test;
		clientRuneList = test;
	}

	@Override
	public final CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		super.getUpdatePacket();
		CompoundTag tag = new CompoundTag();
		writePacketNBT(tag);
		ListTag tagList = new ListTag();
		tagList.add(tag);
		if (runesList != null) {
			for (int i = 0; i < runesList.size(); i++) {
				Integer s = runesList.get(i);
				if (s != null) {
					tagList.add(tag);
				}
			}
			save(tag);
		}
		return new ClientboundBlockEntityDataPacket(worldPosition, -999, tag);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		this.chestContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, this.chestContents);
		}
		if (tag.get(TAG_RUNELIST) != null) {
			for (int i = 0; i < runesList.size(); i++) {
				clientRuneList.add(runesList.get(i));
			}
		}
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

	@Override
	public void startOpen(Player player) {
		if (!player.isSpectator()) {
			if (this.numPlayersUsing < 0) {
				this.numPlayersUsing = 0;
			}

			++this.numPlayersUsing;
			this.onOpenOrClose();
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!player.isSpectator()) {
			--this.numPlayersUsing;
			this.onOpenOrClose();
		}
	}

	public static int getPlayersUsing(BlockGetter reader, BlockPos pos) {
		BlockState blockstate = reader.getBlockState(pos);
		if (blockstate.hasBlockEntity()) {
			BlockEntity tileentity = reader.getBlockEntity(pos);
			if (tileentity instanceof BlockEntityChiselStation) {
				return ((BlockEntityChiselStation) tileentity).numPlayersUsing;
			}
		}
		return 0;
	}

	public static void swapContents(BlockEntityChiselStation te, BlockEntityChiselStation otherTe) {
		NonNullList<ItemStack> list = te.getItems();
		te.setItems(otherTe.getItems());
		otherTe.setItems(list);
	}

	protected void onOpenOrClose() {
		Block block = this.getBlockState().getBlock();
		if (block instanceof BlockChiselStation) {
			this.level.blockEvent(this.worldPosition, block, 1, this.numPlayersUsing);
			this.level.updateNeighborsAt(this.worldPosition, block);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
		return super.getCapability(cap, side);

	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.chisel_station");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new MenuChiselStation(id, player, this);
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	public void craftEvent() {
		List<ItemStack> chestStuff = new ArrayList<ItemStack>();
		chestStuff.add(chestContents.get(0));
		chestStuff.add(chestContents.get(1));

		RecipeChiselStation recipe = null;

		if (currentRecipe != null)
			recipe = currentRecipe;
		else
			for (RecipeChiselStation recipe_ : ChiselRecipes.runeRecipies) {

				List<Ingredient> recipieInObjFirst = recipe_.getInputs();
				if (recipieInObjFirst.get(0).test(chestStuff.get(0)) && recipieInObjFirst.size() == 1) {
					recipe = recipe_;

					break;
				} else if (recipieInObjFirst.get(0).test(chestStuff.get(0))
						&& recipieInObjFirst.get(1).test(chestStuff.get(1)) && recipieInObjFirst.size() == 2) {
					recipe = recipe_;
					break;

				}
			}

		if (recipe != null && chestContents.get(2).isEmpty()) {

			List<Ingredient> recipieInObj = recipe.getInputs();

			List<Integer> list1 = recipe.getActivatedRunes();
			List<Integer> list2 = this.getRuneList();
			// These two make sure that even if you click the renderables in the wrong order
			// they still work.
			Collections.sort(list1);
			Collections.sort(list2);
			// Checks if the two inventories have the exact same values
			boolean matcher = false;
			if (recipieInObj.get(0).test(chestStuff.get(0)) && recipieInObj.size() == 1) {

				matcher = true;
			}

			else if (recipieInObj.get(0).test(chestStuff.get(0)) && recipieInObj.get(1).test(chestStuff.get(1))
					&& recipieInObj.size() == 2) {
				matcher = true;
			}

			if (list1.equals(list2) && matcher) {
				{

					ItemStack output = recipe.getOutput().copy();

					if (level.isClientSide) {

						level.addParticle(ParticleTypes.PORTAL, worldPosition.getX(), worldPosition.getY(),
								worldPosition.getZ(), 0.0D, 0.0D, 0.0D);
					}
					chestContents.set(0, output);
					currentRecipe = null;
					for (int i = 0; i < getContainerSize(); i++) {
						chestContents.set(0, ItemStack.EMPTY);
						chestContents.set(1, ItemStack.EMPTY);
						chestContents.set(2, output);
						ItemStack knapperIn = chestContents.get(3);
						if (knapperIn.getItem() instanceof ItemKnapper) {
							ItemStack newKnapper = knapperIn.copy();
							newKnapper.hurt(recipe.getActivatedRunes().size(), level.random, null);
							chestContents.set(3, newKnapper);
						}
						runesList.clear();
						this.sendUpdates();
						VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, worldPosition);

					}
				}
			}
		}

	}

	public static class SimpleItemStackHandler extends ItemStackHandler {

		private final boolean allowWrite;
		private final BlockEntity tile;

		public SimpleItemStackHandler(BlockEntity inv, boolean allowWrite) {
			super(5);
			this.allowWrite = allowWrite;
			tile = inv;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (allowWrite) {
				return super.insertItem(slot, stack, simulate);
			} else
				return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (allowWrite) {

				return super.extractItem(slot, amount, simulate);
			} else
				return ItemStack.EMPTY;
		}

		@Override
		public void onContentsChanged(int slot) {
			tile.setChanged();
		}
	}

}