package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter;
import com.vincenthuto.hutoslib.common.book.knowledge.BookEntryRegistry;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.Set;

/**
 * Thin bridging handlers that read from HutosLib's {@link BookEntryRegistry}
 * and write the resulting unlocks into Hemomancy's own {@code IBookKnowledge}
 * capability via {@link LiberKnowledgeHelper}.
 *
 * <p>HutosLib's {@code BookDiscoveryEvents} mirrors the same events and writes
 * into HutosLib's own {@code BookKnowledge} attachment (used by HutosLib's
 * generic guide). Because Hemomancy stores knowledge in its own capability
 * ({@code HemoAttachmentTypes.LIBER_KNOWLEDGE}), it must maintain its own
 * listeners — but registration of <em>what unlocks what</em> is now shared
 * through {@code BookEntryRegistry} rather than Hemomancy's own internal maps.
 *
 * <p>Item and advancement → entry mappings are registered during this class's
 * {@code FMLCommonSetupEvent} listener.
 *
 * <p>Rite-based and dialogue-based unlocks are not event-driven and are
 * triggered programmatically from {@link LiberKnowledgeHelper} directly.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LiberDiscoveryEvents {
	private LiberDiscoveryEvents() {
	}

	/**
	 * Registers all Hemomancy item and advancement → Liber Sanguinum entry
	 * unlock mappings with HutosLib's {@link BookEntryRegistry}. This shared
	 * registry is used by HutosLib's own discovery events and Hemomancy's bridge
	 * handlers below.
	 *
	 * <p>Rite-based and dialogue-based unlocks remain in
	 * {@link LiberEntryDefinitions} and are triggered programmatically from
	 * {@link LiberKnowledgeHelper} directly.
	 */
	public static void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(LiberDiscoveryEvents::initLiberBookUnlocks);
	}

	private static void initLiberBookUnlocks() {
		// ── Item pickup → page unlocks ────────────────────────────────────────
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_stained_stone"),  LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bloody_vial"),           LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_crystal_shard"),   LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_rock"),            LiberEntryDefinitions.HEMOMANCY);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bleeding_bulb"),         LiberEntryDefinitions.ERYTHROMYCELIUM);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("dicentra_sap"),          LiberEntryDefinitions.ERYTHROMYCELIUM);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("talaromyces_minus"),     LiberEntryDefinitions.ERYTHROMYCELIUM);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_gourd_red"),       LiberEntryDefinitions.ERYTHROMYCELIUM);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_tendency_gauge"),  LiberEntryDefinitions.THE_HARBINGERS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("barbed_blade"),          LiberEntryDefinitions.THE_HARBINGERS);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fungal_spine"),          LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("spore_sac"),             LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("chitinous_husk"),        LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("desiccated_membrane"),   LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fervent_husk"),          LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("foul_paste"),            LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("serpent_scale"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("ferric_spores"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivacious_spores"),      LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fervent_spores"),        LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("frigid_spores"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("umbral_spores"),         LiberEntryDefinitions.HYPHAE);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("sanguine_formation"),         LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_memory"),             LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("engram_stamp"),               LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("unsigned_ancestral_ledger"),  LiberEntryDefinitions.BLOOD_MEMORIES);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("abyssal_ichor"),  LiberEntryDefinitions.ENTITY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("void_ichor"),     LiberEntryDefinitions.ENTITY);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("qliphoth_seed"),  LiberEntryDefinitions.QLIPHOTH);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("qliphoth_pome"),  LiberEntryDefinitions.QLIPHOTH);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("silthmere_glaive"),            LiberEntryDefinitions.SAINTS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_hemorath"),  LiberEntryDefinitions.HEMORATH);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_putriciel"), LiberEntryDefinitions.SAINTS);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_copper_ingot"),  LiberEntryDefinitions.COPPER_AND_SILVER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("silver_chalice"),            LiberEntryDefinitions.COPPER_AND_SILVER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivianite_cluster"),         LiberEntryDefinitions.COPPER_AND_SILVER);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hemolytic_solution"),  LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivianite_scalpel"),   LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("absolution_dagger"),   LiberEntryDefinitions.THE_UNSTAINED);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("cleansed_blood_crystal_shard"),  LiberEntryDefinitions.PURIFIED);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hemolytic_solution"),     LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_syringe"),    LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("cleansing_hemolymph"),    LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_copper_ingot"),  LiberEntryDefinitions.IMMACULATUS_COPPER_AND_SILVER);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("tears_of_silthmere"),  LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS);

		// ── Advancement → page unlocks ────────────────────────────────────────
		// Harbinger degree chain
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE,  LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE,  LiberEntryDefinitions.THE_HARBINGERS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY,    LiberEntryDefinitions.DEGREES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE,  LiberEntryDefinitions.ORDER_BELIEFS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT,     LiberEntryDefinitions.HISTORICAL_RECORD);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS, LiberEntryDefinitions.HERMITS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED, LiberEntryDefinitions.DEGREES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON,    LiberEntryDefinitions.ENTITY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS,  LiberEntryDefinitions.TRUTH);
		// Harbinger milestone advancements
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND,                LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED,     LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FOUNDING_SANCTUM_ESTABLISHED,  LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN,            LiberEntryDefinitions.ENTITY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED,       LiberEntryDefinitions.TRUTH);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SANGUINE_DOMAIN,               LiberEntryDefinitions.BLOOD_MEMORIES);
		// Unstained purity stages
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_BLESSED_BY_ALTAR,  LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_TAINTED,           LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLEANSING,         LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_ABSOLVED,          LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED,  LiberEntryDefinitions.PURIFIED);
		// Unstained clarity stages
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_DISCERNING,        LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_VIGILANT,          LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_RESOLUTE_STAGE,    LiberEntryDefinitions.PURIFIED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_ENLIGHTENED_SEEKER, LiberEntryDefinitions.PURIFIED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_PURIFIED,          LiberEntryDefinitions.PURIFIED);
	}

	@SubscribeEvent
	public static void onItemPickup(ItemEntityPickupEvent.Post event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		if (event.getOriginalStack().isEmpty()) return;
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(event.getOriginalStack().getItem());
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForItem(itemId);
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ITEM_PICKUP);
		}
	}

	@SubscribeEvent
	public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForAdvancement(event.getAdvancement().id());
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ADVANCEMENT);
		}
	}
}

