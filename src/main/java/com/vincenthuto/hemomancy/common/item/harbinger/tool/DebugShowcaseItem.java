package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOffering;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug item that spawns a showcase area containing every Hemomancy feature
 * organized in relevant groups for unit testing. Creative-mode only.
 * <p>
 * Layout (from player position, extending in +X and +Z):
 * <ul>
 *   <li>Section 1: All Items stored in labeled chests</li>
 *   <li>Section 2: All Blocks placed in organized rows on a platform</li>
 *   <li>Section 3: All Mob Entities spawned in fenced pens</li>
 *   <li>Section 4: All Blood Structures and Cardinal Rites placed as patterns</li>
 * </ul>
 */
public class DebugShowcaseItem extends Item {

	private static final int SECTION_GAP = 4;
	private static final int ROW_WIDTH = 16;

	public DebugShowcaseItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (!serverPlayer.isCreative()) {
				serverPlayer.sendSystemMessage(Component.literal("§cDebug Showcase requires creative mode!"));
				return InteractionResultHolder.fail(stack);
			}

			ServerLevel serverLevel = serverPlayer.serverLevel();
			BlockPos origin = serverPlayer.blockPosition().offset(3, 0, 3);

			serverPlayer.sendSystemMessage(Component.literal("§6[Debug Showcase] §eGenerating showcase area..."));

			int currentZ = 0;

			// Section 1: Items in chests
			currentZ = spawnItemShowcase(serverLevel, origin, currentZ);
			currentZ += SECTION_GAP;

			// Section 2: Blocks placed on platform
			currentZ = spawnBlockShowcase(serverLevel, origin, currentZ);
			currentZ += SECTION_GAP;

			// Section 3: Entities in pens
			currentZ = spawnEntityShowcase(serverLevel, origin, currentZ);
			currentZ += SECTION_GAP;

			// Section 4: Structures
			spawnStructureShowcase(serverLevel, serverPlayer, origin, currentZ);

			serverPlayer.sendSystemMessage(Component.literal(
					"§6[Debug Showcase] §aComplete! Showcase generated at " +
					origin.getX() + ", " + origin.getY() + ", " + origin.getZ()));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	// ===================== ITEMS SECTION =====================

	/**
	 * Fills chests with all registered Hemomancy items, grouped by register.
	 * Returns the next Z offset.
	 */
	private int spawnItemShowcase(ServerLevel level, BlockPos origin, int startZ) {
		int z = startZ;

		// Header marker
		placeSectionHeader(level, origin, z, "ITEMS");
		z += 2;

		// Base Items
		z = placeItemChestGroup(level, origin, z, "Base Items",
				new ArrayList<>(ItemInit.BASEITEMS.getEntries()));

		// Handheld Items
		z = placeItemChestGroup(level, origin, z, "Handheld Items",
				new ArrayList<>(ItemInit.HANDHELDITEMS.getEntries()));

		// Special Items
		z = placeItemChestGroup(level, origin, z, "Special Items",
				new ArrayList<>(ItemInit.SPECIALITEMS.getEntries()));

		// Spawn Eggs
		z = placeItemChestGroup(level, origin, z, "Spawn Eggs",
				new ArrayList<>(ItemInit.SPAWNEGGS.getEntries()));

		return z;
	}

	/**
	 * Places chests filled with items from a group, labeled with a sign row.
	 * Returns next Z offset after this group.
	 */
	private int placeItemChestGroup(ServerLevel level, BlockPos origin, int z,
									String groupName, List<DeferredHolder<Item, ? extends Item>> items) {
		if (items.isEmpty()) return z;

		// Place label marker
		placeGroupLabel(level, origin, z, groupName);
		z += 1;

		int chestX = 0;
		int chestNum = 0;
		int slotIndex = 0;
		ChestBlockEntity currentChest = null;

		for (DeferredHolder<Item, ? extends Item> itemReg : items) {
			// Start a new chest every 27 items (chest capacity)
			if (slotIndex % 27 == 0) {
				BlockPos chestPos = origin.offset(chestX, 0, z);
				// Place platform below chest
				level.setBlock(chestPos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
				level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);

				BlockEntity be = level.getBlockEntity(chestPos);
				if (be instanceof ChestBlockEntity chestBE) {
					currentChest = chestBE;
					chestNum++;
				}
				chestX++;
				if (chestX >= ROW_WIDTH) {
					chestX = 0;
					z += 2;
				}
			}

			if (currentChest != null) {
				try {
					currentChest.setItem(slotIndex % 27, new ItemStack(itemReg.get()));
				} catch (RuntimeException ignored) {
					// Skip items that fail to create stacks
				}
			}
			slotIndex++;
		}

		return z + 2;
	}

