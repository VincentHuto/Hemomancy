package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/**
 * Loaded controllers only; no ticking chunks or searching the world to find a site. Split by side like
 * {@code WardAnchors}: a client keeps one level, and both maps are cleared with the session (RT-12).
 */
public final class VigilSites {
    public static final String FIXTURE_MARKER="hemomancy:antecedent_fixture";
    private static final Map<Level,Set<VigilArchiveBlockEntity>> SERVER=new WeakHashMap<>();
    private static final Map<Level,Set<VigilArchiveBlockEntity>> CLIENT=new WeakHashMap<>();
    private VigilSites() {}
    private static Map<Level,Set<VigilArchiveBlockEntity>> side(Level level) { return level.isClientSide?CLIENT:SERVER; }
    public static synchronized void add(VigilArchiveBlockEntity site) {
        Level level=site.getLevel();
        if(level==null)return;
        // The level a client left on a dimension change never unloads its chunks, so drop it here.
        if(level.isClientSide) CLIENT.keySet().removeIf(held->held!=level);
        side(level).computeIfAbsent(level,ignored->Collections.newSetFromMap(new IdentityHashMap<>())).add(site);
    }
    public static synchronized void remove(VigilArchiveBlockEntity site) {
        Level level=site.getLevel();
        if(level==null)return;
        var set=side(level).get(level);
        if(set!=null){set.remove(site);if(set.isEmpty())side(level).remove(level);}
    }
    public static synchronized List<VigilArchiveBlockEntity> loaded(Level level) { return List.copyOf(side(level).getOrDefault(level,Set.of())); }
    /** On logout: the client's levels are gone. */
    public static synchronized void clearClient() { CLIENT.clear(); }
    /** When a server starts or stops. */
    public static synchronized void clearServer() { SERVER.clear(); }
    public static VigilArchiveBlockEntity fixture(Level level,BlockPos pos) {
        if(!authored(level,pos))return null;
        for(var site:loaded(level)) if(!site.isRemoved() && site.isFixture(pos)) return site;
        return null;
    }
    public static boolean authored(Level level,BlockPos pos) {
        if(!level.hasChunkAt(pos))return false;
        var fixture=level.getBlockEntity(pos);
        return fixture!=null && fixture.getPersistentData().getBoolean(FIXTURE_MARKER);
    }
    public static void changing(Level level,BlockPos pos,BlockState before,BlockState after) {
        if(before.getBlock()==after.getBlock() || !(before.is(net.minecraft.world.level.block.Blocks.SCULK_SENSOR) || before.is(net.minecraft.world.level.block.Blocks.SCULK_CATALYST) || before.is(net.minecraft.world.level.block.Blocks.SCULK_SHRIEKER))) return;
        var site=fixture(level,pos);
        if(site!=null) site.retire(pos);
    }
}
