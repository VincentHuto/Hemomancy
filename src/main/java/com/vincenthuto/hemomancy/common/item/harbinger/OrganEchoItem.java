package com.vincenthuto.hemomancy.common.item.harbinger;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * An "Echo of" organ item produced by the Visceral Mirror ritual.
 * These items represent a sanguine echo — a spectral imprint of the
 * organ's essence, not the physical organ itself — pulled from the
 * player's own reflection for modification.
 *
 * <h3>Restrictions</h3>
 * <ul>
 *   <li>Only one echo of each organ type may exist in a player's
 *       inventory at a time. Duplicates are dissolved on tick.</li>
 *   <li>Echo items are bound to the player. If placed into any
 *       non-player inventory (chest, hopper, etc.) the item dissolves
 *       — it cannot persist outside a living host.</li>
 *   <li>Organ modification state is ultimately saved on the player's
 *       {@link com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans}
 *       capability, not on the item.</li>
 * </ul>
 */
public class OrganEchoItem extends Item {

	private final EnumOrgan organ;

	public OrganEchoItem(Properties prop, EnumOrgan organ) {
		super(prop.stacksTo(1).rarity(Rarity.EPIC));
		this.organ = organ;
	}

	public EnumOrgan getOrgan() {
		return organ;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal(organ.getDescription()).withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Tier " + organ.getTier() + " Visceral Echo")
				.withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.literal("Dissolves if removed from your person.")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true; // All echoes shimmer — they are spectral imprints
	}

	/**
	 * Called every tick while the item sits in a player's inventory.
	 * Enforces the "only one echo per organ type" rule by dissolving
	 * duplicate stacks.
	 */
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (level.isClientSide) return;
		if (!(entity instanceof Player player)) return;

		// Count how many echoes of this organ type the player is carrying
		int count = 0;
		int firstSlot = -1;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack invStack = player.getInventory().getItem(i);
			if (!invStack.isEmpty() && invStack.getItem() instanceof OrganEchoItem echoItem
					&& echoItem.getOrgan() == this.organ) {
				count++;
				if (firstSlot == -1) {
					firstSlot = i;
				}
			}
		}

		// If there are duplicates, dissolve all but the first
		if (count > 1 && slotId != firstSlot) {
			player.getInventory().setItem(slotId, ItemStack.EMPTY);
		}
	}

	/**
	 * Returns {@code false} to signal that this item should not normally
	 * be moved into bundle-like containers.
	 * The actual dissolve-on-chest enforcement happens in
	 * {@link com.vincenthuto.hemomancy.common.capability.player.visceral.VisceralOrgansEvents}.
	 */
	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}
}
