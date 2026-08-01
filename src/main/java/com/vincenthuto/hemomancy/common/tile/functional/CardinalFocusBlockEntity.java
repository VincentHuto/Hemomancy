package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CardinalFocusBlockEntity extends BlockEntity {
	private static final String TAG_MEDIUM = "Medium";
	private BlockPos templeDisplay;
	private ItemStack medium = ItemStack.EMPTY;

	public CardinalFocusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.cardinal_focus.get(), pos, state);
	}

	public void linkTempleDisplay(BlockPos display) {
		templeDisplay = display == null ? null : display.immutable();
		setChanged();
	}

	public BlockPos getTempleDisplay() {
		return templeDisplay;
	}

	public ItemStack getMediumDisplayStack() {
		return medium.copy();
	}

	public ItemStack getMediumForMatching() {
		return medium;
	}

	public boolean insertMedium(Player player, ItemStack stack) {
		if (stack.isEmpty() || !medium.isEmpty()) return false;
		medium = stack.copyWithCount(1);
		if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
		markDirtyAndSync();
		return true;
	}

	public ItemStack extractMedium() {
		if (medium.isEmpty()) return ItemStack.EMPTY;
		ItemStack extracted = medium.copy();
		medium = ItemStack.EMPTY;
		markDirtyAndSync();
		return extracted;
	}

	public boolean consumeMedium(Ingredient required) {
		if (required == null || required == Ingredient.EMPTY || required.isEmpty()) return medium.isEmpty();
		if (!required.test(medium)) return false;
		extractMedium();
		return true;
	}

	public boolean hasMedium() {
		return !medium.isEmpty();
	}

	public void markDirtyAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (templeDisplay != null) tag.putLong("TempleDisplay", templeDisplay.asLong());
		if (!medium.isEmpty()) tag.put(TAG_MEDIUM, medium.save(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		templeDisplay = tag.contains("TempleDisplay") ? BlockPos.of(tag.getLong("TempleDisplay")) : null;
		medium = tag.contains(TAG_MEDIUM)
				? ItemStack.parseOptional(registries, tag.getCompound(TAG_MEDIUM))
				: ItemStack.EMPTY;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		loadAdditional(tag, registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
			HolderLookup.Provider registries) {
		if (packet.getTag() != null) handleUpdateTag(packet.getTag(), registries);
	}
}
