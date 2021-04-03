package com.huto.hemomancy.network.keybind;

import java.util.UUID;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketBloodAbsorbtionEntity {
	UUID uuid;

	public PacketBloodAbsorbtionEntity() {
	}

	public PacketBloodAbsorbtionEntity(UUID uuid) {
		this.uuid = uuid;
	}

	public static PacketBloodAbsorbtionEntity decode(final PacketBuffer buffer) {
		return new PacketBloodAbsorbtionEntity(buffer.readUniqueId());
	}

	public static void encode(final PacketBloodAbsorbtionEntity msg, final PacketBuffer buffer) {
		buffer.writeUniqueId(msg.uuid);

	}

	public static void handle(final PacketBloodAbsorbtionEntity msg, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			IBloodVolume bloodVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			ServerWorld sWorld = (ServerWorld) ctx.get().getSender().world;
			Entity hitEnt = sWorld.getEntityByUuid(msg.uuid);
			Vector3 playerVec = Vector3.fromEntityCenter(player);
			Vector3 entityVec = Vector3.fromEntityCenter(hitEnt);
			System.out.println(hitEnt);

		});
		ctx.get().setPacketHandled(true);
	}
}