package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.saint.EnumCorpusState;
import com.vincenthuto.hemomancy.common.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the Saint Sarcophagus — tracks the Preserved Corpus state,
 * which saint resides within, and how many times extraction has been attempted.
 */
public class SaintSarcophagusBlockEntity extends BlockEntity implements IMultiBlockEntity {

	public static final int OFFERING_THRESHOLD = 3;

	private EnumSaintType saintType = EnumSaintType.HEMORATH;
	private EnumCorpusState corpusState = EnumCorpusState.DORMANT;
	private int extractionAttempts = 0;
	private int cooldownTicks = 0;
	private boolean isOpen = false;
	private boolean isConsecrated = false;
	private int offeringBloodVolume = 0;

	// Client-side animation
	public float lidAngle = 0.0F;
	public float prevLidAngle = 0.0F;

	public SaintSarcophagusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.saint_sarcophagus.get(), pos, state);
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean open) {
		this.isOpen = open;
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	public EnumSaintType getSaintType() {
		return saintType;
	}

	public void setSaintType(EnumSaintType saintType) {
		this.saintType = saintType;
		setChanged();
	}

	public EnumCorpusState getCorpusState() {
		return corpusState;
	}

	public void setCorpusState(EnumCorpusState state) {
		this.corpusState = state;
		setChanged();
	}

	public int getExtractionAttempts() {
		return extractionAttempts;
	}

	public void incrementExtractionAttempts() {
		this.extractionAttempts++;
		// After 3 failed attempts, the corpus awakens
		if (extractionAttempts >= 3 && corpusState != EnumCorpusState.AWAKENED) {
			corpusState = EnumCorpusState.AWAKENED;
		}
		setChanged();
	}

	public int getCooldownTicks() {
		return cooldownTicks;
	}

	public void setCooldownTicks(int ticks) {
		this.cooldownTicks = ticks;
		setChanged();
	}

	public boolean isOnCooldown() {
		return cooldownTicks > 0;
	}

	public boolean isConsecrated() {
		return isConsecrated;
	}

	public void setConsecrated(boolean consecrated) {
		this.isConsecrated = consecrated;
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	public int getOfferingBloodVolume() {
		return offeringBloodVolume;
	}

	public void addOffering() {
		this.offeringBloodVolume++;
		setChanged();
	}

	public float getLidAngle(float partialTick) {
		return Mth.lerp(partialTick, prevLidAngle, lidAngle);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, SaintSarcophagusBlockEntity be) {
		be.prevLidAngle = be.lidAngle;
		if (be.isOpen && be.lidAngle < 1.0F) {
			be.lidAngle = Math.min(1.0F, be.lidAngle + 0.05F);
		} else if (!be.isOpen && be.lidAngle > 0.0F) {
			be.lidAngle = Math.max(0.0F, be.lidAngle - 0.05F);
		}
	}

	public void tick() {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			setChanged();
		}
	}

	@Override
	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putBoolean("IsOpen", isOpen);
		tag.putBoolean("IsConsecrated", isConsecrated);
		tag.putString("SaintType", saintType.name());
		tag.putString("CorpusState", corpusState.name());
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString("SaintType", saintType.name());
		tag.putString("CorpusState", corpusState.name());
		tag.putInt("ExtractionAttempts", extractionAttempts);
		tag.putInt("CooldownTicks", cooldownTicks);
		tag.putBoolean("IsOpen", isOpen);
		tag.putBoolean("IsConsecrated", isConsecrated);
		tag.putInt("OfferingBloodVolume", offeringBloodVolume);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("SaintType")) {
			try {
				saintType = EnumSaintType.valueOf(tag.getString("SaintType"));
			} catch (IllegalArgumentException e) {
				saintType = EnumSaintType.HEMORATH;
			}
		}
		if (tag.contains("CorpusState")) {
			try {
				corpusState = EnumCorpusState.valueOf(tag.getString("CorpusState"));
			} catch (IllegalArgumentException e) {
				corpusState = EnumCorpusState.DORMANT;
			}
		}
		extractionAttempts = tag.getInt("ExtractionAttempts");
		cooldownTicks = tag.getInt("CooldownTicks");
		isOpen = tag.getBoolean("IsOpen");
		isConsecrated = tag.getBoolean("IsConsecrated");
		offeringBloodVolume = tag.getInt("OfferingBloodVolume");
	}
}
