package com.vincenthuto.hemomancy.common.capability;

import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingProvider;
import com.vincenthuto.hemomancy.common.capability.player.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.vascular.VascularSystemProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HemoCapabilityAccess {

private HemoCapabilityAccess() {
}

// ── Blood Volume ──────────────────────────────────────────────────────────

/** Access blood volume from a player. */
public static Optional<IBloodVolume> getBloodVolume(Player player) {
return player.getCapability(BloodVolumeProvider.VOLUME_CAPA);
}

/** Access blood volume from any entity (players, summons, etc.). */
public static Optional<IBloodVolume> getBloodVolume(Entity entity) {
return entity.getCapability(BloodVolumeProvider.VOLUME_CAPA);
}

/** Access blood volume from a block entity (IBloodTile implementations). */
public static Optional<IBloodVolume> getBloodVolume(BlockEntity be) {
return be.getCapability(BloodVolumeProvider.VOLUME_CAPA);
}

/** Access blood volume from an item stack (BloodGourdItem and similar). */
public static Optional<IBloodVolume> getBloodVolume(ItemStack stack) {
return stack.getCapability(BloodVolumeProvider.VOLUME_CAPA);
}

public static IBloodVolume requireBloodVolume(Player player) {
return getBloodVolume(player).orElseThrow(IllegalStateException::new);
}

public static IBloodVolume requireBloodVolume(Entity entity) {
return getBloodVolume(entity).orElseThrow(IllegalStateException::new);
}

// ── Blood Tendency ────────────────────────────────────────────────────────

/** Access blood tendency from a player. */
public static Optional<IBloodTendency> getBloodTendency(Player player) {
return player.getCapability(BloodTendencyProvider.TENDENCY_CAPA);
}

/** Access blood tendency from any entity. */
public static Optional<IBloodTendency> getBloodTendency(Entity entity) {
return entity.getCapability(BloodTendencyProvider.TENDENCY_CAPA);
}

/** Access blood tendency from a block entity (e.g. SomaticLoom). */
public static Optional<IBloodTendency> getBloodTendency(BlockEntity be) {
return be.getCapability(BloodTendencyProvider.TENDENCY_CAPA);
}

public static IBloodTendency requireBloodTendency(Player player) {
return getBloodTendency(player).orElseThrow(IllegalStateException::new);
}

public static IBloodTendency requireBloodTendency(Entity entity) {
return getBloodTendency(entity).orElseThrow(IllegalStateException::new);
}

// ── Vascular System ───────────────────────────────────────────────────────

/** Access vascular system from a player. */
public static Optional<IVascularSystem> getVascularSystem(Player player) {
return player.getCapability(VascularSystemProvider.VASCULAR_CAPA);
}

/** Access vascular system from any entity. */
public static Optional<IVascularSystem> getVascularSystem(Entity entity) {
return entity.getCapability(VascularSystemProvider.VASCULAR_CAPA);
}

public static IVascularSystem requireVascularSystem(Player player) {
return getVascularSystem(player).orElseThrow(IllegalStateException::new);
}

public static IVascularSystem requireVascularSystem(Entity entity) {
return getVascularSystem(entity).orElseThrow(IllegalStateException::new);
}

// ── Known Manipulations ───────────────────────────────────────────────────

/** Access known manipulations from a player. */
public static Optional<IKnownManipulations> getKnownManipulations(Player player) {
return player.getCapability(KnownManipulationProvider.MANIP_CAPA);
}

/** Access known manipulations from any entity. */
public static Optional<IKnownManipulations> getKnownManipulations(Entity entity) {
return entity.getCapability(KnownManipulationProvider.MANIP_CAPA);
}

public static IKnownManipulations requireKnownManipulations(Player player) {
return getKnownManipulations(player).orElseThrow(IllegalStateException::new);
}

public static IKnownManipulations requireKnownManipulations(Entity entity) {
return getKnownManipulations(entity).orElseThrow(IllegalStateException::new);
}

// ── Initiatory Degree ─────────────────────────────────────────────────────

/** Access initiatory degree from a player. */
public static Optional<IInitiatoryDegree> getInitiatoryDegree(Player player) {
return player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA);
}

/** Access initiatory degree from any entity. */
public static Optional<IInitiatoryDegree> getInitiatoryDegree(Entity entity) {
return entity.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA);
}

public static IInitiatoryDegree requireInitiatoryDegree(Player player) {
return getInitiatoryDegree(player).orElseThrow(IllegalStateException::new);
}

public static IInitiatoryDegree requireInitiatoryDegree(Entity entity) {
return getInitiatoryDegree(entity).orElseThrow(IllegalStateException::new);
}

// ── Equipped Morphling ────────────────────────────────────────────────────

/** Access equipped morphling from a player. */
public static Optional<IEquippedMorphling> getEquippedMorphling(Player player) {
return player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA);
}

/** Access equipped morphling from any entity. */
public static Optional<IEquippedMorphling> getEquippedMorphling(Entity entity) {
return entity.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA);
}

public static IEquippedMorphling requireEquippedMorphling(Player player) {
return getEquippedMorphling(player).orElseThrow(IllegalStateException::new);
}

public static IEquippedMorphling requireEquippedMorphling(Entity entity) {
return getEquippedMorphling(entity).orElseThrow(IllegalStateException::new);
}
}
