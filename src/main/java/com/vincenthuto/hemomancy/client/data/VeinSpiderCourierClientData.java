package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.item.harbinger.memory.VeinSpiderRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class VeinSpiderCourierClientData {
    private static final int MAX_COURIERS = 64;
    private static final List<Entry> COURIERS = new ArrayList<>();

    private VeinSpiderCourierClientData() {
    }

    public static void spawn(BlockPos from, BlockPos to, ItemStack stack, int seed) {
        if (from.equals(to) || stack.isEmpty()) {
            return;
        }
        if (COURIERS.size() >= MAX_COURIERS) {
            COURIERS.remove(0);
        }
        COURIERS.add(new Entry(from.immutable(), to.immutable(), stack.copyWithCount(1), seed));
    }

    public static List<Entry> entries() {
        return List.copyOf(COURIERS);
    }

    public static void tick() {
        Iterator<Entry> iterator = COURIERS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().tick()) {
                iterator.remove();
            }
        }
    }

    public static final class Entry {
        private final BlockPos from;
        private final BlockPos to;
        private final ItemStack stack;
        private final int seed;
        private int remainingTicks = VeinSpiderRules.COURIER_VISIBLE_TICKS;

        private Entry(BlockPos from, BlockPos to, ItemStack stack, int seed) {
            this.from = from;
            this.to = to;
            this.stack = stack;
            this.seed = seed;
        }

        public BlockPos from() {
            return from;
        }

        public BlockPos to() {
            return to;
        }

        public ItemStack stack() {
            return stack;
        }

        public int seed() {
            return seed;
        }

        public float progress(float partialTick) {
            return VeinSpiderRules.courierProgress(remainingTicks, VeinSpiderRules.COURIER_VISIBLE_TICKS, partialTick);
        }

        public float alpha(float partialTick) {
            float progress = progress(partialTick);
            return Math.min(1.0F, Math.min(progress * 5.0F, (1.0F - progress) * 5.0F));
        }

        private boolean tick() {
            remainingTicks--;
            return VeinSpiderRules.isCourierExpired(remainingTicks);
        }
    }
}
