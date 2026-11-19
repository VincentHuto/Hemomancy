package com.vincenthuto.hemomancy.client.render.world.circus;

import com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * The Ringmaster finale's merge (owner, 2026-11-18 fourth pass): his real Chamber render projected onto the copied
 * tent's own block faces outside his ring ({@link FinaleVeilRenderer}), strongest at the opening and thinning as he is
 * worn down ({@link FinaleRules#veilStrength}), then his theatre in the tent (the phantom audience in the seats, the echoes
 * at their stations and the stage lights on them, {@link FinaleTheatre}).
 *
 * <p>Drawn at AFTER_TRANSLUCENT_BLOCKS, with the other face layers (the heart-node spread, the fane ground): by then
 * every terrain layer and every entity has written its depth, so the faces mark the stencil only where a tent face is
 * the nearest surface. The carousel, his copy, the player and anything else standing in front of a face hide it as they should, and
 * glass or water in front keeps its own look. The theatre draws after the projection, so the audience on the seats is never
 * painted over.
 */
public final class FinaleMergeRenderer {
	private FinaleMergeRenderer() {
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		if (Minecraft.getInstance().level == null) return;
		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		// The faces are found from the crossing on (strength 0), so they are ready as the veil rises.
		FinaleVeilRenderer.render(event, partial);
		if (RingmasterFinaleClientData.merging(partial)) FinaleTheatre.render(event.getPoseStack(), partial);
	}
}
