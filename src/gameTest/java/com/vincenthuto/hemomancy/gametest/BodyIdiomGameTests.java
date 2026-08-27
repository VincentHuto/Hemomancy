package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BodyIdiomEvents;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BodyIdiomGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private BodyIdiomGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void ironheartedFormsAHeartAndIronHeartsTakeDamageFirst(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		try {
			ManipulationInit.ironhearted.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			var state = HemoCapabilityAccess.getPowerGuardrails(player);
			helper.assertTrue(state.getIronHeartHealth() == 2.0F, "Charged action did not form one Iron Heart");
			LivingIncomingDamageEvent event = damage(player, helper.getLevel().damageSources().generic(), 3.0F);
			BodyIdiomEvents.onIncomingDamage(event);
			helper.assertTrue(Math.abs(event.getAmount() - 1.0F) < 0.001F,
					"Iron Heart did not absorb damage before health: " + event.getAmount());
			helper.assertTrue(state.getIronHeartHealth() == 0.0F, "Spent Iron Heart remained on the body");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void equippedBlackheartedConvertsWitherDamage(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		try {
			HemoCapabilityAccess.getKnownManipulations(player).orElseThrow()
					.setEquippedManipNames(List.of("blackhearted"));
			player.setHealth(10.0F);
			LivingIncomingDamageEvent event = damage(player, helper.getLevel().damageSources().wither(), 4.0F);
			BodyIdiomEvents.onIncomingDamage(event);
			var state = HemoCapabilityAccess.getPowerGuardrails(player);
			helper.assertTrue(Math.abs(event.getAmount() - 1.4F) < 0.001F,
					"Blackhearted did not prevent the expected Wither share: " + event.getAmount());
			helper.assertTrue(Math.abs(player.getHealth() - 11.3F) < 0.01F,
					"Blackhearted did not heal from prevented Wither: " + player.getHealth());
			helper.assertTrue(Math.abs(state.getNecroticSaturation() - 2.6F) < 0.001F,
					"Prevented Wither did not fill necrotic saturation");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static LivingIncomingDamageEvent damage(ServerPlayer player,
			net.minecraft.world.damagesource.DamageSource source, float amount) {
		return new LivingIncomingDamageEvent(player, new DamageContainer(source, amount));
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "body-idiom-test"), false);
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
