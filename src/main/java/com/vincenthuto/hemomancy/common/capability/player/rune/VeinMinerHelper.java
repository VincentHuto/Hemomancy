package com.vincenthuto.hemomancy.common.capability.player.rune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class VeinMinerHelper {

	public static final Comparator<ItemStack> ITEMSTACK_ASCENDING = (o1, o2) -> {
		if (o1.isEmpty() && o2.isEmpty()) {
			return 0;
		}
		if (o1.isEmpty()) {
			return 1;
		}
		if (o2.isEmpty()) {
			return -1;
		}
		if (o1.getItem() != o2.getItem()) {
			// Same item id
			return o1.getCount() - o2.getCount();
		}
		// Different id
		return Item.getId(o1.getItem()) - Item.getId(o2.getItem());
	};

	public static void compactItemListNoStacksize(List<ItemStack> list) {
		for (int i = 0; i < list.size(); i++) {
			ItemStack s = list.get(i);
			if (!s.isEmpty()) {
				for (int j = i + 1; j < list.size(); j++) {
					ItemStack s1 = list.get(j);
					if (ItemHandlerHelper.canItemStacksStack(s, s1)) {
						s.grow(s1.getCount());
						list.set(j, ItemStack.EMPTY);
					}
				}
			}
		}
		list.removeIf(ItemStack::isEmpty);
		list.sort(ITEMSTACK_ASCENDING);
	}

	public static void createLootDrop(List<ItemStack> drops, Level world, BlockPos pos) {
		createLootDrop(drops, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void createLootDrop(List<ItemStack> drops, Level world, double x, double y, double z) {
		if (!drops.isEmpty()) {
			compactItemListNoStacksize(drops);
			for (ItemStack drop : drops) {
				ItemEntity ent = new ItemEntity(world, x, y, z, drop);
				world.addFreshEntity(ent);
			}
		}
	}
	public static AABB getBroadDeepBox(BlockPos pos, int offset) {
		return new AABB(pos.getX() - offset, pos.getY(), pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset,
				pos.getZ() + offset);
	}
	public static Iterable<BlockPos> getPositionsFromBox(AABB box) {
		return getPositionsFromBox(BlockPos.containing(box.minX, box.minY, box.minZ),
				BlockPos.containing(box.maxX, box.maxY, box.maxZ));
	}

	public static Iterable<BlockPos> getPositionsFromBox(BlockPos corner1, BlockPos corner2) {
		return () -> BlockPos.betweenClosedStream(corner1, corner2).iterator();
	}
	public static int harvestVein(Level world, Player player, ItemStack stack, BlockPos pos, Block target,
			List<ItemStack> currentDrops, int numMined) {

		if (numMined >= 250) {
			return numMined;
		}
		AABB b = new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1,
				pos.getZ() + 1);
		for (BlockPos currentPos : getPositionsFromBox(b)) {
			BlockState currentState = world.getBlockState(currentPos);
			if (currentState.getBlock() == target) {
				// Ensure we are immutable so that changing blocks doesn't act weird
				currentPos = currentPos.immutable();
				if (hasBreakPermission((ServerPlayer) player, currentPos)) {
					numMined++;
					currentDrops.addAll(Block.getDrops(currentState, (ServerLevel) world, currentPos,
							world.getBlockEntity(currentPos), player, stack));
					world.removeBlock(currentPos, false);
					numMined = harvestVein(world, player, stack, currentPos, target, currentDrops, numMined);
					if (numMined >= 250) {
						player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
							if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
								manips.setLastVeinMineStart(BlockPos.ZERO);
							}
						});

						break;
					}
				}
			}
		
			System.out.println(numMined);
		}
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
				manips.setLastVeinMineStart(BlockPos.ZERO);
			}
		});
		return numMined;
	}

	public static boolean hasBreakPermission(ServerPlayer player, BlockPos pos) {
		return hasEditPermission(player, pos) && ForgeHooks.onBlockBreakEvent(player.getCommandSenderWorld(),
				player.gameMode.getGameModeForPlayer(), player, pos) != -1;
	}

	public static boolean hasEditPermission(ServerPlayer player, BlockPos pos) {
		if (ServerLifecycleHooks.getCurrentServer().isUnderSpawnProtection((ServerLevel) player.getCommandSenderWorld(),
				pos, player)) {
			return false;
		}
		return Arrays.stream(Direction.values()).allMatch(e -> player.mayUseItemAt(pos, e, ItemStack.EMPTY));
	}

	public static InteractionResultHolder<ItemStack> actionResultFromType(InteractionResult type, ItemStack stack) {
		switch (type) {
		case SUCCESS:
			return InteractionResultHolder.success(stack);
		case CONSUME:
			return InteractionResultHolder.consume(stack);
		case FAIL:
			return InteractionResultHolder.fail(stack);
		case PASS:
		default:
			return InteractionResultHolder.pass(stack);
		}
	}

	public static boolean isOre(Block b) {
		return ForgeRegistries.BLOCKS.tags().getTag(BlockTags.STONE_ORE_REPLACEABLES).contains(b)
				|| ForgeRegistries.BLOCKS.tags().getTag(BlockTags.DEEPSLATE_ORE_REPLACEABLES).contains(b);
	}

	public static boolean isOre(BlockState state) {
		return state.is(Tags.Blocks.ORES);
	}

	public static boolean isOre(Item i) {
		return isOre(Block.byItem(i));
	}

	/**
	 * Scans and harvests an ore vein.
	 */
	public static InteractionResult tryVeinMine(ItemStack itemStack, Player player, BlockPos pos ) {

		Level world = player.getCommandSenderWorld();
		BlockState target = world.getBlockState(pos);
		boolean hasAction = false;
		List<ItemStack> drops = new ArrayList<>();
		
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() == BlockPos.ZERO) {
				manips.setLastVeinMineStart(pos);
			}
		});

		
		
		for (BlockPos newPos : getPositionsFromBox(getBroadDeepBox(pos, 8))) {
			if (!world.isEmptyBlock(newPos)) {
				BlockState state = world.getBlockState(newPos);
				if (target.getBlock() == state.getBlock()) {
					if (world.isClientSide) {
						return InteractionResult.SUCCESS;
					}
					// Ensure we are immutable so that changing blocks doesn't act weird
					if (harvestVein(world, player,itemStack, newPos.immutable(), state.getBlock(), drops, 0) > 0) {
						hasAction = true;
					}
				}
			}
		}
		if (hasAction) {
			createLootDrop(drops, world, pos);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_BREAK,
					SoundSource.PLAYERS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
