package com.vincenthuto.hemomancy.entity.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.recipe.PolypRecipe;
import com.vincenthuto.hemomancy.recipe.PolypRecipes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityMorphlingPolypItem extends ItemEntity {

	private int pickupDelayNew = 40;

	public EntityMorphlingPolypItem(EntityType<? extends ItemEntity> p_i50217_1_, Level world) {
		super(p_i50217_1_, world);

	}

	public EntityMorphlingPolypItem(EntityType<? extends ItemEntity> p_i50217_1_, Level p_i50217_2_, double posX,
			double posY, double posZ, ItemStack stack) {
		super(p_i50217_1_, p_i50217_2_);
		this.setPos(posX, posY, posZ);
		this.setItem(stack);
		this.lifespan = (stack.getItem() == null ? 6000 : stack.getEntityLifespan(p_i50217_2_));
	}

	@Override
	public void tick() {

		if (this.pickupDelayNew > 0 && this.pickupDelayNew != 32767) {
			--this.pickupDelayNew;
		}

		// Client FX
		if (level.isClientSide) {
			double r = 0.1;
			double m = 0.1;
			for (int i = 0; i < 1; i++) {
				level.addParticle(ParticleTypes.REVERSE_PORTAL, getX() + r * (Math.random() - 0.5),
						getY() + r * (Math.random() - 0.5), getZ() + r * (Math.random() - 0.5),
						m * (Math.random() - 0.5), m * (Math.random() - 0.5), m * (Math.random() - 0.5));
			}
		}
		List<Entity> entList = this.level.getEntities(this, this.getBoundingBox().inflate(0.75));
		List<Item> itemList = new ArrayList<Item>();
		List<ItemEntity> itemEntList = new ArrayList<ItemEntity>();

		// Machina Spark
		for (int i = 0; i < entList.size(); i++) {
			if (entList.get(i) instanceof ItemEntity) {
				ItemEntity itemEnt = (ItemEntity) entList.get(i);
				for (PolypRecipe rec : PolypRecipes.POLYPRECIPES) {
					if (rec.getIngr().contains(itemEnt.getItem().getItem())) {
						if (!itemList.contains(itemEnt.getItem().getItem())) {
							itemList.add(itemEnt.getItem().getItem());
						}
						if (!itemEntList.contains(itemEnt)) {
							itemEntList.add(itemEnt);
						}

						if (itemList.containsAll(rec.getIngr())) {
							if (!level.isClientSide) {
								for (ItemEntity it : itemEntList) {
									it.getItem().shrink(1);
								}
								level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(),
										new ItemStack(rec.getOutput())));
								this.getItem().shrink(1);
							} else {
								level.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D,
										0.0D);
								level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D,
										0.0D, 0.0D);
							}
						}
					}
				}

			}
		}
		super.tick();

	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}