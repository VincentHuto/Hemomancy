package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessel;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatVigorRules;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Specimen Jar's creature and its small living buffer (core-loop Track 5): a kept creature lives on Vigor, fed from
 * the buffer, and gives Temper to held Absorption; see {@link KeptSpecimen}. A jar a structure set down has no owner
 * and its specimen is preserved.
 */
public class SpecimenJarBlockEntity extends HemoBlockEntity implements SpecimenVessel {
	private final KeptSpecimen kept = new KeptSpecimen();
	private boolean suppressNextRelease = false;

	public SpecimenJarBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.specimen_jar.get(), pos, state);
	}

	@Override
	public KeptSpecimen kept() {
		return kept;
	}

	@Override
	public double bufferCapacity() {
		return VatVigorRules.JAR_BUFFER;
	}

	@Override
	public boolean fits(Mob mob) {
		return SpecimenJarBlock.fits(mob);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SpecimenJarBlockEntity jar) {
		SpecimenVessels.serverTick(level, pos, jar);
	}

	public boolean hasSpecimen() {
		return kept.occupied();
	}

	public CompoundTag getSpecimenCopy() {
		return kept.specimen();
	}

	/** Puts a saved creature in the jar at the captured Vigor. */
	public void setSpecimen(CompoundTag specimen) {
		kept.keep(specimen, KeptSpecimen.savedMaxHealth(specimen), VatVigorRules.CAPTURED,
				level == null ? 0L : level.getGameTime());
		BlockEntitySync.markAndSync(this);
	}

	public void clearSpecimen() {
		kept.clear();
		BlockEntitySync.markAndSync(this);
	}

	public void suppressNextRelease() {
		this.suppressNextRelease = true;
	}

	public boolean consumeSuppressNextRelease() {
		boolean suppressed = suppressNextRelease;
		suppressNextRelease = false;
		return suppressed;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		kept.load(tag);
		suppressNextRelease = false;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		kept.save(tag);
	}
}
