package com.vincenthuto.hemomancy.containers.slot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;

public class InfoTableSlot {
	public static Codec<InfoTableSlot> CODEC = RecordCodecBuilder.create(instance -> instance
			.group( CompoundTag.CODEC.listOf().fieldOf("info")
					.forGetter(o -> o.info.values().stream().map(InfoBaseSlot::getTag).collect(Collectors.toList())))
			.apply( instance, InfoTableSlot::new));
	private final Map<String, InfoBaseSlot<?>> info = new HashMap();

	public InfoTableSlot() {
	}

	public InfoTableSlot(List<CompoundTag> info) {
		this();
		info.stream().map(InfoBaseSlot::fromNBT).forEach(this::add);
	}

	public boolean isEmpty() {
		return this.info.isEmpty();
	}

	@Nullable
	public InfoBaseSlot<?> getByKey(String key) {
		return this.info.get(key);
	}

	public void add(InfoBaseSlot<?> value) {
		this.info.put(value.getKey(), value);
	}
}
