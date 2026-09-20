package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import java.util.*;

public final class SuccessionWorkplaces {
    private record Cached(long time, List<BlockPos> positions) {}
    private static final Map<ServerLevel, Map<String, Cached>> CACHE = new WeakHashMap<>();
    private SuccessionWorkplaces() {}
    public static boolean valid(ServerLevel level, UUID owner, String profession, BlockPos pos) {
        var fane = FoundingFaneSavedData.get(level);
        return fane.hasFane(owner) && fane.isWithinFane(owner, pos) && level.hasChunkAt(pos)
                && SuccessionProfessions.accepts(profession, level.getBlockState(pos));
    }
    public static BlockPos find(ServerLevel level, UUID owner, String profession, BlockPos near) {
        var data = SuccessionSavedData.get(level);
        var map = CACHE.computeIfAbsent(level, ignored -> new HashMap<>());
        String key = owner + "/" + profession;
        var cached = map.get(key);
        if (cached == null || level.getGameTime() - cached.time >= 100) {
            cached = new Cached(level.getGameTime(), scan(level, owner, profession)); map.put(key, cached);
        }
        return cached.positions.stream().filter(pos -> valid(level, owner, profession, pos))
                .filter(pos -> data.ledger.available(new SuccessionLedger.Workplace(level.dimension().location().toString(), pos.asLong())))
                .min(Comparator.comparingDouble(near::distSqr)).orElse(null);
    }
    private static List<BlockPos> scan(ServerLevel level, UUID owner, String profession) {
        var faneData = FoundingFaneSavedData.get(level);
        var fane = faneData.getAllFootprints().get(owner);
        if (fane == null || !faneData.hasFane(owner)) return List.of();
        var centers = new ArrayList<>(fane.stakes()); centers.add(fane.heart());
        var chunks = new HashSet<Long>();
        for (var center : centers) for (int x = (center.getX() - 40) >> 4; x <= (center.getX() + 40) >> 4; x++)
            for (int z = (center.getZ() - 40) >> 4; z <= (center.getZ() + 40) >> 4; z++) chunks.add(ChunkPos.asLong(x, z));
        var result = new ArrayList<BlockPos>();
        for (long packed : chunks) {
            var cp = new ChunkPos(packed);
            var chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
            if (chunk == null) continue;
            var sections = chunk.getSections();
            for (int s = 0; s < sections.length; s++) {
                var section = sections[s];
                if (section.hasOnlyAir() || !section.getStates().maybeHas(state -> SuccessionProfessions.accepts(profession, state))) continue;
                for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                    if (!SuccessionProfessions.accepts(profession, section.getBlockState(x, y, z))) continue;
                    var pos = new BlockPos(cp.getMinBlockX() + x, ((level.getMinSection() + s) << 4) + y, cp.getMinBlockZ() + z);
                    if (fane.contains(pos)) result.add(pos);
                }
            }
        }
        return List.copyOf(result);
    }
}
