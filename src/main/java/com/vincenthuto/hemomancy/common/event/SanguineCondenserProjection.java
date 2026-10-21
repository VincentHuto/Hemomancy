package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineCondenserBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

import java.util.OptionalDouble;

/**
 * Blood Projection into an empty cauldron or a Sanguine Condenser. Bare blocks no longer condense formations,
 * so these are the only projection targets that make them.
 */
public final class SanguineCondenserProjection {
    private SanguineCondenserProjection() {
    }

    /** Empty when {@code target} is neither an empty cauldron nor a Condenser; otherwise the blood spent. */
    public static OptionalDouble tryProject(ServerLevel level, ServerPlayer player, BlockPos target,
            double tileTransferRate) {
        boolean cauldron = level.getBlockState(target).is(Blocks.CAULDRON);
        if (!cauldron && !(level.getBlockEntity(target) instanceof SanguineCondenserBlockEntity))
            return OptionalDouble.empty();
        IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
        if (blood == null || !blood.isActive() || blood.getBloodVolume() <= 0.0D || tileTransferRate <= 0.0D)
            return OptionalDouble.of(0.0D);

        SanguineCondenserBlockEntity condenser = cauldron
                ? SanguineCondenserBlock.convertCauldron(level, target, player)
                : (SanguineCondenserBlockEntity) level.getBlockEntity(target);
        if (condenser == null) return OptionalDouble.of(0.0D);
        if (cauldron) {
            level.playSound(null, target, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 0.7F);
            SanguineCondenserEffects.converted(level, target);
        }

        double accepted = condenser.receiveProjectedBlood(player,
                Math.min(tileTransferRate, blood.getBloodVolume()));
        if (accepted <= 0.0D || !blood.drain(accepted)) {
            if (!condenser.isRiteLocked() && condenser.getBloodVolume() >= condenser.getMaxBloodVolume())
                player.displayClientMessage(Component.translatable("hemomancy.sanguine_condenser.full")
                        .withStyle(ChatFormatting.DARK_RED), true);
            return OptionalDouble.of(0.0D);
        }
        blood.addBloodSpend(accepted);
        PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));
        return OptionalDouble.of(accepted);
    }
}
