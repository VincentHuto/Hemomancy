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
import com.huto.hemomancy.init.BlockEntityInit;
import com.huto.hemomancy.item.ItemBloodyFlask;
import com.huto.hemomancy.item.ItemEnzyme;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.getInventory();
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class BlockEntityVisceralRecaller extends RandomizableContainerBlockEntity
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
	Map<EnumBloodTendency, Float> clientTendency=new HashMap<EnumBloodTendency,Float>(){{put(EnumBloodTendency.ANIMUS,0f);put(EnumBloodTendency.MORTEM,0f);put(EnumBloodTendency.DUCTILIS,0f);put(EnumBloodTendency.FERRIC,0f);put(EnumBloodTendency.LUX,0f);put(EnumBloodTendency.TENEBRIS,0f);put(EnumBloodTendency.FLAMMEUS,0f);put(EnumBloodTendency.CONGEATIO,0f);

	}};

	public BlockEntityVisceralRecaller() {
		super(BlockEntityInit.visceral_artificial_recaller.get());
	}

	@Override
	public void tick() {
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		return super.triggerEvent(id, type);
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
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		readPacketNBT(tag);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, this.contents);
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		super.save(tag);
		writePacketNBT(tag);
		if (!this.trySaveLootTable(tag)) {
			ContainerHelper.saveAllItems(tag, this.contents);
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

	public void readPacketNBT(CompoundTag tag) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(tag);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, this.contents);
		}
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			clientTendency.put(tend, tag.getFloat(tend.toString()));
		}
	}

	public void writePacketNBT(CompoundTag par1CompoundTag) {
		par1CompoundTag.merge(itemHandler.serializeNBT());

	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundTag tag = pkt.getTag();
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			clientTendency.put(tend, tag.getFloat(tend.toString()));
		}
	}

	@Override
	public final CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag tag = new CompoundTag();
		writePacketNBT(tag);
		tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}

		return new ClientboundBlockEntityDataPacket(worldPosition, -999, tag);
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundTag tag) {
		super.handleUpdateTag(state, tag);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, this.contents);
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

	public static void swapContents(BlockEntityVisceralRecaller te, BlockEntityVisceralRecaller otherTe) {
		NonNullList<ItemStack> list = te.getItems();
		te.setItems(otherTe.getItems());
		otherTe.setItems(list);
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	public int getContainerSize() {
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
					if (level.random.nextInt(20) % 7 == 0) {
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
	protected Component getDefaultName() {
		return new TranslatableComponent("container.visceral_recaller");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new ContainerVisceralRecaller(id, player, this);
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