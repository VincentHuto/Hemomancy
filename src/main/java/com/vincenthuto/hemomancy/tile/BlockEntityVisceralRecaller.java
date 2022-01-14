package com.vincenthuto.hemomancy.tile;

import java.util.Map;

import com.vincenthuto.hemomancy.capa.player.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.MenuVisceralRecaller;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.ItemBloodyFlask;
import com.vincenthuto.hemomancy.item.ItemEnzyme;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityVisceralRecaller extends BaseContainerBlockEntity implements MenuProvider {
	NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	static final String TAG_BLOOD_TENDENCY = "tendency";
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	IBloodTendency tendency = getCapability(BloodTendencyProvider.TENDENCY_CAPA)
			.orElseThrow(IllegalStateException::new);

	public BlockEntityVisceralRecaller(BlockPos pos, BlockState state) {
		super(BlockEntityInit.visceral_artificial_recaller.get(), pos, state);
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		return super.triggerEvent(id, type);
	}

	@Override
	public void startOpen(Player pPlayer) {
		super.startOpen(pPlayer);
	}

	@Override
	public void onLoad() {
		volume.setActive(true);
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
	
	@Override
	public void setItem(int pIndex, ItemStack pStack) {
		// Absorb the enzyme
		this.contents.set(pIndex, pStack);

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
		sendUpdates();

	}
	

	// NBT
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, this.contents);
		if (tag != null) {
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ContainerHelper.saveAllItems(tag, this.contents);
		if (tag != null) {

			tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
			for (EnumBloodTendency key : tendency.getTendency().keySet()) {
				if (tendency.getTendency().get(key) != null) {
					tag.putFloat(key.toString(), tendency.getTendency().get(key));
				} else {
					tag.putFloat(key.toString(), 0);
				}
			}
		}

	}



	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag != null) {
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}
	
	@Override
	public final CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.contents);
		tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		for (EnumBloodTendency key : tendency.getTendency().keySet()) {
			if (tendency.getTendency().get(key) != null) {
				tag.putFloat(key.toString(), tendency.getTendency().get(key));
			} else {
				tag.putFloat(key.toString(), 0);
			}
		}
		return  tag;
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				tendency.getTendency().put(tend, tag.getFloat(tend.toString()));
			}
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}



	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.visceral_recaller");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new MenuVisceralRecaller(id, player, this);
	}

	@Override
	public int getContainerSize() {
		return this.contents.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.contents) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
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

}