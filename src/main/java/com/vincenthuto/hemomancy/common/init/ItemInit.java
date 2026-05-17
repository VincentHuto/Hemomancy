package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoBookFilter;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.HematicIronArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.MarrowCrownArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.BloodlinePoolMonitorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.*;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.*;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.*;
import com.vincenthuto.hemomancy.common.item.shared.*;
import com.vincenthuto.hemomancy.common.item.shared.armor.*;
import com.vincenthuto.hemomancy.common.item.unstained.*;
import com.vincenthuto.hemomancy.common.item.unstained.armor.UnstainedArmorItem;
import com.vincenthuto.hemomancy.common.item.unstained.armor.UnstainedShieldItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.PaleSilverPickaxeItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ItemInit {
    public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(Registries.ITEM,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<BannerPattern> BANNERPATTERNS = DeferredRegister
            .create(Registries.BANNER_PATTERN, Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> HANDHELDITEMS = DeferredRegister.create(Registries.ITEM,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> SPECIALITEMS = DeferredRegister.create(Registries.ITEM,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> SPAWNEGGS = DeferredRegister.create(Registries.ITEM,
            Hemomancy.MOD_ID);
    public static final DeferredHolder<Item, Item> chitinite_arm_banner = SPECIALITEMS.register("chitinite_arm_banner",
            () -> new ItemArmBanner(new Item.Properties(), EnumModArmorTiers.CHITINITE.holder(),
                    Hemomancy.rloc("textures/entity/arm_banner/chitinite_arm_banner.png")));

    // public static final DamageSource bloodLoss = new DamageSource("bloodloss");
    public static final DeferredHolder<BannerPattern, BannerPattern> heart = BANNERPATTERNS.register("hemomancy_heart",
            () -> new BannerPattern(Hemomancy.rloc("hemomancy_heart"), "hemomancy_heart"));
    public static final DeferredHolder<Item, Item> heart_pattern = BASEITEMS.register("heart_pattern",
            () -> new BannerPatternItem(
                    TagKey.create(Registries.BANNER_PATTERN, Hemomancy.rloc("pattern_item/hemomancy_heart")),
                    new Item.Properties()));
    public static final DeferredHolder<BannerPattern, BannerPattern> veins = BANNERPATTERNS.register("hemomancy_veins",
            () -> new BannerPattern(Hemomancy.rloc("hemomancy_veins"), "hemomancy_veins"));
    public static final DeferredHolder<Item, Item> veins_pattern = BASEITEMS.register("veins_pattern",
            () -> new BannerPatternItem(
                    TagKey.create(Registries.BANNER_PATTERN, Hemomancy.rloc("pattern_item/hemomancy_veins")),
                    new Item.Properties()));
    // Charm
    public static final DeferredHolder<Item, Item> charm_of_vascularium = BASEITEMS.register("charm_of_vascularium",
            () -> new VasculariumCharmItem(new Item.Properties(), EnumBloodTendency.ANIMUS, 0));
    // Debug / Testing
    public static final DeferredHolder<Item, Item> structure_spawner = SPECIALITEMS.register("structure_spawner",
            () -> new StructureSpawnerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> debug_showcase = SPECIALITEMS.register("debug_showcase",
            () -> new DebugShowcaseItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> structure_scanner = SPECIALITEMS.register("structure_scanner",
            () -> new StructureScannerItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(
                            DataComponentInit.STRUCTURE_SCANNER_TOOLTIP.get(),
                            new StructureScannerTooltipComponent(List.of(
                                    Component.literal("Right-click two corners of a structure").withStyle(ChatFormatting.GRAY),
                                    Component.literal("to export it as recipe JSON.").withStyle(ChatFormatting.GRAY),
                                    Component.literal("Sneak+click to reset stored corner.").withStyle(ChatFormatting.DARK_GRAY),
                                    Component.literal("Creative mode only!").withStyle(ChatFormatting.RED))))));

    public static final DeferredHolder<Item, Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
            () -> new BloodyBookItem(new Item.Properties().stacksTo(1),
                    Hemomancy.rloc("textures/entity/liber_sanguinum.png"))
                    .withBookPrefix("sanctumsanguinium/")
                    .withPageFilter(new MemoBookFilter())
                    .withKnowledgeProvider(player -> HemoCapabilityAccess.getLiberKnowledge(player)));

    // Book
    public static final DeferredHolder<Item, Item> unsigned_ancestral_ledger = BASEITEMS.register("unsigned_ancestral_ledger",
            () -> new UnsignedLedgerItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> liber_immaculatus = SPECIALITEMS.register("liber_immaculatus",
            () -> new UnstainedBookItem(new Item.Properties().stacksTo(1),
                    Hemomancy.rloc("textures/entity/liber_immaculatus.png"))
                    .withBookPrefix("liberimmaculatus/")
                    .withPageFilter(new MemoBookFilter())
                    .withKnowledgeProvider(player -> HemoCapabilityAccess.getLiberKnowledge(player)));

    public static final DeferredHolder<Item, Item> field_notes = SPECIALITEMS.register("field_notes",
            () -> new FieldNotesBookItem(new Item.Properties().stacksTo(1),
                    Hemomancy.rloc("textures/entity/field_notes.png")));

    public static final DeferredHolder<Item, Item> scout_field_notes = SPECIALITEMS.register("scout_field_notes",
            () -> new PreWrittenMemoItem(new Item.Properties()));
    // Base Items
    public static final DeferredHolder<Item, Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
            () -> new Item(new Item.Properties()));
//    public static final DeferredHolder<Item, Item> field_notes = BASEITEMS.register("field_notes",
//            () -> new FieldNotesItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> sanguine_blob = BASEITEMS.register("sanguine_blob",
            () -> new SanguineBlobItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> rite_hint = BASEITEMS.register("rite_hint",
            () -> new RiteHintItem(new Item.Properties().fireResistant()));
    public static final DeferredHolder<Item, Item> blood_structure_hint = BASEITEMS.register("blood_structure_hint",
            () -> new BloodStructureHintItem(new Item.Properties().fireResistant()));
    public static final DeferredHolder<Item, Item> hematic_suture_needle = BASEITEMS.register("hematic_suture_needle",
            () -> new HematicSutureNeedleItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> stained_church_map = BASEITEMS.register("stained_church_map",
            () -> new StainedChurchMapItem(new Item.Properties().fireResistant()));
    public static final DeferredHolder<Item, Item> hematic_field_ink = BASEITEMS.register("hematic_field_ink",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> pale_field_ink = BASEITEMS.register("pale_field_ink",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> foul_paste = BASEITEMS.register("foul_paste",
            () -> new FoulPasteItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> active_smouldering_ash = SPECIALITEMS.register("active_smouldering_ash",
            () -> new ItemNameBlockItem(BlockInit.active_smouldering_ash_trail.get(), (new Item.Properties())));
    public static final DeferredHolder<Item, Item> active_befouling_ash = SPECIALITEMS.register("active_befouling_ash",
            () -> new ItemNameBlockItem(BlockInit.active_befouling_ash_trail.get(), (new Item.Properties())));
    public static final DeferredHolder<Item, Item> hematic_iron_scrap = BASEITEMS.register("hematic_iron_scrap",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> chalybeate_sclerite = BASEITEMS.register("chalybeate_sclerite",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> erythrocoral_fragment = BASEITEMS.register("erythrocoral_fragment",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> mnemonic_ambergris = BASEITEMS.register("mnemonic_ambergris",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> salt_stained_voyager_log = BASEITEMS.register("salt_stained_voyager_log",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> consecrated_copper_ingot = BASEITEMS.register("consecrated_copper_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> hematic_iron_powder = BASEITEMS.register("hematic_iron_powder",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> serpent_scale = BASEITEMS.register("serpent_scale",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> swollen_leech = BASEITEMS.register("swollen_leech",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> dried_leech = BASEITEMS.register("dried_leech",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> chitinous_husk = BASEITEMS.register("chitinous_husk",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> sanguine_conduit = BASEITEMS.register("sanguine_conduit",
            () -> new ItemSanguineConduit(BlockInit.sanguine_conduit.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> scrying_dish = BASEITEMS.register("scrying_dish",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> vitality_chalice = BASEITEMS.register("vitality_chalice",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> blood_stained_stone = BASEITEMS.register("blood_stained_stone",
            () -> new BloodStainedStoneItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> sanguine_salve = BASEITEMS.register("sanguine_salve",
            () -> new SanguineSalveItem(new Item.Properties(), 25f));
    public static final DeferredHolder<Item, Item> bleeding_bulb = BASEITEMS.register("bleeding_bulb",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> dicentra_sap = BASEITEMS.register("dicentra_sap",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> puppeteering_thread = BASEITEMS.register("puppeteering_thread",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> blood_crystal_shard = BASEITEMS.register("blood_crystal_shard",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> sanguine_quintessence = BASEITEMS.register("sanguine_quintessence",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> cleansed_blood_crystal_shard = BASEITEMS.register("cleansed_blood_crystal_shard",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> cleansing_hemolymph = SPECIALITEMS.register("cleansing_hemolymph",
            () -> new CleansingHemolymphItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> vivianite_cluster = BASEITEMS.register("vivianite_cluster",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> vivianite_scalpel = BASEITEMS.register("vivianite_scalpel",
            () -> new VivianiteScalpelItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> spore_sac = BASEITEMS.register("spore_sac",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> desiccated_membrane = BASEITEMS.register("desiccated_membrane",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> molten_scab = BASEITEMS.register("molten_scab",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> void_ichor = BASEITEMS.register("void_ichor",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> frozen_clot = BASEITEMS.register("frozen_clot",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> abyssal_ichor = BASEITEMS.register("abyssal_ichor",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> nerve_bundle = BASEITEMS.register("nerve_bundle",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> fungal_spine = BASEITEMS.register("fungal_spine",
            () -> new FungalSpineItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> gourd_seeds = BASEITEMS.register("gourd_seeds",
            () -> new ItemNameBlockItem(BlockInit.gourd_stem.get(), new Item.Properties()));
    // Gourd Foods
    public static final DeferredHolder<Item, Item> gourd_slice = BASEITEMS.register("gourd_slice",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).fast().build())));
    public static final DeferredHolder<Item, Item> roasted_gourd_seeds = BASEITEMS.register("roasted_gourd_seeds",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.2F).fast().build())));
    public static final DeferredHolder<Item, Item> gourd_stew = BASEITEMS.register("gourd_stew",
            () -> new Item(new Item.Properties().stacksTo(1).craftRemainder(Items.BOWL)
                    .food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.6F).build())));
    // Qliphoth Reagent
    public static final DeferredHolder<Item, Item> qliphoth_seed = BASEITEMS.register("qliphoth_seed",
            () -> new QliphothSeedItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> qliphoth_pome = BASEITEMS.register("qliphoth_pome",
            () -> new QliphothPomeItem(new Item.Properties().rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8F).alwaysEdible().build())));
    // Anti blood
    public static final DeferredHolder<Item, Item> hemolytic_solution = BASEITEMS.register("hemolytic_solution",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> hemolytic_plating = BASEITEMS.register("hemolytic_plating",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> neutralizing_gasket = BASEITEMS.register("neutralizing_gasket",
            () -> new Item(new Item.Properties()));
    // Enzymes
    public static final DeferredHolder<Item, Item> vivacious_enzyme = BASEITEMS.register("vivacious_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.ANIMUS, 10));
    public static final DeferredHolder<Item, Item> ruinous_enzyme = BASEITEMS.register("ruinous_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.MORTEM, 10));
    public static final DeferredHolder<Item, Item> neurotic_enzyme = BASEITEMS.register("neurotic_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.DUCTILIS, 10));
    public static final DeferredHolder<Item, Item> ferric_enzyme = BASEITEMS.register("ferric_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.FERRIC, 10));
    public static final DeferredHolder<Item, Item> fervent_enzyme = BASEITEMS.register("fervent_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.FLAMMEUS, 10));
    public static final DeferredHolder<Item, Item> frigid_enzyme = BASEITEMS.register("frigid_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.CONGEATIO, 10));
    public static final DeferredHolder<Item, Item> incandescent_enzyme = BASEITEMS.register("incandescent_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.LUX, 10));
    public static final DeferredHolder<Item, Item> umbral_enzyme = BASEITEMS.register("umbral_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.TENEBRIS, 10));
    public static final DeferredHolder<Item, Item> recycled_enzyme = BASEITEMS.register("recycled_enzyme",
            () -> new RecycledEnzymeItem());
    // Hallowed Residuum â€" Saint-Enzymes (hybridized, extracted from Preserved Corpus)
    public static final DeferredHolder<Item, Item> hallowed_residuum_hemorath = BASEITEMS.register("hallowed_residuum_hemorath",
            () -> new HallowedResiduumItem(EnumSaintType.HEMORATH, 20));
    public static final DeferredHolder<Item, Item> hallowed_residuum_seraphae = BASEITEMS.register("hallowed_residuum_seraphae",
            () -> new HallowedResiduumItem(EnumSaintType.SERAPHAE, 20));
    public static final DeferredHolder<Item, Item> hallowed_residuum_putriciel = BASEITEMS.register("hallowed_residuum_putriciel",
            () -> new HallowedResiduumItem(EnumSaintType.PUTRICIEL, 20));
    public static final DeferredHolder<Item, Item> hallowed_residuum_velorum = BASEITEMS.register("hallowed_residuum_velorum",
            () -> new HallowedResiduumItem(EnumSaintType.VELORUM, 20));
    // Consecrated Syringe â€" extraction tool for Saint Sarcophagus
    public static final DeferredHolder<Item, Item> consecrated_syringe = SPECIALITEMS.register("consecrated_syringe",
            () -> new ConsecratedSyringeItem(new Item.Properties().stacksTo(1)));
    // Blood Tendency Gauge
    public static final DeferredHolder<Item, Item> blood_tendency_gauge = BASEITEMS.register("blood_tendency_gauge",
            () -> new BloodTendencyGaugeItem(new Item.Properties().stacksTo(1)));
    // Vascular Status Gauge
    public static final DeferredHolder<Item, Item> vascular_status_gauge = BASEITEMS.register("vascular_status_gauge",
            () -> new VascularStatusGaugeItem(new Item.Properties().stacksTo(1)));
    // Bloodline Pool Monitor
    public static final DeferredHolder<Item, Item> bloodline_pool_monitor = BASEITEMS.register("bloodline_pool_monitor",
            () -> new BloodlinePoolMonitorItem(new Item.Properties().stacksTo(1)));
    // Unstained â€" Our Lady of Still Waters materials
    public static final DeferredHolder<Item, Item> draught_of_still_waters = BASEITEMS.register("draught_of_still_waters",
            () -> new DraughtOfStillWatersItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> draught_of_still_mercy = BASEITEMS.register("draught_of_still_mercy",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> pallid_infusion = BASEITEMS.register("pallid_infusion",
            () -> new PallidInfusionItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> lethean_dew = BASEITEMS.register("lethean_dew",
            () -> new LetheanDewItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> lethean_brew = BASEITEMS.register("lethean_brew",
            () -> new LetheanBrewItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> tears_of_silthmere = BASEITEMS.register("tears_of_silthmere",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> lethean_poppy_wreath = BASEITEMS.register("lethean_poppy_wreath",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, Item> silver_chalice = BASEITEMS.register("silver_chalice",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> tome_of_the_unstained = BASEITEMS.register("tome_of_the_unstained",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> pallid_icon = BASEITEMS.register("pallid_icon",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> pale_silver_ingot = BASEITEMS.register("pale_silver_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> pale_distillate = BASEITEMS.register("pale_distillate",
            () -> new Item(new Item.Properties().stacksTo(16)));
    // Hematic Memories
    public static final DeferredHolder<Item, Item> drudge_electrode = BASEITEMS.register("drudge_electrode",
            () -> new DrudgeElectrodeItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> marionette_crossbar = SPECIALITEMS.register("marionette_crossbar",
            () -> new MarionetteCrossbarItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> drudge_submission_device = BASEITEMS.register("drudge_submission_device",
            () -> new DrudgeSubmissionDeviceItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> fervent_husk = BASEITEMS.register("fervent_husk",
            () -> new FerventHuskItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> hematic_memory = BASEITEMS.register("hematic_memory",
            () -> new HematicMemoryItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> memory_blood_absorption = BASEITEMS.register("memory_blood_absorption",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_absorption));
    public static final DeferredHolder<Item, Item> memory_blood_projection = BASEITEMS.register("memory_blood_projection",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_projection));
    public static final DeferredHolder<Item, Item> memory_venous_travel = BASEITEMS.register("memory_venous_travel",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.venous_travel));
    public static final DeferredHolder<Item, Item> memory_blood_shot = BASEITEMS.register("memory_blood_shot",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_shot));
    public static final DeferredHolder<Item, Item> memory_blood_aneurysm = BASEITEMS.register("memory_blood_aneurysm",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_aneurysm));
    public static final DeferredHolder<Item, Item> memory_blood_rush = BASEITEMS.register("memory_blood_rush",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_rush));
    public static final DeferredHolder<Item, Item> memory_deadly_gaze = BASEITEMS.register("memory_deadly_gaze",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.deadly_gaze));
    public static final DeferredHolder<Item, Item> memory_blood_needle = BASEITEMS.register("memory_blood_needle",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_needle));
    public static final DeferredHolder<Item, Item> memory_blood_cloud = BASEITEMS.register("memory_blood_cloud",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_cloud));
    public static final DeferredHolder<Item, Item> memory_activation_potential = BASEITEMS.register(
            "memory_activation_potential",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.activation_potential));
    public static final DeferredHolder<Item, Item> memory_sanguine_ward = BASEITEMS.register("memory_sanguine_ward",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_ward));
    public static final DeferredHolder<Item, Item> memory_hemolymphal_pulse = BASEITEMS.register("memory_hemolymphal_pulse",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.hemolymphal_pulse));
    public static final DeferredHolder<Item, Item> memory_vital_effusion = BASEITEMS.register("memory_vital_effusion",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vital_effusion));
    public static final DeferredHolder<Item, Item> memory_living_blade = BASEITEMS.register("memory_living_blade",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.conjure_blade));
    public static final DeferredHolder<Item, Item> memory_summon_avatar = BASEITEMS.register("memory_summon_avatar",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.summon_avatar));
    public static final DeferredHolder<Item, Item> memory_summon_thrall = BASEITEMS.register("memory_summon_thrall",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.summon_thrall));
    public static final DeferredHolder<Item, Item> memory_ferric_transmutation = BASEITEMS.register(
            "memory_ferric_transmutation",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.ferric_transmutation));
    public static final DeferredHolder<Item, Item> memory_crimson_flame_conjuration = BASEITEMS.register(
            "memory_crimson_flame_conjuration",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_flame_conjuration));
    // Crude Memory Shards — pre-Somatic-Loom HUMILIS teaching items
    public static final DeferredHolder<Item, Item> crude_memory_blood_shot = BASEITEMS.register(
            "crude_memory_blood_shot",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.blood_shot));
    public static final DeferredHolder<Item, Item> crude_memory_blood_rush = BASEITEMS.register(
            "crude_memory_blood_rush",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.blood_rush));
    public static final DeferredHolder<Item, Item> crude_memory_deadly_gaze = BASEITEMS.register(
            "crude_memory_deadly_gaze",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.deadly_gaze));
    public static final DeferredHolder<Item, Item> crude_memory_crimson_harvest = BASEITEMS.register(
            "crude_memory_crimson_harvest",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.crimson_harvest));
    public static final DeferredHolder<Item, Item> crude_memory_sanguine_mending = BASEITEMS.register(
            "crude_memory_sanguine_mending",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.sanguine_mending));
    public static final DeferredHolder<Item, Item> crude_memory_blood_lamp = BASEITEMS.register(
            "crude_memory_blood_lamp",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.blood_lamp));
    public static final DeferredHolder<Item, Item> crude_memory_hemorrhage = BASEITEMS.register(
            "crude_memory_hemorrhage",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.hemorrhage));
    public static final DeferredHolder<Item, Item> crude_memory_glacial_grasp = BASEITEMS.register(
            "crude_memory_glacial_grasp",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.glacial_grasp));
    public static final DeferredHolder<Item, Item> crude_memory_sanguine_ignition = BASEITEMS.register(
            "crude_memory_sanguine_ignition",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.sanguine_ignition));
    public static final DeferredHolder<Item, Item> crude_memory_void_shroud = BASEITEMS.register(
            "crude_memory_void_shroud",
            () -> new CrudeMemoryShardItem(new Item.Properties(), ManipulationInit.void_shroud));
    // Organ Echoes (Visceral Mirror system)
    public static final DeferredHolder<Item, Item> echo_of_spleen = SPECIALITEMS.register("echo_of_spleen",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.SPLEEN));
    public static final DeferredHolder<Item, Item> echo_of_liver = SPECIALITEMS.register("echo_of_liver",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.LIVER));
    public static final DeferredHolder<Item, Item> echo_of_lungs = SPECIALITEMS.register("echo_of_lungs",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.LUNGS));
    public static final DeferredHolder<Item, Item> echo_of_kidneys = SPECIALITEMS.register("echo_of_kidneys",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.KIDNEYS));
    public static final DeferredHolder<Item, Item> echo_of_heart = SPECIALITEMS.register("echo_of_heart",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.HEART));
    public static final DeferredHolder<Item, Item> memory_sanguine_mending = BASEITEMS.register("memory_sanguine_mending",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_mending));
    public static final DeferredHolder<Item, Item> memory_hemosynthesis = BASEITEMS.register("memory_hemosynthesis",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.hemosynthesis));
    public static final DeferredHolder<Item, Item> memory_blood_lamp = BASEITEMS.register("memory_blood_lamp",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_lamp));
    public static final DeferredHolder<Item, Item> memory_crimson_harvest = BASEITEMS.register("memory_crimson_harvest",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_harvest));
    public static final DeferredHolder<Item, Item> memory_glacial_grasp = BASEITEMS.register("memory_glacial_grasp",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.glacial_grasp));
    public static final DeferredHolder<Item, Item> memory_sanguine_excavation = BASEITEMS.register(
            "memory_sanguine_excavation",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_excavation));
    public static final DeferredHolder<Item, Item> memory_vascular_dowsing = BASEITEMS.register("memory_vascular_dowsing",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vascular_dowsing));
    public static final DeferredHolder<Item, Item> memory_ferric_resonance = BASEITEMS.register("memory_ferric_resonance",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.ferric_resonance));
    public static final DeferredHolder<Item, Item> memory_pyretic_forge = BASEITEMS.register("memory_pyretic_forge",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.pyretic_forge));
    public static final DeferredHolder<Item, Item> memory_umbral_step = BASEITEMS.register("memory_umbral_step",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.umbral_step));
    public static final DeferredHolder<Item, Item> memory_crimson_sight = BASEITEMS.register("memory_crimson_sight",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_sight));
    public static final DeferredHolder<Item, Item> memory_vital_reservoir = BASEITEMS.register("memory_vital_reservoir",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vital_reservoir));
    // â"€â"€ Expanded tendency memories â"€â"€
    public static final DeferredHolder<Item, Item> memory_cryogenic_pulse = BASEITEMS.register("memory_cryogenic_pulse",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.cryogenic_pulse));
    public static final DeferredHolder<Item, Item> memory_glacial_bastion = BASEITEMS.register("memory_glacial_bastion",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.glacial_bastion));
    public static final DeferredHolder<Item, Item> memory_glacial_circulation = BASEITEMS.register("memory_glacial_circulation",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.glacial_circulation));
    public static final DeferredHolder<Item, Item> memory_osseous_bloom = BASEITEMS.register("memory_osseous_bloom",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.osseous_bloom));
    public static final DeferredHolder<Item, Item> memory_sanguine_ignition = BASEITEMS.register("memory_sanguine_ignition",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_ignition));
    public static final DeferredHolder<Item, Item> memory_vitric_combustion = BASEITEMS.register("memory_vitric_combustion",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vitric_combustion));
    public static final DeferredHolder<Item, Item> memory_void_shroud = BASEITEMS.register("memory_void_shroud",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.void_shroud));
    public static final DeferredHolder<Item, Item> memory_blood_eclipse = BASEITEMS.register("memory_blood_eclipse",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_eclipse));
    public static final DeferredHolder<Item, Item> memory_hemorrhage = BASEITEMS.register("memory_hemorrhage",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.hemorrhage));
    public static final DeferredHolder<Item, Item> memory_exsanguinate = BASEITEMS.register("memory_exsanguinate",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.exsanguinate));
    // Saint Canon Memories â€" imprinted from Sainted Mausoleums via Hallowed Residuum
    public static final DeferredHolder<Item, Item> memory_crimson_tithe = BASEITEMS.register("memory_crimson_tithe",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_tithe));
    public static final DeferredHolder<Item, Item> memory_unclosing_eye = BASEITEMS.register("memory_unclosing_eye",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.unclosing_eye));
    public static final DeferredHolder<Item, Item> memory_bloom_of_rot = BASEITEMS.register("memory_bloom_of_rot",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.bloom_of_rot));
    public static final DeferredHolder<Item, Item> memory_endless_hour = BASEITEMS.register("memory_endless_hour",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.endless_hour));
    // Living
    public static final DeferredHolder<Item, Item> blood_absorption = SPECIALITEMS.register("blood_absorption",
            () -> new BloodAbsorptionItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> blood_projection = SPECIALITEMS.register("blood_projection",
            () -> new BloodProjectionItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> sanguis_lancea = SPECIALITEMS.register("sanguis_lancea",
            () -> new SanguisLanceaItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> annettas_sanguis_lancea = SPECIALITEMS.register("annettas_sanguis_lancea",
            () -> new SanguisLanceaItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> living_syringe = SPECIALITEMS.register("living_syringe",
            () -> new LivingSyringeItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_staff = SPECIALITEMS.register("living_staff",
            () -> new LivingStaffItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_blade = SPECIALITEMS.register("living_blade",
            () -> new LivingBladeItem(25f, 3, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_axe = SPECIALITEMS.register("living_axe",
            () -> new LivingAxeItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_spear = SPECIALITEMS.register("living_spear",
            () -> new LivingSpearItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_baghnakh = SPECIALITEMS.register("living_baghnakh",
            () -> new LivingBaghnakhItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> living_crossbow = SPECIALITEMS.register("living_crossbow",
            () -> new LivingCrossbowItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> blood_bolt = BASEITEMS.register("blood_bolt",
            () -> new BloodBoltItem(new Item.Properties()));
    // Blood Thrall â€" creature-based blood transport
    public static final DeferredHolder<Item, Item> blood_thrall_effigy = BASEITEMS.register("blood_thrall_effigy",
            () -> new BloodThrallItem(new Item.Properties().stacksTo(16)));
    // Morphlings
    public static final DeferredHolder<Item, Item> morphling_polyp = BASEITEMS.register("morphling_polyp",
            () -> new ItemMorphlingPolyp(new Item.Properties()));
    public static final DeferredHolder<Item, Item> morphling_jar = SPECIALITEMS.register("morphling_jar",
            () -> new ItemMorphlingJar("morphling_jar", 6, Rarity.UNCOMMON));
    public static final DeferredHolder<Item, Item> morphling_fungal = BASEITEMS.register("morphling_fungal",
            () -> new FungalMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_leeches = BASEITEMS.register("morphling_leeches",
            () -> new LeechesMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_chitinite = BASEITEMS.register("morphling_chitinite",
            () -> new ChitiniteMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_serpent = BASEITEMS.register("morphling_serpent",
            () -> new SerpentMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_pests = BASEITEMS.register("morphling_pests",
            () -> new PestsMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_spider = BASEITEMS.register("morphling_spider",
            () -> new SpiderMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_moth = BASEITEMS.register("morphling_moth",
            () -> new MothMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_tick = BASEITEMS.register("morphling_tick",
            () -> new TickMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_centipede = BASEITEMS.register("morphling_centipede",
            () -> new CentipedeMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_bat = BASEITEMS.register("morphling_bat",
            () -> new BatMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_urchin = BASEITEMS.register("morphling_urchin",
            () -> new UrchinMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> morphling_mole = BASEITEMS.register("morphling_mole",
            () -> new MoleMorphlingItem(new Item.Properties().stacksTo(1)));
    /// Blood Gourds
    public static final DeferredHolder<Item, Item> dried_gourd = BASEITEMS.register("dried_gourd",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.SIMPLE));
    public static final DeferredHolder<Item, Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.CRIMSON));
    public static final DeferredHolder<Item, Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.ASHEN));
    public static final DeferredHolder<Item, Item> curved_horn = SPECIALITEMS.register("curved_horn",
            () -> new CurvedHornItem(EnumBloodGourdTiers.HORN));
    public static final DeferredHolder<Item, Item> hemorath_rib = SPECIALITEMS.register("hemorath_rib",
            () -> new HemorathRibItem(EnumBloodGourdTiers.RIB));
    // Flasks
    public static final DeferredHolder<Item, Item> blood_rock = BASEITEMS
            .register("blood_rock", () -> new BloodyFlaskItem(new Item.Properties(), 250));
    public static final DeferredHolder<Item, Item> bloody_flask = BASEITEMS.register("bloody_flask",
            () -> new BloodyFlaskItem(new Item.Properties(), 2500));
    public static final DeferredHolder<Item, Item> bloody_jug = BASEITEMS.register("bloody_jug",
            () -> new BloodyFlaskItem(new Item.Properties(), 5000));
    public static final DeferredHolder<Item, Item> pale_humor_flask = BASEITEMS.register("pale_humor_flask",
            () -> new PaleHumorFlaskItem(new Item.Properties(), 500));
    public static final DeferredHolder<Item, Item> hemolytic_vial = BASEITEMS.register("hemolytic_vial",
            () -> new HemolyticVialItem(new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, Item> bloody_vial = SPECIALITEMS.register("bloody_vial",
            () -> new BloodVialItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> vial_rack = SPECIALITEMS.register("vial_rack",
            () -> new VialRackItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> engram_stamp = SPECIALITEMS.register("engram_stamp",
            () -> new EngramStampItem(new Item.Properties().stacksTo(1)));

    // Equipment
    // Artifacts
    public static final DeferredHolder<Item, Item> marrow_crown = BASEITEMS.register("marrow_crown",
            () -> new MarrowCrownArmorItem(EnumModArmorTiers.MARROW_CROWN.holder(), ArmorItem.Type.HELMET)
    );
    public static final DeferredHolder<Item, Item> hemolymphopoda_headpiece = BASEITEMS.register("hemolymphopoda_headpiece",
            () -> new HemolymphopodaHeadpieceArmorItem(EnumModArmorTiers.CHITINITE.holder(), ArmorItem.Type.HELMET));
    // Hematic
    public static final DeferredHolder<Item, Item> hematic_iron_helm = BASEITEMS.register("hematic_iron_helm",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON.holder(), ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> hematic_iron_chestplate = BASEITEMS.register("hematic_iron_chestplate",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON.holder(), ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> hematic_iron_leggings = BASEITEMS.register("hematic_iron_leggings",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON.holder(), ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> hematic_iron_boots = BASEITEMS.register("hematic_iron_boots",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON.holder(), ArmorItem.Type.BOOTS));
    public static final DeferredHolder<Item, Item> hematic_iron_sword = HANDHELDITEMS.register("hematic_iron_sword",
            () -> new SwordItem(EnumModToolTiers.HEMATIC_IRON, new Item.Properties()));
    public static final DeferredHolder<Item, Item> hematic_iron_knapper = HANDHELDITEMS.register("hematic_iron_knapper",
            () -> new ItemKnapper(42f, 1, 0, EnumModToolTiers.HEMATIC_IRON, new Item.Properties()));
    // Blood Lust
    // Masks
    public static final DeferredHolder<Item, Item> tengu_mask = BASEITEMS.register("tengu_mask",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> horned_mask = BASEITEMS.register("horned_mask",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> blood_lust_helm = BASEITEMS.register("blood_lust_helm",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.HELMET,
                    BloodLustArmorItem.MaskType.NONE));
    public static final DeferredHolder<Item, Item> blood_lust_helm_tengu = BASEITEMS.register("blood_lust_helm_tengu",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.HELMET,
                    BloodLustArmorItem.MaskType.TENGU));
    public static final DeferredHolder<Item, Item> blood_lust_helm_horned = BASEITEMS.register("blood_lust_helm_horned",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.HELMET,
                    BloodLustArmorItem.MaskType.HORNED));
    public static final DeferredHolder<Item, Item> blood_lust_chest = BASEITEMS.register("blood_lust_chest",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.CHESTPLATE,
                    BloodLustArmorItem.MaskType.NONE));
    public static final DeferredHolder<Item, Item> blood_lust_legs = BASEITEMS.register("blood_lust_legs",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.LEGGINGS,
                    BloodLustArmorItem.MaskType.NONE));
    public static final DeferredHolder<Item, Item> blood_lust_boots = BASEITEMS.register("blood_lust_boots",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST.holder(), ArmorItem.Type.BOOTS,
                    BloodLustArmorItem.MaskType.NONE));
    // Barbed/Barbed
    public static final DeferredHolder<Item, Item> barbed_blade = SPECIALITEMS.register("barbed_blade",
            () -> new SwordItem(EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> barbed_shield = SPECIALITEMS.register("barbed_shield",
            () -> new BarbedShieldItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> barbed_helm = BASEITEMS.register("barbed_helm",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED.holder(), ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> barbed_chestplate = BASEITEMS.register("barbed_chestplate",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED.holder(), ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> barbed_leggings = BASEITEMS.register("barbed_leggings",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED.holder(), ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> barbed_boots = BASEITEMS.register("barbed_boots",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED.holder(), ArmorItem.Type.BOOTS));
    // Chitinite
    public static final DeferredHolder<Item, Item> chitinite_mace = SPECIALITEMS.register("chitinite_mace",
            () -> new SwordItem(EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> chitinite_shield = SPECIALITEMS.register("chitinite_shield",
            () -> new ChitiniteShieldItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> chitinite_helm = BASEITEMS.register("chitinite_helm",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE.holder(), ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> chitinite_chestplate = BASEITEMS.register("chitinite_chestplate",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE.holder(), ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> chitinite_leggings = BASEITEMS.register("chitinite_leggings",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE.holder(), ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> chitinite_boots = BASEITEMS.register("chitinite_boots",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE.holder(), ArmorItem.Type.BOOTS));
    // Unstained
    public static final DeferredHolder<Item, Item> unstained_helm = BASEITEMS.register("unstained_helm",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED.holder(), ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> unstained_chestplate = BASEITEMS.register("unstained_chestplate",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED.holder(), ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> unstained_leggings = BASEITEMS.register("unstained_leggings",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED.holder(), ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> unstained_boots = BASEITEMS.register("unstained_boots",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED.holder(), ArmorItem.Type.BOOTS));
    public static final DeferredHolder<Item, Item> unstained_warhammer = HANDHELDITEMS.register("unstained_warhammer",
            () -> new UnstainedWarhammerItem(8f, -3.4f, EnumModToolTiers.UNSTAINED,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> unstained_shield = SPECIALITEMS.register("unstained_shield",
            () -> new UnstainedShieldItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> absolution_dagger = HANDHELDITEMS.register("absolution_dagger",
            () -> new AbsolutionDaggerItem(EnumModToolTiers.UNSTAINED, -4, -1.8f,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> annettas_absolution_dagger = HANDHELDITEMS.register("annettas_absolution_dagger",
            () -> new AbsolutionDaggerItem(EnumModToolTiers.UNSTAINED, -4, -1.8f,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> silthmere_glaive = HANDHELDITEMS.register("silthmere_glaive",
            () -> new SilthmereGlaiveItem(4, -2.8f, EnumModToolTiers.UNSTAINED,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> pale_silver_pickaxe = HANDHELDITEMS.register("pale_silver_pickaxe",
            () -> new PaleSilverPickaxeItem(EnumModToolTiers.UNSTAINED, 1, -2.8f,
                    new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> pale_silver_bell = SPECIALITEMS.register("pale_silver_bell",
            () -> new PaleSilverBellItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> lethean_chalice = SPECIALITEMS.register("lethean_chalice",
            () -> new LetheanChaliceItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> verdigris_censer = SPECIALITEMS.register("verdigris_censer",
            () -> new VerdigrisCenserItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    // Scars
    public static final DeferredHolder<Item, Item> self_reflection_mirror = BASEITEMS.register("self_reflection_mirror",
            () -> new ItemSelfReflectionMirror(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> mind_spike = BASEITEMS.register("mind_spike",
            () -> new ItemMindSpike(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> scar_blank = BASEITEMS.register("scar_blank",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> runic_motif_paper = BASEITEMS.register("runic_motif_paper",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> scar_binder = BASEITEMS.register("scar_binder",
            () -> new ItemScarBinder("scar_binder", 18, Rarity.UNCOMMON));
//	public static final DeferredHolder<Item, Item> scar_pattern = BASEITEMS.register("scar_pattern",
//			() -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> scar_binder_upgraded = BASEITEMS.register("scar_binder_upgraded",
            () -> new ItemScarBinder("scar_binder_upgraded", 27, Rarity.RARE));
    public static final DeferredHolder<Item, Item> respergillus = BASEITEMS.register("respergillus",
            () -> new RespergillusItem(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1));

    // Functional Spores
    public static final DeferredHolder<Item, Item> talaromyces_minus = BASEITEMS.register("talaromyces_minus",
            () -> new TalaromycesMinusItem(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 1));
    public static final DeferredHolder<Item, Item> lumina_devorans = BASEITEMS.register("lumina_devorans",
            () -> new NoctiluminaDevoransItem(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 1));
    public static final DeferredHolder<Item, Item> noctifly_agaric = BASEITEMS.register("noctifly_agaric",
            () -> new NoctiflyAgaricItem(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1));

    // Archon-tier Fungal Scars
    public static final DeferredHolder<Item, Item> antiphonomyces_resonans = BASEITEMS.register("antiphonomyces_resonans",
            () -> new AntiphonomycesResonansItem(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 1));
    public static final DeferredHolder<Item, Item> sanguiflora_cadens = BASEITEMS.register("sanguiflora_cadens",
            () -> new SanguifloraeCadensItem(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 1));
    public static final DeferredHolder<Item, Item> thanomyces_resurgens = BASEITEMS.register("thanomyces_resurgens",
            () -> new ThanomycesResurgensItem(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 1));
    public static final DeferredHolder<Item, Item> anastocordyceps_nexus = BASEITEMS.register("anastocordyceps_nexus",
            () -> new AnastocordycepsNexusItem(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 1));
    public static final DeferredHolder<Item, Item> saprovitta_vestigium = BASEITEMS.register("saprovitta_vestigium",
            () -> new SaprovittaVestigiumItem(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 1));

    // ── Hyphal substrate — crafting ingredient for the Mycelial Crucible ──────
    public static final DeferredHolder<Item, Item> hyphal_substrate = BASEITEMS.register("hyphal_substrate",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── Immature fungal scar — produced by Phase 1 of the Mycelial Crucible ──
    // A single item; tendency, threshold, and target-scar identity are stored
    // as custom NBT data so each cultivation run carries its own metadata.
    public static final DeferredHolder<Item, Item> immature_fungal_scar = BASEITEMS.register("immature_fungal_scar",
            () -> new ItemImmatureFungalScar(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> scar_heart = BASEITEMS.register("scar_heart",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1, 1)
                    .withModifier(Attributes.MAX_HEALTH, "scar_heart_hp", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withMaxBloodModifier(-100.0));

    // Spores
//	public static final DeferredHolder<Item, Item> incandescent_spores = BASEITEMS.register("incandescent_spores",
//	public static final DeferredHolder<Item, Item> fervent_spores = BASEITEMS.register("fervent_spores",
//	public static final DeferredHolder<Item, Item> ruinous_spores = BASEITEMS.register("ruinous_spores",
//	public static final DeferredHolder<Item, Item> umbral_spores = BASEITEMS.register("umbral_spores",
//	public static final DeferredHolder<Item, Item> frigid_spores = BASEITEMS.register("frigid_spores",
//	public static final DeferredHolder<Item, Item> vivacious_spores = BASEITEMS.register("vivacious_spores",
//	public static final DeferredHolder<Item, Item> ferric_spores = BASEITEMS.register("ferric_spores",

    public static final DeferredHolder<Item, Item> vivacious_spores = BASEITEMS.register("vivacious_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> fervent_spores = BASEITEMS.register("fervent_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> neurotic_spores = BASEITEMS.register("neurotic_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> incandescent_spores = BASEITEMS.register("incandescent_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> ruinous_spores = BASEITEMS.register("ruinous_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> frigid_spores = BASEITEMS.register("frigid_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> ferric_spores = BASEITEMS.register("ferric_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));
    public static final DeferredHolder<Item, Item> umbral_spores = BASEITEMS.register("umbral_spores",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));

    // ── Regular Scars (8 tendencies × 3 tiers = 24 scars) ──
    // Each scar has focused upsides and real downsides; magnitude escalates with tier.

    // ── ANIMUS — Vitality / Healing ──
    public static final DeferredHolder<Item, Item> scar_pattern_heart = BASEITEMS.register("scar_pattern_heart",
            () -> new ItemScarPattern(new Item.Properties(), scar_heart, "scar_heart"));
    public static final DeferredHolder<Item, Item> scar_marrow = BASEITEMS.register("scar_marrow",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 2, 2)
                    .withModifier(Attributes.MAX_HEALTH, "scar_marrow_hp", 4.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_marrow_ms", -0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withMaxBloodModifier(-200.0));
    public static final DeferredHolder<Item, Item> scar_pattern_marrow = BASEITEMS.register("scar_pattern_marrow",
            () -> new ItemScarPattern(new Item.Properties(), scar_marrow, "scar_marrow"));
    public static final DeferredHolder<Item, Item> scar_phoenix = BASEITEMS.register("scar_phoenix",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 3, 3)
                    .withModifier(Attributes.MAX_HEALTH, "scar_phoenix_hp", 6.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_phoenix_ms", -0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withMaxBloodModifier(-400.0));
    public static final DeferredHolder<Item, Item> scar_pattern_phoenix = BASEITEMS.register("scar_pattern_phoenix",
            () -> new ItemScarPattern(new Item.Properties(), scar_phoenix, "scar_phoenix"));
    public static final DeferredHolder<Item, Item> scar_pyre = BASEITEMS.register("scar_pyre",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 1, 1)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_pyre_ad", 1.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.ARMOR, "scar_pyre_armor", -1.0,
                            AttributeModifier.Operation.ADD_VALUE));

    // ── FLAMMEUS — Fire / Offense ──
    public static final DeferredHolder<Item, Item> scar_pattern_pyre = BASEITEMS.register("scar_pattern_pyre",
            () -> new ItemScarPattern(new Item.Properties(), scar_pyre, "scar_pyre"));
    public static final DeferredHolder<Item, Item> scar_sol = BASEITEMS.register("scar_sol",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 2, 2)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_sol_ad", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.ARMOR, "scar_sol_armor", -2.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_sol = BASEITEMS.register("scar_pattern_sol",
            () -> new ItemScarPattern(new Item.Properties(), scar_sol, "scar_sol"));
    public static final DeferredHolder<Item, Item> scar_corona = BASEITEMS.register("scar_corona",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 3, 3)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_corona_ad", 3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.KNOCKBACK_RESISTANCE, "scar_corona_kb", 0.3,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.ARMOR, "scar_corona_armor", -3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withBloodUpkeep(0.5));
    public static final DeferredHolder<Item, Item> scar_pattern_corona = BASEITEMS.register("scar_pattern_corona",
            () -> new ItemScarPattern(new Item.Properties(), scar_corona, "scar_corona"));
    public static final DeferredHolder<Item, Item> scar_feral = BASEITEMS.register("scar_feral",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 1, 1)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_feral_as", 0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ARMOR, "scar_feral_armor", -1.0,
                            AttributeModifier.Operation.ADD_VALUE));

    // ── DUCTILIS — Speed / Kill-chain ──
    public static final DeferredHolder<Item, Item> scar_pattern_feral = BASEITEMS.register("scar_pattern_feral",
            () -> new ItemScarPattern(new Item.Properties(), scar_feral, "scar_feral"));
    public static final DeferredHolder<Item, Item> scar_flux = BASEITEMS.register("scar_flux",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 2, 2)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_flux_as", 0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ARMOR, "scar_flux_armor", -2.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_flux = BASEITEMS.register("scar_pattern_flux",
            () -> new ItemScarPattern(new Item.Properties(), scar_flux, "scar_flux"));
    public static final DeferredHolder<Item, Item> scar_chimera = BASEITEMS.register("scar_chimera",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 3, 3)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_chimera_as", 0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ARMOR, "scar_chimera_armor", -3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MAX_HEALTH, "scar_chimera_hp", -4.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_chimera = BASEITEMS.register("scar_pattern_chimera",
            () -> new ItemScarPattern(new Item.Properties(), scar_chimera, "scar_chimera"));
    public static final DeferredHolder<Item, Item> scar_halo = BASEITEMS.register("scar_halo",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 1, 1)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_halo_at", 1.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_halo_ms", -0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // ── LUX — Light / Reactive Defense ──
    public static final DeferredHolder<Item, Item> scar_pattern_halo = BASEITEMS.register("scar_pattern_halo",
            () -> new ItemScarPattern(new Item.Properties(), scar_halo, "scar_halo"));
    public static final DeferredHolder<Item, Item> scar_veil = BASEITEMS.register("scar_veil",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 2, 2)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_veil_at", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_veil_ms", -0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Item, Item> scar_pattern_veil = BASEITEMS.register("scar_pattern_veil",
            () -> new ItemScarPattern(new Item.Properties(), scar_veil, "scar_veil"));
    public static final DeferredHolder<Item, Item> scar_transcendence = BASEITEMS.register("scar_transcendence",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 3, 3)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_transcendence_at", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_transcendence_ms", -0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withMaxBloodModifier(-100.0));
    public static final DeferredHolder<Item, Item> scar_pattern_transcendence = BASEITEMS.register(
            "scar_pattern_transcendence",
            () -> new ItemScarPattern(new Item.Properties(), scar_transcendence, "scar_transcendence"));
    public static final DeferredHolder<Item, Item> scar_blight = BASEITEMS.register("scar_blight",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 1, 1)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_blight_ad", 1.0,
                            AttributeModifier.Operation.ADD_VALUE));

    // ── MORTEM — Poison / Death ──
    public static final DeferredHolder<Item, Item> scar_pattern_blight = BASEITEMS.register("scar_pattern_blight",
            () -> new ItemScarPattern(new Item.Properties(), scar_blight, "scar_blight"));
    public static final DeferredHolder<Item, Item> scar_wither = BASEITEMS.register("scar_wither",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 2, 2)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_wither_ad", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MAX_HEALTH, "scar_wither_hp", -2.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_wither = BASEITEMS.register("scar_pattern_wither",
            () -> new ItemScarPattern(new Item.Properties(), scar_wither, "scar_wither"));
    public static final DeferredHolder<Item, Item> scar_oblivion = BASEITEMS.register("scar_oblivion",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 3, 3)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_oblivion_ad", 3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MAX_HEALTH, "scar_oblivion_hp", -4.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withBloodUpkeep(0.3));
    public static final DeferredHolder<Item, Item> scar_pattern_oblivion = BASEITEMS.register("scar_pattern_oblivion",
            () -> new ItemScarPattern(new Item.Properties(), scar_oblivion, "scar_oblivion"));
    public static final DeferredHolder<Item, Item> scar_rime = BASEITEMS.register("scar_rime",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 1, 1)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_rime_ms", 0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_rime_as", -0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // ── CONGEATIO — Ice / Control ──
    public static final DeferredHolder<Item, Item> scar_pattern_rime = BASEITEMS.register("scar_pattern_rime",
            () -> new ItemScarPattern(new Item.Properties(), scar_rime, "scar_rime"));
    public static final DeferredHolder<Item, Item> scar_glacier = BASEITEMS.register("scar_glacier",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 2, 2)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_glacier_ms", 0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_glacier_as", -0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Item, Item> scar_pattern_glacier = BASEITEMS.register("scar_pattern_glacier",
            () -> new ItemScarPattern(new Item.Properties(), scar_glacier, "scar_glacier"));
    public static final DeferredHolder<Item, Item> scar_descendence = BASEITEMS.register("scar_descendence",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 3, 3)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_descendence_ms", 0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_descendence_as", -0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_descendence_ad", -2.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_descendence = BASEITEMS.register("scar_pattern_descendence",
            () -> new ItemScarPattern(new Item.Properties(), scar_descendence, "scar_descendence"));
    public static final DeferredHolder<Item, Item> scar_thorn = BASEITEMS.register("scar_thorn",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 1, 1)
                    .withModifier(Attributes.ARMOR, "scar_thorn_armor", 1.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_thorn_ms", -0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // ── FERRIC — Armor / Thorns ──
    public static final DeferredHolder<Item, Item> scar_pattern_thorn = BASEITEMS.register("scar_pattern_thorn",
            () -> new ItemScarPattern(new Item.Properties(), scar_thorn, "scar_thorn"));
    public static final DeferredHolder<Item, Item> scar_anvil = BASEITEMS.register("scar_anvil",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 2, 2)
                    .withModifier(Attributes.ARMOR, "scar_anvil_armor", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_anvil_at", 1.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_anvil_ms", -0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Item, Item> scar_pattern_anvil = BASEITEMS.register("scar_pattern_anvil",
            () -> new ItemScarPattern(new Item.Properties(), scar_anvil, "scar_anvil"));
    public static final DeferredHolder<Item, Item> scar_crucible = BASEITEMS.register("scar_crucible",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 3, 3)
                    .withModifier(Attributes.ARMOR, "scar_crucible_armor", 3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_crucible_at", 2.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_crucible_ms", -0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_crucible_as", -0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<Item, Item> scar_pattern_crucible = BASEITEMS.register("scar_pattern_crucible",
            () -> new ItemScarPattern(new Item.Properties(), scar_crucible, "scar_crucible"));
    public static final DeferredHolder<Item, Item> scar_blood_honed = BASEITEMS.register("scar_blood_honed",
            () -> new BloodHonedScar(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> scar_pattern_blood_honed = BASEITEMS.register("scar_pattern_blood_honed",
            () -> new ItemScarPattern(new Item.Properties(), scar_blood_honed, "scar_blood_honed"));
    public static final DeferredHolder<Item, Item> scar_shade = BASEITEMS.register("scar_shade",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 1, 1)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_shade_ms", 0.05,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_shade_ad", -1.0,
                            AttributeModifier.Operation.ADD_VALUE));

    // ── TENEBRIS — Shadow / Stealth ──
    public static final DeferredHolder<Item, Item> scar_pattern_shade = BASEITEMS.register("scar_pattern_shade",
            () -> new ItemScarPattern(new Item.Properties(), scar_shade, "scar_shade"));
    public static final DeferredHolder<Item, Item> scar_moon = BASEITEMS.register("scar_moon",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 2, 2)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_moon_ms", 0.10,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_moon_ad", -2.0,
                            AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<Item, Item> scar_pattern_moon = BASEITEMS.register("scar_pattern_moon",
            () -> new ItemScarPattern(new Item.Properties(), scar_moon, "scar_moon"));
    public static final DeferredHolder<Item, Item> scar_eye = BASEITEMS.register("scar_eye",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 3, 3)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_eye_ms", 0.15,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_eye_ad", -3.0,
                            AttributeModifier.Operation.ADD_VALUE)
                    .withEffect(MobEffects.NIGHT_VISION, 0)
                    .withBloodUpkeep(0.3));
    public static final DeferredHolder<Item, Item> scar_pattern_eye = BASEITEMS.register("scar_pattern_eye",
            () -> new ItemScarPattern(new Item.Properties(), scar_eye, "scar_eye"));
    // Spawn Eggs
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_leech = SPAWNEGGS.register("spawn_egg_leech",
            () -> new DeferredSpawnEggItem(EntityInit.leech, 7761777, 4206080, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_fargone = SPAWNEGGS.register("spawn_egg_fargone",
            () -> new DeferredSpawnEggItem(EntityInit.fargone, 7352833, 7958646, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_thirster = SPAWNEGGS.register("spawn_egg_thirster",
            () -> new DeferredSpawnEggItem(EntityInit.thirster, 3093151, 9515521, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_fungling = SPAWNEGGS.register("spawn_egg_fungling",
            () -> new DeferredSpawnEggItem(EntityInit.fungling, 7798794, 15711418, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_eruptor = SPAWNEGGS.register("spawn_egg_eruptor",
            () -> new DeferredSpawnEggItem(EntityInit.erythromycelium_eruptus, 7798794, 12235264, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_chitinite = SPAWNEGGS.register(
            "spawn_egg_chitinite",
            () -> new DeferredSpawnEggItem(EntityInit.chitinite, 3617335, 8553354, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_fervent_chitinite = SPAWNEGGS.register(
            "spawn_egg_fervent_chitinite",
            () -> new DeferredSpawnEggItem(EntityInit.fervent_chitinite, 3617335, 12124160, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_chthonian = SPAWNEGGS.register(
            "spawn_egg_chthonian",
            () -> new DeferredSpawnEggItem(EntityInit.chthonian, 7488841, 2170666, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_chthonian_queen = SPAWNEGGS.register(
            "spawn_egg_chthonian_queen",
            () -> new DeferredSpawnEggItem(EntityInit.chthonian_queen, 7488841, 12235264, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_lump_of_thought = SPAWNEGGS.register(
            "spawn_egg_lump_of_thought",
            () -> new DeferredSpawnEggItem(EntityInit.lump_of_thought, 6094848, 11315361, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_abhorent_thought = SPAWNEGGS.register(
            "spawn_egg_abhorent_thought",
            () -> new DeferredSpawnEggItem(EntityInit.abhorent_thought, 12124160, 4259840, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_barbed_urchin = SPAWNEGGS.register(
            "spawn_egg_barbed_urchin",
            () -> new DeferredSpawnEggItem(EntityInit.barbed_urchin, 12124160, 4259840, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_chalybeate_snail = SPAWNEGGS.register(
            "spawn_egg_chalybeate_snail",
            () -> new DeferredSpawnEggItem(EntityInit.chalybeate_snail, 0x2C3032, 0xB35348, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_morphling_polyp = SPAWNEGGS.register(
            "spawn_egg_morphling_polyp",
            () -> new DeferredSpawnEggItem(EntityInit.morphling_polyp, 6881280, 0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_blood_drunk_puppeteer = SPAWNEGGS.register(
            "spawn_egg_blood_drunk_puppeteer",
            () -> new DeferredSpawnEggItem(EntityInit.blood_drunk_puppeteer, 12124160, 12152064, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_enthralled_doll = SPAWNEGGS.register(
            "spawn_egg_enthralled_doll",
            () -> new DeferredSpawnEggItem(EntityInit.enthralled_doll, 12124160, 12564912, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_veinwing_vulture = SPAWNEGGS.register(
            "spawn_egg_veinwing_vulture",
            () -> new DeferredSpawnEggItem(EntityInit.veinwing_vulture, 0x5A0B1B, 0xC04050, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_marrow_spitter = SPAWNEGGS.register(
            "spawn_egg_marrow_spitter",
            () -> new DeferredSpawnEggItem(EntityInit.marrow_spitter, 0xE7D6C3, 0x8A1020, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_gorebound_hulk = SPAWNEGGS.register(
            "spawn_egg_gorebound_hulk",
            () -> new DeferredSpawnEggItem(EntityInit.gorebound_hulk, 0x3A0508, 0xAA1A22, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_hemolymphopoda = SPAWNEGGS.register(
            "spawn_egg_hemolymphopoda",
            () -> new DeferredSpawnEggItem(EntityInit.hemolymphopoda, 6579558, 4875998, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_dessicant = SPAWNEGGS.register(
            "spawn_egg_dessicant",
            () -> new DeferredSpawnEggItem(EntityInit.dessicant, 0xC2A66E, 0x8B1A1A, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_cruor_fiend = SPAWNEGGS.register(
            "spawn_egg_cruor_fiend",
            () -> new DeferredSpawnEggItem(EntityInit.cruor_fiend, 0x4A0000, 0xFF4500, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_void_drinker = SPAWNEGGS.register(
            "spawn_egg_void_drinker",
            () -> new DeferredSpawnEggItem(EntityInit.void_drinker, 0x1A0033, 0x6A0DAD, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_frozen_clot = SPAWNEGGS.register(
            "spawn_egg_frozen_clot",
            () -> new DeferredSpawnEggItem(EntityInit.frozen_clot, 0xA8D8EA, 0x5C0000, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_abyssal_siphon = SPAWNEGGS.register(
            "spawn_egg_abyssal_siphon",
            () -> new DeferredSpawnEggItem(EntityInit.abyssal_siphon, 0x0D0D0D, 0x2D0037, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_synapse_hound = SPAWNEGGS.register(
            "spawn_egg_synapse_hound",
            () -> new DeferredSpawnEggItem(EntityInit.synapse_hound, 0x7DF9FF, 0x8B0000, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_myelin_borer = SPAWNEGGS.register(
            "spawn_egg_myelin_borer",
            () -> new DeferredSpawnEggItem(EntityInit.myelin_borer, 0xE8D8C0, 0x7DF9FF, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_crimson_doe = SPAWNEGGS.register(
            "spawn_egg_crimson_doe",
            () -> new DeferredSpawnEggItem(EntityInit.crimson_doe, 0xFAE6E6, 0x8B0000, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_hemojelly = SPAWNEGGS.register(
            "spawn_egg_hemojelly",
            () -> new DeferredSpawnEggItem(EntityInit.hemojelly, 0xFF6B8A, 0xCC2244, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_blood_lantern_jelly = SPAWNEGGS.register(
            "spawn_egg_blood_lantern_jelly",
            () -> new DeferredSpawnEggItem(EntityInit.blood_lantern_jelly, 0x8B1E4D, 0xFF8AB0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_mnemonic_whale = SPAWNEGGS.register(
            "spawn_egg_mnemonic_whale",
            () -> new DeferredSpawnEggItem(EntityInit.mnemonic_whale, 0x5A3145, 0xC26F8D, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_brined_votary = SPAWNEGGS.register(
            "spawn_egg_brined_votary",
            () -> new DeferredSpawnEggItem(EntityInit.brined_votary, 0x2B3F45, 0x8A1F2C, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_harbinger_voyager = SPAWNEGGS.register(
            "spawn_egg_harbinger_voyager",
            () -> new DeferredSpawnEggItem(EntityInit.harbinger_voyager, 0x4A1F27, 0xC29A68, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_harbinger_votary_wayfarer = SPAWNEGGS.register(
            "spawn_egg_harbinger_votary_wayfarer",
            () -> new DeferredSpawnEggItem(EntityInit.harbinger_votary_wayfarer, 0x63232A, 0x7C8B88,
                    new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_venous_strider = SPAWNEGGS.register(
            "spawn_egg_venous_strider",
            () -> new DeferredSpawnEggItem(EntityInit.venous_strider, 0xF5DEB3, 0x8B4513, new Item.Properties()));
    // Cream body with blood-red spots
    public static final DeferredHolder<Item, DeferredSpawnEggItem> spawn_egg_tooth_pecks = SPAWNEGGS.register(
            "spawn_egg_tooth_pecks",
            () -> new DeferredSpawnEggItem(EntityInit.tooth_pecks, 0xF0EAD6, 0x8B0000, new Item.Properties()));

    public static Stream<DeferredHolder<Item, ? extends Item>> getAllItemEntriesAsStream() {

        return Stream
                .<Collection<? extends DeferredHolder<Item, ? extends Item>>>of(BASEITEMS.getEntries(),
                        HANDHELDITEMS.getEntries(), SPECIALITEMS.getEntries(), SPAWNEGGS.getEntries())
                .flatMap(Collection::stream);
    }

}
