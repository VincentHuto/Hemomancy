package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteDaemonFinale;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** World rites end with the daemon striking the sky or slamming into the ground, through the real culmination. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WorldRiteFinaleGameTests {
	private WorldRiteFinaleGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "world_rite_finale")
	public static void theEclipseSendsTheDaemonIntoTheSky(GameTestHelper helper) {
		Flight flight = culminate(helper, "cardinal_rite/sanguine_eclipse");
		helper.assertTrue(flight.finale() == CardinalRiteDaemonFinale.SKY, "The Blood Moon rite strikes the sky");
		helper.assertTrue(flight.highest() >= flight.center().getY() + 40.0D,
				"The daemon must fly up into the sky; it reached " + (flight.highest() - flight.center().getY()));
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "world_rite_finale")
	public static void rootedVeinSlamsTheDaemonIntoTheGround(GameTestHelper helper) {
		Flight flight = culminate(helper, "cardinal_rite/rooted_vein");
		helper.assertTrue(flight.finale() == CardinalRiteDaemonFinale.GROUND, "Rooted Vein is a ground rite");
		helper.assertTrue(flight.highest() >= flight.center().getY() + 6.0D,
				"The daemon must climb before the slam; it reached " + (flight.highest() - flight.center().getY()));
		helper.assertTrue(flight.last() != null && flight.last().y <= flight.center().getY() + 1.6D,
				"The daemon must end slammed into the floor, at " + flight.last());
		helper.succeed();
	}

	private record Flight(CardinalRiteDaemonFinale finale, BlockPos center, double highest, Vec3 last) {
	}

	private static Flight culminate(GameTestHelper helper, String id) {
		ServerLevel level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer caster = player(helper);
		caster.setPos(Vec3.atBottomCenterOf(center.above()));
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc(id));
		helper.assertTrue(recipe != null, id + " must load");
		ActiveCardinalRite base = ActiveCardinalRite.interactive(caster.getUUID(), center, recipe.getId(), 200,
				recipe.getRiteType().getSize(), 3, false, 0);
		var tag = base.serialize(level.registryAccess());
		tag.putString("Phase", "CULMINATION");
		tag.putInt("PhaseTicks", 0);
		ActiveCardinalRite rite = ActiveCardinalRite.deserialize(tag, level.registryAccess());
		double highest = Double.NEGATIVE_INFINITY;
		Vec3 last = null;
		try {
			for (int tick = 0; tick < 300 && !rite.isComplete(); tick++) {
				CardinalRiteOrdealEngine.tick(level, caster, rite, recipe);
				HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, caster.getUUID(), center);
				if (daemon == null) continue;
				highest = Math.max(highest, daemon.getY());
				last = daemon.position();
			}
			helper.assertTrue(rite.isComplete(), id + " culmination must complete");
		} finally {
			HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, caster.getUUID(), center);
			if (daemon != null) daemon.discard();
			caster.discard();
		}
		return new Flight(recipe.getDaemonFinale(), center, highest, last);
	}

	private static ServerPlayer player(GameTestHelper helper) {
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "world-finale"), false);
		var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
				cookie.clientInformation());
		var connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}

			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet,
					net.minecraft.network.PacketSendListener listener) {
			}
		};
		return player;
	}
}
