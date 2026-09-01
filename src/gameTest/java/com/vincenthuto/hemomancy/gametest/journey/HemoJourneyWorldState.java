package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.UUID;

/** Protects global bloodline and per-dimension Fane data touched by the journey. */
final class HemoJourneyWorldState {
	private static final String BLOODLINE = "bloodline";
	private static final String FANES = "founding_fanes";
	private static final String TEMP_BLOODLINE = "hemomancy.dev_test.journey.temp_bloodline";
	private static final String CHAMBER = "chamber";
	private static final String RESPAWN = "respawn";
	private static final String CHAIR_BOUND = "hemomancy:chamber_visit_chair_bound";
	private static final String ATTUNED = "hemomancy:chamber_visit_attuned";

	private HemoJourneyWorldState() { }

	static void capture(ServerPlayer player, CompoundTag snapshot) {
		snapshot.put(BLOODLINE, HemoCapabilityAccess.requireBloodVolume(player).getBloodLine().serialize());
		ListTag fanes = new ListTag();
		for (ServerLevel level : player.server.getAllLevels()) {
			FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
			FaneFootprint footprint = data.getAllFootprints().get(player.getUUID());
			if (footprint == null) continue;
			CompoundTag saved = new CompoundTag();
			saved.putString("dimension", level.dimension().location().toString());
			saved.putLong("heart", footprint.heart().asLong());
			long[] stakes = footprint.stakes().stream().mapToLong(BlockPos::asLong).toArray();
			saved.putLongArray("stakes", stakes);
			BlockPos recall = data.getRecallPoint(player.getUUID());
			if (recall != null) saved.putLong("recall", recall.asLong());
			fanes.add(saved);
		}
		snapshot.put(FANES, fanes);
		CompoundTag chamber = new CompoundTag();
		captureBoolean(player.getPersistentData(), chamber, CHAIR_BOUND, "chair");
		captureBoolean(player.getPersistentData(), chamber, ATTUNED, "attuned");
		snapshot.put(CHAMBER, chamber);
		CompoundTag respawn = new CompoundTag();
		respawn.putString("dimension", player.getRespawnDimension().location().toString());
		if (player.getRespawnPosition() != null) respawn.putLong("position", player.getRespawnPosition().asLong());
		respawn.putFloat("angle", player.getRespawnAngle());
		respawn.putBoolean("forced", player.isRespawnForced());
		snapshot.put(RESPAWN, respawn);
	}

	static boolean validSnapshot(CompoundTag snapshot) {
		return snapshot.contains(BLOODLINE, Tag.TAG_COMPOUND) && snapshot.contains(FANES, Tag.TAG_LIST)
				&& snapshot.contains(CHAMBER, Tag.TAG_COMPOUND) && snapshot.contains(RESPAWN, Tag.TAG_COMPOUND);
	}

	static void reset(ServerPlayer player, CompoundTag snapshot) {
		cleanupTemporary(player);
		removePlayerFanes(player);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodLine(Bloodline.NOBLOODLINE);
		BloodVolumeEvents.syncVolume(player, blood);
		player.getPersistentData().remove(CHAIR_BOUND);
		player.getPersistentData().remove(ATTUNED);
	}

	static void prepareFoundingFane(ServerPlayer player) {
		cleanupTemporary(player);
		UUID id = UUID.randomUUID();
		Bloodline line = new Bloodline("Journey Test Bloodline", player.getUUID(), id, new ArrayList<>());
		BloodlineSavedData.get(player.server.overworld()).registerBloodline(line);
		player.getPersistentData().putUUID(TEMP_BLOODLINE, id);
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setBloodLine(line);
		BloodVolumeEvents.syncVolume(player, blood);
	}

	static boolean accelerateFoundingFane(ServerPlayer player, BlockPos focusPos) {
		var rite = CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
		if (rite == null || !rite.getCenterPos().equals(focusPos)
				|| !rite.getRecipeId().equals(com.vincenthuto.hemomancy.Hemomancy.rloc("cardinal_rite/founding_fane"))) {
			return false;
		}
		rite.markComplete();
		CardinalRiteSavedData.get(player.serverLevel()).setDirty();
		return true;
	}

	static boolean accelerateRite(ServerPlayer player, BlockPos focusPos, ResourceLocation recipeId) {
		var rite = CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
		if (rite == null || !rite.getCenterPos().equals(focusPos) || !rite.getRecipeId().equals(recipeId)) return false;
		rite.markComplete();
		CardinalRiteSavedData.get(player.serverLevel()).setDirty();
		return true;
	}

