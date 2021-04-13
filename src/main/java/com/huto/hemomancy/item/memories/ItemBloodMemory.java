package com.huto.hemomancy.item.memories;

import java.util.List;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.font.ModTextFormatting;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemBloodMemory extends Item {

	public ItemBloodMemory(Properties properties) {
		super(properties.maxStackSize(1));
	}

	public BloodManipulation getManip() {
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDisplayName(ItemStack stack) {
		return new StringTextComponent(ModTextFormatting
				.stringToBloody(ModTextFormatting.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.mergeStyle(TextFormatting.DARK_RED);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (getManip() != null) {
			tooltip.add(new StringTextComponent(getManip().getProperName()));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		IKnownManipulations known = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		if (handIn == Hand.MAIN_HAND) {
			ItemStack stack = playerIn.getHeldItem(handIn);
			if (stack.getItem() instanceof ItemBloodMemory) {
				ItemBloodMemory memory = (ItemBloodMemory) stack.getItem();
				List<BloodManipulation> knownList = known.getKnownManips();
				if (!worldIn.isRemote) {
					if (!playerIn.isSneaking()) {
						knownList.add(memory.getManip());
						PacketHandler.CHANNELKNOWNMANIPS.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
								new PacketKnownManipulationServer(knownList, known.getSelectedManip()));
						stack.shrink(1);
					}
				}
			}
		}

		return super.onItemRightClick(worldIn, playerIn, handIn);

	}

}
