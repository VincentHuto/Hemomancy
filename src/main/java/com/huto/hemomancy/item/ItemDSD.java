package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.gui.manips.GuiChooseManip;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemDSD extends Item {

	public ItemDSD(Properties prop) {
		super(prop);
		prop.maxStackSize(1);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!worldIn.isRemote) {
			IKnownManipulations manips = playerIn.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			BloodManipulation selected = manips.getSelectedManip();
			List<BloodManipulation> known = manips.getKnownManips();
			PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
					new PacketKnownManipulationServer(known, selected));
		} else
			Minecraft.getInstance().displayGuiScreen(new GuiChooseManip(playerIn));
		playerIn.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.40f, 1F);

		return ActionResult.resultConsume(stack);

	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		SoundCategory soundcategory = entityLiving instanceof PlayerEntity ? SoundCategory.PLAYERS
				: SoundCategory.HOSTILE;
		worldIn.playSound((PlayerEntity) null, entityLiving.getPosX(), entityLiving.getPosY(), entityLiving.getPosZ(),
				SoundEvents.BLOCK_BEACON_DEACTIVATE, soundcategory, 1.0F,
				1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Also known as a D.S.D. used to"));
		tooltip.add(new StringTextComponent("commandeer Drudges to your will."));
	}

}
