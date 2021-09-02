package com.vincenthuto.hemomancy.init;

import com.hutoslib.common.item.ItemKnapper;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.item.EnumBloodGourdTiers;
import com.vincenthuto.hemomancy.item.ItemBloodStainedStone;
import com.vincenthuto.hemomancy.item.ItemBloodVial;
import com.vincenthuto.hemomancy.item.ItemBloodyBook;
import com.vincenthuto.hemomancy.item.ItemBloodyFlask;
import com.vincenthuto.hemomancy.item.ItemDSD;
import com.vincenthuto.hemomancy.item.ItemEnzyme;
import com.vincenthuto.hemomancy.item.ItemRecycledEnzyme;
import com.vincenthuto.hemomancy.item.ItemTendencyBook;
import com.vincenthuto.hemomancy.item.ItemTendencyHiddenBook;
import com.vincenthuto.hemomancy.item.armor.ItemArmBanner;
import com.vincenthuto.hemomancy.item.armor.ItemChitiniteChest;
import com.vincenthuto.hemomancy.item.armor.ItemChitiniteHelmet;
import com.vincenthuto.hemomancy.item.armor.ItemSpikedShield;
import com.vincenthuto.hemomancy.item.memories.ItemBloodMemory;
import com.vincenthuto.hemomancy.item.memories.ItemHematicMemory;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingChitinite;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingFungal;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingLeech;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingPest;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingPolyp;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingSerpent;
import com.vincenthuto.hemomancy.item.rune.ItemContractRune;
import com.vincenthuto.hemomancy.item.rune.ItemGuidanceRune;
import com.vincenthuto.hemomancy.item.rune.ItemMilkweedRune;
import com.vincenthuto.hemomancy.item.rune.ItemRune;
import com.vincenthuto.hemomancy.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.item.rune.ItemSelfReflectionMirror;
import com.vincenthuto.hemomancy.item.rune.ItemVasculariumCharm;
import com.vincenthuto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.item.tool.EnumModArmorTiers;
import com.vincenthuto.hemomancy.item.tool.EnumModToolTiers;
import com.vincenthuto.hemomancy.item.tool.ItemBloodGourd;
import com.vincenthuto.hemomancy.item.tool.ItemDrudgeElectrode;
import com.vincenthuto.hemomancy.item.tool.living.ItemBloodAbsorption;
import com.vincenthuto.hemomancy.item.tool.living.ItemBloodBolt;
import com.vincenthuto.hemomancy.item.tool.living.ItemBloodProjection;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingAxe;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingBaghnakh;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingBlade;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingCrossbow;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingGrasp;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingSpear;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingStaff;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingSyringe;
import com.vincenthuto.hemomancy.recipe.ModChiselRecipes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ItemInit {
	public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> HANDHELDITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> SPECIALITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> SPAWNEGGS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);

	public static final DamageSource bloodLoss = new DamageSource("bloodloss");

	// Banners and Patterns

	public static final RegistryObject<Item> leather_arm_banner = SPECIALITEMS.register("leather_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), ArmorMaterials.LEATHER));
	public static final RegistryObject<Item> iron_arm_banner = SPECIALITEMS.register("iron_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), ArmorMaterials.IRON));
	public static final RegistryObject<Item> gold_arm_banner = SPECIALITEMS.register("gold_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), ArmorMaterials.GOLD));
	public static final RegistryObject<Item> diamond_arm_banner = SPECIALITEMS.register("diamond_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), ArmorMaterials.DIAMOND));
	public static final RegistryObject<Item> netherite_arm_banner = SPECIALITEMS.register("netherite_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), ArmorMaterials.NETHERITE));
	public static final RegistryObject<Item> chitinite_arm_banner = SPECIALITEMS.register("chitinite_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance),
					EnumModArmorTiers.CHITINITE));

	public static final RegistryObject<Item> heart_pattern = BASEITEMS.register("heart_pattern",
			() -> new BannerPatternItem(BannerTypeInit.heart, new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> veins_pattern = BASEITEMS.register("veins_pattern",
			() -> new BannerPatternItem(BannerTypeInit.veins, new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Charm
	public static final RegistryObject<Item> charm_of_vascularium = BASEITEMS.register("charm_of_vascularium",
			() -> new ItemVasculariumCharm(new Item.Properties().tab(HemomancyItemGroup.instance),
					EnumBloodTendency.ANIMUS, 0));

	// Book
	public static final RegistryObject<Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
			() -> new ItemBloodyBook(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> liber_inclinatio = SPECIALITEMS.register("liber_inclinatio",
			() -> new ItemTendencyBook(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> liber_inclinatio_hidden = SPECIALITEMS.register("liber_inclinatio_hidden",
			() -> new ItemTendencyHiddenBook(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));

	// Enzymes
	public static final RegistryObject<Item> vivacious_enzyme = BASEITEMS.register("vivacious_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.ANIMUS, 10));
	public static final RegistryObject<Item> ruinous_enzyme = BASEITEMS.register("ruinous_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.MORTEM, 10));
	public static final RegistryObject<Item> neurotic_enzyme = BASEITEMS.register("neurotic_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.DUCTILIS, 10));
	public static final RegistryObject<Item> ferric_enzyme = BASEITEMS.register("ferric_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.FERRIC, 10));
	public static final RegistryObject<Item> fervent_enzyme = BASEITEMS.register("fervent_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.FLAMMEUS, 10));
	public static final RegistryObject<Item> frigid_enzyme = BASEITEMS.register("frigid_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.CONGEATIO, 10));
	public static final RegistryObject<Item> incandescent_enzyme = BASEITEMS.register("incandescent_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.LUX, 10));
	public static final RegistryObject<Item> umbral_enzyme = BASEITEMS.register("umbral_enzyme",
			() -> new ItemEnzyme(EnumBloodTendency.TENEBRIS, 10));
	public static final RegistryObject<Item> recycled_enzyme = BASEITEMS.register("recycled_enzyme",
			() -> new ItemRecycledEnzyme());
	// Hematic Memories
	public static final RegistryObject<Item> hematic_memory = BASEITEMS.register("hematic_memory",
			() -> new ItemHematicMemory(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> memory_blood_absorption = BASEITEMS.register("memory_blood_absorption",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_blood_absorbtion));
	public static final RegistryObject<Item> memory_blood_projection = BASEITEMS.register("memory_blood_projection",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_blood_projection));
	public static final RegistryObject<Item> memory_blood_shot = BASEITEMS.register("memory_blood_shot",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_shot));
	public static final RegistryObject<Item> memory_blood_aneurysm = BASEITEMS.register("memory_blood_aneurysm",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.aneurysm));
	public static final RegistryObject<Item> memory_blood_rush = BASEITEMS.register("memory_blood_rush",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_rush));
	public static final RegistryObject<Item> memory_deadly_gaze = BASEITEMS.register("memory_deadly_gaze",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.deadly_gaze));
	public static final RegistryObject<Item> memory_blood_needle = BASEITEMS.register("memory_blood_needle",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_needle));
	public static final RegistryObject<Item> memory_blood_cloud = BASEITEMS.register("memory_blood_cloud",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_cloud));
	public static final RegistryObject<Item> memory_activation_potential = BASEITEMS.register(
			"memory_activation_potential",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.activation_potential));
	public static final RegistryObject<Item> memory_sanguine_ward = BASEITEMS.register("memory_sanguine_ward",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.sanguine_ward));
	public static final RegistryObject<Item> memory_living_blade = BASEITEMS.register("memory_living_blade",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_blade));
	public static final RegistryObject<Item> memory_living_axe = BASEITEMS.register("memory_living_axe",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_axe));
	public static final RegistryObject<Item> memory_living_spear = BASEITEMS.register("memory_living_spear",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_spear));
	public static final RegistryObject<Item> memory_living_claws = BASEITEMS.register("memory_living_claws",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_claws));
	public static final RegistryObject<Item> memory_living_crossbow = BASEITEMS.register("memory_living_crossbow",
			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.conjure_crossbow));

	// Living
	public static final RegistryObject<Item> blood_absorption = SPECIALITEMS.register("blood_absorption",
			() -> new ItemBloodAbsorption(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> blood_projection = SPECIALITEMS.register("blood_projection",
			() -> new ItemBloodProjection(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> living_syringe = SPECIALITEMS.register("living_syringe",
			() -> new ItemLivingSyringe(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_staff = SPECIALITEMS.register("living_staff",
			() -> new ItemLivingStaff(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_grasp = SPECIALITEMS.register("living_grasp",
			() -> new ItemLivingGrasp(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_blade = SPECIALITEMS.register("living_blade",
			() -> new ItemLivingBlade(25f, 3, EnumModToolTiers.LIVING,
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_axe = SPECIALITEMS.register("living_axe",
			() -> new ItemLivingAxe(25f, 1, EnumModToolTiers.LIVING,
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_spear = SPECIALITEMS.register("living_spear",
			() -> new ItemLivingSpear(25f, 1, EnumModToolTiers.LIVING,
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_baghnakh = SPECIALITEMS.register("living_baghnakh",
			() -> new ItemLivingBaghnakh(25f, 1, EnumModToolTiers.LIVING,
					new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> living_crossbow = SPECIALITEMS.register("living_crossbow",
			() -> new ItemLivingCrossbow(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> blood_bolt = BASEITEMS.register("blood_bolt",
			() -> new ItemBloodBolt(new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Morphlings
	public static final RegistryObject<Item> morphling_polyp = BASEITEMS.register("morphling_polyp",
			() -> new ItemMorphlingPolyp(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> morphling_jar = SPECIALITEMS.register("morphling_jar",
			() -> new ItemMorphlingJar("morphling_jar", 4, Rarity.UNCOMMON));
	public static final RegistryObject<Item> morphling_fungal = BASEITEMS.register("morphling_fungal",
			() -> new ItemMorphlingFungal(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_leeches = BASEITEMS.register("morphling_leeches",
			() -> new ItemMorphlingLeech(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_chitinite = BASEITEMS.register("morphling_chitinite",
			() -> new ItemMorphlingChitinite(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_serpent = BASEITEMS.register("morphling_serpent",
			() -> new ItemMorphlingSerpent(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_pests = BASEITEMS.register("morphling_pests",
			() -> new ItemMorphlingPest(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));

	/// Blood Gourds
	public static final RegistryObject<Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
			() -> new ItemBloodGourd(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.SIMPLE));
	public static final RegistryObject<Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
			() -> new ItemBloodGourd(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.CRIMSON));
	public static final RegistryObject<Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
			() -> new ItemBloodGourd(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.ASHEN));
	// Base Items
	public static final RegistryObject<Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> smouldering_ash = SPECIALITEMS.register("smouldering_ash",
			() -> new ItemNameBlockItem(BlockInit.smouldering_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> befouling_ash = SPECIALITEMS.register("befouling_ash",
			() -> new ItemNameBlockItem(BlockInit.befouling_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> active_smouldering_ash = SPECIALITEMS.register("active_smouldering_ash",
			() -> new ItemNameBlockItem(BlockInit.active_smouldering_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> active_befouling_ash = SPECIALITEMS.register("active_befouling_ash",
			() -> new ItemNameBlockItem(BlockInit.active_befouling_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> hematic_iron_scrap = BASEITEMS.register("hematic_iron_scrap",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> living_will = BASEITEMS.register("living_will",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> shred_of_animus = BASEITEMS.register("shred_of_animus",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> serpent_scale = BASEITEMS.register("serpent_scale",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> swollen_leech = BASEITEMS.register("swollen_leech",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> dried_leech = BASEITEMS.register("dried_leech",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> chitinous_husk = BASEITEMS.register("chitinous_husk",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> sanguine_conduit = BASEITEMS.register("sanguine_conduit",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> blood_stained_stone = BASEITEMS.register("blood_stained_stone",
			() -> new ItemBloodStainedStone(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> drudge_submission_device = BASEITEMS.register("drudge_submission_device",
			() -> new ItemDSD(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> raw_clay_flask = BASEITEMS.register("raw_clay_flask",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> cured_clay_flask = BASEITEMS.register("cured_clay_flask",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> bleeding_bulb = BASEITEMS.register("bleeding_bulb",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> dicentra_sap = BASEITEMS.register("dicentra_sap",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> bloody_vial = SPECIALITEMS.register("bloody_vial",
			() -> new ItemBloodVial(new Item.Properties().tab(HemomancyItemGroup.instance)));
	// Flasks
	public static final RegistryObject<Item> bloody_flask = BASEITEMS.register("bloody_flask",
			() -> new ItemBloodyFlask(new Item.Properties().tab(HemomancyItemGroup.instance), 250));
	public static final RegistryObject<Item> bloody_jug = BASEITEMS.register("bloody_jug",
			() -> new ItemBloodyFlask(new Item.Properties().tab(HemomancyItemGroup.instance), 2500));
	public static final RegistryObject<Item> stabilized_sanguine_formation = BASEITEMS.register(
			"stabilized_sanguine_formation",
			() -> new ItemBloodyFlask(new Item.Properties().tab(HemomancyItemGroup.instance), 5000));
	// Tools
	public static final RegistryObject<Item> iron_knapper = HANDHELDITEMS.register("iron_knapper",
			() -> new ItemKnapper(25f, 1, 0, Tiers.IRON, new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> obsidian_knapper = HANDHELDITEMS.register("obsidian_knapper",
			() -> new ItemKnapper(50f, 1, 0, EnumModToolTiers.LIVING,
					new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Equipment
	// Tainted
	public static final RegistryObject<Item> hematic_iron_helm = BASEITEMS.register("hematic_iron_helm",
			() -> new ArmorItem(EnumModArmorTiers.HEMATIC_IRON, EquipmentSlot.HEAD,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> hematic_iron_chestplate = BASEITEMS.register("hematic_iron_chestplate",
			() -> new ArmorItem(EnumModArmorTiers.HEMATIC_IRON, EquipmentSlot.CHEST,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> hematic_iron_leggings = BASEITEMS.register("hematic_iron_leggings",
			() -> new ArmorItem(EnumModArmorTiers.HEMATIC_IRON, EquipmentSlot.LEGS,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> hematic_iron_boots = BASEITEMS.register("hematic_iron_boots",
			() -> new ArmorItem(EnumModArmorTiers.HEMATIC_IRON, EquipmentSlot.FEET,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> drudge_electrode = SPECIALITEMS.register("drudge_electrode",
			() -> new ItemDrudgeElectrode(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> hematic_iron_sword = HANDHELDITEMS.register("hematic_iron_sword",
			() -> new SwordItem(EnumModToolTiers.HEMATIC_IRON, 3, -2.4F,
					new Item.Properties().tab(HemomancyItemGroup.instance)));
	// Shield
	public static final RegistryObject<Item> spiked_shield = SPECIALITEMS.register("spiked_shield",
			() -> new ItemSpikedShield(new Item.Properties().tab(HemomancyItemGroup.instance)));
	// Chitinite
	public static final RegistryObject<Item> chitinite_helm = BASEITEMS.register("chitinite_helm",
			() -> new ItemChitiniteHelmet(EnumModArmorTiers.CHITINITEHELMET, EquipmentSlot.HEAD,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> chitinite_chestplate = BASEITEMS.register("chitinite_chestplate",
			() -> new ItemChitiniteChest(EnumModArmorTiers.CHITINITECHEST, EquipmentSlot.CHEST,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> chitinite_leggings = BASEITEMS.register("chitinite_leggings",
			() -> new ArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.LEGS,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));
	public static final RegistryObject<Item> chitinite_boots = BASEITEMS.register("chitinite_boots",
			() -> new ArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.FEET,
					(new Item.Properties()).tab(HemomancyItemGroup.instance).fireResistant()));

	// Runes
	public static final RegistryObject<Item> self_reflection_mirror = BASEITEMS.register("self_reflection_mirror",
			() -> new ItemSelfReflectionMirror(
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final RegistryObject<Item> mind_spike = BASEITEMS.register("mind_spike",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_blank = BASEITEMS.register("rune_blank",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> rune_pattern = BASEITEMS.register("rune_pattern",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> rune_binder = BASEITEMS.register("rune_binder",
			() -> new ItemRuneBinder("rune_binder", 18, Rarity.UNCOMMON));
	public static final RegistryObject<Item> rune_binder_upgraded = BASEITEMS.register("rune_binder_upgraded",
			() -> new ItemRuneBinder("rune_binder_upgraded", 27, Rarity.RARE));

	// Contract Runes
	public static final RegistryObject<Item> rune_beast_c = BASEITEMS.register("rune_beast_c",
			() -> new ItemContractRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.ANIMUS, 5));
	public static final RegistryObject<Item> rune_pattern_beast_c = BASEITEMS.register("rune_pattern_beast_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_beast_c,
					ModChiselRecipes.recipeBeastContract,
					"Beast Contract Rune, Unleash your inner beast, destroy your Ego and let the Id become all you are, a primal beast of pure strength."));
	public static final RegistryObject<Item> rune_corruption_c = BASEITEMS.register("rune_corruption_c",
			() -> new ItemContractRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 5));
	public static final RegistryObject<Item> rune_pattern_corruption_c = BASEITEMS.register("rune_pattern_corruption_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_corruption_c,
					ModChiselRecipes.recipeCorruptionContract,
					"Corruption Contract Rune, Corrupted by those who wished you the worst, your faith has become dark, your dreams have become nightmares!"));
	public static final RegistryObject<Item> rune_impurity_c = BASEITEMS.register("rune_impurity_c",
			() -> new ItemContractRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.TENEBRIS, 5));
	public static final RegistryObject<Item> rune_pattern_impurity_c = BASEITEMS.register("rune_pattern_impurity_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_impurity_c,
					ModChiselRecipes.recipeImpurityContract,
					"Impurity Contract Rune, Your blood has become tainted and black, you hunger for all that is immoral and unclean..."));
	public static final RegistryObject<Item> rune_milkweed_c = BASEITEMS.register("rune_milkweed_c",
			() -> new ItemMilkweedRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.TENEBRIS, 5));
	public static final RegistryObject<Item> rune_pattern_milkweed_c = BASEITEMS.register("rune_pattern_milkweed_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_milkweed_c,
					ModChiselRecipes.recipeMilkweedContract,
					"Milkweed Contract Rune, Gain contact with the outer beings from beyond all known realms, learn from them, if you dare..."));
	public static final RegistryObject<Item> rune_radiance_c = BASEITEMS.register("rune_radiance_c",
			() -> new ItemContractRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.LUX, 5));
	public static final RegistryObject<Item> rune_pattern_radiance_c = BASEITEMS.register("rune_pattern_radiance_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_radiance_c,
					ModChiselRecipes.recipeRadianceContract,
					"Radiance Contract Rune, Shining symbol of rioutous grace and fury, unleash your inner saint against all the heathens you face!"));
	public static final RegistryObject<Item> rune_hunter_c = BASEITEMS.register("rune_hunter_c",
			() -> new ItemContractRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 5));
	public static final RegistryObject<Item> rune_pattern_hunter_c = BASEITEMS.register("rune_pattern_hunter_c",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_hunter_c,
					ModChiselRecipes.recipeHunterContract,
					"Hunter Contract Rune, A trained killer, set in your ways, a true hunter of hunters, weapons are your muse, the battle has become your muse."));

	// Base Runes
	public static final RegistryObject<Item> rune_metamorphosis = BASEITEMS.register("rune_metamorphosis",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_metamorphosis = BASEITEMS.register(
			"rune_pattern_metamorphosis",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_metamorphosis,
					ModChiselRecipes.recipeMetamorphosis,
					"Anti Metamorphosis Rune, The crooked metamorphosis has begun, your vitatlity has increased, you feel like you could regrow anything!"));

	public static final RegistryObject<Item> rune_metamorphosis_cw = BASEITEMS.register("rune_metamorphosis_cw",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_metamorphosis_cw = BASEITEMS.register(
			"rune_pattern_metamorphosis_cw",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_metamorphosis_cw,
					ModChiselRecipes.recipeMetamorphosisCW,
					"Metamorphois Rune, The proper metamorphosis has begun, your vitality has increased, you feel full of life anew!"));

	public static final RegistryObject<Item> rune_lake = BASEITEMS.register("rune_lake",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_lake = BASEITEMS.register("rune_pattern_lake",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_lake,
					ModChiselRecipes.recipeLake,
					"Lake Rune, The rushing of sacred waters, the roar of a great ocean calls to you, you know your home, and it beckons you back.."));
	public static final RegistryObject<Item> rune_clawmark = BASEITEMS.register("rune_clawmark",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.ANIMUS, 1));
	public static final RegistryObject<Item> rune_pattern_clawmark = BASEITEMS.register("rune_pattern_clawmark",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_clawmark,
					ModChiselRecipes.recipeClawMark,
					"Clawmark Rune, Tooth and Nail is all you know, the endless fight has just begun, bring the armies home, they are no threat."));
	public static final RegistryObject<Item> rune_rapture = BASEITEMS.register("rune_rapture",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_rapture = BASEITEMS.register("rune_pattern_rapture",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_rapture,
					ModChiselRecipes.recipeRapture,
					"Rapture Rune, The blood of war is your fuel, the lifeblood of your enemy becomes your own, absorb those who would oppose you!"));
	public static final RegistryObject<Item> rune_oedon = BASEITEMS.register("rune_oedon",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_oedon = BASEITEMS.register("rune_pattern_oedon",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_oedon,
					ModChiselRecipes.recipeOedon,
					"Oedon Rune, You feel as if you have a false heart, one granted to you on contract, nevertheless this life is yours now, use it."));
	public static final RegistryObject<Item> rune_eye = BASEITEMS.register("rune_eye",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_eye = BASEITEMS.register("rune_pattern_eye",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_eye,
					ModChiselRecipes.recipeEye,
					"Eye Rune, Your eyes, o' glorious eyes! do not faulter, do not die, for you shall see, beyond the veil, to see what beyond the darkness entails..."));
	public static final RegistryObject<Item> rune_moon = BASEITEMS.register("rune_moon",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_moon = BASEITEMS.register("rune_pattern_moon",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_moon,
					ModChiselRecipes.recipeMoon,
					"Moon Rune, The great moon above has become your muse, it is what drives you, turn away from the cursed sun, carpe noctum!"));
	public static final RegistryObject<Item> rune_beast = BASEITEMS.register("rune_beast",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.ANIMUS, 1));
	public static final RegistryObject<Item> rune_pattern_beast = BASEITEMS.register("rune_pattern_beast",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_beast,
					ModChiselRecipes.recipeBeast,
					"Beast Rune, The howl of a gale, the howl of the beast, it is one and all in you, run like the wind, endless and with ferver!"));
	public static final RegistryObject<Item> rune_heir = BASEITEMS.register("rune_heir",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_heir = BASEITEMS.register("rune_pattern_heir",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_heir,
					ModChiselRecipes.recipeHeir,
					"Heir Rune, you develop this watchful sense,you, the spiritual succesor to a once great power, shall rekindle their former glory; Divine Heir."));
	public static final RegistryObject<Item> rune_guidance = BASEITEMS.register("rune_guidance",
			() -> new ItemGuidanceRune(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_guidance = BASEITEMS.register("rune_pattern_guidance",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_guidance,
					ModChiselRecipes.recipeGuidance,
					"Guidance Rune, The supernatural blessing, one of fortune and direction, may it guide you to your wants, as it has so many others before."));
	public static final RegistryObject<Item> rune_communion = BASEITEMS.register("rune_communion",
			() -> new ItemRune(new Item.Properties().tab(HemomancyItemGroup.instance), EnumBloodTendency.LUX, 1));
	public static final RegistryObject<Item> rune_pattern_communion = BASEITEMS.register("rune_pattern_communion",
			() -> new ItemRunePattern(new Item.Properties().tab(HemomancyItemGroup.instance), rune_communion,
					ModChiselRecipes.recipeCommunion,
					"Communion Rune, The call from beyond rings louder for you, you wish to join them, you must join them. join.joi,jo..."));

	// Spawn Eggs

//	public static final RegistryObject<SpawnEggItem> spawn_egg_leech = SPAWNEGGS.register("spawn_egg_leech",
//			() -> new SpawnEggItem(EntityInit.leech, 7761777, 4206080,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));

//	public static final RegistryObject<SpawnEggItem> spawn_egg_fargone = SPAWNEGGS.register("spawn_egg_fargone",
//			() -> new SpawnEggItem(EntityInit.fargone, 7352833, 7958646,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//
//	public static final RegistryObject<SpawnEggItem> spawn_egg_thirster = SPAWNEGGS.register("spawn_egg_thirster",
//			() -> new SpawnEggItem(EntityInit.thirster, 3093151, 9515521,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//
//	public static final RegistryObject<SpawnEggItem> spawn_egg_drudge = SPAWNEGGS.register("spawn_egg_drudge",
//			() -> new SpawnEggItem(EntityInit.drudge, 8718848, 9515521,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//
//	public static final RegistryObject<SpawnEggItem> spawn_egg_fungling = SPAWNEGGS.register("spawn_egg_fungling",
//			() -> new SpawnEggItem(EntityInit.fungling, 7798794, 15711418,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//
//	public static final RegistryObject<SpawnEggItem> spawn_egg_chitinite = SPAWNEGGS.register("spawn_egg_chitinite",
//			() -> new SpawnEggItem(EntityInit.chitinite, 3617335, 8553354,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//	public static final RegistryObject<SpawnEggItem> spawn_egg_chthonian = SPAWNEGGS.register("spawn_egg_chthonian",
//			() -> new SpawnEggItem(EntityInit.chthonian, 7488841, 2170666,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//	public static final RegistryObject<SpawnEggItem> spawn_egg_chthonian_queen = SPAWNEGGS
//			.register("spawn_egg_chthonian_queen", () -> new SpawnEggItem(EntityInit.chthonian_queen, 7488841, 12235264,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//	public static final RegistryObject<SpawnEggItem> spawn_egg_lump_of_thought = SPAWNEGGS
//			.register("spawn_egg_lump_of_thought", () -> new SpawnEggItem(EntityInit.lump_of_thought, 6094848, 11315361,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//	public static final RegistryObject<SpawnEggItem> spawn_egg_abhorent_thought = SPAWNEGGS
//			.register("spawn_egg_abhorent_thought", () -> new SpawnEggItem(EntityInit.abhorent_thought, 12124160,
//					4259840, new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));
//	public static final RegistryObject<SpawnEggItem> spawn_egg_morphling_polyp = SPAWNEGGS
//			.register("spawn_egg_morphling_polyp", () -> new SpawnEggItem(EntityInit.morphling_polyp, 6881280, 0,
//					new Item.Properties().tab(ItemGroup.TAB_MISC).tab(HemomancyItemGroup.instance)));

	/*
	 * @SubscribeEvent public static void
	 * registerItemColorHandlers(ColorHandlerEvent.Item event) {
	 * registerSpawnEggColorHandler(event.getItemColors(),
	 * HutosLibItemInit.spawn_test_mob); }
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @SafeVarargs public static void registerSpawnEggColorHandler(ItemColors
	 * colors, RegistryObject<ModSpawnEggItem>... spawnEggs) { for
	 * (RegistryObject<ModSpawnEggItem> spawnEgg : spawnEggs) {
	 * registerItemColorHandler(colors, (stack, tintIndex) ->
	 * spawnEgg.get().getColor(tintIndex), spawnEgg); } }
	 * 
	 * @SuppressWarnings("unchecked") public static void
	 * registerItemColorHandler(ItemColors colors, ItemColor itemColor,
	 * RegistryObject<ModSpawnEggItem>... items) { for
	 * (RegistryObject<ModSpawnEggItem> itemProvider : items) {
	 * colors.register(itemColor, itemProvider.get()); } }
	 */


	// Item Property Override
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void itemPropOverrideClient(final FMLClientSetupEvent event) {

		ItemProperties.register(bloody_vial.get(), new ResourceLocation(Hemomancy.MOD_ID, "state"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(spiked_shield.get(), new ResourceLocation("blocking"),
				(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
					return ent != null && ent.isUsingItem() && ent.getUseItem() == stack ? 1.0F : 0.0F;
				});

		ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("pull"),
				(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
					if (ent == null) {
						return 0.0F;
					} else {
						return ItemLivingCrossbow.isCharged(stack) ? 0.0F
								: (float) (stack.getUseDuration() - ent.getUseItemRemainingTicks())
										/ (float) ItemLivingCrossbow.getChargeTime(stack);
					}
				});
		ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("pulling"),
				(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
					return ent != null && ent.isUsingItem() && ent.getUseItem() == stack
							&& !ItemLivingCrossbow.isCharged(stack) ? 1.0F : 0.0F;
				});
		ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("charged"),
				(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
					return stack != null && ItemLivingCrossbow.isCharged(stack) ? 1.0F : 0.0F;
				});
		ItemProperties.register(ItemInit.living_crossbow.get(), new ResourceLocation("firework"),
				(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) -> {
					return ent != null && ItemLivingCrossbow.isCharged(stack)
							&& ItemLivingCrossbow.hasChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
				});
		

		ItemProperties.register(living_syringe.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(drudge_electrode.get(), new ResourceLocation(Hemomancy.MOD_ID, "mode"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("mode")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(living_blade.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(living_axe.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(living_spear.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(blood_gourd_white.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(blood_gourd_red.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(blood_gourd_black.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							if (stack.getTag().getBoolean("state")) {
								return 1;
							} else {
								return 0;
							}
						}
						return 0;
					}
				});

		ItemProperties.register(morphling_jar.get(), new ResourceLocation(Hemomancy.MOD_ID, "size"),
				new ItemPropertyFunction() {
					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							return stack.getTag().getInt("size");
						} else {
							return 0;
						}
					}
				});

		ItemProperties.register(living_staff.get(), new ResourceLocation(Hemomancy.MOD_ID, "morph"),
				new ItemPropertyFunction() {

					@Override
					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
						if (stack.hasTag()) {
							CompoundTag CompoundTag = stack.getOrCreateTag();
							CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
							if (items != null) {
								if (items.contains("Items", 9)) {
									@SuppressWarnings("static-access")
									ItemStack selectedStack = stack.of(((ListTag) items.get("Items")).getCompound(0));
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
