package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Optional;

public final class HarbingerRecruitmentRules {
	public static final String NPC_OUTPOST_KEY = Hemomancy.MOD_ID + ":harbinger_recruitment_outpost";
	private static final ResourceKey<Structure> HARBINGER_OUTPOST = ResourceKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("harbinger_outpost"));

	private HarbingerRecruitmentRules() {
	}

	public static boolean canRecruitNpc(Bloodline bloodline, Entity npc) {
		ResourceLocation npcType = BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType());
		String npcOutpost = findOutpostKey(npc);
		return !bloodline.hasNpcMemberType(npcType)
				&& !bloodline.hasNpcMemberOutpost(npcOutpost);
	}

	public static void markOutpostOrigin(Entity entity, ResourceLocation dimension, BoundingBox outpostBox) {
		entity.getPersistentData().putString(NPC_OUTPOST_KEY, createOutpostKey(dimension, outpostBox));
	}

	public static void markOutpostOrigin(Entity entity, String outpostKey) {
		if (outpostKey != null && !outpostKey.isBlank()) {
			entity.getPersistentData().putString(NPC_OUTPOST_KEY, outpostKey);
		}
	}

	public static String createOutpostKey(ResourceLocation dimension, BoundingBox outpostBox) {
		return dimension + "|harbinger_outpost|"
				+ outpostBox.minX() + "," + outpostBox.minY() + "," + outpostBox.minZ() + "|"
				+ outpostBox.maxX() + "," + outpostBox.maxY() + "," + outpostBox.maxZ();
	}

	public static Optional<OutpostOrigin> outpostOrigin(String key) {
		if (key == null) return Optional.empty();
		String[] parts = key.split("\\|", -1);
		if (parts.length != 4 || !"harbinger_outpost".equals(parts[1])) return Optional.empty();
		ResourceLocation dimension = ResourceLocation.tryParse(parts[0]);
		String[] min = parts[2].split(",", -1);
		String[] max = parts[3].split(",", -1);
		if (dimension == null || min.length != 3 || max.length != 3) return Optional.empty();
		try {
			int minX = Integer.parseInt(min[0]);
			int minY = Integer.parseInt(min[1]);
			int minZ = Integer.parseInt(min[2]);
			int maxX = Integer.parseInt(max[0]);
			int maxY = Integer.parseInt(max[1]);
			int maxZ = Integer.parseInt(max[2]);
			if (maxX < minX || maxY < minY || maxZ < minZ
					|| minX < -30_000_000 || maxX > 30_000_000
					|| minZ < -30_000_000 || maxZ > 30_000_000) return Optional.empty();
			int minChunkX = Math.floorDiv(minX, 16);
			int maxChunkX = Math.floorDiv(maxX, 16);
			int minChunkZ = Math.floorDiv(minZ, 16);
			int maxChunkZ = Math.floorDiv(maxZ, 16);
			long chunks = ((long) maxChunkX - minChunkX + 1) * ((long) maxChunkZ - minChunkZ + 1);
			if (chunks > 144) return Optional.empty();
			return Optional.of(new OutpostOrigin(dimension, minChunkX, maxChunkX, minChunkZ, maxChunkZ));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public record OutpostOrigin(ResourceLocation dimension, int minChunkX, int maxChunkX,
			int minChunkZ, int maxChunkZ) {}

	public static String findOutpostKey(Entity entity) {
		CompoundTag data = entity.getPersistentData();
		if (data.contains(NPC_OUTPOST_KEY)) {
			String outpostKey = data.getString(NPC_OUTPOST_KEY);
			if (!outpostKey.isBlank()) {
				return outpostKey;
			}
		}
		if (!(entity.level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		StructureStart start = serverLevel.structureManager()
				.getStructureWithPieceAt(entity.blockPosition(), holder -> holder.is(HARBINGER_OUTPOST));
		if (start == null || !start.isValid()) {
			return null;
		}
		String outpostKey = createOutpostKey(serverLevel.dimension().location(), start.getBoundingBox());
		data.putString(NPC_OUTPOST_KEY, outpostKey);
		return outpostKey;
	}
}
