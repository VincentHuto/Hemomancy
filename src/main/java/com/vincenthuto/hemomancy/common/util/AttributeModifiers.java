package com.vincenthuto.hemomancy.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nullable;

/**
 * Holds a modifier on an attribute at a wanted amount, touching the attribute only when something changes. Every
 * remove or add marks a syncable attribute dirty, and a dirty attribute costs one update packet to the holder and
 * every tracker, so re-applying an unchanged modifier each tick is a packet storm (P-1).
 */
public final class AttributeModifiers {
	private AttributeModifiers() {
	}

	/**
	 * Holds a transient modifier with this id, amount and operation while {@code present}, and removes it otherwise.
	 * Does nothing when the attribute already matches; a null attribute is ignored.
	 */
	public static void sync(@Nullable AttributeInstance attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation, boolean present) {
		sync(attribute, id, amount, operation, present, false);
	}

	/** {@link #sync(AttributeInstance, ResourceLocation, double, AttributeModifier.Operation, boolean)} for a fixed modifier. */
	public static void sync(@Nullable AttributeInstance attribute, AttributeModifier modifier, boolean present) {
		sync(attribute, modifier.id(), modifier.amount(), modifier.operation(), present, false);
	}

	/** As {@code sync}, but the modifier is permanent, so it is saved with the entity. */
	public static void syncPermanent(@Nullable AttributeInstance attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation, boolean present) {
		sync(attribute, id, amount, operation, present, true);
	}

	private static void sync(@Nullable AttributeInstance attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation, boolean present, boolean permanent) {
		if (attribute == null) return;
		AttributeModifier held = attribute.getModifier(id);
		if (!present) {
			if (held != null) attribute.removeModifier(id);
			return;
		}
		if (held != null) {
			if (held.amount() == amount && held.operation() == operation) return;
			attribute.removeModifier(id);
		}
		AttributeModifier modifier = new AttributeModifier(id, amount, operation);
		if (permanent) attribute.addPermanentModifier(modifier);
		else attribute.addTransientModifier(modifier);
	}
}
