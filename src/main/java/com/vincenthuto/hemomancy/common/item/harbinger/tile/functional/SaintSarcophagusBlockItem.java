package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

/**
 * The Saint Sarcophagus as an item: one item for every tomb, the Saint carried in the stack's custom data under the
 * key {@code SaintSarcophagusBlock} reads when it is placed. A stack with no Saint places an unnamed tomb, which a
 * relic names later. The creative tab lists the unnamed tomb and one stack per Saint, each drawn in its palette.
 */
public class SaintSarcophagusBlockItem extends BlockItem {
	private static final String TAG_SAINT_TYPE = "SaintType";

	public SaintSarcophagusBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	/** A tomb stack holding {@code saint}. */
	public static ItemStack forSaint(BlockItem item, EnumSaintType saint) {
		ItemStack stack = new ItemStack(item);
		CompoundTag tag = new CompoundTag();
		tag.putString(TAG_SAINT_TYPE, saint.name());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	/** The Saint the stack holds, or null for an unnamed tomb. */
	@Nullable
	public static EnumSaintType saint(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) return null;
		CompoundTag tag = data.copyTag();
		if (!tag.contains(TAG_SAINT_TYPE)) return null;
		try {
			return EnumSaintType.valueOf(tag.getString(TAG_SAINT_TYPE));
		} catch (IllegalArgumentException unknown) {
			return null;
		}
	}

	/**
	 * The item model override for the stack: 1 + the Saint's index in {@link SarcophagusGeometry#SAINT_PALETTES}, or 0
	 * for the default palette (item/saint_sarcophagus overrides, written by saint_sarcophagus.py in the same order).
	 */
	public static float palette(ItemStack stack) {
		String atlas = SarcophagusGeometry.atlas(saint(stack));
		for (int i = 0; i < SarcophagusGeometry.SAINT_PALETTES.length; i++) {
			if (atlas.equals("saint_sarcophagus_" + SarcophagusGeometry.SAINT_PALETTES[i])) return i + 1;
		}
		return 0;
	}

	public static List<ItemStack> creativeStacks(BlockItem item) {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(new ItemStack(item));
		for (EnumSaintType saint : EnumSaintType.values()) stacks.add(forSaint(item, saint));
		return stacks;
	}

	@Override
	public Component getName(ItemStack stack) {
		EnumSaintType saint = saint(stack);
		return saint == null ? super.getName(stack)
				: Component.translatable("block.hemomancy.saint_sarcophagus.of", saint.getDisplayName());
	}
}
