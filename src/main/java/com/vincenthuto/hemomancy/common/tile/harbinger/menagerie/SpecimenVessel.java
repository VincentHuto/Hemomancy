package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.menu.tile.functional.SpecimenVesselMenu;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A block that keeps a creature (core-loop Track 5): the Specimen Jar and the Specimen Vat. Its tank is a small living
 * buffer that never clots (rule 2: something alive holds it) and that the creature eats as Vigor; it is filled by
 * Projection or by its owner's suture like any machine. An empty hand on a kept creature opens the vessel's screen
 * (P6), a {@link SpecimenVesselMenu} with no slots that shows the creature, its blood and its brood.
 *
 * Specimen Vats join into one larger vessel; any vat of it answers for the whole through {@link #vessel()}, so
 * code that finds a vessel by its block reads {@code vessel()} before using it.
 */
public interface SpecimenVessel extends IBloodReservoir, MenuProvider {
	KeptSpecimen kept();

	/** The living buffer's size. */
	double bufferCapacity();

	/** Whether a creature (a led one, or a young born beside it) fits this vessel. */
	boolean fits(Mob mob);

	/** Why a creature that may be kept does not fit, to tell its keeper; null when it fits or no reason is given. */
	@Nullable
	default Component tooSmallFor(Mob mob) {
		return null;
	}

	/** The vessel this block belongs to: itself, or the vat that runs a joined vessel. */
	default SpecimenVessel vessel() {
		return this;
	}

	/** The block the vessel stands on and reports from: where it runs, keeps its creature and lets it go. */
	default BlockPos vesselPos() {
		return ((BlockEntity) this).getBlockPos();
	}

	/** The middle of the vessel's glass. */
	default Vec3 glassCentre() {
		BlockPos pos = vesselPos();
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D);
	}

	/** The middle of the vessel's lid, above the glass and the creature: where the brood's thread leaves it. */
	default Vec3 lidCentre() {
		BlockPos pos = vesselPos();
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.92D, pos.getZ() + 0.5D);
	}

	/** The blocks beside the vessel at its own height(s), where a mate or a young's vessel may stand. */
	default List<BlockPos> beside() {
		List<BlockPos> around = new ArrayList<>();
		for (Direction direction : SpecimenBrood.AROUND) around.add(vesselPos().relative(direction));
		return around;
	}

	@Override
	default boolean isLivingVessel() {
		return true;
	}

	/** The buffer, sized to {@link #bufferCapacity()}. */
	default BlockBloodVolume buffer() {
		BlockBloodVolume volume = ReservoirBlood.settled((BlockEntity) this);
		if (!volume.isActive()) volume.setActive(true);
		if (volume.capacity() != bufferCapacity()) volume.setMaxBloodVolume(bufferCapacity());
		return volume;
	}

	/**
	 * The brood button on this vessel's screen (P6): pairs its creature with a neighbour or parts the pair. The work is
	 * the vessel's ({@link SpecimenBrood#press}); its menu only dispatches it.
	 */
	default BroodRules.Refusal pressBrood(ServerLevel level, ServerPlayer presser) {
		return SpecimenBrood.press(level, this, presser);
	}

	@Override
	default Component getDisplayName() {
		return Component.translatable("container.hemomancy.specimen_vessel");
	}

	@Nullable
	@Override
	default AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return player instanceof ServerPlayer viewer ? new SpecimenVesselMenu(containerId, inventory, vessel(), viewer) : null;
	}
}
