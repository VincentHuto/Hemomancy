package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.tile.SharedTankMember;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * One Specimen Vat (core-loop Track 5), on the vat's lower half. Vats standing together join into one square vessel,
 * one to three vats across and one or two high ({@link VatAssembly}); the vessel's lowest corner vat runs it: it keeps
 * the creature ({@link KeptSpecimen}) and the vessel's one living buffer, 800 mL a vat. Every other vat of the vessel
 * answers for that one: its creature, buffer, size and screen are the running vat's.
 */
public class SpecimenVatBlockEntity extends HemoBlockEntity implements SpecimenVessel, SharedTankMember {
	private static final String TAG_RUNNER = "VatRunner";
	private static final String TAG_WIDTH = "VatWidth";
	private static final String TAG_LAYERS = "VatLayers";
	private final KeptSpecimen kept = new KeptSpecimen();
	private boolean suppressNextRelease;
	/** Where the vat running this one's vessel stands, from this one; null when this vat runs its vessel. */
	@Nullable
	private BlockPos runnerOffset;
	/** The vessel's size in vats, kept by the running vat. */
	private int width = 1;
	private int layers = 1;

	public SpecimenVatBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.specimen_vat.get(), pos, state);
	}

	// ── The vessel ──

	/** Whether this vat runs its vessel (a lone vat always does). */
	public boolean runsVessel() {
		return runnerOffset == null;
	}

	/** The vat running this one's vessel: this one when it runs it, or when that vat is not there. */
	public SpecimenVatBlockEntity runner() {
		if (runnerOffset == null || level == null) return this;
		BlockPos at = worldPosition.offset(runnerOffset);
		return level.isLoaded(at) && level.getBlockEntity(at) instanceof SpecimenVatBlockEntity vat && vat.runsVessel() ? vat : this;
	}

	/** How many vats the vessel is across. */
	public int width() {
		return runner().width;
	}

	/** How many vats the vessel is high. */
	public int layers() {
		return runner().layers;
	}

	/** How many vats the vessel is made of. */
	public int vats() {
		SpecimenVatBlockEntity runner = runner();
		return runner.width * runner.width * runner.layers;
	}

	/** {@link VatAssembly}: this vat runs a vessel {@code width} across and {@code layers} high. */
	void runAs(int width, int layers) {
		runnerOffset = null;
		this.width = width;
		this.layers = layers;
	}

	/** {@link VatAssembly}: this vat belongs to the vessel run by the vat at {@code runner}. */
	void joinTo(BlockPos runner) {
		runnerOffset = runner.subtract(worldPosition);
		width = 1;
		layers = 1;
		kept.clear();
	}

	/** The lower halves of every vat in this one's vessel, the running vat first. */
	public List<BlockPos> vatPositions() {
		SpecimenVatBlockEntity runner = runner();
		List<BlockPos> vats = new ArrayList<>();
		for (int layer = 0; layer < runner.layers; layer++) {
			for (int dx = 0; dx < runner.width; dx++) {
				for (int dz = 0; dz < runner.width; dz++) vats.add(runner.worldPosition.offset(dx, layer * 2, dz));
			}
		}
		return vats;
	}

	@Override
	public BlockEntity tankHolder() {
		return runner();
	}

	@Override
	public SpecimenVessel vessel() {
		return runner();
	}

	@Override
	public BlockPos vesselPos() {
		return runner().worldPosition;
	}

	@Override
	public KeptSpecimen kept() {
		SpecimenVatBlockEntity runner = runner();
		return runner == this ? kept : runner.kept();
	}

	@Override
	public double bufferCapacity() {
		return VatVigorRules.VAT_BUFFER * vats();
	}

	@Override
	public boolean fits(Mob mob) {
		return SpecimenVatBlock.fits(mob, width(), layers());
	}

	@Nullable
	@Override
	public Component tooSmallFor(Mob mob) {
		if (fits(mob) || !SpecimenVatBlock.keeps(mob)) return null;
		int[] needed = MenagerieRules.smallestVat(mob.getBbWidth(), mob.getBbHeight(), mob.getMaxHealth());
		if (needed == null) return Component.translatable("message.hemomancy.menagerie.vat_cannot_hold", mob.getDisplayName());
		return Component.translatable("message.hemomancy.menagerie.vat_too_small", mob.getDisplayName(), needed[0], needed[0],
				needed[1]);
	}

	/** The middle of the vessel's floor, where its creature stands and is let go. */
	private Vec3 floorCentre() {
		SpecimenVatBlockEntity runner = runner();
		BlockPos pos = runner.worldPosition;
		return new Vec3(pos.getX() + runner.width * 0.5D, pos.getY(), pos.getZ() + runner.width * 0.5D);
	}

	@Override
	public Vec3 glassCentre() {
		return floorCentre().add(0.0D, SpecimenVatBlock.FLOOR + MenagerieRules.vatClearHeight(layers()) * 0.5D, 0.0D);
	}

	@Override
	public Vec3 lidCentre() {
		return floorCentre().add(0.0D, layers() * 2.0D + 0.05D, 0.0D);
	}

	@Override
	public List<BlockPos> beside() {
		List<BlockPos> vats = vatPositions();
		List<BlockPos> around = new ArrayList<>();
		for (BlockPos vat : vats) {
			for (Direction direction : SpecimenBrood.AROUND) {
				BlockPos next = vat.relative(direction);
				if (!vats.contains(next)) around.add(next);
			}
		}
		return around;
	}

	/** Where a creature let out of the vessel stands: the middle of its floor. */
	public BlockPos releasePos() {
		return BlockPos.containing(floorCentre());
	}

	@Override
	public void sendUpdates() {
		BlockEntitySync.markAndSync(runner());
	}

	// ── Ticking and lifting ──

	public static void serverTick(Level level, BlockPos pos, BlockState state, SpecimenVatBlockEntity vat) {
		if (vat.runsVessel()) SpecimenVessels.serverTick(level, pos, vat);
	}

	public void suppressNextRelease() {
		suppressNextRelease = true;
	}

	public boolean consumeSuppressNextRelease() {
		boolean suppressed = suppressNextRelease;
		suppressNextRelease = false;
		return suppressed;
	}

	// ── Saving ──

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		kept.load(tag);
		suppressNextRelease = false;
		runnerOffset = tag.contains(TAG_RUNNER) ? BlockPos.of(tag.getLong(TAG_RUNNER)) : null;
		width = Math.max(1, Math.min(MenagerieRules.VAT_MAX_WIDTH, tag.contains(TAG_WIDTH) ? tag.getInt(TAG_WIDTH) : 1));
		layers = Math.max(1, Math.min(MenagerieRules.VAT_MAX_LAYERS, tag.contains(TAG_LAYERS) ? tag.getInt(TAG_LAYERS) : 1));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		kept.save(tag);
		if (runnerOffset != null) tag.putLong(TAG_RUNNER, runnerOffset.asLong());
		if (width > 1) tag.putInt(TAG_WIDTH, width);
		if (layers > 1) tag.putInt(TAG_LAYERS, layers);
	}
}
