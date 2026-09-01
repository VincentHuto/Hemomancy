package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandParticleEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpawnBrazierItemAbsorptionParticlesPacket(Vec3 source, ItemStack stack)
		implements CustomPacketPayload {
	public static final Type<SpawnBrazierItemAbsorptionParticlesPacket> TYPE =
			new Type<>(Hemomancy.rloc("spawn_brazier_item_absorption_particles"));
	public static final StreamCodec<FriendlyByteBuf, SpawnBrazierItemAbsorptionParticlesPacket> STREAM_CODEC =
			StreamCodec.of(SpawnBrazierItemAbsorptionParticlesPacket::encode,
					SpawnBrazierItemAbsorptionParticlesPacket::decode);

	public SpawnBrazierItemAbsorptionParticlesPacket {
		stack = stack.copy();
	}

	public static void encode(FriendlyByteBuf buf, SpawnBrazierItemAbsorptionParticlesPacket packet) {
		buf.writeDouble(packet.source.x);
		buf.writeDouble(packet.source.y);
		buf.writeDouble(packet.source.z);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, packet.stack);
	}

	public static SpawnBrazierItemAbsorptionParticlesPacket decode(FriendlyByteBuf buf) {
		Vec3 source = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new SpawnBrazierItemAbsorptionParticlesPacket(source, stack);
	}

	public static void handle(SpawnBrazierItemAbsorptionParticlesPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> CellHandParticleEffects.spawnBrazierItemAbsorptionParticles(
				packet.stack, packet.source));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
