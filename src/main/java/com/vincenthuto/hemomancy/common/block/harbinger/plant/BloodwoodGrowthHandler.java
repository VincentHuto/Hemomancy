package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartwoodCoreBlockEntity;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blood projected into a plant grows a tree: any sapling becomes a Blood Wood tree, and a Dead Bush becomes a Heartwood
 * Bloodwood tree with one hungry core. The plant keeps what it has drunk across casts, for ten minutes of neglect.
 */
public final class BloodwoodGrowthHandler {
	private static final BloodAccounting.Source GROWTH = BloodAccounting.Source.of("bloodwood_growth", Category.WORLD, true, false);
	private static final long PROGRESS_EXPIRY_TICKS = 20L * 60L * 10L;
	private static final ParticleColor BLOOD = new ParticleColor(190, 18, 34);
	private static final Map<GrowthKey, GrowthProgress> PROGRESS = new ConcurrentHashMap<>();

	private BloodwoodGrowthHandler() {
	}

	public static void clearSessionState() {
		PROGRESS.clear();
	}

	private enum Kind {
		BLOOD_WOOD(BloodwoodGrowthRules.PROJECTION_BLOOD_COST, "message.hemomancy.bloodwood_growth_handler.sapling_needs_space",
				"message.hemomancy.bloodwood_growth_handler.sapling_grown"),
		HEARTWOOD(HeartwoodTreeRules.PROJECTION_BLOOD_COST, "message.hemomancy.bloodwood_growth_handler.dead_bush_needs_space",
				"message.hemomancy.bloodwood_growth_handler.dead_bush_grown");

		private final double cost;
		private final String needsSpace;
		private final String grown;

		Kind(double cost, String needsSpace, String grown) {
			this.cost = cost;
			this.needsSpace = needsSpace;
			this.grown = grown;
		}
	}

	private static Kind kindOf(BlockState state) {
		if (state.is(Blocks.DEAD_BUSH)) return Kind.HEARTWOOD;
		return state.is(BlockTags.SAPLINGS) || state.getBlock() instanceof SaplingBlock ? Kind.BLOOD_WOOD : null;
	}