	// ===================== BLOCKS SECTION =====================

	/**
	 * Places all registered Hemomancy blocks on a platform, grouped by register.
	 * Returns the next Z offset.
	 */
	private int spawnBlockShowcase(ServerLevel level, BlockPos origin, int startZ) {
		int z = startZ;

		placeSectionHeader(level, origin, z, "BLOCKS");
		z += 2;

		Object[][] blockGroups = {
			{"Base Blocks", BlockInit.BASEBLOCKS},
			{"Slab Blocks", BlockInit.SLABBLOCKS},
			{"Stair Blocks", BlockInit.STAIRBLOCKS},
			{"Column Blocks", BlockInit.COLUMNBLOCKS},
			{"Cross Blocks", BlockInit.CROSSBLOCKS},
			{"OBJ Blocks", BlockInit.OBJBLOCKS},
			{"Modeled Blocks", BlockInit.MODELEDBLOCKS},
			{"Special Blocks", BlockInit.SPECIALBLOCKS},
		};

		for (Object[] group : blockGroups) {
			String groupName = (String) group[0];
			@SuppressWarnings("unchecked")
			DeferredRegister<Block> register =
					(DeferredRegister<Block>) group[1];
			List<DeferredHolder<Block, ? extends Block>> blocks = new ArrayList<>(register.getEntries());

			if (blocks.isEmpty()) continue;

			placeGroupLabel(level, origin, z, groupName);
			z += 1;

			int x = 0;
			for (DeferredHolder<Block, ? extends Block> blockReg : blocks) {
				Block block = blockReg.get();
				BlockPos pos = origin.offset(x, 0, z);

				// Platform
				level.setBlock(pos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);

				try {
					level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
				} catch (RuntimeException ignored) {
					// Skip blocks that can't be placed
				}

				x++;
				if (x >= ROW_WIDTH) {
					x = 0;
					z += 2;
				}
			}

			// Move to next group
			if (x > 0) z += 2;
			z += 1;
		}

		return z;
	}

	// ===================== ENTITIES SECTION =====================

	/**
	 * Spawns all Hemomancy mob entities in fenced pens.
	 * Only spawns entities with living mob categories (not projectiles/misc).
	 * Returns the next Z offset.
	 */
	private int spawnEntityShowcase(ServerLevel level, BlockPos origin, int startZ) {
		int z = startZ;

		placeSectionHeader(level, origin, z, "ENTITIES");
		z += 2;

		// Separate mob entities from projectile/misc entities
		List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> mobEntities = new ArrayList<>();
		List<DeferredHolder<EntityType<?>, ? extends EntityType<?>>> miscEntities = new ArrayList<>();

		for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> entityReg : EntityInit.ENTITY_TYPES.getEntries()) {
			EntityType<?> type = entityReg.get();
			MobCategory category = type.getCategory();
			if (category == MobCategory.CREATURE || category == MobCategory.MONSTER
					|| category == MobCategory.WATER_AMBIENT || category == MobCategory.AMBIENT) {
				mobEntities.add(entityReg);
			} else {
				miscEntities.add(entityReg);
			}
		}

		// Spawn mob entities in fenced pens
		placeGroupLabel(level, origin, z, "Living Mobs");
		z += 1;

		int pensPerRow = 4;
		int penSize = 7;
		int penSpacing = penSize + 2;

		int penIndex = 0;
		for (int i = 0; i < mobEntities.size(); i++) {
			DeferredHolder<EntityType<?>, ? extends EntityType<?>> entityReg = mobEntities.get(i);
			EntityType<?> type = entityReg.get();

			// Skip entities that might not have a renderer registered to prevent crashes
			try {
				Entity testEntity = type.create(level);
				if (testEntity == null) continue;
				testEntity.discard();
			} catch (RuntimeException ignored) {
				continue;
			}

			int penCol = penIndex % pensPerRow;
			int penRow = penIndex / pensPerRow;
			int penX = penCol * penSpacing;
			int penZ = z + penRow * penSpacing;

			BlockPos penOrigin = origin.offset(penX, 0, penZ);
			buildFencedPen(level, penOrigin, penSize);

			BlockPos center = penOrigin.offset(penSize / 2, 1, penSize / 2);
			try {
				Entity entity = type.create(level);
				if (entity != null) {
					entity.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
					entity.setCustomName(Component.literal("§e" + entityReg.getId().getPath()));
					entity.setCustomNameVisible(true);
					level.addFreshEntity(entity);
				}
			} catch (RuntimeException ignored) {
				// Skip entities that fail to create
			}
			penIndex++;
		}

