package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SuccessionResidents {
    private SuccessionResidents() {}
    public static Bloodline line(ServerPlayer player) {
        return BloodlineSavedData.get(player.server.overworld()).getBloodlineForPlayer(player.getUUID());
    }
    public static boolean mayServe(ServerPlayer player, ProfessionalHarbingerEntity npc) {
        if (!npc.isSuccessor()) return true;
        var data = SuccessionSavedData.get(player.serverLevel());
        var r = data.residents.get(npc.getUUID()); var life = data.ledger.life(npc.getUUID());
        var line = line(player);
        return r != null && life != null && life.alive() && npc.isAlive() && !r.displaced && !r.dormant && !r.dismissed
                && line != null && line.hasMember(player.getUUID()) && r.bloodline.equals(line.getBloodlineUUID());
    }
    public static boolean helper(Entity entity) {
        if (!(entity instanceof ProfessionalHarbingerEntity npc) || !npc.isSuccessor() || !(npc.level() instanceof ServerLevel level)) return false;
        var data = SuccessionSavedData.get(level); var r = data.residents.get(npc.getUUID());
        var life = data.ledger.life(npc.getUUID());
        return r != null && life != null && life.alive() && !r.dormant && !r.dismissed && !r.displaced && npc.isAlive()
                && BloodlineSavedData.get(level.getServer().overworld()).getBloodline(r.bloodline) != null;
    }
    public static void tick(ServerLevel level, ProfessionalHarbingerEntity npc) {
        if (!npc.isSuccessor()) { returnOriginalHome(level, npc); return; }
        if (com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteNpcTravel.tick(level, npc)) return;
        var data = SuccessionSavedData.get(level); var r = data.residents.get(npc.getUUID());
        if (r == null) return;
        var line = BloodlineSavedData.get(level.getServer().overworld()).getBloodline(r.bloodline);
        if (line == null || r.dismissed) {
            r.dormant = true; data.ledger.unclaim(r.id); npc.getNavigation().stop(); data.setDirty(); return;
        }
        var life = data.ledger.life(r.id);
        if (life == null || !life.alive()) { npc.discard(); return; }
        if (!level.dimension().location().toString().equals(r.dimension)) return;
        var pos = BlockPos.of(r.workplace);
        if (!level.hasChunkAt(pos)) return;
        if (!SuccessionWorkplaces.valid(level, r.faneOwner, r.profession, pos) || !data.ledger.owns(r.id, r.place())) {
            data.ledger.unclaim(r.id); r.displaced = true;
            var replacement = SuccessionWorkplaces.find(level, r.faneOwner, r.profession, npc.blockPosition());
            if (replacement != null) {
                var place = new SuccessionLedger.Workplace(r.dimension, replacement.asLong());
                if (data.ledger.claim(r.id, place)) { r.workplace = replacement.asLong(); r.displaced = false; }
            }
            data.setDirty();
        }
        if (r.displaced) return;
        boolean helping = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).getActiveRites().values().stream()
                .anyMatch(rite -> rite.getAllyRoles().containsKey(r.id));
        if (!helping && npc.blockPosition().distSqr(BlockPos.of(r.workplace)) > 6)
            npc.getNavigation().moveTo(BlockPos.of(r.workplace).getX() + 1.5,
                    BlockPos.of(r.workplace).getY(), BlockPos.of(r.workplace).getZ() + .5, 1);
    }
    public static void died(ServerLevel level, ProfessionalHarbingerEntity npc) {
        var data = SuccessionSavedData.get(level); var r = data.residents.get(npc.getUUID());
        if (r == null) return;
        int generation = data.ledger.die(r.id);
        if (generation < 0) return;
        var bloodlines = BloodlineSavedData.get(level.getServer().overworld()); var line = bloodlines.getBloodline(r.bloodline);
        if (line != null) {
            r.reserve = line.getNpcRiteReserve(r.id, level.getGameTime()); r.bloodspentUntil = line.getNpcBloodspentUntil(r.id);
            line.removeNpcMember(r.id); line.clampPoolToCapacity(); bloodlines.setDirty(); sync(level, line);
        }
        if (!r.dismissed) npc.spawnAtLocation(BoundMnemonicRemnantItem.create(r, generation));
        data.setDirty();
    }
    public static void join(ServerLevel level, SuccessorRecord r) {
        var data = BloodlineSavedData.get(level.getServer().overworld()); var line = data.getBloodline(r.bloodline);
        if (line == null || r.dismissed) { r.dormant = true; SuccessionSavedData.get(level).ledger.unclaim(r.id); return; }
        line.addSuccessorMember(r.id, r.reserve, r.bloodspentUntil); data.setDirty(); sync(level, line);
    }
    public static void sync(ServerLevel level, Bloodline line) {
        for (var player : level.getServer().getPlayerList().getPlayers()) if (line.hasMember(player.getUUID()))
            HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> { volume.setBloodLine(line); BloodVolumeEvents.syncVolume(player, volume); });
    }
    private static void returnOriginalHome(ServerLevel level, ProfessionalHarbingerEntity npc) {
        var tag = npc.getPersistentData();
        String origin = tag.getString(HarbingerRecruitmentRules.NPC_OUTPOST_KEY);
        if (origin.isBlank() || tag.getBoolean("SuccessionHomeChecked")) return;
        String[] parts = origin.split("\\|");
        if (parts.length != 4) return;
        try {
            String[] min = parts[2].split(","), max = parts[3].split(",");
            int x0 = Integer.parseInt(min[0]), z0 = Integer.parseInt(min[2]);
            int x1 = Integer.parseInt(max[0]), z1 = Integer.parseInt(max[2]);
            var home = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(parts[0])));
            if (home == null) return;
            if (home == level && npc.getX() >= x0 && npc.getX() <= x1 + 1 && npc.getZ() >= z0 && npc.getZ() <= z1 + 1) {
                tag.putBoolean("SuccessionHomeChecked", true); return;
            }
            // Never force-load an outpost or guess a missing origin. Retry when its chunks are loaded.
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
                if (!home.hasChunk(x >> 4, z >> 4)) continue;
                int y = home.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                var pos = new BlockPos(x, y, z);
                if (!home.getBlockState(pos).isAir() || !home.getBlockState(pos.above()).isAir()
                        || !home.getBlockState(pos.below()).isSolid()) continue;
                if (home != level) {
                    tag.putBoolean("SuccessionHomeChecked", true);
                    npc.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(home,
                            new net.minecraft.world.phys.Vec3(x + .5, y, z + .5), net.minecraft.world.phys.Vec3.ZERO,
                            npc.getYRot(), npc.getXRot(), net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
                    return;
                }
                npc.teleportTo(x + .5, y, z + .5); tag.putBoolean("SuccessionHomeChecked", true); return;
            }
        } catch (IllegalArgumentException ignored) { tag.putBoolean("SuccessionHomeChecked", true); }
    }
}
