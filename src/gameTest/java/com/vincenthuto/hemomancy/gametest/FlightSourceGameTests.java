package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.armor.ability.EdaciousBloodburstArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.util.FlightArbiter;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Step 9 (G-7): what each flight source grants and takes away. Pinned before {@code FlightArbiter} became the one
 * writer. Since G-7b a revoke keeps flight while another source still grants it.
 */
@GameTestHolder("combat_targeting_validation")
@PrefixGameTestTemplate(false)
public final class FlightSourceGameTests {
	private static final String NAMESPACE = "combat_targeting_validation";

	private FlightSourceGameTests() {
	}

	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "edacious_set_grants_slow_flight_and_takes_it_back", timeoutTicks = 60)
	public static void edaciousSetGrantsSlowFlightAndTakesItBack(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			try {
				wearEdacious(player, true);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "worn", true, false, 0.025F);
				wearEdacious(player, false);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "removed", false, false, 0.05F);
				h.succeed();
			} finally {
				captured.close();
			}
		});
	}

	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "silent_slipping_lifts_into_flight_and_releases", timeoutTicks = 60)
	public static void silentSlippingLiftsIntoFlightAndReleases(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			try {
				SilentArchonArmorAbilityHandler.activateSilentSlipping(player);
				expect(h, player, "slipping", true, true, 0.035F);
				SilentArchonArmorAbilityHandler.activateSilentSlipping(player);
				expect(h, player, "released", false, false, 0.05F);
				h.succeed();
			} finally {
				player.noPhysics = false;
				captured.close();
			}
		});
	}

	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "elytra_effect_hovers_airborne_and_grounds_on_landing", timeoutTicks = 60)
	public static void elytraEffectHoversAirborneAndGroundsOnLanding(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			CapturedPlayer creativeCapture = survivor(h);
			ServerPlayer creative = creativeCapture.player;
			try {
				player.getAbilities().setFlyingSpeed(0.07F);
				elytraTick(player, false);
				expect(h, player, "airborne", true, false, 0.07F);
				elytraTick(player, true);
				expect(h, player, "landed", false, false, 0.07F);

				creative.setGameMode(GameType.CREATIVE);
				creative.getAbilities().setFlyingSpeed(0.07F);
				elytraTick(creative, true);
				expect(h, creative, "creative landed", true, false, 0.05F);
				h.succeed();
			} finally {
				captured.close();
				creativeCapture.close();
			}
		});
	}

	/** G-7b: a landing under the Elytra effect keeps Edacious flight, because Edacious still grants it. */
	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "elytra_landing_keeps_edacious_flight", timeoutTicks = 60)
	public static void elytraLandingKeepsEdaciousFlight(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			try {
				wearEdacious(player, true);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				elytraTick(player, true);
				expect(h, player, "landed under both", true, false, 0.025F);
				wearEdacious(player, false);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "Edacious removed", false, false, 0.05F);
				h.succeed();
			} finally {
				captured.close();
			}
		});
	}

	/**
	 * G-7b: milking the Elytra effect in the air ends its grant, so a later Edacious revoke takes flight away instead
	 * of leaving a stale Elytra grant holding {@code mayfly}.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "elytra_effect_removed_in_the_air_ends_its_grant", timeoutTicks = 60)
	public static void elytraEffectRemovedInTheAirEndsItsGrant(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			try {
				elytraTick(player, false);
				expect(h, player, "airborne", true, false, 0.05F);
				player.removeEffect(EffectInit.noctifly_agaric);
				h.assertFalse(FlightArbiter.grants(player, FlightArbiter.Source.ELYTRA_EFFECT), "the milked Elytra effect still holds a grant");
				expect(h, player, "milked in the air", false, false, 0.05F);

				elytraTick(player, false);
				wearEdacious(player, true);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				player.removeEffect(EffectInit.noctifly_agaric);
				expect(h, player, "milked under Edacious", true, false, 0.025F);
				wearEdacious(player, false);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "Edacious removed after the milk", false, false, 0.05F);
				h.succeed();
			} finally {
				captured.close();
			}
		});
	}

	/**
	 * G-7b, every pair of sources: revoking one while the other still grants keeps flight at the other's speed, and
	 * revoking the second takes flight away.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = "small_room", batch = "flight_revoke_asks_every_other_source", timeoutTicks = 60)
	public static void flightRevokeAsksEveryOtherSource(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			CapturedPlayer captured = survivor(h);
			ServerPlayer player = captured.player;
			try {
				FlightArbiter.Source[] sources = FlightArbiter.Source.values();
				for (FlightArbiter.Source first : sources) {
					for (FlightArbiter.Source second : sources) {
						if (first == second) continue;
						player.getAbilities().setFlyingSpeed(0.07F);
						FlightArbiter.grant(player, first);
						FlightArbiter.grant(player, second);
						FlightArbiter.revoke(player, second);
						float kept = first == FlightArbiter.Source.ELYTRA_EFFECT ? speedOf(second) : speedOf(first);
						h.assertTrue(player.getAbilities().mayfly && player.getAbilities().getFlyingSpeed() == kept,
								second + " revoked under " + first + ": mayfly " + player.getAbilities().mayfly
										+ ", speed " + player.getAbilities().getFlyingSpeed());
						FlightArbiter.revoke(player, first);
						h.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying,
								first + " revoked last: mayfly " + player.getAbilities().mayfly);
					}
				}
				h.succeed();
			} finally {
				captured.close();
			}
		});
	}

	private static float speedOf(FlightArbiter.Source source) {
		return switch (source) {
			case EDACIOUS_SET -> 0.025F;
			case SILENT_SLIPPING -> 0.035F;
			case POSSESSION -> 0.04F;
			case ELYTRA_EFFECT -> 0.07F;
		};
	}

	/** A survival player outside the level, so no parallel test sees it. */
	private static CapturedPlayer survivor(GameTestHelper h) {
		CapturedPlayer captured = CapturedPlayer.create(h);
		captured.player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2.5D, 1.0D, 2.5D)));
		captured.player.setGameMode(GameType.SURVIVAL);
		return captured;
	}

	private static void wearEdacious(ServerPlayer player, boolean worn) {
		player.setItemSlot(EquipmentSlot.HEAD, worn ? new ItemStack(ItemInit.edacious_blood_lust_helm.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.CHEST, worn ? new ItemStack(ItemInit.edacious_blood_lust_chest.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.LEGS, worn ? new ItemStack(ItemInit.edacious_blood_lust_legs.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.FEET, worn ? new ItemStack(ItemInit.edacious_blood_lust_boots.get()) : ItemStack.EMPTY);
	}

	private static void elytraTick(ServerPlayer player, boolean onGround) {
		if (!player.hasEffect(EffectInit.noctifly_agaric)) player.addEffect(new MobEffectInstance(EffectInit.noctifly_agaric, 200));
		player.setOnGround(onGround);
		EffectInit.noctifly_agaric.value().applyEffectTick(player, 0);
	}

	private static void expect(GameTestHelper h, ServerPlayer player, String what, boolean mayfly, boolean flying, float speed) {
		var abilities = player.getAbilities();
		h.assertTrue(abilities.mayfly == mayfly && abilities.flying == flying && abilities.getFlyingSpeed() == speed,
				what + ": mayfly " + abilities.mayfly + ", flying " + abilities.flying + ", speed " + abilities.getFlyingSpeed());
	}
}
