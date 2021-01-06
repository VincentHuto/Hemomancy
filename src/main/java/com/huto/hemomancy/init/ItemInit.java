package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.item.ItemBloodyBook;
import com.huto.hemomancy.item.ModSpawnEggItem;
import com.huto.hemomancy.item.runes.ItemContractRune;
import com.huto.hemomancy.item.runes.ItemGuidanceRune;
import com.huto.hemomancy.item.runes.ItemMilkweedRune;
import com.huto.hemomancy.item.runes.ItemRune;
import com.huto.hemomancy.item.runes.ItemRuneBinder;
import com.huto.hemomancy.item.runes.ItemSelfReflectionMirror;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternBeast;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternBeastContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternClawmark;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternCommunion;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternCorruptionContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternEye;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternGuidance;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternHeir;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternHunterContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternImpurityContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternLake;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternMetamorphosis;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternMetamorphosisCW;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternMilkWeedContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternMoon;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternOedon;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternRadianceContract;
import com.huto.hemomancy.item.runes.patterns.ItemRunePatternRapture;
import com.huto.hemomancy.item.tool.ItemBloodGourd;
import com.huto.hemomancy.item.tool.ItemKnapper;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
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

	//Staff
	public static final RegistryObject<Item> living_staff = SPECIALITEMS.register("living_staff",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));
	
	// Book
	public static final RegistryObject<Item> liber_sanguinum = SPECIALITEMS.register("liber_sanguinum",
			() -> new ItemBloodyBook(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1)));

	/// Blood Gourds
	public static final RegistryObject<Item> blood_gourd_white = SPECIALITEMS.register("blood_gourd_white",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1), 500));
	public static final RegistryObject<Item> blood_gourd_red = SPECIALITEMS.register("blood_gourd_red",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1), 1500));
	public static final RegistryObject<Item> blood_gourd_black = SPECIALITEMS.register("blood_gourd_black",
			() -> new ItemBloodGourd(new Item.Properties().group(HemomancyItemGroup.instance).maxStackSize(1), 2500));
	// Base ItemsHemomancyItemGroup
	public static final RegistryObject<Item> sanguine_formation = BASEITEMS.register("sanguine_formation",
			() -> new Item(new Item.Properties().group(HemomancyItemGroup.instance)));

	public static final RegistryObject<Item> iron_knapper = HANDHELDITEMS.register("iron_knapper",
			() -> new ItemKnapper(25f, 1, 0, ItemTier.IRON, new Item.Properties().group(HemomancyItemGroup.instance)));
	public static final RegistryObject<Item> obsidian_knapper = HANDHELDITEMS.register("obsidian_knapper",
			() -> new ItemKnapper(50f, 1, 0, ItemTier.NETHERITE,
					new Item.Properties().group(HemomancyItemGroup.instance)));

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

	@SubscribeEvent
	public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
		registerSpawnEggColorHandler(event.getItemColors(), ItemInit.spawn_egg_leech);
	}

	// Item Property Override
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void itemPropOverrideClient(final FMLClientSetupEvent event) {
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
