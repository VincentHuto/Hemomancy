package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramSigilGeometry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Drives a paid Somatic Loom weave to completion deterministically: threads each true fragment along the engram's
 * reference path through the loom's own staff steps, then ticks the climax through.
 */
public final class LoomWeaveTestSupport {
    private LoomWeaveTestSupport() {}

    public static boolean weaveAll(ServerLevel level, BlockPos pos, SomaticLoomBlockEntity loom, Player weaver) {
        // A weave carried over from an older save recalls its engram on the loom's first ticks.
        for (int tick = 0; tick < 5 && loom.isWeaving() && loom.getEngram() == null; tick++)
            SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), loom);
        EngramBoard board = loom.getEngram();
        if (board == null) return false;
        for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
            if (board.fragment(fragment).intrusion()) continue;
            if (!thread(loom, weaver, board.solution(fragment))) return false;
        }
        loom.releaseEngram(weaver);
        for (int tick = 0; tick < SomaticWeaveRules.CLIMAX_TICKS + 20 && loom.isWeaving(); tick++)
            SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), loom);
        return !loom.isWeaving();
    }

    /** Presses the path's fragment, then holds the staff over each next node. */
    public static boolean thread(SomaticLoomBlockEntity loom, Player weaver, int[] path) {
        if (path == null) return false;
        loom.engramStep(weaver, path[0], false, true);
        for (int k = 1; k < path.length; k++)
            if (!loom.engramStep(weaver, path[k], false, false).moved()) return false;
        return true;
    }

    /**
     * A legal path for `fragment` from its lane's head home on the board as it stands now (into the Vessel, or plied
     * into a lane already home), by plain breadth-first search through the engram's own rules; null if none.
     */
    public static int[] homeward(EngramBoard live, int fragment, int[] stock) {
        int[] start = live.lane(fragment);
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(start);
        boolean[] seen = new boolean[EngramSigilGeometry.NODES];
        seen[start[start.length - 1]] = true;
        while (!queue.isEmpty()) {
            int[] path = queue.poll();
            int head = path[path.length - 1];
            if (head == EngramSigilGeometry.VESSEL) return Arrays.copyOfRange(path, start.length - 1, path.length);
            for (int next : EngramSigilGeometry.neighbours(head)) {
                if (seen[next]) continue;
                EngramBoard trial = EngramBoard.load(live.save());
                trial.setLane(fragment, path);
                if (EngramWeaveRules.canExtend(trial, fragment, next, stock) != EngramWeaveRules.Step.OK) continue;
                seen[next] = true;
                int[] longer = Arrays.copyOf(path, path.length + 1);
                longer[path.length] = next;
                // Home is the Vessel, or a lane already home that this one plies into.
                if (EngramWeaveRules.connected(withStep(trial, fragment, path, next), fragment))
                    return Arrays.copyOfRange(longer, start.length - 1, longer.length);
                queue.add(longer);
            }
        }
        return null;
    }

    private static EngramBoard withStep(EngramBoard board, int fragment, int[] path, int next) {
        int[] longer = Arrays.copyOf(path, path.length + 1);
        longer[path.length] = next;
        board.setLane(fragment, longer);
        return board;
    }

    /** Indices of the board's intrusions. */
    public static List<Integer> intrusions(EngramBoard board) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < board.fragmentCount(); i++) if (board.fragment(i).intrusion()) out.add(i);
        return out;
    }
}
