package com.vincenthuto.hemomancy.common.network.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record PacketSyncCircusSchool(CompoundTag progress) implements CustomPacketPayload {
    public static final Type<PacketSyncCircusSchool> TYPE = new Type<>(Hemomancy.rloc("sync_circus_school"));
    public static final StreamCodec<FriendlyByteBuf, PacketSyncCircusSchool> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeNbt(packet.progress), buf -> new PacketSyncCircusSchool(buf.readNbt()));
    public static PacketSyncCircusSchool from(ServerPlayer player) {
        var snapshot = CircusApprenticeshipProgress.state(player).copy();
        for (var lesson : CircusCurriculum.lessons()) snapshot.putBoolean("known." + lesson.summon(), CircusApprenticeshipProgress.knows(player, lesson.summon()));
        return new PacketSyncCircusSchool(snapshot);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
