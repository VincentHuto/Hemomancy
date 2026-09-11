package com.vincenthuto.hemomancy.common.mission.hermit;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;

/** Personal initiation access after another practitioner has spent this temple's display. */
public final class SpentTempleInitiation {
    private static final ResourceKey<Structure> BLOOD_TEMPLE = ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc("blood_temple"));

    private SpentTempleInitiation() {}

    public static boolean acceptBlessing(ServerPlayer player, HarbingerHermitEntity hermit) {
        if (player.serverLevel() != hermit.level() || player.distanceToSqr(hermit) > 64 || hermit.isFarewellDying()
                || HemoCapabilityAccess.getPlayerDegreeNumber(player) != 0
                || HemoCapabilityAccess.getBloodVolume(player).map(v -> v.isActive()).orElse(true)) return false;
        if (TempleOathRules.claimedHeartHermit(player) != null) return TempleOathRules.hasClaimedHeartFrom(player, hermit.getUUID());
        CardinalFocusBlockEntity focus = findSpentFocus(player.serverLevel(), hermit);
        if (focus == null) return false;
        var equipment = HemoCapabilityAccess.getEquipment(player).orElse(null);
        if (equipment == null) return false;
        TempleOathRules.recordHeartClaim(player, hermit.getUUID());
        // A pre-initiation death may have retained the original dormant charm.
        boolean hasCharm = player.getInventory().countItem(ItemInit.charm_of_vascularium.get()) > 0;
        for (int i = 0; i < equipment.getSlots(); i++) hasCharm |= equipment.getStackInSlot(i).is(ItemInit.charm_of_vascularium.get());
        if (!hasCharm) {
            ItemStack charm = new ItemStack(ItemInit.charm_of_vascularium.get());
            if (equipment.getStackInSlot(5).isEmpty()) equipment.setStackInSlot(5, charm);
            else if (!player.addItem(charm)) player.drop(charm, false);
        }
        return true;
    }

    private static CardinalFocusBlockEntity findSpentFocus(ServerLevel level, HarbingerHermitEntity hermit) {
        CardinalFocusBlockEntity found = null;
        for (BlockPos pos : BlockPos.betweenClosed(hermit.blockPosition().offset(-12, -6, -12), hermit.blockPosition().offset(12, 6, 12))) {
            if (!(level.getBlockEntity(pos) instanceof CardinalFocusBlockEntity focus) || focus.getTempleDisplay() == null) continue;
            var display = level.getBlockEntity(focus.getTempleDisplay());
            if (display instanceof MortalDisplayBlockEntity livingDisplay) {
                if (!livingDisplay.isClaimed() || !hermit.getUUID().equals(livingDisplay.getLinkedHermit())) continue;
                focus.linkTempleHermit(hermit.getUUID());
            } else {
                if (!level.getBlockState(focus.getTempleDisplay()).is(BlockInit.placed_blood_stained_stone.get())) continue;
                if (focus.getTempleHermit() == null && !recoverLegacyLink(level, focus, hermit)) continue;
                if (!hermit.getUUID().equals(focus.getTempleHermit())) continue;
            }
            if (found != null) return null; // Ambiguous temple links must not authorize an unrelated focus.
            found = focus;
        }
        return found;
    }

    private static boolean recoverLegacyLink(ServerLevel level, CardinalFocusBlockEntity focus, HarbingerHermitEntity hermit) {
        var start = level.structureManager().getStructureWithPieceAt(focus.getBlockPos(), holder -> holder.is(BLOOD_TEMPLE));
        if (!start.isValid() || !start.getBoundingBox().isInside(hermit.blockPosition())
                || !start.getBoundingBox().isInside(focus.getTempleDisplay())) return false;
        var hermits = level.getEntitiesOfClass(HarbingerHermitEntity.class, AABB.of(start.getBoundingBox()));
        if (hermits.size() != 1 || hermits.getFirst() != hermit) return false;
        focus.linkTempleHermit(hermit.getUUID());
        return true;
    }
}
