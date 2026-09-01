package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.WitnessOrganRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.WitnessOrganBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WitnessOrganEvents {
    private WitnessOrganEvents() {
    }

    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos notePos = event.getPos();
        long notePacked = WitnessOrganRules.packPosition(notePos.getX(), notePos.getY(), notePos.getZ());
        BlockPos.betweenClosedStream(notePos.offset(-WitnessOrganRules.RECORD_RADIUS_BLOCKS,
                        -WitnessOrganRules.RECORD_RADIUS_BLOCKS, -WitnessOrganRules.RECORD_RADIUS_BLOCKS),
                notePos.offset(WitnessOrganRules.RECORD_RADIUS_BLOCKS,
                        WitnessOrganRules.RECORD_RADIUS_BLOCKS, WitnessOrganRules.RECORD_RADIUS_BLOCKS))
                .forEach(candidate -> recordIfWitnessOrgan(level, candidate.immutable(), notePacked, notePos,
                        event.getVanillaNoteId()));
    }

    private static void recordIfWitnessOrgan(ServerLevel level, BlockPos organPos, long notePacked, BlockPos notePos,
            int vanillaNoteId) {
        if (!level.getBlockState(organPos).is(BlockInit.witness_organ.get())) {
            return;
        }
        long organPacked = WitnessOrganRules.packPosition(organPos.getX(), organPos.getY(), organPos.getZ());
        if (!WitnessOrganRules.shouldRecordNote(notePacked, organPacked)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(organPos);
        if (blockEntity instanceof WitnessOrganBlockEntity organ) {
            organ.record(notePos, vanillaNoteId);
        }
    }
}
