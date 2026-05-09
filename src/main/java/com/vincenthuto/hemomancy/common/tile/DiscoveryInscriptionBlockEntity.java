package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DiscoveryInscriptionBlockEntity extends BlockEntity {
	private static final String TAG_INSCRIPTION_ID = "InscriptionId";
	private ResourceLocation inscriptionId = Hemomancy.rloc("empty");

	public DiscoveryInscriptionBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.discovery_inscription.get(), pos, state);
	}

	public ResourceLocation getInscriptionId() {
		return inscriptionId;
	}

	public void setInscriptionId(ResourceLocation inscriptionId) {
		this.inscriptionId = inscriptionId;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putString(TAG_INSCRIPTION_ID, inscriptionId.toString());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains(TAG_INSCRIPTION_ID)) {
			ResourceLocation parsed = ResourceLocation.tryParse(tag.getString(TAG_INSCRIPTION_ID));
			inscriptionId = parsed == null ? Hemomancy.rloc("empty") : parsed;
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		saveAdditional(tag, provider);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
