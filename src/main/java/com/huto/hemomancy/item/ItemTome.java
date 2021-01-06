package com.huto.hemomancy.item;

import java.util.Random;

import com.huto.hemomancy.render.item.RenderItemTome;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemTome extends Item {
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

	public ItemTome(Properties prop) {
		super(prop.setISTER(() -> RenderItemTome::new));
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
		//++this.ticks;
		this.field_195524_g = this.field_195523_f;
		float f = (this.field_195525_h - this.field_195523_f) * 0.4F;
		f = MathHelper.clamp(f, -0.2F, 0.2F);
		this.field_195526_i += (f - this.field_195526_i) * 0.9F;
		this.field_195523_f += this.field_195526_i;

	}

}
