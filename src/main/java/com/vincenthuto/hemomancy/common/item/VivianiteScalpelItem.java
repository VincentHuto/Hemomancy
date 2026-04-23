package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class VivianiteScalpelItem  extends Item {

    public VivianiteScalpelItem(Properties prop) {
        super(prop);
        prop.stacksTo(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            Minecraft mc = Minecraft.getInstance();
            mc.cameraEntity.setPos(mc.cameraEntity.getPosition(HLClientUtils.getPartialTicks()).add(0, 2, 0));
        }
        return super.use(level, player, hand);
    }

}
