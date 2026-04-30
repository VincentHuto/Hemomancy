package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

/**
 * Previously held {@code onItemPickup} and {@code onAdvancementEarned}
 * event handlers for the Liber Sanguinum unlock system.
 *
 * <p>Both handlers have been removed: HutosLib's {@code BookDiscoveryEvents}
 * (registered under the {@code hutoslib} modid) now listens to
 * {@code ItemEntityPickupEvent.Post} and {@code AdvancementEarnEvent} and
 * delegates to {@code BookKnowledgeHelper}, which writes into the
 * {@code IBookKnowledge} capability registered via {@code BookKnowledgeProvider}.
 *
 * <p>Item and advancement → entry mappings are registered with
 * {@code BookEntryRegistry} during {@code FMLCommonSetupEvent} in
 * {@link com.vincenthuto.hemomancy.Hemomancy#initLiberBookUnlocks()}.
 *
 * <p>Rite-based and dialogue-based unlocks still use
 * {@link LiberKnowledgeHelper} directly, as those are Hemomancy-specific
 * triggers with no generic equivalent in HutosLib.
 */
public final class LiberDiscoveryEvents {
	private LiberDiscoveryEvents() {
	}
}
