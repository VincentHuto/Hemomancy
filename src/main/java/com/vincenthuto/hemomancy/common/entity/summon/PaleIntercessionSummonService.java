package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PaleIntercessionSummonService {
	private PaleIntercessionSummonService() { }

	public static boolean summonOrRecall(ServerPlayer player) {
		boolean eligible = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasClarityUnlocked()
						&& EnumClarityStage.byClarity(progress.getClarity()).getLevel() >= EnumClarityStage.RESOLUTE.getLevel())
				.orElse(false);
		boolean known = HemoCapabilityAccess.getKnownStillArts(player)
				.map(arts -> arts.isKnown(StillArtInit.pale_intercession.get())).orElse(false);
		if (!eligible || !known) return false;

		List<PaleIntercessionEntity> bodies = new ArrayList<>();
		for (ServerLevel level : player.server.getAllLevels()) {
			for (var entity : level.getAllEntities()) {
				if (entity instanceof PaleIntercessionEntity body && player.getUUID().equals(body.getOwnerUUID())) bodies.add(body);
			}
		}
		UUID markedUuid = player.getPersistentData().hasUUID(PaleIntercessionEntity.ACTIVE_MARKER)
				? player.getPersistentData().getUUID(PaleIntercessionEntity.ACTIVE_MARKER) : null;
		PaleIntercessionEntity active = markedUuid == null ? null : bodies.stream()
				.filter(body -> markedUuid.equals(body.getUUID())).findFirst().orElse(null);
		PaleIntercessionEntity current = active != null && active.level() == player.level() ? active : null;
		Optional<Vec3> safe = safePosition(player, current != null ? current : EntityInit.spectral_companion.get().create(player.level()));
		if (safe.isEmpty()) {
			if (player.connection != null) {
				player.displayClientMessage(Component.translatable("hemomancy.still_art.pale_intercession.failure"), true);
			}
			return false;
		}
		for (PaleIntercessionEntity body : bodies) {
			if (body == current) continue;
			if (body == active) body.beginDissolution();
			else body.discard();
		}
		if (current == null) {
			current = EntityInit.spectral_companion.get().create(player.level());
			if (current == null) return false;
			current.bindTo(player);
			current.moveTo(safe.get().x, safe.get().y, safe.get().z, player.getYRot(), 0);
			player.level().addFreshEntity(current);
		} else {
			current.recallTo(safe.get());
			if (player.connection != null) {
				player.displayClientMessage(Component.translatable("hemomancy.still_art.pale_intercession.recall"), true);
			}
		}
		player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, current.getUUID());
		player.level().playSound(null, BlockPos.containing(safe.get()), SoundInit.ENTITY_PALE_INTERCESSION_MANIFEST.get(),
				SoundSource.PLAYERS, 0.9f, 1.0f);
		current.paleEffects(28);
		return true;
	}

	public static Optional<Vec3> safePosition(ServerPlayer owner, PaleIntercessionEntity entity) {
		if (entity == null) return Optional.empty();
		Vec3 origin = owner.position();
		double[][] offsets = {{1.4,0},{-1.4,0},{0,1.4},{0,-1.4},{1,1},{-1,1},{1,-1},{-1,-1},{0,0}};
		Vec3 old = entity.position();
		for (double[] offset : offsets) {
			Vec3 candidate = origin.add(offset[0], 0, offset[1]);
			entity.setPos(candidate);
			if (owner.level().noCollision(entity)) {
				entity.setPos(old);
				return Optional.of(candidate);
			}
		}
		entity.setPos(old);
		return Optional.empty();
	}
}
