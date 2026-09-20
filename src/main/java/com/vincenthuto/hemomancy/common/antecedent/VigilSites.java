package com.vincenthuto.hemomancy.common.antecedent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Loaded controllers only; no ticking chunks or searching the world to find a site. */
public final class VigilSites {
    public static final String FIXTURE_MARKER="hemomancy:antecedent_fixture";
    private static final Map<Level,Set<VigilArchiveBlockEntity>> LOADED=new WeakHashMap<>();
    private VigilSites() {}
    public static synchronized void add(VigilArchiveBlockEntity site) { LOADED.computeIfAbsent(site.getLevel(),ignored->Collections.newSetFromMap(new IdentityHashMap<>())).add(site); }
    public static synchronized void remove(VigilArchiveBlockEntity site) { var set=LOADED.get(site.getLevel());if(set!=null)set.remove(site); }
    public static synchronized List<VigilArchiveBlockEntity> loaded(Level level) { return List.copyOf(LOADED.getOrDefault(level,Set.of())); }
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
