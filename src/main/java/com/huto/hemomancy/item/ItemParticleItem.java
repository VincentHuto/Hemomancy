package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capabilities.manipulation.IKnownManipulations;
import com.huto.hemomancy.capabilities.manipulation.KnownManipulationProvider;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;
import com.huto.hemomancy.render.item.RenderParticleItem;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemParticleItem extends Item {

	public ItemParticleItem(Properties prop) {
		super(prop.setISTER(() -> RenderParticleItem::new));
		prop.maxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		IKnownManipulations known = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(NullPointerException::new);
		List<BloodManipulation> knownList = known.getKnownManips();

		if (!worldIn.isRemote) {
			if (!playerIn.isSneaking()) {
				knownList.add(ManipulationInit.blood_rush);
				PacketHandler.CHANNELKNOWNMANIPS.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
						new PacketKnownManipulationServer(knownList, known.getSelectedManip()));
				System.out.println(knownList.toString());

			} else {
				knownList.add(ManipulationInit.blood_shot);
				// knownList.clear();
				PacketHandler.CHANNELKNOWNMANIPS.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
						new PacketKnownManipulationServer(knownList,known.getSelectedManip()));
			}
		}

		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Flashy"));
	}

}
