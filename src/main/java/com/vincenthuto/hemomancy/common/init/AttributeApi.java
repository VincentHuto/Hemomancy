package com.vincenthuto.hemomancy.common.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public abstract class AttributeApi {

	public static AttributeApi getInstance() {
		return AttributeImpl.INSTANCE;
	}

	public abstract String getModId();

	/**
	 * The elytra flight attribute, will provide elytra flight if the value is 1.0
	 * or above. No flight otherwise.
	 */
	public abstract Attribute getFlightAttribute();

	/**
	 * The attribute modifier used for the vanilla elytra item.
	 */
	public abstract AttributeModifier getElytraModifier();

	/**
	 * Checks whether or not an entity is able to elytra fly.
	 *
	 * @param livingEntity The entity to check for elytra flight capabilities
	 * @return True if the entity can elytra fly, false otherwise
	 */
	public abstract boolean canFly(LivingEntity livingEntity);
}
