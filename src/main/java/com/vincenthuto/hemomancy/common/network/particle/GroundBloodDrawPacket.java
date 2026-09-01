package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.EngramBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.EngramTextureCache;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionVisuals;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.BitLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

import java.util.Arrays;

public class GroundBloodDrawPacket implements CustomPacketPayload {

	public static final Type<GroundBloodDrawPacket> TYPE = new Type<>(Hemomancy.rloc("ground_blood_draw_packet"));
	public static final StreamCodec<FriendlyByteBuf, GroundBloodDrawPacket> STREAM_CODEC = StreamCodec.of(GroundBloodDrawPacket::encode, GroundBloodDrawPacket::decode);

	public static GroundBloodDrawPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new GroundBloodDrawPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final GroundBloodDrawPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final GroundBloodDrawPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;
				if (player.getMainHandItem().getItem() == ItemInit.living_staff.get()) {
					IBloodVolume bloodVol = HemoCapabilityAccess.getBloodVolume(player)
							.orElseThrow(NullPointerException::new);
					ServerLevel sLevel = (ServerLevel) player.level();
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
									BlockInit.active_smouldering_ash_trail.get(), BlockInit.engram_block.get() };

							// Engram particle projection: spawn a particle at each non-transparent pixel
							BlockState hitState = sLevel.getBlockState(bHit.getBlockPos());
							if (hitState.getBlock() instanceof EngramBlock) {
								int charIndex = hitState.getValue(EngramBlock.CHARACTERINDEX);
								EngramTextureCache.loadAll(); // Lazy-load if not yet cached
								boolean[][] pixels = EngramTextureCache.getPixels(charIndex);
								int[][] colors = EngramTextureCache.getColors(charIndex);
								if (pixels != null && colors != null) {
									DiscoveryInscriptionVisuals.Face face = toVisualFace(hitState.getValue(EngramBlock.FACING));
									for (int px = 0; px < 16; px++) {
										for (int pz = 0; pz < 16; pz++) {
											if (pixels[px][pz]) {
												DiscoveryInscriptionVisuals.PixelCenter center =
														DiscoveryInscriptionVisuals.particleCenter(face, px, pz);
												double particleX = x + center.x();
												double particleY = y + center.y();
												double particleZ = z + center.z();

												int argb = colors[px][pz];
												int r = (argb >> 16) & 0xFF;
												int g = (argb >> 8) & 0xFF;
												int b = argb & 0xFF;

												sLevel.sendParticles(
														GlowParticleFactory.createData(new ParticleColor(r, g, b)),
														particleX, particleY, particleZ, 1, 0, 0, 0, 0);
											}
										}
									}
								}
							}
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
	}

	float parTick;

	public GroundBloodDrawPacket() {
	}

	public GroundBloodDrawPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	private static DiscoveryInscriptionVisuals.Face toVisualFace(Direction facing) {
		return switch (facing) {
			case NORTH -> DiscoveryInscriptionVisuals.Face.NORTH;
			case SOUTH -> DiscoveryInscriptionVisuals.Face.SOUTH;
			case EAST -> DiscoveryInscriptionVisuals.Face.EAST;
			case WEST -> DiscoveryInscriptionVisuals.Face.WEST;
			default -> DiscoveryInscriptionVisuals.Face.UP;
		};
	}
}
