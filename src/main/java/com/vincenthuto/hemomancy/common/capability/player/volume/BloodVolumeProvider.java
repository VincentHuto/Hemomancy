package com.vincenthuto.hemomancy.common.capability.player.volume;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class BloodVolumeProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<IBloodVolume> VOLUME_CAPA = CapabilityManager
			.get(new CapabilityToken<IBloodVolume>() {
			});

	BloodVolume capability = new BloodVolume();

	private LazyOptional<IBloodVolume> instance = LazyOptional.of(() -> capability);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(VOLUME_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == VOLUME_CAPA ? instance.cast() : LazyOptional.empty();
	}

	public void readNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof BloodVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag entry) {
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
					&& entry.contains("Bloodline")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxBloodVolume(entry.getDouble("Max"));
				instance.setBloodVolume(entry.getDouble("Volume"));
				instance.setBloodLine(Bloodline.deserialize(entry.getCompound("Bloodline")));
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
		return writeNBT(VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public CompoundTag writeNBT(Capability<IBloodVolume> capability, IBloodVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putDouble("Max", instance.getMaxBloodVolume());
		entry.putDouble("Volume", instance.getBloodVolume());
		entry.put("Bloodline", instance.getBloodLine().serialize());
		// Bloodline pool donation & auto-draw settings
		entry.putBoolean("TrickleEnabled", instance.isTrickleEnabled());
		entry.putDouble("TrickleRate", instance.getTrickleRate());
		entry.putBoolean("AutoDrawEnabled", instance.isAutoDrawEnabled());
		entry.putDouble("AutoDrawThreshold", instance.getAutoDrawThreshold());
		return entry;
	}
}