	static boolean accelerateCovenantVigil(ServerPlayer player, BlockPos focusPos) {
		var rite = CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
		if (rite == null || !rite.getCenterPos().equals(focusPos)
				|| !rite.getRecipeId().equals(com.vincenthuto.hemomancy.Hemomancy.rloc("cardinal_rite/covenant_vigil"))) {
			return false;
		}
		int[] anchors = rite.getAnchorBloodMl();
		for (int index = 0; index < anchors.length; index++) rite.fillAnchor(index, 50);
		if (!rite.enterInscription()) return false;
		Mob helper = null;
		for (Entity entity : player.serverLevel().getEntitiesOfClass(Entity.class,
				HemoJourneyFixtures.bounds(BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY))),
				entity -> entity.getTags().contains(HemoJourneyFixtures.entityMarker(
						BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY)))))) {
			if (entity instanceof Mob mob) {
				helper = mob;
				break;
			}
		}
		if (helper == null || !CardinalRiteAllyService.tryAssignNpc(player.serverLevel(), player, rite, helper)
				|| !CardinalRiteAllyService.isAvailable(player.serverLevel(), rite, helper.getUUID())) return false;
		rite.markComplete();
		CardinalRiteSavedData.get(player.serverLevel()).setDirty();
		return true;
	}

	static void restore(ServerPlayer player, CompoundTag snapshot) {
		cleanupTemporary(player);
		removePlayerFanes(player);
		for (Tag value : snapshot.getList(FANES, Tag.TAG_COMPOUND)) {
			CompoundTag saved = (CompoundTag) value;
			ResourceLocation id = ResourceLocation.tryParse(saved.getString("dimension"));
			if (id == null) continue;
			ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
			if (level == null) continue;
			FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
			data.consecrateHeart(player.getUUID(), BlockPos.of(saved.getLong("heart")));
			for (long stake : saved.getLongArray("stakes")) {
				data.addStake(player.getUUID(), BlockPos.of(stake), FaneFootprint.MAX_STAKE_BUDGET);
			}
			if (saved.contains("recall", Tag.TAG_LONG)) {
				data.setRecallPoint(player.getUUID(), BlockPos.of(saved.getLong("recall")));
			}
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		Bloodline savedLine = Bloodline.deserialize(snapshot.getCompound(BLOODLINE));
		Bloodline globalLine = savedLine.isValid()
				? BloodlineSavedData.get(player.server.overworld()).getBloodline(savedLine.getBloodlineUUID()) : null;
		blood.setBloodLine(globalLine != null ? globalLine : savedLine);
		BloodVolumeEvents.syncVolume(player, blood);
		CompoundTag chamber = snapshot.getCompound(CHAMBER);
		restoreBoolean(player.getPersistentData(), chamber, CHAIR_BOUND, "chair");
		restoreBoolean(player.getPersistentData(), chamber, ATTUNED, "attuned");
		CompoundTag respawn = snapshot.getCompound(RESPAWN);
		ResourceLocation respawnId = ResourceLocation.tryParse(respawn.getString("dimension"));
		ResourceKey<Level> respawnDimension = respawnId == null ? Level.OVERWORLD
				: ResourceKey.create(Registries.DIMENSION, respawnId);
		BlockPos respawnPos = respawn.contains("position", Tag.TAG_LONG)
				? BlockPos.of(respawn.getLong("position")) : null;
		player.setRespawnPosition(respawnDimension, respawnPos, respawn.getFloat("angle"),
				respawn.getBoolean("forced"), false);
	}

	static void leaveTemporaryChamber(ServerPlayer player) {
		if (ChamberVisitService.isActive(player)
				|| player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			ChamberVisitService.returnFromVisit(player);
		}
	}

	static void cleanupTemporary(ServerPlayer player) {
		CompoundTag playerData = player.getPersistentData();
		boolean hasTemporaryBloodline = playerData.hasUUID(TEMP_BLOODLINE);
		for (ServerLevel level : player.server.getAllLevels()) {
			if (hasTemporaryBloodline) {
				var rites = CardinalRiteSavedData.get(level);
				var rite = rites.getRite(player.getUUID());
				if (rite != null) {
					rites.removeRite(player.getUUID());
				}
			}
			FoundingFaneSavedData fanes = FoundingFaneSavedData.get(level);
			BlockPos heart = fanes.getHeart(player.getUUID());
			if (hasTemporaryBloodline && heart != null
					&& level.getBlockState(heart).is(BlockInit.consecrated_bloodwell.get())) {
				level.setBlock(heart, Blocks.AIR.defaultBlockState(), 3);
			}
			fanes.remove(player.getUUID());
		}
		if (hasTemporaryBloodline) {
			BloodlineSavedData.get(player.server.overworld()).disbandBloodline(playerData.getUUID(TEMP_BLOODLINE));
			playerData.remove(TEMP_BLOODLINE);
		}
	}

	private static void removePlayerFanes(ServerPlayer player) {
		for (ServerLevel level : player.server.getAllLevels()) {
			FoundingFaneSavedData.get(level).remove(player.getUUID());
		}
	}

	private static void captureBoolean(CompoundTag source, CompoundTag target, String key, String name) {
		target.putBoolean(name + "_present", source.contains(key, Tag.TAG_BYTE));
		target.putBoolean(name, source.getBoolean(key));
	}

	private static void restoreBoolean(CompoundTag target, CompoundTag source, String key, String name) {
		if (source.getBoolean(name + "_present")) target.putBoolean(key, source.getBoolean(name));
		else target.remove(key);
	}
}
