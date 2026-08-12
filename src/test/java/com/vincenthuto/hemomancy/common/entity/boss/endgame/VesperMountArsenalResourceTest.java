package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperMountArsenalResourceTest {
	private static final Path ROOT = Path.of("src/main");

	@Test
	void crownedModelOwnsBothAuthoredAttacksAndNamedSockets() throws Exception {
		JsonObject model = JsonParser.parseString(Files.readString(ROOT.resolve(
				"resources/assets/hemomancy/models/entity/bbmodel/bosses/VesperTheCrownedRefusalModel.bbmodel"))).getAsJsonObject();
		JsonArray animations = model.getAsJsonArray("animations");
		JsonArray groups = model.getAsJsonArray("groups");
		assertTrue(animations.asList().stream().anyMatch(value -> value.getAsJsonObject().get("name").getAsString().endsWith("carapace_aneurysm")));
		assertTrue(animations.asList().stream().anyMatch(value -> value.getAsJsonObject().get("name").getAsString().endsWith("grab_impalement")));
		for (String required : new String[] { "scutes_front", "scutes_mid", "scutes_rear", "grab_socket", "bite_socket", "impale_socket" }) {
			assertTrue(groups.asList().stream().anyMatch(value -> required.equals(value.getAsJsonObject().get("name").getAsString())), required);
		}
	}

	@Test
	void projectileRendererSoundsAndServerStateAreWired() throws Exception {
		String entities = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String clients = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String sounds = Files.readString(ROOT.resolve("resources/assets/hemomancy/sounds.json"));
		String crowned = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java"));
		String actions = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/common/entity/boss/endgame/EndgameBossActions.java"));
		String projectile = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/common/entity/projectile/VesperScuteProjectileEntity.java"));
		String model = Files.readString(ROOT.resolve("java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java"));
		assertTrue(entities.contains("vesper_scute_projectile"));
		assertTrue(clients.contains("VesperScuteProjectileRenderer::new"));
		for (String sound : new String[] { "entity.vesper.scute_launch", "entity.vesper.carapace_reform",
				"entity.vesper.grab_telegraph", "entity.vesper.grab_bite", "entity.vesper.grab_pierce", "entity.vesper.grab_release" }) {
			assertTrue(sounds.contains("\"" + sound + "\""), sound);
		}
		for (String state : new String[] { "DATA_CARAPACE_EXPOSED", "DATA_RESTRAINED_VICTIM_ID", "RestrainedVictim",
				"CarapaceCooldown", "GrabHitMask" }) {
			assertTrue(crowned.contains(state), state);
		}
		assertTrue(actions.contains("for (int i = 0; i < 12; i++)"));
		assertTrue(actions.contains("ring < 2"));
		assertTrue(actions.contains("applyGrabBite"));
		assertTrue(actions.contains("applyGrabImpale"));
		assertTrue(actions.contains("releaseRestrainedVictim(true)"));
		assertTrue(crowned.contains("EffectInit.blood_loss, VesperMountAttackRules.BLOOD_LOSS_TICKS"));
		assertTrue(crowned.contains("MobEffects.POISON, VesperMountAttackRules.POISON_TICKS"));
		assertTrue(projectile.contains("HemoDamageTypes.vesperScute(level(), this, boss), 6.0F"));
		assertTrue(projectile.contains("onHitBlock"));
		assertTrue(projectile.contains("ScuteOriginX"));
		assertTrue(model.contains("applyCarapaceVisibility"));
	}
}
