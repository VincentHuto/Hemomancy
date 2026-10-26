package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.shared.IMultiBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Block entity for the Saint Sarcophagus — tracks the Preserved Corpus state,
 * which saint resides within, and how many times extraction has been attempted.
 * A sarcophagus holds no Saint until a relic names it (or its item carries one);
 * Saints are never chosen at random.
 */
public class SaintSarcophagusBlockEntity extends HemoBlockEntity implements IMultiBlockEntity {

	public static final int OFFERING_THRESHOLD = 3;

	@Nullable
	private EnumSaintType saintType;
	private EnumCorpusState corpusState = EnumCorpusState.DORMANT;
	private int extractionAttempts = 0;
	private int cooldownTicks = 0;
	private boolean isOpen = false;
	private boolean isConsecrated = false;
	private int offeringBloodVolume = 0;
	private boolean sampleYielded = false;

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

	/** The Saint at rest here, or null while no relic has named one. */
	@Nullable
	public EnumSaintType getSaintType() {
		return saintType;
	}

	public void setSaintType(EnumSaintType saintType) {
		this.saintType = saintType;
		setChanged();
	}

	public boolean isSaintTypeAssigned() {
		return saintType != null;
	}

	/**
	 * The tomb as a Saint's trial leaves it: {@code saint} back inside, dormant, lid closed, unconsecrated, no
	 * offerings, nothing drawn and no cooldown.
	 */
	public void restoreDormant(EnumSaintType saint) {
		this.saintType = saint;
		this.corpusState = EnumCorpusState.DORMANT;
		this.extractionAttempts = 0;
		this.cooldownTicks = 0;
		this.isOpen = false;
		this.isConsecrated = false;
		this.offeringBloodVolume = 0;
		this.sampleYielded = false;
		BlockEntitySync.markAndSync(this);
	}

	public boolean hasSampleYielded() {
		return sampleYielded;
	}

	public void setSampleYielded(boolean sampleYielded) {
		this.sampleYielded = sampleYielded;
		setChanged();
	}

	public EnumCorpusState getCorpusState() {
		return corpusState;
	}

	public void setCorpusState(EnumCorpusState state) {
		this.corpusState = state;
		setChanged();
		// The renderer kindles the corpus's veins by state.
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
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
		// The offering bowl's blood level is drawn from this count.
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
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

	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putBoolean("IsOpen", isOpen);
		tag.putBoolean("IsConsecrated", isConsecrated);
		tag.putInt("OfferingBloodVolume", offeringBloodVolume);
		if (saintType != null) tag.putString("SaintType", saintType.name());
		tag.putString("CorpusState", corpusState.name());
		tag.putBoolean("SampleYielded", sampleYielded);
		return tag;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		if (saintType != null) tag.putString("SaintType", saintType.name());
		tag.putString("CorpusState", corpusState.name());
		tag.putInt("ExtractionAttempts", extractionAttempts);
		tag.putInt("CooldownTicks", cooldownTicks);
		tag.putBoolean("IsOpen", isOpen);
		tag.putBoolean("IsConsecrated", isConsecrated);
		tag.putInt("OfferingBloodVolume", offeringBloodVolume);
		tag.putBoolean("SaintTypeAssigned", saintType != null);
		tag.putBoolean("SampleYielded", sampleYielded);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		// An older save stored HEMORATH with SaintTypeAssigned false before its first tick: that tomb was never named.
		saintType = null;
		if (tag.contains("SaintType") && (!tag.contains("SaintTypeAssigned") || tag.getBoolean("SaintTypeAssigned"))) {
			try {
				saintType = EnumSaintType.valueOf(tag.getString("SaintType"));
			} catch (IllegalArgumentException e) {
				saintType = null;
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
		sampleYielded = tag.getBoolean("SampleYielded");
	}
}
