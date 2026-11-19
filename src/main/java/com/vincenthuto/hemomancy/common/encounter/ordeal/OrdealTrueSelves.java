package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** The professor and the puppet as their true selves on an ordeal's stage. */
public final class OrdealTrueSelves {
	private OrdealTrueSelves() {
	}

	public static void markTrueSelf(Entity entity) {
		if (entity instanceof TrueSelf self) self.setTrueSelf(true);
	}

	/** The professor's true self, where their choreography has them enter, out of reach of the ring. */
	public static CircusPerformerEntity spawnProfessor(ServerLevel chamber, OrdealProfessor professor, ServerPlayer caster,
			BlockPos stage) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Hemomancy.rloc(professor.role()));
		if (!(type.create(chamber) instanceof CircusPerformerEntity performer)) return null;
		var at = professor.choreography().entrance(stage);
		performer.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
		performer.setPersistenceRequired();
		performer.setTrueSelf(true);
		return chamber.addFreshEntity(performer) ? performer : null;
	}
}
