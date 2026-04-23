package com.vincenthuto.hemomancy.common.network.capa.visceral;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.screen.tile.functional.VisceralMirrorScreen;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client packet that tells the client to open the Visceral Mirror
 * screen. Carries all the organ data the screen needs so we don't have to
 * rely on an organ-capability sync channel.
 */
public class OpenVisceralMirrorPacket implements CustomPacketPayload {

	public static final Type<OpenVisceralMirrorPacket> TYPE = new Type<>(Hemomancy.rloc("open_visceral_mirror_packet"));
	public static final StreamCodec<FriendlyByteBuf, OpenVisceralMirrorPacket> STREAM_CODEC = StreamCodec.of(OpenVisceralMirrorPacket::encode, OpenVisceralMirrorPacket::decode);

	private final BlockPos pos;
	private final int[] organLevels;
	private final boolean[] hasEcho;
	private final double bloodVolume;
	private final double maxBloodVolume;
	private final int degree;

	public OpenVisceralMirrorPacket(BlockPos pos, int[] organLevels, boolean[] hasEcho,
			double bloodVolume, double maxBloodVolume, int degree) {
		this.pos = pos;
		this.organLevels = organLevels;
		this.hasEcho = hasEcho;
		this.bloodVolume = bloodVolume;
		this.maxBloodVolume = maxBloodVolume;
		this.degree = degree;
	}

	public static void encode(FriendlyByteBuf buf, OpenVisceralMirrorPacket msg) {
		buf.writeBlockPos(msg.pos);
		EnumOrgan[] organs = EnumOrgan.values();
		for (int i = 0; i < organs.length; i++) {
			buf.writeInt(msg.organLevels[i]);
			buf.writeBoolean(msg.hasEcho[i]);
		}
		buf.writeDouble(msg.bloodVolume);
		buf.writeDouble(msg.maxBloodVolume);
		buf.writeInt(msg.degree);
	}

	public static OpenVisceralMirrorPacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		EnumOrgan[] organs = EnumOrgan.values();
		int[] organLevels = new int[organs.length];
		boolean[] hasEcho = new boolean[organs.length];
		for (int i = 0; i < organs.length; i++) {
			organLevels[i] = buf.readInt();
			hasEcho[i] = buf.readBoolean();
		}
		double bloodVolume = buf.readDouble();
		double maxBloodVolume = buf.readDouble();
		int degree = buf.readInt();
		return new OpenVisceralMirrorPacket(pos, organLevels, hasEcho, bloodVolume, maxBloodVolume, degree);
	}

	public static void handle(final OpenVisceralMirrorPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				VisceralMirrorScreen.open(
						msg.pos, msg.organLevels, msg.hasEcho,
						msg.bloodVolume, msg.maxBloodVolume, msg.degree);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
