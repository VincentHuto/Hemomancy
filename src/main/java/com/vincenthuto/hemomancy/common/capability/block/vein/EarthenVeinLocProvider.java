package com.vincenthuto.hemomancy.common.capability.block.vein;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EarthenVeinLocProvider implements ICapabilitySerializable<CompoundTag> {
	public static final Capability<IEarthenVeinLoc> VEIN_CAPA = CapabilityManager
			.get(new CapabilityToken<IEarthenVeinLoc>() {
			});
	public static VeinLocation getLocation(Player player) {
		return player.getCapability(VEIN_CAPA).orElseThrow(IllegalStateException::new).getVeinLocation();
	}

	private LazyOptional<IEarthenVeinLoc> instance = LazyOptional.of(EarthenVeinLoc::new);

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		readNBT(VEIN_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);

	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VEIN_CAPA.orEmpty(cap, instance);
	}

	public void readNBT(Capability<IEarthenVeinLoc> capability, IEarthenVeinLoc instance, Direction side,
			CompoundTag nbt) {
		instance.setVeinLoc(VeinLocation.deserializeToLoc(nbt.getCompound("loc")));

	}

	@Override
	public CompoundTag serializeNBT() {
		return writeNBT(VEIN_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public CompoundTag writeNBT(Capability<IEarthenVeinLoc> capability, IEarthenVeinLoc instance, Direction side) {
		CompoundTag loc = new CompoundTag();
		loc.put("loc", instance.getVeinLocation().serializeNBT());
		return loc;
	}

}
