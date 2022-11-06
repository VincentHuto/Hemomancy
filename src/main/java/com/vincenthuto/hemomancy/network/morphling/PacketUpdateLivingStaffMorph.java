package com.vincenthuto.hemomancy.network.morphling;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.item.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.itemhandler.MorphlingJarItemHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateLivingStaffMorph {
	public static class Handler {
		public static void handle(final PacketUpdateLivingStaffMorph msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ItemStack staff = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), LivingStaffItem.class);
				ItemStack jar = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), ItemMorphlingJar.class);
				if (staff.getItem() instanceof LivingStaffItem) {
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
								CompoundTag CompoundTag = jar.getOrCreateTag();
								CompoundTag items = (CompoundTag) CompoundTag.get("Inventory");
								if (items != null) {
									if (items.contains("Items", 9)) {
										@SuppressWarnings("static-access")
										ItemStack selectedStack = ItemStack
												.of(((ListTag) items.get("Items")).getCompound(msg.selected));
										if (selectedStack.getItem() instanceof IMorphling) {
											CompoundTag staffnbt = staff.getOrCreateTag();
											CompoundTag staffItems = (CompoundTag) staffnbt.get("Inventory");
											if (staffItems != null) {
												if (staffItems.contains("Items", 9)) {
													@SuppressWarnings("static-access")
													ItemStack selectedStaffStack = ItemStack
															.of(((ListTag) staffItems.get("Items")).getCompound(0));
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

	public static PacketUpdateLivingStaffMorph decode(FriendlyByteBuf buf) {
		return new PacketUpdateLivingStaffMorph(buf.readInt());
	}

	public static void encode(PacketUpdateLivingStaffMorph msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.selected);
	}

	private int selected;

	public PacketUpdateLivingStaffMorph(int selectedIn) {
		this.selected = selectedIn;

	}
}