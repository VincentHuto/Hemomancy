package com.vincenthuto.hemomancy.common.worldgen;

import java.util.*;

/** A river network in world coordinates, independent of chunk generation order. */
public record PhlegethonticBasinLayout(long id, int cellX, int cellZ, List<Basin> basins, List<Channel> channels) {
    public record Basin(double x, double z, double radiusX, double radiusZ, int surface) {}
    public record Point(double x, double z, double surface) {}
    public record Channel(int from, int to, double halfWidth, List<Point> points, boolean raised) {}
    public record Column(double distance, int surface, int floor, boolean channel, boolean raised,
                         double courseDistance, double dx, double dz, double ceiling) {
        public Column withCeiling(double height) {
            return new Column(distance, surface, floor, channel, raised, courseDistance, dx, dz, height);
        }
    }

    public static PhlegethonticBasinLayout create(long worldSeed, int cellX, int cellZ, int seaLevel) {
        long seed = PhlegethonticRules.seed(worldSeed, cellX, cellZ, 0x50484c424153494eL);
        Random random = new Random(seed);
        int count = 2 + random.nextInt(3);
        List<Basin> basins = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count + random.nextDouble() * .5;
            double x = cellX * 128 + 64 + Math.cos(angle) * (42 + random.nextInt(15));
            double z = cellZ * 128 + 64 + Math.sin(angle) * (42 + random.nextInt(15));
            double radius = 18 + random.nextInt(25);
            basins.add(new Basin(x, z, Math.clamp(radius * (.7 + random.nextDouble() * .6),18,42),
                    Math.clamp(radius * (.7 + random.nextDouble() * .6),18,42), seaLevel + 8 + random.nextInt(29)));
        }
        List<Channel> channels = new ArrayList<>();
        boolean[] connected = new boolean[count];
        connected[0] = true;
        for (int i = 1; i < count; i++) {
            int from = -1, to = -1;
            double nearest = Double.POSITIVE_INFINITY;
            for (int a = 0; a < count; a++) if (connected[a]) {
                for (int b = 0; b < count; b++) if (!connected[b]) {
                    double distance = Math.hypot(basins.get(a).x - basins.get(b).x, basins.get(a).z - basins.get(b).z);
                    if (distance < nearest) { nearest = distance; from = a; to = b; }
                }
            }
            channels.add(connect(basins, from, to, random));
            connected[to] = true;
        }
        if (count == 4) {
            outer: for (int a = 0; a < count; a++) for (int b = a + 1; b < count; b++) {
                boolean existing = false;
                for (Channel c : channels) if (c.from == a && c.to == b || c.from == b && c.to == a) existing = true;
                if (!existing) { channels.add(connect(basins, a, b, random)); break outer; }
            }
        }
        return new PhlegethonticBasinLayout(seed, cellX, cellZ, List.copyOf(basins), List.copyOf(channels));
    }

    private static Channel connect(List<Basin> basins, int from, int to, Random random) {
        if (basins.get(from).surface < basins.get(to).surface) { int swap = from; from = to; to = swap; }
        Basin a = basins.get(from), b = basins.get(to);
        double length = Math.max(1, Math.hypot(b.x - a.x, b.z - a.z));
        double nx = -(b.z - a.z) / length, nz = (b.x - a.x) / length;
        double bendA = (12 + random.nextDouble() * 24) * (random.nextBoolean() ? 1 : -1);
        double bendB = (12 + random.nextDouble() * 24) * (random.nextBoolean() ? 1 : -1);
        double ax = a.x + (b.x - a.x) / 3 + nx * bendA, az = a.z + (b.z - a.z) / 3 + nz * bendA;
        double bx = a.x + (b.x - a.x) * 2 / 3 + nx * bendB, bz = a.z + (b.z - a.z) * 2 / 3 + nz * bendB;
        List<Point> points = new ArrayList<>();
        int samples = Math.max(24, (int)(length / 2));
        for (int i = 0; i <= samples; i++) {
            double t = (double)i / samples, u = 1 - t;
            points.add(new Point(u*u*u*a.x + 3*u*u*t*ax + 3*u*t*t*bx + t*t*t*b.x,
                    u*u*u*a.z + 3*u*u*t*az + 3*u*t*t*bz + t*t*t*b.z,
                    a.surface + (b.surface - a.surface) * t));
        }
        double halfWidth=4+random.nextDouble()*3;
        boolean raised=random.nextDouble()<.8;
        Basin primary=basins.stream().max(Comparator.comparingDouble(basin -> basin.radiusX*basin.radiusZ)).orElseThrow();
        return new Channel(from,to,halfWidth,List.copyOf(points),raised || a.equals(primary) || b.equals(primary));
    }

    public Column sample(double x, double z) {
        Column best = null;
        double warp = PhlegethonticRules.noise(id, x / 19, z / 19) * 3;
        double ceiling = Double.NEGATIVE_INFINITY;
        double relief = PhlegethonticRules.noise(id ^ 0x5641554c54L, x / 31, z / 31) * 4
                + PhlegethonticRules.noise(id ^ 0x524f4f46L, x / 9, z / 9) * 1.25;
        for (Basin b : basins) {
            double dx = (x - b.x) / b.radiusX, dz = (z - b.z) / b.radiusZ;
            double radius = Math.min(b.radiusX, b.radiusZ);
            double smoothDistance = (Math.sqrt(dx * dx + dz * dz) - 1) * radius + warp;
            double distance = basinDistance(b, dx, dz, radius, smoothDistance);
            ceiling = Math.max(ceiling, vaultRoof(smoothDistance, radius, b.surface, 29 + relief));
            int depth = (int)Math.clamp(-distance * .45, 0, 8);
            int floor = b.surface - depth - 1;
            if (best == null || distance < best.distance)
                best = new Column(distance, b.surface, floor, false, false, 0, -dz, dx, ceiling);
        }
        for (Channel c : channels) {
            double travelled = 0;
            for (int i = 1; i < c.points.size(); i++) {
                Point a = c.points.get(i - 1), b = c.points.get(i);
                double dx = b.x - a.x, dz = b.z - a.z, squared = dx*dx + dz*dz;
                double t = squared == 0 ? 0 : Math.clamp(((x-a.x)*dx + (z-a.z)*dz) / squared, 0, 1);
                double length = Math.sqrt(squared);
                double distance = Math.hypot(x-a.x-t*dx, z-a.z-t*dz) - c.halfWidth + warp * .3;
                double riverSurface = a.surface + (b.surface-a.surface)*t;
                ceiling = Math.max(ceiling, vaultRoof(distance, c.halfWidth, riverSurface, 22 + relief));
                // A connecting trough keeps its floor and flow direction through the reservoir.
                boolean outlet=best!=null && !best.channel && distance<=0;
                if (best == null || distance < best.distance || outlet) {
                    int surface = (int)Math.floor(riverSurface);
                    best = new Column(distance, surface, surface-3, true, c.raised,
                            travelled+t*length, length == 0 ? 0 : dx/length, length == 0 ? 0 : dz/length, ceiling);
                }
                travelled += length;
            }
        }
        return best.withCeiling(ceiling);
    }

    private double basinDistance(Basin basin,double x,double z) {
        double dx=(x-basin.x)/basin.radiusX,dz=(z-basin.z)/basin.radiusZ;
        double radius=Math.min(basin.radiusX,basin.radiusZ);
        double smoothDistance=(Math.sqrt(dx*dx+dz*dz)-1)*radius
                +PhlegethonticRules.noise(id,x/19,z/19)*3;
        return basinDistance(basin,dx,dz,radius,smoothDistance);
    }

    private double basinDistance(Basin basin,double dx,double dz,double radius,double smoothDistance) {
        // The largest edge displacement is twelve blocks. Outside this band it cannot affect
        // either the capped basin floor or the sixteen-block authored shore, so avoid angular work.
        if(smoothDistance<-30 || smoothDistance>28)return smoothDistance;
        return smoothDistance+fingerWarp(basin,dx,dz,radius);
    }

    private double fingerWarp(Basin basin, double dx, double dz, double radius) {
        long edgeSeed=PhlegethonticRules.seed(id,(int)Math.floor(basin.x),(int)Math.floor(basin.z),0x46494e47455253L);
        int fingers=5+(int)Math.floorMod(edgeSeed,4);
        double turns=Math.atan2(dz,dx)/(Math.PI*2);
        double phase=unit(edgeSeed^0x50484153454f4e45L)*.5;
        double secondPhase=unit(edgeSeed^0x504841534554574fL)*.5;
        double wave=triangle(turns*fingers+phase)*.72
                +triangle(turns*(fingers*2+1)+secondPhase)*.28;
        wave=Math.clamp(wave,-1,1);
        double shaped=wave*Math.abs(wave);
        double amplitude=Math.clamp(radius*.22+3,6,12);
        return shaped*amplitude;
    }

    private static double triangle(double value) {
        double fraction=value-Math.floor(value);
        return 1-4*Math.abs(fraction-.5);
    }

    private static double unit(long value) {
        return (value>>>11)*0x1.0p-53*2-1;
    }

    /** Rounded clearance joins independently of which reservoir or trough owns the fluid column. */
    private static double vaultRoof(double distance, double radius, double surface, double height) {
        double radial = Math.max(0, (distance + radius) / (radius + 12));
        return radial >= 1 ? Double.NEGATIVE_INFINITY : surface - 4 + height * Math.sqrt(1 - radial * radial);
    }

    public Basin largestBasin() {
        return basins.stream().max(Comparator.comparingDouble(b -> b.radiusX * b.radiusZ)).orElseThrow();
    }

    /** Candidate order is shared by all receiver chunks; only the first eligible shore owns a sentinel. */
    public List<PhlegethonticTerrainPlan.Voxel> sentinelCandidates() {
        List<PhlegethonticTerrainPlan.Voxel> result=new ArrayList<>();
        var ordered=basins.stream().sorted(Comparator.comparingDouble((Basin b) -> b.radiusX*b.radiusZ).reversed()).toList();
        double phase=(id>>>11)*0x1.0p-53*Math.PI*2;
        for(Basin b:ordered)for(int i=0;i<32;i++) {
            double angle=phase+i*Math.PI/16;
            double radius=Math.max(b.radiusX,b.radiusZ);
            PhlegethonticTerrainPlan.Voxel previous=null;
            for(int step=0;step<=Math.ceil(radius*1.3);step++) {
                double scale=.55+step/radius;
                int x=(int)Math.floor(b.x+Math.cos(angle)*b.radiusX*scale);
                int z=(int)Math.floor(b.z+Math.sin(angle)*b.radiusZ*scale);
                var candidate=new PhlegethonticTerrainPlan.Voxel(x,0,z);
                if(candidate.equals(previous))continue;
                previous=candidate;
                double localDistance=basinDistance(b,x+.5,z+.5);
                if(localDistance<3 || localDistance>7)continue;
                var column=sample(x+.5,z+.5);
                if(column.distance()<3 || column.distance()>7)continue;
                int supportTop=column.surface()+1+PhlegethonticTerrainPlan.bankRelief(x,z);
                boolean supported=true;
                for(int dx=-1;dx<=1 && supported;dx++)for(int dz=-1;dz<=1;dz++) {
                    double neighborDistance=basinDistance(b,x+dx+.5,z+dz+.5);
                    if(neighborDistance<1 || neighborDistance>9){supported=false;break;}
                    var neighbor=sample(x+dx+.5,z+dz+.5);
                    if(neighbor.distance()<1 || neighbor.distance()>9
                            || neighbor.surface()+1+PhlegethonticTerrainPlan.bankRelief(x+dx,z+dz)!=supportTop) {
                        supported=false;break;
                    }
                }
                if(supported) {
                    result.add(new PhlegethonticTerrainPlan.Voxel(x,supportTop+1,z));
                    break;
                }
            }
        }
        return List.copyOf(result);
    }

    public static List<PhlegethonticBasinLayout> nearChunk(long seed, int chunkX, int chunkZ, int seaLevel) {
        int cellX = Math.floorDiv(chunkX, 8), cellZ = Math.floorDiv(chunkZ, 8);
        List<PhlegethonticBasinLayout> result = new ArrayList<>(9);
        for (int x = cellX - 1; x <= cellX + 1; x++) for (int z = cellZ - 1; z <= cellZ + 1; z++)
            result.add(create(seed, x, z, seaLevel));
        return List.copyOf(result);
    }
}
