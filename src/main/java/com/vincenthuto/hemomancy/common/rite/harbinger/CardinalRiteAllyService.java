package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

/**
 * Server-side bloodline participation and recipe-defined helper requirements.
 */
public final class CardinalRiteAllyService {
	private CardinalRiteAllyService() {
	}

	public static Map<CardinalRiteAllyRole, BlockPos> markers(CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return java.util.Map.of();
		return CardinalRiteNpcStationRules.roleMarkers(CardinalRiteInteractionHandler.occupiedSigilTargets(
				recipe.getCeremony().anchors(), CardinalRiteInteractionHandler.supportSigils(recipe)));
	}

	public static boolean tryClaimPlayerRole(ServerLevel level, ServerPlayer player, ActiveCardinalRite rite,
			BlockPos clicked) {
		if (rite.getPhase() != CardinalRitePhase.INSCRIPTION || rite.getDegree() < 5
				|| rite.getPlayerUUID().equals(player.getUUID())) return false;
		CardinalRiteAllyRole role = roleAt(level, rite, clicked);
		if (role == null || !supportsRole(CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId()), role)) return false;
		Bloodline line = bloodline(level, rite);
		if (line == null || !line.hasMember(player.getUUID())) return false;
        for (var entry : rite.getAllyRoles().entrySet()) {
            if (entry.getValue() != role || entry.getKey().equals(player.getUUID())) continue;
            var resident = CardinalRiteNpcTravel.find(level, entry.getKey());
            if (resident == null || !line.hasNpcMember(entry.getKey())
                    || !CardinalRiteNpcTravel.returnHome(level, resident)) return true;
            rite.removeAlly(entry.getKey());
        }
		int quota = helperQuota(level, rite);
		if (!rite.getAllyRoles().containsKey(player.getUUID()) && rite.getAllyRoles().size() >= quota) {
			player.displayClientMessage(Component.literal("Every bloodline station is already occupied.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return true;
		}
		rite.assignAlly(player.getUUID(), role);
		if (player.isShiftKeyDown()) {
			boolean optIn = !rite.getSharedPoolOptIns().contains(player.getUUID());
			rite.setSharedPoolOptIn(player.getUUID(), optIn);
			player.displayClientMessage(Component.literal("Shared blood access: " + (optIn ? "permitted" : "withheld"))
					.withStyle(ChatFormatting.GOLD), false);
		} else {
			player.displayClientMessage(Component.literal("You take the " + displayName(role) + " station.")
					.withStyle(ChatFormatting.RED), false);
		}
		return true;
	}

	public static boolean tryAssignNpc(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			Entity npc) {
        if (!com.vincenthuto.hemomancy.common.succession.SuccessionResidents.helper(npc)) return false;
		if (rite.getPhase() != CardinalRitePhase.INSCRIPTION || rite.getDegree() < 5
				|| !rite.getPlayerUUID().equals(caster.getUUID())) return false;
		Bloodline line = bloodline(level, rite);
		if (line == null || !line.hasNpcMember(npc.getUUID())) return false;
		if (line.isNpcBloodspent(npc.getUUID(), level.getGameTime())) {
			caster.displayClientMessage(Component.literal("That ally is Bloodspent and must rest for a full day.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return true;
		}
		int quota = helperQuota(level, rite);
		if (!rite.getAllyRoles().containsKey(npc.getUUID()) && rite.getAllyRoles().size() >= quota) {
			caster.displayClientMessage(Component.literal("Every bloodline station is already occupied.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return true;
		}
        if (!rite.getAllyRoles().containsKey(npc.getUUID()) && CardinalRiteNpcTravel.assigned(level, npc.getUUID())) return false;
        var recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
        CardinalRiteAllyRole next = nextRole(rite.getAllyRoles().get(npc.getUUID()));
        for (int i = 0; i < CardinalRiteAllyRole.values().length; i++) {
            if (supportsRole(recipe, next) && !rite.getAllyRoles().containsValue(next)) break;
            next = nextRole(next);
        }
        if (!supportsRole(recipe, next) || rite.getAllyRoles().containsValue(next)) return true;
		if (!(npc instanceof Mob mob) || !safeStation(level, mob, station(level, rite, next))) {
			caster.displayClientMessage(Component.literal(
					"That rite station has no safe footing for an ally.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return true;
		}
		CardinalRiteNpcTravel.remember(level, rite, mob);
		rite.assignAlly(npc.getUUID(), next);
		directToStation(level, rite, mob, next);
		caster.displayClientMessage(Component.literal("Assigned " + npc.getName().getString() + " as "
				+ displayName(next) + ".").withStyle(ChatFormatting.RED), false);
		return true;
	}

	/**
	 * NPCs consume the shared pool first and then their persistent private
	 * reserve. Player allies can touch the shared pool only after opting in.
	 */
	public static int spend(ServerLevel level, ActiveCardinalRite rite, UUID ally, int requestedMl) {
		if (requestedMl <= 0) return 0;
		Bloodline line = bloodline(level, rite);
		if (line == null) return 0;
		boolean npc = line.hasNpcMember(ally);
		if (!npc && !rite.getSharedPoolOptIns().contains(ally)) return 0;
		BloodlineSavedData data = BloodlineSavedData.get(level.getServer().overworld());
		int poolRequest = Math.min(requestedMl, Math.max(0, (int) line.getBloodVolume()));
		int fromPool = Math.round(data.drawBlood(line.getBloodlineUUID(), poolRequest));
		if (!npc || fromPool >= requestedMl) return fromPool;
		return fromPool + data.drawNpcRiteReserve(line.getBloodlineUUID(), ally,
				requestedMl - fromPool, level.getGameTime());
	}

	public static boolean trySpend(ServerLevel level, ActiveCardinalRite rite, UUID ally, int requestedMl) {
		if (requestedMl <= 0) return false;
		Bloodline line = bloodline(level, rite);
		if (line == null) return false;
		boolean npc = line.hasNpcMember(ally);
		if (!npc && !rite.getSharedPoolOptIns().contains(ally)) return false;
		int fromPool = Math.min(requestedMl, Math.max(0, (int) line.getBloodVolume()));
		int remaining = requestedMl - fromPool;
		if (remaining > 0 && (!npc || line.isNpcBloodspent(ally, level.getGameTime())
				|| line.getNpcRiteReserve(ally, level.getGameTime()) < remaining)) return false;
		return spend(level, rite, ally, requestedMl) == requestedMl;
	}

	public static boolean isAvailable(ServerLevel level, ActiveCardinalRite rite, UUID ally) {
		Bloodline line = bloodline(level, rite);
		if (line == null) return false;
		if (line.hasNpcMember(ally)) {
			if (line.isNpcBloodspent(ally, level.getGameTime())) return false;
			Entity entity = level.getEntity(ally);
			CardinalRiteAllyRole role = rite.getAllyRoles().get(ally);
			if (!(entity instanceof Mob mob) || role == null
                    || !com.vincenthuto.hemomancy.common.succession.SuccessionResidents.helper(entity)) return false;
			BlockPos station = station(level, rite, role);
			boolean safe = safeStation(level, mob, station);
			return CardinalRiteNpcStationRules.participates(mob.position(), station, safe);
		}
        var player = level.getServer().getPlayerList().getPlayer(ally);
        var role = rite.getAllyRoles().get(ally);
        return player != null && player.isAlive() && !player.isSpectator() && player.level() == level
                && line.hasMember(ally) && role != null
                && CardinalRiteNpcStationRules.participates(player.position(), station(level, rite, role), true);
	}

	public static void maintainNpcStations(ServerLevel level, ActiveCardinalRite rite) {
		Bloodline line = bloodline(level, rite);
        if (line == null || rite.isComplete() || rite.getPhase() == CardinalRitePhase.COLLAPSED) return;
        if (CardinalRiteNpcTravel.gathering(rite)) {
            for (var ally : rite.getAllyRoles().keySet()) {
                if (!line.hasNpcMember(ally) && !isAvailable(level, rite, ally)) rite.removeAlly(ally);
            }
            CardinalRiteNpcTravel.gather(level, rite, line);
        }
		for (var assignment : rite.getAllyRoles().entrySet()) {
			if (!line.hasNpcMember(assignment.getKey())) continue;
			Entity entity = level.getEntity(assignment.getKey());
			if (entity instanceof Mob mob && com.vincenthuto.hemomancy.common.succession.SuccessionResidents.helper(entity)) {
				directToStation(level, rite, mob, assignment.getValue());
			}
		}
	}

    public static void returnNpcAlliesToFane(ServerLevel level, ActiveCardinalRite rite) {
        for (var id : rite.getAllyRoles().keySet()) {
            if (!com.vincenthuto.hemomancy.common.succession.SuccessionSavedData.get(level).residents.containsKey(id)) continue;
            var role = rite.getAllyRoles().get(id);
            var position = station(level, rite, role);
            if (position != null) level.getChunkAt(position);
            var npc = CardinalRiteNpcTravel.find(level, id);
            if (npc != null) {
                CardinalRiteNpcTravel.remember(level, rite, npc);
                CardinalRiteNpcTravel.returnHome(level, npc);
            }
            rite.removeAlly(id);
        }
    }

	public static boolean hasRequiredHelperCount(int available, int required) {
		return Math.max(0, available) >= Math.max(0, required);
	}

	public static boolean hasRequiredHelpers(ServerLevel level, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return true;
		long available = rite.getAllyRoles().keySet().stream()
				.filter(ally -> isAvailable(level, rite, ally))
				.count();
		return hasRequiredHelperCount((int) available, recipe.getCeremony().requiredHelpers());
	}

	public static boolean tryCorrectMiss(ServerLevel level, ActiveCardinalRite rite) {
		for (var entry : rite.getAllyRoles().entrySet()) {
			if (entry.getValue() != CardinalRiteAllyRole.ATTENDANT
					|| rite.hasUsedAttendantCatch(entry.getKey())
					|| !isAvailable(level, rite, entry.getKey())) continue;
			if (trySpend(level, rite, entry.getKey(), 50)) {
				return rite.tryUseAttendantCatch(entry.getKey());
			}
		}
		return false;
	}

	private static Bloodline bloodline(ServerLevel level, ActiveCardinalRite rite) {
		return BloodlineSavedData.get(level.getServer().overworld()).getBloodlineForPlayer(rite.getPlayerUUID());
	}

    static boolean supportsRole(CardinalRiteRecipe recipe, CardinalRiteAllyRole role) {
        return recipe != null && recipe.getCeremony() != null
                && recipe.getCeremony().helperRoles().contains(role.name().toLowerCase(java.util.Locale.ROOT));
    }

	private static int helperQuota(ServerLevel level, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return 0;
		return recipe.getCeremony().helperRoles().size();
	}

	private static CardinalRiteAllyRole roleAt(ServerLevel level, ActiveCardinalRite rite, BlockPos clicked) {
		for (var marker : markers(CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId())).entrySet()) {
			BlockPos pos = rite.getCenterPos().offset(marker.getValue());
			if (clicked.closerThan(pos, 1.5D) || clicked.closerThan(pos.below(), 1.5D)) return marker.getKey();
		}
		return null;
	}

	private static void directToStation(ServerLevel level, ActiveCardinalRite rite,
			Mob npc, CardinalRiteAllyRole role) {
		BlockPos station = station(level, rite, role);
		boolean safe = safeStation(level, npc, station);
		switch (CardinalRiteNpcStationRules.correction(npc.position(), station, safe)) {
			case UNAVAILABLE -> npc.getNavigation().stop();
			case RECALL -> {
				npc.getNavigation().stop();
				npc.teleportTo(station.getX() + 0.5D, station.getY(), station.getZ() + 0.5D);
				npc.setDeltaMovement(Vec3.ZERO);
				npc.fallDistance = 0.0F;
			}
			case APPROACH -> {
				if (npc.getNavigation().isDone() || level.getGameTime() % 10L == 0L) {
					npc.getNavigation().moveTo(
							station.getX() + 0.5D, station.getY(), station.getZ() + 0.5D, 0.8D);
				}
			}
			case HOLD -> {
				npc.getNavigation().stop();
				if (npc.onGround()) {
					Vec3 motion = npc.getDeltaMovement();
					npc.setDeltaMovement(0.0D, motion.y, 0.0D);
				}
			}
		}
	}

	private static BlockPos station(ServerLevel level, ActiveCardinalRite rite, CardinalRiteAllyRole role) {
		BlockPos offset = markers(CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId())).get(role);
		return offset == null ? null : rite.getCenterPos().offset(offset);
	}

	static boolean safeStation(ServerLevel level, Mob npc, BlockPos station) {
		if (station == null) return false;
		boolean loaded = level.hasChunkAt(station);
		boolean sturdySupport = loaded && level.getBlockState(station.below())
				.isFaceSturdy(level, station.below(), Direction.UP);
		AABB targetBounds = npc.getBoundingBox().move(
				station.getX() + 0.5D - npc.getX(),
				station.getY() - npc.getY(),
				station.getZ() + 0.5D - npc.getZ());
		boolean collisionFree = loaded && level.noCollision(npc, targetBounds);
		return CardinalRiteNpcStationRules.stationSafe(loaded, sturdySupport, collisionFree);
	}

	private static CardinalRiteAllyRole nextRole(CardinalRiteAllyRole current) {
		if (current == null) return CardinalRiteAllyRole.ANCHOR;
		return switch (current) {
			case ANCHOR -> CardinalRiteAllyRole.ATTENDANT;
			case ATTENDANT -> CardinalRiteAllyRole.WARDEN;
			case WARDEN -> CardinalRiteAllyRole.ANCHOR;
		};
	}

	private static String displayName(CardinalRiteAllyRole role) {
		String lower = role.name().toLowerCase(java.util.Locale.ROOT);
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}
