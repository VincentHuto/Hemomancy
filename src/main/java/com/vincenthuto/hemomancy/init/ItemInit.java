package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.item.BloodVialItem;
import com.vincenthuto.hemomancy.item.BloodyBookItem;
import com.vincenthuto.hemomancy.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.item.EnumBloodGourdTiers;
import com.vincenthuto.hemomancy.item.EnzymeItem;
import com.vincenthuto.hemomancy.item.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.item.armor.BarbedArmorItem;
import com.vincenthuto.hemomancy.item.armor.BarbedShieldItem;
import com.vincenthuto.hemomancy.item.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.item.armor.ChitiniteArmorItem;
import com.vincenthuto.hemomancy.item.armor.ChitiniteShieldItem;
import com.vincenthuto.hemomancy.item.armor.EnumModArmorTiers;
import com.vincenthuto.hemomancy.item.armor.UnstainedArmorItem;
import com.vincenthuto.hemomancy.item.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.item.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.item.memories.BloodStainedStoneItem;
import com.vincenthuto.hemomancy.item.memories.FerventHuskItem;
import com.vincenthuto.hemomancy.item.memories.HematicMemoryItem;
import com.vincenthuto.hemomancy.item.memories.LethianBrewItem;
import com.vincenthuto.hemomancy.item.memories.LethianDewItem;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingPolyp;
import com.vincenthuto.hemomancy.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.item.tool.CurvedHornItem;
import com.vincenthuto.hemomancy.item.tool.EnumModToolTiers;
import com.vincenthuto.hemomancy.item.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.item.tool.living.BloodBoltItem;
import com.vincenthuto.hemomancy.item.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.item.tool.living.LivingCrossbowItem;
import com.vincenthuto.hemomancy.item.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.item.tool.living.LivingSyringeItem;
import com.vincenthuto.hemomancy.item.tool.living.VeinRecallerItem;
import com.vincenthuto.hemomancy.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import com.vincenthuto.hutoslib.common.item.ModSpawnEggItem;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
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
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
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
	public static final DeferredRegister<Item> HANDHELDITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> SPECIALITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<Item> SPAWNEGGS = DeferredRegister.create(ForgeRegistries.ITEMS,
			Hemomancy.MOD_ID);

	public static final DamageSource bloodLoss = new DamageSource("bloodloss");

	public static final RegistryObject<Item> chitinite_arm_banner = SPECIALITEMS.register("chitinite_arm_banner",
			() -> new ItemArmBanner(new Item.Properties().tab(HemomancyItemGroup.instance), EnumModArmorTiers.CHITINITE,
					new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/arm_banner/chitinite_arm_banner.png")));

	public static final RegistryObject<Item> heart_pattern = BASEITEMS.register("heart_pattern",
			() -> new BannerPatternItem(BannerTypeInit.heart, new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> veins_pattern = BASEITEMS.register("veins_pattern",
			() -> new BannerPatternItem(BannerTypeInit.veins, new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Charm
	public static final RegistryObject<Item> charm_of_vascularium = BASEITEMS.register("charm_of_vascularium",
			() -> new VasculariumCharmItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					EnumBloodTendency.ANIMUS, 0));

	// Book

	public static final RegistryObject<Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
			() -> new BloodyBookItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/liber_sanguinum.png")));
	public static final RegistryObject<Item> unsigned_ancestral_ledger = BASEITEMS.register("unsigned_ancestral_ledger",
			() -> new UnsignedLedgerItem(new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Base Items
	public static final RegistryObject<Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> foul_paste = BASEITEMS.register("foul_paste",
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
	public static final RegistryObject<Item> hematic_iron_powder = BASEITEMS.register("hematic_iron_powder",
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
	public static final RegistryObject<Item> scrying_dish = BASEITEMS.register("scrying_dish",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> blood_stained_stone = BASEITEMS.register("blood_stained_stone",
			() -> new BloodStainedStoneItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> bleeding_bulb = BASEITEMS.register("bleeding_bulb",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> dicentra_sap = BASEITEMS.register("dicentra_sap",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> living_stent = BASEITEMS.register("living_stent",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> puppeteering_thread = BASEITEMS.register("puppeteering_thread",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> blood_crystal_shard = BASEITEMS.register("blood_crystal_shard",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Anti blood
	public static final RegistryObject<Item> hemolytic_solution = BASEITEMS.register("hemolytic_solution",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> hemolytic_plating = BASEITEMS.register("hemolytic_plating",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> neutralizing_gasket = BASEITEMS.register("neutralizing_gasket",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));

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

	// Hematic Memories
	public static final RegistryObject<Item> lethian_dew = BASEITEMS.register("lethian_dew",
			() -> new LethianDewItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> lethian_brew = BASEITEMS.register("lethian_brew",
			() -> new LethianBrewItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> fervent_husk = BASEITEMS.register("fervent_husk",
			() -> new FerventHuskItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> hematic_memory = BASEITEMS.register("hematic_memory",
			() -> new HematicMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> memory_blood_absorption = BASEITEMS.register("memory_blood_absorption",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_absorption));
	public static final RegistryObject<Item> memory_blood_projection = BASEITEMS.register("memory_blood_projection",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_projection));
	public static final RegistryObject<Item> memory_venous_travel = BASEITEMS.register("memory_venous_travel",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.venous_travel));
	public static final RegistryObject<Item> memory_blood_shot = BASEITEMS.register("memory_blood_shot",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_shot));
	public static final RegistryObject<Item> memory_blood_aneurysm = BASEITEMS.register("memory_blood_aneurysm",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_aneurysm));
	public static final RegistryObject<Item> memory_blood_rush = BASEITEMS.register("memory_blood_rush",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_rush));
	public static final RegistryObject<Item> memory_deadly_gaze = BASEITEMS.register("memory_deadly_gaze",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.deadly_gaze));
	public static final RegistryObject<Item> memory_blood_needle = BASEITEMS.register("memory_blood_needle",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_needle));
	public static final RegistryObject<Item> memory_blood_cloud = BASEITEMS.register("memory_blood_cloud",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.blood_cloud));
	public static final RegistryObject<Item> memory_activation_potential = BASEITEMS.register(
			"memory_activation_potential",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.activation_potential));
	public static final RegistryObject<Item> memory_sanguine_ward = BASEITEMS.register("memory_sanguine_ward",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.sanguine_ward));
//	public static final RegistryObject<Item> memory_living_blade = BASEITEMS.register("memory_living_blade",
//			() -> new ItemBloodMemory(new Item.Properties().tab(HemomancyItemGroup.instance),
//					ManipulationInit.conjure_blade));
	public static final RegistryObject<Item> memory_summon_avatar = BASEITEMS.register("memory_summon_avatar",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.summon_avatar));
	public static final RegistryObject<Item> memory_ferric_transmutation = BASEITEMS.register(
			"memory_ferric_transmutation",
			() -> new BloodMemoryItem(new Item.Properties().tab(HemomancyItemGroup.instance),
					ManipulationInit.ferric_transmutation));
	// Living
	public static final RegistryObject<Item> blood_absorption = SPECIALITEMS.register("blood_absorption",
			() -> new BloodAbsorptionItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> blood_projection = SPECIALITEMS.register("blood_projection",
			() -> new BloodProjectionItem(new Item.Properties().tab(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> venous_recaller = SPECIALITEMS.register("venous_recaller",
			() -> new VeinRecallerItem(new Item.Properties().tab(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> living_syringe = SPECIALITEMS.register("living_syringe",
			() -> new LivingSyringeItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));

	public static final RegistryObject<Item> living_staff = SPECIALITEMS.register("living_staff",
			() -> new LivingStaffItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
//	public static final RegistryObject<Item> living_blade = SPECIALITEMS.register("living_blade",
//			() -> new ItemLivingBlade(25f, 3, EnumModToolTiers.LIVING,
//					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> living_crossbow = SPECIALITEMS.register("living_crossbow",
			() -> new LivingCrossbowItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> blood_bolt = BASEITEMS.register("blood_bolt",
			() -> new BloodBoltItem(new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Morphlings
	public static final RegistryObject<Item> morphling_polyp = BASEITEMS.register("morphling_polyp",
			() -> new ItemMorphlingPolyp(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> morphling_jar = SPECIALITEMS.register("morphling_jar",
			() -> new ItemMorphlingJar("morphling_jar", 4, Rarity.UNCOMMON));
	public static final RegistryObject<Item> morphling_fungal = BASEITEMS.register("morphling_fungal",
			() -> new MorphlingItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_leeches = BASEITEMS.register("morphling_leeches",
			() -> new MorphlingItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_chitinite = BASEITEMS.register("morphling_chitinite",
			() -> new MorphlingItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_serpent = BASEITEMS.register("morphling_serpent",
			() -> new MorphlingItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> morphling_pests = BASEITEMS.register("morphling_pests",
			() -> new MorphlingItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));

	/// Blood Gourds
	public static final RegistryObject<Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
			() -> new BloodGourdItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.SIMPLE));
	public static final RegistryObject<Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
			() -> new BloodGourdItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.CRIMSON));
	public static final RegistryObject<Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
			() -> new BloodGourdItem(new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1),
					EnumBloodGourdTiers.ASHEN));
	public static final RegistryObject<Item> curved_horn = SPECIALITEMS.register("curved_horn",
			() -> new CurvedHornItem(EnumBloodGourdTiers.HORN));

	// Flasks
	public static final RegistryObject<Item> bloody_flask = BASEITEMS.register("bloody_flask",
			() -> new BloodyFlaskItem(new Item.Properties().tab(HemomancyItemGroup.instance), 250));
	public static final RegistryObject<Item> bloody_jug = BASEITEMS.register("bloody_jug",
			() -> new BloodyFlaskItem(new Item.Properties().tab(HemomancyItemGroup.instance), 2500));
	public static final RegistryObject<Item> stabilized_sanguine_formation = BASEITEMS.register(
			"stabilized_sanguine_formation",
			() -> new BloodyFlaskItem(new Item.Properties().tab(HemomancyItemGroup.instance), 5000));
	public static final RegistryObject<Item> bloody_vial = SPECIALITEMS.register("bloody_vial",
			() -> new BloodVialItem(new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Equipment
	// Hematic
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
	public static final RegistryObject<Item> hematic_iron_sword = HANDHELDITEMS.register("hematic_iron_sword",
			() -> new SwordItem(EnumModToolTiers.HEMATIC_IRON, 3, -2.4F,
					new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> hematic_iron_knapper = HANDHELDITEMS.register("hematic_iron_knapper",
			() -> new ItemKnapper(42f, 1, 0, EnumModToolTiers.HEMATIC_IRON,
					new Item.Properties().tab(HemomancyItemGroup.instance)));

	// Blood Lust
	// Masks
	public static final RegistryObject<Item> tengu_mask = BASEITEMS.register("tengu_mask",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> horned_mask = BASEITEMS.register("horned_mask",
			() -> new Item(new Item.Properties().tab(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> blood_lust_helm = BASEITEMS.register("blood_lust_helm",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.HEAD, EnumBloodLustMaskTypes.NONE));
	public static final RegistryObject<Item> blood_lust_helm_tengu = BASEITEMS.register("blood_lust_helm_tengu",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.HEAD,
					EnumBloodLustMaskTypes.TENGU));
	public static final RegistryObject<Item> blood_lust_helm_horned = BASEITEMS.register("blood_lust_helm_horned",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.HEAD,
					EnumBloodLustMaskTypes.HORNED));
	public static final RegistryObject<Item> blood_lust_chest = BASEITEMS.register("blood_lust_chest",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.CHEST,
					EnumBloodLustMaskTypes.NONE));
	public static final RegistryObject<Item> blood_lust_legs = BASEITEMS.register("blood_lust_legs",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.LEGS, EnumBloodLustMaskTypes.NONE));
	public static final RegistryObject<Item> blood_lust_boots = BASEITEMS.register("blood_lust_boots",
			() -> new BloodLustArmorItem(EnumModArmorTiers.BLOODLUST, EquipmentSlot.FEET, EnumBloodLustMaskTypes.NONE));

	// Barbed/Barbed
	public static final RegistryObject<Item> barbed_blade = SPECIALITEMS.register("barbed_blade",
			() -> new SwordItem(EnumModToolTiers.LIVING, 3, 25f,
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> barbed_shield = SPECIALITEMS.register("barbed_shield",
			() -> new BarbedShieldItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> barbed_helm = BASEITEMS.register("barbed_helm",
			() -> new BarbedArmorItem(EnumModArmorTiers.BARBED, EquipmentSlot.HEAD));
	public static final RegistryObject<Item> barbed_chestplate = BASEITEMS.register("barbed_chestplate",
			() -> new BarbedArmorItem(EnumModArmorTiers.BARBED, EquipmentSlot.CHEST));
	public static final RegistryObject<Item> barbed_leggings = BASEITEMS.register("barbed_leggings",
			() -> new BarbedArmorItem(EnumModArmorTiers.BARBED, EquipmentSlot.LEGS));
	public static final RegistryObject<Item> barbed_boots = BASEITEMS.register("barbed_boots",
			() -> new BarbedArmorItem(EnumModArmorTiers.BARBED, EquipmentSlot.FEET));

	// Chitinite
	public static final RegistryObject<Item> chitinite_mace = SPECIALITEMS.register("chitinite_mace",
			() -> new SwordItem(EnumModToolTiers.LIVING, 3, 25f,
					new Item.Properties().tab(HemomancyItemGroup.instance).stacksTo(1)));
	public static final RegistryObject<Item> chitinite_shield = SPECIALITEMS.register("chitinite_shield",
			() -> new ChitiniteShieldItem(new Item.Properties().tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> chitinite_helm = BASEITEMS.register("chitinite_helm",
			() -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.HEAD));
	public static final RegistryObject<Item> chitinite_chestplate = BASEITEMS.register("chitinite_chestplate",
			() -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.CHEST));
	public static final RegistryObject<Item> chitinite_leggings = BASEITEMS.register("chitinite_leggings",
			() -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.LEGS));
	public static final RegistryObject<Item> chitinite_boots = BASEITEMS.register("chitinite_boots",
			() -> new ChitiniteArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlot.FEET));

	// Unstained
	public static final RegistryObject<Item> unstained_helm = BASEITEMS.register("unstained_helm",
			() -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, EquipmentSlot.HEAD));
	public static final RegistryObject<Item> unstained_chestplate = BASEITEMS.register("unstained_chestplate",
			() -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, EquipmentSlot.CHEST));
	public static final RegistryObject<Item> unstained_leggings = BASEITEMS.register("unstained_leggings",
			() -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, EquipmentSlot.LEGS));
	public static final RegistryObject<Item> unstained_boots = BASEITEMS.register("unstained_boots",
			() -> new UnstainedArmorItem(EnumModArmorTiers.UNSTAINED, EquipmentSlot.FEET));

	// Spawn Eggs
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_leech = SPAWNEGGS.register("spawn_egg_leech",
			() -> new ModSpawnEggItem(EntityInit.leech, 7761777, 4206080,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_fargone = SPAWNEGGS.register("spawn_egg_fargone",
			() -> new ModSpawnEggItem(EntityInit.fargone, 7352833, 7958646,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_thirster = SPAWNEGGS.register("spawn_egg_thirster",
			() -> new ModSpawnEggItem(EntityInit.thirster, 3093151, 9515521,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_fungling = SPAWNEGGS.register("spawn_egg_fungling",
			() -> new ModSpawnEggItem(EntityInit.fungling, 7798794, 15711418,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_chitinite = SPAWNEGGS.register("spawn_egg_chitinite",
			() -> new ModSpawnEggItem(EntityInit.chitinite, 3617335, 8553354,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_chthonian = SPAWNEGGS.register("spawn_egg_chthonian",
			() -> new ModSpawnEggItem(EntityInit.chthonian, 7488841, 2170666,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_chthonian_queen = SPAWNEGGS
			.register("spawn_egg_chthonian_queen", () -> new ModSpawnEggItem(EntityInit.chthonian_queen, 7488841,
					12235264, new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_lump_of_thought = SPAWNEGGS
			.register("spawn_egg_lump_of_thought", () -> new ModSpawnEggItem(EntityInit.lump_of_thought, 6094848,
					11315361, new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_abhorent_thought = SPAWNEGGS
			.register("spawn_egg_abhorent_thought", () -> new ModSpawnEggItem(EntityInit.abhorent_thought, 12124160,
					4259840, new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_morphling_polyp = SPAWNEGGS
			.register("spawn_egg_morphling_polyp", () -> new ModSpawnEggItem(EntityInit.morphling_polyp, 6881280, 0,
					new Item.Properties().tab(CreativeModeTab.TAB_MISC).tab(HemomancyItemGroup.instance)));

	@SubscribeEvent
	public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
		registerSpawnEggColorHandler(event.getItemColors(), ItemInit.spawn_egg_morphling_polyp,
				ItemInit.spawn_egg_abhorent_thought, ItemInit.spawn_egg_lump_of_thought,
				ItemInit.spawn_egg_chthonian_queen, ItemInit.spawn_egg_chthonian, ItemInit.spawn_egg_chitinite,
				ItemInit.spawn_egg_fungling, ItemInit.spawn_egg_thirster, ItemInit.spawn_egg_fargone,
				ItemInit.spawn_egg_leech);
	}

	@SuppressWarnings("unchecked")

	@SafeVarargs
	public static void registerSpawnEggColorHandler(ItemColors colors, RegistryObject<ModSpawnEggItem>... spawnEggs) {
		for (RegistryObject<ModSpawnEggItem> spawnEgg : spawnEggs) {
			registerItemColorHandler(colors, (stack, tintIndex) -> spawnEgg.get().getColor(tintIndex), spawnEgg);
		}
	}

	@SuppressWarnings("unchecked")
	public static void registerItemColorHandler(ItemColors colors, ItemColor itemColor,
			RegistryObject<ModSpawnEggItem>... items) {
		for (RegistryObject<ModSpawnEggItem> itemProvider : items) {
			colors.register(itemColor, itemProvider.get());
		}
	}

	// Item Property Override
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void itemPropOverrideClient(final FMLClientSetupEvent event) {

		ItemProperties.register(unsigned_ancestral_ledger.get(), new ResourceLocation(Hemomancy.MOD_ID, "unsigned"),
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

//		ItemProperties.register(living_blade.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
//				new ItemPropertyFunction() {
//					@Override
//					public float call(ItemStack stack, ClientLevel world, LivingEntity ent, int p_174679_) {
//						if (stack.hasTag()) {
//							if (stack.getTag().getBoolean("state")) {
//								return 1;
//							} else {
//								return 0;
//							}
//						}
//						return 0;
//					}
//				});

		ItemProperties.register(curved_horn.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
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
