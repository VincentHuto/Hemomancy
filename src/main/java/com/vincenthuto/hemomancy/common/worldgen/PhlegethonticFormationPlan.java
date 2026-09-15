package com.vincenthuto.hemomancy.common.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.Material;
import static com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.Voxel;

/** Reconstructs sparse pointed formations from the completed cavern plan. */
public final class PhlegethonticFormationPlan {
    public record Segment(Voxel pos,Direction direction,DripstoneThickness thickness) {}

    private PhlegethonticFormationPlan() {}

    public static List<Segment> plan(long seed,int chunkX,int chunkZ,Map<Voxel,Material> terrain) {
        return plan(seed,chunkX,chunkZ,terrain,p -> terrain.get(p)==Material.AIR,
                (support,direction) -> solid(terrain.get(support)));
    }

    public static List<Segment> plan(long seed,int chunkX,int chunkZ,Map<Voxel,Material> terrain,
                                     Predicate<Voxel> empty,BiPredicate<Voxel,Direction> sturdySupport) {
        List<Segment> result=new ArrayList<>();
        int minX=chunkX*16,minZ=chunkZ*16;
        for(int x=minX;x<minX+16;x++)for(int z=minZ;z<minZ+16;z++) {
            int floorAir=Integer.MAX_VALUE,roofAir=Integer.MIN_VALUE;
            for(var entry:terrain.entrySet()) {
                Voxel pos=entry.getKey();
                if(pos.x()==x && pos.z()==z && entry.getValue()==Material.AIR) {
                    floorAir=Math.min(floorAir,pos.y());
                    roofAir=Math.max(roofAir,pos.y());
                }
            }
            if(roofAir-floorAir<7)continue;
            int floorLength=length(seed,x,z,0x5550L,roofAir-floorAir);
            if(selected(seed,x,z,0x464c4f4f52L,18)
                    && supportedChain(x,z,floorAir,Direction.UP,floorLength,empty,sturdySupport))
                append(result,x,z,floorAir,Direction.UP,floorLength);
            int roofLength=length(seed,x,z,0x444f574eL,roofAir-floorAir);
            if(selected(seed,x,z,0x524f4f46L,14)
                    && supportedChain(x,z,roofAir,Direction.DOWN,roofLength,empty,sturdySupport))
                append(result,x,z,roofAir,Direction.DOWN,roofLength);
        }
        result.sort(Comparator.comparingInt((Segment s)->s.pos().x())
                .thenComparingInt(s->s.pos().z()).thenComparingInt(s->s.pos().y()));
        return List.copyOf(result);
    }

    private static boolean supportedChain(int x,int z,int start,Direction direction,int length,
                                          Predicate<Voxel> empty,BiPredicate<Voxel,Direction> sturdySupport) {
        Voxel support=new Voxel(x,start+(direction==Direction.UP?-1:1),z);
        if(!sturdySupport.test(support,direction))return false;
        for(int i=0;i<length;i++) {
            int y=start+(direction==Direction.UP?i:-i);
            if(!empty.test(new Voxel(x,y,z)))return false;
        }
        return true;
    }

    private static boolean solid(Material material) {
        return material!=null && material!=Material.AIR && material!=Material.ICHOR;
    }

    private static boolean selected(long seed,int x,int z,long salt,int chance) {
        return Math.floorMod(PhlegethonticRules.seed(seed,x,z,salt),chance)==0;
    }

    private static int length(long seed,int x,int z,long salt,int clearance) {
        int cap=Math.min(5,Math.max(1,(clearance-3)/2));
        return 1+(int)Math.floorMod(PhlegethonticRules.seed(seed,x,z,salt),cap);
    }

    private static void append(List<Segment> result,int x,int z,int start,Direction direction,int length) {
        for(int i=0;i<length;i++) {
            int y=start+(direction==Direction.UP?i:-i);
            result.add(new Segment(new Voxel(x,y,z),direction,thickness(i,length)));
        }
    }

    private static DripstoneThickness thickness(int index,int length) {
        if(length==1 || index==length-1)return DripstoneThickness.TIP;
        if(index==length-2)return DripstoneThickness.FRUSTUM;
        if(index==0)return DripstoneThickness.BASE;
        return DripstoneThickness.MIDDLE;
    }
}
