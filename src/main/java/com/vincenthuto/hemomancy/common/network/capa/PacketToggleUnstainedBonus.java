package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server packet: player toggles a passive Unstained bonus
 * (Silver Ward or Verdigris Aura) on or off from the progress screen.
 */
public class PacketToggleUnstainedBonus {

    /** 0 = Silver Ward, 1 = Verdigris Aura */
    private final int bonusId;

    public PacketToggleUnstainedBonus(int bonusId) {
        this.bonusId = bonusId;
    }

    public static void encode(PacketToggleUnstainedBonus msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.bonusId);
    }

    public static PacketToggleUnstainedBonus decode(FriendlyByteBuf buf) {
        return new PacketToggleUnstainedBonus(buf.readVarInt());
    }

    public static void handle(PacketToggleUnstainedBonus msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
                switch (msg.bonusId) {
                    case 0 -> progress.setSilverWardEnabled(!progress.isSilverWardEnabled());
                    case 1 -> progress.setVerdigrisAuraEnabled(!progress.isVerdigrisAuraEnabled());
                    default -> { return; }
                }
                UnstainedProgressEvents.syncProgress(player, progress);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
