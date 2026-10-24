package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.shared.StructureSpawnerItem;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFoundationRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFootprintResolver;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class PlaceStructurePacket implements CustomPacketPayload {
	private static final ResourceLocation FOUNDING_FANE_RITE =
			Hemomancy.rloc("cardinal_rite/founding_fane");

	public static final Type<PlaceStructurePacket> TYPE = new Type<>(Hemomancy.rloc("place_structure_packet"));
	public static final StreamCodec<FriendlyByteBuf, PlaceStructurePacket> STREAM_CODEC = StreamCodec.of(PlaceStructurePacket::encode, PlaceStructurePacket::decode);

	public enum StructureType {
		BLOOD_STRUCTURE,
		CARDINAL_RITE,
		/** A whole Cardinal Rite floor with its Focus and a lit, empty brazier on every socket. */
		RITE_FLOOR
	}

	private final ResourceLocation recipeId;
	private final StructureType type;
	/** Shift-click: also raise everything the craft needs to run (its Working floor, medium and lit offerings). */
	private final boolean fullSetup;

	public PlaceStructurePacket(ResourceLocation recipeId, StructureType type, boolean fullSetup) {
		this.recipeId = recipeId;
		this.type = type;
		this.fullSetup = fullSetup;
	}

	public ResourceLocation recipeId() {
		return recipeId;
	}

	public StructureType structureType() {
		return type;
	}

	public boolean fullSetup() {
		return fullSetup;
	}

	public static void encode(FriendlyByteBuf buf, PlaceStructurePacket msg) {
		buf.writeResourceLocation(msg.recipeId);
		buf.writeEnum(msg.type);
		buf.writeBoolean(msg.fullSetup);
	}

	public static PlaceStructurePacket decode(FriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		StructureType type = buf.readEnum(StructureType.class);
		return new PlaceStructurePacket(id, type, buf.readBoolean());
	}

	public static void handle(final PlaceStructurePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				// Creative-only check
				if (!player.isCreative()) {
					player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.structure_spawner_requires_creative_mode"));
					return;
				}
				// Pasting rites, floors and offerings is an operator tool, used from the held spawner (F-9).
				if (!player.hasPermissions(2)) {
					player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.structure_spawner_requires_operator_permission"));
					return;
				}
				if (ServerboundGuards.heldItem(player, StructureSpawnerItem.class).isEmpty()) {
					return;
				}

				spawn(player, msg.recipeId, msg.type, msg.fullSetup);
			}
		});
	}

	/**
	 * Spawns a structure, rite or floor at the player's feet. A Working craft spawns only its
	 * structure unless {@code fullSetup} (a shift-click) also raises its Working floor, with the
	 * medium seated in the Focus and the offerings lit in their braziers.
	 *
	 * @return blocks placed, or -1 when nothing could be spawned
	 */
	public static int spawn(ServerPlayer player, ResourceLocation recipeId, StructureType type, boolean fullSetup) {
		ServerLevel level = player.serverLevel();
		if (type == StructureType.RITE_FLOOR) {
			CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(recipeId).orElse(null);
			if (floor == null) {
				player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.could_not_find_cardinal_rite_floor", recipeId));
				return -1;
			}
			int placed = placeRiteFloor(level, player.blockPosition(), floor);
			player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.placed_blocks_for",
					placed,
					floor.displayName()));
			return placed;
		}
		MultiblockPattern pattern = null;
		BloodStructureRecipe bloodStructure = null;
		CardinalRiteRecipe cardinalRite = null;

		if (type == StructureType.BLOOD_STRUCTURE) {
			BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(level, recipeId);
			if (recipe != null) {
				pattern = recipe.getPattern();
				bloodStructure = recipe;
			}
		} else if (type == StructureType.CARDINAL_RITE) {
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, recipeId);
			if (recipe != null) {
				cardinalRite = recipe;
				pattern = recipe.getPattern();
			}
		}

		boolean craftStructureOnly = cardinalRite != null && cardinalRite.isWorkingCraft() && !fullSetup;
		if (craftStructureOnly) {
			// Just the structure, placed like a floorless Blood Structure; shift-click raises its Working floor too.
			pattern = cardinalRite.getSubject();
			cardinalRite = null;
		} else if (cardinalRite != null && cardinalRite.hasLayeredStation()) {
			int placed = placeLayeredCardinalRite(level, player.blockPosition(), cardinalRite, player);
			if (placed >= 0) {
				player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.placed_blocks_for",
						placed,
						recipeId.getPath()));
			}
			return placed;
		}

		if (pattern == null) {
			player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.could_not_find_recipe", recipeId));
			return -1;
		}

		// Get structure dimensions for centering
		List<BlockPosBlockPair> blockPairs = pattern.getBlockPosBlockList();
		if (blockPairs.isEmpty()) {
			player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.recipe_has_no_blocks_to_place"));
			return -1;
		}

		// Find bounding box
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			minX = Math.min(minX, pos.getX());
			maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY());
			maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		// Center offsets
		int centerX = (minX + maxX) / 2;
		int centerZ = (minZ + maxZ) / 2;

		BlockPos playerPos = player.blockPosition();
		int placed = 0;
		List<BlockPos> placedPositions = new java.util.ArrayList<>();

		if (cardinalRite != null) {
			float footprintRadius = CardinalRiteFootprintResolver.radius(cardinalRite, null);
			BlockPos floorCenter = playerPos.offset(0, minY, 0);
			for (BlockPos foundationPos : CardinalRiteFoundationRules.squareBelow(
					floorCenter, footprintRadius, FOUNDING_FANE_RITE.equals(cardinalRite.getId()))) {
				level.setBlock(foundationPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
				placed++;
			}
		}

		// Sort by Y ascending so support blocks are placed before blocks that
		// depend on them (e.g. befouling ash trails need a solid block below).
		blockPairs.sort(java.util.Comparator.comparingInt(p -> p.getPos().getY()));

		// Pre-pass: clear the entire structure volume to air so positions marked
		// as spaces in the pattern are guaranteed to match. Some pattern helpers
		// only expose non-air blocks, so iterating blockPairs is not enough for
		// large hollow structures like the Crimson Lodge.
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockPos relativePos = new BlockPos(x, y, z);
			BlockPos worldPos = playerPos.offset(
					relativePos.getX() - centerX,
					relativePos.getY(),
					relativePos.getZ() - centerZ
			);
					level.setBlock(worldPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
							Block.UPDATE_CLIENTS);
				}
			}
		}

		// Main pass: place all non-air blocks with UPDATE_CLIENTS only
		// (flag 2) to avoid neighbor-update cascades that can break
		// canSurvive-dependent blocks like befouling ash trails.
		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) continue;

			BlockPos relativePos = pair.getPos();
			BlockPos worldPos = playerPos.offset(
					relativePos.getX() - centerX,
					relativePos.getY(),
					relativePos.getZ() - centerZ
			);

			level.setBlock(worldPos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
			placedPositions.add(worldPos);
			placed++;
		}

		// Post-pass: notify neighbors so blocks update their shapes
		// (e.g. ash trail connections) now that all blocks are present.
		for (BlockPos pos : placedPositions) {
			level.blockUpdated(pos, level.getBlockState(pos).getBlock());
			level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
		}

		player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.placed_blocks_for_2",
				placed,
				recipeId.getPath(),
				(craftStructureOnly ? Component.translatable("message.hemomancy.place_structure_packet.working_floor_hint") : Component.empty())));
		return placed;
	}

	public static int placeLayeredCardinalRite(ServerLevel level, BlockPos focusPos,
			CardinalRiteRecipe recipe, ServerPlayer player) {
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElse(null);
		if (floor == null) {
			player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.could_not_find_cardinal_rite_floor",
					recipe.getFloorId()));
			return -1;
		}

		int requiredBraziers = recipe.getBrazierSignature().stream()
				.mapToInt(CardinalRiteRecipe.BrazierRequirement::count)
				.sum();
		if (requiredBraziers > floor.brazierSockets().size()) {
			player.sendSystemMessage(HemoText.translatable("message.hemomancy.place_structure_packet.rite_requires_braziers_but_floor_only",
					requiredBraziers,
					floor.id(),
					floor.brazierSockets().size()));
			return -1;
		}
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			if (requirement.ingredient().getItems().length == 0) {
				player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.rite_has_a_brazier_ingredient_with"));
				return -1;
			}
		}
		if (recipe.hasMedium() && recipe.getMedium().getItems().length == 0) {
			player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.rite_has_a_focus_medium_with"));
			return -1;
		}

		List<BlockPos> placedPositions = new java.util.ArrayList<>();
		int placed = placeFloor(level, focusPos, floor, CardinalRiteFootprintResolver.radius(recipe, floor),
				FOUNDING_FANE_RITE.equals(recipe.getId()), placedPositions);
		if (recipe.hasMedium()) {
			ItemStack mediumStack = recipe.getMedium().getItems()[0].copyWithCount(1);
			if (!(level.getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus)
					|| !focus.insertMedium(null, mediumStack)) {
				player.sendSystemMessage(Component.translatable("message.hemomancy.place_structure_packet.could_not_seat_the_rite_s"));
				return -1;
			}
		}

		// Reset every socket so spawning another rite at this station cannot leave stale offerings that make exact
		// brazier-signature matching fail. Before the structures: a socket on the floor's ring can lie in a craft's bed.
		for (BlockPos socket : floor.brazierSockets()) {
			level.setBlock(riteOffset(focusPos, socket), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}

		MultiblockPattern requiredStructure = recipe.getRequiredStructure();
		if (requiredStructure != null) {
			placed += placePatternAtCell(level, requiredStructure, focusPos.above(),
					requiredStructure.getBlockPattern().getWidth() / 2,
					requiredStructure.getBlockPattern().getHeight() - 1,
					requiredStructure.getBlockPattern().getDepth() / 2,
					placedPositions);
		}

		placed += placeWorkingSubject(level, focusPos, recipe, placedPositions);


		int socketIndex = 0;
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				BlockPos socketPos = riteOffset(focusPos, floor.brazierSockets().get(socketIndex++));
				placed += placeLitBrazier(level, socketPos, placedPositions);
				if (level.getBlockEntity(socketPos) instanceof IronBrazierBlockEntity brazier) {
					ItemStack offeringStack = requirement.ingredient().getItems()[0].copy();
					offeringStack.setCount(1);
					brazier.insertOffering(null, offeringStack);
					brazier.setChanged();
				}
			}
		}

		notifyPlacedBlocks(level, placedPositions);
		return placed;
	}

	/** The most braziers any rite takes; a spawned floor gets this many along its gash. */
	public static final int SPAWNED_BRAZIERS = 6;

	/**
	 * A whole rite floor around {@code focusPos}: foundation, floor and Focus, and {@link #SPAWNED_BRAZIERS} lit, empty
	 * braziers on the first cells of its gash (its back and sides, clear of a craft's bed).
	 */
	public static int placeRiteFloor(ServerLevel level, BlockPos focusPos, CardinalRiteFloorDefinition floor) {
		List<BlockPos> placedPositions = new java.util.ArrayList<>();
		int placed = placeFloor(level, focusPos, floor, floor.footprintRadius(), false, placedPositions);
		for (BlockPos socket : floor.brazierSockets().subList(0, Math.min(SPAWNED_BRAZIERS, floor.brazierSockets().size()))) {
			placed += placeLitBrazier(level, riteOffset(focusPos, socket), placedPositions);
		}
		notifyPlacedBlocks(level, placedPositions);
		return placed;
	}

	/** A Working craft's subject on the bed south of the Focus, its first aisle against it; 0 for other rites. */
	public static int placeWorkingSubject(ServerLevel level, BlockPos focusPos, CardinalRiteRecipe recipe) {
		List<BlockPos> placedPositions = new java.util.ArrayList<>();
		int placed = placeWorkingSubject(level, focusPos, recipe, placedPositions);
		notifyPlacedBlocks(level, placedPositions);
		return placed;
	}

	private static int placeWorkingSubject(ServerLevel level, BlockPos focusPos, CardinalRiteRecipe recipe,
			List<BlockPos> placedPositions) {
		MultiblockPattern subject = recipe.getSubject();
		if (subject == null) return 0;
		BlockPattern subjectPattern = subject.getBlockPattern();
		return placePatternAtCell(level, subject, focusPos.above().south(),
				subjectPattern.getWidth() / 2, subjectPattern.getHeight() - 1, subjectPattern.getDepth() - 1,
				placedPositions);
	}

	private static int placeFloor(ServerLevel level, BlockPos focusPos, CardinalRiteFloorDefinition floor,
			float footprintRadius, boolean faneBoundary, List<BlockPos> placedPositions) {
		int placed = 0;
		for (BlockPos foundationPos : CardinalRiteFoundationRules.squareBelow(focusPos, footprintRadius, faneBoundary)) {
			level.setBlock(foundationPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
			placed++;
		}
		return placed + placePatternAtCell(level, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ(), placedPositions);
	}

	/** A fresh, lit, empty iron brazier at {@code socketPos}, on a sturdy support. */
	private static int placeLitBrazier(ServerLevel level, BlockPos socketPos, List<BlockPos> placedPositions) {
		// Clear first: replacing a brazier with a brazier keeps its block entity and any old offering.
		level.setBlock(socketPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		int placed = 0;
		BlockPos supportPos = socketPos.below();
		if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
			level.setBlock(supportPos, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
			placedPositions.add(supportPos);
			placed++;
		}
		BlockState brazierState = BlockInit.iron_brazier.get().defaultBlockState()
				.setValue(BrazierBlock.RITUAL_PHASE, 1);
		level.setBlock(socketPos, brazierState, Block.UPDATE_ALL);
		placedPositions.add(socketPos);
		return placed + 1;
	}

	/**
	 * Places a pattern in the same canonical orientation used for Cardinal Rite
	 * socket offsets: north is forwards, east is right, and positive Y is up.
	 * Pattern rows run top-to-bottom, while BlockPosBlockPair Y runs bottom-to-top.
	 */
	private static int placePatternAtCell(ServerLevel level, MultiblockPattern pattern,
			BlockPos targetCell, int cellX, int cellY, int cellZ, List<BlockPos> placedPositions) {
		int physicalCellY = pattern.getBlockPattern().getHeight() - cellY - 1;
		List<BlockPosBlockPair> pairs = pattern.getBlockPosBlockList();

		for (BlockPosBlockPair pair : pairs) {
			BlockPos worldPos = patternOffset(targetCell, pair.getPos(), cellX, physicalCellY, cellZ);
			level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}

		pairs.sort(java.util.Comparator.comparingInt(pair -> pair.getPos().getY()));
		int placed = 0;
		for (BlockPosBlockPair pair : pairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos worldPos = patternOffset(targetCell, pair.getPos(), cellX, physicalCellY, cellZ);
			level.setBlock(worldPos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
			placedPositions.add(worldPos);
			placed++;
		}
		return placed;
	}

	private static BlockPos patternOffset(BlockPos targetCell, BlockPos patternPos,
			int cellX, int physicalCellY, int cellZ) {
		return targetCell.offset(
				patternPos.getX() - cellX,
				patternPos.getY() - physicalCellY,
				cellZ - patternPos.getZ());
	}

	private static BlockPos riteOffset(BlockPos focusPos, BlockPos relative) {
		return focusPos.offset(relative.getX(), relative.getY(), -relative.getZ());
	}

	private static void notifyPlacedBlocks(ServerLevel level, List<BlockPos> placedPositions) {
		for (BlockPos pos : placedPositions) {
			level.blockUpdated(pos, level.getBlockState(pos).getBlock());
			level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
