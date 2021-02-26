package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.BloodCellParticleData;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.huto.hemomancy.particle.data.SerpentParticleData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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

	@SuppressWarnings("static-access")
	public static void handle(final PacketAirBloodDraw message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				float pTic = message.parTick;
				ServerWorld sWorld = (ServerWorld) player.world;
				RayTraceResult airTrace = player.pick(1.5, pTic, false);
				ItemStack staff = Hemomancy.findLivingStaff(player);
				IItemHandler handler = staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseThrow(NullPointerException::new);
				ItemStack selected = handler.getStackInSlot(0);
				CompoundNBT compoundnbt = staff.getOrCreateTag();
				CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						selected = staff.read(((ListNBT) items.get("Items")).getCompound(0));
						if (selected.getItem() == ItemInit.morphling_pests.get()) {
							sWorld.spawnParticle(GlowParticleData.createData(new ParticleColor(0, 255, 0)),
									airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 5, 0, 0, 0,
									0.015f);
						} else if (selected.getItem() == ItemInit.morphling_serpent.get()) {
							sWorld.spawnParticle(SerpentParticleData.createData(new ParticleColor(255, 20, 0)),
									airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 2, 0, 0, 0,
									0.0015f);
						} else {
							sWorld.spawnParticle(BloodCellParticleData.createData(new ParticleColor(255, 0, 0)),
									airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 15, 0, 0, 0,
									0.005f);
						}
					}

				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

}