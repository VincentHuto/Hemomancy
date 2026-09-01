package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.*;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRecipeUnlocks;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.inscription.DiscoveryInscriptionBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** Deterministic fixtures with explicit persistent ownership and action baselines. */
public final class HemoJourneyFixtures {
	public static final String ORIGIN_KEY = "hemomancy.dev_test.journey.fixture_origin";
	public static final String DIMENSION_KEY = "hemomancy.dev_test.journey.fixture_dimension";
	public static final String ENTITY_MARKER = "hemomancy.dev_test.journey";
	public static final String OWNED_BLOCKS_KEY = "hemomancy.dev_test.journey.owned_blocks";
	public static final String BASELINE_KEY = "hemomancy.dev_test.journey.stage_baseline";
	public static final String OWNED_OUTPUTS_KEY = "hemomancy.dev_test.journey.owned_outputs";
	public static final String OUTPUT_MARKER = "hemomancy.dev_test.journey.output";
	private static final String BASELINE_INVENTORY = "inventory";
	private static final String BASELINE_ENTITIES = "entities";
	private static final String BASELINE_BLOOD = "blood";
	private static final String BASELINE_ADVANCEMENT = "advancement_complete";
	private static final int RADIUS = 8;
	private static final int HEIGHT = 10;

	private HemoJourneyFixtures() {
	}

	public static BlockPos findClearOrigin(ServerPlayer player) {
		BlockPos base = player.blockPosition().offset(0, 2, 8);
		for (int rise = 0; rise <= 12; rise++) {
			for (BlockPos offset : List.of(BlockPos.ZERO, new BlockPos(10, 0, 0), new BlockPos(-10, 0, 0),
					new BlockPos(0, 0, 10), new BlockPos(0, 0, -10))) {
				BlockPos candidate = base.offset(offset).above(rise);
				if (allPlacementPositions(candidate).stream().allMatch(pos -> canPlace(player.serverLevel(), pos))) {
					return candidate;
				}
			}
		}
		throw new IllegalStateException("No clear journey fixture volume was found near the player");
	}

