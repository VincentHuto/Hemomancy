package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.item.EnumBloodGourdTiers;
import com.huto.hemomancy.item.ItemBloodyBook;
import com.huto.hemomancy.item.ItemDSD;
import com.huto.hemomancy.item.ItemParticleItem;
import com.huto.hemomancy.item.ItemTendencyBook;
import com.huto.hemomancy.item.ItemTendencyHiddenBook;
import com.huto.hemomancy.item.ModSpawnEggItem;
import com.huto.hemomancy.item.armor.ItemChitiniteChest;
import com.huto.hemomancy.item.armor.ItemChitiniteHelmet;
import com.huto.hemomancy.item.morphlings.ItemMorphlingFungal;
import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.huto.hemomancy.item.morphlings.ItemMorphlingLeech;
import com.huto.hemomancy.item.morphlings.ItemMorphlingPest;
import com.huto.hemomancy.item.morphlings.ItemMorphlingPolyp;
import com.huto.hemomancy.item.morphlings.ItemMorphlingSerpent;
import com.huto.hemomancy.item.morphlings.ItemMorphlingSymbiote;
import com.huto.hemomancy.item.rune.ItemContractRune;
import com.huto.hemomancy.item.rune.ItemGuidanceRune;
import com.huto.hemomancy.item.rune.ItemMilkweedRune;
import com.huto.hemomancy.item.rune.ItemRune;
import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.item.rune.ItemSelfReflectionMirror;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternBeast;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternBeastContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternClawmark;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternCommunion;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternCorruptionContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternEye;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternGuidance;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternHeir;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternHunterContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternImpurityContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternLake;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternMetamorphosis;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternMetamorphosisCW;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternMilkWeedContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternMoon;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternOedon;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternRadianceContract;
import com.huto.hemomancy.item.rune.pattern.ItemRunePatternRapture;
import com.huto.hemomancy.item.tool.EnumModArmorTiers;
import com.huto.hemomancy.item.tool.EnumModToolTiers;
import com.huto.hemomancy.item.tool.ItemBloodGourd;
import com.huto.hemomancy.item.tool.ItemDrudgeElectrode;
import com.huto.hemomancy.item.tool.ItemIronRod;
import com.huto.hemomancy.item.tool.ItemKnapper;
import com.huto.hemomancy.item.tool.ItemLivingGrasp;
import com.huto.hemomancy.item.tool.ItemLivingStaff;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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

	// Book
	public static final RegistryObject<Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
			() -> new ItemBloodyBook(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));
	public static final RegistryObject<Item> liber_inclinatio = SPECIALITEMS.register("liber_inclinatio",
			() -> new ItemTendencyBook(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));
	public static final RegistryObject<Item> liber_inclinatio_hidden = SPECIALITEMS.register("liber_inclinatio_hidden",
			() -> new ItemTendencyHiddenBook(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	// Living tools
	public static final RegistryObject<Item> living_staff = SPECIALITEMS.register("living_staff",
			() -> new ItemLivingStaff(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));
	public static final RegistryObject<Item> living_grasp = SPECIALITEMS.register("living_grasp",
			() -> new ItemLivingGrasp(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	// Morphlings
	public static final RegistryObject<Item> morphling_polyp = BASEITEMS.register("morphling_polyp",
			() -> new ItemMorphlingPolyp(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> morphling_jar = SPECIALITEMS.register("morphling_jar",
			() -> new ItemMorphlingJar("morphling_jar", 4, Rarity.UNCOMMON));
	public static final RegistryObject<Item> morphling_fungal = BASEITEMS.register("morphling_fungal",
			() -> new ItemMorphlingFungal(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					""));
	public static final RegistryObject<Item> morphling_leeches = BASEITEMS.register("morphling_leeches",
			() -> new ItemMorphlingLeech(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1), ""));
	public static final RegistryObject<Item> morphling_symbiote = BASEITEMS.register("morphling_symbiote",
			() -> new ItemMorphlingSymbiote(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					""));
	public static final RegistryObject<Item> morphling_serpent = BASEITEMS.register("morphling_serpent",
			() -> new ItemMorphlingSerpent(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					""));
	public static final RegistryObject<Item> morphling_pests = BASEITEMS.register("morphling_pests",
			() -> new ItemMorphlingPest(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1), ""));
	// Iron Rod
	public static final RegistryObject<Item> rod_of_exhortation = SPECIALITEMS.register("rod_of_exhortation",
			() -> new ItemIronRod(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	public static final RegistryObject<Item> rod_of_exhortation_pillar = SPECIALITEMS.register(
			"rod_of_exhortation_pillar",
			() -> new ItemIronRod(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));
	public static final RegistryObject<Item> rod_of_exhortation_spike = SPECIALITEMS.register(
			"rod_of_exhortation_spike",
			() -> new ItemIronRod(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	/// Blood Gourds
	public static final RegistryObject<Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodGourdTiers.SIMPLE));
	public static final RegistryObject<Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodGourdTiers.CRIMSON));
	public static final RegistryObject<Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodGourdTiers.ASHEN));
	// Base Items
	public static final RegistryObject<Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> smouldering_ash = SPECIALITEMS.register("smouldering_ash",
			() -> new BlockNamedItem(BlockInit.smouldering_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> befouling_ash = SPECIALITEMS.register("befouling_ash",
			() -> new BlockNamedItem(BlockInit.befouling_ash_trail.get(), (new Item.Properties())));
	public static final RegistryObject<Item> tainted_iron_scrap = BASEITEMS.register("tainted_iron_scrap",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> living_will = BASEITEMS.register("living_will",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> serpent_scale = BASEITEMS.register("serpent_scale",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> swollen_leech = BASEITEMS.register("swollen_leech",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> dried_leech = BASEITEMS.register("dried_leech",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> chitinous_husk = BASEITEMS.register("chitinous_husk",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> sanguine_conduit = BASEITEMS.register("sanguine_conduit",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> drudge_submission_device = BASEITEMS.register("drudge_submission_device",
			() -> new ItemDSD(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> particle_item = BASEITEMS.register("particle_item",
			() -> new ItemParticleItem(new Item.Properties().group(HemomancyItemGroup.instance)));

	// Tools
	public static final RegistryObject<Item> iron_knapper = HANDHELDITEMS.register("iron_knapper",
			() -> new ItemKnapper(25f, 1, 0, ItemTier.IRON, new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> obsidian_knapper = HANDHELDITEMS.register("obsidian_knapper",
			() -> new ItemKnapper(50f, 1, 0, ItemTier.NETHERITE,
					new Item.Properties().group(HemomancyItemGroup.instance)));
	// Equipment
	// Tainted
	public static final RegistryObject<Item> tainted_iron_helm = BASEITEMS.register("tainted_iron_helm",
			() -> new ArmorItem(EnumModArmorTiers.TAINTED_IRON, EquipmentSlotType.HEAD,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> tainted_iron_chestplate = BASEITEMS.register("tainted_iron_chestplate",
			() -> new ArmorItem(EnumModArmorTiers.TAINTED_IRON, EquipmentSlotType.CHEST,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> tainted_iron_leggings = BASEITEMS.register("tainted_iron_leggings",
			() -> new ArmorItem(EnumModArmorTiers.TAINTED_IRON, EquipmentSlotType.LEGS,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> tainted_iron_boots = BASEITEMS.register("tainted_iron_boots",
			() -> new ArmorItem(EnumModArmorTiers.TAINTED_IRON, EquipmentSlotType.FEET,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));

	public static final RegistryObject<Item> drudge_electrode = SPECIALITEMS.register("drudge_electrode",
			() -> new ItemDrudgeElectrode(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	public static final RegistryObject<Item> tainted_iron_sword = HANDHELDITEMS.register("tainted_iron_sword",
			() -> new SwordItem(EnumModToolTiers.TAINTED_IRON, 3, -2.4F,
					new Item.Properties().group(HemomancyItemGroup.instance)));

	// Chitinite
	public static final RegistryObject<Item> chitinite_helm = BASEITEMS.register("chitinite_helm",
			() -> new ItemChitiniteHelmet(EnumModArmorTiers.CHITINITEHELMET, EquipmentSlotType.HEAD,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> chitinite_chestplate = BASEITEMS.register("chitinite_chestplate",
			() -> new ItemChitiniteChest(EnumModArmorTiers.CHITINITECHEST, EquipmentSlotType.CHEST,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> chitinite_leggings = BASEITEMS.register("chitinite_leggings",
			() -> new ArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlotType.LEGS,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));
	public static final RegistryObject<Item> chitinite_boots = BASEITEMS.register("chitinite_boots",
			() -> new ArmorItem(EnumModArmorTiers.CHITINITE, EquipmentSlotType.FEET,
					(new Item.Properties()).group(HemomancyItemGroup.instance).isImmuneToFire()));

	// Runes
	public static final RegistryObject<Item> self_reflection_mirror = BASEITEMS.register("self_reflection_mirror",
			() -> new ItemSelfReflectionMirror(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1).rarity(Rarity.UNCOMMON)));
	public static final RegistryObject<Item> mind_spike = BASEITEMS.register("mind_spike",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_blank = BASEITEMS.register("rune_blank",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> rune_pattern = BASEITEMS.register("rune_pattern",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> rune_binder = BASEITEMS.register("rune_binder",
			() -> new ItemRuneBinder("rune_binder", 18, Rarity.UNCOMMON));
	public static final RegistryObject<Item> rune_binder_upgraded = BASEITEMS.register("rune_binder_upgraded",
			() -> new ItemRuneBinder("rune_binder_upgraded", 27, Rarity.RARE));

	// Contract Runes
	public static final RegistryObject<Item> rune_beast_c = BASEITEMS.register("rune_beast_c",
			() -> new ItemContractRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.ANIMUS, 5));
	public static final RegistryObject<Item> rune_pattern_beast_c = BASEITEMS.register("rune_pattern_beast_c",
			() -> new ItemRunePatternBeastContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Beast Contract Rune, Unleash your inner beast, destroy your Ego and let the Id become all you are, a primal beast of pure strength."));
	public static final RegistryObject<Item> rune_corruption_c = BASEITEMS.register("rune_corruption_c",
			() -> new ItemContractRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 5));
	public static final RegistryObject<Item> rune_pattern_corruption_c = BASEITEMS.register("rune_pattern_corruption_c",
			() -> new ItemRunePatternCorruptionContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Corruption Contract Rune, Corrupted by those who wished you the worst, your faith has become dark, your dreams have become nightmares!"));
	public static final RegistryObject<Item> rune_impurity_c = BASEITEMS.register("rune_impurity_c",
			() -> new ItemContractRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.TENEBRIS, 5));
	public static final RegistryObject<Item> rune_pattern_impurity_c = BASEITEMS.register("rune_pattern_impurity_c",
			() -> new ItemRunePatternImpurityContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Impurity Contract Rune, Your blood has become tainted and black, you hunger for all that is immoral and unclean..."));
	public static final RegistryObject<Item> rune_milkweed_c = BASEITEMS.register("rune_milkweed_c",
			() -> new ItemMilkweedRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.TENEBRIS, 5));
	public static final RegistryObject<Item> rune_pattern_milkweed_c = BASEITEMS.register("rune_pattern_milkweed_c",
			() -> new ItemRunePatternMilkWeedContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Milkweed Contract Rune, Gain contact with the outer beings from beyond all known realms, learn from them, if you dare..."));
	public static final RegistryObject<Item> rune_radiance_c = BASEITEMS.register("rune_radiance_c",
			() -> new ItemContractRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.LUX, 5));
	public static final RegistryObject<Item> rune_pattern_radiance_c = BASEITEMS.register("rune_pattern_radiance_c",
			() -> new ItemRunePatternRadianceContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Radiance Contract Rune, Shining symbol of rioutous grace and fury, unleash your inner saint against all the heathens you face!"));
	public static final RegistryObject<Item> rune_hunter_c = BASEITEMS.register("rune_hunter_c",
			() -> new ItemContractRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 5));
	public static final RegistryObject<Item> rune_pattern_hunter_c = BASEITEMS.register("rune_pattern_hunter_c",
			() -> new ItemRunePatternHunterContract(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Hunter Contract Rune, A trained killer, set in your ways, a true hunter of hunters, weapons are your muse, the battle has become your muse."));

	// Base Runes
	public static final RegistryObject<Item> rune_metamorphosis = BASEITEMS.register("rune_metamorphosis",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_metamorphosis = BASEITEMS.register(
			"rune_pattern_metamorphosis",
			() -> new ItemRunePatternMetamorphosis(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Anti Metamorphosis Rune, The crooked metamorphosis has begun, your vitatlity has increased, you feel like you could regrow anything!"));
	public static final RegistryObject<Item> rune_metamorphosis_cw = BASEITEMS.register("rune_metamorphosis_cw",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));

	public static final RegistryObject<Item> rune_pattern_metamorphosis_cw = BASEITEMS.register(
			"rune_pattern_metamorphosis_cw",
			() -> new ItemRunePatternMetamorphosisCW(
					new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Metamorphois Rune, The proper metamorphosis has begun, your vitality has increased, you feel full of life anew!"));
	public static final RegistryObject<Item> rune_lake = BASEITEMS.register("rune_lake",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_lake = BASEITEMS.register("rune_pattern_lake",
			() -> new ItemRunePatternLake(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Lake Rune, The rushing of sacred waters, the roar of a great ocean calls to you, you know your home, and it beckons you back.."));
	public static final RegistryObject<Item> rune_clawmark = BASEITEMS.register("rune_clawmark",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.ANIMUS, 1));
	public static final RegistryObject<Item> rune_pattern_clawmark = BASEITEMS.register("rune_pattern_clawmark",
			() -> new ItemRunePatternClawmark(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Clawmark Rune, Tooth and Nail is all you know, the endless fight has just begun, bring the armies home, they are no threat."));
	public static final RegistryObject<Item> rune_rapture = BASEITEMS.register("rune_rapture",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_rapture = BASEITEMS.register("rune_pattern_rapture",
			() -> new ItemRunePatternRapture(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Rapture Rune, The blood of war is your fuel, the lifeblood of your enemy becomes your own, absorb those who would oppose you!"));
	public static final RegistryObject<Item> rune_oedon = BASEITEMS.register("rune_oedon",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_oedon = BASEITEMS.register("rune_pattern_oedon",
			() -> new ItemRunePatternOedon(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Oedon Rune, You feel as if you have a false heart, one granted to you on contract, nevertheless this life is yours now, use it."));
	public static final RegistryObject<Item> rune_eye = BASEITEMS.register("rune_eye",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_eye = BASEITEMS.register("rune_pattern_eye",
			() -> new ItemRunePatternEye(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Eye Rune, Your eyes, o' glorious eyes! do not faulter, do not die, for you shall see, beyond the veil, to see what the darkness entails..."));
	public static final RegistryObject<Item> rune_moon = BASEITEMS.register("rune_moon",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_moon = BASEITEMS.register("rune_pattern_moon",
			() -> new ItemRunePatternMoon(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Moon Rune, The great moon above has become your muse, it is what drives you, turn away from the cursed sun, carpe noctum!"));
	public static final RegistryObject<Item> rune_beast = BASEITEMS.register("rune_beast",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.ANIMUS, 1));
	public static final RegistryObject<Item> rune_pattern_beast = BASEITEMS.register("rune_pattern_beast",
			() -> new ItemRunePatternBeast(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Beast Rune, The howl of a gale, the howl of the beast, it is one and all in you, run like the wind, endless and with drive!"));
	public static final RegistryObject<Item> rune_heir = BASEITEMS.register("rune_heir",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_heir = BASEITEMS.register("rune_pattern_heir",
			() -> new ItemRunePatternHeir(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Heir Rune, you develop this watchful sense,you, the spiritual succesor to a once great power, shall rekindle their former glory; Divine Heir."));
	public static final RegistryObject<Item> rune_guidance = BASEITEMS.register("rune_guidance",
			() -> new ItemGuidanceRune(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					EnumBloodTendency.DUCTILIS, 1));
	public static final RegistryObject<Item> rune_pattern_guidance = BASEITEMS.register("rune_pattern_guidance",
			() -> new ItemRunePatternGuidance(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Guidance Rune, The supernatural blessing, one of fortune and direction, may it guide you to your wants, as it has so many others before."));
	public static final RegistryObject<Item> rune_communion = BASEITEMS.register("rune_communion",
			() -> new ItemRune(new Item.Properties().group(HemomancyItemGroup.instance), EnumBloodTendency.LUX, 1));
	public static final RegistryObject<Item> rune_pattern_communion = BASEITEMS.register("rune_pattern_communion",
			() -> new ItemRunePatternCommunion(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1),
					"Communion Rune, The call from beyond rings louder for you, you wish to join them, you must join them. join.joi,jo..."));

	// Spawn Eggs

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_leech = SPAWNEGGS.register("spawn_egg_leech",
			() -> new ModSpawnEggItem(EntityInit.leech, 7761777, 4206080,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_fargone = SPAWNEGGS.register("spawn_egg_fargone",
			() -> new ModSpawnEggItem(EntityInit.fargone, 7352833, 7958646,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_thirster = SPAWNEGGS.register("spawn_egg_thirster",
			() -> new ModSpawnEggItem(EntityInit.thirster, 3093151, 9515521,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_drudge = SPAWNEGGS.register("spawn_egg_drudge",
			() -> new ModSpawnEggItem(EntityInit.drudge, 8718848, 9515521,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_fungling = SPAWNEGGS.register("spawn_egg_fungling",
			() -> new ModSpawnEggItem(EntityInit.fungling, 7798794, 15711418,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	public static final RegistryObject<ModSpawnEggItem> spawn_egg_chitinite = SPAWNEGGS.register("spawn_egg_chitinite",
			() -> new ModSpawnEggItem(EntityInit.chitinite, 3617335, 8553354,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));
	public static final RegistryObject<ModSpawnEggItem> spawn_egg_chthonian = SPAWNEGGS.register("spawn_egg_chthonian",
			() -> new ModSpawnEggItem(EntityInit.chthonian, 7488841, 2170666,
					new Item.Properties().group(ItemGroup.MISC).group(HemomancyItemGroup.instance)));

	@SubscribeEvent
	public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
		registerSpawnEggColorHandler(event.getItemColors(), ItemInit.spawn_egg_leech, ItemInit.spawn_egg_fargone,
				ItemInit.spawn_egg_thirster, ItemInit.spawn_egg_drudge, ItemInit.spawn_egg_fungling,
				ItemInit.spawn_egg_chitinite, ItemInit.spawn_egg_chthonian);
	}

	// Item Property Override
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void itemPropOverrideClient(final FMLClientSetupEvent event) {

		ItemModelsProperties.registerProperty(drudge_electrode.get(), new ResourceLocation(Hemomancy.MOD_ID, "mode"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
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

		ItemModelsProperties.registerProperty(blood_gourd_white.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
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

		ItemModelsProperties.registerProperty(blood_gourd_red.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
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

		ItemModelsProperties.registerProperty(blood_gourd_black.get(), new ResourceLocation(Hemomancy.MOD_ID, "open"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
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

		ItemModelsProperties.registerProperty(morphling_jar.get(), new ResourceLocation(Hemomancy.MOD_ID, "size"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
						if (stack.hasTag()) {
							return stack.getTag().getInt("size");
						} else {
							return 0;
						}
					}
				});

		ItemModelsProperties.registerProperty(living_staff.get(), new ResourceLocation(Hemomancy.MOD_ID, "morph"),
				new IItemPropertyGetter() {
					@Override
					public float call(ItemStack stack, ClientWorld world, LivingEntity ent) {
						if (stack.hasTag()) {
							CompoundNBT compoundnbt = stack.getOrCreateTag();
							CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
							if (items != null) {
								if (items.contains("Items", 9)) {
									@SuppressWarnings("static-access")
									ItemStack selectedStack = stack.read(((ListNBT) items.get("Items")).getCompound(0));
									if (selectedStack.getItem() == ItemInit.morphling_serpent.get()) {
										return 1;

									} else if (selectedStack.getItem() == ItemInit.morphling_leeches.get()) {
										return 2;

									} else if (selectedStack.getItem() == ItemInit.morphling_fungal.get()) {
										return 3;

									} else if (selectedStack.getItem() == ItemInit.morphling_pests.get()) {
										return 4;

									} else if (selectedStack.getItem() == ItemInit.morphling_symbiote.get()) {
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

	@SafeVarargs
	public static void registerSpawnEggColorHandler(ItemColors colors, RegistryObject<ModSpawnEggItem>... spawnEggs) {
		for (RegistryObject<ModSpawnEggItem> spawnEgg : spawnEggs) {
			registerItemColorHandler(colors, (stack, tintIndex) -> spawnEgg.get().getColor(tintIndex), spawnEgg);
		}
	}

	@SafeVarargs
	public static void registerItemColorHandler(ItemColors colors, IItemColor itemColor,
			RegistryObject<ModSpawnEggItem>... items) {
		for (RegistryObject<ModSpawnEggItem> itemProvider : items) {
			colors.register(itemColor, itemProvider.get());
		}
	}

}
