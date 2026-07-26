package com.vincenthuto.hemomancy.gametest.journey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.FirstBloodcraftAssignmentHelper;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
	private static final int RADIUS = 4;
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
				case MORTAL_DISPLAY -> set(player, origin.above(), BlockInit.mortal_display.get());
				case SANGUINE_INITIATION -> {
					buildFloor(player, origin, new Block[][] {
							{ Blocks.STONE_BRICKS, BlockInit.engram_block.get(), Blocks.STONE_BRICKS },
							{ BlockInit.engram_block.get(), BlockInit.hematic_iron_block.get(), BlockInit.engram_block.get() },
							{ Blocks.STONE_BRICKS, BlockInit.engram_block.get(), Blocks.STONE_BRICKS }
					});
					var blood = HemoCapabilityAccess.requireBloodVolume(player);
					blood.setBloodVolume(Math.max(blood.getBloodVolume(), 100.0D));
				}
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
				case VICAR_REWARD -> spawnVicar(level, origin);
				case VOTARY_RITE -> buildVotaryRite(player, origin);
				case DEGREE_2_REACHED, ALCHEMIST_BRIEFING, ALCHEMIST_REWARD -> spawnAlchemist(level, origin);
				case CENTRIFUGE_PREPARED -> prepareCentrifugeCraft(player, origin);
				case SEPARATION_STARTED, ENZYME_RECOVERED -> prepareCentrifuge(player, origin);
				case INITIATE_RITE, ADEPT_RITE, ILLUMINATUS_RITE, SANCTIFIED_RITE, ARCHON_RITE ->
					buildRankupRite(player, origin, rankupRecipe(stage));
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
				entity -> entity.getTags().contains(ENTITY_MARKER))) entity.discard();
		ListTag owned = player.getPersistentData().getList(OWNED_BLOCKS_KEY, Tag.TAG_LONG);
		for (Tag value : owned) {
			BlockPos pos = BlockPos.of(((LongTag) value).getAsLong());
			if (isInsideOwnedBounds(origin, pos)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		player.getPersistentData().remove(OWNED_BLOCKS_KEY);
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
		List<ItemStack> required = expectedOutputStacks(stage);
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
					|| stage == HemoJourneyStage.VICAR_REWARD;
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
			case VICAR_REWARD -> FirstBloodcraftAssignmentHelper.isClaimed(player);
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

	private static List<ItemStack> expectedOutputStacks(HemoJourneyStage stage) {
		return switch (stage) {
			case FORMATION_PROJECTED -> List.of(new ItemStack(ItemInit.sanguine_formation.get()));
			case LIBER_CRAFTED -> List.of(new ItemStack(ItemInit.liber_sanguinum.get()));
			case HEMATIC_IRON_CRAFTED -> List.of(new ItemStack(BlockInit.hematic_iron_block.get()));
			case VICAR_REWARD -> FirstBloodcraftAssignmentHelper.rewardStacks();
			default -> List.of();
		};
	}

	private static AABB expectedSpawnBounds(HemoJourneyStage stage, BlockPos origin) {
		if (stage == HemoJourneyStage.FORMATION_PROJECTED || stage == HemoJourneyStage.LIBER_CRAFTED
				|| stage == HemoJourneyStage.HEMATIC_IRON_CRAFTED) {
			return new AABB(origin.getX() - 0.5D, origin.getY() + 0.95D, origin.getZ() - 0.5D,
					origin.getX() + 1.5D, origin.getY() + 3.5D, origin.getZ() + 1.5D);
		}
		Vec3 center = switch (stage) {
			case FORMATION_PROJECTED -> Vec3.atCenterOf(origin.above());
			case VICAR_REWARD -> new Vec3(origin.getX() + 0.5D, origin.getY() + 1.5D, origin.getZ() + 0.5D);
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
		if (stage == HemoJourneyStage.MORTAL_DISPLAY || stage == HemoJourneyStage.FORMATION_PROJECTED) {
			positions.add(origin.above());
		} else if (stage == HemoJourneyStage.SANGUINE_INITIATION || stage == HemoJourneyStage.LIBER_CRAFTED || stage == HemoJourneyStage.HEMATIC_IRON_CRAFTED) {
			for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) positions.add(origin.offset(x, 1, z));
		} else if (stage == HemoJourneyStage.VOTARY_RITE) {
			for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) positions.add(origin.offset(x, 1, z));
		} else if (stage == HemoJourneyStage.CENTRIFUGE_PREPARED || stage == HemoJourneyStage.SEPARATION_STARTED || stage == HemoJourneyStage.ENZYME_RECOVERED) {
			for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) positions.add(origin.offset(x, 2, z));
			if (stage == HemoJourneyStage.CENTRIFUGE_PREPARED) {
				positions.add(origin.offset(-2, 1, 0));
				positions.add(origin.offset(2, 1, 0));
			}
		} else if (isRankupStage(stage)) {
			for (int x = -RADIUS; x <= RADIUS; x++) {
				for (int y = 1; y <= 9; y++) {
					for (int z = -RADIUS; z <= RADIUS; z++) positions.add(origin.offset(x, y, z));
				}
			}
		}
		return positions;
	}

	private static void buildPlatform(ServerPlayer player, BlockPos origin) {
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
		vicar.addTag(ENTITY_MARKER);
		if (!level.addFreshEntity(vicar)) throw new IllegalStateException("Harbinger Vicar could not be spawned");
	}

	private static void spawnAlchemist(ServerLevel level, BlockPos origin) {
		HarbingerAlchemistEntity alchemist = EntityInit.harbinger_alchemist.get().create(level);
		if (alchemist == null) throw new IllegalStateException("Harbinger Alchemist entity creation returned null");
		alchemist.setPos(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
		alchemist.setNoAi(true); alchemist.setInvulnerable(true); alchemist.addTag(ENTITY_MARKER);
		if (!level.addFreshEntity(alchemist)) throw new IllegalStateException("Harbinger Alchemist could not be spawned");
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
		int halfWidth = recipe.getPattern().getBlockPattern().getWidth() / 2;
		int halfDepth = recipe.getPattern().getBlockPattern().getDepth() / 2;
		for (var pair : recipe.getPattern().getBlockPosBlockList()) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			set(player, origin.offset(relative.getX() - halfWidth, relative.getY() + 1,
					relative.getZ() - halfDepth), block);
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), recipe.getBloodCost()));
	}

	private static boolean isRankupStage(HemoJourneyStage stage) {
		return stage == HemoJourneyStage.INITIATE_RITE || stage == HemoJourneyStage.ADEPT_RITE
				|| stage == HemoJourneyStage.ILLUMINATUS_RITE || stage == HemoJourneyStage.SANCTIFIED_RITE
				|| stage == HemoJourneyStage.ARCHON_RITE;
	}

	private static String rankupRecipe(HemoJourneyStage stage) {
		return switch (stage) {
			case INITIATE_RITE -> "initiate_rite";
			case ADEPT_RITE -> "sanguine_brotherhood";
			case ILLUMINATUS_RITE -> "illuminatus_rite";
			case SANCTIFIED_RITE -> "sanctified_rite";
			case ARCHON_RITE -> "archon_rite";
			default -> throw new IllegalArgumentException("Not a rank-up journey stage: " + stage);
		};
	}

	private static void prepareCentrifuge(ServerPlayer player, BlockPos origin) {
		if (!fixtureLevel(player).getBlockState(origin.above(2)).is(BlockInit.vial_centrifuge.get())) return;
		if (!(fixtureLevel(player).getBlockEntity(origin.above(2)) instanceof VialCentrifugeBlockEntity centrifuge)) return;
		ItemStack vial = new ItemStack(ItemInit.bloody_vial.get()); CompoundTag tag = new CompoundTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, "hemomancy:crimson_doe"); tag.putBoolean(BloodVialItem.TAG_STATE, true);
		vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); centrifuge.inventory.set(2, vial); centrifuge.inventory.set(6, vial.copy()); centrifuge.setChanged();
	}

	private static void prepareCentrifugeCraft(ServerPlayer player, BlockPos origin) {
		buildFloorAt(player, origin, structureBaseHeight(2), new Block[][] {
				{ BlockInit.hematic_iron_block.get(), Blocks.GLASS, BlockInit.hematic_iron_block.get() },
				{ Blocks.GLASS, BlockInit.hematic_iron_block.get(), Blocks.GLASS },
				{ BlockInit.hematic_iron_block.get(), Blocks.GLASS, BlockInit.hematic_iron_block.get() }
		});
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemInit.ferric_binder.get()));
		BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(fixtureLevel(player),
				Hemomancy.rloc("blood_structure/vial_centrifuge"));
		if (recipe == null) throw new IllegalStateException("Vial Centrifuge blood structure recipe is unavailable");
		var offeringSlots = BloodStructureOfferingPlacement.plan(origin.above(), 1, 1, 1, recipe.getOfferings());
		for (var slot : offeringSlots) {
			set(player, slot.pos(), BlockInit.iron_brazier.get());
			player.getInventory().add(slot.representativeStack().copy());
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		double requiredBlood = recipe.getBloodCost() + offeringSlots.size() * BrazierBlock.BLOOD_TO_LIGHT;
		blood.setBloodVolume(Math.max(blood.getBloodVolume(), requiredBlood));
	}

	private static void set(ServerPlayer player, BlockPos pos, Block block) {
		if (!fixtureLevel(player).setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL)) {
			throw new IllegalStateException("Could not place " + block + " at " + pos);
		}
		ListTag owned = player.getPersistentData().getList(OWNED_BLOCKS_KEY, Tag.TAG_LONG);
		owned.add(LongTag.valueOf(pos.asLong()));
		player.getPersistentData().put(OWNED_BLOCKS_KEY, owned);
	}
}
