package com.vincenthuto.hemomancy.compat.mna;

import com.mna.Registries;
import com.mna.api.events.CastingResourceGuiRegistrationEvent;
import com.mna.api.events.CastingResourceRegistrationEvent;
import com.mna.api.events.RunicAnvilItemUsedEvent;
import com.mna.api.events.RunicAnvilShouldActivateEvent;
import com.mna.api.faction.BaseFaction;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.items.ItemInit;
import com.mna.items.armor.DyeableMageArmor;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.armor.EnumModArmorTiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class MnAPlugin {

	public static final ResourceLocation FACTION_HARBINGERS_ID = Hemomancy.rloc("harbingers_faction");
	public static final ResourceLocation HARBINGERS_MANA = Hemomancy.rloc("harbingers_mana");
	public static final ResourceLocation HARBINGERS_HUD_TEXTURE = Hemomancy
			.rloc("textures/gui/gui_harbingers_manabars.png");
	public static final BaseFaction HARBINGERS_FACTION = new HarbingersFaction();

	public static final DeferredRegister<Item> MNAITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);

	// Items
	public static final RegistryObject<Item> mark_of_blood = MNAITEMS.register("mark_of_blood",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> befouled_vinteum_dust = MNAITEMS.register("befouled_vinteum_dust",
			() -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> living_infused_thread = MNAITEMS.register("living_infused_thread",
			() -> new Item(new Item.Properties()));

	// Robes
	public static final RegistryObject<Item> living_thread_hood = MNAITEMS.register("living_thread_hood",
			() -> new ArmorItem(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.HELMET,
					(new Item.Properties()).fireResistant()));
	public static final RegistryObject<Item> living_thread_robes = MNAITEMS.register("living_thread_robes",
			() -> new ArmorItem(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.CHESTPLATE,
					(new Item.Properties()).fireResistant()));
	public static final RegistryObject<Item> living_thread_leggings = MNAITEMS.register("living_thread_leggings",
			() -> new ArmorItem(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.LEGGINGS,
					(new Item.Properties()).fireResistant()));
	public static final RegistryObject<Item> living_thread_boots = MNAITEMS.register("living_thread_boots",
			() -> new ArmorItem(MnAPluginArmorTiers.LIVING_THREAD, ArmorItem.Type.BOOTS,
					(new Item.Properties()).fireResistant()));

	public static void buildMnaCompatContents(BuildCreativeModeTabContentsEvent populator) {
		if (populator.getTabKey() == Hemomancy.hemomancytab.getKey()) {
			MNAITEMS.getEntries().forEach(i -> populator.accept(i.get()));
		}
	}

	// Events
	public static void onRegisterGuidebooks(RegisterGuidebooksEvent event) {

		event.getRegistry().addGuidebookPath(new ResourceLocation(Hemomancy.MOD_ID, "mna_guide"));
		event.getRegistry().registerGuidebookCategory("Hemomancy",
				new ResourceLocation(Hemomancy.MOD_ID, "charm_of_vascularium"));
	}

	// Anvil
	public static void onRunicAnvil(RunicAnvilShouldActivateEvent event) {
		ItemStack patternStack = event.pattern;
		ItemStack materialStack = event.material;

		if (patternStack.getItem() instanceof DyeableMageArmor) {
			if (materialStack.getItem() == living_infused_thread.get()) {
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
				if (materialStack.getItem() == living_infused_thread.get()) {

					if (patternStack.getItem() == ItemInit.MAGE_HOOD.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(living_thread_hood.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_ROBES.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(living_thread_robes.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_LEGGINGS.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(living_thread_leggings.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
					if (patternStack.getItem() == ItemInit.MAGE_BOOTS.get()) {
						te.setItem(0, ItemStack.EMPTY);
						te.setItem(1, ItemStack.EMPTY);
						te.setItem(0, new ItemStack(living_thread_boots.get()));
						catalystStack.hurtAndBreak(1, (LivingEntity) player, e -> {
						});
						event.setResult(Event.Result.ALLOW);
					}
				}
			}
		}

	}

	// Faction
	public static void registerFactions(RegisterEvent event) {
		event.register(Registries.Factions.get().getRegistryKey(), (helper) -> {
			helper.register(FACTION_HARBINGERS_ID, HARBINGERS_FACTION);
		});
	}

	public static void onCastingResourceRegistrationEvent(CastingResourceRegistrationEvent event) {
		event.getRegistry().register(HARBINGERS_MANA, HarbingersMana.class);
	}

	// client event
	public static class MnAPluginClient {
		public static void onCastingResourceRegistrationEventClient(CastingResourceGuiRegistrationEvent event) {
			event.getRegistry().registerResourceGui(HARBINGERS_MANA, new HarbingersManaGui());
		}
	}

}
