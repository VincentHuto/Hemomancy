package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;

public class AirBloodDrawPacket implements CustomPacketPayload {

	public static final Type<AirBloodDrawPacket> TYPE = new Type<>(Hemomancy.rloc("air_blood_draw_packet"));
	public static final StreamCodec<FriendlyByteBuf, AirBloodDrawPacket> STREAM_CODEC = StreamCodec.of(AirBloodDrawPacket::encode, AirBloodDrawPacket::decode);

	public static AirBloodDrawPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new AirBloodDrawPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final AirBloodDrawPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final AirBloodDrawPacket message, final IPayloadContext ctx) {
//		ctx.enqueueWork(() -> {
//			Player player = ctx.player();
//			if (player == null)
//				return;
//			if (!player.level().isClientSide) {
//				float pTic = message.parTick;
//				ServerLevel sLevel = (ServerLevel) player.level;
//				HitResult airTrace = player.pick(1.5, pTic, false);
//				ItemStack staff = Hemomancy.findItemInPlayerInv(player, ItemLivingStaff.class);
//				IItemHandler handler = staff.getCapability(ForgeCapabilities.ITEM_HANDLER)
//						.orElseThrow(NullPointerException::new);
//				ItemStack selected = handler.getStackInSlot(0);
//				CompoundTag CompoundTag = staff.getOrCreateTag();
//				CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
//				if (items != null) {
//					if (items.contains("Items", 9)) {
//						selected = staff.of(((ListTag) items.get("Items")).getCompound(0));
//						if (selected.getItem() == ItemInit.morphling_pests.get()) {
//							sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(0, 255, 0)),
//									airTrace.getLocation().x, airTrace.getLocation().y, airTrace.getLocation().z, 5, 0,
//									0, 0, 0.015f);
//						} else if (selected.getItem() == ItemInit.morphling_serpent.get()) {
//							sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 20, 0)),
//									airTrace.getLocation().x, airTrace.getLocation().y, airTrace.getLocation().z, 2, 0,
//									0, 0, 0.0015f);
//						} else {
//							sLevel.sendParticles(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
//									airTrace.getLocation().x, airTrace.getLocation().y, airTrace.getLocation().z, 15, 0,
//									0, 0, 0.005f);
//						}
//					}
//
//				}
//			}
//		});
	}

	float parTick;

	public AirBloodDrawPacket() {
	}

	public AirBloodDrawPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
