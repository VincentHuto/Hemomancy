package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.saint.EnumCorpusState;
import com.vincenthuto.hemomancy.common.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the Saint Sarcophagus — tracks the Preserved Corpus state,
 * which saint resides within, and how many times extraction has been attempted.
 */
public class SaintSarcophagusBlockEntity extends BlockEntity implements IMultiBlockEntity {

	private EnumSaintType saintType = EnumSaintType.HEMORATH;
	private EnumCorpusState corpusState = EnumCorpusState.DORMANT;
	private int extractionAttempts = 0;
	private int cooldownTicks = 0;

	public SaintSarcophagusBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.saint_sarcophagus.get(), pos, state);
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
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString("SaintType", saintType.name());
		tag.putString("CorpusState", corpusState.name());
		tag.putInt("ExtractionAttempts", extractionAttempts);
		tag.putInt("CooldownTicks", cooldownTicks);
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
	}
}
