package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
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
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class PlaceStructurePacket implements CustomPacketPayload {

	public static final Type<PlaceStructurePacket> TYPE = new Type<>(Hemomancy.rloc("place_structure_packet"));
	public static final StreamCodec<FriendlyByteBuf, PlaceStructurePacket> STREAM_CODEC = StreamCodec.of(PlaceStructurePacket::encode, PlaceStructurePacket::decode);

	public enum StructureType {
		BLOOD_STRUCTURE,
		CARDINAL_RITE
	}

	private final ResourceLocation recipeId;
	private final StructureType type;

	public PlaceStructurePacket(ResourceLocation recipeId, StructureType type) {
		this.recipeId = recipeId;
		this.type = type;
	}

	public static void encode(FriendlyByteBuf buf, PlaceStructurePacket msg) {
		buf.writeResourceLocation(msg.recipeId);
		buf.writeEnum(msg.type);
	}

	public static PlaceStructurePacket decode(FriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		StructureType type = buf.readEnum(StructureType.class);
		return new PlaceStructurePacket(id, type);
	}

	public static void handle(final PlaceStructurePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				// Creative-only check
				if (!player.isCreative()) {
					player.sendSystemMessage(Component.literal("§cStructure Spawner requires creative mode!"));
					return;
				}

				ServerLevel level = player.serverLevel();
				MultiblockPattern pattern = null;
				BloodStructureRecipe bloodStructure = null;
				CardinalRiteRecipe cardinalRite = null;

				if (msg.type == StructureType.BLOOD_STRUCTURE) {
					BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(level, msg.recipeId);
					if (recipe != null) {
						pattern = recipe.getPattern();
						bloodStructure = recipe;
					}
				} else if (msg.type == StructureType.CARDINAL_RITE) {
					CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, msg.recipeId);
					if (recipe != null) {
						cardinalRite = recipe;
						pattern = recipe.getPattern();
					}
				}

				if (cardinalRite != null && cardinalRite.hasLayeredStation()) {
					int placed = placeLayeredCardinalRite(level, player.blockPosition(), cardinalRite, player);
					if (placed >= 0) {
						player.sendSystemMessage(Component.literal(
								"§aPlaced " + placed + " blocks for: " + msg.recipeId.getPath()));
					}
					return;
				}

				if (pattern == null) {
					player.sendSystemMessage(Component.literal("§cCould not find recipe: " + msg.recipeId));
					return;
				}

				// Get structure dimensions for centering
				List<BlockPosBlockPair> blockPairs = pattern.getBlockPosBlockList();
				if (blockPairs.isEmpty()) {
					player.sendSystemMessage(Component.literal("§cRecipe has no blocks to place!"));
					return;
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
				List<BlockPos> placedPositions = new java.util.ArrayList<>();

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

				if (bloodStructure != null) {
					BlockPos offeringCenter = playerPos.offset(0, minY, 0);
					int halfWidth = Math.max(centerX - minX, maxX - centerX);
					int halfDepth = Math.max(centerZ - minZ, maxZ - centerZ);
					for (var slot : BloodStructureOfferingPlacement.plan(
							offeringCenter, halfWidth, halfDepth, 1, bloodStructure.getOfferings())) {
						BlockPos support = slot.pos().below();
						if (!level.getBlockState(support).isFaceSturdy(level, support,
								net.minecraft.core.Direction.UP)) {
							level.setBlock(support, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
						}
						BlockState brazierState = BlockInit.iron_brazier.get().defaultBlockState()
								.setValue(BrazierBlock.RITUAL_PHASE, 1);
						level.setBlock(slot.pos(), brazierState, Block.UPDATE_ALL);
						if (level.getBlockEntity(slot.pos()) instanceof IronBrazierBlockEntity brazier) {
							var offeringStack = slot.representativeStack().copy();
							brazier.insertOffering(null, offeringStack);
						}
					}
				}

				player.sendSystemMessage(Component.literal("§aPlaced " + placed + " blocks for: " + msg.recipeId.getPath()));
			}
		});
	}

	public static int placeLayeredCardinalRite(ServerLevel level, BlockPos focusPos,
			CardinalRiteRecipe recipe, ServerPlayer player) {
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElse(null);
		if (floor == null) {
			player.sendSystemMessage(Component.literal(
					"§cCould not find Cardinal Rite floor: " + recipe.getFloorId()));
			return -1;
		}

		int requiredBraziers = recipe.getBrazierSignature().stream()
				.mapToInt(CardinalRiteRecipe.BrazierRequirement::count)
				.sum();
		if (requiredBraziers > floor.brazierSockets().size()) {
			player.sendSystemMessage(Component.literal(
					"§cRite requires " + requiredBraziers + " braziers, but floor "
							+ floor.id() + " only defines " + floor.brazierSockets().size() + " sockets"));
			return -1;
		}
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			if (requirement.ingredient().getItems().length == 0) {
				player.sendSystemMessage(Component.literal(
						"§cRite has a brazier ingredient with no available representative item"));
				return -1;
			}
		}

		List<BlockPos> placedPositions = new java.util.ArrayList<>();
		int placed = placePatternAtCell(level, floor.pattern(), focusPos,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ(), placedPositions);

		MultiblockPattern requiredStructure = recipe.getRequiredStructure();
		if (requiredStructure != null) {
			placed += placePatternAtCell(level, requiredStructure, focusPos.above(),
					requiredStructure.getBlockPattern().getWidth() / 2,
					requiredStructure.getBlockPattern().getHeight() - 1,
					requiredStructure.getBlockPattern().getDepth() / 2,
					placedPositions);
		}

		// Reset every socket so spawning another rite at this station cannot leave
		// stale offerings that make exact brazier-signature matching fail.
		for (BlockPos socket : floor.brazierSockets()) {
			level.setBlock(riteOffset(focusPos, socket), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}

		int socketIndex = 0;
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				BlockPos socketPos = riteOffset(focusPos, floor.brazierSockets().get(socketIndex++));
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
				placed++;
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
