package com.vincenthuto.hemomancy.common.rite.sigil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class IchorianSigilLoader extends JsonResourceReloadListener<IchorianSigilDefinition> {
	private static final Logger LOGGER = LogManager.getLogger();

	public IchorianSigilLoader() {
		super("ichorian_sigil", "Ichorian Sigil", LOGGER,
				IchorianSigilLoader::parseDefinition, IchorianSigilRegistry::reload);
	}

	static IchorianSigilDefinition parseDefinition(ResourceLocation id, JsonObject root) {
		var nodes = new ArrayList<IchorianSigilDefinition.Node>();
		for (JsonElement element : root.getAsJsonArray("nodes")) {
			var pair = element.getAsJsonArray();
			nodes.add(new IchorianSigilDefinition.Node(pair.get(0).getAsDouble(),
					pair.get(1).getAsDouble()));
		}
		var connections = new ArrayList<IchorianSigilDefinition.Connection>();
		if (root.has("connections")) {
			for (JsonElement element : root.getAsJsonArray("connections")) {
				var pair = element.getAsJsonArray();
				connections.add(new IchorianSigilDefinition.Connection(
						pair.get(0).getAsInt(), pair.get(1).getAsInt()));
			}
		}
		Optional<IchorianSigilAnatomy> awakenedForm = Optional.empty();
		if (root.has("awakened_form")) {
			IchorianSigilAnatomy parsed = parseAnatomy(root.getAsJsonObject("awakened_form"));
			IchorianSigilAnatomyValidator.Result validation =
					IchorianSigilAnatomyValidator.validate(nodes.size(), parsed);
			for (String error : validation.errors()) {
				LOGGER.error("Invalid awakened form for Ichorian Sigil {}: {}", id, error);
			}
			awakenedForm = validation.form();
		}
		return new IchorianSigilDefinition(
				id,
				IchorianSigilDefinition.Kind.valueOf(root.get("kind").getAsString()
						.toUpperCase(Locale.ROOT)),
				root.has("tier") ? root.get("tier").getAsInt() : 1,
				Integer.decode(root.get("color").getAsString()),
				root.get("name").getAsString(),
				root.get("purpose").getAsString(),
				root.has("stability") ? root.get("stability").getAsInt() : 0,
				root.has("capacity_ml") ? root.get("capacity_ml").getAsInt() : 0,
				nodes, connections, awakenedForm);
	}

	private static IchorianSigilAnatomy parseAnatomy(JsonObject root) {
		Vec3 forward = vector(root.getAsJsonArray("forward"));
		JsonObject animationRoot = root.getAsJsonObject("animation");
		var animation = new IchorianSigilAnatomy.Animation(
				IchorianSigilAnatomy.Style.valueOf(animationRoot.get("style").getAsString()
						.toUpperCase(Locale.ROOT)),
				animationRoot.get("pulse").getAsFloat(),
				animationRoot.get("flex").getAsFloat(),
				animationRoot.get("lag").getAsFloat());
		var landmarks = new ArrayList<IchorianSigilAnatomy.Landmark>();
		for (JsonElement element : root.getAsJsonArray("nodes")) {
			JsonObject landmark = element.getAsJsonObject();
			landmarks.add(new IchorianSigilAnatomy.Landmark(
					landmark.get("source").getAsInt(),
					vector(landmark.getAsJsonArray("position")),
					IchorianSigilAnatomy.Role.valueOf(landmark.get("role").getAsString()
							.toUpperCase(Locale.ROOT)),
					landmark.get("radius").getAsFloat()));
		}
		var vessels = new ArrayList<IchorianSigilAnatomy.Vessel>();
		for (JsonElement element : root.getAsJsonArray("vessels")) {
			JsonObject vessel = element.getAsJsonObject();
			vessels.add(new IchorianSigilAnatomy.Vessel(
					vessel.get("from").getAsInt(),
					vessel.get("to").getAsInt(),
					vessel.get("thickness").getAsFloat()));
		}
		var membranes = new ArrayList<IchorianSigilAnatomy.Membrane>();
		for (JsonElement element : root.getAsJsonArray("membranes")) {
			var triangle = element.getAsJsonArray();
			membranes.add(new IchorianSigilAnatomy.Membrane(
					triangle.get(0).getAsInt(),
					triangle.get(1).getAsInt(),
					triangle.get(2).getAsInt()));
		}
		return new IchorianSigilAnatomy(forward, animation, landmarks, vessels, membranes);
	}

	private static Vec3 vector(com.google.gson.JsonArray values) {
		return new Vec3(values.get(0).getAsDouble(),
				values.get(1).getAsDouble(), values.get(2).getAsDouble());
	}

}
