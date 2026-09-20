package com.vincenthuto.hemomancy.common.antecedent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import java.util.List;

/** Versioned authored coordinates; old generated sites retain their original layout. */
public final class VigilLayout {
    public static final BlockPos VESSEL=new BlockPos(14,1,57);
    public static final BlockPos[] SENSORS={new BlockPos(11,1,41),new BlockPos(15,1,42),new BlockPos(13,1,45)};
    public static final BlockPos CATALYST=new BlockPos(16,1,44),SHRIEKER=new BlockPos(10,1,45),RECORD=new BlockPos(21,1,48);
    public static final Plan LEGACY = new Plan(1,VESSEL,List.of(SENSORS),CATALYST,SHRIEKER,RECORD,
            new BlockPos(8,16,4),new BoundingBox(2,0,33,23,18,51),new BlockPos(24,21,62),10,
            new BlockPos(9,0,40),9,6,new BlockPos(12,15,40),new BlockPos(3,15,24),new BlockPos(11,14,21),null);
    public static final Plan COMPACT = new Plan(2,new BlockPos(4,1,25),
            List.of(new BlockPos(14,1,16),new BlockPos(18,1,17),new BlockPos(16,1,20)),
            new BlockPos(18,1,21),new BlockPos(13,1,20),new BlockPos(20,1,25),
            new BlockPos(5,9,4),new BoundingBox(8,0,9,22,13,28),new BlockPos(23,14,29),7,
            new BlockPos(12,0,13),8,11,new BlockPos(15,11,15),new BlockPos(1,11,14),new BlockPos(7,9,18),null);
    private VigilLayout() {}
    public static Plan forVersion(int version) {
        if(version==2)return COMPACT;
        return VigilModules.ALL.stream().filter(layout->layout.plan().version()==version)
                .map(VigilModules.Layout::plan).findFirst().orElse(LEGACY);
    }
    public record Approach(BlockPos origin, Rotation rotation, List<BoundingBox> rooms) {
        BlockPos local(BlockPos p) {
            var inverse=switch(rotation) {
                case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
                case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
                default -> rotation;
            };
            return p.subtract(origin).rotate(inverse);
        }
    }
    public record Plan(int version, BlockPos vessel, List<BlockPos> sensors, BlockPos catalyst,
                       BlockPos shrieker, BlockPos record, BlockPos chest, BoundingBox gallery,
                       BlockPos size, int balcony, BlockPos bed, int bedWidth, int bedDepth,
                       BlockPos demonstration, BlockPos firstClick, BlockPos secondClick, Approach approach) {
        public boolean contains(BlockPos p) {
            if(approach!=null)return approach.rooms().stream().anyMatch(room->room.isInside(p));
            return p.getX()>=0 && p.getX()<size.getX() && p.getY()>=0 && p.getY()<size.getY()
                    && p.getZ()>=0 && p.getZ()<size.getZ();
        }
        /** Normalize the shorter passage to the original sequence of encounter thresholds. */
        public int passageDepth(BlockPos p) {
            if(version==1) return p.getZ();
            if(approach!=null) {
                var local=approach.local(p);
                if(local.getX()<0 || local.getX()>4 || local.getZ()>=9 || local.getZ()<-5 || local.getY()<0) return 34;
                int z=local.getZ();
                return z<0 ? z+5 : z<3 ? z+8 : 14+(z-3)*3;
            }
            if(p.getX()>=8 || p.getZ()>=19) return 34;
            return p.getZ()<6 ? p.getZ() : p.getZ()<9 ? p.getZ()+2 : 14+(p.getZ()-9)*2;
        }
        public float ambience(BlockPos p) {
            if(version==1 && p.getZ()>51) return 1;
            if(!contains(p) || passageDepth(p)<11 || p.getY()<balcony-3) return 1;
            return p.getY()<balcony ? .15F+.85F*(balcony-p.getY())/3 : .15F;
        }
    }
}
