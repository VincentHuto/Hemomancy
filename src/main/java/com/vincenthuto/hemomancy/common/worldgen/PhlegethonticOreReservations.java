package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.BitSet;

/** Reserve attempted low-Nether ore cells even when another vanilla feature wins their replacement race. */
public final class PhlegethonticOreReservations implements INBTSerializable<CompoundTag> {
    private static final ThreadLocal<WorldGenLevel> DECORATING=new ThreadLocal<>();
    private BitSet cells=new BitSet(4096);
    public static void begin(WorldGenLevel level){if(level.getLevel().dimension().equals(Level.NETHER))DECORATING.set(level);}
    public static void end(){DECORATING.remove();}
    public static void reserve(BlockPos pos,BlockState result) {
        WorldGenLevel level=DECORATING.get();
        if(level==null || result.is(PhlegethonticTags.VEIN_REPLACEABLE))return;
        int y=pos.getY()-level.getMinBuildHeight()-5;
        if(y<0 || y>=16)return;
        var chunk=level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        synchronized(chunk) {
            chunk.getData(HemoAttachmentTypes.PHLEGETHONTIC_ORE_RESERVATIONS).cells.set(index(pos,y));
        }
    }
    public static boolean reserved(WorldGenLevel level,BlockPos pos) {
        int y=pos.getY()-level.getMinBuildHeight()-5;
        if(y<0 || y>=16)return false;
        var chunk=level.getChunk(pos.getX()>>4,pos.getZ()>>4);
        synchronized(chunk) {
            return chunk.hasData(HemoAttachmentTypes.PHLEGETHONTIC_ORE_RESERVATIONS)
                    && chunk.getData(HemoAttachmentTypes.PHLEGETHONTIC_ORE_RESERVATIONS).cells.get(index(pos,y));
        }
    }
    private static int index(BlockPos pos,int y){return (y<<8)|((pos.getZ()&15)<<4)|(pos.getX()&15);}
    @Override public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag=new CompoundTag();tag.putLongArray("Cells",cells.toLongArray());return tag;
    }
    @Override public void deserializeNBT(HolderLookup.Provider provider,CompoundTag tag) {
        cells=BitSet.valueOf(tag.getLongArray("Cells"));
    }
}
