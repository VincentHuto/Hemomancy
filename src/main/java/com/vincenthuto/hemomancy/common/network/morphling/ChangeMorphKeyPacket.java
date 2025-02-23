package com.vincenthuto.hemomancy.common.network.morphling;

import java.util.Random;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

public class ChangeMorphKeyPacket {

	public static class Handler {
		public static void handle(final ChangeMorphKeyPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ItemStack staff = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), LivingStaffItem.class);
				ItemStack jar = Hemomancy.findItemInPlayerInv(ctx.get().getSender(), ItemMorphlingJar.class);
				if (staff.getItem() instanceof LivingStaffItem) {
					if (jar.getItem() instanceof ItemMorphlingJar) {
						IItemHandler staffHandler = staff.getCapability(ForgeCapabilities.ITEM_HANDLER)
								.orElseThrow(NullPointerException::new);
						IItemHandler jarHandler = jar.getCapability(ForgeCapabilities.ITEM_HANDLER)
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
									Random rand = new Random();
									int randSlot = rand.nextInt(4);
									if (items.contains("Items", 9)) {
										@SuppressWarnings("static-access")
										ItemStack selectedStack = ItemStack
												.of(((ListTag) items.get("Items")).getCompound(randSlot));
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
													castedJar.setStackInSlot(randSlot, selectedStaffStack);
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

	public static ChangeMorphKeyPacket decode(FriendlyByteBuf buf) {
		return new ChangeMorphKeyPacket();
	}

	public static void encode(ChangeMorphKeyPacket msg, FriendlyByteBuf buf) {
	}

	public ChangeMorphKeyPacket() {

	}
}