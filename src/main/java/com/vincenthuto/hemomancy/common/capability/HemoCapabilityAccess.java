package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
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
return HemoCapabilityAccess.getBloodTendency(player);
}

/** Access blood tendency from any entity. */
public static Optional<IBloodTendency> getBloodTendency(Entity entity) {
return HemoCapabilityAccess.getBloodTendency(entity);
}

/** Access blood tendency from a block entity (e.g. SomaticLoom). */
public static Optional<IBloodTendency> getBloodTendency(BlockEntity be) {
return HemoCapabilityAccess.getBloodTendency(be);
}

public static IBloodTendency requireBloodTendency(Player player) {
return getBloodTendency(player).orElseThrow(IllegalStateException::new);
}

public static IBloodTendency requireBloodTendency(Entity entity) {
return getBloodTendency(entity).orElseThrow(IllegalStateException::new);
}
}