		int mobRows = (penIndex + pensPerRow - 1) / pensPerRow;
		z += mobRows * penSpacing + 3;

		// List misc/projectile entities in chests as named paper items
		placeGroupLabel(level, origin, z, "Projectile/Misc (names in chest)");
		z += 1;

		if (!miscEntities.isEmpty()) {
			int chestX = 0;
			int chestNum = 0;
			int slotIndex = 0;
			ChestBlockEntity currentChest = null;

			for (DeferredHolder<EntityType<?>, ? extends EntityType<?>> entityReg : miscEntities) {
				if (slotIndex % 27 == 0) {
					BlockPos chestPos = origin.offset(chestX, 0, z);
					level.setBlock(chestPos.below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
					level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);

					BlockEntity be = level.getBlockEntity(chestPos);
					if (be instanceof ChestBlockEntity chestBE) {
						currentChest = chestBE;
						chestNum++;
					}
					chestX++;
				}

				if (currentChest != null) {
					ItemStack paper = new ItemStack(Items.PAPER);
					paper.set(DataComponents.CUSTOM_NAME, Component.literal("§b" + entityReg.getId().getPath()));
					currentChest.setItem(slotIndex % 27, paper);
				}
				slotIndex++;
			}
		}

		return z + 2;
	}

	// ===================== STRUCTURES SECTION =====================

	/**
	 * Spawns all blood structures and cardinal rite patterns spaced apart.
	 * Uses the same placement logic as PlaceStructurePacket.
	 */
	private void spawnStructureShowcase(ServerLevel level, ServerPlayer player, BlockPos origin, int startZ) {
		int z = startZ;

		placeSectionHeader(level, origin, z, "STRUCTURES & RITES");
		z += 2;

		List<BloodStructureRecipe> structures = BloodStructureRecipe.getAllRecipes(level);
		List<CardinalRiteRecipe> rites = CardinalRiteRecipe.getAllRecipes(level);

		// Place blood structures
		if (!structures.isEmpty()) {
			placeGroupLabel(level, origin, z, "Blood Structures");
			z += 2;

			for (BloodStructureRecipe recipe : structures) {
				z = placeStructurePattern(level, origin, z, recipe.getPattern(),
						recipe.getId().getPath(), recipe.getOfferings());
				z += 3;
			}
		}

		// Place cardinal rite patterns
		if (!rites.isEmpty()) {
			placeGroupLabel(level, origin, z, "Cardinal Rites");
			z += 2;

			for (CardinalRiteRecipe recipe : rites) {
				String name = recipe.getRiteName() != null && !recipe.getRiteName().isEmpty()
						? recipe.getRiteName()
						: recipe.getId().getPath();
				if (recipe.hasLayeredStation()) {
					placeGroupLabel(level, origin, z, name);
					PlaceStructurePacket.placeLayeredCardinalRite(
							level, origin.offset(0, 0, z), recipe, player);
					MultiblockPattern floorPattern = recipe.getFloorPattern();
					MultiblockPattern upperPattern = recipe.getRequiredStructure();
					int floorDepth = floorPattern == null ? 1 : floorPattern.getBlockPattern().getDepth();
					int upperDepth = upperPattern == null ? 1 : upperPattern.getBlockPattern().getDepth();
					int footprintDepth = CardinalRiteFloorRegistry.get(recipe.getFloorId())
							.map(floor -> (int) Math.ceil(floor.footprintRadius() * 2.0F) + 1)
							.orElse(floorDepth);
					z += Math.max(Math.max(floorDepth, upperDepth), footprintDepth);
				} else {
					z = placeStructurePattern(level, origin, z, recipe.getPattern(), name, List.of());
				}
				z += 3;
			}
		}

		player.sendSystemMessage(Component.literal(
				"§6[Debug Showcase] §7Structures: §f" + structures.size() +
				" §7| Rites: §f" + rites.size()));
	}

	/**
	 * Places a multiblock structure pattern at the given Z offset.
	 * Returns the Z position after the structure.
	 */
	private int placeStructurePattern(ServerLevel level, BlockPos origin, int z,
									  MultiblockPattern pattern, String name,
									  List<BloodStructureOffering> offerings) {
		if (pattern == null) return z;
		List<BlockPosBlockPair> blockPairs = pattern.getBlockPosBlockList();
		if (blockPairs.isEmpty()) return z;

		// Find bounding box
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			minX = Math.min(minX, pos.getX());
			maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		int centerX = (minX + maxX) / 2;
		int centerZ = (minZ + maxZ) / 2;
		int structWidth = maxZ - minZ + 1;

		BlockPos placeOrigin = origin.offset(0, 0, z);

		// Place label
		placeGroupLabel(level, origin, z, name);

		// Sort by Y ascending for proper support
		blockPairs.sort(java.util.Comparator.comparingInt(p -> p.getPos().getY()));

		// Place blocks
		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;

			BlockPos relativePos = pair.getPos();
			BlockPos worldPos = placeOrigin.offset(
					relativePos.getX() - centerX,
					relativePos.getY(),
					relativePos.getZ() - centerZ
			);

			try {
				level.setBlock(worldPos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
			} catch (RuntimeException ignored) {
				// Skip blocks that fail
			}
		}

		BlockPos offeringCenter = placeOrigin.offset(0, minY, 0);
		int halfWidth = Math.max(centerX - minX, maxX - centerX);
		int halfDepth = Math.max(centerZ - minZ, maxZ - centerZ);
		for (var slot : BloodStructureOfferingPlacement.plan(
				offeringCenter, halfWidth, halfDepth, 1, offerings)) {
			level.setBlock(slot.pos().below(), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
			BlockState brazierState = BlockInit.iron_brazier.get().defaultBlockState()
					.setValue(BrazierBlock.RITUAL_PHASE, 1);
			level.setBlock(slot.pos(), brazierState, Block.UPDATE_ALL);
			if (level.getBlockEntity(slot.pos()) instanceof IronBrazierBlockEntity brazier) {
				ItemStack offeringStack = slot.representativeStack().copy();
				brazier.insertOffering(null, offeringStack);
			}
		}

		return z + structWidth + (offerings.isEmpty() ? 1 : 3);
	}

	// ===================== HELPER METHODS =====================

	/**
	 * Builds a fenced pen of the given size at the given origin.
	 */
	private void buildFencedPen(ServerLevel level, BlockPos origin, int size) {
		// Floor
		for (int x = 0; x < size; x++) {
			for (int penZ = 0; penZ < size; penZ++) {
				level.setBlock(origin.offset(x, 0, penZ), Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}

		// Fence walls (2 high to prevent jumping out)
		BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
		for (int y = 1; y <= 2; y++) {
			for (int x = 0; x < size; x++) {
				level.setBlock(origin.offset(x, y, 0), fence, Block.UPDATE_CLIENTS);
				level.setBlock(origin.offset(x, y, size - 1), fence, Block.UPDATE_CLIENTS);
			}
			for (int sideZ = 1; sideZ < size - 1; sideZ++) {
				level.setBlock(origin.offset(0, y, sideZ), fence, Block.UPDATE_CLIENTS);
				level.setBlock(origin.offset(size - 1, y, sideZ), fence, Block.UPDATE_CLIENTS);
			}
		}
	}

	/**
	 * Places a section header using glowstone and redstone lamps as visible markers.
	 */
	private void placeSectionHeader(ServerLevel level, BlockPos origin, int z, String name) {
		// Place a row of glowstone as a section divider
		for (int x = 0; x < ROW_WIDTH; x++) {
			level.setBlock(origin.offset(x, 0, z), Blocks.GLOWSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		// Place a sign chest at the start with the label
		BlockPos labelPos = origin.offset(0, 1, z);
		level.setBlock(labelPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
		BlockEntity be = level.getBlockEntity(labelPos);
		if (be instanceof ChestBlockEntity) {
			// Chest naming API changed in 1.21.1; keep marker placement without custom names.
		}
	}

	/**
	 * Places a group label using a named chest.
	 */
	private void placeGroupLabel(ServerLevel level, BlockPos origin, int z, String name) {
		BlockPos pos = origin.offset(-1, 0, z);
		level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof ChestBlockEntity) {
			// Chest naming API changed in 1.21.1; keep marker placement without custom names.
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("§7Right-click to spawn a showcase of"));
		tooltip.add(Component.literal("§7every Hemomancy feature in organized groups."));
		tooltip.add(Component.literal("§cCreative mode only!"));
		tooltip.add(Component.literal("§8Items in chests | Blocks on platforms | Mobs in pens"));
	}
}


