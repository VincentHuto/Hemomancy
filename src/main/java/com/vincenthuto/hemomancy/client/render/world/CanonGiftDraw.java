package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * What a Saint gift's geometry draws with in one pass (D-1): the flow materials, the buffers that hold Hemorath's iron
 * and Seraphae's bone for the solid pass, the camera relative to the cue's anchor, the shared time and the caster's pose. {@code source}
 * is the cue's entity (the caster, or the chained or grown body) and {@code caster} the gift's caster where the cue
 * names one.
 */
record CanonGiftDraw(PoseStack poses, LuxUmbraBatch flows, MultiBufferSource buffers, boolean solids,
		Vec3 camera, Vec3 right, Vec3 up, double time, float age, float opacity, CasterFrame frame,
		@Nullable Entity source, @Nullable Entity caster, Vec3 anchor, float partial) {

	/** Hemorath's iron, fetched where it is drawn: taking another solid buffer would end this one's batch. */
	VertexConsumer iron() {
		return solids ? buffers.getBuffer(HemorathRenderTypes.IRON) : FerricDuctilisGeometry.DISCARD;
	}

	/** Seraphae's bone, fetched where it is drawn, as {@link #iron()}. */
	VertexConsumer bone() {
		if (!solids) return FerricDuctilisGeometry.DISCARD;
		SeraphaeRenderTypes.begin(time);
		return buffers.getBuffer(SeraphaeRenderTypes.BONE);
	}

	/** Putriciel's growths (caps, knots, sacs), fetched where they are drawn, as {@link #iron()}. */
	VertexConsumer growth() {
		if (!solids) return FerricDuctilisGeometry.DISCARD;
		PutricielRenderTypes.begin(time);
		return buffers.getBuffer(PutricielRenderTypes.GROWTH);
	}

	/** Velorum's Frozen Cruor, fetched where it is drawn, as {@link #iron()}. */
	VertexConsumer ice() {
		if (!solids) return FerricDuctilisGeometry.DISCARD;
		VelorumRenderTypes.begin(time);
		return buffers.getBuffer(VelorumRenderTypes.ICE);
	}

	VertexConsumer rot() {
		return flows.vertices(LuxUmbraBatch.Material.PUTRICIEL_ROT);
	}

	VertexConsumer flame() {
		return flows.vertices(LuxUmbraBatch.Material.FLAME);
	}

	VertexConsumer rime() {
		return flows.vertices(LuxUmbraBatch.Material.RIME);
	}

	VertexConsumer umbra() {
		return flows.vertices(LuxUmbraBatch.Material.UMBRA);
	}

	VertexConsumer blood() {
		return flows.vertices(LuxUmbraBatch.Material.ANIMUS);
	}

	VertexConsumer clot() {
		return flows.vertices(LuxUmbraBatch.Material.CRUOR);
	}

	VertexConsumer lux() {
		return flows.vertices(LuxUmbraBatch.Material.LUX);
	}

	VertexConsumer gold() {
		return flows.vertices(LuxUmbraBatch.Material.DUCTILIS);
	}
}
