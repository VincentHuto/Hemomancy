package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.HeartyCompassItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class HeartyCompassItem extends HematicMemoryToolItem implements HemoClientItemExtensionsProvider {
    public HeartyCompassItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseHematicTool(player)) {
            return InteractionResultHolder.fail(stack);
        }
        LastDeathMemory memory = HemoCapabilityAccess.getLastDeathMemory(player);
        if (!memory.hasMemory()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hemomancy.hearty_compass.empty")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                HematicMemoryToolEvents.syncLastDeathMemory(serverPlayer);
            }
            player.displayClientMessage(Component.translatable("item.hemomancy.hearty_compass.memory",
                    memory.dimensionId(), LastDeathMemory.unpackX(memory.packedPosition()),
                    LastDeathMemory.unpackY(memory.packedPosition()), LastDeathMemory.unpackZ(memory.packedPosition()))
                    .withStyle(memory.isSameDimension(level.dimension().location().toString())
                            ? ChatFormatting.DARK_RED : ChatFormatting.DARK_PURPLE), true);
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public IClientItemExtensions hemomancy$getClientItemExtensions() {
        return RenderPropHeartyCompass.INSTANCE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.hearty_compass.tooltip").withStyle(ChatFormatting.DARK_RED));
    }
}

class RenderPropHeartyCompass implements IClientItemExtensions {
    static final RenderPropHeartyCompass INSTANCE = new RenderPropHeartyCompass();

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new HeartyCompassItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }
}
