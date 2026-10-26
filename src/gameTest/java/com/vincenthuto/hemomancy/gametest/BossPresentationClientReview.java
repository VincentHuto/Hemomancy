package com.vincenthuto.hemomancy.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameClientState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.bossPresentationReview: the boss bar frame gallery (every frame, each in its phases, fed through the
 * client's own boss overlay so the real renderer draws it; a hit taken mid-scene shows the chip), then the openings
 * that have no review of their own: the Naeglerophaeon gathering, Vesper's arrival, a Saint rising out of its
 * sarcophagus, and the Hemorath and Evening Star cards. Screenshots land in
 * build/boss-presentation-review/screenshots. Run bossPresentationReviewClient and drop
 * {"op":"create","name":"BossPresentationReview","seed":1} into operator/inbox after startup; -PbossPresentationReviewOnly
 * picks scenes. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BossPresentationClientReview {
	/** One bar in a gallery scene: its frame, title key, colour and progress. */
	private record Bar(ResourceLocation frame, String title, BossEvent.BossBarColor color, float progress) {
	}

	private record Scene(String name, int length, int[] shots, List<Bar> bars, BiConsumer<ServerPlayer, BlockPos> server) {
		Scene(String name, int length, int[] shots, List<Bar> bars) {
			this(name, length, shots, bars, null);
		}

		Scene(String name, int length, int[] shots, BiConsumer<ServerPlayer, BlockPos> server) {
			this(name, length, shots, List.of(), server);
		}
	}

	private static final int[] GALLERY_SHOTS = { 6, 14, 30, 40, 52 };
	private static final Scene[] SCENES = {
			new Scene("bars_annetta_arc", 60, GALLERY_SHOTS, List.of(
					new Bar(BossPresentationIds.FRAME_ANNETTA_KNOWLES, "entity.hemomancy.annetta_knowles", BossEvent.BossBarColor.PURPLE, 0.85F),
					new Bar(BossPresentationIds.FRAME_STAINED_PRIESTESS, "entity.hemomancy.stained_priestess", BossEvent.BossBarColor.RED, 0.7F),
					new Bar(BossPresentationIds.FRAME_STAINED_PRIESTESS, "entity.hemomancy.stained_priestess", BossEvent.BossBarColor.RED, 0.35F),
					new Bar(BossPresentationIds.FRAME_LATENT_INFECTION, "entity.hemomancy.latent_annetta_infection", BossEvent.BossBarColor.WHITE, 0.6F))),
			new Scene("bars_mycophant", 60, GALLERY_SHOTS, List.of(
					new Bar(BossPresentationIds.FRAME_MYCOPHANT, "entity.hemomancy.mycophant", BossEvent.BossBarColor.YELLOW, 0.9F),
					new Bar(BossPresentationIds.FRAME_MYCOPHANT, "entity.hemomancy.mycophant.joining", BossEvent.BossBarColor.YELLOW, 0.4F),
					new Bar(BossPresentationIds.FRAME_NAEGLEROPHAEON, "entity.hemomancy.naeglerophaeon", BossEvent.BossBarColor.RED, 0.8F),
					new Bar(BossPresentationIds.FRAME_NAEGLEROPHAEON, "entity.hemomancy.naeglerophaeon", BossEvent.BossBarColor.YELLOW, 0.08F))),
			new Scene("bars_vesper", 60, GALLERY_SHOTS, List.of(
					new Bar(BossPresentationIds.FRAME_VESPER_CROWNED_REFUSAL, "entity.hemomancy.vesper_crowned_refusal", BossEvent.BossBarColor.RED, 0.9F),
					new Bar(BossPresentationIds.FRAME_VESPER_CROWNED_REFUSAL, "entity.hemomancy.vesper_crowned_refusal", BossEvent.BossBarColor.RED, 0.3F),
					new Bar(BossPresentationIds.FRAME_VESPER_EVENING_STAR, "entity.hemomancy.vesper_evening_star", BossEvent.BossBarColor.RED, 0.5F),
					new Bar(BossPresentationIds.FRAME_VESPER_EVENING_STAR,
							"message.hemomancy.vesper_the_evening_star_entity.vesper_the_shamed_evening_star", BossEvent.BossBarColor.RED, 0.6F))),
			new Scene("bars_saints", 60, GALLERY_SHOTS, List.of(
					new Bar(BossPresentationIds.FRAME_HEMORATH, "message.hemomancy.hemorath.saint_hemorath_the_vessel_empties", BossEvent.BossBarColor.PURPLE, 0.4F),
					new Bar(BossPresentationIds.FRAME_PUTRICIEL, "entity.hemomancy.putriciel", BossEvent.BossBarColor.GREEN, 0.6F),
					new Bar(BossPresentationIds.FRAME_VELORUM, "entity.hemomancy.velorum", BossEvent.BossBarColor.BLUE, 0.2F),
					new Bar(BossPresentationIds.FRAME_SERAPHAE, "entity.hemomancy.seraphae", BossEvent.BossBarColor.YELLOW, 0.45F))),
			new Scene("open_naeglerophaeon", 110, new int[] { 6, 20, 34, 46, 58, 72, 90 }, (player, center) -> {
				NaeglerophaeonEntity boss = EntityInit.naeglerophaeon.get().create(player.serverLevel());
				boss.moveTo(center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 10.5, 180.0F, 0.0F);
				boss.setPersistenceRequired();
				boss.beginIntro();
				player.serverLevel().addFreshEntity(boss);
				BossScenes.playTo(player, boss, BossPresentationIds.SCENE_NAEGLEROPHAEON, 0);
			}),
			new Scene("open_vesper", 110, new int[] { 6, 22, 30, 38, 48, 60, 74, 90 }, (player, center) -> {
				VesperTheCrownedRefusalEntity boss = EntityInit.vesper_crowned_refusal.get().create(player.serverLevel());
				boss.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 12.5, 180.0F, 0.0F);
				boss.setPersistenceRequired();
				boss.beginArrival();
				player.serverLevel().addFreshEntity(boss);
				BossScenes.playTo(player, boss, BossPresentationIds.SCENE_VESPER_CROWNED_REFUSAL, 0);
			}),
			new Scene("open_saint", 100, new int[] { 6, 18, 30, 42, 50, 62, 76 }, (player, center) ->
					HarbingerSaintEncounterHooks.spawnSaintBoss(player.serverLevel(), center.offset(0, 0, 8), player,
							EnumSaintType.VELORUM)),
			new Scene("open_saint_seraphae", 100, new int[] { 6, 18, 30, 42, 50, 62, 76 }, (player, center) ->
					HarbingerSaintEncounterHooks.spawnSaintBoss(player.serverLevel(), center.offset(0, 0, 8), player,
							EnumSaintType.SERAPHAE)),
			new Scene("cards", 130, new int[] { 14, 30, 80, 100 }, (player, center) -> {
				var boss = EntityInit.vesper_evening_star.get().create(player.serverLevel());
				boss.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 8.5, 180.0F, 0.0F);
				boss.setNoAi(true);
				boss.setPersistenceRequired();
				player.serverLevel().addFreshEntity(boss);
				BossScenes.playTo(player, boss, BossPresentationIds.SCENE_VESPER_EVENING_STAR, 0);
			}),
	};

	private static int scene, tick, captured;
	private static boolean started;
	private static volatile boolean ready;
	private static BlockPos center;
	private static final List<ServerBossEvent> GALLERY = new ArrayList<>();

	private BossPresentationClientReview() {
	}

	private static boolean wanted(Scene scene) {
		String only = System.getProperty("hemomancy.bossPresentationReview.only", "");
		if (only.isBlank()) return true;
		for (String name : only.split(",")) if (scene.name().equals(name.trim())) return true;
		return false;
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.bossPresentationReview")) return;
		Minecraft client = Minecraft.getInstance();
		client.options.pauseOnLostFocus = false;
		if (client.player == null || client.getSingleplayerServer() == null) return;
		var server = client.getSingleplayerServer();
		var uuid = client.player.getUUID();
		try {
			if (scene == SCENES.length) {
				LogUtils.getLogger().info("BOSS_PRESENTATION_REVIEW captured {}", captured);
				client.stop();
				return;
			}
			if (!started && !wanted(SCENES[scene])) {
				scene++;
				return;
			}
			Scene current = SCENES[scene];
			if (!started) {
				started = true;
				ready = false;
				client.options.hideGui = false;
				clearGallery(client);
				BossSceneDirector.INSTANCE.clear();
				server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), current));
				return;
			}
			if (!ready) return;
			tick++;
			if (tick == 1) showGallery(client, current);
			// a hit mid-scene: the gallery's bars drop a tenth and the chip holds, then drains
			if (tick == 26) for (ServerBossEvent bar : GALLERY) {
				bar.setProgress(Math.max(0.0F, bar.getProgress() - 0.1F));
				client.gui.getBossOverlay().update(ClientboundBossEventPacket.createUpdateProgressPacket(bar));
			}
			for (int shot : current.shots()) {
				if (tick != shot) continue;
				Screenshot.grab(client.gameDirectory, "boss-" + current.name() + "-" + shot + ".png", client.getMainRenderTarget(),
						message -> LogUtils.getLogger().info("BOSS_PRESENTATION_SCREENSHOT {}", message.getString()));
				captured++;
			}
			if (tick > current.length()) {
				scene++;
				tick = 0;
				started = false;
			}
		} catch (Exception exception) {
			LogUtils.getLogger().error("Boss presentation review failed", exception);
			client.stop();
		}
	}

	private static void setup(ServerPlayer player, Scene scene) {
		ServerLevel level = player.serverLevel();
		if (center == null) center = player.blockPosition().offset(0, 0, 4);
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] { "gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
				"weather clear", "gamerule doMobSpawning false", "difficulty normal" })
			player.server.getCommands().performPrefixedCommand(source, cmd);
		level.getEntities((Entity) null, new AABB(center).inflate(40), e -> !(e instanceof net.minecraft.world.entity.player.Player))
				.forEach(Entity::discard);
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-24, -1, -12), center.offset(24, 14, 30)))
			level.setBlock(pos, pos.getY() == center.getY() - 1 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		player.teleportTo(level, center.getX() + 0.5, center.getY(), center.getZ() - 2.5, 0.0F, 6.0F);
		if (scene.server() != null) scene.server().accept(player, center);
		LogUtils.getLogger().info("BOSS_PRESENTATION_REVIEW {}", scene.name());
		ready = true;
	}

	private static void showGallery(Minecraft client, Scene scene) {
		for (Bar bar : scene.bars()) {
			ServerBossEvent event = new ServerBossEvent(Component.translatable(bar.title()), bar.color(),
					BossEvent.BossBarOverlay.PROGRESS);
			event.setProgress(bar.progress());
			BossBarFrameClientState.bind(event.getId(), bar.frame());
			client.gui.getBossOverlay().update(ClientboundBossEventPacket.createAddPacket(event));
			GALLERY.add(event);
		}
	}

	private static void clearGallery(Minecraft client) {
		for (ServerBossEvent bar : GALLERY)
			client.gui.getBossOverlay().update(ClientboundBossEventPacket.createRemovePacket(bar.getId()));
		GALLERY.clear();
	}
}
