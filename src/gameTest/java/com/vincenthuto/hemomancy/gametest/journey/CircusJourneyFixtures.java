package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

final class CircusJourneyFixtures {
	private CircusJourneyFixtures() { }

	static BlockPos findClearOrigin(ServerPlayer player, boolean upperLane) {
		ServerLevel level = player.serverLevel();
		BlockPos base = HemoJourneyFixtures.findClearOrigin(player).above(upperLane ? 12 : 0);
		for (int radius = 0; radius <= 4; radius++) {
			for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
				if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) continue;
				BlockPos candidate = base.offset(x * 20, 0, z * 20);
				if (!canPrepare(level, candidate)) continue;
				if (!CircusPavilionSavedData.get(level).hasSite(level, candidate.above())) return candidate;
			}
		}
		throw new IllegalStateException("No clear, unused Circus fixture volume was found near the player");
	}

	static void prepare(ServerPlayer player, BlockPos origin) {
		ServerLevel level = HemoJourneyFixtures.fixtureLevel(player);
		if (!canPrepare(level, origin)) throw new IllegalStateException("The selected Circus fixture volume is occupied");
		player.getPersistentData().put(HemoJourneyFixtures.OWNED_BLOCKS_KEY, new ListTag());
		for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++)
			HemoJourneyFixtures.set(player, origin.offset(x, 0, z), Blocks.SMOOTH_STONE);
		spawn(level, EntityInit.circus_carousel.get(), origin.above(), origin);
		spawn(level, EntityInit.circus_ringmaster.get(), origin.offset(0, 8, 5), origin);
		for (var placement : List.of(
				new Placement(EntityInit.circus_fire_eater.get(), -4, -4),
				new Placement(EntityInit.circus_acrobat.get(), 4, -4),
				new Placement(EntityInit.circus_stilt_walker.get(), -4, 4),
				new Placement(EntityInit.circus_knife_thrower.get(), 4, 4))) {
			spawn(level, placement.type(), origin.offset(placement.x(), 1, placement.z()), origin);
		}
		HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(4);
		player.teleportTo(level, origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() - 7.5D, 0.0F, 0.0F);
	}

	private static boolean canPrepare(ServerLevel level, BlockPos origin) {
		for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) {
			BlockPos pos = origin.offset(x, 0, z);
			if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
					|| level.getBlockEntity(pos) != null || !level.getFluidState(pos).isEmpty()
					|| (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced())) return false;
		}
		return true;
	}

	private static <T extends Entity> void spawn(ServerLevel level, EntityType<T> type, BlockPos pos, BlockPos origin) {
		T entity = type.create(level);
		if (entity == null) throw new IllegalStateException("Could not create Circus fixture entity " + type);
		entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		entity.addTag(HemoJourneyFixtures.entityMarker(origin));
		if (!level.addFreshEntity(entity)) throw new IllegalStateException("Could not spawn Circus fixture entity " + type);
	}

	static CircusRingmasterEntity ringmaster(ServerPlayer player, BlockPos origin) {
		return one(player, origin, CircusRingmasterEntity.class);
	}

	static CircusCarouselEntity carousel(ServerPlayer player, BlockPos origin) {
		return one(player, origin, CircusCarouselEntity.class);
	}

	static List<CircusPerformerEntity> performers(ServerPlayer player, BlockPos origin) {
		return HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(CircusPerformerEntity.class,
				new net.minecraft.world.phys.AABB(origin).inflate(20.0D),
				performer -> performer.getTags().contains(HemoJourneyFixtures.entityMarker(origin)));
	}

	private static <T extends Entity> T one(ServerPlayer player, BlockPos origin, Class<T> type) {
		List<T> entities = HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(type, HemoJourneyFixtures.bounds(origin));
		if (entities.size() != 1) throw new IllegalStateException("Expected one " + type.getSimpleName() + ", found " + entities.size());
		return entities.getFirst();
	}

	private record Placement(EntityType<? extends CircusPerformerEntity> type, int x, int z) { }
}
