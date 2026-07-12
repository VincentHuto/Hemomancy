package com.vincenthuto.hemomancy.common.network.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCommandMode;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCrossbarCommands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

public record PacketCrossbarRadialAction(UUID crossbarId, Action action, String value)
		implements CustomPacketPayload {
	public static final int MAX_VALUE_LENGTH = 64;
	public static final Type<PacketCrossbarRadialAction> TYPE =
			new Type<>(Hemomancy.rloc("crossbar_radial_action"));
	public static final StreamCodec<FriendlyByteBuf, PacketCrossbarRadialAction> STREAM_CODEC =
			StreamCodec.of(PacketCrossbarRadialAction::encode, PacketCrossbarRadialAction::decode);

	public enum Action {
		SET_MODE,
		HOT_SWAP
	}

	private static void encode(FriendlyByteBuf buf, PacketCrossbarRadialAction msg) {
		buf.writeUUID(msg.crossbarId);
		buf.writeEnum(msg.action);
		buf.writeUtf(msg.value == null ? "" : msg.value, MAX_VALUE_LENGTH);
	}

	private static PacketCrossbarRadialAction decode(FriendlyByteBuf buf) {
		return new PacketCrossbarRadialAction(buf.readUUID(), buf.readEnum(Action.class),
				buf.readUtf(MAX_VALUE_LENGTH));
	}

	public static void handle(PacketCrossbarRadialAction msg, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || msg.crossbarId == null
					|| msg.action == null || msg.value == null || msg.value.isBlank()
					|| msg.value.length() > MAX_VALUE_LENGTH) {
				return;
			}
			Optional<ItemStack> equipped = MarionetteCrossbarItem.findEquippedCrossbar(player, msg.crossbarId);
			if (equipped.isEmpty()) {
				return;
			}
			switch (msg.action) {
				case SET_MODE -> PuppeteerCommandMode.tryParse(msg.value)
						.ifPresent(mode -> PuppeteerCrossbarCommands.setMode(player, equipped.get(), mode));
				case HOT_SWAP -> PuppeteerCrossbarCommands.hotSwap(player, equipped.get(), msg.value);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
