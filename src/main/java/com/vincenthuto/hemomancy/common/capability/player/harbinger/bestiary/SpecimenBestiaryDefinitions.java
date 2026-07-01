package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpecimenBestiaryDefinitions {
	private static final List<ResearchEntry> RESEARCH_ENTRIES = List.of(
			specimen("barbed_urchin"),
			specimen("blood_lantern_jelly"),
			specimen("chalybeate_snail"),
			specimen("chitinite"),
			specimen("crimson_doe"),
			specimen("desiccant"),
			specimen("fervent_chitinite"),
			specimen("hematic_burrower"),
			specimen("hemojelly"),
			specimen("hemolymphopoda"),
			specimen("lantern_tick"),
			specimen("morphling_polyp"),
			specimen("prism_cuttle"),
			specimen("scarlet_serpent"),
			specimen("tooth_pecks"),
			specimen("venom_rib_centipede"),
			specimen("venous_strider"),
			specimen("verdigris_moth")
	);

	private static final List<MorphlingEntry> MORPHLING_ENTRIES = List.of(
			morphling(MorphlingPolypLayer.BAT, ItemInit.morphling_bat),
			morphling(MorphlingPolypLayer.CENTIPEDE, ItemInit.morphling_centipede),
			morphling(MorphlingPolypLayer.CHITINITE, ItemInit.morphling_chitinite),
			morphling(MorphlingPolypLayer.CUTTLEFISH, ItemInit.morphling_cuttlefish),
			morphling(MorphlingPolypLayer.FUNGAL, ItemInit.morphling_fungal),
			morphling(MorphlingPolypLayer.LEECHES, ItemInit.morphling_leeches),
			morphling(MorphlingPolypLayer.MOLE, ItemInit.morphling_mole),
			morphling(MorphlingPolypLayer.PESTS, ItemInit.morphling_pests),
			morphling(MorphlingPolypLayer.SERPENT, ItemInit.morphling_serpent),
			morphling(MorphlingPolypLayer.SPIDER, ItemInit.morphling_spider),
			morphling(MorphlingPolypLayer.TICK, ItemInit.morphling_tick),
			morphling(MorphlingPolypLayer.URCHIN, ItemInit.morphling_urchin)
	);
	private static final Set<ResourceLocation> ECOLOGY_SPECIMENS = RESEARCH_ENTRIES.stream()
			.map(ResearchEntry::id)
			.collect(Collectors.toUnmodifiableSet());
	private static final Map<MorphlingPolypLayer, DeferredHolder<Item, Item>> LAYER_MORPHLINGS = MORPHLING_ENTRIES.stream()
			.collect(Collectors.toUnmodifiableMap(MorphlingEntry::layer, MorphlingEntry::morphling));

	private SpecimenBestiaryDefinitions() {
	}

	public static List<ResearchEntry> orderedResearchEntries() {
		return RESEARCH_ENTRIES;
	}

	public static List<MorphlingEntry> orderedMorphlingEntries() {
		return MORPHLING_ENTRIES;
	}

	public static boolean isResearchSpecimen(ResourceLocation id) {
		return id != null && ECOLOGY_SPECIMENS.contains(id);
	}

	public static int totalResearchSpecimens() {
		return ECOLOGY_SPECIMENS.size();
	}

	public static ItemStack createSurrenderReward(ResourceLocation id) {
		if (!isResearchSpecimen(id)) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(ItemInit.enzyme_primer.get());
	}

	public static String recordedDialogueKey(ResourceLocation id) {
		if (Hemomancy.rloc("tooth_pecks").equals(id)) {
			return "hemomancy.dialogue.event.alchemist_bestiary_recorded.tooth_pecks";
		}
		return "hemomancy.dialogue.event.alchemist_bestiary_recorded";
	}

	public static String surrenderedDialogueKey(ResourceLocation id) {
		if (Hemomancy.rloc("tooth_pecks").equals(id)) {
			return "hemomancy.dialogue.event.alchemist_bestiary_surrendered.tooth_pecks";
		}
		return "hemomancy.dialogue.event.alchemist_bestiary_surrendered";
	}

	public static ItemStack createWildBoundMorphling(MorphlingPolypLayer layer) {
		DeferredHolder<Item, Item> item = LAYER_MORPHLINGS.get(layer);
		if (item == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = new ItemStack(item.get());
		MorphlingItem.setWildBound(stack);
		return stack;
	}

	private static ResearchEntry specimen(String path) {
		return new ResearchEntry(Hemomancy.rloc(path),
				"bestiary.hemomancy.specimen." + path + ".title",
				"bestiary.hemomancy.specimen." + path + ".description",
				"bestiary.hemomancy.specimen." + path + ".source");
	}

	private static MorphlingEntry morphling(MorphlingPolypLayer layer, DeferredHolder<Item, Item> item) {
		String id = layer.serializedName();
		return new MorphlingEntry(layer, item,
				"bestiary.hemomancy.morphling." + id + ".title",
				"bestiary.hemomancy.morphling." + id + ".description",
				"bestiary.hemomancy.morphling." + id + ".source");
	}

	public record ResearchEntry(ResourceLocation id, String titleKey, String descriptionKey, String sourceKey) {
	}

	public record MorphlingEntry(MorphlingPolypLayer layer, DeferredHolder<Item, Item> morphling,
			String titleKey, String descriptionKey, String sourceKey) {
	}
}
