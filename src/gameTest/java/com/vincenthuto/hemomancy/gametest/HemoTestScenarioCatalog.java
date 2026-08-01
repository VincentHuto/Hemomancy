package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class HemoTestScenarioCatalog {
	private static final ResourceLocation COVENANT_THRONE_RECIPE =
			Hemomancy.rloc("blood_structure/covenant_throne");
	private static final ResourceLocation SANGUINE_INITIATION_RITE =
			Hemomancy.rloc("cardinal_rite/sanguine_initiation");
	private static final ResourceLocation VOTARY_RITE = Hemomancy.rloc("cardinal_rite/votary_rite");
	private static final ResourceLocation BLOOM_RITE = Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth");
	private static final ResourceLocation FOUNDING_FANE_RITE = Hemomancy.rloc("cardinal_rite/founding_fane");
	private static final String ACTIVE_SCENARIO_KEY = "hemomancy.dev_test.active_scenario";
	private static final String DEGREE_SNAPSHOT_KEY = "hemomancy.dev_test.snapshot.degree";
	private static final String CLAIM_SNAPSHOT_KEY = "hemomancy.dev_test.snapshot.worn_vow_claim";
	private static final String ARMOR_SNAPSHOT_PREFIX = "hemomancy.dev_test.snapshot.armor.";

	private static final List<HemoTestScenario> SCENARIOS = List.of(
			new HemoTestScenario(
					"blood_structure_locked",
					"Covenant Throne is rejected one degree below its explicit progression gate.",
					player -> prepareDegree(player, 5),
					player -> verifyRecipeGate(player, false),
					HemoTestScenarioCatalog::restoreDegree),
			new HemoTestScenario(
					"blood_structure_unlocked",
					"Covenant Throne becomes available at its required degree.",
					player -> prepareDegree(player, 6),
					player -> verifyRecipeGate(player, true),
					HemoTestScenarioCatalog::restoreDegree),
			new HemoTestScenario(
					"artificer_assignment_ready",
					"A complete Hematic Iron fitting prepares the Artificer's Worn Vow reward.",
					HemoTestScenarioCatalog::equipHematicIron,
					HemoTestScenarioCatalog::verifyArtificerAssignment,
					HemoTestScenarioCatalog::restoreArmor),
			new HemoTestScenario(
					"artificer_reward_claimed",
					"Artificer lesson claim state remains idempotent when applied repeatedly.",
					HemoTestScenarioCatalog::prepareRewardClaim,
					HemoTestScenarioCatalog::verifyRewardClaim,
					HemoTestScenarioCatalog::restoreRewardClaim),
			new HemoTestScenario(
					"uninitiated_cannot_pass_bloodcraft_degree_gate",
					"A Degree-0 player cannot satisfy the loaded Covenant Throne bloodcraft gate.",
					player -> prepareDegree(player, 0),
					player -> verifyRecipeGate(player, false),
					HemoTestScenarioCatalog::restoreDegree),
			new HemoTestScenario(
					"sanguine_initiation_recipe_loaded",
					"The Sanguine Initiation rite recipe is available to the loaded server registry.",
					player -> { },
					HemoTestScenarioCatalog::verifySanguineInitiationRecipeLoaded,
					player -> { }),
			new HemoTestScenario(
					"sanguine_initiation_degree_mapping",
					"The Sanguine Initiation rite retains its Degree-1 rank-up mapping and conduit reward registration.",
					player -> { },
					HemoTestScenarioCatalog::verifySanguineInitiationDegreeMapping,
					player -> { }),
			new HemoTestScenario(
					"cardinal_rite_media_loaded",
					"Loaded Cardinal Rite recipes expose their authored Focus media.",
					player -> { },
					HemoTestScenarioCatalog::verifyCardinalRiteMedia,
					player -> { }));

	private HemoTestScenarioCatalog() {
	}

	public static List<HemoTestScenario> all() {
		return SCENARIOS;
	}

	public static Optional<HemoTestScenario> find(String id) {
		if (id == null) {
			return Optional.empty();
		}
		String normalized = id.toLowerCase(Locale.ROOT);
		return SCENARIOS.stream().filter(scenario -> scenario.id().equals(normalized)).findFirst();
	}

	public static void markActive(ServerPlayer player, HemoTestScenario scenario) {
		player.getPersistentData().putString(ACTIVE_SCENARIO_KEY, scenario.id());
	}

	public static Optional<HemoTestScenario> active(ServerPlayer player) {
		return find(player.getPersistentData().getString(ACTIVE_SCENARIO_KEY));
	}

	public static void clearActive(ServerPlayer player) {
		active(player).ifPresent(scenario -> scenario.clear().apply(player));
		player.getPersistentData().remove(ACTIVE_SCENARIO_KEY);
	}

	private static void prepareDegree(ServerPlayer player, int degree) {
		if (!player.getPersistentData().contains(DEGREE_SNAPSHOT_KEY)) {
			player.getPersistentData().putInt(DEGREE_SNAPSHOT_KEY,
					HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber());
		}
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(degree);
	}

	private static void restoreDegree(ServerPlayer player) {
		if (player.getPersistentData().contains(DEGREE_SNAPSHOT_KEY)) {
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(
					player.getPersistentData().getInt(DEGREE_SNAPSHOT_KEY));
			player.getPersistentData().remove(DEGREE_SNAPSHOT_KEY);
		}
	}

	private static HemoTestResult verifySanguineInitiationRecipeLoaded(ServerPlayer player) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(player.level(), SANGUINE_INITIATION_RITE);
		return recipe != null
				? HemoTestResult.pass("Sanguine Initiation rite recipe is loaded")
				: HemoTestResult.fail("Sanguine Initiation rite was not loaded");
	}

	private static HemoTestResult verifySanguineInitiationDegreeMapping(ServerPlayer player) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(player.level(), SANGUINE_INITIATION_RITE);
		if (recipe == null) {
			return HemoTestResult.fail("Sanguine Initiation rite was not loaded");
		}
		Integer targetDegree = RecipeDegreeGates.getRankupTargetDegree(SANGUINE_INITIATION_RITE);
		return targetDegree != null && targetDegree == 1 && ItemInit.sanguine_conduit.get() != null
				? HemoTestResult.pass("Sanguine Initiation maps to Degree 1 and its conduit reward is registered")
				: HemoTestResult.fail("Sanguine Initiation Degree-1 mapping or conduit reward is missing");
	}

	private static HemoTestResult verifyCardinalRiteMedia(ServerPlayer player) {
		try {
			var getter = CardinalRiteRecipe.class.getMethod("getMedium");
			if (!matchesMedium(player, getter, SANGUINE_INITIATION_RITE, new ItemStack(Items.IRON_NUGGET))) {
				return HemoTestResult.fail("Sanguine Initiation does not require an iron-nugget medium");
			}
			if (!matchesMedium(player, getter, VOTARY_RITE, new ItemStack(Items.IRON_NUGGET))) {
				return HemoTestResult.fail("Rite of the Votary does not require an iron-nugget medium");
			}
			if (!matchesMedium(player, getter, BLOOM_RITE, new ItemStack(ItemInit.qliphoth_seed.get()))) {
				return HemoTestResult.fail("Bloom of the Qliphoth does not require its seed medium");
			}
			if (!matchesMedium(player, getter, FOUNDING_FANE_RITE,
					new ItemStack(ItemInit.sanguine_quintessence.get()))) {
				return HemoTestResult.fail("Founding Fane does not require its quintessence medium");
			}
			return HemoTestResult.pass("Cardinal Rite media are loaded from recipe data");
		} catch (ReflectiveOperationException exception) {
			return HemoTestResult.fail("Cardinal Rite recipes do not expose a medium ingredient: "
					+ exception.getClass().getSimpleName());
		}
	}

	private static boolean matchesMedium(ServerPlayer player, java.lang.reflect.Method getter,
			ResourceLocation id, ItemStack expected) throws ReflectiveOperationException {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(player.level(), id);
		return recipe != null && getter.invoke(recipe) instanceof Ingredient ingredient
				&& ingredient.test(expected);
	}

	private static HemoTestResult verifyRecipeGate(ServerPlayer player, boolean expected) {
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(player.level(), COVENANT_THRONE_RECIPE);
		if (recipe == null) {
			return HemoTestResult.fail("Covenant Throne recipe was not loaded");
		}
		boolean actual = RecipeDegreeGates.playerMeets(player, recipe);
		if (recipe.getRequiredDegree() != 6) {
			return HemoTestResult.fail("Expected recipe degree 6, found " + recipe.getRequiredDegree());
		}
		return actual == expected
				? HemoTestResult.pass("degree=" + RecipeDegreeGates.getPlayerLevel(player, false)
						+ ", required=6, allowed=" + actual)
				: HemoTestResult.fail("Expected allowed=" + expected + " but was " + actual);
	}

	private static void equipHematicIron(ServerPlayer player) {
		snapshotArmor(player, EquipmentSlot.HEAD);
		snapshotArmor(player, EquipmentSlot.CHEST);
		snapshotArmor(player, EquipmentSlot.LEGS);
		snapshotArmor(player, EquipmentSlot.FEET);
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.hematic_iron_helm.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.hematic_iron_chestplate.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.hematic_iron_leggings.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.hematic_iron_boots.get()));
	}

	private static HemoTestResult verifyArtificerAssignment(ServerPlayer player) {
		if (!ArmorSetHelper.hasFullHematicIronSet(player)) {
			return HemoTestResult.fail("Player is not wearing the complete Hematic Iron set");
		}
		return HarbingerArtificerAssignmentHelper.canGrantHematicIronFitting(player)
				? HemoTestResult.pass("Worn Vow fitting reward is ready")
				: HemoTestResult.fail("Expected the Worn Vow fitting reward");
	}

	private static void snapshotArmor(ServerPlayer player, EquipmentSlot slot) {
		String key = ARMOR_SNAPSHOT_PREFIX + slot.getName();
		if (!player.getPersistentData().contains(key)) {
			player.getPersistentData().put(key,
					player.getItemBySlot(slot).saveOptional(player.level().registryAccess()));
		}
	}

	private static void restoreArmor(ServerPlayer player) {
		for (EquipmentSlot slot : new EquipmentSlot[] {
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
			String key = ARMOR_SNAPSHOT_PREFIX + slot.getName();
			if (player.getPersistentData().contains(key)) {
				Tag snapshot = player.getPersistentData().get(key);
				ItemStack restored = snapshot instanceof CompoundTag compound && compound.isEmpty()
						? ItemStack.EMPTY
						: ItemStack.parse(player.level().registryAccess(), snapshot).orElse(ItemStack.EMPTY);
				player.setItemSlot(slot, restored);
				player.getPersistentData().remove(key);
			}
		}
	}

	private static void prepareRewardClaim(ServerPlayer player) {
		player.getPersistentData().putBoolean(CLAIM_SNAPSHOT_KEY,
				player.getPersistentData().getBoolean(HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY));
		player.getPersistentData().remove(HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY);
	}

	private static void restoreRewardClaim(ServerPlayer player) {
		if (!player.getPersistentData().contains(CLAIM_SNAPSHOT_KEY)) {
			return;
		}
		String claimKey = HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY;
		if (player.getPersistentData().getBoolean(CLAIM_SNAPSHOT_KEY)) {
			player.getPersistentData().putBoolean(claimKey, true);
		} else {
			player.getPersistentData().remove(claimKey);
		}
		player.getPersistentData().remove(CLAIM_SNAPSHOT_KEY);
	}

	private static HemoTestResult verifyRewardClaim(ServerPlayer player) {
		String key = HarbingerArtificerAssignmentHelper.WORN_VOW_REWARD_CLAIM_KEY;
		HarbingerArtificerAssignmentHelper.markArtificerLessonRewardClaimed(player, key);
		HarbingerArtificerAssignmentHelper.markArtificerLessonRewardClaimed(player, key);
		return HarbingerArtificerAssignmentHelper.isArtificerLessonRewardClaimed(player, key)
				? HemoTestResult.pass("Worn Vow lesson reward remains claimed after a repeated mark")
				: HemoTestResult.fail("Worn Vow lesson reward claim was not persisted");
	}
}
