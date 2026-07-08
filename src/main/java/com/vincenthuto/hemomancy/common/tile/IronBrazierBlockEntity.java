package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
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
	private UUID graftRitePlayer;
	private String graftRiteForm = "";
	private int graftRiteProgressTicks;
	private long graftRiteLastTick;

	public IronBrazierBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.iron_brazier.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, IronBrazierBlockEntity te) {
		if (te.graftRiteProgressTicks > 0
				&& (te.offering.isEmpty() || level.getGameTime() - te.graftRiteLastTick > 8L)) {
			te.resetGraftRiteProgress();
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
		resetGraftRiteProgress();
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
		resetGraftRiteProgress();
		offering = ItemStack.EMPTY;
		markDirtyAndSync();
		return extracted;
	}

	public ItemStack consumeOffering() {
		return extractOffering();
	}

	public int advanceGraftRite(ServerPlayer player, LivingWeaponForm form, int requiredTicks) {
		return form == null ? 0 : advanceGraftRite(player, form.serializedName(), requiredTicks);
	}

	public int advanceGraftRite(ServerPlayer player, String riteId, int requiredTicks) {
		if (player == null || riteId == null || riteId.isBlank() || level == null) {
			resetGraftRiteProgress();
			return 0;
		}
		UUID playerId = player.getUUID();
		if (!playerId.equals(graftRitePlayer) || !riteId.equals(graftRiteForm)) {
			resetGraftRiteProgress();
			graftRitePlayer = playerId;
			graftRiteForm = riteId;
		}
		graftRiteProgressTicks = Math.min(requiredTicks, graftRiteProgressTicks + 1);
		graftRiteLastTick = level.getGameTime();
		setChanged();
		return graftRiteProgressTicks;
	}

	public void resetGraftRiteProgress() {
		graftRitePlayer = null;
		graftRiteForm = "";
		graftRiteProgressTicks = 0;
		graftRiteLastTick = 0L;
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
