package com.vincenthuto.hemomancy.common.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class SpawnMonolithShatterBurstPacket {

	public static SpawnMonolithShatterBurstPacket decode(FriendlyByteBuf buf) {
		SpawnMonolithShatterBurstPacket msg = new SpawnMonolithShatterBurstPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(SpawnMonolithShatterBurstPacket msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
	}

	public static void handle(SpawnMonolithShatterBurstPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientLevel = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientLevel.isPresent()) {
			return;
		}
		ClientLevel world = ((ClientLevel) clientLevel.get());
		if (world != null) {
			SanguineMonolithShatterRenderer.spawnBurst(msg.getPos(), world.random);
		}
		ctx.setPacketHandled(true);
	}

	private Vec3 pos;

	public SpawnMonolithShatterBurstPacket() {
	}

	public SpawnMonolithShatterBurstPacket(Vec3 pos) {
		this.pos = pos;
	}

	public Vec3 getPos() {
		return pos;
	}
}
