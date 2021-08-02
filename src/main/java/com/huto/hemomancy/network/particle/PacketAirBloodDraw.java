package com.huto.hemomancy.network.particle;

import java.util.function.Supplier;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.tool.living.ItemLivingStaff;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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

	@SuppressWarnings("static-access")
	public static void handle(final PacketAirBloodDraw message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level.isClientSide) {
				float pTic = message.parTick;
				ServerWorld sWorld = (ServerWorld) player.world;
				RayTraceResult airTrace = player.pick(1.5, pTic, false);
				ItemStack staff = Hemomancy.findItemInPlayerInv(player, ItemLivingStaff.class);
				IItemHandler handler = staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
						.orElseThrow(NullPointerException::new);
				ItemStack selected = handler.getStackInSlot(0);
				CompoundNBT compoundnbt = staff.getOrCreateTag();
				CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						selected = staff.read(((ListNBT) items.get("Items")).getCompound(0));
						if (selected.getItem() == ItemInit.morphling_pests.get()) {
							sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(0, 255, 0)),
									airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 5, 0, 0, 0,
									0.015f);
						} else if (selected.getItem() == ItemInit.morphling_serpent.get()) {
							sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(255, 20, 0)),
									airTrace.getHitVec().x, airTrace.getHitVec().y, airTrace.getHitVec().z, 2, 0, 0, 0,
									0.0015f);
						} else {
							sWorld.spawnParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
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