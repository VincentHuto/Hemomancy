package com.vincenthuto.hemomancy.radial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;

public class SlotInfoTable {
	public static Codec<SlotInfoTable> CODEC = RecordCodecBuilder.create(instance -> instance
			.group( CompoundTag.CODEC.listOf().fieldOf("info")
					.forGetter(o -> o.info.values().stream().map(SlotInfoBase::getTag).collect(Collectors.toList())))
			.apply( instance, SlotInfoTable::new));
	private final Map<String, SlotInfoBase<?>> info = new HashMap();

	public SlotInfoTable() {
	}

	public SlotInfoTable(List<CompoundTag> info) {
		this();
		info.stream().map(SlotInfoBase::fromNBT).forEach(this::add);
	}

	public boolean isEmpty() {
		return this.info.isEmpty();
	}

	@Nullable
	public SlotInfoBase<?> getByKey(String key) {
		return this.info.get(key);
	}

	public void add(SlotInfoBase<?> value) {
		this.info.put(value.getKey(), value);
	}
}
