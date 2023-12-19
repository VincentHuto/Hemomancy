package com.vincenthuto.hemomancy.common.init;

import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AttributeImpl extends AttributeApi {

	public static final AttributeApi INSTANCE = new AttributeImpl();

	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
			Hemomancy.MOD_ID);

	private static final RegistryObject<Attribute> FALL_FLYING = ATTRIBUTES.register("fall_flying",
			() -> new RangedAttribute("hemomancy.fallFlying", 0.0d, 0.0d, 1.0d).setSyncable(true));
	private static final AttributeModifier ELYTRA_MODIFIER = new AttributeModifier(
			UUID.fromString("2e701a8a-9eb0-11ee-8c90-0242ac120002"), "Elytra modifier", 1.0f,
			AttributeModifier.Operation.ADDITION);

	@Override
	public String getModId() {
		return Hemomancy.MOD_ID;
	}

	@Override
	public Attribute getFlightAttribute() {
		return FALL_FLYING.get();
	}

	@Override
	public AttributeModifier getElytraModifier() {
		return ELYTRA_MODIFIER;
	}

	@Override
	public boolean canFly(LivingEntity livingEntity) {
		AttributeInstance attribute = livingEntity.getAttribute(FALL_FLYING.get());

		if (attribute != null) {
			return attribute.getValue() >= 1.0d;
		}
		ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		return stack.canElytraFly(livingEntity);
	}
}
