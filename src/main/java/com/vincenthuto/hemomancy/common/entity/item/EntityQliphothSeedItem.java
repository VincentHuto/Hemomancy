package com.vincenthuto.hemomancy.common.entity.item;

import javax.annotation.Nonnull;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityQliphothSeedItem extends ItemEntity {

    public EntityQliphothSeedItem(EntityType<? extends ItemEntity> type, Level world) {
        super(type, world);
    }

    public EntityQliphothSeedItem(EntityType<? extends ItemEntity> type, Level world,
            double x, double y, double z, ItemStack stack) {
        super(type, world);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.lifespan = stack.getItem() == null ? 6000 : stack.getEntityLifespan(world);
    }

    @Nonnull
}
