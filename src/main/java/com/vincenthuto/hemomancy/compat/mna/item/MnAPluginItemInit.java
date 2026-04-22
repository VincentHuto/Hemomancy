package com.vincenthuto.hemomancy.compat.mna.item;

import com.mna.items.ItemInit;
import com.mna.items.artifice.ItemFactionHorn;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.MnAPluginArmorTiers;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.compat.mna.spell.MnAPluginManipulationInit;
import com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MnAPluginItemInit {

	public static final DeferredRegister<Item> MNAITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);


	public static final DeferredHolder<Item, Item> blood_shot_occulus = MNAITEMS.register("blood_shot_occulus",
			() -> new BloodShotOcculusItem(new Item.Properties()));


	// Faction
	public static final DeferredHolder<Item, Item> mark_of_blood = MNAITEMS.register("mark_of_blood",
			() -> new Item(new Item.Properties()));

	public static final DeferredHolder<Item, Item> harbinger_grimore = MNAITEMS.register("harbinger_grimore",
			() -> new HarbingerGrimoireItem(new Item.Properties().rarity(Rarity.EPIC),
					HarbingerEventHandler.FACTION_HARBINGERS_ID, Hemomancy.rloc("item/special/grimoire_harbinger_open"),
					Hemomancy.rloc("item/special/grimoire_harbinger_closed"), true));

	public static final DeferredHolder<ItemFactionHorn, ItemFactionHorn> faction_horn_harbingers = MNAITEMS.register(
			"faction_horn_harbingers", () -> new ItemFactionHorn(HarbingerEventHandler.FACTION_HARBINGERS_ID));

	// Items

	public static final DeferredHolder<Item, Item> befouled_vinteum_dust = MNAITEMS.register("befouled_vinteum_dust",
			() -> new Item(new Item.Properties()));

	public static final DeferredHolder<Item, Item> foul_vinteum_ingot = MNAITEMS.register("foul_vinteum_ingot",
			() -> new Item(new Item.Properties()));
	public static final DeferredHolder<Item, Item> mana_infushed_memory_blank = MNAITEMS.register("mana_infushed_memory_blank",
			() -> new Item(new Item.Properties()));

	public static final DeferredHolder<Item, Item> living_infused_thread = MNAITEMS.register("living_infused_thread",
			() -> new Item(new Item.Properties()));
	public static final DeferredHolder<Item, Item> mote_blood = MNAITEMS.register("mote_blood",
			() -> new Item(new Item.Properties()));

	// Memories (MnA-dependent manipulations)
	public static final DeferredHolder<Item, Item> mana_memory_sanguine_transfusion = MNAITEMS.register(
			"mana_memory_sanguine_transfusion",
			() -> new BloodMemoryItem(new Item.Properties(), MnAPluginManipulationInit.mana_memory_sanguine_transfusion));

	// Robes
	public static final DeferredHolder<Item, Item> living_thread_hood = MNAITEMS.register("living_thread_hood",
			() -> new DyeableLivingThreadArmor(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.HELMET,
					(new Item.Properties()).fireResistant(), 0.25f, 0.01f));
	public static final DeferredHolder<Item, Item> living_thread_robes = MNAITEMS.register("living_thread_robes",
			() -> new DyeableLivingThreadArmor(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.CHESTPLATE,
					(new Item.Properties()).fireResistant(), 0.25f, 0.01f));
	public static final DeferredHolder<Item, Item> living_thread_leggings = MNAITEMS.register("living_thread_leggings",
			() -> new DyeableLivingThreadArmor(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.LEGGINGS,
					(new Item.Properties()).fireResistant(), 0.25f, 0.01f));
	public static final DeferredHolder<Item, Item> living_thread_boots = MNAITEMS.register("living_thread_boots",
			() -> new DyeableLivingThreadArmor(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.BOOTS,
					(new Item.Properties()).fireResistant(), 0.25f, 0.01f));

	public static void buildMnaCompatItemContents(BuildCreativeModeTabContentsEvent populator) {
		if (populator.getTabKey() == Hemomancy.hemomancytab.getKey()) {
			MNAITEMS.getEntries().forEach(i -> populator.accept(i.get()));
			populator.accept(ItemInit.GREATER_MOTE_BLOOD.get());

		}
	}

}
