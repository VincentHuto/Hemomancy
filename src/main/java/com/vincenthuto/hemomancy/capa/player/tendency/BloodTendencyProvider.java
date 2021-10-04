package com.vincenthuto.hemomancy.capa.player.tendency;

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

public class BloodTendencyProvider implements ICapabilitySerializable<Tag> {
	@CapabilityInject(IBloodTendency.class)
	public static final Capability<IBloodTendency> TENDENCY_CAPA = null;
	private LazyOptional<IBloodTendency> instance = LazyOptional.of(BloodTendency::new);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return TENDENCY_CAPA.orEmpty(cap, instance);

	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(TENDENCY_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(TENDENCY_CAPA,
				instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);

	}

	public static Map<EnumBloodTendency, Float> getPlayerTendency(Player player) {
		return player.getCapability(TENDENCY_CAPA).orElseThrow(IllegalStateException::new).getTendency();
	}

	public CompoundTag writeNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side) {
		CompoundTag covenTag = new CompoundTag();
		instance.getTendency().keySet().forEach((key -> {
			if (instance.getTendency().get(key) != null) {
				covenTag.putFloat(key.toString(), instance.getTendency().get(key));
			} else {
				covenTag.putFloat(key.toString(), 0);

			}
		}));
		return covenTag;
	}

	public void readNBT(Capability<IBloodTendency> capability, IBloodTendency instance, Direction side, Tag nbt) {
		if (!(instance instanceof BloodTendency))
			throw new IllegalArgumentException(
					"Can not deserialize to an instance that isn't the default implementation");
		CompoundTag test = (CompoundTag) nbt;
		for (EnumBloodTendency coven : EnumBloodTendency.values()) {
			instance.getTendency().put(coven, test.getFloat(coven.toString()));
		}
	}
}
