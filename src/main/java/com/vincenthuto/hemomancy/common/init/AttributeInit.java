package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class AttributeInit {

	public enum TriState {
		ALLOW, DEFAULT, DENY
	}

	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE,
			Hemomancy.MOD_ID);

	private static final DeferredHolder<Attribute, Attribute> FALL_FLYING = ATTRIBUTES.register("fall_flying",
			() -> new RangedAttribute("hemomancy.fallFlying", 0.1d, 0.0d, 1.0d).setSyncable(true));
	private static final AttributeModifier ELYTRA_MODIFIER = new AttributeModifier(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "elytra_modifier"), 1.0f,
			AttributeModifier.Operation.ADD_VALUE);

	@SubscribeEvent
	public static void attributeSetup(final EntityAttributeModificationEvent evt) {

		for (EntityType<? extends LivingEntity> type : evt.getTypes()) {
			evt.add(type, AttributeInit.getFlightAttribute());
		}
	}

	public static Holder<Attribute> getFlightAttribute() {
		return FALL_FLYING;
	}

	public static AttributeModifier getElytraModifier() {
		return ELYTRA_MODIFIER;
	}

	public static TriState canFallFly(LivingEntity livingEntity) {
		Holder<Attribute> fallFlying = FALL_FLYING;
		AttributeInstance attribute = livingEntity.getAttribute(fallFlying);

		if (attribute != null) {
			double val = attribute.getValue();
			double baseValue = attribute.getBaseValue();
			double actualBaseValue = fallFlying.value().getDefaultValue();

			if (baseValue != actualBaseValue) {
				attribute.setBaseValue(actualBaseValue);
			}

			if (val >= 1.0d) {
				return TriState.ALLOW;
			} else if (val > 0.0d) {
				return TriState.DEFAULT;
			}
			return TriState.DENY;
		}
		ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);

		if (stack.canElytraFly(livingEntity)) {
			return TriState.ALLOW;
		}
		return TriState.DEFAULT;
	}

	public boolean canFly(LivingEntity livingEntity) {
		return canFallFly(livingEntity) == TriState.ALLOW;
	}

}

