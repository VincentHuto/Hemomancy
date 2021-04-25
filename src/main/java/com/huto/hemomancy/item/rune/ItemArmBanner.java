package com.huto.hemomancy.item.rune;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.huto.hemomancy.render.item.RenderArmBanner;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemArmBanner extends Item {
	public int ticks;
	public float field_195523_f;
	public float field_195524_g;
	public float field_195525_h;
	public float field_195526_i;
	public float nextPageTurningSpeed;
	public float pageTurningSpeed;
	public float nextPageAngle;
	public float pageAngle;
	public float field_195531_n;
	private static final Random random = new Random();

	public ItemArmBanner(Properties prop) {
		super(prop.setISTER(() -> RenderArmBanner::new));
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		this.pageTurningSpeed = this.nextPageTurningSpeed;
		this.pageAngle = this.nextPageAngle;

		this.nextPageTurningSpeed += 0.1F;
		if (this.nextPageTurningSpeed < 0.5F || random.nextInt(40) == 0) {
			float f1 = this.field_195525_h;

			do {
				this.field_195525_h += (float) (random.nextInt(4) - random.nextInt(4));
			} while (f1 == this.field_195525_h);
		}

		while (this.nextPageAngle >= (float) Math.PI) {
			this.nextPageAngle -= ((float) Math.PI * 2F);
		}

		while (this.nextPageAngle < -(float) Math.PI) {
			this.nextPageAngle += ((float) Math.PI * 2F);
		}

		while (this.field_195531_n >= (float) Math.PI) {
			this.field_195531_n -= ((float) Math.PI * 2F);
		}

		while (this.field_195531_n < -(float) Math.PI) {
			this.field_195531_n += ((float) Math.PI * 2F);
		}

		float f2;
		for (f2 = this.field_195531_n - this.nextPageAngle; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) {
		}

		while (f2 < -(float) Math.PI) {
			f2 += ((float) Math.PI * 2F);
		}

		this.nextPageAngle += f2 * 0.4F;
		this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
		// ++this.ticks;
		this.field_195524_g = this.field_195523_f;
		float f = (this.field_195525_h - this.field_195523_f) * 0.4F;
		f = MathHelper.clamp(f, -0.2F, 0.2F);
		this.field_195526_i += (f - this.field_195526_i) * 0.9F;
		this.field_195523_f += this.field_195526_i;

	}

	/**
	 * Returns the unlocalized name of this item. This version accepts an ItemStack
	 * so different stacks can have different names based on their damage or NBT.
	 */
	@Override
	public String getTranslationKey(ItemStack stack) {
		return stack.getChildTag("BlockEntityTag") != null
				? this.getTranslationKey() + '.' + getColor(stack).getTranslationKey()
				: super.getTranslationKey(stack);
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		BannerItem.appendHoverTextFromTileEntityTag(stack, tooltip);
	}

	/**
	 * returns the action that specifies what animation to play when the items is
	 * being used
	 */
	@Override

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override

	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when
	 * this item is used on a Block, see {@link #onItemUse}.
	 */
	@Override

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(itemstack);
	}

	/**
	 * Return whether this item is repairable in an anvil.
	 */
	@Override

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return ItemTags.PLANKS.contains(repair.getItem()) || super.getIsRepairable(toRepair, repair);
	}

	public static DyeColor getColor(ItemStack stack) {
		return DyeColor.byId(stack.getOrCreateChildTag("BlockEntityTag").getInt("Base"));
	}
}
