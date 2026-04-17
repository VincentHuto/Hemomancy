package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.item.*;
import com.vincenthuto.hemomancy.common.item.armor.*;
import com.vincenthuto.hemomancy.common.item.bloodline.BloodlinePoolMonitorItem;
import com.vincenthuto.hemomancy.common.item.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.item.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.cosmetic.SanguineBlobItem;
import com.vincenthuto.hemomancy.common.item.memories.*;
import com.vincenthuto.hemomancy.common.item.morphlings.*;
import com.vincenthuto.hemomancy.common.item.scar.ItemMindSpike;
import com.vincenthuto.hemomancy.common.item.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.item.scar.ItemSelfReflectionMirror;
import com.vincenthuto.hemomancy.common.item.scar.functional.NoctiflyAgaricItem;
import com.vincenthuto.hemomancy.common.item.scar.functional.NoctiluminaDevoransItem;
import com.vincenthuto.hemomancy.common.item.scar.functional.RespergillusItem;
import com.vincenthuto.hemomancy.common.item.scar.functional.TalaromycesMinusItem;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.tool.*;
import com.vincenthuto.hemomancy.common.item.tool.living.*;
import com.vincenthuto.hemomancy.common.item.tool.unstained.UnstainedWarhammerItem;
import com.vincenthuto.hemomancy.common.saint.EnumSaintType;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ItemInit {
    public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<BannerPattern> BANNERPATTERNS = DeferredRegister
            .create(Registries.BANNER_PATTERN, Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> HANDHELDITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> SPECIALITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Hemomancy.MOD_ID);
    public static final DeferredRegister<Item> SPAWNEGGS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Hemomancy.MOD_ID);

    // public static final DamageSource bloodLoss = new DamageSource("bloodloss");

    public static final RegistryObject<Item> chitinite_arm_banner = SPECIALITEMS.register("chitinite_arm_banner",
            () -> new ItemArmBanner(new Item.Properties(), EnumModArmorTiers.CHITINITE,
                    Hemomancy.rloc("textures/entity/arm_banner/chitinite_arm_banner.png")));

    public static final RegistryObject<BannerPattern> heart = BANNERPATTERNS.register("hemomancy_heart",
            () -> new BannerPattern("hemomancy_heart"));

    public static final RegistryObject<Item> heart_pattern = BASEITEMS.register("heart_pattern",
            () -> new BannerPatternItem(
                    TagKey.create(Registries.BANNER_PATTERN, Hemomancy.rloc("pattern_item/hemomancy_heart")),
                    new Item.Properties()));

    public static final RegistryObject<BannerPattern> veins = BANNERPATTERNS.register("hemomancy_veins",
            () -> new BannerPattern("hemomancy_veins"));
    public static final RegistryObject<Item> veins_pattern = BASEITEMS.register("veins_pattern",
            () -> new BannerPatternItem(
                    TagKey.create(Registries.BANNER_PATTERN, Hemomancy.rloc("pattern_item/hemomancy_veins")),
                    new Item.Properties()));

    // Charm
    public static final RegistryObject<Item> charm_of_vascularium = BASEITEMS.register("charm_of_vascularium",
            () -> new VasculariumCharmItem(new Item.Properties(), EnumBloodTendency.ANIMUS, 0));

    // Debug / Testing
    public static final RegistryObject<Item> structure_spawner = SPECIALITEMS.register("structure_spawner",
            () -> new StructureSpawnerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> debug_showcase = SPECIALITEMS.register("debug_showcase",
            () -> new DebugShowcaseItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> structure_scanner = SPECIALITEMS.register("structure_scanner",
            () -> new StructureScannerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // Book

    public static final RegistryObject<Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
            () -> new BloodyBookItem(new Item.Properties().stacksTo(1),
                    Hemomancy.rloc("textures/entity/liber_sanguinum.png")));
    public static final RegistryObject<Item> unsigned_ancestral_ledger = BASEITEMS.register("unsigned_ancestral_ledger",
            () -> new UnsignedLedgerItem(new Item.Properties()));

    // Base Items
    public static final RegistryObject<Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> sanguine_blob = BASEITEMS.register("sanguine_blob",
            () -> new SanguineBlobItem(new Item.Properties()));
    public static final RegistryObject<Item> rite_hint = BASEITEMS.register("rite_hint",
            () -> new RiteHintItem(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> foul_paste = BASEITEMS.register("foul_paste",
            () -> new FoulPasteItem(new Item.Properties()));
    public static final RegistryObject<Item> smouldering_ash = SPECIALITEMS.register("smouldering_ash",
            () -> new ItemNameBlockItem(BlockInit.smouldering_ash_trail.get(), (new Item.Properties())));
    public static final RegistryObject<Item> befouling_ash = SPECIALITEMS.register("befouling_ash",
            () -> new ItemNameBlockItem(BlockInit.befouling_ash_trail.get(), (new Item.Properties())));
    public static final RegistryObject<Item> active_smouldering_ash = SPECIALITEMS.register("active_smouldering_ash",
            () -> new ItemNameBlockItem(BlockInit.active_smouldering_ash_trail.get(), (new Item.Properties())));
    public static final RegistryObject<Item> active_befouling_ash = SPECIALITEMS.register("active_befouling_ash",
            () -> new ItemNameBlockItem(BlockInit.active_befouling_ash_trail.get(), (new Item.Properties())));
    public static final RegistryObject<Item> hematic_iron_scrap = BASEITEMS.register("hematic_iron_scrap",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> consecrated_copper_ingot = BASEITEMS.register("consecrated_copper_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> hematic_iron_powder = BASEITEMS.register("hematic_iron_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> serpent_scale = BASEITEMS.register("serpent_scale",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> swollen_leech = BASEITEMS.register("swollen_leech",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> dried_leech = BASEITEMS.register("dried_leech",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> chitinous_husk = BASEITEMS.register("chitinous_husk",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> sanguine_conduit = BASEITEMS.register("sanguine_conduit",
            () -> new ItemSanguineConduit(new Item.Properties()));
    public static final RegistryObject<Item> scrying_dish = BASEITEMS.register("scrying_dish",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> vitality_chalice = BASEITEMS.register("vitality_chalice",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> blood_stained_stone = BASEITEMS.register("blood_stained_stone",
            () -> new BloodStainedStoneItem(new Item.Properties()));
    public static final RegistryObject<Item> sanguine_salve = BASEITEMS.register("sanguine_salve",
            () -> new SanguineSalveItem(new Item.Properties(), 25f));
    public static final RegistryObject<Item> bleeding_bulb = BASEITEMS.register("bleeding_bulb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> dicentra_sap = BASEITEMS.register("dicentra_sap",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> puppeteering_thread = BASEITEMS.register("puppeteering_thread",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> blood_crystal_shard = BASEITEMS.register("blood_crystal_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> sanguine_quintessence = BASEITEMS.register("sanguine_quintessence",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> cleansed_blood_crystal_shard = BASEITEMS.register("cleansed_blood_crystal_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> cleansing_hemolymph = SPECIALITEMS.register("cleansing_hemolymph",
            () -> new CleansingHemolymphItem(new Item.Properties()));

    public static final RegistryObject<Item> vivianite_cluster = BASEITEMS.register("vivianite_cluster",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> vivianite_scalpel = BASEITEMS.register("vivianite_scalpel",
            () -> new VivianiteScalpelItem(new Item.Properties()));
    public static final RegistryObject<Item> spore_sac = BASEITEMS.register("spore_sac",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> desiccated_membrane = BASEITEMS.register("desiccated_membrane",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> molten_clot = BASEITEMS.register("molten_clot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> void_ichor = BASEITEMS.register("void_ichor",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> frozen_cruor = BASEITEMS.register("frozen_cruor",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> abyssal_ichor = BASEITEMS.register("abyssal_ichor",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> nerve_bundle = BASEITEMS.register("nerve_bundle",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> fungal_spine = BASEITEMS.register("fungal_spine",
            () -> new FungalSpineItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> gourd_seeds = BASEITEMS.register("gourd_seeds",
            () -> new ItemNameBlockItem(BlockInit.gourd_stem.get(), new Item.Properties()));

    // Gourd Foods
    public static final RegistryObject<Item> gourd_slice = BASEITEMS.register("gourd_slice",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).fast().build())));
    public static final RegistryObject<Item> roasted_gourd_seeds = BASEITEMS.register("roasted_gourd_seeds",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.2F).fast().build())));
    public static final RegistryObject<Item> gourd_stew = BASEITEMS.register("gourd_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1)
                    .food(new FoodProperties.Builder().nutrition(7).saturationMod(0.6F).build())));

    // Qliphoth Reagent
    public static final RegistryObject<Item> qliphoth_pome = BASEITEMS.register("qliphoth_pome",
            () -> new QliphothPomeItem(new Item.Properties().rarity(Rarity.UNCOMMON)
                    .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.8F).alwaysEat().build())));

    // Anti blood
    public static final RegistryObject<Item> hemolytic_solution = BASEITEMS.register("hemolytic_solution",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> hemolytic_plating = BASEITEMS.register("hemolytic_plating",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> neutralizing_gasket = BASEITEMS.register("neutralizing_gasket",
            () -> new Item(new Item.Properties()));


    // Enzymes
    public static final RegistryObject<Item> vivacious_enzyme = BASEITEMS.register("vivacious_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.ANIMUS, 10));
    public static final RegistryObject<Item> ruinous_enzyme = BASEITEMS.register("ruinous_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.MORTEM, 10));
    public static final RegistryObject<Item> neurotic_enzyme = BASEITEMS.register("neurotic_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.DUCTILIS, 10));
    public static final RegistryObject<Item> ferric_enzyme = BASEITEMS.register("ferric_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.FERRIC, 10));
    public static final RegistryObject<Item> fervent_enzyme = BASEITEMS.register("fervent_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.FLAMMEUS, 10));
    public static final RegistryObject<Item> frigid_enzyme = BASEITEMS.register("frigid_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.CONGEATIO, 10));
    public static final RegistryObject<Item> incandescent_enzyme = BASEITEMS.register("incandescent_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.LUX, 10));
    public static final RegistryObject<Item> umbral_enzyme = BASEITEMS.register("umbral_enzyme",
            () -> new EnzymeItem(EnumBloodTendency.TENEBRIS, 10));
    public static final RegistryObject<Item> recycled_enzyme = BASEITEMS.register("recycled_enzyme",
            () -> new RecycledEnzymeItem());
//	public static final RegistryObject<Item> saint_enzyme = BASEITEMS.register("saint_enzyme",
//			() -> new EnzymeItem(EnumBloodTendency.LUX, 20));

    // Hallowed Residuum — Saint-Enzymes (hybridized, extracted from Preserved Corpus)
    public static final RegistryObject<Item> hallowed_residuum_hemorath = BASEITEMS.register("hallowed_residuum_hemorath",
            () -> new HallowedResiduumItem(EnumSaintType.HEMORATH, 20));
    public static final RegistryObject<Item> hallowed_residuum_seraphae = BASEITEMS.register("hallowed_residuum_seraphae",
            () -> new HallowedResiduumItem(EnumSaintType.SERAPHAE, 20));
    public static final RegistryObject<Item> hallowed_residuum_putriciel = BASEITEMS.register("hallowed_residuum_putriciel",
            () -> new HallowedResiduumItem(EnumSaintType.PUTRICIEL, 20));
    public static final RegistryObject<Item> hallowed_residuum_velorum = BASEITEMS.register("hallowed_residuum_velorum",
            () -> new HallowedResiduumItem(EnumSaintType.VELORUM, 20));

    // Consecrated Syringe — extraction tool for Saint Sarcophagus
    public static final RegistryObject<Item> consecrated_syringe = SPECIALITEMS.register("consecrated_syringe",
            () -> new ConsecratedSyringeItem(new Item.Properties().stacksTo(1)));

    // Blood Tendency Gauge
    public static final RegistryObject<Item> blood_tendency_gauge = BASEITEMS.register("blood_tendency_gauge",
            () -> new BloodTendencyGaugeItem(new Item.Properties().stacksTo(1)));

    // Vascular Status Gauge
    public static final RegistryObject<Item> vascular_status_gauge = BASEITEMS.register("vascular_status_gauge",
            () -> new VascularStatusGaugeItem(new Item.Properties().stacksTo(1)));

    // Bloodline Pool Monitor
    public static final RegistryObject<Item> bloodline_pool_monitor = BASEITEMS.register("bloodline_pool_monitor",
            () -> new BloodlinePoolMonitorItem(new Item.Properties().stacksTo(1)));

    // Unstained — Our Lady of Still Waters materials
    public static final RegistryObject<Item> lethean_dew = BASEITEMS.register("lethean_dew",
            () -> new LetheanDewItem(new Item.Properties()));
    public static final RegistryObject<Item> lethean_brew = BASEITEMS.register("lethean_brew",
            () -> new LetheanBrewItem(new Item.Properties()));
    public static final RegistryObject<Item> tears_of_silthmere = BASEITEMS.register("tears_of_silthmere",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> lethean_poppy_wreath = BASEITEMS.register("lethean_poppy_wreath",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> silver_chalice = BASEITEMS.register("silver_chalice",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> tome_of_the_unstained = BASEITEMS.register("tome_of_the_unstained",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> pallid_icon = BASEITEMS.register("pallid_icon",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> pale_silver_ingot = BASEITEMS.register("pale_silver_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> pale_distillate = BASEITEMS.register("pale_distillate",
            () -> new Item(new Item.Properties().stacksTo(16)));

    // Hematic Memories
    public static final RegistryObject<Item> fervent_husk = BASEITEMS.register("fervent_husk",
            () -> new FerventHuskItem(new Item.Properties()));
    public static final RegistryObject<Item> hematic_memory = BASEITEMS.register("hematic_memory",
            () -> new HematicMemoryItem(new Item.Properties()));
    public static final RegistryObject<Item> memory_blood_absorption = BASEITEMS.register("memory_blood_absorption",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_absorption));
    public static final RegistryObject<Item> memory_blood_projection = BASEITEMS.register("memory_blood_projection",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_projection));
    public static final RegistryObject<Item> memory_venous_travel = BASEITEMS.register("memory_venous_travel",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.venous_travel));
    public static final RegistryObject<Item> memory_blood_shot = BASEITEMS.register("memory_blood_shot",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_shot));
    public static final RegistryObject<Item> memory_blood_aneurysm = BASEITEMS.register("memory_blood_aneurysm",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_aneurysm));
    public static final RegistryObject<Item> memory_blood_rush = BASEITEMS.register("memory_blood_rush",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_rush));
    public static final RegistryObject<Item> memory_deadly_gaze = BASEITEMS.register("memory_deadly_gaze",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.deadly_gaze));
    public static final RegistryObject<Item> memory_blood_needle = BASEITEMS.register("memory_blood_needle",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_needle));
    public static final RegistryObject<Item> memory_blood_cloud = BASEITEMS.register("memory_blood_cloud",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_cloud));
    public static final RegistryObject<Item> memory_activation_potential = BASEITEMS.register(
            "memory_activation_potential",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.activation_potential));
    public static final RegistryObject<Item> memory_sanguine_ward = BASEITEMS.register("memory_sanguine_ward",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_ward));
    public static final RegistryObject<Item> memory_living_blade = BASEITEMS.register("memory_living_blade",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.conjure_blade));
    public static final RegistryObject<Item> memory_summon_avatar = BASEITEMS.register("memory_summon_avatar",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.summon_avatar));
    public static final RegistryObject<Item> memory_summon_thrall = BASEITEMS.register("memory_summon_thrall",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.summon_thrall));
    public static final RegistryObject<Item> memory_ferric_transmutation = BASEITEMS.register(
            "memory_ferric_transmutation",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.ferric_transmutation));
    public static final RegistryObject<Item> memory_crimson_flame_conjuration = BASEITEMS.register(
            "memory_crimson_flame_conjuration",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_flame_conjuration));
    // Organ Echoes (Visceral Mirror system)
    public static final RegistryObject<Item> echo_of_spleen = SPECIALITEMS.register("echo_of_spleen",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.SPLEEN));
    public static final RegistryObject<Item> echo_of_liver = SPECIALITEMS.register("echo_of_liver",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.LIVER));
    public static final RegistryObject<Item> echo_of_lungs = SPECIALITEMS.register("echo_of_lungs",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.LUNGS));
    public static final RegistryObject<Item> echo_of_kidneys = SPECIALITEMS.register("echo_of_kidneys",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.KIDNEYS));
    public static final RegistryObject<Item> echo_of_heart = SPECIALITEMS.register("echo_of_heart",
            () -> new OrganEchoItem(new Item.Properties(), EnumOrgan.HEART));
    public static final RegistryObject<Item> memory_sanguine_mending = BASEITEMS.register("memory_sanguine_mending",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_mending));
    public static final RegistryObject<Item> memory_hemosynthesis = BASEITEMS.register("memory_hemosynthesis",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.hemosynthesis));
    public static final RegistryObject<Item> memory_blood_lamp = BASEITEMS.register("memory_blood_lamp",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_lamp));
    public static final RegistryObject<Item> memory_crimson_harvest = BASEITEMS.register("memory_crimson_harvest",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_harvest));
    public static final RegistryObject<Item> memory_glacial_grasp = BASEITEMS.register("memory_glacial_grasp",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.glacial_grasp));
    public static final RegistryObject<Item> memory_sanguine_excavation = BASEITEMS.register(
            "memory_sanguine_excavation",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_excavation));
    public static final RegistryObject<Item> memory_pyretic_forge = BASEITEMS.register("memory_pyretic_forge",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.pyretic_forge));
    public static final RegistryObject<Item> memory_umbral_step = BASEITEMS.register("memory_umbral_step",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.umbral_step));
    public static final RegistryObject<Item> memory_crimson_sight = BASEITEMS.register("memory_crimson_sight",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.crimson_sight));
    public static final RegistryObject<Item> memory_vital_reservoir = BASEITEMS.register("memory_vital_reservoir",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vital_reservoir));
    // ── Expanded tendency memories ──
    public static final RegistryObject<Item> memory_cryogenic_pulse = BASEITEMS.register("memory_cryogenic_pulse",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.cryogenic_pulse));
    public static final RegistryObject<Item> memory_glacial_bastion = BASEITEMS.register("memory_glacial_bastion",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.glacial_bastion));
    public static final RegistryObject<Item> memory_sanguine_ignition = BASEITEMS.register("memory_sanguine_ignition",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.sanguine_ignition));
    public static final RegistryObject<Item> memory_vitric_combustion = BASEITEMS.register("memory_vitric_combustion",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.vitric_combustion));
    public static final RegistryObject<Item> memory_void_shroud = BASEITEMS.register("memory_void_shroud",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.void_shroud));
    public static final RegistryObject<Item> memory_blood_eclipse = BASEITEMS.register("memory_blood_eclipse",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.blood_eclipse));
    public static final RegistryObject<Item> memory_hemorrhage = BASEITEMS.register("memory_hemorrhage",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.hemorrhage));
    public static final RegistryObject<Item> memory_exsanguinate = BASEITEMS.register("memory_exsanguinate",
            () -> new BloodMemoryItem(new Item.Properties(), ManipulationInit.exsanguinate));
    // Living
    public static final RegistryObject<Item> blood_absorption = SPECIALITEMS.register("blood_absorption",
            () -> new BloodAbsorptionItem(new Item.Properties()));
    public static final RegistryObject<Item> blood_projection = SPECIALITEMS.register("blood_projection",
            () -> new BloodProjectionItem(new Item.Properties()));
    public static final RegistryObject<Item> sanguis_lancea = SPECIALITEMS.register("sanguis_lancea",
            () -> new SanguisLanceaItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> living_syringe = SPECIALITEMS.register("living_syringe",
            () -> new LivingSyringeItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> living_staff = SPECIALITEMS.register("living_staff",
            () -> new LivingStaffItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> living_blade = SPECIALITEMS.register("living_blade",
            () -> new LivingBladeItem(25f, 3, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> living_axe = SPECIALITEMS.register("living_axe",
            () -> new LivingAxeItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> living_spear = SPECIALITEMS.register("living_spear",
            () -> new LivingSpearItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> living_baghnakh = SPECIALITEMS.register("living_baghnakh",
            () -> new LivingBaghnakhItem(25f, 1, EnumModToolTiers.LIVING, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> living_crossbow = SPECIALITEMS.register("living_crossbow",
            () -> new LivingCrossbowItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> blood_bolt = BASEITEMS.register("blood_bolt",
            () -> new BloodBoltItem(new Item.Properties()));

    // Blood Thrall — creature-based blood transport
    public static final RegistryObject<Item> blood_thrall_effigy = BASEITEMS.register("blood_thrall_effigy",
            () -> new BloodThrallItem(new Item.Properties().stacksTo(16)));

    // Morphlings
    public static final RegistryObject<Item> morphling_polyp = BASEITEMS.register("morphling_polyp",
            () -> new ItemMorphlingPolyp(new Item.Properties()));
    public static final RegistryObject<Item> morphling_jar = SPECIALITEMS.register("morphling_jar",
            () -> new ItemMorphlingJar("morphling_jar", 6, Rarity.UNCOMMON));
    public static final RegistryObject<Item> morphling_fungal = BASEITEMS.register("morphling_fungal",
            () -> new FungalMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_leeches = BASEITEMS.register("morphling_leeches",
            () -> new LeechesMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_chitinite = BASEITEMS.register("morphling_chitinite",
            () -> new ChitiniteMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_serpent = BASEITEMS.register("morphling_serpent",
            () -> new SerpentMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_pests = BASEITEMS.register("morphling_pests",
            () -> new PestsMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_spider = BASEITEMS.register("morphling_spider",
            () -> new SpiderMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_moth = BASEITEMS.register("morphling_moth",
            () -> new MothMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_tick = BASEITEMS.register("morphling_tick",
            () -> new TickMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_centipede = BASEITEMS.register("morphling_centipede",
            () -> new CentipedeMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_bat = BASEITEMS.register("morphling_bat",
            () -> new BatMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_urchin = BASEITEMS.register("morphling_urchin",
            () -> new UrchinMorphlingItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> morphling_mole = BASEITEMS.register("morphling_mole",
            () -> new MoleMorphlingItem(new Item.Properties().stacksTo(1)));

    /// Blood Gourds
    public static final RegistryObject<Item> dried_gourd = BASEITEMS.register("dried_gourd",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.SIMPLE));
    public static final RegistryObject<Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.CRIMSON));
    public static final RegistryObject<Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
            () -> new BloodGourdItem(new Item.Properties().stacksTo(1), EnumBloodGourdTiers.ASHEN));
    public static final RegistryObject<Item> curved_horn = SPECIALITEMS.register("curved_horn",
            () -> new CurvedHornItem(EnumBloodGourdTiers.HORN));

    // Flasks
    public static final RegistryObject<Item> blood_rock = BASEITEMS
            .register("blood_rock", () -> new BloodyFlaskItem(new Item.Properties(), 250));
    public static final RegistryObject<Item> bloody_flask = BASEITEMS.register("bloody_flask",
            () -> new BloodyFlaskItem(new Item.Properties(), 2500));
    public static final RegistryObject<Item> bloody_jug = BASEITEMS.register("bloody_jug",
            () -> new BloodyFlaskItem(new Item.Properties(), 5000));


    public static final RegistryObject<Item> bloody_vial = SPECIALITEMS.register("bloody_vial",
            () -> new BloodVialItem(new Item.Properties()));

    // Equipment

    public static final RegistryObject<Item> engram_stamp = SPECIALITEMS.register("engram_stamp",
            () -> new EngramStampItem(new Item.Properties().stacksTo(1)));

    // Artifacts
    public static final RegistryObject<Item> marrow_crown = BASEITEMS.register("marrow_crown",
            () -> new MarrowCrownArmorItem(EnumModArmorTiers.MARROW_CROWN, ArmorItem.Type.HELMET)
    );

    // Hematic
    public static final RegistryObject<Item> hematic_iron_helm = BASEITEMS.register("hematic_iron_helm",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> hematic_iron_chestplate = BASEITEMS.register("hematic_iron_chestplate",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> hematic_iron_leggings = BASEITEMS.register("hematic_iron_leggings",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> hematic_iron_boots = BASEITEMS.register("hematic_iron_boots",
            () -> new HematicIronArmorItem(EnumModArmorTiers.HEMATIC_IRON, ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> hematic_iron_sword = HANDHELDITEMS.register("hematic_iron_sword",
            () -> new SwordItem(EnumModToolTiers.HEMATIC_IRON, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> hematic_iron_knapper = HANDHELDITEMS.register("hematic_iron_knapper",
            () -> new ItemKnapper(42f, 1, 0, EnumModToolTiers.HEMATIC_IRON, new Item.Properties()));

    // Blood Lust
    // Masks
    public static final RegistryObject<Item> tengu_mask = BASEITEMS.register("tengu_mask",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> horned_mask = BASEITEMS.register("horned_mask",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> blood_lust_helm = BASEITEMS.register("blood_lust_helm",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.HELMET,
                    EnumBloodLustMaskTypes.NONE));
    public static final RegistryObject<Item> blood_lust_helm_tengu = BASEITEMS.register("blood_lust_helm_tengu",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.HELMET,
                    EnumBloodLustMaskTypes.TENGU));
    public static final RegistryObject<Item> blood_lust_helm_horned = BASEITEMS.register("blood_lust_helm_horned",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.HELMET,
                    EnumBloodLustMaskTypes.HORNED));
    public static final RegistryObject<Item> blood_lust_chest = BASEITEMS.register("blood_lust_chest",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.CHESTPLATE,
                    EnumBloodLustMaskTypes.NONE));
    public static final RegistryObject<Item> blood_lust_legs = BASEITEMS.register("blood_lust_legs",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.LEGGINGS,
                    EnumBloodLustMaskTypes.NONE));
    public static final RegistryObject<Item> blood_lust_boots = BASEITEMS.register("blood_lust_boots",
            () -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, ArmorItem.Type.BOOTS,
                    EnumBloodLustMaskTypes.NONE));

    // Barbed/Barbed
    public static final RegistryObject<Item> barbed_blade = SPECIALITEMS.register("barbed_blade",
            () -> new SwordItem(EnumModToolTiers.LIVING, 3, 25f, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> barbed_shield = SPECIALITEMS.register("barbed_shield",
            () -> new BarbedShieldItem(new Item.Properties()));
    public static final RegistryObject<Item> barbed_helm = BASEITEMS.register("barbed_helm",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> barbed_chestplate = BASEITEMS.register("barbed_chestplate",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> barbed_leggings = BASEITEMS.register("barbed_leggings",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> barbed_boots = BASEITEMS.register("barbed_boots",
            () -> new BarbedArmorItem(EnumModArmorTiers.BARBED, ArmorItem.Type.BOOTS));

    // Chitinite
    public static final RegistryObject<Item> chitinite_mace = SPECIALITEMS.register("chitinite_mace",
            () -> new SwordItem(EnumModToolTiers.LIVING, 3, 25f, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> chitinite_shield = SPECIALITEMS.register("chitinite_shield",
            () -> new ChitiniteShieldItem(new Item.Properties()));
    public static final RegistryObject<Item> chitinite_helm = BASEITEMS.register("chitinite_helm",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> chitinite_chestplate = BASEITEMS.register("chitinite_chestplate",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> chitinite_leggings = BASEITEMS.register("chitinite_leggings",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> chitinite_boots = BASEITEMS.register("chitinite_boots",
            () -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, ArmorItem.Type.BOOTS));

    // Unstained
    public static final RegistryObject<Item> unstained_helm = BASEITEMS.register("unstained_helm",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> unstained_chestplate = BASEITEMS.register("unstained_chestplate",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> unstained_leggings = BASEITEMS.register("unstained_leggings",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> unstained_boots = BASEITEMS.register("unstained_boots",
            () -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> unstained_warhammer = HANDHELDITEMS.register("unstained_warhammer",
            () -> new UnstainedWarhammerItem(8f, -3.0f, EnumModToolTiers.UNSTAINED,
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> unstained_shield = SPECIALITEMS.register("unstained_shield",
            () -> new UnstainedShieldItem(new Item.Properties()));

    // Scars
    public static final RegistryObject<Item> self_reflection_mirror = BASEITEMS.register("self_reflection_mirror",
            () -> new ItemSelfReflectionMirror(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> mind_spike = BASEITEMS.register("mind_spike",
            () -> new ItemMindSpike(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> scar_blank = BASEITEMS.register("scar_blank",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> runic_motif_paper = BASEITEMS.register("runic_motif_paper",
            () -> new Item(new Item.Properties()));
//	public static final RegistryObject<Item> scar_pattern = BASEITEMS.register("scar_pattern",
//			() -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> scar_binder = BASEITEMS.register("scar_binder",
            () -> new ItemScarBinder("scar_binder", 18, Rarity.UNCOMMON));
    public static final RegistryObject<Item> scar_binder_upgraded = BASEITEMS.register("scar_binder_upgraded",
            () -> new ItemScarBinder("scar_binder_upgraded", 27, Rarity.RARE));

    // Functional Spores

    public static final RegistryObject<Item> respergillus = BASEITEMS.register("respergillus",
            () -> new RespergillusItem(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1));

    public static final RegistryObject<Item> talaromyces_minus = BASEITEMS.register("talaromyces_minus",
            () -> new TalaromycesMinusItem(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 1));

    public static final RegistryObject<Item> lumina_devorans = BASEITEMS.register("lumina_devorans",
            () -> new NoctiluminaDevoransItem(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 1));

    public static final RegistryObject<Item> noctifly_agaric = BASEITEMS.register("noctifly_agaric",
            () -> new NoctiflyAgaricItem(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1));

    // Spores
//	public static final RegistryObject<Item> incandescent_spores = BASEITEMS.register("incandescent_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 1));
//
//	public static final RegistryObject<Item> fervent_spores = BASEITEMS.register("fervent_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 1));
//
//	public static final RegistryObject<Item> neurotic_spores = BASEITEMS.register("neurotic_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 1));
//
//	public static final RegistryObject<Item> ruinous_spores = BASEITEMS.register("ruinous_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 1));
//
//	public static final RegistryObject<Item> umbral_spores = BASEITEMS.register("umbral_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 1));
//
//	public static final RegistryObject<Item> frigid_spores = BASEITEMS.register("frigid_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 1));
//
//	public static final RegistryObject<Item> vivacious_spores = BASEITEMS.register("vivacious_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1));
//
//	public static final RegistryObject<Item> ferric_spores = BASEITEMS.register("ferric_spores",
//			() -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 1));

    // Fungal Scars

    public static final RegistryObject<Item> scar_transcendence = BASEITEMS.register("scar_transcendence",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 1, 1)
                    .withModifier(Attributes.KNOCKBACK_RESISTANCE, "scar_transcendence_kb", 0.1,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_transcendence = BASEITEMS.register(
            "scar_pattern_transcendence",
            () -> new ItemScarPattern(new Item.Properties(), scar_transcendence, "scar_transcendence"));

    public static final RegistryObject<Item> scar_sol = BASEITEMS.register("scar_sol",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 1, 1)
                    .withEffect(MobEffects.FIRE_RESISTANCE, 0));

    public static final RegistryObject<Item> scar_pattern_sol = BASEITEMS.register("scar_pattern_sol",
            () -> new ItemScarPattern(new Item.Properties(), scar_sol, "scar_sol"));

    public static final RegistryObject<Item> scar_heart = BASEITEMS.register("scar_heart",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 1, 1)
                    .withModifier(Attributes.MAX_HEALTH, "scar_heart_hp", 2.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_heart = BASEITEMS.register("scar_pattern_heart",
            () -> new ItemScarPattern(new Item.Properties(), scar_heart, "scar_heart"));

    public static final RegistryObject<Item> scar_descendence = BASEITEMS.register("scar_descendence",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 1, 1)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_descendence_ad", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_descendence = BASEITEMS.register("scar_pattern_descendence",
            () -> new ItemScarPattern(new Item.Properties(), scar_descendence, "scar_descendence"));

    public static final RegistryObject<Item> scar_moon = BASEITEMS.register("scar_moon",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 1, 1)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_moon_ms", 0.05,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_moon = BASEITEMS.register("scar_pattern_moon",
            () -> new ItemScarPattern(new Item.Properties(), scar_moon, "scar_moon"));

    public static final RegistryObject<Item> scar_eye = BASEITEMS.register("scar_eye",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 1, 1)
                    .withModifier(Attributes.LUCK, "scar_eye_luck", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_eye = BASEITEMS.register("scar_pattern_eye",
            () -> new ItemScarPattern(new Item.Properties(), scar_eye, "scar_eye"));

    public static final RegistryObject<Item> scar_feral = BASEITEMS.register("scar_feral",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 1, 1)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_feral_as", 0.05,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_feral = BASEITEMS.register("scar_pattern_feral",
            () -> new ItemScarPattern(new Item.Properties(), scar_feral, "scar_feral"));

    // Tier 1 Scars (fill remaining tendencies)

    public static final RegistryObject<Item> scar_thorn = BASEITEMS.register("scar_thorn",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 1, 1)
                    .withModifier(Attributes.ARMOR, "scar_thorn_armor", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_thorn = BASEITEMS.register("scar_pattern_thorn",
            () -> new ItemScarPattern(new Item.Properties(), scar_thorn, "scar_thorn"));

    public static final RegistryObject<Item> scar_shade = BASEITEMS.register("scar_shade",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 1, 1)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_shade_ms", 0.05,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_shade = BASEITEMS.register("scar_pattern_shade",
            () -> new ItemScarPattern(new Item.Properties(), scar_shade, "scar_shade"));

    // Tier 2 Scars (intermediate, one per tendency)

    public static final RegistryObject<Item> scar_pyre = BASEITEMS.register("scar_pyre",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 2, 2)
                    .withEffect(MobEffects.FIRE_RESISTANCE, 0)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_pyre_ad", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_pyre = BASEITEMS.register("scar_pattern_pyre",
            () -> new ItemScarPattern(new Item.Properties(), scar_pyre, "scar_pyre"));

    public static final RegistryObject<Item> scar_marrow = BASEITEMS.register("scar_marrow",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 2, 2)
                    .withModifier(Attributes.MAX_HEALTH, "scar_marrow_hp", 4.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_marrow = BASEITEMS.register("scar_pattern_marrow",
            () -> new ItemScarPattern(new Item.Properties(), scar_marrow, "scar_marrow"));

    public static final RegistryObject<Item> scar_blight = BASEITEMS.register("scar_blight",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 2, 2)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_blight_ad", 2.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_blight = BASEITEMS.register("scar_pattern_blight",
            () -> new ItemScarPattern(new Item.Properties(), scar_blight, "scar_blight"));

    public static final RegistryObject<Item> scar_rime = BASEITEMS.register("scar_rime",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 2, 2)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_rime_ms", 0.10,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_rime = BASEITEMS.register("scar_pattern_rime",
            () -> new ItemScarPattern(new Item.Properties(), scar_rime, "scar_rime"));

    public static final RegistryObject<Item> scar_flux = BASEITEMS.register("scar_flux",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 2, 2)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_flux_as", 0.10,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_flux = BASEITEMS.register("scar_pattern_flux",
            () -> new ItemScarPattern(new Item.Properties(), scar_flux, "scar_flux"));

    public static final RegistryObject<Item> scar_halo = BASEITEMS.register("scar_halo",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 2, 2)
                    .withModifier(Attributes.KNOCKBACK_RESISTANCE, "scar_halo_kb", 0.2,
                            AttributeModifier.Operation.ADDITION)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_halo_at", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_halo = BASEITEMS.register("scar_pattern_halo",
            () -> new ItemScarPattern(new Item.Properties(), scar_halo, "scar_halo"));

    public static final RegistryObject<Item> scar_anvil = BASEITEMS.register("scar_anvil",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 2, 2)
                    .withModifier(Attributes.ARMOR, "scar_anvil_armor", 2.0,
                            AttributeModifier.Operation.ADDITION)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_anvil_at", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_anvil = BASEITEMS.register("scar_pattern_anvil",
            () -> new ItemScarPattern(new Item.Properties(), scar_anvil, "scar_anvil"));

    public static final RegistryObject<Item> scar_veil = BASEITEMS.register("scar_veil",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 2, 2)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_veil_ms", 0.10,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_veil = BASEITEMS.register("scar_pattern_veil",
            () -> new ItemScarPattern(new Item.Properties(), scar_veil, "scar_veil"));

    // Tier 3 Scars (advanced, one per tendency)

    public static final RegistryObject<Item> scar_phoenix = BASEITEMS.register("scar_phoenix",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FLAMMEUS, 3, 3)
                    .withEffect(MobEffects.FIRE_RESISTANCE, 0)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_phoenix_ad", 2.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_phoenix = BASEITEMS.register("scar_pattern_phoenix",
            () -> new ItemScarPattern(new Item.Properties(), scar_phoenix, "scar_phoenix"));

    public static final RegistryObject<Item> scar_ichor = BASEITEMS.register("scar_ichor",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.ANIMUS, 3, 3)
                    .withModifier(Attributes.MAX_HEALTH, "scar_ichor_hp", 6.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_ichor = BASEITEMS.register("scar_pattern_ichor",
            () -> new ItemScarPattern(new Item.Properties(), scar_ichor, "scar_ichor"));

    public static final RegistryObject<Item> scar_wither = BASEITEMS.register("scar_wither",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.MORTEM, 3, 3)
                    .withModifier(Attributes.ATTACK_DAMAGE, "scar_wither_ad", 3.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_wither = BASEITEMS.register("scar_pattern_wither",
            () -> new ItemScarPattern(new Item.Properties(), scar_wither, "scar_wither"));

    public static final RegistryObject<Item> scar_glacier = BASEITEMS.register("scar_glacier",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.CONGEATIO, 3, 3)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_glacier_ms", 0.15,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_glacier = BASEITEMS.register("scar_pattern_glacier",
            () -> new ItemScarPattern(new Item.Properties(), scar_glacier, "scar_glacier"));

    public static final RegistryObject<Item> scar_chimera = BASEITEMS.register("scar_chimera",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.DUCTILIS, 3, 3)
                    .withModifier(Attributes.ATTACK_SPEED, "scar_chimera_as", 0.15,
                            AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .withModifier(Attributes.LUCK, "scar_chimera_luck", 1.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_chimera = BASEITEMS.register("scar_pattern_chimera",
            () -> new ItemScarPattern(new Item.Properties(), scar_chimera, "scar_chimera"));

    public static final RegistryObject<Item> scar_corona = BASEITEMS.register("scar_corona",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.LUX, 3, 3)
                    .withModifier(Attributes.KNOCKBACK_RESISTANCE, "scar_corona_kb", 0.3,
                            AttributeModifier.Operation.ADDITION)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_corona_at", 2.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_corona = BASEITEMS.register("scar_pattern_corona",
            () -> new ItemScarPattern(new Item.Properties(), scar_corona, "scar_corona"));

    public static final RegistryObject<Item> scar_crucible = BASEITEMS.register("scar_crucible",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.FERRIC, 3, 3)
                    .withModifier(Attributes.ARMOR, "scar_crucible_armor", 3.0,
                            AttributeModifier.Operation.ADDITION)
                    .withModifier(Attributes.ARMOR_TOUGHNESS, "scar_crucible_at", 2.0,
                            AttributeModifier.Operation.ADDITION));

    public static final RegistryObject<Item> scar_pattern_crucible = BASEITEMS.register("scar_pattern_crucible",
            () -> new ItemScarPattern(new Item.Properties(), scar_crucible, "scar_crucible"));

    public static final RegistryObject<Item> scar_oblivion = BASEITEMS.register("scar_oblivion",
            () -> new ItemScar(new Item.Properties().stacksTo(1), EnumBloodTendency.TENEBRIS, 3, 3)
                    .withModifier(Attributes.MOVEMENT_SPEED, "scar_oblivion_ms", 0.15,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<Item> scar_pattern_oblivion = BASEITEMS.register("scar_pattern_oblivion",
            () -> new ItemScarPattern(new Item.Properties(), scar_oblivion, "scar_oblivion"));

    // Spawn Eggs
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_leech = SPAWNEGGS.register("spawn_egg_leech",
            () -> new ForgeSpawnEggItem(EntityInit.leech, 7761777, 4206080, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_fargone = SPAWNEGGS.register("spawn_egg_fargone",
            () -> new ForgeSpawnEggItem(EntityInit.fargone, 7352833, 7958646, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_thirster = SPAWNEGGS.register("spawn_egg_thirster",
            () -> new ForgeSpawnEggItem(EntityInit.thirster, 3093151, 9515521, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_fungling = SPAWNEGGS.register("spawn_egg_fungling",
            () -> new ForgeSpawnEggItem(EntityInit.fungling, 7798794, 15711418, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_eruptor = SPAWNEGGS.register("spawn_egg_eruptor",
            () -> new ForgeSpawnEggItem(EntityInit.erythromycelium_eruptus, 7798794, 12235264, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_chitinite = SPAWNEGGS.register(
            "spawn_egg_chitinite",
            () -> new ForgeSpawnEggItem(EntityInit.chitinite, 3617335, 8553354, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_fervent_chitinite = SPAWNEGGS.register(
            "spawn_egg_fervent_chitinite",
            () -> new ForgeSpawnEggItem(EntityInit.fervent_chitinite, 3617335, 12124160, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_chthonian = SPAWNEGGS.register(
            "spawn_egg_chthonian",
            () -> new ForgeSpawnEggItem(EntityInit.chthonian, 7488841, 2170666, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_chthonian_queen = SPAWNEGGS.register(
            "spawn_egg_chthonian_queen",
            () -> new ForgeSpawnEggItem(EntityInit.chthonian_queen, 7488841, 12235264, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_lump_of_thought = SPAWNEGGS.register(
            "spawn_egg_lump_of_thought",
            () -> new ForgeSpawnEggItem(EntityInit.lump_of_thought, 6094848, 11315361, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_abhorent_thought = SPAWNEGGS.register(
            "spawn_egg_abhorent_thought",
            () -> new ForgeSpawnEggItem(EntityInit.abhorent_thought, 12124160, 4259840, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_barbed_urchin = SPAWNEGGS.register(
            "spawn_egg_barbed_urchin",
            () -> new ForgeSpawnEggItem(EntityInit.barbed_urchin, 12124160, 4259840, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_morphling_polyp = SPAWNEGGS.register(
            "spawn_egg_morphling_polyp",
            () -> new ForgeSpawnEggItem(EntityInit.morphling_polyp, 6881280, 0, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_blood_drunk_puppeteer = SPAWNEGGS.register(
            "spawn_egg_blood_drunk_puppeteer",
            () -> new ForgeSpawnEggItem(EntityInit.blood_drunk_puppeteer, 12124160, 12152064, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_enthralled_doll = SPAWNEGGS.register(
            "spawn_egg_enthralled_doll",
            () -> new ForgeSpawnEggItem(EntityInit.enthralled_doll, 12124160, 12564912, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_hemolymphopoda = SPAWNEGGS.register(
            "spawn_egg_hemolymphopoda",
            () -> new ForgeSpawnEggItem(EntityInit.hemolymphopoda, 6579558, 4875998, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_dessicant = SPAWNEGGS.register(
            "spawn_egg_dessicant",
            () -> new ForgeSpawnEggItem(EntityInit.dessicant, 0xC2A66E, 0x8B1A1A, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_cruor_fiend = SPAWNEGGS.register(
            "spawn_egg_cruor_fiend",
            () -> new ForgeSpawnEggItem(EntityInit.cruor_fiend, 0x4A0000, 0xFF4500, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_void_drinker = SPAWNEGGS.register(
            "spawn_egg_void_drinker",
            () -> new ForgeSpawnEggItem(EntityInit.void_drinker, 0x1A0033, 0x6A0DAD, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_frozen_clot = SPAWNEGGS.register(
            "spawn_egg_frozen_clot",
            () -> new ForgeSpawnEggItem(EntityInit.frozen_clot, 0xA8D8EA, 0x5C0000, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_abyssal_siphon = SPAWNEGGS.register(
            "spawn_egg_abyssal_siphon",
            () -> new ForgeSpawnEggItem(EntityInit.abyssal_siphon, 0x0D0D0D, 0x2D0037, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_synapse_hound = SPAWNEGGS.register(
            "spawn_egg_synapse_hound",
            () -> new ForgeSpawnEggItem(EntityInit.synapse_hound, 0x7DF9FF, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_myelin_borer = SPAWNEGGS.register(
            "spawn_egg_myelin_borer",
            () -> new ForgeSpawnEggItem(EntityInit.myelin_borer, 0xE8D8C0, 0x7DF9FF, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_crimson_doe = SPAWNEGGS.register(
            "spawn_egg_crimson_doe",
            () -> new ForgeSpawnEggItem(EntityInit.crimson_doe, 0xFAE6E6, 0x8B0000, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_hemojelly = SPAWNEGGS.register(
            "spawn_egg_hemojelly",
            () -> new ForgeSpawnEggItem(EntityInit.hemojelly, 0xFF6B8A, 0xCC2244, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> spawn_egg_venous_strider = SPAWNEGGS.register(
            "spawn_egg_venous_strider",
            () -> new ForgeSpawnEggItem(EntityInit.venous_strider, 0xF5DEB3, 0x8B4513, new Item.Properties()));

    // Item Property Override
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void itemPropOverrideClient(final FMLClientSetupEvent event) {

        ItemProperties.register(unsigned_ancestral_ledger.get(), Hemomancy.rloc("unsigned"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(bloody_vial.get(), Hemomancy.rloc("state"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(barbed_shield.get(), new ResourceLocation("blocking"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    return ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F;
                });

        ItemProperties.register(chitinite_shield.get(), new ResourceLocation("blocking"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    return ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F;
                });

        ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("pull"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    if (ent == null) {
                        return 0.0F;
                    } else {
                        return LivingCrossbowItem.isCharged(stack) ? 0.0F
                                : (float) (stack.getUseDuration() - ent.getUseItemRemainingTicks())
                                / (float) LivingCrossbowItem.getChargeTime(stack);
                    }
                });
        ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("pulling"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    return ent != null && ent.isUsingItem() && ent.getUseItem() == stack
                            && !LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
                });
        ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("charged"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    return stack != null && LivingCrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
                });
        ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("firework"),
                (ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
                    return ent != null && LivingCrossbowItem.isCharged(stack)
                            && LivingCrossbowItem.hasChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
                });

        ItemProperties.register(living_syringe.get(), Hemomancy.rloc("open"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(curved_horn.get(), Hemomancy.rloc("open"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(blood_gourd_white.get(), Hemomancy.rloc("open"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(blood_gourd_red.get(), Hemomancy.rloc("open"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(blood_gourd_black.get(), Hemomancy.rloc("open"),
                HemoItemProperties.booleanTag("state"));

        ItemProperties.register(morphling_jar.get(), Hemomancy.rloc("size"),
                HemoItemProperties.intTag("size"));

        ItemProperties.register(living_staff.get(), Hemomancy.rloc("morph"), new ItemPropertyFunction() {

            @Override
            public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
                if (stack.hasTag()) {
                    CompoundTag CompoundTag = stack.getOrCreateTag();
                    CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
                    if (items != null) {
                        if (items.contains("Items", 9)) {
                            @SuppressWarnings("static-access")
                            ItemStack selectedStack = ItemStack.of(((ListTag) items.get("Items")).getCompound(0));
                            if (selectedStack.getItem() == ItemInit.morphling_serpent.get()) {
                                return 1;
                            } else if (selectedStack.getItem() == ItemInit.morphling_leeches.get()) {
                                return 2;
                            } else if (selectedStack.getItem() == ItemInit.morphling_fungal.get()) {
                                return 3;
                            } else if (selectedStack.getItem() == ItemInit.morphling_pests.get()) {
                                return 4;
                            } else if (selectedStack.getItem() == ItemInit.morphling_chitinite.get()) {
                                return 5;
                            } else if (selectedStack.getItem() == ItemInit.morphling_spider.get()) {
                                return 6;
                            } else if (selectedStack.getItem() == ItemInit.morphling_moth.get()) {
                                return 7;
                            } else if (selectedStack.getItem() == ItemInit.morphling_tick.get()) {
                                return 8;
                            } else if (selectedStack.getItem() == ItemInit.morphling_centipede.get()) {
                                return 9;
                            } else if (selectedStack.getItem() == ItemInit.morphling_bat.get()) {
                                return 10;
                            } else if (selectedStack.getItem() == ItemInit.morphling_urchin.get()) {
                                return 11;
                            } else if (selectedStack.getItem() == ItemInit.morphling_mole.get()) {
                                return 12;
                            } else {
                                return 0;
                            }
                        }
                    } else {
                        return 0;

                    }

                } else {
                    return 0;

                }
                return 0;
            }
        });

    }

}
