package com.vincenthuto.hemomancy.radial;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IRadialInventorySelect extends IRadialMenuItem {
	public static final String NBT_INDEX = "index";

	@Override
	default public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		String txt = I18n.get((String)RadialClientEvents.OPEN_TOOL_MENU_KEYBIND.getKey().getDisplayName().getString(),
				(Object[]) new Object[0]);
		tooltip.add((Component) new TranslatableComponent("item.mna.item-with-gui.radial-open", new Object[] { txt })
				.withStyle(ChatFormatting.AQUA));
	}

	public int capacity();

	public void setIndex(ItemStack var1, int var2);

	public int getIndex(ItemStack var1);

	default public IItemHandlerModifiable getInventory(ItemStack stackEquipped) {
		return new ItemInventoryBase(stackEquipped, this.capacity());
	}

	default public void setSlot(Player player, ItemStack stack, int index, boolean offhand, boolean packet) {
		if (stack.getItem() instanceof IRadialInventorySelect) {
			CompoundTag tag = stack.getOrCreateTag();
			tag.putInt("index", index);
			if (packet) {
				ClientMessageDispatcher.sendRadialInventorySlotChange(index, offhand);
			}
		}
	}
}
