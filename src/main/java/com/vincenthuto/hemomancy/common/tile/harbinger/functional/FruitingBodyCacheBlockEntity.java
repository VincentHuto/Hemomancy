package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A dead Apotheos's gear, held in a fruiting body at the death point until its owner comes back for it. */
public class FruitingBodyCacheBlockEntity extends HemoBlockEntity {
	@Nullable
	private UUID owner;
	private final List<ItemStack> stacks = new ArrayList<>();

	public FruitingBodyCacheBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.fruiting_body_cache.get(), pos, state);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	public void store(UUID owner, List<ItemStack> items) {
		this.owner = owner;
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) stacks.add(stack.copy());
		}
		setChanged();
	}

	/** Empties the cache, handing every stack it held to the caller. */
	public List<ItemStack> takeAll() {
		List<ItemStack> taken = new ArrayList<>(stacks);
		stacks.clear();
		setChanged();
		return taken;
	}

	public int size() {
		return stacks.size();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (owner != null) tag.putUUID("Owner", owner);
		ListTag list = new ListTag();
		for (ItemStack stack : stacks) list.add(stack.save(registries));
		tag.put("Items", list);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		stacks.clear();
		for (Tag element : tag.getList("Items", Tag.TAG_COMPOUND)) {
			ItemStack.parse(registries, element).ifPresent(stacks::add);
		}
	}
}
