package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * What an offering stand holds: one Litany candidate and how many of it are left. The template stocks it as
 * {@code Item} (the candidate with its count); the stand remembers its candidate after the last is taken, so the same
 * item can be given back to it.
 */
public class OfferingStandBlockEntity extends HemoBlockEntity {
	/** As many of its candidate as a stand will hold; the template stocks four. */
	public static final int CAPACITY = 4;
	private ItemStack candidate = ItemStack.EMPTY;
	private int count;

	public OfferingStandBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.offering_stand.get(), pos, state);
	}

	/** The candidate with the count left, empty when the stand is bare. */
	public ItemStack held() {
		return count > 0 ? candidate.copyWithCount(count) : ItemStack.EMPTY;
	}

	public void stock(ItemStack stack) {
		candidate = stack.copyWithCount(1);
		count = stack.isEmpty() ? 0 : Math.min(stack.getCount(), CAPACITY);
		BlockEntitySync.markAndSync(this);
	}

	/** One of the candidate, or empty when the stand is bare. */
	public ItemStack take() {
		if (count <= 0) return ItemStack.EMPTY;
		count--;
		BlockEntitySync.markAndSync(this);
		return candidate.copyWithCount(1);
	}

	/** Whether {@code stack} can go back on the stand: its own candidate, while there is room. */
	public boolean accepts(ItemStack stack) {
		return !stack.isEmpty() && !candidate.isEmpty() && ItemStack.isSameItemSameComponents(candidate, stack)
				&& count < CAPACITY;
	}

	public void give(ItemStack stack, @Nullable Player player) {
		if (!accepts(stack)) return;
		count++;
		if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
		BlockEntitySync.markAndSync(this);
	}

	private void write(CompoundTag tag, HolderLookup.Provider registries) {
		if (candidate.isEmpty()) return;
		tag.put("Candidate", candidate.save(registries));
		tag.putInt("Count", count);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		write(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains("Item")) {
			// As the structure template stocks it: the candidate and its count in one stack.
			ItemStack stocked = ItemStack.parseOptional(registries, tag.getCompound("Item"));
			candidate = stocked.copyWithCount(1);
			count = stocked.isEmpty() ? 0 : Math.min(stocked.getCount(), CAPACITY);
		} else {
			candidate = tag.contains("Candidate") ? ItemStack.parseOptional(registries, tag.getCompound("Candidate"))
					: ItemStack.EMPTY;
			count = candidate.isEmpty() ? 0 : tag.getInt("Count");
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		write(tag, registries);
		return tag;
	}
}
