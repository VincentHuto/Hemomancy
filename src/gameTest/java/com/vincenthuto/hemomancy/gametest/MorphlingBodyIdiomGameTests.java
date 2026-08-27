package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.EmberfangMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.WinterShroudMorphlingItem;
import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MorphlingBodyIdiomGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private MorphlingBodyIdiomGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hotheadedExtremeHeatAppliesTheFullTradeoff(GameTestHelper helper) {
		ServerPlayer player = player(helper, "hotheaded-test");
		try {
			ItemStack emberfang = maturity(ItemInit.morphling_emberfang.get().getDefaultInstance(), 30.0F);
			HemoCapabilityAccess.requireEquippedMorphling(player).setEquippedMorphling(emberfang);
			player.setRemainingFireTicks(100);

			EmberfangMorphlingItem.applyHotheadedTick(player, emberfang);

			double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
			double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
			helper.assertTrue(Math.abs(speed - 0.115D) < 0.000001D,
					"Extreme Hotheaded did not grant +15% movement speed: " + speed);
			helper.assertTrue(Math.abs(damage - 1.15D) < 0.000001D,
					"Extreme Hotheaded did not grant +15% attack damage: " + damage);
			helper.assertTrue(Math.abs(EmberfangMorphlingItem.scaleExhaustion(player, 1.0F) - 1.5F) < 0.000001F,
					"Extreme Hotheaded did not add 50% exhaustion");
			helper.assertTrue(Math.abs(EmberfangMorphlingItem.adjustIncomingDamage(player, emberfang, 10.0F) - 11.0F) < 0.000001F,
					"Extreme Hotheaded did not add 10% incoming damage");
			helper.succeed();
		} finally {
			EmberfangMorphlingItem.clearHeatModifiers(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void coldBloodedApexSeparatesEnvironmentalAndMagicalCold(GameTestHelper helper) {
		ServerPlayer player = player(helper, "cold-blooded-test");
		try {
			ItemStack shroud = maturity(ItemInit.morphling_winter_shroud.get().getDefaultInstance(), 100.0F);
			HemoCapabilityAccess.requireEquippedMorphling(player).setEquippedMorphling(shroud);
			player.isInPowderSnow = true;
			player.setTicksFrozen(120);

			WinterShroudMorphlingItem.applyColdBloodedTick(player, shroud);

			helper.assertTrue(player.getTicksFrozen() == 0,
					"Apex Cold-Blooded did not clear environmental freezing");
			helper.assertTrue(MixinHooks.canWalkOnPowderSnow(player, false),
					"Mature+ Winter Shroud could not traverse powder snow");
			float environmental = WinterShroudMorphlingItem.adjustIncomingColdDamage(player, shroud,
					helper.getLevel().damageSources().freeze(), 8.0F);
			player.isInPowderSnow = false;
			player.wasInPowderSnow = false;
			float magical = WinterShroudMorphlingItem.adjustIncomingColdDamage(player, shroud,
					helper.getLevel().damageSources().freeze(), 8.0F);
			helper.assertTrue(environmental == 0.0F,
					"Apex Cold-Blooded did not negate ordinary environmental freeze damage");
			helper.assertTrue(Math.abs(magical - 2.0F) < 0.000001F,
					"Apex Cold-Blooded erased or under-reduced magical cold: " + magical);
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static ItemStack maturity(ItemStack stack, float enzymePower) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putFloat("EnzymePower", enzymePower);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	private static ServerPlayer player(GameTestHelper helper, String name) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		return player;
	}
}
