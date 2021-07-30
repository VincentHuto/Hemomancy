package com.huto.hemomancy.tile;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.huto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.container.ContainerVisceralRecaller;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.ItemBloodyFlask;
import com.huto.hemomancy.item.ItemEnzyme;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityVisceralRecaller extends RandomizableContainerBlockEntity
		implements TickableBlockEntity, MenuProvider {
	NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	SimpleItemStackHandler itemHandler = createItemHandler();
	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	static final String TAG_BLOOD_TENDENCY = "tendency";
	float clientBloodLevel = 0.0f;
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA)
			.orElseThrow(IllegalStateException::new);
	@SuppressWarnings("serial")
	Map<EnumBloodTendency, Float> clientTendency = new HashMap<EnumBloodTendency, Float>() {
		{
			put(EnumBloodTendency.ANIMUS, 0f);
			put(EnumBloodTendency.MORTEM, 0f);
			put(EnumBloodTendency.DUCTILIS, 0f);
			put(EnumBloodTendency.FERRIC, 0f);
			put(EnumBloodTendency.LUX, 0f);
			put(EnumBloodTendency.TENEBRIS, 0f);
			put(EnumBloodTendency.FLAMMEUS, 0f);
			put(EnumBloodTendency.CONGEATIO, 0f);

		}
	};

	public TileEntityVisceralRecaller() {
		super(TileEntityInit.visceral_artificial_recaller.get());
	}

	@Override
	public void tick() {
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		return super.receiveClientEvent(id, type);
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
	}

	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, true);
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public float getBloodVolume() {
		return volume.getBloodVolume();
	}

	public float getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	public IBloodTendency getTendCapability() {
		return tendency;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency.getTendency();
	}

	// NBT
	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);
		readPacketNBT(tag);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(tag)) {
			ItemStackHelper.loadAllItems(tag, this.contents);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);
		writePacketNBT(tag);
		if (!this.checkLootAndWrite(tag)) {
			ItemStackHelper.saveAllItems(tag, this.contents);
		}
		tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());

		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}

		return tag;
	}

	public void readPacketNBT(CompoundNBT tag) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(tag);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(tag)) {
			ItemStackHelper.loadAllItems(tag, this.contents);
		}
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			clientTendency.put(tend, tag.getFloat(tend.toString()));
		}
	}

	public void writePacketNBT(CompoundNBT par1CompoundNBT) {
		par1CompoundNBT.merge(itemHandler.serializeNBT());

	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundNBT tag = pkt.getNbtCompound();
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			clientTendency.put(tend, tag.getFloat(tend.toString()));
		}
	}

	@Override
	public final CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		writePacketNBT(tag);
		tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}

		return new SUpdateTileEntityPacket(pos, -999, tag);
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(tag)) {
			ItemStackHelper.loadAllItems(tag, this.contents);
		}
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			clientTendency.put(tend, tag.getFloat(tend.toString()));
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
		return super.getCapability(cap, side);

	}

	public static void swapContents(TileEntityVisceralRecaller te, TileEntityVisceralRecaller otherTe) {
		NonNullList<ItemStack> list = te.getItems();
		te.setItems(otherTe.getItems());
		otherTe.setItems(list);
	}

	public void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, getBlockState(), getBlockState());
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
		markDirty();
	}

	@Override
	public int getSizeInventory() {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getItems() {

		// Absorb the enzyme
		if (!contents.get(1).isEmpty()) {
			ItemStack stack = contents.get(1);
			if (stack.getItem() instanceof ItemEnzyme) {
				ItemEnzyme enzyme = (ItemEnzyme) stack.getItem();
				if (getTendency().get(enzyme.getTend()) < 2.0f) {
					tendency.addTendencyAlignment(enzyme.getTend(), enzyme.getAmount() / 50);
					stack.shrink(1);
				}
				// Adds a recycled chance
				if (contents.get(3).isEmpty()) {
					if (world.rand.nextInt(20) % 7 == 0) {
						contents.set(3, new ItemStack(ItemInit.recycled_enzyme.get()));
					}
				}
			}
		}

		// Absorbs the flask
		if (!contents.get(2).isEmpty()) {
			ItemStack stack = contents.get(2);
			if (stack.getItem() instanceof ItemBloodyFlask) {
				ItemBloodyFlask flask = (ItemBloodyFlask) stack.getItem();
				if (this.getBloodVolume() < this.getMaxBloodVolume()) {
					volume.addBloodVolume(flask.getAmount());
					stack.shrink(1);
				}
			}
		}

		return this.contents;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.contents = itemsIn;

	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.visceral_recaller");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new ContainerVisceralRecaller(id, player, this);
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