package com.vincenthuto.hemomancy.client.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class NpcProgressionMarkerClientEventsTest {
	@Test
	void progressionSigilCompletesEntityFormatVertices() {
		try (ByteBufferBuilder bytes = new ByteBufferBuilder(256)) {
			BufferBuilder vertices = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
			NpcProgressionMarkerClientEvents.renderQuad(vertices, new PoseStack().last(), 1.0F);
			try (var ignored = vertices.buildOrThrow()) {
			}
		}
	}

	@Test
	void unstainedNpcsUseTheirOwnSigil() {
		assertEquals(ResourceLocation.parse("hemomancy:textures/entity/npc/unstained_progression_sigil.png"),
				NpcProgressionMarkerClientEvents.textureFor(ResourceLocation.parse("hemomancy:unstained_acolyte")));
		assertEquals(ResourceLocation.parse("hemomancy:textures/entity/npc/progression_sigil.png"),
				NpcProgressionMarkerClientEvents.textureFor(ResourceLocation.parse("hemomancy:harbinger_vicar")));
	}
}
