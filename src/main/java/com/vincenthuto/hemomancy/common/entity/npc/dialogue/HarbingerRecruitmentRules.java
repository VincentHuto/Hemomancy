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
