package com.vincenthuto.hemomancy.common.item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;

public class VialRackItem extends Item {
	public static final String TAG_VIALS = "Vials";
	public static final int MAX_VIALS = 8;

	public VialRackItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, level, tooltip, flagIn);
		NonNullList<ItemStack> vials = getVials(stack);
		tooltip.add(Component.translatable("item.hemomancy.vial_rack.empty_count", countEmptyVials(stack), MAX_VIALS));
		Map<String, Integer> sampleCounts = new LinkedHashMap<>();
		for (ItemStack vial : vials) {
			if (!isEmptyVial(vial)) {
				String key = vial.getHoverName().getString();
				sampleCounts.merge(key, 1, Integer::sum);
			}
		}
		for (Map.Entry<String, Integer> entry : sampleCounts.entrySet()) {
			tooltip.add(Component.literal("- " + entry.getKey() + " x" + entry.getValue()));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		ensureInitialized(stack);
	}

	private static NonNullList<ItemStack> createDefaultVials() {
		NonNullList<ItemStack> defaults = NonNullList.withSize(MAX_VIALS, ItemStack.EMPTY);
		for (int i = 0; i < MAX_VIALS; i++) {
			defaults.set(i, new ItemStack(ItemInit.bloody_vial.get()));
		}
		return defaults;
	}

	public static void ensureInitialized(ItemStack rack) {
		CompoundTag tag = rack.getOrCreateTag();
		if (!tag.contains(TAG_VIALS, Tag.TAG_LIST) || tag.getList(TAG_VIALS, Tag.TAG_COMPOUND).size() != MAX_VIALS) {
			setVials(rack, createDefaultVials());
		}
	}

	public static NonNullList<ItemStack> getVials(ItemStack rack) {
		ensureInitialized(rack);
		NonNullList<ItemStack> vials = NonNullList.withSize(MAX_VIALS, ItemStack.EMPTY);
		ListTag list = rack.getOrCreateTag().getList(TAG_VIALS, Tag.TAG_COMPOUND);
		for (int i = 0; i < MAX_VIALS; i++) {
			vials.set(i, i < list.size() ? ItemStack.of(list.getCompound(i)) : ItemStack.EMPTY);
		}
		return vials;
	}

	public static void setVials(ItemStack rack, NonNullList<ItemStack> vials) {
		ListTag list = new ListTag();
		for (int i = 0; i < MAX_VIALS; i++) {
			ItemStack slotStack = i < vials.size() ? vials.get(i) : ItemStack.EMPTY;
			list.add(slotStack.save(new CompoundTag()));
		}
		rack.getOrCreateTag().put(TAG_VIALS, list);
	}

	public static boolean isEmptyVial(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == ItemInit.bloody_vial.get() && BloodVialItem.getEntityType(stack) == null;
	}

	public static int countEmptyVials(ItemStack rack) {
		int count = 0;
		for (ItemStack vial : getVials(rack)) {
			if (isEmptyVial(vial)) {
				count++;
			}
		}
		return count;
	}

	public static int findFirstEmptyVialSlot(ItemStack rack) {
		NonNullList<ItemStack> vials = getVials(rack);
		for (int i = 0; i < vials.size(); i++) {
			if (isEmptyVial(vials.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public static boolean hasLoadableCapacity(ItemStack rack) {
		return countEmptyVials(rack) > 0;
	}

	public static boolean hasFilledVials(ItemStack rack) {
		for (ItemStack vial : getVials(rack)) {
			if (!vial.isEmpty() && !isEmptyVial(vial)) {
				return true;
			}
		}
		return false;
	}
}
