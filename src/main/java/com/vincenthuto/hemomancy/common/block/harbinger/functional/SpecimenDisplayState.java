package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CabinetStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import java.util.List;
import java.util.stream.IntStream;

/** Each occupied compartment contributes one baked vial to the multipart model. */
public final class SpecimenDisplayState {
    public static final List<BooleanProperty> OCCUPIED = IntStream.range(0, 9)
            .mapToObj(i -> BooleanProperty.create("occupied_" + i)).toList();

    private SpecimenDisplayState() {}

    public static BlockState empty(BlockState state) {
        for (var property : OCCUPIED) state = state.setValue(property, false);
        return state;
    }

    public static void sync(BlockEntity entity, CabinetStorage storage) {
        var level = entity.getLevel();
        if (level == null || level.isClientSide || level.getBlockEntity(entity.getBlockPos()) != entity) return;
        var state = entity.getBlockState();
        var updated = state;
        for (int i = 0; i < OCCUPIED.size(); i++) updated = updated.setValue(OCCUPIED.get(i), storage.count(i) > 0);
        if (updated != state) level.setBlock(entity.getBlockPos(), updated, Block.UPDATE_CLIENTS);
    }
}
