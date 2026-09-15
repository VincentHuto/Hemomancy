package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import net.minecraft.core.Direction;
import java.util.*;
import java.util.function.Predicate;

/** Pure, bounded plans: natural surface replacement or a small solid ground mound. */
public final class EscharianOvergrowthLayout {
    public static final int REACH = 6;
    public enum Layer { CENTER, RIM }
    public record Plant(Direction facing, int count) {
        public Plant {
            if (count < 1 || count > 5) throw new IllegalArgumentException("Scyphus count must be between 1 and 5");
        }
    }
    public record Cell(int x, int y, int z) {
        public Cell relative(Direction d) { return offset(d.getStepX(), d.getStepY(), d.getStepZ()); }
        public Cell offset(int dx, int dy, int dz) { return new Cell(x+dx,y+dy,z+dz); }
    }
    public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public int spanX() { return maxX-minX+1; }
        public int spanZ() { return maxZ-minZ+1; }
        public int height() { return maxY-minY+1; }
    }
    public record Plan(boolean ground, Map<Cell,Layer> backing, Map<Cell,Plant> plants) {
        public Plan {
            backing = Collections.unmodifiableMap(new LinkedHashMap<>(backing));
            plants = Collections.unmodifiableMap(new LinkedHashMap<>(plants));
        }
        public boolean empty() { return backing.isEmpty(); }
    }
    private EscharianOvergrowthLayout() {}
    private static Plan empty(boolean ground) { return new Plan(ground, Map.of(), Map.of()); }

    public static Plan patch(long seed, Cell support, EscharianOvergrowthConfiguration config,
                             Predicate<Cell> natural, Predicate<Cell> air) {
        Random random = new Random(new Random(seed).nextLong());
        int target = random.nextInt(config.minPatchBlocks(), config.maxPatchBlocks()+1);
        Map<Cell,Layer> backing = new LinkedHashMap<>();
        List<Cell> frontier = new ArrayList<>();
        Set<Cell> seen = new HashSet<>();
        frontier.add(support); seen.add(support);
        while (!frontier.isEmpty() && backing.size() < target) {
            Cell cell = frontier.remove(random.nextInt(frontier.size()));
            if (!natural.test(cell) || exposed(cell, air).isEmpty()) continue;
            backing.put(cell, Layer.CENTER);
            for (Direction d : Direction.values()) {
                Cell next = cell.relative(d);
                enqueue(next, support, frontier, seen);
                // Exposed cells meet at an edge around an inward corner; the shared stone stays intact.
                if (natural.test(next)) for (Direction turn : Direction.values()) {
                    if (turn.getAxis()!=d.getAxis()) enqueue(next.relative(turn), support, frontier, seen);
                }
            }
        }
        if (backing.size() < config.minPatchBlocks()) return empty(false);
        return decorate(false, backing, random, air);
    }

    private static void enqueue(Cell next, Cell origin, List<Cell> frontier, Set<Cell> seen) {
        if (Math.abs(next.x-origin.x)<=REACH && Math.abs(next.y-origin.y)<=REACH
                && Math.abs(next.z-origin.z)<=REACH && seen.add(next)) frontier.add(next);
    }

    public static Plan pile(long seed, Cell anchor, Predicate<Cell> natural, Predicate<Cell> air) {
        Random random = new Random(new Random(seed).nextLong());
        int width = 3+random.nextInt(2), depth = 3+random.nextInt(2), height = 2+random.nextInt(2);
        Set<Cell> base = new LinkedHashSet<>();
        int ox = random.nextInt(width), oz = random.nextInt(depth);
        for (int x=0;x<width;x++) for (int z=0;z<depth;z++) {
            if ((x==0 || x==width-1) && (z==0 || z==depth-1) && random.nextBoolean()) continue;
            base.add(anchor.offset(x-ox,0,z-oz));
        }
        Map<Cell,Layer> backing = new LinkedHashMap<>();
        for (Cell cell : base) {
            if (!natural.test(cell.relative(Direction.DOWN))) return empty(true);
            boolean edge = Direction.Plane.HORIZONTAL.stream().anyMatch(d -> !base.contains(cell.relative(d)));
            backing.put(cell, edge ? Layer.RIM : Layer.CENTER);
        }
        // The inner rectangle guarantees a solid, tapered tier despite irregular base corners.
        for (int x=1;x<width-1;x++) for (int z=1;z<depth-1;z++)
            backing.put(anchor.offset(x-ox,1,z-oz), Layer.CENTER);
        if (height==3) backing.put(anchor.offset(1+random.nextInt(width-2)-ox,2,1+random.nextInt(depth-2)-oz), Layer.CENTER);
        if (backing.keySet().stream().anyMatch(c -> !air.test(c))) return empty(true);
        return decorate(true, backing, random, air);
    }

    private static Plan decorate(boolean ground, Map<Cell,Layer> backing, Random random, Predicate<Cell> air) {
        Predicate<Cell> free = c -> !backing.containsKey(c) && air.test(c);
        if (ground) {
            Map<Cell,Plant> plants = new LinkedHashMap<>();
            for (var entry : backing.entrySet()) {
                if (entry.getValue()!=Layer.CENTER) continue;
                for (Direction face : exposed(entry.getKey(), free))
                    plants.putIfAbsent(entry.getKey().relative(face), new Plant(face, random.nextInt(1, 6)));
            }
            return plants.size()>=3 ? new Plan(true,backing,plants) : empty(true);
        }
        List<Cell> surface = new ArrayList<>();
        for (Cell c : backing.keySet()) if (!exposed(c, free).isEmpty()) surface.add(c);
        Collections.shuffle(surface, random);
        int minimum = Math.max(3, (int)Math.ceil(surface.size()*0.5));
        int maximum = (int)Math.floor(surface.size()*0.7);
        if (maximum<minimum) return empty(ground);
        int target = random.nextInt(minimum, maximum+1);
        Map<Cell,Plant> plants = new LinkedHashMap<>();
        for (Cell c : surface) {
            var faces = exposed(c, free);
            Collections.shuffle(faces, random);
            for (Direction face : faces) if (!plants.containsKey(c.relative(face))) {
                plants.put(c.relative(face),new Plant(face, random.nextInt(1, 6)));
                break;
            }
            if (plants.size()==target) break;
        }
        return plants.size()>=minimum ? new Plan(ground,backing,plants) : empty(ground);
    }
    private static List<Direction> exposed(Cell cell, Predicate<Cell> air) {
        List<Direction> result = new ArrayList<>();
        for (Direction d : Direction.values()) if (air.test(cell.relative(d))) result.add(d);
        return result;
    }
}
