package com.vincenthuto.hemomancy.compat.mna;

import com.mna.api.events.RunicAnvilItemUsedEvent;
import com.mna.api.events.RunicAnvilShouldActivateEvent;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.items.ItemInit;
import com.mna.items.armor.DyeableMageArmor;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class MnAPlugin {

	// Events
	public static void onRegisterGuidebooks(RegisterGuidebooksEvent event) {

		event.getRegistry().addGuidebookPath(Hemomancy.rloc("mna_guide"));
		event.getRegistry().registerGuidebookCategory("Hemomancy",
				Hemomancy.rloc("charm_of_vascularium"));
	}

	// Anvil
	public static void onRunicAnvil(RunicAnvilShouldActivateEvent event) {
		ItemStack patternStack = event.pattern;
		ItemStack materialStack = event.material;

		if (patternStack.getItem() instanceof DyeableMageArmor
				&& materialStack.getItem() == MnAPluginItemInit.living_infused_thread.get()) {
			event.setResult(Event.Result.ALLOW);
		}
	}

	public static void playerInteractAnvil(PlayerInteractEvent event) {
		BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
		ItemStack activeStack = event.getItemStack();
		Player player = event.getEntity();
		if (be instanceof RunicAnvilTile te) {
			RunicAnvilItemUsedEvent anvilEvent = new RunicAnvilItemUsedEvent(te.getItem(0), te.getItem(1), activeStack,
					player);
			ItemStack patternStack = anvilEvent.pattern;
			ItemStack materialStack = anvilEvent.material;
			ItemStack catalystStack = anvilEvent.getPlayer().getMainHandItem();

			if (catalystStack.getItem() == ItemInit.SORCEROUS_SEWING_SET.get()
					&& materialStack.getItem() == MnAPluginItemInit.living_infused_thread.get()) {
				ItemStack result = ItemStack.EMPTY;
				if (patternStack.getItem() == ItemInit.MAGE_HOOD.get()) {
					result = new ItemStack(MnAPluginItemInit.living_thread_hood.get());
				} else if (patternStack.getItem() == ItemInit.MAGE_ROBES.get()) {
					result = new ItemStack(MnAPluginItemInit.living_thread_robes.get());
				} else if (patternStack.getItem() == ItemInit.MAGE_LEGGINGS.get()) {
					result = new ItemStack(MnAPluginItemInit.living_thread_leggings.get());
				} else if (patternStack.getItem() == ItemInit.MAGE_BOOTS.get()) {
					result = new ItemStack(MnAPluginItemInit.living_thread_boots.get());
				}

				if (!result.isEmpty()) {
					te.setItem(0, ItemStack.EMPTY);
					te.setItem(1, ItemStack.EMPTY);
					te.setItem(0, result);
					catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
					});
					event.setResult(Event.Result.ALLOW);
				}
			}
		}

	}

}
