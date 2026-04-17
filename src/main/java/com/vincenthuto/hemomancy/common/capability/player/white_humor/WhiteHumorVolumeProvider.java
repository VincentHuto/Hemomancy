package com.vincenthuto.hemomancy.common.capability.player.white_humor;

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

public class WhiteHumorVolumeProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<IWhiteHumorVolume> WHITE_HUMOR_VOLUME_CAPA = CapabilityManager
			.get(new CapabilityToken<IWhiteHumorVolume>() {
			});

	WhiteHumorVolume capability = new WhiteHumorVolume();

	private LazyOptional<IWhiteHumorVolume> instance = LazyOptional.of(() -> capability);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(WHITE_HUMOR_VOLUME_CAPA, instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")),
				null, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == WHITE_HUMOR_VOLUME_CAPA ? instance.cast() : LazyOptional.empty();
	}

	public void readNBT(Capability<IWhiteHumorVolume> capability, IWhiteHumorVolume instance, Direction side, Tag nbt) {
		if (!(instance instanceof WhiteHumorVolume))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		if (nbt instanceof CompoundTag entry) {
			if (entry.contains("Active") && entry.contains("Max") && entry.contains("Volume")
					&& entry.contains("Bloodline")) {
				instance.setActive(entry.getBoolean("Active"));
				instance.setMaxWhiteHumorVolume(entry.getDouble("Max"));
				instance.setWhiteHumorVolume(entry.getDouble("Volume"));
				instance.setWhiteHumorLine(Bloodline.deserialize(entry.getCompound("Bloodline")));
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
		return writeNBT(WHITE_HUMOR_VOLUME_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	public CompoundTag writeNBT(Capability<IWhiteHumorVolume> capability, IWhiteHumorVolume instance, Direction side) {
		CompoundTag entry = new CompoundTag();
		entry.putBoolean("Active", instance.isActive());
		entry.putDouble("Max", instance.getMaxWhiteHumorVolume());
		entry.putDouble("Volume", instance.getWhiteHumorVolume());
		entry.put("Bloodline", instance.getWhiteHumorLine().serialize());
		// Bloodline pool donation & auto-draw settings
		entry.putBoolean("TrickleEnabled", instance.isTrickleEnabled());
		entry.putDouble("TrickleRate", instance.getTrickleRate());
		entry.putBoolean("AutoDrawEnabled", instance.isAutoDrawEnabled());
		entry.putDouble("AutoDrawThreshold", instance.getAutoDrawThreshold());
		return entry;
	}
}
