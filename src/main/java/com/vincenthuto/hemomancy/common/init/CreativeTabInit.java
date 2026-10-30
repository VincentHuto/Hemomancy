package com.vincenthuto.hemomancy.common.init;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The two creative tabs and what goes in them. The main tab lists the registers in order, minus
 * the hidden items and everything on {@link #WIP}, grouped by {@link CreativeTabSection}; the WIP
 * tab lists exactly {@link #WIP}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CreativeTabInit {

	public static final DeferredRegister<CreativeModeTab> CREATIVETABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, Hemomancy.MOD_ID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> hemomancytab = CREATIVETABS.register(
			"hemomancytab",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("item_group." + Hemomancy.MOD_ID + ".hemomancytab"))
					.icon(() -> new ItemStack(ItemInit.charm_of_vascularium.get()))
					// Bone title and a crimson hover over the client's banded frame (CreativeTabChrome).
					.withLabelColor(0xE6D8BC)
					.withSlotColor(0x60D04848)
					.build());
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> hemomancywiptab = CREATIVETABS.register(
			"hemomancywip",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("item_group." + Hemomancy.MOD_ID + ".hemomancywip"))
					.withTabsBefore(hemomancytab.getKey())
					.icon(() -> new ItemStack(ItemInit.sanguine_blob.get()))
					.build());

	/** The WIP tab in display order. Each entry is also kept out of the main tab. */
	private static final List<DeferredHolder<?, ? extends ItemLike>> WIP = List.of(
			ItemInit.CHAMBER_OF_WILL_TELEPORTER,
			ItemInit.structure_spawner,
			ItemInit.structure_scanner,
			ItemInit.debug_showcase,
			ItemInit.drudge_electrode,
			ItemInit.drudge_submission_device,
			ItemInit.hematic_suture_needle,
			ItemInit.unsigned_ancestral_ledger,
			ItemInit.sanguine_blob,
			ItemInit.vascular_poultice,
			ItemInit.memory_thread,
			ItemInit.void_eye_organ,
			ItemInit.zombie_husk_effigy,
			ItemInit.desert_husk_effigy,
			ItemInit.spider_husk_effigy,
			ItemInit.vitality_chalice,
			ItemInit.vein_spider,
			ItemInit.blood_thrall_effigy,
			ItemInit.blood_bolt,
			ItemInit.vascular_status_gauge,
			ItemInit.hemorath_rib,
			ItemInit.echo_of_spleen,
			ItemInit.echo_of_liver,
			ItemInit.echo_of_heart,
			ItemInit.echo_of_lungs,
			ItemInit.echo_of_kidneys,
			ItemInit.draught_of_still_mercy,
			ItemInit.lethean_brew,
			ItemInit.spawn_egg_peacock_spider,
			BlockInit.semi_sentient_construct,
			BlockInit.humane_idol,
			BlockInit.serpentine_idol,
			BlockInit.morphling_cradle,
			BlockInit.witness_organ,
			BlockInit.gourdvine_tap,
			BlockInit.sanguine_vigil,
			BlockInit.sanguine_omen,
			BlockInit.visceral_mirror,
			BlockInit.non_euclidean_hallway,
			BlockInit.blood_pylon,
			BlockInit.blood_trial_altar,
			BlockInit.offering_gate,
			BlockInit.crucible_of_nether_ichor,
			BlockInit.voidtouched_vessel);

	// Ids, so the block check works while items are still being registered.
	private static final Set<ResourceLocation> WIP_ITEMS = wipIds(Registries.ITEM.location());
	private static final Set<ResourceLocation> WIP_BLOCKS = wipIds(Registries.BLOCK.location());

	private CreativeTabInit() {
	}

	private static Set<ResourceLocation> wipIds(ResourceLocation registry) {
		return WIP.stream()
				.filter(holder -> holder.getKey().registry().equals(registry))
				.map(DeferredHolder::getId)
				.collect(Collectors.toUnmodifiableSet());
	}

	/** Whether a block's item belongs in the WIP tab rather than the main one. */
	public static boolean isWipBlock(ResourceLocation blockId) {
		return WIP_BLOCKS.contains(blockId);
	}

	private static boolean isWipItem(Item item) {
		return WIP_ITEMS.contains(BuiltInRegistries.ITEM.getKey(item));
	}

	@SubscribeEvent
	public static void buildContents(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == hemomancytab.getKey()) {
			// Gathered in registry order, then handed on grouped by CreativeTabSection.
			CreativeTabSection.Sorter populator = new CreativeTabSection.Sorter();
			ItemInit.getAllItemEntriesAsStream().forEach(item -> {
				if (item.get() instanceof ConsecratedSyringeItem) {
					// One per Saint rather than the random default.
					for (EnumSaintType saint : EnumSaintType.values()) {
						ItemStack syringe = new ItemStack(item.get());
						ConsecratedSyringeItem.setSaintType(syringe, saint);
						populator.accept(syringe);
					}
				} else if (shouldShowItemInCreativeTab(item.get()) && item.get() != ItemInit.fervent_husk.get()
						&& item.get() != ItemInit.cleansing_hemolymph.get()) {
					populator.accept(item.get());
					if (item.get() == ItemInit.bloody_vial.get()) {
						populator.accept(ItemInit.cleansing_hemolymph.get());
					}
					if (item.get() == ItemInit.chitinous_husk.get()) {
						populator.accept(ItemInit.fervent_husk.get());
					}
				}
			});
			LivingWeaponGraftItem.creativeStacks().forEach(populator::accept);
			BlockInit.getAllBlockEntriesAsStream().forEach(block -> {
				if (shouldShowBlockInCreativeTab(block.get())) {
					populator.accept(block.get());
				}
			});
			populator.emitTo(event);
		} else if (event.getTabKey() == hemomancywiptab.getKey()) {
			for (DeferredHolder<?, ? extends ItemLike> entry : WIP) {
				event.accept(entry.get());
			}
		}
	}

	private static boolean shouldShowItemInCreativeTab(Item item) {
		// The dictation table's item comes from BlockInit's RegisterEvent, so this item loop never
		// meets it and the check does nothing (audit F-22, awaiting a decision).
		return item != BlockInit.dictation_table.get().asItem()
				&& item != ItemInit.tome_of_the_unstained.get()
				&& item != ItemInit.active_befouling_ash.get()
				&& item != ItemInit.active_smouldering_ash.get()
				&& item != ItemInit.living_weapon_graft.get()
				&& item != ItemInit.living_sickle.get()
				&& item != ItemInit.synaptic_step.get()
				&& item != ItemInit.memory_conjure_living_staff.get()
				&& !isLegacyLivingWeaponMemory(item)
				&& !ManipulationRetirementRules.isRetiredMemoryItem(item)
				&& !isWipItem(item);
	}

	private static boolean isLegacyLivingWeaponMemory(Item item) {
		return item == ItemInit.memory_living_blade.get()
				|| item == ItemInit.memory_living_axe.get()
				|| item == ItemInit.memory_living_spear.get()
				|| item == ItemInit.memory_living_claws.get()
				|| item == ItemInit.memory_living_crossbow.get()
				|| item == ItemInit.memory_living_torch.get()
				|| item == ItemInit.memory_living_flail.get();
	}

	private static boolean shouldShowBlockInCreativeTab(Block block) {
		var decision = BlockInit.itemDecision(BuiltInRegistries.BLOCK.getKey(block));
		return block.asItem() != Items.AIR
				&& decision.creativeRoute() == BlockInit.CreativeRoute.MAIN
				&& (decision.itemRoute() == BlockInit.ItemRoute.AUTO
						|| decision.itemRoute() == BlockInit.ItemRoute.CUSTOM);
	}
}
