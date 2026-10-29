package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Registered content agrees with itself: F-23, the tag files moved to the singular 1.21 folders are read, so their
 * items are in the tags; F-27, every Hemomancy effect's beneficial answer comes from its registered category.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class RegistrationConsistencyGameTests {

	private static final TagKey<Item> KNAPPERS = TagKey.create(Registries.ITEM,
			ResourceLocation.fromNamespaceAndPath("hutoslib", "knappers"));

	private RegistrationConsistencyGameTests() {
	}

	@GameTest(template = "empty")
	public static void singularFolderItemTagsAreLoaded(GameTestHelper helper) {
		assertTagged(helper, ItemTags.WALLS, "venous_stone_wall");
		assertTagged(helper, ItemTags.WALLS, "polished_venous_stone_brick_wall");
		assertTagged(helper, ItemTags.WALLS, "bone_brick_wall");
		assertTagged(helper, ItemTags.WALLS, "hematic_iron_plating_wall");
		assertTagged(helper, ItemTags.WALLS, "polished_venous_stone_wall");
		assertTagged(helper, ItemTags.SMALL_FLOWERS, "bleeding_heart");
		assertTagged(helper, KNAPPERS, "hematic_iron_knapper");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void everyEffectIsBeneficialExactlyWhenItsCategoryIs(GameTestHelper helper) {
		BuiltInRegistries.MOB_EFFECT.entrySet().stream()
				.filter(entry -> entry.getKey().location().getNamespace().equals(Hemomancy.MOD_ID))
				.forEach(entry -> {
					MobEffect effect = entry.getValue();
					helper.assertTrue(effect.isBeneficial() == (effect.getCategory() == MobEffectCategory.BENEFICIAL),
							entry.getKey().location() + " is registered " + effect.getCategory()
									+ " but answers isBeneficial " + effect.isBeneficial());
				});
		helper.assertTrue(BuiltInRegistries.MOB_EFFECT.get(Hemomancy.rloc("hemolysis")).isBeneficial(),
				"Hemolysis keeps reporting beneficial: the Lethean Chalice must not strip it");
		helper.succeed();
	}

	private static void assertTagged(GameTestHelper helper, TagKey<Item> tag, String item) {
		Item value = BuiltInRegistries.ITEM.get(Hemomancy.rloc(item));
		helper.assertTrue(value.builtInRegistryHolder().is(tag), item + " should be in #" + tag.location());
	}
}
