package com.huto.hemomancy.item;

import com.huto.hemomancy.gui.guide.GuiGuideTitlePage;
import com.huto.hemomancy.render.item.RenderItemTome;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBloodyBook extends ItemTome {

	public ItemBloodyBook(Properties prop) {
		super(prop.setISTER(() -> RenderItemTome::new));
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (worldIn.isRemote) {
			Minecraft.getInstance().displayGuiScreen(new GuiGuideTitlePage(false));
			playerIn.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.40f, 1F);

		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

}
