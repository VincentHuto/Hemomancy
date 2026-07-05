package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.List;

public class BloodMemoryItem extends Item {

	DeferredHolder<BloodManipulation, BloodManipulation> manip;

	public BloodMemoryItem(Properties properties, DeferredHolder<BloodManipulation, BloodManipulation> manip) {
		super(properties.stacksTo(1));
		this.manip = manip;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (getManip() != null) {
			tooltip.add(Component.literal(getManip().getProperName()));
			if (isRetiredMemoryItem()) {
				tooltip.add(Component.literal("This memory has gone dormant.")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
				return;
			}
			getManip().getDrudgeAction().ifPresentOrElse(da -> {
				if (da == com.vincenthuto.hemomancy.common.manipulation.DrudgeAction.DRUDGE_UNSUPPORTED) {
					tooltip.add(Component.literal("§cNot usable by Drudges"));
				} else {
					getManip().getDrudgeDescription().ifPresent(desc ->
							tooltip.add(Component.literal("§7Drudge: " + desc)));
				}
			}, () -> { /* no DrudgeAction registered yet — show nothing */ });
		}
	}

	public BloodManipulation getManip() {
		return manip.get();
	}

	public boolean isRetiredMemoryItem() {
		return ManipulationRetirementRules.isRetiredMemoryItem(this, getManip());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(playerIn)
				.orElseThrow(NullPointerException::new);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
				.orElseThrow(NullPointerException::new);
		LinkedHashMap<BloodManipulation, ManipLevel> knownList = known.getKnownManips();

		if (handIn == InteractionHand.MAIN_HAND) {
			ItemStack stack = playerIn.getItemInHand(handIn);
			if (!worldIn.isClientSide) {
				if (volume.isActive()) {
					if (!playerIn.isShiftKeyDown()) {
						BloodManipulation manipulation = getManip();
						if (ManipulationRetirementRules.isRetiredMemoryItem(this, manipulation)) {
							playerIn.displayClientMessage(Component.literal("This memory has gone dormant.")
									.withStyle(ChatFormatting.DARK_GRAY), true);
							return InteractionResultHolder.fail(stack);
						}
						int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(playerIn);
						if (!ManipulationRankGates.playerMeetsRank(playerDegree, manipulation.getRank())) {
							playerIn.displayClientMessage(Component.literal("This memory requires Degree "
											+ ManipulationRankGates.minDegreeForRank(manipulation.getRank()) + ".")
									.withStyle(ChatFormatting.DARK_RED), true);
						} else if (!known.doesListContainName(knownList, manipulation)) {
							knownList.put(manipulation, ManipLevel.BLANK);
							int maxSlots = ManipSlotHelper.getMaxSlots(playerIn);
							boolean fixedMechanical = ManipulationEquipHelper.isFixedMechanicalManip(manipulation.getName());
							boolean hasOpenNormalSlot = ManipulationEquipHelper.countNormalEquippedNames(
									known.getEquippedManipNames()) < maxSlots;
							boolean equipped = fixedMechanical || (hasOpenNormalSlot
									&& known.equipManip(manipulation.getName(), maxSlots));
							if (equipped) {
								playerIn.displayClientMessage(Component.literal("Memorized and equipped: "
												+ manipulation.getProperName())
										.withStyle(ChatFormatting.DARK_RED), true);
							} else {
								playerIn.displayClientMessage(Component.literal(
												"Memory learned. Use a Mnemonic Reliquary to change equipped memories.")
										.withStyle(ChatFormatting.DARK_RED), true);
							}
							PacketHandler.sendToPlayer((ServerPlayer) playerIn, new KnownManipulationServerPacket(known));
							stack.shrink(1);
						} else {
							playerIn.displayClientMessage(Component.literal("Player Already Knowns This Manipulation!")
									.withStyle(ChatFormatting.DARK_RED), true);
						}
					}
				} else {
					playerIn.displayClientMessage(
							Component.literal("You lack understanding of what your even holding...")
									.withStyle(ChatFormatting.DARK_RED),
							true);
				}
			}
		}
		return super.use(worldIn, playerIn, handIn);

	}
}
