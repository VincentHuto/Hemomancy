package com.vincenthuto.hemomancy.capa.block.vein;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.util.VeinLocation;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EarthenVeinLocProvider implements ICapabilitySerializable<CompoundTag> {
	@CapabilityInject(IEarthenVeinLoc.class)
	public static final Capability<IEarthenVeinLoc> VEIN_CAPA = null;
	private LazyOptional<IEarthenVeinLoc> instance = LazyOptional.of(EarthenVeinLoc::new);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VEIN_CAPA.orEmpty(cap, instance);
	}

	@Override
	public CompoundTag serializeNBT() {
		return writeNBT(VEIN_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		readNBT(VEIN_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);

	}

	public static VeinLocation getLocation(Player player) {
		return player.getCapability(VEIN_CAPA).orElseThrow(IllegalStateException::new).getVeinLocation();
	}

	public CompoundTag writeNBT(Capability<IEarthenVeinLoc> capability, IEarthenVeinLoc instance, Direction side) {
		CompoundTag loc = new CompoundTag();
		loc.put("loc", instance.getVeinLocation().serializeNBT());
		return loc;
	}

	public void readNBT(Capability<IEarthenVeinLoc> capability, IEarthenVeinLoc instance, Direction side,
			CompoundTag nbt) {
		instance.setVeinLoc(VeinLocation.deserializeToLoc(nbt.getCompound("loc")));

	}

}