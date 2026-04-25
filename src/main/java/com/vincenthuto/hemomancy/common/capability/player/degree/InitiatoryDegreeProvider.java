package com.vincenthuto.hemomancy.common.capability.player.degree;

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

public class InitiatoryDegreeProvider implements ICapabilitySerializable<Tag> {

	public static final Capability<IInitiatoryDegree> DEGREE_CAPA =
			CapabilityManager.get(new CapabilityToken<IInitiatoryDegree>() {});

	public static int getPlayerDegreeNumber(Player player) {
		return player.getCapability(DEGREE_CAPA)
				.orElseThrow(IllegalStateException::new)
				.getDegreeNumber();
	}

	private LazyOptional<IInitiatoryDegree> instance = LazyOptional.of(InitiatoryDegree::new);

	@Override
	public void deserializeNBT(Tag nbt) {
		readNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return DEGREE_CAPA.orEmpty(cap, instance);
	}

	@Override
	public Tag serializeNBT() {
		return writeNBT(instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")));
	}

	private CompoundTag writeNBT(IInitiatoryDegree instance) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("degree", instance.getDegreeNumber());
		tag.putBoolean("pome_communion_done", instance.isQliphothCommunionDone());
		tag.putLong("pome_empowerment_expiry", instance.getPomeEmpowermentExpiry());
		tag.putInt("pome_total_consumed", instance.getTotalPomesConsumed());

		CompoundTag progressTag = new CompoundTag();
		instance.getPomeCommunionProgress().forEach((origin, count) ->
				progressTag.putInt(String.valueOf(origin), count));
		tag.put("pome_communion_progress", progressTag);
		return tag;
	}

	private void readNBT(IInitiatoryDegree instance, Tag nbt) {
		if (nbt instanceof CompoundTag tag) {
			instance.setDegreeNumber(tag.getInt("degree"));
			instance.setQliphothCommunionDone(tag.getBoolean("pome_communion_done"));
			instance.setPomeEmpowermentExpiry(tag.getLong("pome_empowerment_expiry"));
			instance.syncTotalPomesConsumed(tag.getInt("pome_total_consumed"));
			instance.getPomeCommunionProgress().clear();
			CompoundTag progressTag = tag.getCompound("pome_communion_progress");
			for (String key : progressTag.getAllKeys()) {
				try {
					instance.getPomeCommunionProgress().put(Long.parseLong(key), progressTag.getInt(key));
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}
}