	public static ServerLevel fixtureLevel(ServerPlayer player) {
		String saved = player.getPersistentData().getString(DIMENSION_KEY);
		ResourceLocation id = ResourceLocation.tryParse(saved);
		if (id == null) throw new IllegalStateException("Journey fixture dimension is missing or invalid: " + saved);
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);
		ServerLevel level = player.getServer().getLevel(dimension);
		if (level == null) throw new IllegalStateException("Journey fixture dimension is unavailable: " + id);
		return level;
	}

	public static void prepare(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		ServerLevel level = fixtureLevel(player);
		cleanup(player, origin);
		List<BlockPos> planned = plannedPositions(stage, origin);
		for (BlockPos pos : planned) {
			if (!canPlace(level, pos)) {
				throw new IllegalStateException("Fixture position is occupied by preexisting block "
						+ level.getBlockState(pos) + " at " + pos);
			}
		}
		player.getPersistentData().put(OWNED_BLOCKS_KEY, new ListTag());
		try {
			buildPlatform(player, origin);
			player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			switch (stage) {
				case MORTAL_DISPLAY -> prepareMortalDisplay(player, level, origin);
				case SANGUINE_INITIATION -> {
					prepareSanguineInitiation(player, origin);
				}
				case FIRST_REMNANT_DISCOVERED -> prepareFirstRemnant(player, origin);
				case VICAR_HERMIT_ROAD_REPORT -> spawnVicar(level, origin);
				case VESSEL_FILLED -> player.setItemSlot(EquipmentSlot.OFFHAND,
						new ItemStack(ItemInit.bloody_jug.get()));
				case FORMATION_PROJECTED -> {
					set(player, origin.above(), BlockInit.venous_stone.get());
					player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
				}
				case LIBER_CRAFTED -> {
					buildFloor(player, origin, ashWall(Blocks.BOOKSHELF));
					player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
					player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.sanguine_formation.get()));
				}
				case HEMATIC_IRON_CRAFTED -> {
					buildFloor(player, origin, ashWall(Blocks.IRON_BLOCK));
					player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
					player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.INK_SAC));
				}
				case LIVING_STAFF_CRAFTED -> prepareLivingStaffCraft(player, origin);
				case VICAR_REWARD -> spawnVicar(level, origin);
				case VOTARY_RITE -> buildRankupRite(player, origin, "votary_rite");
				case DEGREE_2_REACHED, ALCHEMIST_BRIEFING, ALCHEMIST_REWARD, BODY_ANSWERS_BRIEFING ->
					spawnAlchemist(level, origin);
				case BODY_ANSWERS_TINCTURE -> prepareBodyAnswersAlembic(player, origin);
				case RED_TAXONOMY -> prepareRedTaxonomy(player, level, origin);
				case LIVING_BESTIARY_RECORD -> prepareLivingBestiaryRecord(player, level, origin);
				case LIVING_BESTIARY_SURRENDER -> { }
				case HYPHAE_DISCOVERED -> spawnDiscoveryItem(level, origin, ItemInit.fungal_spine.get());
				case ARTIFICER_WORN_VOW_BRIEFING -> {
					HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
					spawnArtificer(level, origin);
					player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BlockInit.hematic_armature.get()));
				}
				case ARTIFICER_ARMATURE_PLACED -> { }
				case ARTIFICER_HEMATIC_UPGRADE -> prepareHematicUpgrade(player, origin);
				case ARTIFICER_WORN_VOW_REWARD -> spawnArtificer(level, origin);
				case ARTIFICER_WORN_VOW_FITTING -> {
					spawnArtificer(level, origin);
					player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.hematic_iron_helm.get()));
					player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.hematic_iron_chestplate.get()));
					player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.hematic_iron_leggings.get()));
					player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.hematic_iron_boots.get()));
				}
				case ENZYME_MASTERY -> prepareEnzymeMastery(player);
				case FIRST_CULTURE -> prepareFirstCulture(player, origin);
				case CENTRIFUGE_PREPARED -> prepareCentrifugeCraft(player, origin);
				case SEPARATION_STARTED, ENZYME_RECOVERED -> prepareCentrifuge(player, origin);
				case WOVEN_VESSEL_TURN_IN -> prepareWovenVesselTurnIn(player, level, origin);
				case FIRST_MEMORY_WOVEN -> prepareFirstMemoryWeave(player, origin);
				case NOETIC_MARK_RECOGNIZED -> spawnMnemonist(level, origin);
				case ARTIFICER_THREE_ANSWERS_BRIEFING -> {
					HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
					spawnArtificer(level, origin);
				}
				case ARTIFICER_FORK_UPGRADE -> prepareForkUpgrade(player, origin);
				case ARTIFICER_THREE_ANSWERS_INSPECTION, ARTIFICER_FORK_FITTING ->
					spawnArtificer(level, origin);
				case ARTIFICER_THREE_ANSWERS_COUNSEL -> spawnAlchemist(level, origin);
				case ARTIFICER_BARBED_RESEARCH -> prepareBarbedResearch(player, level, origin);
				case ARTIFICER_BARBED_RESEARCH_REWARD -> spawnAlchemist(level, origin);
				case ARTIFICER_FORK_DEMONSTRATION -> prepareForkDemonstration(player, level, origin);
				case VEIN_MASON_LESSON -> prepareVeinMasonLesson(player, level, origin);
				case FIRST_SCAR_CARVED -> prepareFirstScarCarve(player, origin);
				case FIRST_SCAR_LEARNED -> prepareScarBrazier(player, origin, false);
				case FIRST_EFFIGY_PATTERN -> prepareFirstEffigyPattern(player, origin);
				case FIRST_EFFIGY_LOADOUT -> prepareScarBrazier(player, origin, true);
				case VEIN_MASON_REWARD -> prepareVeinMason(player, level, origin);
				case VEIN_MASON_D5_STRAIN -> prepareVeinMasonD5Strain(player, level, origin);
				case VEIN_MASON_D5_DIAGNOSIS, VEIN_MASON_D5_REWARD -> prepareVeinMason(player, level, origin);
				case VEIN_MASON_D5_TREATMENT -> {
					player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.vascular_poultice.get()));
					HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
				}
				case VEIN_MASON_D5_FORTIFICATION -> prepareHematicFortification(player, origin);
				case ARTIFICER_ASSUMED_LIMB_BRIEFING, ARTIFICER_ASSUMED_LIMB_REWARD,
						ARTIFICER_LIVING_ARSENAL_FITTING -> spawnArtificer(level, origin);
				case ARTIFICER_FIRST_LIVING_GRAFT -> prepareLivingWeaponGrafts(player, origin,
						List.of(LivingWeaponForm.BLADE));
				case ARTIFICER_LIVING_ARSENAL_DEMONSTRATION -> prepareLivingArsenalDemonstration(player, level, origin);
				case ARTIFICER_FULL_LIVING_ARSENAL -> prepareLivingWeaponGrafts(player, origin,
						List.of(LivingWeaponForm.AXE, LivingWeaponForm.SPEAR, LivingWeaponForm.CLAWS,
								LivingWeaponForm.CROSSBOW, LivingWeaponForm.TORCH, LivingWeaponForm.FLAIL));
				case ARTIFICER_CRIMSON_VESTMENT_BRIEFING,
						ARTIFICER_CRIMSON_VESTMENT_INSPECTION, ARTIFICER_BLOOD_LUST_FITTING ->
						spawnArtificer(level, origin);
				case VICAR_CONSECRATION_KIT -> spawnVicar(level, origin);
				case ARTIFICER_FRAME_CONSECRATED -> prepareFrameConsecration(player, origin);
				case ARTIFICER_CRIMSON_VESTMENT_COUNSEL -> spawnAlchemist(level, origin);
				case ARTIFICER_BLOOD_LUST_UPGRADE -> prepareBloodLustUpgrade(player, origin);
				case ARTIFICER_BLOOD_LUST_DEMONSTRATION -> prepareBloodLustDemonstration(player, level, origin);
				case FOUNDING_FANE -> prepareFoundingFane(player, origin);
				case VEIN_MASON_D6_REFERRAL, VEIN_MASON_D6_REWARD -> {
					prepareVeinMasonD6(player);
					prepareVeinMason(player, level, origin);
				}
				case VEIN_MASON_D6_COUNSEL -> {
					prepareVeinMasonD6(player);
					spawnMnemonist(level, origin);
				}
				case VEIN_MASON_D6_FIRST_ROUTE, VEIN_MASON_D6_SECOND_ROUTE -> prepareMatchingNoeticCast(player, level, origin);
				case VEIN_MASON_D6_SCAR_CARVED -> prepareContinuationScarCarve(player, origin);
				case VEIN_MASON_D6_SCAR_LEARNED -> prepareContinuationScarBrazier(player, origin, false);
				case VEIN_MASON_D6_LOADOUT -> prepareContinuationScarBrazier(player, origin, true);
				case CHAMBER_RETURNED -> HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
				case COVENANT_THRONE_BOUND -> set(player, origin.above(), BlockInit.covenant_throne.get());
				case COVENANT_VIGIL -> prepareCovenantVigil(player, level, origin);
				case INITIATE_RITE, ADEPT_RITE, ILLUMINATUS_RITE, SANCTIFIED_RITE, ARCHON_RITE ->
					buildRankupRite(player, origin, rankupRecipe(stage));
				case ARTIFICER_WEIGHT_OF_FRAME_BRIEFING -> {
					HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
					spawnArtificer(level, origin);
				}
				case ARTIFICER_WEIGHT_OF_FRAME_INSPECTION, ARTIFICER_D7_FITTING -> spawnArtificer(level, origin);
				case ARTIFICER_MONOLITHIC_FRAME -> prepareMonolithicFrame(player, origin);
				case ARTIFICER_D7_UPGRADE -> prepareD7Upgrade(player, origin);
				case ARTIFICER_D7_DEMONSTRATION -> prepareD7Demonstration(player);
				case QLIPHOTH_COMMUNION -> prepareQliphothCommunion(player, origin);
				case APOTHEOS_CHOICE -> prepareApotheosChoice(player);
				case APOTHEOS_RITE -> buildRankupRite(player, origin, rankupRecipe(stage));
				case COMPLETE -> { }
			}
			recordBaseline(player, stage, origin);
		} catch (RuntimeException exception) {
			cleanup(player, origin);
			throw exception;
		}
	}

	public static void cleanup(ServerPlayer player, BlockPos origin) {
		ServerLevel level = fixtureLevel(player);
		for (Entity entity : level.getEntitiesOfClass(Entity.class, bounds(origin),
				entity -> entity.getTags().contains(entityMarker(origin)))) entity.discard();
		ListTag owned = player.getPersistentData().getList(OWNED_BLOCKS_KEY, Tag.TAG_LONG);
		for (Tag value : owned) {
			BlockPos pos = BlockPos.of(((LongTag) value).getAsLong());
			if (isInsideOwnedBounds(origin, pos)) {
				if (player.getVehicle() instanceof com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity restraint
						&& restraint.isForArmature(pos)) player.stopRiding();
				if (level.getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom) loom.contents.clear();
				if (level.getBlockEntity(pos) instanceof ScarStationBlockEntity station) station.contents.clear();
				if (level.getBlockEntity(pos) instanceof GhastlyAlembicBlockEntity alembic) alembic.clearContent();
				if (level.getBlockEntity(pos) instanceof MycelialLanternBlockEntity lantern) lantern.clearContent();
				if (level.getBlockEntity(pos) instanceof HematicArmatureBlockEntity armature) armature.clearContent();
				if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) brazier.extractOffering();
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		player.getPersistentData().remove(OWNED_BLOCKS_KEY);
	}

	public static String entityMarker(BlockPos origin) {
		return ENTITY_MARKER + "." + origin.asLong();
	}

	public static boolean captureExpectedOutputs(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		List<ItemEntity> selected = selectExpectedOutputs(player, stage, origin);
		if (selected == null) return false;
		ListTag owned = new ListTag();
		for (ItemEntity entity : selected) {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("uuid", entity.getUUID());
			owned.add(entry);
			entity.addTag(OUTPUT_MARKER);
		}
		player.getPersistentData().put(OWNED_OUTPUTS_KEY, owned);
		return true;
	}

	public static boolean expectedOutputsPresent(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		return selectExpectedOutputs(player, stage, origin) != null;
	}

	private static List<ItemEntity> selectExpectedOutputs(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		List<ItemStack> required = expectedOutputStacks(player, stage);
		if (required.isEmpty()) return List.of();
		Set<UUID> baseline = baselineEntityIds(player);
		List<ItemEntity> candidates = new ArrayList<>(fixtureLevel(player).getEntitiesOfClass(ItemEntity.class,
				expectedSpawnBounds(stage, origin), entity -> !baseline.contains(entity.getUUID())));
		candidates.sort(java.util.Comparator.comparing(entity -> entity.getUUID().toString()));
		List<ItemEntity> selected = new ArrayList<>();
		for (ItemStack expected : required) {
			boolean inventoryMayContainOutput = stage == HemoJourneyStage.FORMATION_PROJECTED
					|| stage == HemoJourneyStage.LIBER_CRAFTED
					|| stage == HemoJourneyStage.HEMATIC_IRON_CRAFTED
					|| stage == HemoJourneyStage.LIVING_STAFF_CRAFTED
					|| stage == HemoJourneyStage.VICAR_REWARD
					|| stage == HemoJourneyStage.VICAR_HERMIT_ROAD_REPORT
					|| stage == HemoJourneyStage.VICAR_CONSECRATION_KIT
					|| stage == HemoJourneyStage.ARTIFICER_WORN_VOW_REWARD
					|| stage == HemoJourneyStage.ARTIFICER_WORN_VOW_FITTING
					|| stage == HemoJourneyStage.ARTIFICER_THREE_ANSWERS_COUNSEL
					|| stage == HemoJourneyStage.ARTIFICER_BARBED_RESEARCH_REWARD
					|| stage == HemoJourneyStage.ARTIFICER_FORK_FITTING
					|| stage == HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_COUNSEL
					|| stage == HemoJourneyStage.ARTIFICER_BLOOD_LUST_FITTING
					|| stage == HemoJourneyStage.ARTIFICER_ASSUMED_LIMB_REWARD
					|| stage == HemoJourneyStage.ARTIFICER_LIVING_ARSENAL_FITTING
					|| stage == HemoJourneyStage.ARTIFICER_WEIGHT_OF_FRAME_INSPECTION
					|| stage == HemoJourneyStage.ARTIFICER_D7_FITTING
					|| stage == HemoJourneyStage.WOVEN_VESSEL_TURN_IN
					|| stage == HemoJourneyStage.FIRST_MEMORY_WOVEN
					|| stage == HemoJourneyStage.VEIN_MASON_LESSON
					|| stage == HemoJourneyStage.FIRST_EFFIGY_PATTERN
					|| stage == HemoJourneyStage.VEIN_MASON_REWARD
					|| stage == HemoJourneyStage.VEIN_MASON_D5_REWARD
					|| stage == HemoJourneyStage.VEIN_MASON_D6_REWARD;
			int inventoryDelta = inventoryMayContainOutput
					? inventoryCount(player, expected.getItem()) - baselineInventoryCount(player, expected.getItem()) : 0;
			if (inventoryDelta < 0) return null;
			int neededDrops = stage == HemoJourneyStage.FORMATION_PROJECTED
					? Math.max(0, expected.getCount() - inventoryDelta)
					: expected.getCount() - inventoryDelta;
			if (neededDrops < 0) return null;
			List<ItemEntity> matching = candidates.stream()
					.filter(entity -> entity.getItem().is(expected.getItem())).toList();
			List<ItemEntity> matches = exactQuantitySubset(matching, neededDrops, 0);
			if (matches == null) return null;
			int attributedDrops = matches.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
			if (stage == HemoJourneyStage.FORMATION_PROJECTED) {
				if (inventoryDelta + attributedDrops < expected.getCount()) return null;
			} else if (stage == HemoJourneyStage.VICAR_REWARD) {
				if (!HemoJourneyCheckpointRules.rewardQuantityPassed(
						baselineInventoryCount(player, expected.getItem()), inventoryCount(player, expected.getItem()),
						attributedDrops, expected.getCount())) return null;
			} else if (!HemoJourneyCheckpointRules.rewardQuantityPassed(
					0, inventoryDelta, attributedDrops, expected.getCount())) return null;
			selected.addAll(matches);
			candidates.removeAll(matches);
		}
		return selected;
	}

	private static List<ItemEntity> exactQuantitySubset(List<ItemEntity> candidates, int needed, int index) {
		if (needed == 0) return new ArrayList<>();
		if (needed < 0 || index >= candidates.size()) return null;
		ItemEntity candidate = candidates.get(index);
		List<ItemEntity> with = exactQuantitySubset(candidates, needed - candidate.getItem().getCount(), index + 1);
		if (with != null) {
			with.add(candidate);
			return with;
		}
		return exactQuantitySubset(candidates, needed, index + 1);
	}

	private static int baselineInventoryCount(ServerPlayer player, Item item) {
		return player.getPersistentData().getCompound(BASELINE_KEY).getCompound(BASELINE_INVENTORY)
				.getInt(itemId(item));
	}

	private static int inventoryCount(ServerPlayer player, Item item) {
		int count = 0;
		for (ItemStack stack : player.getInventory().items) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().offhand) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().armor) if (stack.is(item)) count += stack.getCount();
		return count;
	}

	public static void cleanupOwnedOutputs(ServerPlayer player, BlockPos origin) {
		for (Tag value : player.getPersistentData().getList(OWNED_OUTPUTS_KEY, Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) value;
			if (!entry.hasUUID("uuid")) continue;
			Entity entity = fixtureLevel(player).getEntity(entry.getUUID("uuid"));
			if (entity instanceof ItemEntity && entity.getTags().contains(OUTPUT_MARKER)
					&& bounds(origin).contains(entity.position())) entity.discard();
		}
		player.getPersistentData().remove(OWNED_OUTPUTS_KEY);
	}

	public static void cleanupForExit(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		captureExpectedOutputs(player, stage, origin);
		cleanupOwnedOutputs(player, origin);
		cleanup(player, origin);
	}

	public static double baselineBlood(ServerPlayer player) {
		return player.getPersistentData().getCompound(BASELINE_KEY).getDouble(BASELINE_BLOOD);
	}

	public static boolean baselineAdvancementIncomplete(ServerPlayer player) {
		return !player.getPersistentData().getCompound(BASELINE_KEY).getBoolean(BASELINE_ADVANCEMENT);
	}

	public static AABB bounds(BlockPos origin) {
		return new AABB(origin.getX() - RADIUS, origin.getY(), origin.getZ() - RADIUS,
				origin.getX() + RADIUS + 1, origin.getY() + HEIGHT + 1, origin.getZ() + RADIUS + 1);
	}

	private static void recordBaseline(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		CompoundTag baseline = new CompoundTag();
		CompoundTag inventory = new CompoundTag();
		for (ItemStack stack : player.getInventory().items) addCount(inventory, stack);
		for (ItemStack stack : player.getInventory().offhand) addCount(inventory, stack);
		for (ItemStack stack : player.getInventory().armor) addCount(inventory, stack);
		baseline.put(BASELINE_INVENTORY, inventory);
		ListTag entities = new ListTag();
		for (ItemEntity entity : fixtureLevel(player).getEntitiesOfClass(ItemEntity.class, bounds(origin))) {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("uuid", entity.getUUID());
			entry.putString("item", itemId(entity.getItem().getItem()));
			entry.putInt("count", entity.getItem().getCount());
			entities.add(entry);
		}
		baseline.put(BASELINE_ENTITIES, entities);
		baseline.putDouble(BASELINE_BLOOD, HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume());
		baseline.putString("stage", stage.id());
		baseline.putBoolean(BASELINE_ADVANCEMENT, switch (stage) {
			case LIBER_CRAFTED -> HarbingerAdvancementGranter.isLiberSanguinumCrafted(player);
			case HEMATIC_IRON_CRAFTED -> HarbingerAdvancementGranter.isHematicIronBlockCrafted(player);
			case VICAR_REWARD -> FirstBloodcraftAssignment.isClaimed(player);
			case WOVEN_VESSEL_TURN_IN -> HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(player);
			case FIRST_MEMORY_WOVEN -> HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player);
			case VEIN_MASON_LESSON -> HarbingerAdvancementGranter.isVeinMasonFirstLesson(player);
			case FIRST_SCAR_CARVED -> HarbingerAdvancementGranter.isVeinMasonFirstScarCarved(player);
			case FIRST_SCAR_LEARNED -> HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(player);
			case FIRST_EFFIGY_PATTERN -> HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(player);
			case FIRST_EFFIGY_LOADOUT -> HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player);
			case VEIN_MASON_REWARD -> HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player);
			case VEIN_MASON_D5_REWARD -> VeinMasonAssignments.has(player, VeinMasonAssignments.D5_REWARD);
			case VEIN_MASON_D6_REWARD -> VeinMasonAssignments.has(player, VeinMasonAssignments.D6_REWARD);
			default -> false;
		});
		player.getPersistentData().put(BASELINE_KEY, baseline);
	}

	private static Set<UUID> baselineEntityIds(ServerPlayer player) {
		Set<UUID> ids = new HashSet<>();
		for (Tag value : player.getPersistentData().getCompound(BASELINE_KEY)
				.getList(BASELINE_ENTITIES, Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) value;
			if (entry.hasUUID("uuid")) ids.add(entry.getUUID("uuid"));
		}
		return ids;
	}

	private static List<ItemStack> expectedOutputStacks(ServerPlayer player, HemoJourneyStage stage) {
		return switch (stage) {
			case FORMATION_PROJECTED -> List.of(new ItemStack(ItemInit.sanguine_formation.get()));
			case LIBER_CRAFTED -> List.of(new ItemStack(ItemInit.liber_sanguinum.get()));
			case HEMATIC_IRON_CRAFTED -> List.of(new ItemStack(BlockInit.hematic_iron_block.get()));
			case LIVING_STAFF_CRAFTED -> List.of(new ItemStack(ItemInit.living_staff.get()));
			case VICAR_HERMIT_ROAD_REPORT -> List.of(
					new ItemStack(ItemInit.harbinger_assignment_ledger.get()),
					new ItemStack(BlockInit.befouling_ash_trail.get(), 4));
			case VICAR_REWARD -> FirstBloodcraftAssignment.rewardStacks();
			case VICAR_CONSECRATION_KIT -> List.of(new ItemStack(ItemInit.vicars_consecration_kit.get()));
			case ARTIFICER_WORN_VOW_REWARD -> List.of(new ItemStack(ItemInit.hematic_iron_scrap.get(), 4));
			case ARTIFICER_WORN_VOW_FITTING -> List.of(new ItemStack(ItemInit.worn_vow_fitting.get()));
			case ARTIFICER_THREE_ANSWERS_COUNSEL -> List.of(new ItemStack(ItemInit.aculeate_vitriol.get()));
			case ARTIFICER_BARBED_RESEARCH_REWARD -> List.of(new ItemStack(ItemInit.aculeate_vitriol.get()));
			case ARTIFICER_FORK_FITTING -> List.of(new ItemStack(ItemInit.barbed_fitting.get()));
			case ARTIFICER_CRIMSON_VESTMENT_COUNSEL -> List.of(new ItemStack(ItemInit.crimson_lacquer.get()));
			case ARTIFICER_BLOOD_LUST_FITTING -> List.of(new ItemStack(ItemInit.crimson_vestment_fitting.get()));
			case ARTIFICER_ASSUMED_LIMB_REWARD -> List.of(new ItemStack(ItemInit.hematic_memory.get()));
			case ARTIFICER_LIVING_ARSENAL_FITTING -> List.of(new ItemStack(ItemInit.assumed_limb_fitting.get()));
			case ARTIFICER_WEIGHT_OF_FRAME_INSPECTION -> List.of(new ItemStack(ItemInit.fargone_proboscis.get()));
			case ARTIFICER_D7_FITTING -> List.of(new ItemStack(ItemInit.monolithic_frame_fitting.get()));
			case WOVEN_VESSEL_TURN_IN -> List.of(new ItemStack(ItemInit.bleeding_bulb.get()),
					new ItemStack(ItemInit.vivacious_enzyme.get()));
			case FIRST_MEMORY_WOVEN -> List.of(new ItemStack(ItemInit.memory_blood_shot.get()));
			case VEIN_MASON_LESSON -> List.of(new ItemStack(ItemInit.scar_pattern.get()),
					new ItemStack(ItemInit.scar_blank.get()),
					new ItemStack(VeinMasonScarLesson.forPlayer(player).catalyst()),
					new ItemStack(ItemInit.hematic_iron_knapper.get()));
			case FIRST_EFFIGY_PATTERN -> List.of(new ItemStack(ItemInit.scar_pattern.get()));
			case VEIN_MASON_REWARD -> List.of(new ItemStack(ItemInit.scar_pattern.get()),
					new ItemStack(ItemInit.scar_blank.get()),
					new ItemStack(VeinMasonScarLesson.continuationForPlayer(player).catalyst()),
					new ItemStack(ItemInit.runic_motif_paper.get(), 4));
			case VEIN_MASON_D5_REWARD -> List.of(new ItemStack(ItemInit.scar_pattern.get()),
					new ItemStack(ItemInit.scar_blank.get()),
					new ItemStack(VeinMasonScarLesson.strongestForPlayer(player, 2).catalyst()),
					new ItemStack(ItemInit.runic_motif_paper.get(), 4));
			case VEIN_MASON_D6_REWARD -> List.of(new ItemStack(ItemInit.scar_pattern.get()),
					new ItemStack(ItemInit.scar_blank.get()),
					new ItemStack(VeinMasonScarLesson.strongestForPlayer(player, 3).catalyst()),
					new ItemStack(ItemInit.runic_motif_paper.get(), 8));
			default -> List.of();
		};
	}

	private static AABB expectedSpawnBounds(HemoJourneyStage stage, BlockPos origin) {
		if (stage == HemoJourneyStage.FORMATION_PROJECTED || stage == HemoJourneyStage.LIBER_CRAFTED
				|| stage == HemoJourneyStage.HEMATIC_IRON_CRAFTED
				|| stage == HemoJourneyStage.LIVING_STAFF_CRAFTED
				|| stage == HemoJourneyStage.FIRST_MEMORY_WOVEN
				|| stage == HemoJourneyStage.FIRST_EFFIGY_PATTERN) {
			return new AABB(origin.getX() - 0.5D, origin.getY() + 0.95D, origin.getZ() - 0.5D,
					origin.getX() + 1.5D, origin.getY() + 3.5D, origin.getZ() + 1.5D);
		}
		Vec3 center = switch (stage) {
			case FORMATION_PROJECTED -> Vec3.atCenterOf(origin.above());
			case VICAR_HERMIT_ROAD_REPORT, VICAR_REWARD, VICAR_CONSECRATION_KIT,
					ARTIFICER_WORN_VOW_REWARD, ARTIFICER_WORN_VOW_FITTING,
					ARTIFICER_THREE_ANSWERS_COUNSEL, ARTIFICER_FORK_FITTING,
					ARTIFICER_BARBED_RESEARCH_REWARD,
					ARTIFICER_CRIMSON_VESTMENT_COUNSEL, ARTIFICER_BLOOD_LUST_FITTING,
					ARTIFICER_ASSUMED_LIMB_REWARD, ARTIFICER_LIVING_ARSENAL_FITTING,
					ARTIFICER_WEIGHT_OF_FRAME_INSPECTION, ARTIFICER_D7_FITTING,
					VEIN_MASON_LESSON, VEIN_MASON_REWARD, VEIN_MASON_D5_REWARD, VEIN_MASON_D6_REWARD ->
					new Vec3(origin.getX() + 0.5D, origin.getY() + 1.5D, origin.getZ() + 0.5D);
			default -> Vec3.atCenterOf(origin);
		};
		return AABB.ofSize(center, 2.0D, 2.0D, 2.0D);
	}

	private static boolean isInsideOwnedBounds(BlockPos origin, BlockPos pos) {
		return Math.abs(pos.getX() - origin.getX()) <= RADIUS && Math.abs(pos.getZ() - origin.getZ()) <= RADIUS
				&& pos.getY() >= origin.getY() && pos.getY() <= origin.getY() + HEIGHT;
	}

	private static void addCount(CompoundTag counts, ItemStack stack) {
		if (!stack.isEmpty()) {
			String id = itemId(stack.getItem());
			counts.putInt(id, counts.getInt(id) + stack.getCount());
		}
	}

	private static String itemId(Item item) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
		return id.toString();
	}

	private static boolean canPlace(ServerLevel level, BlockPos pos) {
		return !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos)
				&& level.getBlockEntity(pos) == null && level.getFluidState(pos).isEmpty()
				&& (level.getBlockState(pos).isAir() || level.getBlockState(pos).canBeReplaced());
	}

	private static List<BlockPos> allPlacementPositions(BlockPos origin) {
		Set<BlockPos> positions = new HashSet<>();
		for (HemoJourneyStage stage : HemoJourneyStage.values()) positions.addAll(plannedPositions(stage, origin));
		return List.copyOf(positions);
	}

	private static List<BlockPos> plannedPositions(HemoJourneyStage stage, BlockPos origin) {
		List<BlockPos> positions = new ArrayList<>();
		for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) positions.add(origin.offset(x, 0, z));
		if (stage == HemoJourneyStage.MORTAL_DISPLAY || stage == HemoJourneyStage.FIRST_REMNANT_DISCOVERED
				|| stage == HemoJourneyStage.FORMATION_PROJECTED
				|| stage == HemoJourneyStage.FIRST_MEMORY_WOVEN || stage == HemoJourneyStage.FIRST_SCAR_CARVED
				|| stage == HemoJourneyStage.FIRST_SCAR_LEARNED || stage == HemoJourneyStage.FIRST_EFFIGY_PATTERN
				|| stage == HemoJourneyStage.FIRST_EFFIGY_LOADOUT
				|| stage == HemoJourneyStage.BODY_ANSWERS_TINCTURE
				|| stage == HemoJourneyStage.FIRST_CULTURE) {
			positions.add(origin.above());
		} else if (stage == HemoJourneyStage.LIVING_STAFF_CRAFTED) {
			for (int y = 1; y <= 3; y++) positions.add(origin.above(y));
		} else if (stage == HemoJourneyStage.ARTIFICER_FIRST_LIVING_GRAFT
				|| stage == HemoJourneyStage.ARTIFICER_FULL_LIVING_ARSENAL) {
			for (int x = -1; x <= 1; x++) for (int z = 0; z <= 1; z++) positions.add(origin.offset(x, 1, z));
		} else if (stage == HemoJourneyStage.SANGUINE_INITIATION || stage == HemoJourneyStage.LIBER_CRAFTED || stage == HemoJourneyStage.HEMATIC_IRON_CRAFTED) {
			for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) positions.add(origin.offset(x, 1, z));
		} else if (stage == HemoJourneyStage.VOTARY_RITE) {
			for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) positions.add(origin.offset(x, 1, z));
		} else if (stage == HemoJourneyStage.ARTIFICER_ARMATURE_PLACED) {
			for (int x = -3; x <= 3; x++) for (int y = 1; y <= 5; y++) {
				for (int z = -1; z <= 1; z++) positions.add(origin.offset(x, y, z));
			}
		} else if (stage == HemoJourneyStage.CENTRIFUGE_PREPARED || stage == HemoJourneyStage.SEPARATION_STARTED || stage == HemoJourneyStage.ENZYME_RECOVERED) {
			for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) positions.add(origin.offset(x, 2, z));
			if (stage == HemoJourneyStage.CENTRIFUGE_PREPARED) {
				for (int x = -1; x <= 1; x++) for (int y = 1; y <= 3; y++) {
					for (int z = 0; z <= 1; z++) positions.add(origin.offset(x, y, z));
				}
				for (int x : List.of(-2, 2)) {
					positions.add(origin.offset(x, 1, 0));
					positions.add(origin.offset(x, 2, 0));
				}
			}
		} else if (stage == HemoJourneyStage.FOUNDING_FANE || isRankupStage(stage)) {
			for (int x = -RADIUS; x <= RADIUS; x++) {
				for (int y = 1; y <= 9; y++) {
					for (int z = -RADIUS; z <= RADIUS; z++) positions.add(origin.offset(x, y, z));
				}
			}
		}
		return positions;
	}

	static void buildPlatform(ServerPlayer player, BlockPos origin) {
		for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) set(player, origin.offset(x, 0, z), Blocks.STONE);
	}

	private static Block[][] ashWall(Block center) {
		Block ash = BlockInit.befouling_ash_trail.get();
		return new Block[][] {{ ash, ash, ash }, { ash, center, ash }, { ash, ash, ash }};
	}

	private static void buildWall(ServerPlayer player, BlockPos origin, Block[][] rows) {
		for (int row = rows.length - 1; row >= 0; row--) {
			for (int column = 0; column < rows[row].length; column++) {
				set(player, origin.offset(column - 1, 3 - row, 0), rows[row][column]);
			}
		}
	}

	private static void buildFloor(ServerPlayer player, BlockPos origin, Block[][] rows) {
		buildFloorAt(player, origin, 1, rows);
	}

	private static void buildFloorAt(ServerPlayer player, BlockPos origin, int y, Block[][] rows) {
		for (int z = 0; z < rows.length; z++) {
			for (int x = 0; x < rows[z].length; x++) {
				set(player, origin.offset(x - 1, y, z - 1), rows[z][x]);
			}
		}
	}

	/** Tall recipes start above the platform by their full pattern height. */
	public static int structureBaseHeight(int patternHeight) {
		return Math.max(1, patternHeight);
	}

	private static void spawnVicar(ServerLevel level, BlockPos origin) {
		HarbingerVicarEntity vicar = EntityInit.harbinger_vicar.get().create(level);
		if (vicar == null) throw new IllegalStateException("Harbinger Vicar entity creation returned null");
		vicar.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		vicar.setNoAi(true);
		vicar.setInvulnerable(true);
		vicar.addTag(entityMarker(origin));
		if (!level.addFreshEntity(vicar)) throw new IllegalStateException("Harbinger Vicar could not be spawned");
	}

	private static void prepareMortalDisplay(ServerPlayer player, ServerLevel level, BlockPos origin) {
		spawnVicar(level, origin);
		HarbingerVicarEntity vicar = level.getEntitiesOfClass(HarbingerVicarEntity.class, bounds(origin),
				entity -> entity.getTags().contains(entityMarker(origin))).getFirst();
		BlockPos displayPos = origin.above();
		set(player, displayPos, BlockInit.mortal_display.get());
		if (!(level.getBlockEntity(displayPos) instanceof MortalDisplayBlockEntity display)) {
			throw new IllegalStateException("Mortal Display block entity was not created");
		}
		display.linkHermit(vicar.getUUID());
		TempleOathRules.bless(player, vicar.getUUID());
	}

	private static void prepareSanguineInitiation(ServerPlayer player, BlockPos origin) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(fixtureLevel(player),
				Hemomancy.rloc("cardinal_rite/sanguine_initiation"));
		if (recipe == null || !recipe.hasLayeredStation()) {
			throw new IllegalStateException("Sanguine Initiation rite recipe is unavailable");
		}
		prepareLayeredRankupRite(player, origin, recipe);
		BlockPos focusPos = origin.above();
		CardinalFocusBlockEntity focus = (CardinalFocusBlockEntity) fixtureLevel(player).getBlockEntity(focusPos);
		focus.extractMedium();
		BlockPos displayPos = origin.above(4);
		set(player, displayPos, BlockInit.mortal_display.get());
		UUID hermit = TempleOathRules.blessedHermit(player);
		if (hermit == null) {
			hermit = UUID.randomUUID();
			TempleOathRules.bless(player, hermit);
		}
		if (!(fixtureLevel(player).getBlockEntity(displayPos) instanceof MortalDisplayBlockEntity display)) {
			throw new IllegalStateException("Claimed temple oath is unavailable for Sanguine Initiation");
		}
		display.linkHermit(hermit);
		display.claim(player.getUUID());
		focus.linkTempleDisplay(displayPos);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_NUGGET));
	}

	private static void prepareFirstRemnant(ServerPlayer player, BlockPos origin) {
		BlockPos inscriptionPos = origin.above();
		set(player, inscriptionPos, BlockInit.blood_echo_inscription.get());
		if (!(fixtureLevel(player).getBlockEntity(inscriptionPos) instanceof DiscoveryInscriptionBlockEntity inscription)) {
			throw new IllegalStateException("Blood Echo inscription block entity was not created");
		}
		inscription.setInscriptionId(Hemomancy.rloc("hermitage_remnant/first_invitation_stone"));
	}

	private static void spawnAlchemist(ServerLevel level, BlockPos origin) {
		HarbingerAlchemistEntity alchemist = EntityInit.harbinger_alchemist.get().create(level);
		if (alchemist == null) throw new IllegalStateException("Harbinger Alchemist entity creation returned null");
		alchemist.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		alchemist.setNoAi(true); alchemist.setInvulnerable(true); alchemist.addTag(entityMarker(origin));
		if (!level.addFreshEntity(alchemist)) throw new IllegalStateException("Harbinger Alchemist could not be spawned");
	}

	private static void spawnArtificer(ServerLevel level, BlockPos origin) {
		HarbingerArtificerEntity artificer = EntityInit.harbinger_artificer.get().create(level);
		if (artificer == null) throw new IllegalStateException("Harbinger Artificer entity creation returned null");
		artificer.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		artificer.setNoAi(true); artificer.setInvulnerable(true); artificer.addTag(entityMarker(origin));
		if (!level.addFreshEntity(artificer)) throw new IllegalStateException("Harbinger Artificer could not be spawned");
	}

	private static void prepareHematicUpgrade(ServerPlayer player, BlockPos origin) {
		BlockPos armaturePos = origin.above();
		if (!(fixtureLevel(player).getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature)) {
			throw new IllegalStateException("Place the supplied Hematic Armature at the fixture center first");
		}
		ownExistingBlock(player, armaturePos);
		armature.setItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT,
				new ItemStack(ItemInit.hematic_iron_scrap.get()));
		var blood = armature.getBloodCapability();
		if (blood == null) throw new IllegalStateException("Hematic Armature blood reservoir is unavailable");
		blood.setActive(true);
		blood.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
		blood.setBloodVolume(250.0D);
		player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
	}

	private static void prepareForkUpgrade(ServerPlayer player, BlockPos origin) {
		BlockPos armaturePos = origin.above();
		set(player, armaturePos, BlockInit.hematic_armature.get());
		if (!(fixtureLevel(player).getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature)) {
			throw new IllegalStateException("Hematic Armature block entity was not created");
		}
		armature.setItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT,
				new ItemStack(ItemInit.aculeate_vitriol.get()));
		var blood = armature.getBloodCapability();
		if (blood == null) throw new IllegalStateException("Hematic Armature blood reservoir is unavailable");
		blood.setActive(true);
		blood.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
		blood.setBloodVolume(500.0D);
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.hematic_iron_boots.get()));
	}

	private static void prepareForkDemonstration(ServerPlayer player, ServerLevel level, BlockPos origin) {
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.barbed_helm.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.barbed_chestplate.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.barbed_leggings.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.barbed_boots.get()));
		var attacker = EntityType.ZOMBIE.create(level);
		if (attacker == null) throw new IllegalStateException("Journey attacker creation returned null");
		attacker.setPos(origin.getX() + 2.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		attacker.setTarget(player);
		attacker.addTag(entityMarker(origin));
		if (!level.addFreshEntity(attacker)) throw new IllegalStateException("Journey attacker could not be spawned");
	}

	private static void prepareBarbedResearch(ServerPlayer player, ServerLevel level, BlockPos origin) {
		spawnAlchemist(level, origin);
		List<EntityType<? extends Mob>> specimens = List.of(EntityInit.barbed_urchin.get(), EntityInit.desiccant.get(),
				EntityInit.venom_rib_centipede.get());
		for (int i = 0; i < specimens.size(); i++) {
			Mob specimen = specimens.get(i).create(level);
			if (specimen == null) throw new IllegalStateException("Barbed research specimen creation returned null");
			specimen.setPos(origin.getX() + i - 0.5D, origin.getY() + 1.0D, origin.getZ() + 2.5D);
			specimen.setNoAi(true);
			specimen.setInvulnerable(true);
			specimen.addTag(entityMarker(origin));
			if (!level.addFreshEntity(specimen)) throw new IllegalStateException("Barbed research specimen could not be spawned");
		}
		player.getInventory().selected = 0;
		for (int slot = 0; slot < 3; slot++) {
			player.getInventory().setItem(slot, new ItemStack(BlockInit.specimen_jar.get()));
		}
	}

	private static void prepareFrameConsecration(ServerPlayer player, BlockPos origin) {
		set(player, origin.above(), BlockInit.hematic_armature.get());
		player.setItemSlot(EquipmentSlot.MAINHAND,
				takeOne(player, ItemInit.vicars_consecration_kit.get(), "Vicar's Consecration Kit"));
	}

	private static void prepareBloodLustUpgrade(ServerPlayer player, BlockPos origin) {
		BlockPos armaturePos = origin.above();
		set(player, armaturePos, BlockInit.hematic_armature.get());
		if (!(fixtureLevel(player).getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature)) {
			throw new IllegalStateException("Hematic Armature block entity was not created");
		}
		ItemStack lacquer = takeOne(player, ItemInit.crimson_lacquer.get(), "Crimson Lacquer");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.vicars_consecration_kit.get()));
		if (!armature.applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND)) {
			throw new IllegalStateException("Hematic Armature could not restore the earned consecrated tier");
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		armature.setItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT, lacquer);
		var blood = armature.getBloodCapability();
		if (blood == null) throw new IllegalStateException("Hematic Armature blood reservoir is unavailable");
		blood.setActive(true);
		blood.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
		blood.setBloodVolume(1200.0D);
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.barbed_boots.get()));
	}

	private static void prepareBloodLustDemonstration(ServerPlayer player, ServerLevel level, BlockPos origin) {
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.blood_lust_helm.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.blood_lust_chest.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.blood_lust_legs.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.blood_lust_boots.get()));
		var target = EntityType.ZOMBIE.create(level);
		if (target == null) throw new IllegalStateException("Journey target creation returned null");
		target.setPos(origin.getX() + 2.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		target.setNoAi(true);
		target.addTag(entityMarker(origin));
		if (!level.addFreshEntity(target)) throw new IllegalStateException("Journey target could not be spawned");
	}

	private static void prepareMonolithicFrame(ServerPlayer player, BlockPos origin) {
		BlockPos armaturePos = origin.above();
		set(player, armaturePos, BlockInit.hematic_armature.get());
		if (!(fixtureLevel(player).getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature)) {
			throw new IllegalStateException("Hematic Armature block entity was not created");
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.vicars_consecration_kit.get()));
		if (!armature.applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND)) {
			throw new IllegalStateException("Hematic Armature could not restore the earned consecrated tier");
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.monolithic_cornerstone.get()));
	}

	private static void prepareD7Upgrade(ServerPlayer player, BlockPos origin) {
		BlockPos armaturePos = origin.above();
		set(player, armaturePos, BlockInit.hematic_armature.get());
		if (!(fixtureLevel(player).getBlockEntity(armaturePos) instanceof HematicArmatureBlockEntity armature)) {
			throw new IllegalStateException("Hematic Armature block entity was not created");
		}
		for (Item item : List.of(ItemInit.vicars_consecration_kit.get(), ItemInit.monolithic_cornerstone.get())) {
			player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
			if (!armature.applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND)) {
				throw new IllegalStateException("Hematic Armature tier could not be prepared for Weight of the Frame");
			}
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		armature.setItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT,
				new ItemStack(ItemInit.fargone_proboscis.get()));
		var blood = armature.getBloodCapability();
		if (blood == null) throw new IllegalStateException("Hematic Armature blood reservoir is unavailable");
		blood.setActive(true);
		blood.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
		blood.setBloodVolume(2000.0D);
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.blood_lust_boots.get()));
	}

	private static void prepareD7Demonstration(ServerPlayer player) {
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.edacious_blood_lust_helm.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.edacious_blood_lust_chest.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.edacious_blood_lust_legs.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.edacious_blood_lust_boots.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), 250.0D));
	}

	private static void prepareQliphothCommunion(ServerPlayer player, BlockPos origin) {
		player.getInventory().selected = 0;
		for (int husk = 0; husk < 9; husk++) {
			player.getInventory().setItem(husk,
					QliphothPomeItem.createPickedPomeStack(origin.asLong(), husk, player.getUUID()));
		}
	}

	private static void prepareApotheosChoice(ServerPlayer player) {
		player.getPersistentData().putBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING, true);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setFungalRevelationWitnessed(true);
		if (!JourneyAutoRunner.activeForTest(player)) {
			PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.coreWitnessDialogue()));
		}
	}

	private static void prepareBodyAnswersAlembic(ServerPlayer player, BlockPos origin) {
		set(player, origin, Blocks.MAGMA_BLOCK);
		BlockPos alembicPos = origin.above();
		set(player, alembicPos, BlockInit.ghastly_alembic.get());
		if (!(fixtureLevel(player).getBlockEntity(alembicPos) instanceof GhastlyAlembicBlockEntity alembic)) {
			throw new IllegalStateException("Ghastly Alembic block entity was not created");
		}
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT,
				takeOne(player, ItemInit.sanguine_formation.get(), "Sanguine Formation"));
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
				takeOne(player, ItemInit.fervent_enzyme.get(), "Fervent Enzyme"));
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_TINCTURE_BLOOD,
				takeOne(player, ItemInit.bloody_flask.get(), "Bloody Flask"));
	}

	private static void prepareRedTaxonomy(ServerPlayer player, ServerLevel level, BlockPos origin) {
		spawnAlchemist(level, origin);
		java.util.Arrays.stream(HarbingerAlchemistDialogueTrees.RedTaxonomySample.values())
				.limit(4)
				.forEach(sample -> player.getInventory().add(new ItemStack(sample.block())));
	}

	private static void prepareEnzymeMastery(ServerPlayer player) {
		for (Item item : List.of(ItemInit.vivacious_enzyme.get(), ItemInit.fervent_enzyme.get(),
				ItemInit.neurotic_enzyme.get(), ItemInit.incandescent_enzyme.get(),
				ItemInit.ruinous_enzyme.get(), ItemInit.frigid_enzyme.get(),
				ItemInit.ferric_enzyme.get(), ItemInit.umbral_enzyme.get())) {
			player.getInventory().add(new ItemStack(item));
		}
	}

	private static void prepareLivingBestiaryRecord(ServerPlayer player, ServerLevel level, BlockPos origin) {
		spawnAlchemist(level, origin);
		CrimsonDoeEntity doe = EntityInit.crimson_doe.get().create(level);
		if (doe == null) throw new IllegalStateException("Crimson Doe entity creation returned null");
		doe.setPos(origin.getX() + 1.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		doe.setNoAi(true);
		doe.setInvulnerable(true);
		doe.addTag(entityMarker(origin));
		if (!level.addFreshEntity(doe)) throw new IllegalStateException("Crimson Doe could not be spawned");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BlockInit.specimen_jar.get()));
	}

	private static void spawnDiscoveryItem(ServerLevel level, BlockPos origin, Item item) {
		ItemEntity entity = new ItemEntity(level, origin.getX() + 0.5D, origin.getY() + 1.0D,
				origin.getZ() + 0.5D, new ItemStack(item));
		entity.setDeltaMovement(Vec3.ZERO);
		entity.addTag(entityMarker(origin));
		if (!level.addFreshEntity(entity)) throw new IllegalStateException("Discovery item could not be spawned");
	}

	private static void prepareFirstCulture(ServerPlayer player, BlockPos origin) {
		BlockPos lanternPos = origin.above();
		set(player, lanternPos, BlockInit.mycelial_lantern.get());
		if (!(fixtureLevel(player).getBlockEntity(lanternPos) instanceof MycelialLanternBlockEntity lantern)) {
			throw new IllegalStateException("Mycelial Lantern block entity was not created");
		}
		lantern.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(ItemInit.vivacious_spores.get()));
		var blood = lantern.getBloodCapability();
		if (blood == null) throw new IllegalStateException("Mycelial Lantern blood reservoir is unavailable");
		blood.setBloodVolume(600.0D);
	}

	private static ItemStack takeOne(ServerPlayer player, Item item, String name) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.is(item)) continue;
			stack.shrink(1);
			return new ItemStack(item);
		}
		throw new IllegalStateException("Journey fixture is missing " + name + " from the prior real interaction");
	}

	private static void prepareWovenVesselTurnIn(ServerPlayer player, ServerLevel level, BlockPos origin) {
		spawnMnemonist(level, origin);
		player.getInventory().add(new ItemStack(ItemInit.hematic_memory.get()));
		player.getInventory().add(new ItemStack(Items.BOOK));
		player.getInventory().add(new ItemStack(Items.INK_SAC));
		player.getInventory().add(new ItemStack(Items.PAPER, 3));
	}

	private static void spawnMnemonist(ServerLevel level, BlockPos origin) {
		HarbingerMnemonistEntity mnemonist = EntityInit.harbinger_mnemonist.get().create(level);
		if (mnemonist == null) throw new IllegalStateException("Harbinger Mnemonist entity creation returned null");
		mnemonist.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		mnemonist.setNoAi(true); mnemonist.setInvulnerable(true); mnemonist.addTag(entityMarker(origin));
		if (!level.addFreshEntity(mnemonist)) throw new IllegalStateException("Harbinger Mnemonist could not be spawned");
	}

	private static void prepareFirstMemoryWeave(ServerPlayer player, BlockPos origin) {
		BlockPos loomPos = origin.above();
		set(player, loomPos, BlockInit.somatic_loom.get());
		if (!(fixtureLevel(player).getBlockEntity(loomPos) instanceof SomaticLoomBlockEntity loom)) {
			throw new IllegalStateException("Somatic Loom block entity was not created");
		}
		loom.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
		loom.addItem(null, new ItemStack(ItemInit.bleeding_bulb.get()), null);
		loom.addItem(null, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
		if (!loom.hasValidRecipe()) throw new IllegalStateException("Blood Shot memory weave could not be prepared");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.living_staff.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), 50.0D));
	}

	private static void prepareVeinMasonLesson(ServerPlayer player, ServerLevel level, BlockPos origin) {
		prepareVeinMasonRank(player);
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
		prepareVeinMason(player, level, origin);
	}

	private static void prepareVeinMason(ServerPlayer player, ServerLevel level, BlockPos origin) {
		HarbingerCicatrixAnchoriteEntity mason = EntityInit.harbinger_cicatrix_anchorite.get().create(level);
		if (mason == null) throw new IllegalStateException("Vein-Mason entity creation returned null");
		mason.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		mason.setNoAi(true); mason.setInvulnerable(true); mason.addTag(entityMarker(origin));
		if (!level.addFreshEntity(mason)) throw new IllegalStateException("Vein-Mason could not be spawned");
	}

	private static void prepareFirstScarCarve(ServerPlayer player, BlockPos origin) {
		prepareVeinMasonRank(player);
		BlockPos stationPos = origin.above();
		set(player, stationPos, BlockInit.scar_station.get());
		if (!(fixtureLevel(player).getBlockEntity(stationPos) instanceof ScarStationBlockEntity station)) {
			throw new IllegalStateException("Cerebral Scarring Station block entity was not created");
		}
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(player);
		station.setItem(0, new ItemStack(ItemInit.scar_blank.get()));
		station.setItem(1, new ItemStack(lesson.catalyst()));
		station.setItem(3, new ItemStack(ItemInit.hematic_iron_knapper.get()));
		station.setItem(4, lesson.patternStack());
		if (station.getCurrentRecipe() == null || !station.tryLoadPatternFromSlot() || !station.areScarsMatching()) {
			throw new IllegalStateException("First scar recipe could not be prepared");
		}
	}

	private static void prepareScarBrazier(ServerPlayer player, BlockPos origin, boolean preparedLoadout) {
		prepareVeinMasonRank(player);
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(player);
		if (preparedLoadout) HemoCapabilityAccess.requireScarState(player).addKnownCerebralScar(lesson.patternScarId());
		BlockPos brazierPos = origin.above();
		set(player, brazierPos, BlockInit.iron_brazier.get());
		fixtureLevel(player).setBlock(brazierPos,
				fixtureLevel(player).getBlockState(brazierPos).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
		if (!(fixtureLevel(player).getBlockEntity(brazierPos) instanceof IronBrazierBlockEntity brazier)) {
			throw new IllegalStateException("Iron Brazier block entity was not created");
		}
		ItemStack offering = preparedLoadout
				? ItemScarPattern.createPreparedPattern(List.of(lesson.patternScarId()))
				: new ItemStack(lesson.scar().get());
		if (!brazier.insertOffering(null, offering)) throw new IllegalStateException("Scar offering could not be prepared");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_absorption.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), preparedLoadout
				? ScarBrazierRite.LOADOUT_BLOOD_COST : ScarBrazierRite.LEARN_BLOOD_COST));
	}

	private static void prepareFirstEffigyPattern(ServerPlayer player, BlockPos origin) {
		prepareVeinMasonRank(player);
		ResourceLocation scarId = VeinMasonScarLesson.forPlayer(player).patternScarId();
		HemoCapabilityAccess.requireScarState(player).addKnownCerebralScar(scarId);
		BlockPos effigyPos = origin.above();
		set(player, effigyPos, BlockInit.mason_effigy.get());
		if (!(fixtureLevel(player).getBlockEntity(effigyPos) instanceof MasonsEffigyBlockEntity effigy)) {
			throw new IllegalStateException("Mason's Effigy block entity was not created");
		}
		effigy.setSelectedScarIds(List.of(scarId));
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.runic_motif_paper.get()));
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.blood_projection.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), MasonsEffigyBlockEntity.BLOOD_PER_SCAR));
	}

	private static void prepareVeinMasonRank(ServerPlayer player) {
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(4);
	}

	private static void prepareVeinMasonD5Strain(ServerPlayer player, ServerLevel level, BlockPos origin) {
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED);
		HemoCapabilityAccess.requireVascularSystem(player).getVascularSystem().put(EnumVeinSections.ARMS, 50.1F);
		var memory = player.getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.MUSCLE_MEMORY);
		memory.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 1_200);
		memory.activate(MuscleMemory.SANGUINE_FISTS);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), 500.0D));
		Entity target = EntityType.ZOMBIE.create(level);
		if (target == null) throw new IllegalStateException("Vein-Mason strain target creation returned null");
		target.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		target.addTag(entityMarker(origin));
		if (!level.addFreshEntity(target)) throw new IllegalStateException("Vein-Mason strain target could not be spawned");
	}

	private static void prepareHematicFortification(ServerPlayer player, BlockPos origin) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(
				fixtureLevel(player), Hemomancy.rloc("cardinal_rite/hematic_fortification"));
		if (recipe == null || !recipe.hasLayeredStation()) {
			throw new IllegalStateException("Hematic Fortification rite recipe is unavailable");
		}
		var floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow(
				() -> new IllegalStateException("Hematic Fortification floor is unavailable: " + recipe.getFloorId()));
		BlockPos focusPos = origin.above();
		placePattern(player, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ());
		int socketIndex = 0;
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				BlockPos relative = floor.brazierSockets().get(socketIndex++);
				BlockPos socket = focusPos.offset(relative.getX(), relative.getY(), -relative.getZ());
				set(player, socket, BlockInit.iron_brazier.get());
				fixtureLevel(player).setBlock(socket,
						fixtureLevel(player).getBlockState(socket).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
				if (!(fixtureLevel(player).getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier)
						|| !brazier.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1))) {
					throw new IllegalStateException("Could not load the Hematic Fortification offering");
				}
			}
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
	}

	private static void prepareVeinMasonD6(ServerPlayer player) {
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
		VeinMasonAssignments.grant(player, VeinMasonAssignments.D5_REWARD);
	}

	private static void prepareMatchingNoeticCast(ServerPlayer player, ServerLevel level, BlockPos origin) {
		prepareVeinMasonD6(player);
		var scars = HemoCapabilityAccess.requireScarState(player);
		if (scars.getActiveCerebralScars().isEmpty()) {
			ResourceLocation fallback = VeinMasonScarLesson.forPlayer(player).patternScarId();
			scars.addKnownCerebralScar(fallback);
			scars.activateCerebralScar(fallback);
		}
		ResourceLocation active = scars.getActiveCerebralScars().iterator().next();
		var definition = ScarInit.getByName(active.toString());
		if (definition == null) throw new IllegalStateException("Active cerebral scar is unavailable: " + active);
		BloodManipulation manipulation = matchingManipulation(definition.getAssignedTendency());
		var known = HemoCapabilityAccess.requireKnownManipulations(player);
		known.getKnownManips().putIfAbsent(manipulation, new ManipLevel(0, 0));
		known.setEquippedManipNames(List.of(manipulation.getName()));
		known.setSelectedManip(manipulation);
		KnownManipulationEvents.syncPlayerEvent(player);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), manipulation.getCost() + 500.0D));
		player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		Entity target = EntityType.ZOMBIE.create(level);
		if (target == null) throw new IllegalStateException("Noetic routing target creation returned null");
		target.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		target.addTag(entityMarker(origin));
		if (!level.addFreshEntity(target)) throw new IllegalStateException("Noetic routing target could not be spawned");
	}

	private static BloodManipulation matchingManipulation(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> ManipulationInit.blood_shot.get();
			case DUCTILIS -> ManipulationInit.deadly_gaze.get();
			case FERRIC -> ManipulationInit.sanguine_mending.get();
			case FLAMMEUS -> ManipulationInit.crimson_flame_conjuration.get();
			case LUX -> ManipulationInit.hemosynthesis.get();
			case CONGEATIO -> ManipulationInit.glacial_grasp.get();
			case TENEBRIS -> ManipulationInit.void_shroud.get();
			case MORTEM -> ManipulationInit.hemorrhage.get();
		};
	}

	private static void prepareContinuationScarCarve(ServerPlayer player, BlockPos origin) {
		prepareVeinMasonD6(player);
		BlockPos stationPos = origin.above();
		set(player, stationPos, BlockInit.scar_station.get());
		if (!(fixtureLevel(player).getBlockEntity(stationPos) instanceof ScarStationBlockEntity station)) {
			throw new IllegalStateException("Continuation Cerebral Scarring Station block entity was not created");
		}
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.strongestForPlayer(player, 2);
		station.setItem(0, new ItemStack(ItemInit.scar_blank.get()));
		station.setItem(1, new ItemStack(lesson.catalyst()));
		station.setItem(3, new ItemStack(ItemInit.hematic_iron_knapper.get()));
		station.setItem(4, lesson.patternStack());
		if (station.getCurrentRecipe() == null || !station.tryLoadPatternFromSlot() || !station.areScarsMatching()) {
			throw new IllegalStateException("Continuation scar recipe could not be prepared");
		}
	}

	private static void prepareContinuationScarBrazier(ServerPlayer player, BlockPos origin, boolean loadout) {
		prepareVeinMasonD6(player);
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.strongestForPlayer(player, 2);
		if (loadout) HemoCapabilityAccess.requireScarState(player).addKnownCerebralScar(lesson.patternScarId());
		BlockPos brazierPos = origin.above();
		set(player, brazierPos, BlockInit.iron_brazier.get());
		fixtureLevel(player).setBlock(brazierPos,
				fixtureLevel(player).getBlockState(brazierPos).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
		if (!(fixtureLevel(player).getBlockEntity(brazierPos) instanceof IronBrazierBlockEntity brazier)) {
			throw new IllegalStateException("Continuation Iron Brazier block entity was not created");
		}
		ItemStack offering = loadout ? lesson.patternStack() : new ItemStack(lesson.scar().get());
		if (!brazier.insertOffering(null, offering)) throw new IllegalStateException("Continuation scar offering could not be prepared");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_absorption.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), loadout
				? ScarBrazierRite.LOADOUT_BLOOD_COST : ScarBrazierRite.LEARN_BLOOD_COST));
	}

	private static void buildVotaryRite(ServerPlayer player, BlockPos origin) {
		String[] rows = { "OEOEO", "E H E", "OHBHO", "E H E", "OEOEO" };
		for (int z = 0; z < rows.length; z++) for (int x = 0; x < rows[z].length(); x++) {
			char key = rows[z].charAt(x);
			if (key != ' ') set(player, origin.offset(x - 2, 1, z - 2), key == 'O' ? Blocks.OBSIDIAN
					: key == 'E' ? BlockInit.engram_block.get() : BlockInit.hematic_iron_block.get());
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player); blood.setBloodVolume(Math.max(blood.getBloodVolume(), 250.0D));
	}

	private static void buildRankupRite(ServerPlayer player, BlockPos origin, String recipePath) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(
				fixtureLevel(player), Hemomancy.rloc("cardinal_rite/" + recipePath));
		if (recipe == null || !recipe.isRankup()) {
			throw new IllegalStateException("Rank-up rite recipe is unavailable: " + recipePath);
		}
		if (recipe.hasLayeredStation()) {
			prepareLayeredRankupRite(player, origin, recipe);
			return;
		}
		var structure = recipe.getPattern() != null ? recipe.getPattern() : recipe.getRequiredStructure();
		if (structure == null) throw new IllegalStateException("Rank-up rite structure is unavailable: " + recipePath);
		int halfWidth = structure.getBlockPattern().getWidth() / 2;
		int halfDepth = structure.getBlockPattern().getDepth() / 2;
		for (var pair : structure.getBlockPosBlockList()) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			set(player, origin.offset(relative.getX() - halfWidth, relative.getY() + 1,
					relative.getZ() - halfDepth), block);
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), recipe.getBloodCost()));
	}

	static void prepareCardinalRite(ServerPlayer player, BlockPos origin, String recipePath) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(
				fixtureLevel(player), Hemomancy.rloc("cardinal_rite/" + recipePath));
		if (recipe == null) throw new IllegalStateException("Cardinal rite recipe is unavailable: " + recipePath);
		if (recipe.hasLayeredStation()) {
			prepareLayeredRankupRite(player, origin, recipe);
			return;
		}
		var structure = recipe.getPattern() != null ? recipe.getPattern() : recipe.getRequiredStructure();
		if (structure == null) throw new IllegalStateException("Cardinal rite structure is unavailable: " + recipePath);
		int halfWidth = structure.getBlockPattern().getWidth() / 2;
		int halfDepth = structure.getBlockPattern().getDepth() / 2;
		for (var pair : structure.getBlockPosBlockList()) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			set(player, origin.offset(relative.getX() - halfWidth, relative.getY() + 1,
					relative.getZ() - halfDepth), block);
		}
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
	}

	private static void prepareLayeredRankupRite(ServerPlayer player, BlockPos origin, CardinalRiteRecipe recipe) {
		var floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow(
				() -> new IllegalStateException("Rank-up rite floor is unavailable: " + recipe.getFloorId()));
		BlockPos focusPos = origin.above();
		placePattern(player, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ());
		if (recipe.getRequiredStructure() != null) {
			placePattern(player, recipe.getRequiredStructure(), focusPos.above(),
					recipe.getRequiredStructure().getBlockPattern().getWidth() / 2,
					recipe.getRequiredStructure().getBlockPattern().getHeight() - 1,
					recipe.getRequiredStructure().getBlockPattern().getDepth() / 2);
		}
		ItemStack[] media = recipe.getMedium().getItems();
		if (media.length > 0 && (!(fixtureLevel(player).getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus)
				|| !focus.insertMedium(null, media[0].copyWithCount(1)))) {
			throw new IllegalStateException("Could not seat the rank-up rite medium");
		}
		int socketIndex = 0;
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				BlockPos relative = floor.brazierSockets().get(socketIndex++);
				BlockPos socket = focusPos.offset(relative.getX(), relative.getY(), -relative.getZ());
				set(player, socket, BlockInit.iron_brazier.get());
				fixtureLevel(player).setBlock(socket,
						fixtureLevel(player).getBlockState(socket).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
				if (!(fixtureLevel(player).getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier)
						|| !brazier.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1))) {
					throw new IllegalStateException("Could not load a rank-up rite offering");
				}
			}
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), recipe.getBloodCost()));
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
	}

	private static void prepareFoundingFane(ServerPlayer player, BlockPos origin) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(
				fixtureLevel(player), Hemomancy.rloc("cardinal_rite/founding_fane"));
		if (recipe == null || !recipe.hasLayeredStation()) {
			throw new IllegalStateException("Founding Fane rite recipe is unavailable");
		}
		var floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow(
				() -> new IllegalStateException("Founding Fane floor is unavailable: " + recipe.getFloorId()));
		BlockPos focusPos = origin.above();
		placePattern(player, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ());
		placePattern(player, recipe.getRequiredStructure(), focusPos.above(),
				recipe.getRequiredStructure().getBlockPattern().getWidth() / 2,
				recipe.getRequiredStructure().getBlockPattern().getHeight() - 1,
				recipe.getRequiredStructure().getBlockPattern().getDepth() / 2);
		if (!(fixtureLevel(player).getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus)
				|| !focus.insertMedium(null, recipe.getMedium().getItems()[0].copyWithCount(1))) {
			throw new IllegalStateException("Could not seat the Founding Fane medium");
		}
		BlockPos socket = focusPos.offset(floor.brazierSockets().getFirst().getX(),
				floor.brazierSockets().getFirst().getY(), -floor.brazierSockets().getFirst().getZ());
		set(player, socket, BlockInit.iron_brazier.get());
		fixtureLevel(player).setBlock(socket,
				fixtureLevel(player).getBlockState(socket).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
		if (!(fixtureLevel(player).getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier)
				|| !brazier.insertOffering(null, new ItemStack(Items.NAUTILUS_SHELL))) {
			throw new IllegalStateException("Could not load the Founding Fane offering");
		}
		HemoJourneyWorldState.prepareFoundingFane(player);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);
		player.getInventory().add(new ItemStack(ItemInit.living_staff.get()));
		player.getInventory().add(new ItemStack(ItemInit.blood_projection.get()));
	}

	private static void prepareCovenantVigil(ServerPlayer player, ServerLevel level, BlockPos origin) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(
				level, Hemomancy.rloc("cardinal_rite/covenant_vigil"));
		if (recipe == null || !recipe.hasLayeredStation()) {
			throw new IllegalStateException("Covenant Vigil rite recipe is unavailable");
		}
		var floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow(
				() -> new IllegalStateException("Covenant Vigil floor is unavailable: " + recipe.getFloorId()));
		BlockPos focusPos = origin.above();
		placePattern(player, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ());
		int socketIndex = 0;
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				BlockPos relative = floor.brazierSockets().get(socketIndex++);
				BlockPos socket = focusPos.offset(relative.getX(), relative.getY(), -relative.getZ());
				set(player, socket, BlockInit.iron_brazier.get());
				level.setBlock(socket, level.getBlockState(socket).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
				ItemStack offering = requirement.ingredient().getItems()[0].copyWithCount(1);
				if (!(level.getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier)
						|| !brazier.insertOffering(null, offering)) {
					throw new IllegalStateException("Could not load a Covenant Vigil offering");
				}
			}
		}
		var line = HemoCapabilityAccess.requireBloodVolume(player).getBloodLine();
		if (!line.isValid()) throw new IllegalStateException("Covenant Vigil requires the journey bloodline");
		HarbingerVicarEntity helper = EntityInit.harbinger_vicar.get().create(level);
		if (helper == null) throw new IllegalStateException("Covenant Vigil helper creation returned null");
		BlockPos station = focusPos.offset(-3, 1, 0);
		helper.setPos(station.getX() + 0.5D, station.getY(), station.getZ() + 0.5D);
		helper.setNoAi(true);
		helper.setInvulnerable(true);
		helper.addTag(entityMarker(origin));
		if (!level.addFreshEntity(helper)) throw new IllegalStateException("Covenant Vigil helper could not be spawned");
		BloodlineSavedData.get(player.server.overworld()).addNpcMember(line.getBloodlineUUID(), helper.getUUID(),
				BuiltInRegistries.ENTITY_TYPE.getKey(helper.getType()));
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
	}

	private static void placePattern(ServerPlayer player, com.vincenthuto.hutoslib.math.MultiblockPattern pattern,
			BlockPos targetCell, int cellX, int cellY, int cellZ) {
		int physicalCellY = pattern.getBlockPattern().getHeight() - cellY - 1;
		List<BlockPos> placed = new ArrayList<>();
		var pairs = pattern.getBlockPosBlockList();
		pairs.sort(java.util.Comparator.comparingInt(pair -> pair.getPos().getY()));
		for (var pair : pairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			BlockPos worldPos = targetCell.offset(relative.getX() - cellX,
					relative.getY() - physicalCellY, cellZ - relative.getZ());
			if (!fixtureLevel(player).setBlock(worldPos, block.defaultBlockState(), Block.UPDATE_CLIENTS)) {
				throw new IllegalStateException("Could not place " + block + " at " + worldPos);
			}
			ownExistingBlock(player, worldPos);
			placed.add(worldPos);
		}
		for (BlockPos pos : placed) {
			fixtureLevel(player).blockUpdated(pos, fixtureLevel(player).getBlockState(pos).getBlock());
			fixtureLevel(player).updateNeighborsAt(pos, fixtureLevel(player).getBlockState(pos).getBlock());
		}
	}

	private static boolean isRankupStage(HemoJourneyStage stage) {
		return stage == HemoJourneyStage.INITIATE_RITE || stage == HemoJourneyStage.ADEPT_RITE
				|| stage == HemoJourneyStage.ILLUMINATUS_RITE || stage == HemoJourneyStage.SANCTIFIED_RITE
				|| stage == HemoJourneyStage.ARCHON_RITE || stage == HemoJourneyStage.APOTHEOS_RITE;
	}

	private static String rankupRecipe(HemoJourneyStage stage) {
		return switch (stage) {
			case INITIATE_RITE -> "initiate_rite";
			case ADEPT_RITE -> "sanguine_brotherhood";
			case ILLUMINATUS_RITE -> "illuminatus_rite";
			case SANCTIFIED_RITE -> "sanctified_rite";
			case ARCHON_RITE -> "archon_rite";
			case APOTHEOS_RITE -> "apotheos_rite";
			default -> throw new IllegalArgumentException("Not a rank-up journey stage: " + stage);
		};
	}

	private static void prepareCentrifuge(ServerPlayer player, BlockPos origin) {
		if (!fixtureLevel(player).getBlockState(origin.above(2)).is(BlockInit.vial_centrifuge.get())) {
			set(player, origin.above(2), BlockInit.vial_centrifuge.get());
		}
		for (int x : List.of(-2, 2)) {
			Entity cow = EntityType.COW.create(fixtureLevel(player));
			if (cow == null) throw new IllegalStateException("Journey sample cow could not be created");
			BlockPos spawn = origin.offset(x, 1, 1);
			cow.setPos(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
			cow.addTag(entityMarker(origin));
			if (!fixtureLevel(player).addFreshEntity(cow)) {
				throw new IllegalStateException("Journey sample cow could not be spawned");
			}
		}
	}

	private static void prepareCentrifugeCraft(ServerPlayer player, BlockPos origin) {
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(fixtureLevel(player),
				Hemomancy.rloc("blood_structure/vial_centrifuge"));
		if (recipe == null) throw new IllegalStateException("Vial Centrifuge blood structure recipe is unavailable");
		BlockPos hit = origin.above(2);
		set(player, origin.above(), Blocks.SMOOTH_STONE);
		placePattern(player, recipe.getPattern(), hit, 1, 1, 1);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.ferric_binder.get()));
		var offeringSlots = BloodStructureOfferingPlacement.plan(hit, 1, 1, 1, recipe.getOfferings());
		for (var slot : offeringSlots) {
			BlockPos support = slot.pos().below();
			if (!fixtureLevel(player).getBlockState(support).isFaceSturdy(fixtureLevel(player), support, Direction.UP)) {
				set(player, support, Blocks.SMOOTH_STONE);
			}
			set(player, slot.pos(), BlockInit.iron_brazier.get());
			fixtureLevel(player).setBlock(slot.pos(), fixtureLevel(player).getBlockState(slot.pos())
					.setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
			if (!(fixtureLevel(player).getBlockEntity(slot.pos()) instanceof IronBrazierBlockEntity brazier)
					|| !brazier.insertOffering(null, slot.representativeStack().copy())) {
				throw new IllegalStateException("Vial Centrifuge offering brazier could not be prepared");
			}
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), recipe.getBloodCost()));
	}

	private static void prepareLivingStaffCraft(ServerPlayer player, BlockPos origin) {
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(fixtureLevel(player),
				Hemomancy.rloc("blood_structure/living_staff"));
		if (recipe == null) throw new IllegalStateException("Living Staff blood structure recipe is unavailable");
		placePattern(player, recipe.getPattern(), origin.above(2), 0, 1, 0);
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.sanguine_formation.get()));
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), recipe.getBloodCost()));
	}

	private static void prepareLivingWeaponGrafts(ServerPlayer player, BlockPos origin, List<LivingWeaponForm> forms) {
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_staff.get()));
		var known = HemoCapabilityAccess.requireKnownManipulations(player);
		known.getKnownManips().putIfAbsent(ManipulationInit.blood_absorption.get(), new ManipLevel(0, 0));
		known.setSelectedManip(ManipulationInit.blood_absorption.get());
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), 1000.0D));
		for (int index = 0; index < forms.size(); index++) {
			LivingWeaponForm form = forms.get(index);
			if (!LivingWeaponGraftRecipeUnlocks.awardRecipeUnlock(player, form)) {
				throw new IllegalStateException("Could not prepare the " + form.displayName() + " graft recipe unlock");
			}
			BlockPos pos = forms.size() == 1 ? origin.above()
					: origin.offset(index % 3 - 1, 1, index / 3);
			set(player, pos, BlockInit.iron_brazier.get());
			fixtureLevel(player).setBlock(pos,
					fixtureLevel(player).getBlockState(pos).setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_ALL);
			if (!(fixtureLevel(player).getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier)
					|| !brazier.insertOffering(null, LivingWeaponGraftData.createStack(form))) {
				throw new IllegalStateException(form.displayName() + " graft brazier could not be prepared");
			}
		}
	}

	private static void prepareLivingArsenalDemonstration(ServerPlayer player, ServerLevel level, BlockPos origin) {
		Entity target = EntityType.ZOMBIE.create(level);
		if (!(target instanceof net.minecraft.world.entity.LivingEntity living)) {
			throw new IllegalStateException("Living Arsenal target could not be created");
		}
		BlockPos spawn = origin.offset(0, 1, 1);
		living.setPos(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
		living.setHealth(1.0F);
		living.addTag(entityMarker(origin));
		if (!level.addFreshEntity(living)) throw new IllegalStateException("Living Arsenal target could not be spawned");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_blade.get()));
	}

	static void set(ServerPlayer player, BlockPos pos, Block block) {
		if (!fixtureLevel(player).setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL)) {
			throw new IllegalStateException("Could not place " + block + " at " + pos);
		}
		ListTag owned = player.getPersistentData().getList(OWNED_BLOCKS_KEY, Tag.TAG_LONG);
		owned.add(LongTag.valueOf(pos.asLong()));
		player.getPersistentData().put(OWNED_BLOCKS_KEY, owned);
	}

	private static void ownExistingBlock(ServerPlayer player, BlockPos pos) {
		ListTag owned = player.getPersistentData().getList(OWNED_BLOCKS_KEY, Tag.TAG_LONG);
		long packed = pos.asLong();
		for (Tag value : owned) if (((LongTag) value).getAsLong() == packed) return;
		owned.add(LongTag.valueOf(packed));
		player.getPersistentData().put(OWNED_BLOCKS_KEY, owned);
	}
}
