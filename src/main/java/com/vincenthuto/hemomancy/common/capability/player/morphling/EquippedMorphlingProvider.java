package com.vincenthuto.hemomancy.common.capability.player.morphling;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EquippedMorphlingProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<IEquippedMorphling> MORPHLING_CAPA = CapabilityManager
			.get(new CapabilityToken<IEquippedMorphling>() {
			});

	private final EquippedMorphling capability = new EquippedMorphling();
	private final LazyOptional<IEquippedMorphling> instance = LazyOptional.of(() -> capability);

	@Override
	public void deserializeNBT(Tag nbt) {
		if (nbt instanceof CompoundTag tag) {
			if (tag.contains("EquippedMorphling")) {
				capability.setEquippedMorphling(ItemStack.of(tag.getCompound("EquippedMorphling")));
			} else {
				capability.clearMorphling();
			}
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return cap == MORPHLING_CAPA ? instance.cast() : LazyOptional.empty();
	}

	@Override
	public Tag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		ItemStack stack = capability.getEquippedMorphling();
		if (!stack.isEmpty()) {
			tag.put("EquippedMorphling", stack.save(new CompoundTag()));
		}
		return tag;
	}

}
