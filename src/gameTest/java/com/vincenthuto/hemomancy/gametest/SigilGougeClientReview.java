package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.data.SigilGougeClientState;
import com.vincenthuto.hemomancy.client.render.world.IchorianSigilNodeTendrilGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeStages;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteSync;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;

/**
 * With hemomancy.sigilGougeReview (run runSigilGougeReviewClient, then drop {"op":"create","name":"SigilGouge","seed":"1"}
 * into the operator inbox), lays a grass platform in the sky and starts a Sanctified Rite in inscription over it for an
 * absent caster, so the rite holds still and only its sync runs. The two support sigils are fed node by node through
 * the handler's own offer path (so the real awakening, beat, living form and sync run): the Bastion on flat ground, the
 * Reservoir across a one-block step. Captures open, completing, awake, low angle, stepped, night and both sigils to
 * build/sigil-gouge-client-review/screenshots, logs SIGIL_GOUGE_REVIEW PASS/FAIL per check and stops. Excluded from
 * release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SigilGougeClientReview {
	private static final String PROPERTY = "hemomancy.sigilGougeReview";
	private static final BlockPos CENTER = new BlockPos(0, 150, 0);
	private static final double SURFACE = CENTER.getY() + 1.0D;
	private static final UUID CASTER = UUID.nameUUIDFromBytes("hemomancy:sigil_gouge_review".getBytes());
	private static final ResourceLocation RECIPE = Hemomancy.rloc("cardinal_rite/sanctified_rite");
	private static final ResourceLocation BASTION = Hemomancy.rloc("bastion");
	private static final ResourceLocation RESERVOIR = Hemomancy.rloc("reservoir");
	private static final int STAGE = 40;
	private static final int BUILD = 100;
	private static final int RITE = 130;
	/** The Bastion's first four nodes (three links), one a tick. */
	private static final int FEED_OPEN = 150;
	/** Its fifth node, so one link is mid cut-in when the first still is taken. */
	private static final int FEED_CUTTING = 200;
	/** Its sixth and last node: the pattern closes and the form peels up. */
	private static final int FEED_CLOSE = 240;
	/** The Reservoir, all four nodes, across the step. */
	private static final int FEED_STEPPED = 280;
	/** Each shot: name, client tick, eye offset from its sigil, look offset from its sigil, sigil, night. */
	private static final Shot[] SHOTS = {
			new Shot("1-open_flat", 206, 0.0, 2.7, 2.5, 0.0, 0.0, 0.0, BASTION, false),
			new Shot("2-completing", 260, 0.0, 2.9, 3.1, 0.0, 0.2, 0.0, BASTION, false),
			new Shot("3-awake_flat", 480, 0.0, 2.4, 4.4, 0.0, 1.0, 0.0, BASTION, false),
			new Shot("4-awake_low_angle", 520, 0.5, 0.45, 3.3, 0.0, 0.25, 0.0, BASTION, false),
			new Shot("5-awake_stepped", 560, 2.6, 2.4, 2.6, -0.3, 0.5, 0.0, RESERVOIR, false),
			new Shot("6-awake_night", 610, 0.0, 2.4, 4.4, 0.0, 1.0, 0.0, BASTION, true),
			new Shot("7-two_sigils", 660, -6.0, 6.5, 9.0, -6.0, 0.8, 0.0, BASTION, false),
			new Shot("8-form_close", 700, 1.4, 2.7, 1.9, 0.0, 2.2, 0.0, BASTION, false),
			new Shot("9-form_close_reservoir", 740, -1.3, 2.4, 1.6, -0.3, 2.0, 0.0, RESERVOIR, false),
	};
	/** Each shot is framed this long before it is taken: the client hears the day time once a second. */
	private static final int FRAME_LEAD = 30;
	private static final int DONE = 770;
	private static int ticks;
	/** Each sigil's resolved placement offset from the centre, set on the server thread at {@link #RITE}. */
	private static volatile BlockPos bastion;
	private static volatile BlockPos reservoir;

	private SigilGougeClientReview() {}

	private record Shot(String name, int at, double eyeX, double eyeY, double eyeZ, double lookX, double lookY,
			double lookZ, ResourceLocation sigil, boolean night) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("sigil-gouge-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (ticks == STAGE) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		if (ticks == BUILD) server.execute(() -> build(server.getPlayerList().getPlayer(uuid)));
		if (ticks == RITE) server.execute(() -> startRite(server.getPlayerList().getPlayer(uuid)));
		// The caster is absent; keep the rite from guttering out while it is filmed.
		if (ticks > RITE) server.execute(() -> {
			ActiveCardinalRite rite = CardinalRiteSavedData.get(server.overworld()).getRite(CASTER);
			if (rite != null) rite.setDisconnectTicks(0);
		});
		for (int node = 0; node < 4; node++) {
			if (ticks == FEED_OPEN + node) feed(server, uuid, BASTION, node);
			if (ticks == FEED_STEPPED + node) feed(server, uuid, RESERVOIR, node);
		}
		if (ticks == FEED_CUTTING) feed(server, uuid, BASTION, 4);
		if (ticks == FEED_CLOSE) feed(server, uuid, BASTION, 5);
		if (ticks == FEED_CLOSE + 4) reportClientSigils(mc, "after the Bastion closes", 1);
		if (ticks == FEED_STEPPED + 30) {
			reportClientSigils(mc, "after the Reservoir closes", 2);
			server.execute(() -> reportForms(server.overworld()));
		}
		for (Shot shot : SHOTS) {
			if (ticks == shot.at() - FRAME_LEAD) server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), shot));
			if (ticks == shot.at()) capture(mc, "sigil-gouge-" + shot.name() + ".png");
		}
		if (ticks == SHOTS[SHOTS.length - 1].at() + 1) reportTurns(mc);
		if (ticks == SHOTS[2].at()) reportTendrils(mc);
		if (ticks == DONE) {
			server.execute(() -> CardinalRiteSavedData.get(server.overworld()).removeRite(CASTER));
			Hemomancy.LOGGER.info("SIGIL_GOUGE_REVIEW captured {}", SHOTS.length);
			mc.stop();
		}
	}

	/** Clear weather, no mobs, a still noon, and the platform's chunks force-loaded. */
	private static void stage(ServerPlayer player) {
		if (player == null) return;
		run(player, "gamemode spectator", "difficulty peaceful", "gamerule doMobSpawning false",
				"gamerule doDaylightCycle false", "gamerule doWeatherCycle false", "weather clear", "time set noon",
				"forceload add -32 -32 32 32");
		player.teleportTo(player.serverLevel(), 0.5, 160.0, 8.5, 180f, 40f);
		Hemomancy.LOGGER.info("SIGIL_GOUGE_REVIEW staged");
	}

	/** A stone and grass platform at the centre's height, with a one-block step across the Reservoir's socket. */
	private static void build(ServerPlayer player) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-24, -3, -24), CENTER.offset(24, 6, 24))) {
			Block block = pos.getY() < CENTER.getY() ? Blocks.STONE
					: pos.getY() == CENTER.getY() ? Blocks.GRASS_BLOCK : Blocks.AIR;
			level.setBlock(pos, block.defaultBlockState(), 2);
		}
		Hemomancy.LOGGER.info("SIGIL_GOUGE_REVIEW platform laid");
	}

	/**
	 * A Sanctified Rite in inscription for an absent caster: its anchors full, its two support sigils unfed. Resolves
	 * the sockets the way the handler does, then raises the ground west of the Reservoir's centre column by a block so
	 * that sigil's links cross the step.
	 */
	private static void startRite(ServerPlayer player) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, RECIPE);
		if (recipe == null || recipe.getCeremony() == null) {
			report(false, "the Sanctified Rite recipe is loaded");
			return;
		}
		Set<BlockPos> occupied = new HashSet<>();
		for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
		for (var socket : recipe.getCeremony().supportSockets()) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(Hemomancy.rloc(socket.suggestedSigil()));
			BlockPos resolved = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
					new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
			occupied.addAll(CardinalRiteSigilPlacementRules.footprint(resolved, sigil.nodes()));
			if (sigil.id().equals(BASTION)) bastion = resolved;
			if (sigil.id().equals(RESERVOIR)) reservoir = resolved;
		}
		if (bastion == null || reservoir == null) {
			report(false, "the Sanctified Rite holds a Bastion and a Reservoir socket");
			return;
		}
		for (BlockPos pos : BlockPos.betweenClosed(
				CENTER.offset(reservoir.getX() - 6, 1, reservoir.getZ() - 4),
				CENTER.offset(reservoir.getX() - 1, 1, reservoir.getZ() + 4))) {
			level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
		}
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(CASTER, CENTER, RECIPE, 36000, 9, 7, false, 0,
				recipe.getCeremony().anchors().size());
		for (int i = 0; i < recipe.getCeremony().anchors().size(); i++) rite.fillAnchor(i, 100_000);
		boolean inscribing = rite.enterInscription();
		CardinalRiteSavedData.get(level).startRite(rite);
		CardinalRiteSync.syncActiveRites(level);
		report(inscribing, "the review rite is in inscription (bastion " + bastion + ", reservoir " + reservoir + ")");
	}

	/** Offers one node's worth of blood to {@code sigilId}'s node {@code node} through the handler's own path. */
	private static void feed(MinecraftServer server, UUID uuid, ResourceLocation sigilId, int node) {
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player == null) return;
			ServerLevel level = player.serverLevel();
			ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(CASTER);
			BlockPos placement = sigilId.equals(BASTION) ? bastion : reservoir;
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(sigilId);
			if (rite == null || placement == null || sigil == null) {
				report(false, "node " + node + " of " + sigilId + " could be fed");
				return;
			}
			IchorianSigilDefinition.Node at = sigil.nodes().get(node);
			BlockPos target = CardinalRiteSigilRules.surfaceAirPosition(level, CENTER,
					placement.getX() + (int) Math.round(at.x()), placement.getZ() + (int) Math.round(at.z()));
			try {
				offer().invoke(null, level, player, rite, target, 50.0D, (IntUnaryOperator) requested -> requested);
			} catch (ReflectiveOperationException exception) {
				Hemomancy.LOGGER.error("SIGIL_GOUGE_REVIEW could not offer blood to {}", sigilId, exception);
				return;
			}
			CardinalRiteSync.syncActiveRites(level);
			int done = rite.getSigilProgress().getOrDefault(sigilId.toString(), 0);
			report(done == node + 1, sigilId + " has " + done + " of " + sigil.nodes().size() + " nodes after feeding node "
					+ node + (rite.isSigilAwakened(sigilId.toString()) ? " (awake)" : ""));
		});
	}

	private static Method offer() throws NoSuchMethodException {
		Method method = CardinalRiteInteractionHandler.class.getDeclaredMethod("tryOfferSigilBlood", ServerLevel.class,
				ServerPlayer.class, ActiveCardinalRite.class, BlockPos.class, double.class, IntUnaryOperator.class);
		method.setAccessible(true);
		return method;
	}

	/** The client has the awakened sigils' links, flagged awake, not dropped on awakening. */
	private static void reportClientSigils(Minecraft mc, String when, int expectedAwake) {
		List<ActiveRiteData.SigilSegment> segments = ActiveRiteClientData.getActiveRites().stream()
				.filter(entry -> entry.getCenter().equals(CENTER))
				.flatMap(entry -> entry.getSigilSegments().stream()).toList();
		long awake = segments.stream().filter(ActiveRiteData.SigilSegment::awakened)
				.map(ActiveRiteData.SigilSegment::sigilKey).distinct().count();
		report(awake == expectedAwake, when + ": the client holds " + segments.size() + " links, " + awake
				+ " sigil(s) awake");
	}

	/** Each awakened form holds its hover point over its own pattern. */
	private static void reportForms(ServerLevel level) {
		List<AwakenedIchorianSigilEntity> forms = level.getEntitiesOfClass(AwakenedIchorianSigilEntity.class,
				new AABB(CENTER).inflate(24.0D));
		report(forms.size() == 2, forms.size() + " living forms stand over the rite");
		for (AwakenedIchorianSigilEntity form : forms) {
			BlockPos placement = form.getSigilId().equals(BASTION) ? bastion : reservoir;
			double dx = form.getX() - (CENTER.getX() + placement.getX() + 0.5D);
			double dz = form.getZ() - (CENTER.getZ() + placement.getZ() + 0.5D);
			report(Math.hypot(dx, dz) < 1.0D, form.getSigilId() + " hovers " + String.format("%.2f", Math.hypot(dx, dz))
					+ " blocks off its pattern's centre at height " + String.format("%.2f", form.getY() - SURFACE));
		}
	}

	/** The Bastion's node tendrils have risen: two a node, every one stopping short of the hover point. */
	private static void reportTendrils(Minecraft mc) {
		if (mc.level == null) return;
		List<ActiveRiteData.SigilSegment> segments = ActiveRiteClientData.getActiveRites().stream()
				.filter(entry -> entry.getCenter().equals(CENTER))
				.flatMap(entry -> entry.getSigilSegments().stream())
				.filter(segment -> segment.sigilKey().equals(BASTION.toString())).toList();
		float time = mc.level.getGameTime();
		float rise = IchorianSigilGougeStages.tendrilRise(SigilGougeClientState.awakeAge(CENTER, BASTION.toString(), time));
		List<Vec3> nodes = IchorianSigilNodeTendrilGeometry.nodes(segments);
		Vec3 hover = IchorianSigilNodeTendrilGeometry.hoverPoint(nodes);
		var strands = IchorianSigilNodeTendrilGeometry.strands(nodes, hover, 1L, time, rise);
		double tipHeight = strands.isEmpty() ? 0.0D
				: strands.getFirst().joints().getLast().center().y - strands.getFirst().joints().getFirst().center().y;
		report(strands.size() == nodes.size() * IchorianSigilNodeTendrilGeometry.TENDRILS_PER_NODE && rise > 0.99F,
				"the Bastion's tendrils have risen: " + strands.size() + " strands over " + nodes.size()
						+ " nodes, rise " + rise + ", the first climbing " + String.format("%.2f", tipHeight) + " blocks");
	}

	/** Two awake forms do not turn in step. */
	private static void reportTurns(Minecraft mc) {
		if (mc.level == null) return;
		List<Float> yaws = new java.util.ArrayList<>();
		for (var entity : mc.level.entitiesForRendering()) {
			if (entity instanceof AwakenedIchorianSigilEntity form) yaws.add(form.getRenderFacingYaw(0.0F));
		}
		report(yaws.size() == 2 && Math.abs(net.minecraft.util.Mth.wrapDegrees(yaws.get(0) - yaws.get(1))) > 5.0F,
				"the two forms face apart " + yaws);
	}

	private static void run(ServerPlayer player, String... commands) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : commands) player.server.getCommands().performPrefixedCommand(source, command);
	}

	private static void frame(ServerPlayer player, Shot shot) {
		BlockPos placement = shot.sigil().equals(BASTION) ? bastion : reservoir;
		if (player == null || placement == null) return;
		run(player, shot.night() ? "time set midnight" : "time set noon");
		double sx = CENTER.getX() + placement.getX() + 0.5D;
		double sz = CENTER.getZ() + placement.getZ() + 0.5D;
		double x = sx + shot.eyeX(), y = SURFACE + shot.eyeY(), z = sz + shot.eyeZ();
		double fx = sx + shot.lookX(), fy = SURFACE + shot.lookY(), fz = sz + shot.lookZ();
		float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
		float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
		player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
	}

	private static void capture(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("SIGIL_GOUGE_REVIEW captured {}", name);
	}

	private static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("SIGIL_GOUGE_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}
