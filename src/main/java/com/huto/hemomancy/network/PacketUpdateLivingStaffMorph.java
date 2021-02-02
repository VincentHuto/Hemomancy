package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.huto.hemomancy.item.tool.ItemLivingStaff;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PacketUpdateLivingStaffMorph {
	private int selected;

	public PacketUpdateLivingStaffMorph(int selectedIn) {
		this.selected = selectedIn;

	}

	public static void encode(PacketUpdateLivingStaffMorph msg, PacketBuffer buf) {
		buf.writeInt(msg.selected);
	}

	public static PacketUpdateLivingStaffMorph decode(PacketBuffer buf) {
		return new PacketUpdateLivingStaffMorph(buf.readInt());
	}

	public static class Handler {
		public static void handle(final PacketUpdateLivingStaffMorph msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ItemStack staff = Hemomancy.findLivingStaff(ctx.get().getSender());
				ItemStack jar = Hemomancy.findMorphlingJar(ctx.get().getSender());
				if (staff.getItem() instanceof ItemLivingStaff) {
					if (jar.getItem() instanceof ItemMorphlingJar) {
						IItemHandler staffHandler = staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						IItemHandler jarHandler = jar.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						System.out.println("Selected Slot" + msg.selected);

						if (jarHandler instanceof MorphlingJarItemHandler
								&& staffHandler instanceof LivingStaffItemHandler) {
							System.out.println("IS WORKING");
							MorphlingJarItemHandler castedJar = (MorphlingJarItemHandler) jarHandler;
							System.out.println(castedJar.toString());

							LivingStaffItemHandler castedStaff = (LivingStaffItemHandler) staffHandler;
						//	ItemStack originalFocus = castedStaff.getStackInSlot(0).copy();
							ItemStack inJar = castedJar.getStackInSlot(msg.selected).copy();
							System.out.println(inJar);
							castedStaff.extractItem(0, 1, false);
							//castedJar.extractItem(msg.selected, 1, false);
							castedStaff.setStackInSlot(0, inJar);
							//castedJar.setStackInSlot(msg.selected, originalFocus);
						}

					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}