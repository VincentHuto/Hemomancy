package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;

public record ResidentsRequestPacket(UUID identity, boolean dismiss) implements CustomPacketPayload {
    public static final UUID LIST = new UUID(0, 0);
    public static final Type<ResidentsRequestPacket> TYPE = new Type<>(Hemomancy.rloc("residents_request"));
    public static final StreamCodec<FriendlyByteBuf, ResidentsRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> { buf.writeUUID(msg.identity); buf.writeBoolean(msg.dismiss); },
            buf -> new ResidentsRequestPacket(buf.readUUID(), buf.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(ResidentsRequestPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> { if (context.player() instanceof ServerPlayer player) process(player, msg.identity, msg.dismiss); });
    }
    public static boolean process(ServerPlayer player, UUID identity, boolean dismiss) {
        var line = SuccessionResidents.line(player); if (line == null) return false;
        var data = SuccessionSavedData.get(player.serverLevel()); boolean changed = false;
        var heart = FoundingFaneSavedData.get(player.serverLevel()).getHeart(line.getLeaderUUID());
        boolean atFane = heart != null && player.distanceToSqr(heart.getX()+.5, heart.getY()+.5, heart.getZ()+.5) <= 64
                && player.serverLevel().getBlockState(heart).is(BlockInit.consecrated_bloodwell.get());
        var resident = data.residents.get(identity);
        if (!LIST.equals(identity) && resident != null && resident.bloodline.equals(line.getBloodlineUUID())
                && !resident.dismissed && !data.ledger.locked(identity)) {
            if (dismiss && line.canManage(player.getUUID())) {
                resident.dismissed = true; resident.dormant = true; data.ledger.unclaim(identity);
                line.removeNpcMember(identity); line.clampPoolToCapacity();
                BloodlineSavedData.get(player.server.overworld()).setDirty(); SuccessionResidents.sync(player.serverLevel(), line); changed = true;
            } else if (!dismiss && atFane) {
                int generation = data.ledger.reissue(identity);
                if (generation >= 0) {
                    var item = BoundMnemonicRemnantItem.create(resident, generation);
                    if (!player.getInventory().add(item)) player.drop(item, false);
                    changed = true;
                }
            }
        }
        if (changed) data.setDirty();
        var result = new CompoundTag(); var list = new ListTag();
        for (var r : data.residents.values()) if (r.bloodline.equals(line.getBloodlineUUID())) {
            var t = r.save(); var life = data.ledger.life(r.id);
            t.putBoolean("Alive", life != null && life.alive()); t.putBoolean("Locked", data.ledger.locked(r.id)); list.add(t);
        }
        result.put("Residents", list); result.putBoolean("AtFane", atFane); result.putBoolean("Leader", line.canManage(player.getUUID()));
        PacketHandler.sendToPlayer(player, new ResidentsSnapshotPacket(result)); return changed;
    }
}
