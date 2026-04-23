package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → Server packet: player toggles a passive Unstained bonus
 * (Silver Ward or Verdigris Aura) on or off from the progress screen.
 */
public class PacketToggleUnstainedBonus implements CustomPacketPayload {

	public static final Type<PacketToggleUnstainedBonus> TYPE = new Type<>(Hemomancy.rloc("packet_toggle_unstained_bonus"));
	public static final StreamCodec<FriendlyByteBuf, PacketToggleUnstainedBonus> STREAM_CODEC = StreamCodec.of(PacketToggleUnstainedBonus::encode, PacketToggleUnstainedBonus::decode);

    /** 0 = Verdigris Aura (purity path), 1 = Silver Ward (clarity path) */
    private final int bonusId;

    public PacketToggleUnstainedBonus(int bonusId) {
        this.bonusId = bonusId;
    }

    public static void encode(FriendlyByteBuf buf, PacketToggleUnstainedBonus msg) {
        buf.writeVarInt(msg.bonusId);
    }

    public static PacketToggleUnstainedBonus decode(FriendlyByteBuf buf) {
        return new PacketToggleUnstainedBonus(buf.readVarInt());
    }

    public static void handle(final PacketToggleUnstainedBonus msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.player();
            if (player == null) return;

            HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
                switch (msg.bonusId) {
                    case 0 -> progress.setVerdigrisAuraEnabled(!progress.isVerdigrisAuraEnabled());
                    case 1 -> progress.setSilverWardEnabled(!progress.isSilverWardEnabled());
                    default -> { return; }
                }
                UnstainedProgressEvents.syncProgress(player, progress);
            });
        });
    }

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
