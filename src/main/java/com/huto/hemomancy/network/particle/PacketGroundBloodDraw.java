package com.huto.hemomancy.network.particle;

import java.util.Arrays;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.math.BitLocation;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketGroundBloodDraw {

	float parTick;

	public PacketGroundBloodDraw() {
	}

	public PacketGroundBloodDraw(float par) {
		this.parTick = par;
	}

	public static PacketGroundBloodDraw decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketGroundBloodDraw(buffer.readFloat());
	}

	public static void encode(final PacketGroundBloodDraw message, final PacketBuffer buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final PacketGroundBloodDraw message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				if (player.getHeldItemMainhand().getItem() == ItemInit.living_staff.get()) {
					IBloodVolume bloodVol = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					ServerWorld sWorld = (ServerWorld) player.world;
					RayTraceResult trace = player.pick(6, pTic, false);

					if (trace != null) {
						if (trace.getType() == RayTraceResult.Type.BLOCK) {
							BlockRayTraceResult bHit = (BlockRayTraceResult) trace;
							double x = bHit.getPos().getX();
							double y = bHit.getPos().getY();
							double z = bHit.getPos().getZ();
							Direction side = bHit.getFace();
							final BitLocation loc = new BitLocation(bHit);
							Vector3d truePos = new Vector3d((loc.getBitX() + 1.0) / 16.0, (loc.getBitY() + 1.0) / 16.0,
									(loc.getBitZ() + 1.0) / 16.0);
							if (player.getHeldItemOffhand().getItem() == BlockInit.smouldering_ash_trail.get()
									.asItem()) {
								if (sWorld.getBlockState(bHit.getPos().add(0, 1, 0)).getBlock() == Blocks.AIR
										&& sWorld.getBlockState(bHit.getPos())
												.getBlock() != BlockInit.smouldering_ash_trail.get()
										&& sWorld.getBlockState(bHit.getPos())
												.getBlock() != BlockInit.befouling_ash_trail.get()) {
									sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
											x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
											z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
									sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
											x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
											z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);
									sWorld.setBlockState(bHit.getPos().add(0, 1, 0),
											BlockInit.smouldering_ash_trail.get().getDefaultState());
									player.getHeldItemOffhand().shrink(1);
								}
							}

							if (sWorld.getBlockState(bHit.getPos()).getBlock() == BlockInit.smouldering_ash_trail
									.get()) {
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);
								sWorld.setBlockState(bHit.getPos(),
										BlockInit.active_smouldering_ash_trail.get().getDefaultState());
								bloodVol.subtractBloodVolume(25);

							}
							if (sWorld.getBlockState(bHit.getPos()).getBlock() == BlockInit.active_smouldering_ash_trail
									.get()) {
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 100, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);

							}

							if (player.getHeldItemOffhand().getItem() == BlockInit.befouling_ash_trail.get().asItem()) {
								if (sWorld.getBlockState(bHit.getPos().add(0, 1, 0)).getBlock() == Blocks.AIR
										&& sWorld.getBlockState(bHit.getPos())
												.getBlock() != BlockInit.befouling_ash_trail.get()
										&& sWorld.getBlockState(bHit.getPos())
												.getBlock() != BlockInit.smouldering_ash_trail.get()) {
									sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
											x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
											z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
									sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
											x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
											z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);
									sWorld.setBlockState(bHit.getPos().add(0, 1, 0),
											BlockInit.befouling_ash_trail.get().getDefaultState());
									player.getHeldItemOffhand().shrink(1);
								}
							}
							if (sWorld.getBlockState(bHit.getPos()).getBlock() == BlockInit.befouling_ash_trail.get()) {
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);
								sWorld.setBlockState(bHit.getPos(),
										BlockInit.active_befouling_ash_trail.get().getDefaultState());
								bloodVol.subtractBloodVolume(25);
							}
							if (sWorld.getBlockState(bHit.getPos()).getBlock() == BlockInit.active_befouling_ash_trail
									.get()) {
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 255, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.005f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y - 1,
										z + side.getZOffset() + truePos.z, 3, 0, 0, 0, 0.015f);
							}

							Block[] bannedBlocks = { BlockInit.befouling_ash_trail.get(),
									BlockInit.smouldering_ash_trail.get(), BlockInit.active_befouling_ash_trail.get(),
									BlockInit.active_smouldering_ash_trail.get() };
							if (!Arrays.asList(bannedBlocks).contains(sWorld.getBlockState(bHit.getPos()).getBlock())) {
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(0, 0, 255)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(0, 255, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 0, 255)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255, 255, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(0, 255, 255)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 1, 0, 0, 0, 0.015f);
								sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(0, 0, 0)),
										x + side.getXOffset() + truePos.x, y + side.getYOffset() + truePos.y,
										z + side.getZOffset() + truePos.z, 15, 0, 0, 0, 0.015f);
							}

						}
					}
				}
			}

		});
		ctx.get().setPacketHandled(true);
	}

}