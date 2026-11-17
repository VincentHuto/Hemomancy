package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Structure-only evidence has no survival recipe or drops. Each record is its own object (a stage bill, a loft rigging
 * log, a quarters ledger, a carousel maker's plate), modelled in tools/model_export/circus_set_pieces.py.
 */
public final class CircusEvidenceBlock extends TroupePropBlock {
    private final String clue;
    public CircusEvidenceBlock(Properties properties, String clue, double[]... outline) { super(properties, outline); this.clue = clue; }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && player.isAlive() && !player.isSpectator()) {
            if (!clue.equals("carousel") || !level.getEntitiesOfClass(CircusCarouselEntity.class, new AABB(pos).inflate(24)).isEmpty()) {
                CircusApprenticeshipProgress.state(player).putBoolean("clue." + clue, true);
                player.displayClientMessage(Component.translatable("hemomancy.circus.school.clue." + clue), false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
