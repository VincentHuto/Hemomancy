package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.encounter.VagrantMindEncounterManager;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** A deliberate break, after protection/event cancellation checks, opts into the Mind's encounter. */
public final class DormantGanglionBlock extends Block {
    private static final ParticleColor GLOW = new ParticleColor(210, 48, 82);

    public DormantGanglionBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_RED).strength(3.0F, 3_600_000.0F)
                .sound(SoundType.WART_BLOCK).lightLevel(state -> 8).pushReaction(PushReaction.BLOCK).noLootTable());
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (player instanceof ServerPlayer serverPlayer)
            VagrantMindEncounterManager.tryAwaken(serverPlayer, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) return;
        level.addParticle(HemoParticleData.glow(GLOW), pos.getX() + random.nextDouble(),
                pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, .008, 0);
    }
}
