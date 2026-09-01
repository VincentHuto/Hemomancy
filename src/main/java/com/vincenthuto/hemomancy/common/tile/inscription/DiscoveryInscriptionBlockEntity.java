package com.vincenthuto.hemomancy.common.tile.inscription;

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

import java.util.random.RandomGenerator;

public class DiscoveryInscriptionBlockEntity extends BlockEntity {
	private static final String TAG_INSCRIPTION_ID = "InscriptionId";
	private static final String TAG_ENGRAM_INDEX   = "EngramIndex";

	private ResourceLocation inscriptionId = Hemomancy.rloc("empty");
	/** Randomly chosen on first placement (0-25), persisted so the glyph never changes after that. */
	private int engramIndex = RandomGenerator.getDefault().nextInt(26);
	private long lastClientGlowTick = Long.MIN_VALUE;

	public DiscoveryInscriptionBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.discovery_inscription.get(), pos, state);
	}

	public ResourceLocation getInscriptionId() {
		return inscriptionId;
	}

	public int getEngramIndex() {
		return engramIndex;
	}

	public boolean shouldEmitClientGlow(long gameTime) {
		if (lastClientGlowTick == gameTime) {
			return false;
		}
		lastClientGlowTick = gameTime;
		return true;
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
		tag.putInt(TAG_ENGRAM_INDEX, engramIndex);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		if (tag.contains(TAG_INSCRIPTION_ID)) {
			ResourceLocation parsed = ResourceLocation.tryParse(tag.getString(TAG_INSCRIPTION_ID));
			inscriptionId = parsed == null ? Hemomancy.rloc("empty") : parsed;
		}
		if (tag.contains(TAG_ENGRAM_INDEX)) {
			engramIndex = tag.getInt(TAG_ENGRAM_INDEX);
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
