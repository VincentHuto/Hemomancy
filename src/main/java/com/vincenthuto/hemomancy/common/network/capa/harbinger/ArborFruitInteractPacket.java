package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborOfWillLayout;
import com.vincenthuto.hemomancy.common.worldgen.arbor.ArborSkillPresentation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ArborFruitInteractPacket(int arborEntityId, int skillId) implements CustomPacketPayload {
    public static final Type<ArborFruitInteractPacket> TYPE = new Type<>(Hemomancy.rloc("arbor_fruit_interact"));
    public static final StreamCodec<FriendlyByteBuf, ArborFruitInteractPacket> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> { buf.writeVarInt(msg.arborEntityId); buf.writeVarInt(msg.skillId); },
            buf -> new ArborFruitInteractPacket(buf.readVarInt(), buf.readVarInt()));

    public static void handle(ArborFruitInteractPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)
                    || !player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
            Entity entity = player.level().getEntity(msg.arborEntityId);
            SkillPoint skill = SkillPointInit.getById(msg.skillId);
            if (!(entity instanceof ArborOfWillEntity arbor) || skill == null || !arbor.isOwnedBy(player)) return;
            if (!HemoCapabilityAccess.requireSkillProgress(player).isUnlocked(skill)) return;
            ArborOfWillLayout.FruitPlacement fruit = ArborSkillPresentation.placements(arbor.chamberRadius()).stream()
                    .filter(candidate -> candidate.skillId() == skill.getId()).findFirst().orElse(null);
            if (fruit == null) return;
            double reach = player.isCreative() ? 5.0D : 4.5D;
            if (player.getEyePosition().distanceToSqr(arbor.position().add(fruit.x(), fruit.y(), fruit.z()))
                    > reach * reach) return;
            PacketHandler.sendToPlayer(player, new OpenArborSkillsPacket(skill.getId()));
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
