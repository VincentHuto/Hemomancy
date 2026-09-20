package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.succession.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.UUID;

/** A helper trip belongs to the existing resident entity, including while it is unloaded. */
public final class CardinalRiteNpcTravel {
    private static final String TRIP = "HematicRiteHelperTrip";
    private static final TicketType<UUID> TRAVEL_TICKET = TicketType.create("hemomancy_rite_helper", UUID::compareTo, 200);

    private CardinalRiteNpcTravel() {}

    public static void gather(ServerLevel level, ActiveCardinalRite rite, Bloodline line) {
        if (line == null || !gathering(rite)) return;
        var recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
        if (recipe == null || recipe.getCeremony() == null) return;
        int required = recipe.getCeremony().requiredHelpers();
        for (var id : rite.getAllyRoles().keySet()) {
            var npc = find(level, id);
            if (npc == null || !npc.getPersistentData().getCompound(TRIP).getBoolean("Automatic")) continue;
            if (rite.getAllyRoles().size() > required || !SuccessionResidents.helper(npc)
                    || line.isNpcBloodspent(id, level.getGameTime())) {
                returnHome(level, npc); rite.removeAlly(id);
            }
        }
        if (required == 0) return;
        var data = SuccessionSavedData.get(level);
        for (var resident : data.residents.values().stream().sorted(Comparator.comparing(r -> r.id)).toList()) {
            if (rite.getAllyRoles().size() >= required) break;
            if (!resident.bloodline.equals(line.getBloodlineUUID()) || !line.hasNpcMember(resident.id)
                    || resident.displaced || resident.dormant || resident.dismissed
                    || line.isNpcBloodspent(resident.id, level.getGameTime()) || assigned(level, resident.id)) continue;
            var home = dimension(level.getServer(), resident.dimension);
            if (home == null) continue;
            var workplace = BlockPos.of(resident.workplace);
            // Loading the recorded home can load the existing entity on a later tick; never manufacture a replacement.
            home.resetEmptyTime();
            home.getChunkSource().addRegionTicket(TRAVEL_TICKET, new ChunkPos(workplace), 2, resident.id, true);
            home.getChunkAt(workplace);
            if (!SuccessionWorkplaces.valid(home, resident.faneOwner, resident.profession, workplace)
                    || !data.ledger.owns(resident.id, resident.place())) continue;
            if (home.getEntity(resident.id) == null) {
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                    home.getChunkAt(workplace.offset(x * 16, 0, z * 16));
                continue;
            }
            if (!(home.getEntity(resident.id) instanceof Mob npc) || !SuccessionResidents.helper(npc)
                    || npc.getPersistentData().contains(TRIP)
                    || !FoundingFaneSavedData.get(home).isWithinFane(resident.faneOwner, npc.blockPosition())) continue;
            for (var role : CardinalRiteAllyRole.values()) {
                if (!CardinalRiteAllyService.supportsRole(recipe, role) || rite.getAllyRoles().containsValue(role)) continue;
                var station = levelStation(level, rite, role);
                if (!CardinalRiteAllyService.safeStation(level, npc, station)) continue;
                remember(level, rite, npc);
                npc.getPersistentData().getCompound(TRIP).putBoolean("Automatic", true);
                if (move(npc, level, Vec3.atBottomCenterOf(station)) != null) {
                    rite.assignAlly(resident.id, role);
                    CardinalRiteSavedData.get(level).setDirty();
                } else npc.getPersistentData().remove(TRIP);
                break;
            }
        }
    }

    public static boolean gathering(ActiveCardinalRite rite) {
        return rite.getPhase() == CardinalRitePhase.CONSECRATION || rite.getPhase() == CardinalRitePhase.INSCRIPTION;
    }

    public static boolean assigned(ServerLevel level, UUID npc) {
        for (var world : level.getServer().getAllLevels())
            for (var rite : CardinalRiteSavedData.get(world).getActiveRites().values())
                if (rite.getAllyRoles().containsKey(npc)) return true;
        return false;
    }

