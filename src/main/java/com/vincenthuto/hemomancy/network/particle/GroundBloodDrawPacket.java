package com.vincenthuto.hemomancy.network.particle;

import java.util.Arrays;
import java.util.function.Supplier;

import com.mojang.math.Vector3d;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.BitLocation;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkEvent;

public class GroundBloodDrawPacket {

	float parTick;

	public GroundBloodDrawPacket() {
	}

	public GroundBloodDrawPacket(float par) {
		this.parTick = par;
	}

	public static GroundBloodDrawPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new GroundBloodDrawPacket(buffer.readFloat());
	}

	public static void encode(final GroundBloodDrawPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final GroundBloodDrawPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level.isClientSide) {
				float pTic = message.parTick;
				if (player.getMainHandItem().getItem() == ItemInit.living_staff.get()) {
					IBloodVolume bloodVol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					ServerLevel sLevel = (ServerLevel) player.level;
					HitResult trace = player.pick(6, pTic, false);

					if (trace != null) {
						if (trace.getType() == HitResult.Type.BLOCK) {
							BlockHitResult bHit = (BlockHitResult) trace;
							double x = bHit.getBlockPos().getX();
							double y = bHit.getBlockPos().getY();
							double z = bHit.getBlockPos().getZ();
							Direction side = bHit.getDirection();
							final BitLocation loc = new BitLocation(bHit);
							Vector3d truePos = new Vector3d((loc.getBitX() + 1.0) / 16.0, (loc.getBitY() + 1.0) / 16.0,
									(loc.getBitZ() + 1.0) / 16.0);
							if (player.getOffhandItem().getItem() == BlockInit.smouldering_ash_trail.get().asItem()) {
								if (sLevel.getBlockState(bHit.getBlockPos().offset(0, 1, 0)).getBlock() == Blocks.AIR
										&& sLevel.getBlockState(bHit.getBlockPos())
												.getBlock() != BlockInit.smouldering_ash_trail.get()
										&& sLevel.getBlockState(bHit.getBlockPos())
												.getBlock() != BlockInit.befouling_ash_trail.get()) {
									sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
											x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
											z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
									sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
											x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
											z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
									sLevel.setBlockAndUpdate(bHit.getBlockPos().offset(0, 1, 0),
											BlockInit.smouldering_ash_trail.get().defaultBlockState());
									player.getOffhandItem().shrink(1);
								}
							}

							if (sLevel.getBlockState(bHit.getBlockPos()).getBlock() == BlockInit.smouldering_ash_trail
									.get()) {
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.setBlockAndUpdate(bHit.getBlockPos(),
										BlockInit.active_smouldering_ash_trail.get().defaultBlockState());
								bloodVol.drain(25);

							}
							if (sLevel.getBlockState(bHit.getBlockPos())
									.getBlock() == BlockInit.active_smouldering_ash_trail.get()) {
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);

							}

							if (player.getOffhandItem().getItem() == BlockInit.befouling_ash_trail.get().asItem()) {
								if (sLevel.getBlockState(bHit.getBlockPos().offset(0, 1, 0)).getBlock() == Blocks.AIR
										&& sLevel.getBlockState(bHit.getBlockPos())
												.getBlock() != BlockInit.befouling_ash_trail.get()
										&& sLevel.getBlockState(bHit.getBlockPos())
												.getBlock() != BlockInit.smouldering_ash_trail.get()) {
									sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
											x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
											z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
									sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
											x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
											z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
									sLevel.setBlockAndUpdate(bHit.getBlockPos().offset(0, 1, 0),
											BlockInit.befouling_ash_trail.get().defaultBlockState());
									player.getOffhandItem().shrink(1);
								}
							}
							if (sLevel.getBlockState(bHit.getBlockPos()).getBlock() == BlockInit.befouling_ash_trail
									.get()) {
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.setBlockAndUpdate(bHit.getBlockPos(),
										BlockInit.active_befouling_ash_trail.get().defaultBlockState());
								bloodVol.drain(25);
							}
							if (sLevel.getBlockState(bHit.getBlockPos())
									.getBlock() == BlockInit.active_befouling_ash_trail.get()) {
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y - 1,
										z + side.getStepZ() + truePos.z, 3, 0, 0, 0, 0.005f);
							}

							Block[] bannedBlocks = { BlockInit.befouling_ash_trail.get(),
									BlockInit.smouldering_ash_trail.get(), BlockInit.active_befouling_ash_trail.get(),
									BlockInit.active_smouldering_ash_trail.get() };
							if (!Arrays.asList(bannedBlocks)
									.contains(sLevel.getBlockState(bHit.getBlockPos()).getBlock())) {
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(0, 0, 255)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(0, 255, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 0, 255)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 255, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(0, 255, 255)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 1, 0, 0, 0, 0.005f);
								sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(0, 0, 0)),
										x + side.getStepX() + truePos.x, y + side.getStepY() + truePos.y,
										z + side.getStepZ() + truePos.z, 15, 0, 0, 0, 0.005f);
							}

						}
					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}

}