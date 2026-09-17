package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.HemolymphopodaEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.ChatFormatting;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class BloodVialItem extends Item {

	public static String TAG_ENTITY_TYPE = "entity_type";
	public static String TAG_STATE = "state";

    public static EntityType<?> getEntityType(ItemStack stack) {
        return BloodSampleData.entityType(stack);
    }

	public BloodVialItem(Properties prop) {
		super(prop.stacksTo(1));
	}

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!BloodSampleData.isFilled(stack)) return;
        var type = getEntityType(stack);
        tooltip.add(Component.translatable("item.hemomancy.bloody_vial.source", type == null
                ? Component.literal(BloodSampleData.rawSource(stack)) : type.getDescription()));
        if (type == null) {
            tooltip.add(Component.translatable("item.hemomancy.bloody_vial.unreadable").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!BloodSampleData.identified(stack)) {
            tooltip.add(Component.translatable("item.hemomancy.bloody_vial.unknown").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.hemomancy.bloody_vial.examine").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            var data = BloodInjectionData.snapshot(context.level() == null || context.level().isClientSide);
            var profile = BloodSampleData.profile(stack, data.properties());
            for (var tendency : profile.tendencies()) tooltip.add(Component.translatable(
                    "item.hemomancy.bloody_vial.tendency", Component.translatable("blood_tendency.hemomancy." + tendency.name().toLowerCase(java.util.Locale.ROOT)))
                    .withStyle(ChatFormatting.DARK_GRAY));
            for (var property : profile.properties()) tooltip.add(Component.translatable("item.hemomancy.bloody_vial.property",
                    Component.translatableWithFallback("blood_property." + property.getNamespace() + "." + property.getPath().replace('/', '.'), property.toString()))
                    .withStyle(ChatFormatting.DARK_GRAY));
            var result = data.resolve(profile);
            for (var benefit : result.benefits()) tooltip.add(Component.translatable("item.hemomancy.bloody_vial.injection", responseText(benefit)).withStyle(ChatFormatting.BLUE));
            for (var drawback : result.drawbacks()) tooltip.add(Component.translatable("item.hemomancy.bloody_vial.drawback", responseText(drawback)).withStyle(ChatFormatting.RED));
            if (!result.usable()) tooltip.add(Component.translatable("message.hemomancy.blood_injection.no_response"));
            tooltip.add(Component.translatable("item.hemomancy.bloody_vial.saturation").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.hemomancy.bloody_vial.inject").withStyle(ChatFormatting.DARK_RED));
    }

    private static net.minecraft.network.chat.MutableComponent responseText(BloodInjectionRules.Benefit benefit) {
        Component name = benefit.effect() == null ? Component.translatable("item.hemomancy.bloody_vial.reveal", benefit.radius())
                : BuiltInRegistries.MOB_EFFECT.getOptional(benefit.effect()).orElseThrow().getDisplayName();
        return Component.translatable("item.hemomancy.bloody_vial.response", name, benefit.amplifier() + 1, benefit.duration() / 20.0);
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return BloodInjectionRules.USE_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!BloodSampleData.isFilled(stack)) return InteractionResultHolder.pass(stack);
        if (player.isSpectator() || !player.isAlive()) return InteractionResultHolder.fail(stack);
        if (player.hasEffect(EffectInit.transfusion_saturation)) {
            feedback(player, "saturated");
            return InteractionResultHolder.fail(stack);
        }
        var data = BloodInjectionData.snapshot(level.isClientSide);
        if (getEntityType(stack) == null || !data.resolve(BloodSampleData.profile(stack, data.properties())).usable()) {
            feedback(player, "no_response");
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        BloodVialInjectionAnimation.start(player, hand, stack);
        return InteractionResultHolder.consume(stack);
    }

    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) BloodVialInjectionAnimation.cancel(player);
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player) || player.isSpectator() || !player.isAlive()
                || !player.isUsingItem() || player.getTicksUsingItem() < BloodInjectionRules.USE_TICKS
                || player.getUseItem() != stack || player.getItemInHand(player.getUsedItemHand()) != stack
                || stack.getCount() != 1) return stack;
        if (player.hasEffect(EffectInit.transfusion_saturation)) {
            BloodVialInjectionAnimation.cancel(player);
            feedback(player, "saturated");
            return stack;
        }
        var data = BloodInjectionData.snapshot(false);
        var profile = BloodSampleData.profile(stack, data.properties());
        var result = data.resolve(profile);
        if (getEntityType(stack) == null || !result.usable()) {
            BloodVialInjectionAnimation.cancel(player);
            feedback(player, "no_response");
            return stack;
        }
        player.addEffect(new MobEffectInstance(EffectInit.transfusion_saturation, BloodInjectionRules.SATURATION_TICKS));
        for (var benefit : result.benefits()) applyResponse(player, benefit);
        for (var drawback : result.drawbacks()) applyResponse(player, drawback);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (player.getAbilities().instabuild) {
            BloodVialInjectionAnimation.finish(player, stack);
            return stack;
        }
        var empty = BloodSampleData.emptyVessel(stack);
        BloodVialInjectionAnimation.finish(player, empty);
        stack.shrink(1);
        return empty;
    }

    private static void applyResponse(Player player, BloodInjectionRules.Benefit benefit) {
        if (benefit.effect() != null) {
            var effect = BuiltInRegistries.MOB_EFFECT.getHolder(benefit.effect()).orElseThrow();
            player.addEffect(new MobEffectInstance(effect, benefit.duration(), benefit.amplifier()));
        } else {
            for (var mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(benefit.radius()),
                    mob -> mob.isAlive() && mob.distanceToSqr(player) <= benefit.radius() * benefit.radius()))
                SchoolStates.apply(player, mob, SchoolState.ILLUMINATED, benefit.duration());
        }
    }

    private static void feedback(Player player, String reason) {
        if (!player.level().isClientSide) player.displayClientMessage(Component.translatable("message.hemomancy.blood_injection." + reason), true);
    }

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		boolean alreadyFilled = BloodSampleData.isFilled(stack);
		if (entity instanceof LivingEntity living && !alreadyFilled) {
				// Special case: Hemolymphopoda produces Cleansing Hemolymph instead of a standard sample
				if (living instanceof HemolymphopodaEntity) {
					if (!player.level().isClientSide) {
						ItemStack hemolymphStack = new ItemStack(ItemInit.cleansing_hemolymph.get());
						player.setItemInHand(InteractionHand.MAIN_HAND, hemolymphStack);
						player.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
					}
					// Return true on both sides to cancel the attack; inventory syncs from server
					return true;
				}
		}
		ResourceLocation entityTypeId = entity == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		BloodSamplingResult result = BloodSamplingRules.evaluate(alreadyFilled, entity instanceof LivingEntity,
				entity instanceof LivingEntity living && living.isAlive(),
				entity instanceof LivingEntity living && living.isInvulnerable(), entityTypeId != null);
		if (!player.level().isClientSide) {
			if (result == BloodSamplingResult.SUCCESS) {
				tag.putString(TAG_ENTITY_TYPE, entityTypeId.toString());
				tag.putBoolean(TAG_STATE, true);
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                stack.remove(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get());
				player.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
			}
			Component targetName = entity == null ? Component.translatable("message.hemomancy.blood_sampling.unknown")
					: entity.getDisplayName();
			player.displayClientMessage(Component.translatable(result.translationKey(), targetName), true);
		}
		return true;
	}

}
