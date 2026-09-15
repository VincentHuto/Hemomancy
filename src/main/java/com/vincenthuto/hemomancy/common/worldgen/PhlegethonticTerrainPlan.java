package com.vincenthuto.hemomancy.common.worldgen;

import java.util.*;
import java.util.function.Predicate;

/** Plans replacement states before touching the world. All returned positions belong to one chunk. */
public final class PhlegethonticTerrainPlan {
    private static final double BANK_REACH=16;
    public enum Material { AIR, SCAB, BLACKSTONE, BASALT, VENOUS, ICHOR }
    public record Voxel(int x, int y, int z) {
        public Voxel offset(int dx,int dy,int dz) { return new Voxel(x+dx,y+dy,z+dz); }
        public List<Voxel> neighbors() { return List.of(offset(1,0,0),offset(-1,0,0),offset(0,1,0),
                offset(0,-1,0),offset(0,0,1),offset(0,0,-1)); }
    }
    private PhlegethonticTerrainPlan() {}

    public static Map<Voxel,Material> vein(List<PhlegethonticVeinPath.Node> nodes,int chunkX,int chunkZ,
                                          Predicate<Voxel> replaceable) {
        Set<Voxel> core = new HashSet<>();
        int minX=chunkX*16, minZ=chunkZ*16;
        for(var n:nodes) for(int x=-n.radius();x<=n.radius();x++) for(int y=-n.radius();y<=n.radius();y++) {
            for(int z=-n.radius();z<=n.radius();z++) if(x*x+y*y+z*z<=n.radius()*n.radius()) {
                Voxel p=new Voxel(n.x()+x,n.y()+y,n.z()+z);
                if(p.x>=minX-2 && p.x<=minX+17 && p.z>=minZ-2 && p.z<=minZ+17) core.add(p);
            }
        }
        Map<Voxel,Boolean> availability=new HashMap<>();
        Set<Voxel> accepted=new HashSet<>();
        for(Voxel p:core) {
            boolean valid=true;
            scan: for(int dx=-2;dx<=2;dx++) for(int dy=-2;dy<=2;dy++) for(int dz=-2;dz<=2;dz++) {
                if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2) continue;
                if(!availability.computeIfAbsent(p.offset(dx,dy,dz),replaceable::test)) {valid=false;break scan;}
            }
            if(valid) accepted.add(p);
        }
        Map<Voxel,Material> result=new HashMap<>();
        for(Voxel p:accepted) for(int dx=-2;dx<=2;dx++) for(int dy=-2;dy<=2;dy++) for(int dz=-2;dz<=2;dz++) {
            if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2) continue;
            Voxel shell=p.offset(dx,dy,dz);
            if(owns(shell,chunkX,chunkZ)) result.put(shell,Material.SCAB);
        }
        for(Voxel p:accepted) if(owns(p,chunkX,chunkZ)) result.put(p,Material.ICHOR);
        return result;
    }

    public static PhlegethonticBasinLayout.Column column(List<PhlegethonticBasinLayout> layouts,int x,int z) {
        PhlegethonticBasinLayout.Column result=null;
        double ceiling=Double.NEGATIVE_INFINITY;
        for(var layout:layouts) {
            var sampled=layout.sample(x+.5,z+.5);
            if(prefers(sampled,result)) result=sampled;
            ceiling=Math.max(ceiling,sampled.ceiling());
        }
        return result.withCeiling(ceiling);
    }

    public static boolean prefers(PhlegethonticBasinLayout.Column candidate,PhlegethonticBasinLayout.Column current) {
        if(current==null)return true;
        boolean candidateCourse=candidate.channel() && candidate.distance()<=0;
        boolean currentCourse=current.channel() && current.distance()<=0;
        return candidateCourse!=currentCourse?candidateCourse:candidate.distance()<current.distance();
    }

    public static Map<Voxel,Material> basin(List<PhlegethonticBasinLayout> layouts,long seed,int chunkX,int chunkZ,
                                           int bottom,int top,Predicate<Voxel> allowedColumn,
                                           Predicate<Voxel> replaceable) {
        Map<Voxel,Material> result=new HashMap<>();
        int minX=chunkX*16-8,minZ=chunkZ*16-8;
        // Biome edges vary within quart cells; cache exact block positions in the bounded read halo.
        byte[] clearance=new byte[(top-bottom)*32*32];
        Predicate<Voxel> open=p -> {
            if(p.y()<bottom || p.y()>=top) return false;
            int index=((p.y()-bottom)*32+p.z()-minZ)*32+p.x()-minX;
            if(clearance[index]==0) clearance[index]=(byte)(allowedColumn.test(p)?1:2);
            return clearance[index]==1;
        };
        Map<Long,Boolean> columns=new HashMap<>();
        Map<Long,PhlegethonticBasinLayout.Column> samples=new HashMap<>();
        Map<Long,Integer> floors=new HashMap<>();
        for(int x=chunkX*16-4;x<chunkX*16+20;x++)for(int z=chunkZ*16-4;z<chunkZ*16+20;z++) {
            var sample=column(layouts,x,z);
            long key=columnKey(x,z);
            samples.put(key,sample);
            floors.put(key,sample.channel()?sample.floor()
                    :Math.min(sample.surface()-1,sample.floor()+floorRelief(seed,x,z)));
        }
        Map<Long,Bank> banks=new HashMap<>();
        for(int x=chunkX*16-2;x<chunkX*16+18;x++) for(int z=chunkZ*16-2;z<chunkZ*16+18;z++) {
            var bank=retainingBank(samples,x,z);banks.put(columnKey(x,z),bank);
            boolean allowed=true;
            for(int y=bank.floor()-2;y<=bank.surface()+4;y++) if(!open.test(new Voxel(x,y,z))) {allowed=false;break;}
            columns.put(columnKey(x,z),allowed);
        }
        for(int x=chunkX*16;x<chunkX*16+16;x++) for(int z=chunkZ*16;z<chunkZ*16+16;z++) {
            if(!columns.get(columnKey(x,z))) continue;
            var c=samples.get(columnKey(x,z));
            if(c.distance()>BANK_REACH) continue;
            boolean sealed=true;
            for(int dx=-2;dx<=2;dx++) for(int dz=-2;dz<=2;dz++)
                if(!columns.getOrDefault(columnKey(x+dx,z+dz),false)) sealed=false;
            int floor=floors.get(columnKey(x,z));
            var retaining=banks.get(columnKey(x,z));
            int bank=retaining.surface()+1+bankRelief(x,z);
            int underside=Math.min(c.raised()?Math.max(bottom+24,floor-28):floor-3,retaining.floor()-3);
            int ceiling=Math.min(top-6,Math.max(bank,(int)Math.floor(c.ceiling())));
            double wallSlope=.35+PhlegethonticRules.noise(seed ^ 0x5641554c54L,x/17.0,z/17.0)*.06;
            for(int y=Math.max(bottom+5,underside);y<=ceiling;y++) {
                Voxel p=new Voxel(x,y,z);
                if(!open.test(p) || !replaceable.test(p)) continue;
                Material material=null;
                if(c.distance()<=0) {
                    if(!sealed && y>=floor-2 && y<=bank) material=Material.SCAB;
                    else if(y>c.surface()) material=Material.AIR;
                    else if(y>floor) material=Material.ICHOR;
                    else if(y>=floor-2) material=Material.SCAB;
                    else if(c.raised()) {
                        double phase=c.courseDistance()+PhlegethonticRules.noise(seed,x/13.0,z/13.0)*2;
                        double arch=Math.sin(phase*Math.PI*2/11);
                        boolean voidArch=y<floor-5 && y>underside+2 && arch>.3
                                && (floor-5-y)<arch*17;
                        material=voidArch?Material.AIR:stone(seed,x,y,z,floor);
                    } else if(y>=retaining.floor()-3)material=stone(seed,x,y,z,floor);
                } else if(y<=bank && y>=retaining.floor()-2) {
                    material=c.distance()<2 || touchesIchor(samples,floors,x,y,z)?Material.SCAB:y>=bank-2
                            ?shoreMaterial(seed,x,y,z,c.distance()):stone(seed,x,y,z,floor);
                } else if(y>bank) material=Material.AIR;
                if(material==Material.AIR && y>bank+2 && !vaultFits(open,p,bank,wallSlope)) continue;
                if(material!=null) result.put(p,material);
            }
        }
        return result;
    }

    /** Wider clearance at greater height rounds the cut before a biome or protected obstruction. */
    private static boolean vaultFits(Predicate<Voxel> open,Voxel p,int bank,double slope) {
        int radius=Math.min(8,(int)Math.ceil((p.y()-bank-2)*slope));
        if(!open.test(p.offset(0,(radius+1)/2,0))) return false;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            if((dx!=0 || dz!=0) && !open.test(p.offset(dx*radius,0,dz*radius))) return false;
        }
        return true;
    }

    private record Bank(int floor,int surface) {}
    private static Bank retainingBank(Map<Long,PhlegethonticBasinLayout.Column> samples,int x,int z) {
        var own=samples.get(columnKey(x,z));int floor=own.floor(),surface=own.surface();
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++) {
            var neighbor=samples.get(columnKey(x+dx,z+dz));
            if(neighbor.distance()<=0){floor=Math.min(floor,neighbor.floor());surface=Math.max(surface,neighbor.surface());}
        }
        return new Bank(floor,surface);
    }

    private static Material stone(long seed,int x,int y,int z,int floor) {
        if(y>=floor-7 && y<floor-2) return Material.VENOUS;
        double noise=PhlegethonticRules.noise(seed+y*17L,x/7.0,z/7.0);
        return noise<-.1?Material.BLACKSTONE:noise<.6?Material.BASALT:Material.VENOUS;
    }
    private static Material shoreMaterial(long seed,int x,int y,int z,double distance) {
        if(distance<2)return Material.SCAB;
        double dither=dither(seed,x,y,z);
        if(distance<7)return dither<(distance-2)/5?Material.BLACKSTONE:Material.SCAB;
        if(distance<10)return Material.BLACKSTONE;
        if(distance<16)return dither<(distance-10)/6?Material.VENOUS:Material.BLACKSTONE;
        return Material.VENOUS;
    }
    public static int bankRelief(int x,int z) {
        double relief=PhlegethonticRules.noise(0x42414e4b52454c49L,x/11.0,z/11.0)*.7
                +PhlegethonticRules.noise(0x53555246414345L,x/4.0,z/4.0)*.3;
        return (int)Math.clamp(Math.round(relief+1),0,2);
    }
    private static int floorRelief(long seed,int x,int z) {
        double relief=PhlegethonticRules.noise(seed^0x464c4f4f524f4e45L,x/13.0,z/13.0)*.65
                +PhlegethonticRules.noise(seed^0x464c4f4f5254574fL,x/5.0,z/5.0)*.35;
        return relief<-.22?-1:relief>.22?1:0;
    }
    private static boolean touchesIchor(Map<Long,PhlegethonticBasinLayout.Column> samples,Map<Long,Integer> floors,
                                        int x,int y,int z) {
        return ichorAt(samples,floors,x+1,y,z) || ichorAt(samples,floors,x-1,y,z)
                || ichorAt(samples,floors,x,y,z+1) || ichorAt(samples,floors,x,y,z-1);
    }
    private static boolean ichorAt(Map<Long,PhlegethonticBasinLayout.Column> samples,Map<Long,Integer> floors,
                                   int x,int y,int z) {
        long key=columnKey(x,z);
        var neighbor=samples.get(key);
        int floor=floors.get(key);
        return neighbor.distance()<=0 && y>floor && y<=neighbor.surface();
    }
    private static double dither(long seed,int x,int y,int z) {
        long mixed=PhlegethonticRules.seed(seed^y*0x632be59bd9b4e019L,x,z,0x444954484552L);
        return (mixed>>>11)*0x1.0p-53;
    }
    private static long columnKey(int x,int z) { return ((long)x<<32) ^ (z&0xffffffffL); }
    public static boolean owns(Voxel p,int chunkX,int chunkZ) {return Math.floorDiv(p.x,16)==chunkX && Math.floorDiv(p.z,16)==chunkZ;}
}
