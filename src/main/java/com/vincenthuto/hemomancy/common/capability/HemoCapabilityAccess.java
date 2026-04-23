package com.vincenthuto.hemomancy.common.capability;

import java.util.Map;
import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.white_humor.IWhiteHumorVolume;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import com.vincenthuto.hemomancy.common.tile.IWhiteHumorTile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HemoCapabilityAccess {

private HemoCapabilityAccess() {}

// ── Blood Volume ──────────────────────────────────────────────────────────

public static Optional<IBloodVolume> getBloodVolume(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.BLOOD_VOLUME));
}

public static Optional<IBloodVolume> getBloodVolume(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.BLOOD_VOLUME));
}

public static Optional<IBloodVolume> getBloodVolume(BlockEntity be) {
    if (be instanceof IBloodTile) {
        return Optional.of(be.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME));
    }
    return Optional.empty();
}

public static Optional<IBloodVolume> getBloodVolume(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(HemoCapabilityKeys.ITEM_BLOOD_VOLUME));
}

public static IBloodVolume requireBloodVolume(Player player) {
    return getBloodVolume(player).orElseThrow(IllegalStateException::new);
}

public static IBloodVolume requireBloodVolume(Entity entity) {
    return getBloodVolume(entity).orElseThrow(IllegalStateException::new);
}

// ── Blood Tendency ────────────────────────────────────────────────────────

public static Optional<IBloodTendency> getBloodTendency(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.BLOOD_TENDENCY));
}

public static Optional<IBloodTendency> getBloodTendency(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.BLOOD_TENDENCY));
}

public static Optional<IBloodTendency> getBloodTendency(BlockEntity be) {
    return Optional.of(be.getData(HemoAttachmentTypes.BLOCK_BLOOD_TENDENCY));
}

public static IBloodTendency requireBloodTendency(Player player) {
    return getBloodTendency(player).orElseThrow(IllegalStateException::new);
}

public static IBloodTendency requireBloodTendency(Entity entity) {
    return getBloodTendency(entity).orElseThrow(IllegalStateException::new);
}

// ── Vascular System ───────────────────────────────────────────────────────

public static Optional<IVascularSystem> getVascularSystem(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.VASCULAR_SYSTEM));
}

public static Optional<IVascularSystem> getVascularSystem(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.VASCULAR_SYSTEM));
}

public static IVascularSystem requireVascularSystem(Player player) {
    return getVascularSystem(player).orElseThrow(IllegalStateException::new);
}

public static IVascularSystem requireVascularSystem(Entity entity) {
    return getVascularSystem(entity).orElseThrow(IllegalStateException::new);
}

/** Convenience helper replacing VascularSystemProvider.getPlayerVascularSystem. */
public static Map<EnumVeinSections, Float> getPlayerVascularSystem(Player player) {
    return requireVascularSystem(player).getVascularSystem();
}

// ── Known Manipulations ───────────────────────────────────────────────────

public static Optional<IKnownManipulations> getKnownManipulations(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.KNOWN_MANIPULATIONS));
}

public static Optional<IKnownManipulations> getKnownManipulations(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.KNOWN_MANIPULATIONS));
}

public static IKnownManipulations requireKnownManipulations(Player player) {
    return getKnownManipulations(player).orElseThrow(IllegalStateException::new);
}

public static IKnownManipulations requireKnownManipulations(Entity entity) {
    return getKnownManipulations(entity).orElseThrow(IllegalStateException::new);
}

// ── Initiatory Degree ─────────────────────────────────────────────────────

public static Optional<IInitiatoryDegree> getInitiatoryDegree(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.INITIATORY_DEGREE));
}

public static Optional<IInitiatoryDegree> getInitiatoryDegree(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.INITIATORY_DEGREE));
}

public static IInitiatoryDegree requireInitiatoryDegree(Player player) {
    return getInitiatoryDegree(player).orElseThrow(IllegalStateException::new);
}

public static IInitiatoryDegree requireInitiatoryDegree(Entity entity) {
    return getInitiatoryDegree(entity).orElseThrow(IllegalStateException::new);
}

/** Convenience helper replacing InitiatoryDegreeProvider.getPlayerDegreeNumber. */
public static int getPlayerDegreeNumber(Player player) {
    return getInitiatoryDegree(player).map(IInitiatoryDegree::getDegreeNumber).orElse(0);
}

// ── Equipped Morphling ────────────────────────────────────────────────────

public static Optional<IEquippedMorphling> getEquippedMorphling(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.EQUIPPED_MORPHLING));
}

public static Optional<IEquippedMorphling> getEquippedMorphling(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.EQUIPPED_MORPHLING));
}

public static IEquippedMorphling requireEquippedMorphling(Player player) {
    return getEquippedMorphling(player).orElseThrow(IllegalStateException::new);
}

public static IEquippedMorphling requireEquippedMorphling(Entity entity) {
    return getEquippedMorphling(entity).orElseThrow(IllegalStateException::new);
}

// ── Unstained Progress ────────────────────────────────────────────────────

public static Optional<IUnstainedProgress> getUnstainedProgress(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.UNSTAINED_PROGRESS));
}

public static Optional<IUnstainedProgress> getUnstainedProgress(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.UNSTAINED_PROGRESS));
}

public static IUnstainedProgress requireUnstainedProgress(Player player) {
    return getUnstainedProgress(player).orElseThrow(IllegalStateException::new);
}

public static IUnstainedProgress requireUnstainedProgress(Entity entity) {
    return getUnstainedProgress(entity).orElseThrow(IllegalStateException::new);
}

// ── White Humor Volume ────────────────────────────────────────────────────

public static Optional<IWhiteHumorVolume> getWhiteHumorVolume(BlockEntity be) {
    if (be instanceof IWhiteHumorTile) {
        return Optional.of(be.getData(HemoAttachmentTypes.BLOCK_WHITE_HUMOR_VOLUME));
    }
    return Optional.empty();
}

public static Optional<IWhiteHumorVolume> getWhiteHumorVolume(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.WHITE_HUMOR_VOLUME));
}

public static Optional<IWhiteHumorVolume> getWhiteHumorVolume(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.WHITE_HUMOR_VOLUME));
}

public static IWhiteHumorVolume requireWhiteHumorVolume(BlockEntity be) {
    return getWhiteHumorVolume(be).orElseThrow(IllegalStateException::new);
}

public static IWhiteHumorVolume requireWhiteHumorVolume(Player player) {
    return getWhiteHumorVolume(player).orElseThrow(IllegalStateException::new);
}

// ── Visceral Organs ───────────────────────────────────────────────────────

public static Optional<IVisceralOrgans> getVisceralOrgans(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.VISCERAL_ORGANS));
}

public static Optional<IVisceralOrgans> getVisceralOrgans(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.VISCERAL_ORGANS));
}

public static IVisceralOrgans requireVisceralOrgans(Player player) {
    return getVisceralOrgans(player).orElseThrow(IllegalStateException::new);
}

// ── Scars ─────────────────────────────────────────────────────────────────

public static Optional<IScarsItemHandler> getScars(Player player) {
    return Optional.ofNullable(player.getCapability(HemoCapabilityKeys.SCARS));
}

public static IScarsItemHandler requireScars(Player player) {
    return getScars(player).orElseThrow(IllegalStateException::new);
}

public static Optional<IScar> getScar(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(HemoCapabilityKeys.ITEM_SCAR));
}
}
