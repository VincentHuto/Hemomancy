package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.util.List;

import com.vincenthuto.hemomancy.common.init.ItemInit;

import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.core.NonNullList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class VialRackItem extends Item {
	public static final String TAG_VIALS = "Vials";
	public static final int MAX_VIALS = 8;

	public VialRackItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		int emptyCount = countEmptyVials(stack);
		tooltip.add(Component.translatable("item.hemomancy.vial_rack.empty_count", emptyCount, MAX_VIALS));
		if (Screen.hasShiftDown()) {
			NonNullList<ItemStack> vials = getVials(stack);
			for (int i = 0; i < vials.size(); i++) {
				ItemStack vial = vials.get(i);
				Component slotLabel = Component.literal("[" + (i + 1) + "] ").withStyle(ChatFormatting.DARK_GRAY);
				if (isEmptyVial(vial)) {
					tooltip.add(slotLabel.copy().append(
							Component.translatable("item.hemomancy.vial_rack.slot_empty").withStyle(ChatFormatting.GRAY)));
				} else {
					EntityType<?> entityType = BloodVialItem.getEntityType(vial);
					String entityName = entityType != null
							? net.minecraft.client.resources.language.I18n.get(entityType.getDescriptionId()) + " Sample"
							: vial.getHoverName().getString();
					tooltip.add(slotLabel.copy().append(Component.literal(entityName).withStyle(ChatFormatting.RED)));
				}
			}
		} else {
			if (emptyCount < MAX_VIALS) {
				tooltip.add(Component.translatable("item.hemomancy.vial_rack.shift_hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			}
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
			defaults.set(i, createDefaultVial());
		}
		return defaults;
	}

	public static ItemStack createDefaultVial() {
		return new ItemStack(ItemInit.bloody_vial.get());
	}

	public static void ensureInitialized(ItemStack rack) {
		CompoundTag tag = rack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains(TAG_VIALS, Tag.TAG_LIST) || tag.getList(TAG_VIALS, Tag.TAG_COMPOUND).size() != MAX_VIALS) {
			setVials(rack, createDefaultVials());
		}
	}

	public static NonNullList<ItemStack> getVials(ItemStack rack) {
		ensureInitialized(rack);
		NonNullList<ItemStack> vials = NonNullList.withSize(MAX_VIALS, ItemStack.EMPTY);
		ListTag list = rack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList(TAG_VIALS, Tag.TAG_COMPOUND);
		for (int i = 0; i < MAX_VIALS; i++) {
			vials.set(i, i < list.size() ? ItemStack.parseOptional(provider(), list.getCompound(i)) : ItemStack.EMPTY);
		}
		return vials;
	}

	public static void setVials(ItemStack rack, NonNullList<ItemStack> vials) {
		CompoundTag tag = rack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ListTag list = new ListTag();
		for (int i = 0; i < MAX_VIALS; i++) {
			ItemStack slotStack = i < vials.size() ? vials.get(i) : ItemStack.EMPTY;
			list.add(slotStack.save(provider()));
		}
		tag.put(TAG_VIALS, list);
		rack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static HolderLookup.Provider provider() {
		return ServerLifecycleHooks.getCurrentServer() != null
				? ServerLifecycleHooks.getCurrentServer().registryAccess()
				: RegistryAccess.EMPTY;
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
