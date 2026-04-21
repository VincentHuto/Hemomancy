package com.vincenthuto.hemomancy.common.network.capa.visceral;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.screen.tile.functional.VisceralMirrorScreen;
import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server → Client packet that tells the client to open the Visceral Mirror
 * screen. Carries all the organ data the screen needs so we don't have to
 * rely on an organ-capability sync channel.
 */
public class OpenVisceralMirrorPacket {

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

	public static void encode(OpenVisceralMirrorPacket msg, FriendlyByteBuf buf) {
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

	public static void handle(OpenVisceralMirrorPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				VisceralMirrorScreen.open(
						msg.pos, msg.organLevels, msg.hasEcho,
						msg.bloodVolume, msg.maxBloodVolume, msg.degree);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
