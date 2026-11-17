package com.vincenthuto.hemomancy.common.menu.tile.functional;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.menu.HemoContainerMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenBrood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessel;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VesselView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * A Specimen Jar's or Vat's screen (core-loop round two, P6): no slots, only the vessel's position and its
 * {@link VesselView} data, which the server works out for its one viewer. Its one button asks the vessel to pair or
 * part its brood ({@link SpecimenVessel#pressBrood}, which runs {@link SpecimenBrood#press}); the work is never the
 * menu's.
 */
public class SpecimenVesselMenu extends HemoContainerMenu {
	public static final int BROOD_BUTTON = 0;
	private static final double REACH_SQR = 8.0D * 8.0D;

	private final BlockPos pos;
	private final Level level;
	private final ContainerData data;

	/** The client's copy, opened by the server's {@code openMenu(vessel, pos)}. */
	public SpecimenVesselMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
		this(containerId, inventory, buffer.readBlockPos(), new SimpleContainerData(VesselView.COUNT));
	}

	public SpecimenVesselMenu(int containerId, Inventory inventory, SpecimenVessel vessel, ServerPlayer viewer) {
		this(containerId, inventory, ((BlockEntity) vessel).getBlockPos(), new VesselView(vessel, viewer));
	}

	private SpecimenVesselMenu(int containerId, Inventory inventory, BlockPos pos, ContainerData data) {
		super(ContainerInit.specimen_vessel.get(), containerId);
		this.pos = pos.immutable();
		this.level = inventory.player.level();
		this.data = data;
		addDataSlots(data);
	}

	public BlockPos pos() {
		return pos;
	}

	/** The vessel this screen is of, or null when it is gone. */
	@Nullable
	public SpecimenVessel vessel() {
		return level.isLoaded(pos) && level.getBlockEntity(pos) instanceof SpecimenVessel vessel ? vessel.vessel() : null;
	}

	public int data(int slot) {
		return data.get(slot);
	}

	public boolean flag(int flag) {
		return (data.get(VesselView.FLAGS) & flag) != 0;
	}

	@Override
	public boolean stillValid(Player player) {
		SpecimenVessel vessel = vessel();
		return vessel != null && !((BlockEntity) vessel).isRemoved() && vessel.kept().occupied() && player.isAlive()
				&& player.distanceToSqr(pos.getCenter()) <= REACH_SQR;
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id != BROOD_BUTTON) return false;
		SpecimenVessel vessel = vessel();
		if (vessel != null && player instanceof ServerPlayer presser && level instanceof ServerLevel server
				&& stillValid(player)) {
			vessel.pressBrood(server, presser);
		}
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
