package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

/**
 * Track 2's scenes in the heart-node review: a floor, a wall and a ceiling synapse raised one after another in a chain
 * out from the node (through the real feed, so the special path runs), each framed for a capture, and a raise that
 * would touch another owner's envelope, refused with the foreign message on the action bar.
 */
final class SynapseScenes {
	private static final Direction[] FACES = { Direction.UP, Direction.WEST, Direction.DOWN };
	static final UUID NEIGHBOUR = UUID.nameUUIDFromBytes("hemomancy:heart_node_review_neighbour".getBytes());
	private static int raised;

	private SynapseScenes() {}

	private static BlockPos iron(BlockPos node, int index) {
		return switch (index) {
			case 0 -> node.offset(10, 0, 0);
			case 1 -> node.offset(17, 1, 0);
			default -> node.offset(28, 4, 0);
		};
	}

	/** Raises the next synapse of the chain: floor, then wall, then ceiling, with the wall and roof it grows from. */
	static void raiseChain(ServerPlayer player, BlockPos node) {
		if (player == null || node == null || raised >= FACES.length) return;
		int index = raised++;
		ServerLevel level = player.serverLevel();
		BlockPos iron = iron(node, index);
		if (index == 0) {
			// Clear a standing cell on open ground so the floor synapse shows instead of sinking into a dip.
			for (BlockPos pos : BlockPos.betweenClosed(iron.offset(-1, 0, -1), iron.offset(1, 3, 1)))
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			for (BlockPos pos : BlockPos.betweenClosed(iron.offset(-1, -1, -1), iron.offset(1, -1, 1)))
				level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
		} else if (index == 1) {
			for (int y = -1; y <= 3; y++) {
				for (int z = -1; z <= 1; z++) level.setBlockAndUpdate(iron.offset(1, y, z), BlockInit.erythrocytic_mycelium.get().defaultBlockState());
			}
		} else if (index == 2) {
			for (BlockPos pos : BlockPos.betweenClosed(iron.offset(-2, 1, -2), iron.offset(2, 1, 2)))
				level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
		}
		BlockPos crystal = iron.relative(FACES[index]);
		level.setBlockAndUpdate(iron, BlockInit.hematic_iron_block.get().defaultBlockState());
		level.setBlockAndUpdate(crystal, BlockInit.sporite_crystal.get().defaultBlockState());
		HemoCapabilityAccess.getBloodVolume(player).setBloodVolume(2_000.0D);
		BloodStructureFeedManager.feedStructure(player, level, iron,
				new ItemStack(ItemInit.sanguine_formation.get()), 120.0D);
	}

	/** Frames synapse {@code index} close up, or the whole chain from above for {@code index} 3. */
	static void frame(ServerPlayer player, BlockPos node, int index) {
		if (player == null || node == null) return;
		ServerLevel level = player.serverLevel();
		if (index == 3) {
			player.teleportTo(level, node.getX() + 14.5, node.getY() + 22, node.getZ() + 18.5, 180f, 55f);
			HeartNodeClientReview.report(HeartNodeSavedData.get(level).entry(player.getUUID()) != null
					&& HeartNodeSavedData.get(level).entry(player.getUUID()).synapses().size() == FACES.length,
					"the chain of three synapses is recorded");
			return;
		}
		BlockPos iron = iron(node, index);
		var state = level.getBlockState(iron);
		HeartNodeClientReview.report(state.is(BlockInit.mycelial_synapse.get())
				&& state.getValue(MycelialSynapseBlock.FACING) == FACES[index],
				"the " + FACES[index] + " synapse is raised and points at its crystal");
		switch (index) {
			case 0 -> player.teleportTo(level, iron.getX() + 0.5, iron.getY() + 1.2, iron.getZ() + 2.6, 180f, 30f);
			case 1 -> player.teleportTo(level, iron.getX() - 2.7, iron.getY() - 0.4, iron.getZ() + 0.5, -90f, 5f);
			default -> player.teleportTo(level, iron.getX() + 0.5, iron.getY() - 3.0, iron.getZ() + 3.0, 180f, -40f);
		}
	}

	/** A neighbour's sanctum just west; a raise whose lobe would reach it is refused before any blood. */
	static void foreign(ServerPlayer player, BlockPos node) {
		if (player == null || node == null) return;
		ServerLevel level = player.serverLevel();
		HeartNodeSavedData data = HeartNodeSavedData.get(level);
		data.plant(NEIGHBOUR, node.offset(-38, 0, 0), level.getGameTime());
		BlockPos iron = node.offset(-18, 0, 0);
		level.setBlockAndUpdate(iron, BlockInit.hematic_iron_block.get().defaultBlockState());
		level.setBlockAndUpdate(iron.above(), BlockInit.sporite_crystal.get().defaultBlockState());
		level.setBlockAndUpdate(iron.above(2), Blocks.AIR.defaultBlockState());
		ItemStack catalyst = new ItemStack(ItemInit.sanguine_formation.get());
		double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		var match = BloodStructureCraftingHelper.findProjectionCraftMatch(player, level, iron, catalyst);
		HeartNodeClientReview.report(match.isPresent() && !match.get().valid()
				&& match.get().error().getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals("block.hemomancy.mycelial_synapse.foreign"),
				"a synapse reaching a neighbour's sanctum is refused as foreign");
		player.teleportTo(level, iron.getX() + 0.5, iron.getY() + 0.6, iron.getZ() + 3.2, 180f, 15f);
		BloodStructureFeedManager.feedStructure(player, level, iron, catalyst, 120.0D);
		HeartNodeClientReview.report(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == before
				&& level.getBlockState(iron).is(BlockInit.hematic_iron_block.get()),
				"the foreign refusal takes no blood and raises nothing");
	}
}
