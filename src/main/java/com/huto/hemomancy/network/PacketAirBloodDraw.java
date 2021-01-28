package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.GlowParticleData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAirBloodDraw {

	float parTick;

	public PacketAirBloodDraw() {
	}

	public PacketAirBloodDraw(float par) {
		this.parTick = par;
	}

	public static PacketAirBloodDraw decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketAirBloodDraw(buffer.readFloat());
	}

	public static void encode(final PacketAirBloodDraw message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final PacketAirBloodDraw message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				ServerWorld sWorld = (ServerWorld) player.world;
				if (player.getHeldItemMainhand().getItem() == ItemInit.living_staff.get()) {
					if (player.isHandActive()) {
						RayTraceResult airTrace = player.pick(1.5, pTic, false);
						sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(255, 0, 0)),
								airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 5, 0, 0, 0,
								0.015f);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}