package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Preview only. The server chooses the selected power and clips its aim. */
public record ManipulationChargeVisualPacket(int heldTicks) implements CustomPacketPayload {
    public static final Type<ManipulationChargeVisualPacket> TYPE = new Type<>(Hemomancy.rloc("manipulation_charge_visual"));
    public static final StreamCodec<FriendlyByteBuf, ManipulationChargeVisualPacket> STREAM_CODEC =
            StreamCodec.of((buf,p) -> buf.writeVarInt(p.heldTicks), buf -> new ManipulationChargeVisualPacket(buf.readVarInt()));

    public static void handle(ManipulationChargeVisualPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (packet.heldTicks>0 && com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.isParalyzed(player)) return;
            var known=HemoCapabilityAccess.requireKnownManipulations(player);
            var saved=known.getSelectedManip();
            var selected=saved==null?null:ManipulationInit.getByName(saved.getName());
            if(selected==null || selected.getType()!=EnumManipulationType.CHARGED || !known.isManipEquipped(selected)
                    || !known.isManipulationAvailable(selected) || !HemoCapabilityAccess.requireBloodVolume(player).isActive()
                    || selected.isOnCooldown(player) || !player.isAlive()) return;
            if(HemoCapabilityAccess.getUnstainedProgress(player).map(
                    com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules::blocksKnownBloodPowerUse).orElse(false)
                    || HemoCapabilityAccess.getBloodTendency(player).map(t -> t.getAlignmentByTendency(selected.getTend()) < selected.getAlignLevel()).orElse(true)) return;
            var form=ManipulationVisuals.chargeForm(selected.getName());
            if(form==null || packet.heldTicks<0 || packet.heldTicks>selected.getRequiredChargeTicks())return;
            long now=player.level().getGameTime();
            String key="hemomancy:charge_visual_tick";
            if(packet.heldTicks>0 && player.getPersistentData().contains(key)
                    && now-player.getPersistentData().getLong(key)<4)return;
            player.getPersistentData().putLong(key,now);
            float progress=ManipulationCastingRules.chargeFraction(packet.heldTicks,selected.getRequiredChargeTicks());
            var from=player.getEyePosition();
            var to=ManipulationCombatHelper.clipToGeometry(player,from.add(player.getLookAngle().scale(8+16*progress)));
            PacketDistributor.sendToPlayersNear(player.serverLevel(),null,from.x,from.y,from.z,80,
                    new ManipulationVisualPacket(form,player.getId(),from,to,progress,packet.heldTicks==0?0:10,1));
        });
    }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
