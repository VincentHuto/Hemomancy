package com.vincenthuto.hemomancy.gui.radial;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.containers.slot.InfoTableSlot;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ItemStackEntry {
	public static Codec<ItemStackEntry> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ItemStack.CODEC.fieldOf("item").forGetter(o -> o.stack),
					Codec.LONG.fieldOf("count").forGetter(o -> o.count),
					InfoTableSlot.CODEC.optionalFieldOf("info").forGetter(
							o -> o.info == null || o.info.isEmpty() ? Optional.empty() : Optional.of(o.info)))
			.apply(instance, (a, b, c) -> new ItemStackEntry((ItemStack) a, (long) b, c.orElse(new InfoTableSlot()))));
	public static ItemStackEntry EMPTY = new ItemStackEntry(ItemStack.EMPTY);
	private ItemStack stack;
	private long count;
	private InfoTableSlot info;

	public ItemStackEntry(ItemStack stack) {
		this.stack = stack;
		this.count = stack.getCount();
		this.info = null;
	}

	private ItemStackEntry(ItemStack stack, long count, InfoTableSlot info) {
		this.stack = stack;
		this.count = count;
		this.info = info;
	}

	public ItemStack getStackOriginal() {
		return this.stack;
	}

	public ItemStack getStackCopy() {
		ItemStack copy = this.stack.copy();
		copy.setCount((int) this.count);
		return copy;
	}

	public ItemStack extract(int count) {
		if ((long) count > this.count) {
			count = (int) this.count;
		}
		if (this.stack.getCount() == count) {
			this.count = 0L;
			ItemStack result = this.stack;
			this.stack = ItemStack.EMPTY;
			return result;
		}
		ItemStack copy = this.getStackCopy();
		copy.setCount(count);
		this.count -= (long) count;
		return copy;
	}

	public long getCount() {
		return this.count;
	}

	public void setCount(long count) {
		this.count = count;
		this.stack.setCount((int) this.count);
	}

	public void grow(long count) {
		this.count += count;
		this.stack.setCount((int) this.count);
	}

	public InfoTableSlot getInfo() {
		return this.info;
	}

	public boolean isEmpty() {
		if (this.stack.isEmpty()) {
			return true;
		}
		return this.count == 0L && this.info.isEmpty();
	}

	public Tag serialize() {
		return (Tag) ((DataResult) NbtOps.INSTANCE.withEncoder(CODEC).apply(this)).getOrThrow(false,
				arg_0 -> (Hemomancy.LOGGER).error(arg_0));
	}

	public static ItemStackEntry deserialize(Tag tag) {
		if (tag == null) {
			return EMPTY;
		}
		return (ItemStackEntry) CODEC.parse((DynamicOps) NbtOps.INSTANCE, (Object) tag).getOrThrow(false,
				arg_0 -> (Hemomancy.LOGGER).error(arg_0));
	}
}
