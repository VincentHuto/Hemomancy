package com.vincenthuto.hemomancy.common.manipulation.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The hook for cast outpourings that change the world (fire where Flammeus blood lands, frost under Congeatio, Mortem
 * corrosion). Outpourings are visual today: the config gate defaults to never and every effect is a no-op. Contacts are
 * sampled here on the server; the client's simulated blood never decides gameplay.
 */
public final class OutpouringWorldEffects {
    public interface Effect {
        void apply(ServerLevel level, ServerPlayer caster, EnumVeinSections section, EnumManipulationRank rank, List<BlockPos> contacts);
    }

    public static final Effect NONE = (level, caster, section, rank, contacts) -> {};
    /** Reach and mark count per rank, mirroring the client outpouring profile without depending on client code. */
    private static final double[] REACH = {.6, 1, 2, 3.5, 5};
    private static final int[] MAX_CONTACTS = {2, 6, 14, 26, 40};

    private OutpouringWorldEffects() {}

    public static boolean allowed(EnumManipulationRank rank, int minRank) {
        return minRank >= 0 && rank != null && rank.ordinal() >= minRank;
    }

    public static Effect effect(EnumBloodTendency school) { return NONE; }

    public static void release(ServerPlayer caster, BloodManipulation manipulation) {
        if (!allowed(manipulation.getRank(), com.vincenthuto.hemomancy.config.HemoConfigValues.get(
                HemoServerConfig.CAST_OUTPOURING_REAL_EFFECT_MIN_RANK, -1))) return;
        var effect = effect(manipulation.getTend());
        if (effect == NONE) return;
        int rank = manipulation.getRank().ordinal();
        effect.apply(caster.serverLevel(), caster, manipulation.getSection(), manipulation.getRank(),
                contacts(caster, REACH[rank], MAX_CONTACTS[rank]));
    }

    /** Solid blocks with open air above them near the caster's feet, shuffled and capped. */
    static List<BlockPos> contacts(ServerPlayer caster, double reach, int max) {
        var level = caster.serverLevel();
        BlockPos feet = caster.blockPosition();
        int r = (int) Math.ceil(reach);
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-r, -2, -r), feet.offset(r, 1, r))) {
            if (pos.distSqr(feet) > reach * reach + 2) continue;
            if (level.getBlockState(pos).blocksMotion() && !level.getBlockState(pos.above()).blocksMotion()) found.add(pos.immutable());
        }
        Collections.shuffle(found, new java.util.Random(caster.getRandom().nextLong()));
        return found.subList(0, Math.min(max, found.size()));
    }
}
