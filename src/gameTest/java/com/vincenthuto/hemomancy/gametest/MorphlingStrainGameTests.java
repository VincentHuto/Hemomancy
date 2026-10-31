package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.HouseMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MorphlingCradleBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Brined Court Track 1, loaded: the eight house strains register from the table, incubate from their partner
 * family's catalyst, keep their own passive while worn, come out of the Bestiary as their family's strain, and arm
 * only their table's Last Rite.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphlingStrainGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private MorphlingStrainGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "morphling_strains")
	public static void everyStrainRegistersAndIncubatesFromItsPartnerFamily(GameTestHelper helper) {
		var recipes = IncubatorRecipe.getAllRecipes(helper.getLevel());
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			var item = strain.item().get();
			helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).equals(Hemomancy.rloc(strain.itemPath())),
					strain.itemPath() + " is not registered under its table id");
			helper.assertTrue(item instanceof HouseMorphlingItem house && house.strain() == strain,
					strain.itemPath() + " does not read its own table row");
			helper.assertTrue(MorphlingStrains.of(new ItemStack(item)).orElse(null) == strain,
					strain.itemPath() + " stack lookup misses its row");
			var catalyst = BuiltInRegistries.ITEM.get(ResourceLocation.parse(strain.polypFamily().hintItemId()));
			long matching = recipes.stream()
					.filter(recipe -> recipe.getResultItemStack().is(item))
					.filter(recipe -> recipe.isValidCatalyst(new ItemStack(catalyst)))
					.count();
			helper.assertTrue(matching == 1, strain.itemPath() + " needs one incubation from "
					+ strain.polypFamily().hintItemId() + ", found " + matching);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "morphling_strains")
	public static void aWornStrainKeepsOnlyItsOwnPassive(GameTestHelper helper) {
		var player = DuctilisGameTests.player(helper);
		try {
			for (MorphlingStrain strain : MorphlingStrains.ALL) {
				ItemStack stack = new ItemStack(strain.item().get());
				((HouseMorphlingItem) stack.getItem()).onEquippedTick(player, stack);
				for (MorphlingStrain other : MorphlingStrains.ALL) {
					helper.assertTrue(player.hasEffect(other.passiveEffect()) == (other == strain),
							strain.id() + " worn but " + other.id() + " passive is "
									+ (player.hasEffect(other.passiveEffect()) ? "present" : "missing"));
				}
			}
			MorphlingItem.clearMorphlingPassiveEffects(player);
			helper.assertTrue(MorphlingStrains.ALL.stream().noneMatch(strain -> player.hasEffect(strain.passiveEffect())),
					"unequipping must clear every strain passive");
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "morphling_strains")
	public static void bestiaryFamiliesYieldTheirWildBoundStrain(GameTestHelper helper) {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			ItemStack wild = SpecimenBestiaryDefinitions.createWildBoundMorphling(strain.polypFamily());
			helper.assertTrue(wild.is(strain.item().get()), strain.polypFamily() + " yields " + wild);
			helper.assertTrue(MorphlingItem.isWildBound(wild), strain.id() + " surrender is not wild-bound");
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "morphling_strains")
	public static void lastRitesAndCradleFollowTheTable(GameTestHelper helper) {
		var player = DuctilisGameTests.player(helper);
		try {
			for (MorphlingStrain strain : MorphlingStrains.ALL) {
				ItemStack apex = withPower(new ItemStack(strain.item().get()), 100.0F);
				ItemStack primal = apex.copy();
				MorphlingItem.setPrimalized(primal);
				LastRiteHelper.clearMorphlingRites(player);
				LastRiteHelper.armForMorphling(player, apex);
				String expectedApex = strain == MorphlingStrains.INK_CAP ? LastRiteHelper.INK_MANTLE_ID : "";
				helper.assertTrue(expectedApex.equals(LastRiteHelper.getArmedSource(player)),
						strain.id() + " at Apex armed '" + LastRiteHelper.getArmedSource(player) + "'");
				LastRiteHelper.clearMorphlingRites(player);
				LastRiteHelper.armForMorphling(player, primal);
				String expectedPrimal = strain == MorphlingStrains.INK_CAP ? LastRiteHelper.INK_MANTLE_ID
						: strain == MorphlingStrains.HUSHBELL ? LastRiteHelper.CRYPTOBIOSIS_ID : "";
				helper.assertTrue(expectedPrimal.equals(LastRiteHelper.getArmedSource(player)),
						strain.id() + " at Primal armed '" + LastRiteHelper.getArmedSource(player) + "'");
				helper.assertTrue(MorphlingCradleBlockEntity.isCradleSupportedPrimalStrain(primal) == strain.cradlePrimal(),
						strain.id() + " Cradle support differs from the table");
			}
			LastRiteHelper.clearMorphlingRites(player);
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	private static ItemStack withPower(ItemStack stack, float power) {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("EnzymePower", power);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}
}
