package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncEquipmentLayerVisibilityPacket implements CustomPacketPayload {
	public static final Type<SyncEquipmentLayerVisibilityPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_equipment_layer_visibility"));
	public static final StreamCodec<FriendlyByteBuf, SyncEquipmentLayerVisibilityPacket> STREAM_CODEC =
			StreamCodec.of(SyncEquipmentLayerVisibilityPacket::encode, SyncEquipmentLayerVisibilityPacket::decode);

	private final int playerId;
	private final boolean fungalVisible;
	private final boolean jarVisible;
	private final boolean charmVisible;
	private final boolean gourdVisible;

	public SyncEquipmentLayerVisibilityPacket(int playerId, boolean fungalVisible, boolean jarVisible, boolean charmVisible,
			boolean gourdVisible) {
		this.playerId = playerId;
		this.fungalVisible = fungalVisible;
		this.jarVisible = jarVisible;
		this.charmVisible = charmVisible;
		this.gourdVisible = gourdVisible;
	}

	public static void encode(FriendlyByteBuf buf, SyncEquipmentLayerVisibilityPacket msg) {
		buf.writeVarInt(msg.playerId);
		buf.writeBoolean(msg.fungalVisible);
		buf.writeBoolean(msg.jarVisible);
		buf.writeBoolean(msg.charmVisible);
		buf.writeBoolean(msg.gourdVisible);
	}

	public static SyncEquipmentLayerVisibilityPacket decode(FriendlyByteBuf buf) {
		return new SyncEquipmentLayerVisibilityPacket(buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean());
	}

	public static void handle(final SyncEquipmentLayerVisibilityPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}

			Entity entity = mc.level.getEntity(msg.playerId);
			if (entity instanceof Player player) {
				HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
					equipment.setRenderLayerVisible(IScars.FUNGAL_SLOT, msg.fungalVisible);
					equipment.setRenderLayerVisible(HarbingerEquipmentMenu.JAR_SLOT_INDEX, msg.jarVisible);
					equipment.setRenderLayerVisible(HarbingerEquipmentMenu.CHARM_SLOT_INDEX, msg.charmVisible);
					equipment.setRenderLayerVisible(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, msg.gourdVisible);
				});
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