	public static double tryGrowFromProjection(ServerLevel level, BlockPos pos, ServerPlayer player, double maxAmount) {
		Kind kind = maxAmount <= 0.0D ? null : kindOf(level.getBlockState(pos));
		if (kind == null) {
			return 0.0D;
		}
		if (!PlayerBlockEdits.mayBreak(player, pos)) {
			return 0.0D;
		}
		List<HeartwoodTreeRules.Cell> wood = kind == Kind.HEARTWOOD ? HeartwoodTreeRules.grow(pos.asLong()).logs() : BloodwoodGrowthRules.grow(pos.asLong()).logs();
		if (!hasClearance(level, pos, wood, player)) {
			player.displayClientMessage(Component.translatable(kind.needsSpace).withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() <= 0.0D) {
			player.displayClientMessage(Component.translatable("message.hemomancy.bloodwood_growth_handler.no_active_blood").withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		GrowthKey key = new GrowthKey(level.dimension(), pos.immutable());
		long gameTime = level.getGameTime();
		GrowthProgress progress = PROGRESS.get(key);
		double current = progress == null || gameTime - progress.lastUpdatedTick() > PROGRESS_EXPIRY_TICKS
				? 0.0D
				: progress.amount();
		double remaining = Math.max(0.0D, kind.cost - current);
		double transfer = Math.min(maxAmount, Math.min(remaining, volume.getBloodVolume()));
		if (transfer <= 0.0D || !BloodAccounting.pay(player, transfer, GROWTH).satisfied()) {
			return 0.0D;
		}

		double updated = current + transfer;
		if (updated + 0.001D < kind.cost) {
			PROGRESS.put(key, new GrowthProgress(updated, gameTime));
			return transfer;
		}

		PROGRESS.remove(key);
		if (kind == Kind.BLOOD_WOOD) growBloodWood(level, pos, BloodwoodGrowthRules.grow(pos.asLong()), player);
		else growHeartwood(level, pos, HeartwoodTreeRules.grow(pos.asLong()));
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 24, 0.5D, 0.8D, 0.5D, 0.03D);
		level.playSound(null, pos, SoundEvents.AZALEA_PLACE, SoundSource.BLOCKS, 1.0F, 0.55F);
		player.displayClientMessage(Component.translatable(kind.grown).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		return transfer;
	}

	/** Only wood needs its cells free; leaves grow around whatever they meet. A Heartwood core takes a trunk cell's place, so it is checked with them. */
	private static boolean hasClearance(ServerLevel level, BlockPos origin, List<HeartwoodTreeRules.Cell> wood, ServerPlayer player) {
		for (HeartwoodTreeRules.Cell cell : wood) {
			if (!clear(level, origin, origin.offset(cell.x(), cell.y(), cell.z()), player)) return false;
		}
		return clear(level, origin, origin.above(HeartwoodTreeRules.CORE_HEIGHT), player);
	}

	private static boolean clear(ServerLevel level, BlockPos origin, BlockPos pos, ServerPlayer player) {
		return pos.equals(origin) || !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).canBeReplaced()
				&& ManipulationCombatHelper.mayAlter(player, pos);
	}

	private static void growBloodWood(ServerLevel level, BlockPos origin, BloodwoodGrowthRules.Shape shape, ServerPlayer player) {
		BlockState log = BlockInit.blood_wood_log.get().defaultBlockState();
		BlockState leaves = BlockInit.blood_wood_leaves.get().defaultBlockState()
				.setValue(LeavesBlock.PERSISTENT, true);
		for (HeartwoodTreeRules.Cell cell : shape.logs()) {
			level.setBlock(origin.offset(cell.x(), cell.y(), cell.z()), log.setValue(RotatedPillarBlock.AXIS, axis(cell.axis())), Block.UPDATE_ALL);
		}
		for (BloodwoodGrowthRules.Offset offset : shape.leaves()) {
			BlockPos pos = origin.offset(offset.x(), offset.y(), offset.z());
			if (!level.isOutsideBuildHeight(pos) && level.getBlockState(pos).canBeReplaced() && ManipulationCombatHelper.mayAlter(player, pos)) {
				level.setBlock(pos, leaves, Block.UPDATE_ALL);
			}
		}
	}

	private static void growHeartwood(ServerLevel level, BlockPos origin, HeartwoodTreeRules.Shape shape) {
		BlockState log = BlockInit.HEARTWOOD.log().get().defaultBlockState();
		List<BlockPos> placed = new ArrayList<>(shape.logs().size());
		for (HeartwoodTreeRules.Cell cell : shape.logs()) {
			BlockPos pos = origin.offset(cell.x(), cell.y(), cell.z());
			level.setBlock(pos, log.setValue(RotatedPillarBlock.AXIS, axis(cell.axis())), Block.UPDATE_ALL);
			placed.add(pos);
		}
		BlockPos corePos = origin.offset(shape.core().x(), shape.core().y(), shape.core().z());
		level.setBlock(corePos, BlockInit.heartwood_core.get().defaultBlockState(), Block.UPDATE_ALL);
		if (level.getBlockEntity(corePos) instanceof HeartwoodCoreBlockEntity core) {
			core.bindTree(placed);
		}
	}

	private static Direction.Axis axis(HeartwoodTreeRules.Axis axis) {
		return switch (axis) {
			case X -> Direction.Axis.X;
			case Y -> Direction.Axis.Y;
			case Z -> Direction.Axis.Z;
		};
	}

	private record GrowthKey(ResourceKey<net.minecraft.world.level.Level> dimension, BlockPos pos) {
	}

	private record GrowthProgress(double amount, long lastUpdatedTick) {
	}
}
