package com.vincenthuto.hemomancy.network.particle;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketAirBloodDraw {

	float parTick;

	public PacketAirBloodDraw() {
	}

	public PacketAirBloodDraw(float par) {
		this.parTick = par;
	}

	public static PacketAirBloodDraw decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketAirBloodDraw(buffer.readFloat());
	}

	public static void encode(final PacketAirBloodDraw message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final PacketAirBloodDraw message, final Supplier<NetworkEvent.Context> ctx) {
//		ctx.get().enqueueWork(() -> {
//			Player player = ctx.get().getSender();
//			if (player == null)
//				return;
//			if (!player.level.isClientSide) {
//				float pTic = message.parTick;
//				ServerLevel sLevel = (ServerLevel) player.level;
//				HitResult airTrace = player.pick(1.5, pTic, false);
//				ItemStack staff = Hemomancy.findItemInPlayerInv(player, ItemLivingStaff.class);
//				IItemHandler handler = staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
		ctx.get().setPacketHandled(true);
	}

}