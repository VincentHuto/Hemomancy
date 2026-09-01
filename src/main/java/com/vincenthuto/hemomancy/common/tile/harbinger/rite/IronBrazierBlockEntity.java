package com.vincenthuto.hemomancy.common.tile.harbinger.rite;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierSpecialOfferingEffects;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class IronBrazierBlockEntity extends BlockEntity {
	private static final String TAG_OFFERING = "Offering";

	private ItemStack offering = ItemStack.EMPTY;
	private UUID itemAbsorptionPlayer;
	private String itemAbsorptionRiteId = "";
	private int itemAbsorptionProgressTicks;
	private long itemAbsorptionLastTick;

	public IronBrazierBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.iron_brazier.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, IronBrazierBlockEntity te) {
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& state.hasProperty(BrazierBlock.RITUAL_PHASE)
				&& state.getValue(BrazierBlock.RITUAL_PHASE) > 0
				&& !te.offering.isEmpty()) {
			BrazierSpecialOfferingEffects.spawnPersistent(serverLevel, pos, te.offering, level.getGameTime());
		}
		if (te.itemAbsorptionProgressTicks > 0
				&& (te.offering.isEmpty() || level.getGameTime() - te.itemAbsorptionLastTick > 8L)) {
			te.resetItemAbsorptionProgress();
		}
	}

	public ItemStack getOfferingDisplayStack() {
		return offering.copy();
	}

	public ItemStack getOfferingForMatching() {
		return offering;
	}

	public boolean hasOffering() {
		return !offering.isEmpty();
	}

	public boolean insertOffering(Player player, ItemStack stack) {
		if (stack.isEmpty() || !offering.isEmpty()) {
			return false;
		}
		resetItemAbsorptionProgress();
		offering = stack.copyWithCount(1);
		if (player == null || !player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		markDirtyAndSync();
		return true;
	}

	public ItemStack extractOffering() {
		if (offering.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = offering.copy();
		resetItemAbsorptionProgress();
		offering = ItemStack.EMPTY;
		markDirtyAndSync();
		return extracted;
	}

	public ItemStack consumeOffering() {
		return extractOffering();
	}

	public int advanceItemAbsorption(ServerPlayer player, String riteId, int requiredTicks) {
		if (player == null || riteId == null || riteId.isBlank() || level == null) {
			resetItemAbsorptionProgress();
			return 0;
		}
		UUID playerId = player.getUUID();
		if (!playerId.equals(itemAbsorptionPlayer) || !riteId.equals(itemAbsorptionRiteId)) {
			resetItemAbsorptionProgress();
			itemAbsorptionPlayer = playerId;
			itemAbsorptionRiteId = riteId;
		}
		itemAbsorptionProgressTicks = Math.min(requiredTicks, itemAbsorptionProgressTicks + 1);
		itemAbsorptionLastTick = level.getGameTime();
		setChanged();
		return itemAbsorptionProgressTicks;
	}

	public void resetItemAbsorptionProgress() {
		itemAbsorptionPlayer = null;
		itemAbsorptionRiteId = "";
		itemAbsorptionProgressTicks = 0;
		itemAbsorptionLastTick = 0L;
		setChanged();
	}

	public void markDirtyAndSync() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		if (!offering.isEmpty()) {
			tag.put(TAG_OFFERING, offering.save(provider));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		offering = tag.contains(TAG_OFFERING)
				? ItemStack.parseOptional(provider, tag.getCompound(TAG_OFFERING))
				: ItemStack.EMPTY;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return saveWithoutMetadata(provider);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
		super.handleUpdateTag(tag, provider);
		loadAdditional(tag, provider);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
		if (pkt.getTag() != null) {
			handleUpdateTag(pkt.getTag(), provider);
		}
	}
}
