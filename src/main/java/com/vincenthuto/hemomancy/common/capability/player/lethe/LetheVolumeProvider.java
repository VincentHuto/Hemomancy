package com.vincenthuto.hemomancy.common.capability.player.lethe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LetheVolumeProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<ILetheVolume> LETHE_VOLUME_CAPA = CapabilityManager
			.get(new CapabilityToken<ILetheVolume>() {
			});

	LetheVolume capability = new LetheVolume();

	private LazyOptional<ILetheVolume> instance = LazyOptional.of(() -> capability);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(LETHE_VOLUME_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == LETHE_VOLUME_CAPA ? instance.cast() : LazyOptional.empty();
	}

	public void readNBT(Capability<ILetheVolume> capability, ILetheVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof LetheVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag entry) {
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
					&& entry.contains("Bloodline")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxLetheVolume(entry.getDouble("Max"));
				instance.setLetheVolume(entry.getDouble("Volume"));
				instance.setLetheLine(Bloodline.deserialize(entry.getCompound("Bloodline")));
				// Bloodline pool donation & auto-draw settings
				instance.setTrickleEnabled(entry.getBoolean("TrickleEnabled"));
				instance.setTrickleRate(entry.getDouble("TrickleRate"));
				instance.setAutoDrawEnabled(entry.getBoolean("AutoDrawEnabled"));
				instance.setAutoDrawThreshold(entry.getDouble("AutoDrawThreshold"));
			}
		}

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(LETHE_VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public CompoundTag writeNBT(Capability<ILetheVolume> capability, ILetheVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putDouble("Max", instance.getMaxLetheVolume());
		entry.putDouble("Volume", instance.getLetheVolume());
		entry.put("Bloodline", instance.getLetheLine().serialize());
		// Bloodline pool donation & auto-draw settings
		entry.putBoolean("TrickleEnabled", instance.isTrickleEnabled());
		entry.putDouble("TrickleRate", instance.getTrickleRate());
		entry.putBoolean("AutoDrawEnabled", instance.isAutoDrawEnabled());
		entry.putDouble("AutoDrawThreshold", instance.getAutoDrawThreshold());
		return entry;
	}
}
