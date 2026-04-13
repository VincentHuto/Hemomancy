package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.client.screen.skilltree.SkillTreeScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemSanguineConduit extends Item{

    public ItemSanguineConduit(Properties prop) {
        super(prop.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level lvl, Player p_41433_, InteractionHand p_41434_) {
        // Opens the Skill Tree screen (client-only)
        if (lvl.isClientSide) {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> SkillTreeScreen::openScreen);
        }
        return super.use(lvl, p_41433_, p_41434_);
    }


}
