package com.huto.hemomancy.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.huto.hemomancy.block.BlockChiselStation;
import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.tool.ItemKnapper;
import com.huto.hemomancy.recipe.ModChiselRecipes;
import com.huto.hemomancy.recipe.RecipeChiselStation;
import com.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityChiselStation extends RandomizableContainerBlockEntity
		implements TickableBlockEntity, MenuProvider {
	public NonNullList<ItemStack> chestContents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	public SimpleItemStackHandler itemHandler = createItemHandler();

	public int numPlayersUsing = 0;
	public float lidAngle, prevLidAngle;
	public final String TAG_RUNELIST = "RUNELIST";
	public List<Integer> runesList;
	public List<Integer> clientRuneList;

	RecipeChiselStation currentRecipe;

	public TileEntityChiselStation() {
		super(TileEntityInit.runic_chisel_station.get());
	}

	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, true);
	}

	public IItemHandlerModifiable getItemHandler() {
		return itemHandler;
	}

	@Override
	public void tick() {
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
	public int getSizeInventory() {
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
	public int getInventoryStackLimit() {
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
		for (RecipeChiselStation recipe : ModChiselRecipes.runeRecipies) {
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
		for (RecipeChiselStation recipe : ModChiselRecipes.runeRecipies) {
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
	public void read(BlockState state, CompoundTag compound) {
		super.read(state, compound);
		readPacketNBT(compound);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(compound)) {
			ItemStackHelper.loadAllItems(compound, this.chestContents);
		}
		ListNBT tagList = compound.getList(TAG_RUNELIST, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundNBT tag = tagList.getCompound(i);
			int s = tag.getInt("ListPos " + i);
			runesList.add(i, s);
			runesList.set(i, s);
		}

	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		writePacketNBT(compound);
		if (!this.checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, this.chestContents);
		}

		ListNBT tagList = new ListNBT();
		if (runesList != null) {
			for (int i = 0; i < runesList.size(); i++) {
				Integer s = runesList.get(i);
				CompoundNBT ss = new CompoundNBT();
				if (s != null) {
					ss.putInt("Int", s);
					tagList.add(ss);
				}
			}

			compound.put(TAG_RUNELIST, tagList);
		}

		return compound;
	}

	public void readPacketNBT(CompoundNBT par1CompoundNBT) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(par1CompoundNBT);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(par1CompoundNBT)) {
			ItemStackHelper.loadAllItems(par1CompoundNBT, this.chestContents);
		}
	}

	public void writePacketNBT(CompoundNBT par1CompoundNBT) {
		par1CompoundNBT.merge(itemHandler.serializeNBT());

	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(pkt.getNbtCompound())) {
			ItemStackHelper.loadAllItems(pkt.getNbtCompound(), this.chestContents);
		}
		ListNBT tagList = pkt.getNbtCompound().getList(TAG_RUNELIST, Constants.NBT.TAG_COMPOUND);
		List<Integer> test = new ArrayList<Integer>();
		for (int i = 0; i < tagList.size(); i++) {
			CompoundNBT tag = tagList.getCompound(i);
			int s = Integer.valueOf(tag.toString().substring(5).replace("}", ""));
			test.add(i, s);
			test.set(i, s);
		}
		this.runesList = test;
		clientRuneList = test;
	}

	@Override
	public final CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		super.getUpdatePacket();
		CompoundNBT tag = new CompoundNBT();
		writePacketNBT(tag);
		ListNBT tagList = new ListNBT();
		tagList.add(tag);
		if (runesList != null) {
			for (int i = 0; i < runesList.size(); i++) {
				Integer s = runesList.get(i);
				if (s != null) {
					tagList.add(tag);
				}
			}
			write(tag);
		}
		return new SUpdateTileEntityPacket(pos, -999, tag);
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.chestContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(tag)) {
			ItemStackHelper.loadAllItems(tag, this.chestContents);
		}
		if (tag.get(TAG_RUNELIST) != null) {
			for (int i = 0; i < runesList.size(); i++) {
				clientRuneList.add(runesList.get(i));
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.numPlayersUsing = type;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	@Override
	public void openInventory(PlayerEntity player) {
		if (!player.isSpectator()) {
			if (this.numPlayersUsing < 0) {
				this.numPlayersUsing = 0;
			}

			++this.numPlayersUsing;
			this.onOpenOrClose();
		}
	}

	@Override
	public void closeInventory(PlayerEntity player) {
		if (!player.isSpectator()) {
			--this.numPlayersUsing;
			this.onOpenOrClose();
		}
	}

	public static int getPlayersUsing(IBlockReader reader, BlockPos pos) {
		BlockState blockstate = reader.getBlockState(pos);
		if (blockstate.hasTileEntity()) {
			TileEntity tileentity = reader.getTileEntity(pos);
			if (tileentity instanceof TileEntityChiselStation) {
				return ((TileEntityChiselStation) tileentity).numPlayersUsing;
			}
		}
		return 0;
	}

	public static void swapContents(TileEntityChiselStation te, TileEntityChiselStation otherTe) {
		NonNullList<ItemStack> list = te.getItems();
		te.setItems(otherTe.getItems());
		otherTe.setItems(list);
	}

	protected void onOpenOrClose() {
		Block block = this.getBlockState().getBlock();
		if (block instanceof BlockChiselStation) {
			this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
			this.world.notifyNeighborsOfStateChange(this.pos, block);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
		return super.getCapability(cap, side);

	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.chisel_station");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new ContainerChiselStation(id, player, this);
	}

	public void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, getBlockState(), getBlockState());
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
		markDirty();
	}

	public void craftEvent() {
		List<ItemStack> chestStuff = new ArrayList<ItemStack>();
		chestStuff.add(chestContents.get(0));
		chestStuff.add(chestContents.get(1));

		RecipeChiselStation recipe = null;

		if (currentRecipe != null)
			recipe = currentRecipe;
		else
			for (RecipeChiselStation recipe_ : ModChiselRecipes.runeRecipies) {

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
			// These two make sure that even if you click the buttons in the wrong order
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

						world.addParticle(ParticleTypes.PORTAL, pos.getX(), pos.getY(), pos.getZ(), 0.0D, 0.0D, 0.0D);
					}
					chestContents.set(0, output);
					currentRecipe = null;
					for (int i = 0; i < getSizeInventory(); i++) {
						chestContents.set(0, ItemStack.EMPTY);
						chestContents.set(1, ItemStack.EMPTY);
						chestContents.set(2, output);
						ItemStack knapperIn = chestContents.get(3);
						if (knapperIn.getItem() instanceof ItemKnapper) {
							ItemStack newKnapper = knapperIn.copy();
							newKnapper.attemptDamageItem(recipe.getActivatedRunes().size(), world.rand, null);
							chestContents.set(3, newKnapper);
						}
						runesList.clear();
						this.sendUpdates();
						VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, pos);

					}
				}
			}
		}

	}

	public static class SimpleItemStackHandler extends ItemStackHandler {

		private final boolean allowWrite;
		private final TileEntity tile;

		public SimpleItemStackHandler(TileEntity inv, boolean allowWrite) {
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
			tile.markDirty();
		}
	}

}