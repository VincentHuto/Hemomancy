package com.vincenthuto.hemomancy.compat.mna.item;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedOcculus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloodShotOcculusItem extends Item {

    public BloodShotOcculusItem(Properties prop) {
        super(prop.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.literal("Used to Quickly View Your Occulus"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        } else {
            IPlayerMagic magic = (IPlayerMagic)playerIn.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
            if (magic != null && magic.isMagicUnlocked()) {
                playerIn.openMenu(new NamedOcculus());
            } else {
                playerIn.sendSystemMessage(Component.translatable("block.mna.occulus.confusion"));
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

    }

}