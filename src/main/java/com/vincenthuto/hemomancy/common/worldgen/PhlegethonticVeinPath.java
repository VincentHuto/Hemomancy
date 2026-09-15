package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PhlegethonticVeinPath {
    public record Node(int x, int y, int z, int radius) {}
    private PhlegethonticVeinPath() {}

    public static List<Node> fromChunk(long seed, int chunkX, int chunkZ, int bottom) {
        Random random = new Random(PhlegethonticRules.seed(seed, chunkX, chunkZ, 0x50484c5645494eL));
        if (random.nextInt(18) != 0) return List.of();
        int length = 32 + random.nextInt(41), radius = 1 + random.nextInt(2);
        double x = chunkX * 16 + random.nextInt(16), z = chunkZ * 16 + random.nextInt(16);
        double heading = random.nextDouble() * Math.PI * 2;
        int y = bottom + 9 + random.nextInt(8);
        List<Node> nodes = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            if (i % 4 == 0) y = Math.clamp(y + random.nextInt(3) - 1, bottom + 9, bottom + 16);
            if (i % 9 == 0) radius = 1 + random.nextInt(2);
            nodes.add(new Node((int)Math.floor(x), y, (int)Math.floor(z), radius));
            heading += (random.nextDouble() - .5) * .6;
            x += Math.cos(heading);
            z += Math.sin(heading);
        }
        return List.copyOf(nodes);
    }

    public static List<Node> nearChunk(long seed, int chunkX, int chunkZ, int bottom) {
        List<Node> result = new ArrayList<>();
        int reach = PhlegethonticRules.VEIN_REACH_CHUNKS;
        for (int x = chunkX - reach; x <= chunkX + reach; x++) {
            for (int z = chunkZ - reach; z <= chunkZ + reach; z++) {
                for (Node node : fromChunk(seed, x, z, bottom)) {
                    if (node.x + node.radius + 4 >= chunkX * 16 && node.x - node.radius - 4 <= chunkX * 16 + 15
                            && node.z + node.radius + 4 >= chunkZ * 16 && node.z - node.radius - 4 <= chunkZ * 16 + 15)
                        result.add(node);
                }
            }
        }
        return List.copyOf(result);
    }
}
