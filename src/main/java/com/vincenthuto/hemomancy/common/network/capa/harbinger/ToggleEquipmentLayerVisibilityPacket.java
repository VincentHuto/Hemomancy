package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleEquipmentLayerVisibilityPacket implements CustomPacketPayload {
	public static final Type<ToggleEquipmentLayerVisibilityPacket> TYPE =
			new Type<>(Hemomancy.rloc("toggle_equipment_layer_visibility"));
	public static final StreamCodec<FriendlyByteBuf, ToggleEquipmentLayerVisibilityPacket> STREAM_CODEC =
			StreamCodec.of(ToggleEquipmentLayerVisibilityPacket::encode, ToggleEquipmentLayerVisibilityPacket::decode);

	private final int slot;

	public ToggleEquipmentLayerVisibilityPacket(int slot) {
		this.slot = slot;
	}

	public static void encode(FriendlyByteBuf buf, ToggleEquipmentLayerVisibilityPacket msg) {
		buf.writeVarInt(msg.slot);
	}

	public static ToggleEquipmentLayerVisibilityPacket decode(FriendlyByteBuf buf) {
		return new ToggleEquipmentLayerVisibilityPacket(buf.readVarInt());
	}

	public static void handle(final ToggleEquipmentLayerVisibilityPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player) || !isToggleableSlot(msg.slot)) {
				return;
			}

			HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
				equipment.setRenderLayerVisible(msg.slot, !equipment.isRenderLayerVisible(msg.slot));
				HarbingerEquipmentEntityEventHandler.syncLayerVisibilityToClient(player);
			});
		});
	}

	private static boolean isToggleableSlot(int slot) {
		return slot == HarbingerEquipmentMenu.JAR_SLOT_INDEX
				|| slot == HarbingerEquipmentMenu.CHARM_SLOT_INDEX
				|| slot == HarbingerEquipmentMenu.GOURD_SLOT_INDEX;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
