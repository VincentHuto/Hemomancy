package com.vincenthuto.hemomancy.capa.vascular;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VascularSystemProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IVascularSystem.class)
	public static final Capability<IVascularSystem> VASCULAR_CAPA = null;
	private LazyOptional<IVascularSystem> instance = LazyOptional.of(VascularSystem::new);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return VASCULAR_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(VASCULAR_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static Map<EnumVeinSections, Float> getPlayerVascularSystem(Player player) {
		return player.getCapability(VASCULAR_CAPA).orElseThrow(IllegalStateException::new).getVascularSystem();
	}

	public CompoundTag writeNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumVeinSections key : instance.getVascularSystem().keySet()) {
			if (instance.getVascularSystem().get(key) != null) {
				covenTag.putFloat(key.toString(), instance.getVascularSystem().get(key));
			} else {
				covenTag.putFloat(key.toString(), 100f);

			}
		}
		return covenTag;
	}

	public void readNBT(Capability<IVascularSystem> capability, IVascularSystem instance, Direction side, Tag nbt) {
		if (!(instance instanceof VascularSystem))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundTag test = (CompoundTag) nbt;
		for (EnumVeinSections key : EnumVeinSections.values()) {
			instance.getVascularSystem().put(key, test.getFloat(key.toString()));
		}
	}

}
