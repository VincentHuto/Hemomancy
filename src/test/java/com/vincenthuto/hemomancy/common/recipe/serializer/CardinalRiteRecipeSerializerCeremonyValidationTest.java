package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteRecipeSerializerCeremonyValidationTest {
	@Test
	void rejectsHarbingerRecipesThatOmitCeremonyData() throws Exception {
		JsonObject json = new JsonObject();

		try {
			parseCeremony(json);
			throw new AssertionError("missing ceremony must reject the recipe");
		} catch (InvocationTargetException exception) {
			assertTrue(exception.getCause().getMessage().contains("ceremony"),
					"error should identify the missing ceremony field");
		}
	}

	@Test
	void preservesIntentionallyEmptyCeremonyCollections() throws Exception {
		JsonObject json = ceremonyWithAnchors(new JsonArray());

		CardinalRiteCeremonyDefinition parsed = parseCeremony(json);

		assertEquals(0, parsed.anchors().size());
		assertEquals(0, parsed.supportSockets().size());
		assertEquals(0, parsed.waves().size());
		assertEquals(0, parsed.fragileOffsets().size());
		assertEquals(400, parsed.getClass().getMethod("targetDurationTicks").invoke(parsed));
		assertEquals(0, parsed.getClass().getMethod("requiredHelpers").invoke(parsed));
		assertEquals(0, parsed.getClass().getMethod("stillIntervalTicks").invoke(parsed));
	}

	@Test
	void decodedCeremoniesResolveAnchorsFromTheLiveRingArrays() throws Exception {
		JsonObject anchor = new JsonObject();
		anchor.addProperty("y", 1);
		anchor.addProperty("ring", 1);
		anchor.addProperty("order", 0);
		JsonArray anchors = new JsonArray();
		anchors.add(anchor);
		double originalAngle = CardinalRiteRingTuning.ROTATION_DEGREES[1];
		double originalRadius = CardinalRiteRingTuning.RADIUS_BLOCKS[1];
		try {
			CardinalRiteRingTuning.ROTATION_DEGREES[1] = 0.0D;
			CardinalRiteRingTuning.RADIUS_BLOCKS[1] = 5.0D;

			var parsed = parseCeremony(ceremonyWithAnchors(anchors));

			assertEquals(new CardinalRiteCeremonyDefinition.Anchor(0, 1, -5, 1, 0),
					parsed.anchors().getFirst());
		} finally {
			CardinalRiteRingTuning.ROTATION_DEGREES[1] = originalAngle;
			CardinalRiteRingTuning.RADIUS_BLOCKS[1] = originalRadius;
		}
	}

	private static JsonObject ceremonyWithAnchors(JsonArray anchors) {
		JsonObject json = new JsonObject();
		JsonObject ceremony = new JsonObject();
		ceremony.addProperty("profile", "simple");
		ceremony.add("anchors", anchors);
		ceremony.add("support_sockets", new JsonArray());
		ceremony.add("waves", new JsonArray());
		ceremony.add("guaranteed_waves", new JsonArray());
		ceremony.addProperty("signature", "test");
		ceremony.add("fragile_offsets", new JsonArray());
		ceremony.addProperty("target_duration_ticks", 400);
		ceremony.addProperty("focus", "hematic_medium");
		ceremony.addProperty("required_helpers", 0);
		JsonArray roles = new JsonArray();
		roles.add("anchor");
		ceremony.add("helper_roles", roles);
		ceremony.addProperty("still_interval_ticks", 0);
		JsonObject atmosphere = new JsonObject();
		atmosphere.addProperty("fog", "none");
		atmosphere.addProperty("lightning", false);
		atmosphere.addProperty("dome", false);
		ceremony.add("atmosphere", atmosphere);
		ceremony.addProperty("failure", "safe_retry");
		json.add("ceremony", ceremony);
		return json;
	}

	private static CardinalRiteCeremonyDefinition parseCeremony(JsonObject json) throws Exception {
		Method method = CardinalRiteRecipeSerializer.class.getDeclaredMethod("ceremonyFromJson",
				JsonObject.class, ResourceLocation.class, CardinalRiteType.class, int.class);
		method.setAccessible(true);
		return (CardinalRiteCeremonyDefinition) method.invoke(null, json,
				ResourceLocation.parse("hemomancy:cardinal_rite/test"), CardinalRiteType.MINOR, 1);
	}
}