    public static void remember(ServerLevel level, ActiveCardinalRite rite, Mob npc) {
        if (npc.getPersistentData().contains(TRIP)) return;
        var trip = new CompoundTag();
        trip.putUUID("Caster", rite.getPlayerUUID());
        trip.putString("RiteDimension", level.dimension().location().toString());
        trip.putLong("Center", rite.getCenterPos().asLong());
        npc.getPersistentData().put(TRIP, trip);
    }

    /** Stops ordinary residency movement during a rite and finishes interrupted returns on entity reload. */
    public static boolean tick(ServerLevel level, ProfessionalHarbingerEntity npc) {
        if (!npc.getPersistentData().contains(TRIP)) return false;
        var trip = npc.getPersistentData().getCompound(TRIP);
        if (trip.getBoolean("Returning")) { returnHome(level, npc); return true; }
        var data = SuccessionSavedData.get(level);
        var resident = data.residents.get(npc.getUUID());
        var home = resident == null ? null : dimension(level.getServer(), resident.dimension);
        if (home != null && home.hasChunkAt(BlockPos.of(resident.workplace))
                && !SuccessionWorkplaces.valid(home, resident.faneOwner, resident.profession, BlockPos.of(resident.workplace))) {
            resident.displaced = true;
            data.ledger.unclaim(resident.id);
            data.setDirty();
        }
        var world = dimension(level.getServer(), trip.getString("RiteDimension"));
        var rite = world == null ? null : CardinalRiteSavedData.get(world).getRite(trip.getUUID("Caster"));
        if (rite == null || rite.getCenterPos().asLong() != trip.getLong("Center")
                || !rite.getAllyRoles().containsKey(npc.getUUID()) || rite.isComplete()
                || rite.getPhase() == CardinalRitePhase.COLLAPSED || !SuccessionResidents.helper(npc)) {
            returnHome(level, npc);
        }
        return true;
    }

    public static boolean returnHome(ServerLevel level, Mob npc) {
        var trip = npc.getPersistentData().getCompound(TRIP);
        trip.putBoolean("Returning", true);
        npc.getPersistentData().put(TRIP, trip);
        var resident = SuccessionSavedData.get(level).residents.get(npc.getUUID());
        if (resident == null) return false;
        var home = dimension(level.getServer(), resident.dimension);
        if (home == null) return false;
        var workplace = BlockPos.of(resident.workplace);
        home.resetEmptyTime();
        home.getChunkSource().addRegionTicket(TRAVEL_TICKET, new ChunkPos(workplace), 2, npc.getUUID(), true);
        // Return beside the workstation, never inside its collision box or to the generic fane recall point.
        for (int radius = 1; radius <= 3; radius++) for (int y : new int[]{0, 1, -1})
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                var destination = workplace.offset(x, y, z);
                home.getChunkAt(destination);
                if (!CardinalRiteAllyService.safeStation(home, npc, destination)) continue;
                var returned = move(npc, home, Vec3.atBottomCenterOf(destination));
                if (returned == null) return false;
                returned.getPersistentData().remove(TRIP);
                return true;
            }
        return false;
    }

    static Mob find(ServerLevel level, UUID id) {
        for (var world : level.getServer().getAllLevels())
            if (world.getEntity(id) instanceof Mob mob) return mob;
        return null;
    }

    private static BlockPos levelStation(ServerLevel level, ActiveCardinalRite rite, CardinalRiteAllyRole role) {
        var offset = CardinalRiteAllyService.markers(CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId())).get(role);
        return offset == null ? null : rite.getCenterPos().offset(offset);
    }

    private static ServerLevel dimension(MinecraftServer server, String id) {
        var location = ResourceLocation.tryParse(id);
        return location == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
    }

    private static Mob move(Mob npc, ServerLevel destination, Vec3 position) {
        npc.getNavigation().stop(); npc.stopRiding(); npc.setTarget(null);
        npc.setDeltaMovement(Vec3.ZERO); npc.fallDistance = 0;
        if (npc.level() == destination) {
            npc.teleportTo(position.x, position.y, position.z);
            return npc;
        }
        var moved = npc.changeDimension(new DimensionTransition(destination, position, Vec3.ZERO,
                npc.getYRot(), npc.getXRot(), DimensionTransition.DO_NOTHING));
        return moved instanceof Mob mob ? mob : null;
    }
}
