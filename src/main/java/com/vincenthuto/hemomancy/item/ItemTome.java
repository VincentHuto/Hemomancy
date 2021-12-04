package com.vincenthuto.hemomancy.item;

import java.util.Random;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.render.item.RenderItemTome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemTome extends Item {
	public int ticks;
	public float flip;
	public float oFlip;
	public float flipT;
	public float flipA;
	public float nextPageTurningSpeed;
	public float pageTurningSpeed;
	public float nextPageAngle;
	public float pageAngle;
	public float tRot;
	private static final Random random = new Random();

	public ItemTome(Properties prop) {
		super(prop);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (isSelected) {

			this.pageTurningSpeed = this.nextPageTurningSpeed;
			this.pageAngle = this.nextPageAngle;
			this.nextPageTurningSpeed += 0.1F;
			if (this.nextPageTurningSpeed < 0.5F || random.nextInt(40) == 0) {
				float f1 = this.flipT;

				do {
					this.flipT += random.nextInt(4) - random.nextInt(4);
				} while (f1 == this.flipT);
			}
			while (this.nextPageAngle >= (float) Math.PI) {
				this.nextPageAngle -= ((float) Math.PI * 2F);
			}

			while (this.nextPageAngle < -(float) Math.PI) {
				this.nextPageAngle += ((float) Math.PI * 2F);
			}

			while (this.tRot >= (float) Math.PI) {
				this.tRot -= ((float) Math.PI * 2F);
			}

			while (this.tRot < -(float) Math.PI) {
				this.tRot += ((float) Math.PI * 2F);
			}

			float f2;
			for (f2 = this.tRot - this.nextPageAngle; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) {
			}

			while (f2 < -(float) Math.PI) {
				f2 += ((float) Math.PI * 2F);
			}
			this.nextPageAngle += f2 * 0.4F;
			this.nextPageTurningSpeed = Mth.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
			// ++this.ticks;
			this.oFlip = this.flip;
			float f = (this.flipT - this.flip) * 0.4F;
			f = Mth.clamp(f, -0.2F, 0.2F);
			this.flipA += (f - this.flipA) * 0.9F;
			this.flip += this.flipA;

		}
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropTome.INSTANCE);

	}
}

class RenderPropTome implements IItemRenderProperties {

	public static RenderPropTome INSTANCE = new RenderPropTome();

	@Override
	public Font getFont(ItemStack stack) {
		return Minecraft.getInstance().font;
	}

	@Override
	public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
		return new RenderItemTome(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
