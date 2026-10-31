package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Sets a Court site's residents on their marks as the site places, one chunk at a time: each resident mark in this
 * chunk whose role has a member type becomes that member, persistent, facing the way the mark faces, homed and leashed
 * to it. The Vent Sanctum's Head-tender has no mark (the threshold walkway is laid on the terrain's chimneys at place
 * time), so it stands where the walkway leaves the processional ring. Marks whose role is not built yet stay empty.
 */
public final class CourtResidentSpawner {
	private CourtResidentSpawner() {
	}

	public static void spawn(WorldGenLevel level, BoundingBox chunkBox, PiecesContainer pieces) {
		List<StructurePiece> here = pieces.pieces().stream().filter(piece -> piece.getBoundingBox().intersects(chunkBox)).toList();
		if (here.isEmpty()) return;
		for (var marker : CourtResidentMarkers.read(level.getLevel().getStructureManager(), here)) {
			if (!marker.resident() || !chunkBox.isInside(marker.pos())) continue;
			CourtRole role = CourtRole.forMark(marker.name());
			if (role != null) place(level, role, marker.pos(), marker.facing().toYRot(), marker.name());
		}
	}

	/** The Head-tender at the ring end of the threshold walkway, facing in toward the heads. */
	public static void spawnHeadTender(WorldGenLevel level, BoundingBox chunkBox, List<VentSanctumHeads.Spot> heads,
			BlockPos centre) {
		BlockPos stand = threshold(heads, centre);
		if (stand == null || !chunkBox.isInside(stand)) return;
		float yaw = (float) Math.toDegrees(Math.atan2(-(centre.getX() - stand.getX()), centre.getZ() - stand.getZ()));
		place(level, CourtRole.HEAD_TENDER, stand, yaw, "threshold");
	}

	/** Where the threshold walkway leaves the ring: above its top block farthest from the field's centre. */
	@Nullable
	public static BlockPos threshold(List<VentSanctumHeads.Spot> heads, BlockPos centre) {
		return heads.stream().filter(spot -> spot.kind() == VentSanctumHeads.Kind.WALK_TOP)
				.max(Comparator.comparingDouble(spot -> horizontal(spot.pos(), centre)))
				.map(spot -> spot.pos().above()).orElse(null);
	}

	private static double horizontal(BlockPos a, BlockPos b) {
		double dx = a.getX() - b.getX(), dz = a.getZ() - b.getZ();
		return dx * dx + dz * dz;
	}

	/** One member on its mark; false when the role has no member type yet. */
	public static boolean place(WorldGenLevel level, CourtRole role, BlockPos pos, float yaw, String mark) {
		var type = EntityInit.court_members.get(role);
		if (type == null) return false;
		CourtMemberEntity member = type.get().create(level.getLevel());
		if (member == null) return false;
		member.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yaw, 0.0F);
		member.settle(pos, yaw, mark);
		member.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
		level.addFreshEntityWithPassengers(member);
		return true;
	}
}
