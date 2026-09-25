package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;

public final class ScriptoriumPower {
    public static final TagKey<Block> SOURCES = TagKey.create(Registries.BLOCK,
            Hemomancy.rloc("scriptorium_power_sources"));

    private ScriptoriumPower() {}

    public static float contribution(Level level, BlockPos station, BlockPos offset) {
        if (!EnchantingTableBlock.BOOKSHELF_OFFSETS.contains(offset)) return 0;
        BlockPos source = station.offset(offset);
        var state = level.getBlockState(source);
        float power = state.getEnchantPowerBonus(level, source);
        if (state.is(SOURCES)) power = Math.max(1, power);
        if (power <= 0 || !level.getBlockState(station.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2))
                .is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) return 0;
        return power;
    }

    public static int power(Level level, BlockPos station) {
        float total = 0;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
            total += contribution(level, station, offset);
        return Math.clamp((int) total, 0, 15);
    }
}
