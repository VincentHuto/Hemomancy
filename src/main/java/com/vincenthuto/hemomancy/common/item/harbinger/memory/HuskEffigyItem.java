package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HuskEffigyItem extends HematicMemoryToolItem {
    private final HuskEffigyRules.Profile profile;

    public HuskEffigyItem(Properties properties, HuskEffigyRules.Profile profile) {
        super(properties, 3);
        this.profile = profile;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseHematicTool(player) || !drainBlood(player, profile.getBloodCost())) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            EntityType<? extends Mob> type = switch (profile) {
                case HUSK -> EntityType.HUSK;
                case SPIDER -> EntityType.SPIDER;
                case ZOMBIE -> EntityType.ZOMBIE;
            };
            Mob mob = type.create(serverLevel);
            if (mob == null) {
                return InteractionResultHolder.fail(stack);
            }
            mob.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null);
            mob.getPersistentData().putBoolean(HematicMemoryToolEvents.TAG_EFFIGY, true);
            mob.getPersistentData().putUUID(HematicMemoryToolEvents.TAG_OWNER, player.getUUID());
            mob.getPersistentData().putLong(HematicMemoryToolEvents.TAG_EXPIRES, serverLevel.getGameTime() + profile.getLifetimeTicks());
            mob.setCustomName(Component.translatable("entity.hemomancy.husk_effigy." + profile.getId()));
            mob.setPersistenceRequired();
            serverLevel.addFreshEntity(mob);
            player.getCooldowns().addCooldown(this, 20);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hemomancy.husk_effigy.tooltip",
                profile.getLifetimeTicks() / 20, profile.getBloodCost()).withStyle(ChatFormatting.DARK_RED));
    }
}
