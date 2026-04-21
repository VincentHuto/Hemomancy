package com.vincenthuto.hemomancy.common.capability;

import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
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
}
