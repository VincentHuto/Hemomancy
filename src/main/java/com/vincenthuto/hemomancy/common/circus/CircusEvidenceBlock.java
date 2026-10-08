package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Structure-only evidence has no survival recipe or drops. */
public final class CircusEvidenceBlock extends Block {
    private final String clue;
    public CircusEvidenceBlock(Properties properties, String clue) { super(properties); this.clue = clue; }
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
