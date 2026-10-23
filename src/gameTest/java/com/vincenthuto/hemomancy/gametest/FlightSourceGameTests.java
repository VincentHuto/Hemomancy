package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.armor.ability.EdaciousBloodburstArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
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
 * writer, including the pending conflict where the Elytra effect grounds an Edacious wearer until Edacious grants
 * flight again.
 */
@GameTestHolder("combat_targeting_validation")
@PrefixGameTestTemplate(false)
public final class FlightSourceGameTests {
	private static final String NAMESPACE = "combat_targeting_validation";

	private FlightSourceGameTests() {
	}

	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "flight_sources", timeoutTicks = 60)
	public static void edaciousSetGrantsSlowFlightAndTakesItBack(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer player = survivor(h);
			try {
				wearEdacious(player, true);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "worn", true, false, 0.025F);
				wearEdacious(player, false);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "removed", false, false, 0.05F);
				h.succeed();
			} finally {
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "flight_sources", timeoutTicks = 60)
	public static void silentSlippingLiftsIntoFlightAndReleases(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer player = survivor(h);
			try {
				SilentArchonArmorAbilityHandler.activateSilentSlipping(player);
				expect(h, player, "slipping", true, true, 0.035F);
				SilentArchonArmorAbilityHandler.activateSilentSlipping(player);
				expect(h, player, "released", false, false, 0.05F);
				h.succeed();
			} finally {
				player.noPhysics = false;
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "flight_sources", timeoutTicks = 60)
	public static void elytraEffectHoversAirborneAndGroundsOnLanding(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer player = survivor(h);
			ServerPlayer creative = survivor(h);
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
				player.discard();
				creative.discard();
			}
		});
	}

	/** Pending G-7: a landing under the Elytra effect drops Edacious flight until Edacious's next tick restores it. */
	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "flight_sources", timeoutTicks = 60)
	public static void elytraLandingDropsEdaciousFlightUntilItsNextTick(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer player = survivor(h);
			try {
				wearEdacious(player, true);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				elytraTick(player, true);
				expect(h, player, "landed under both", false, false, 0.025F);
				EdaciousBloodburstArmorAbilityHandler.updateEdaciousFlight(player);
				expect(h, player, "Edacious tick", true, false, 0.025F);
				h.succeed();
			} finally {
				player.discard();
			}
		});
	}

	private static ServerPlayer survivor(GameTestHelper h) {
		ServerPlayer player = DuctilisGameTests.player(h);
		player.setGameMode(GameType.SURVIVAL);
		return player;
	}

	private static void wearEdacious(ServerPlayer player, boolean worn) {
		player.setItemSlot(EquipmentSlot.HEAD, worn ? new ItemStack(ItemInit.edacious_blood_lust_helm.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.CHEST, worn ? new ItemStack(ItemInit.edacious_blood_lust_chest.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.LEGS, worn ? new ItemStack(ItemInit.edacious_blood_lust_legs.get()) : ItemStack.EMPTY);
		player.setItemSlot(EquipmentSlot.FEET, worn ? new ItemStack(ItemInit.edacious_blood_lust_boots.get()) : ItemStack.EMPTY);
	}

	private static void elytraTick(ServerPlayer player, boolean onGround) {
		if (!player.hasEffect(EffectInit.fungal_elytra)) player.addEffect(new MobEffectInstance(EffectInit.fungal_elytra, 200));
		player.setOnGround(onGround);
		EffectInit.fungal_elytra.value().applyEffectTick(player, 0);
	}

	private static void expect(GameTestHelper h, ServerPlayer player, String what, boolean mayfly, boolean flying, float speed) {
		var abilities = player.getAbilities();
		h.assertTrue(abilities.mayfly == mayfly && abilities.flying == flying && abilities.getFlyingSpeed() == speed,
				what + ": mayfly " + abilities.mayfly + ", flying " + abilities.flying + ", speed " + abilities.getFlyingSpeed());
	}
}
