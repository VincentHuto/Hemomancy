package com.vincenthuto.hemomancy.compat.mna;

import com.mna.api.events.RunicAnvilItemUsedEvent;
import com.mna.api.events.RunicAnvilShouldActivateEvent;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.items.ItemInit;
import com.mna.items.armor.DyeableMageArmor;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.Event;

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

		if (patternStack.getItem() instanceof DyeableMageArmor) {
			if (materialStack.getItem() == MnAPluginItemInit.living_infused_thread.get()) {
				event.setResult(Event.Result.ALLOW);
			}
		}
	}

	public static void playerInteractAnvil(PlayerInteractEvent event) {
		BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
		ItemStack activeStack = event.getItemStack();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();

		if (be instanceof RunicAnvilTile te) {
			RunicAnvilItemUsedEvent anvilEvent = new RunicAnvilItemUsedEvent(te.getItem(0), te.getItem(1), activeStack,
					player);
			ItemStack patternStack = anvilEvent.pattern;
			ItemStack materialStack = anvilEvent.material;
			ItemStack catalystStack = anvilEvent.getPlayer().getMainHandItem();

			if (catalystStack.getItem() == ItemInit.SORCEROUS_SEWING_SET.get()) {
				if (materialStack.getItem() == MnAPluginItemInit.living_infused_thread.get()) {

					if (patternStack.getItem() == ItemInit.MAGE_HOOD.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(MnAPluginItemInit.living_thread_hood.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_ROBES.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(MnAPluginItemInit.living_thread_robes.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_LEGGINGS.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(MnAPluginItemInit.living_thread_leggings.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_BOOTS.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(MnAPluginItemInit.living_thread_boots.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
				}
			}
		}

	}

}
