package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.morphlings.IMorphling;
import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.huto.hemomancy.item.tool.living.ItemLivingStaff;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
				ItemStack staff = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), ItemLivingStaff.class);
				ItemStack jar = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), ItemMorphlingJar.class);
				if (staff.getItem() instanceof ItemLivingStaff) {
					if (jar.getItem() instanceof ItemMorphlingJar) {
						IItemHandler staffHandler = staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						IItemHandler jarHandler = jar.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						if (staffHandler instanceof LivingStaffItemHandler) {
							LivingStaffItemHandler castedStaff = (LivingStaffItemHandler) staffHandler;
							if (jarHandler instanceof MorphlingJarItemHandler) {
								MorphlingJarItemHandler castedJar = (MorphlingJarItemHandler) jarHandler;
								castedJar.setDirty();
								castedStaff.setDirty();
								CompoundNBT compoundnbt = jar.getOrCreateTag();
								CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
								if (items != null) {
									if (items.contains("Items", 9)) {
										@SuppressWarnings("static-access")
										ItemStack selectedStack = jar
												.read(((ListNBT) items.get("Items")).getCompound(msg.selected));
										if (selectedStack.getItem() instanceof IMorphling) {
											CompoundNBT staffnbt = staff.getOrCreateTag();
											CompoundNBT staffItems = (CompoundNBT) staffnbt.get("Inventory");
											if (staffItems != null) {
												if (staffItems.contains("Items", 9)) {
													@SuppressWarnings("static-access")
													ItemStack selectedStaffStack = staff
															.read(((ListNBT) staffItems.get("Items")).getCompound(0));
													castedJar.setDirty();
													castedStaff.setDirty();
													castedJar.setStackInSlot(msg.selected, selectedStaffStack);
													castedJar.setDirty();
													castedStaff.setStackInSlot(0, selectedStack);
													castedStaff.setDirty();
												}

											}
										}

									}
								}
							}
						}

					}
				}
				/*
				 * if (jarHandler instanceof MorphlingJarItemHandler && staffHandler instanceof
				 * LivingStaffItemHandler) { System.out.println("IS WORKING");
				 * MorphlingJarItemHandler castedJar = (MorphlingJarItemHandler) jarHandler;
				 * System.out.println(castedJar.toString());
				 * 
				 * LivingStaffItemHandler castedStaff = (LivingStaffItemHandler) staffHandler;
				 * // ItemStack originalFocus = castedStaff.getStackInSlot(0).copy(); ItemStack
				 * inJar = castedJar.getStackInSlot(msg.selected).copy(); if (inJar.getItem() !=
				 * Items.AIR) { castedStaff.extractItem(0, 1, false); //
				 * castedJar.extractItem(msg.selected, 1, false); castedStaff.setStackInSlot(0,
				 * inJar); // castedJar.setStackInSlot(msg.selected, originalFocus); } }
				 */

			});
			ctx.get().setPacketHandled(true);
		}
	}
}