package com.vincenthuto.hemomancy.common.network.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActRules;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetActs;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** D-37: the Act entry on the Crossbar radial. The selected body's act, or the Pattern's Ring, is fired server side. */
public record PacketPuppetAct(UUID crossbarId) implements CustomPacketPayload {
	public static final Type<PacketPuppetAct> TYPE = new Type<>(Hemomancy.rloc("puppet_act"));
	public static final StreamCodec<FriendlyByteBuf, PacketPuppetAct> STREAM_CODEC =
			StreamCodec.of((buf, msg) -> buf.writeUUID(msg.crossbarId), buf -> new PacketPuppetAct(buf.readUUID()));

	public static void handle(PacketPuppetAct msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || msg.crossbarId == null
					|| !ServerboundGuards.activePlayer(player)
					|| !ServerboundRateLimiter.tryAcquire(player, "puppet_act", PuppetActRules.REQUEST_INTERVAL_TICKS)) {
				return;
			}
			MarionetteCrossbarItem.findEquippedCrossbar(player, msg.crossbarId)
					.ifPresent(crossbar -> PuppetActs.fire(player, crossbar));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
