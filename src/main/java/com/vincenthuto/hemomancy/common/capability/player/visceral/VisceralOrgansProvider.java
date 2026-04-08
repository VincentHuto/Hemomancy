package com.vincenthuto.hemomancy.common.capability.player.visceral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VisceralOrgansProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<IVisceralOrgans> ORGANS_CAPA =
			CapabilityManager.get(new CapabilityToken<IVisceralOrgans>() {});

	private LazyOptional<IVisceralOrgans> instance = LazyOptional.of(VisceralOrgans::new);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return ORGANS_CAPA.orEmpty(cap, instance);
	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")));
	}

	@Override
	public void invalidateCaps() {
		instance.invalidate();
	}

	private CompoundTag writeNBT(IVisceralOrgans organs) {
		CompoundTag tag = new CompoundTag();
		for (EnumOrgan organ : EnumOrgan.values()) {
			tag.putInt(organ.name().toLowerCase(), organs.getOrganLevel(organ));
		}
		return tag;
	}

	private void readNBT(IVisceralOrgans organs, Tag nbt) {
		if (nbt instanceof CompoundTag tag) {
			for (EnumOrgan organ : EnumOrgan.values()) {
				String key = organ.name().toLowerCase();
				if (tag.contains(key)) {
					organs.setOrganLevel(organ, tag.getInt(key));
				}
			}
		}
	}
}
