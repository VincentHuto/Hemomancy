package com.vincenthuto.hemomancy.common.init;

import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class AttributeInit {

	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
			Hemomancy.MOD_ID);

	private static final RegistryObject<Attribute> FALL_FLYING = ATTRIBUTES.register("fall_flying",
			() -> new RangedAttribute("hemomancy.fallFlying", 0.0d, 0.0d, 1.0d).setSyncable(true));
	private static final AttributeModifier ELYTRA_MODIFIER = new AttributeModifier(
			UUID.fromString("2e701a8a-9eb0-11ee-8c90-0242ac120002"), "Elytra modifier", 1.0f,
			AttributeModifier.Operation.ADDITION);

	@SubscribeEvent
	public static void attributeSetup(final EntityAttributeModificationEvent evt) {

		for (EntityType<? extends LivingEntity> type : evt.getTypes()) {
			evt.add(type, AttributeInit.getFlightAttribute());
		}
	}

	public static Attribute getFlightAttribute() {
		return FALL_FLYING.get();
	}

	public static AttributeModifier getElytraModifier() {
		return ELYTRA_MODIFIER;
	}

	public static boolean canFly(LivingEntity livingEntity) {
		AttributeInstance attribute = livingEntity.getAttribute(FALL_FLYING.get());

		if (attribute != null) {
			return attribute.getValue() >= 1.0d;
		}
		ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		return stack.canElytraFly(livingEntity);
	}
}
