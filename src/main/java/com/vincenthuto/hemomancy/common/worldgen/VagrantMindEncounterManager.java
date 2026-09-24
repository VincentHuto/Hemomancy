package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.VagrantMindPiece;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VagrantMindEncounterManager {
	private static final ResourceKey<Structure> MIND = ResourceKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("vagrant_mind"));

	private VagrantMindEncounterManager() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0
				|| !player.level().dimension().equals(Level.END) || player.level().getDifficulty() == Difficulty.PEACEFUL
				|| player.isSpectator()) return;
		ServerLevel level = player.serverLevel();
		var start = level.structureManager().getStructureWithPieceAt(player.blockPosition(), holder -> holder.is(MIND));
		if (!start.isValid()) return;
		VagrantMindPiece piece = start.getPieces().stream()
				.filter(VagrantMindPiece.class::isInstance).map(VagrantMindPiece.class::cast)
				.findFirst().orElse(null);
		if (piece == null || !piece.containsInterior(player.blockPosition())) return;
		List<BlockPos> nearbyNodes = piece.nodePositions().stream()
				.filter(node -> player.distanceToSqr(node.getX() + 0.5,
						node.getY() + 1.0, node.getZ() + 0.5) <= 96 * 96)
				.sorted(Comparator.comparingDouble(node -> node.distSqr(player.blockPosition())))
				.toList();
		if (nearbyNodes.isEmpty()) return;
		VagrantMindEncounterData data = VagrantMindEncounterData.get(level);
		var encounter = data.encounter(piece.origin());
		if (encounter != null) {
			if (encounter.defeated()) return;
			if (encounter.entity() != null) {
				Entity existing = level.getEntity(encounter.entity());
				if (existing instanceof NaeglerophaeonEntity) return;
				// An absent entity may simply be unloaded. Wait until its last chunk is present.
				if (!level.hasChunkAt(encounter.position())) return;
			}
		}
		for (NaeglerophaeonEntity nearby : level.getEntitiesOfClass(NaeglerophaeonEntity.class,
				new AABB(piece.origin()).inflate(112), boss -> piece.origin().equals(boss.mindOrigin()))) {
			data.active(piece.origin(), nearby.getUUID(), nearby.blockPosition());
			return;
		}
		NaeglerophaeonEntity boss = EntityInit.naeglerophaeon.get().create(level);
		if (boss == null) return;
		BlockPos landing = null;
		for (BlockPos node : nearbyNodes) {
			if (!level.hasChunkAt(node) || !level.getBlockState(node).is(com.vincenthuto.hemomancy.common.init.BlockInit.synaptic_node.get())) continue;
			for (int height = 1; height <= 3; height++) {
				BlockPos candidate = node.above(height);
				boss.moveTo(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5, 0.0F, 0.0F);
				if (level.noCollision(boss)) {
					landing = candidate;
					break;
				}
			}
			if (landing != null) break;
		}
		if (landing == null) return;
		boss.bindMind(piece.origin(), piece.seed(), piece.nodePositions());
		boss.setPersistenceRequired();
		if (level.addFreshEntity(boss)) data.active(piece.origin(), boss.getUUID(), landing);
	}
}
