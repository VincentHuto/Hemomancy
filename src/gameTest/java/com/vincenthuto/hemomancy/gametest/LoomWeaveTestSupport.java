package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** Drives a paid Somatic Loom weave to completion deterministically: ticks the loom, spends and hooks each thread. */
public final class LoomWeaveTestSupport {
    private LoomWeaveTestSupport() {}

    public static boolean weaveAll(ServerLevel level, BlockPos pos, SomaticLoomBlockEntity loom, Player weaver) {
        int budget = SomaticWeaveRules.LOOP_TICKS * (loom.getWeaveThreads().size() + 4) + SomaticWeaveRules.CLIMAX_TICKS + 20;
        for (int tick = 0; tick < budget && loom.isWeaving(); tick++) {
            SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), loom);
            for (FrayedThreadEntity thread : loom.looseThreadEntities(level)) {
                thread.addStrain(1f);
                if (!thread.tryHook(weaver)) return false;
            }
        }
        return !loom.isWeaving();
    }
}
