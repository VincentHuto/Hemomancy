package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

/**
 * The room a possessed body takes: each form's hitbox and eye height, copied from the vanilla mob it was taken from.
 * Server and client both read this, so movement checks agree and nothing rubber-bands through a doorway. The tall and
 * wide forms pay for their size in the corridors they no longer fit.
 */
public final class PossessionBodyRules {
	public record Body(float width, float height, float eyeHeight) {}

	private PossessionBodyRules() {}

	public static Body body(SurrogateForm form) {
		return switch (form) {
			case BAT -> new Body(0.5f, 0.9f, 0.45f);
			case ENDERMAN -> new Body(0.6f, 2.9f, 2.55f);
			case BLAZE -> new Body(0.6f, 1.8f, 1.53f);
			case GOLEM -> new Body(1.4f, 2.7f, 2.295f);
			case HORSE -> new Body(1.3964844f, 1.6f, 1.52f);
			case DONKEY -> new Body(1.3964844f, 1.5f, 1.425f);
			case WOLF -> new Body(0.6f, 0.85f, 0.68f);
			case SPIDER -> new Body(1.4f, 0.9f, 0.65f);
			case ARMOR_STAND -> new Body(0.5f, 1.975f, 1.7775f);
		};
	}
}
