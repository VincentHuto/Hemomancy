package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Functional interface that describes what a {@link BloodManipulation} does when
 * executed on behalf of a Drudge construct or Will-like mob rather than a player.
 *
 * <p>Each manipulation that can be equipped by a Drudge registers its own
 * {@code DrudgeAction} via
 * {@link BloodManipulation#setDrudgeAction(DrudgeAction, String)}.
 * Manipulations that are inherently player-only (inventory conjurations, nested
 * summons, etc.) should register {@link #DRUDGE_UNSUPPORTED} so the equip
 * interaction is blocked at the source.
 *
 * <p>Blood-charge draining and cooldown application are handled by the Drudge's
 * goal — the action lambda is only responsible for firing the actual effect and
 * returning {@code true} if the effect succeeded (and {@code false} if there was
 * nothing to do, e.g. no valid target).
 */
@FunctionalInterface
public interface DrudgeAction {

    /**
     * Execute the manipulation on behalf of the given mob caster.
     *
     * @param caster  the executing PathfinderMob caster
     * @param world   the server-side level (never client-side; callers guarantee this)
     * @param centre  the caster's current block position used as the work origin
     * @param radius  the configured work radius ({@code HemoServerConfig.DRUDGE_WORK_RADIUS})
     * @return {@code true} if the effect fired and blood should be drained / cooldown applied;
     *         {@code false} if there was nothing to act on this tick
     */
    boolean execute(PathfinderMob caster, Level world, BlockPos centre, int radius);

    /**
     * Sentinel value indicating that the manipulation cannot be equipped by a Drudge.
     * Checking identity ({@code action == DrudgeAction.DRUDGE_UNSUPPORTED}) is the
     * canonical way to detect this state.
     */
    DrudgeAction DRUDGE_UNSUPPORTED = (d, w, c, r) -> false;
}
