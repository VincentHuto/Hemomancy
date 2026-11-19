package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * An entity renderer's shadow size, which the dispatcher reads straight from the renderer it picked. A renderer that
 * hands its entity to another one (the Chamber ordeals' {@code TrueSelfRenderer}) copies the inner renderer's shadow.
 */
@Mixin(EntityRenderer.class)
public interface EntityRendererShadowAccessor {
	@Accessor("shadowRadius")
	float hemomancy$shadowRadius();
}
