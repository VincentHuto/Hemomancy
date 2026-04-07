package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client packet: synchronises the player's current Unstained progress.
 */
public class PacketSyncUnstainedProgress {

    private final boolean begunPurification;
    private final float purity;
    private final boolean clarityUnlocked;
    private final float clarity;

    public PacketSyncUnstainedProgress(IUnstainedProgress progress) {
        this.begunPurification = progress.hasBegunPurification();
        this.purity = progress.getPurity();
        this.clarityUnlocked = progress.hasClarityUnlocked();
        this.clarity = progress.getClarity();
    }

    public PacketSyncUnstainedProgress(boolean begunPurification, float purity, boolean clarityUnlocked, float clarity) {
        this.begunPurification = begunPurification;
        this.purity = purity;
        this.clarityUnlocked = clarityUnlocked;
        this.clarity = clarity;
    }

    public static void encode(PacketSyncUnstainedProgress msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.begunPurification);
        buf.writeFloat(msg.purity);
        buf.writeBoolean(msg.clarityUnlocked);
        buf.writeFloat(msg.clarity);
    }

    public static PacketSyncUnstainedProgress decode(FriendlyByteBuf buf) {
        return new PacketSyncUnstainedProgress(buf.readBoolean(), buf.readFloat(), buf.readBoolean(), buf.readFloat());
    }

    public static void handle(PacketSyncUnstainedProgress msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA)
                        .ifPresent(progress -> {
                            progress.setBegunPurification(msg.begunPurification);
                            progress.setPurity(msg.purity);
                            progress.setClarityUnlocked(msg.clarityUnlocked);
                            progress.setClarity(msg.clarity);
                        });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
