package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.manips.PossessedRadialScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAbilities;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSceneRules.Sequence;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSequences;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTethers;
import com.vincenthuto.hemomancy.common.entity.PossessionEchoEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionWheelRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import com.vincenthuto.hemomancy.client.data.CrimsonFireClientState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionBodyRules;
import com.vincenthuto.hemomancy.common.entity.mob.facsimile.FacsimileBlazeEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.mixin.core.BlazeChargeInvoker;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Locale;
import java.util.UUID;

/**
 * With hemomancy.possessionReview (run possessionReviewClient), makes the reviewer a Silentium and possesses each of
 * the nine surrogates in turn: the possession wheel is opened over the Gardens and captured to
 * build/possession-client-review/screenshots, with the form's gift just used so its cooldown shows (the bat also holds
 * Still on). Track 4 then turns the camera to face the body: it checks the hitbox and eye height match the form on
 * both sides, captures the body idle and walking, and for the blaze and golem uses the gift again in view (the golem
 * with its Concentration drained so its crimson cracks show). A return pass chooses Return from the wheel, captures
 * the hold and checks the will came home; a last fauna pass stands a facsimile blaze (charged) and a hurt facsimile
 * golem in view. The form passes skip the departure and arrival at once, which also checks that a skip ends in the
 * state the scenes would have. Track 3's scene passes then play each cinematic for the wolf and the blaze (departure
 * and arrival, broken return, clean return) and capture their beats, and a last pass skips a beginning and a broken
 * return and checks the state. Logs POSSESSION_REVIEW PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PossessionClientReview {
	private static final int START = 200;
	private static final int STEP = 240;
	private static final SurrogateForm[] FORMS = SurrogateForm.values();
	private static int ticks;
	private static int step = -1;

	private PossessionClientReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.possessionReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("possession-client-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		// A save left dead by an earlier run would refuse every pass from the death screen.
		if (mc.player.isDeadOrDying()) {
			if (mc.player.tickCount % 20 == 0) mc.player.respawn();
			return;
		}
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		if (++ticks == 40) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
						"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "effect clear @s"})
					server.getCommands().performPrefixedCommand(source, cmd);
				var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
				degree.setDegreeNumber(8);
				degree.setArchonPath(EnumArchonPath.SILENTIUM);
				Hemomancy.LOGGER.info("POSSESSION_REVIEW staged");
			});
		}
		if (ticks < START) return;
		int index = (ticks - START) / STEP;
		int local = (ticks - START) % STEP;
		// One pass per form, a wolf pass that draws the will home from the wheel, the fauna facsimiles, then the scenes.
		if (index >= FORMS.length + 2 + SCENE_PASSES) {
			mc.stop();
			return;
		}
		if (index >= FORMS.length + 2) {
			scenes(mc, server, uuid, index, index - FORMS.length - 2, local);
			return;
		}
		if (index == FORMS.length + 1) {
			fauna(mc, server, uuid, index, local);
			return;
		}
		boolean returnPass = index == FORMS.length;
		SurrogateForm form = returnPass ? SurrogateForm.WOLF : FORMS[index];
		String name = returnPass ? "return" : form.name().toLowerCase(Locale.ROOT);
		if (local == 0 && step != index) {
			step = index;
			server.execute(() -> possess(server.getPlayerList().getPlayer(uuid), form));
		}
		if (local == 60) mc.setScreen(new PossessedRadialScreen(false));
		if (local == 100) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				if (returnPass) {
					PossessionAbilities.act(player, PossessionAction.RETURN);
					return;
				}
				for (PossessionAction action : PossessionWheelRules.body(form)) {
					if (!PossessionWheelRules.isToggle(action)) PossessionAbilities.act(player, action);
				}
				if (form == SurrogateForm.BAT) PossessionAbilities.act(player, PossessionAction.STILL);
			});
		}
		if (local == 110) {
			Screenshot.grab(mc.gameDirectory, "possession-" + index + "-" + name + "-wheel.png", mc.getMainRenderTarget(),
					message -> {});
			Hemomancy.LOGGER.info("POSSESSION_REVIEW captured {} {}", index, name);
		}
		if (!returnPass) body(mc, server, uuid, form, index, name, local);
		if (local == 100 + PossessionRules.RETURN_HOLD_TICKS + 15 && returnPass) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				boolean staged = PossessionSequences.sequence(player) == Sequence.RETURN_CLEAN;
				Hemomancy.LOGGER.info("POSSESSION_REVIEW {} return from the wheel starts the clean return", staged ? "PASS" : "FAIL");
				PossessionSequences.skip(player);
				boolean home = !HemoCapabilityAccess.isPossessing(player) && !PossessionService.inGardens(player.level());
				Hemomancy.LOGGER.info("POSSESSION_REVIEW {} return from the wheel brings the will home", home ? "PASS" : "FAIL");
			});
		}
	}

	/** Track 4: the body seen from the front, idle and walking, its room checked; the blaze fires, the golem slams. */
	private static void body(Minecraft mc, MinecraftServer server, UUID uuid, SurrogateForm form, int index,
			String name, int local) {
		if (local == 120) {
			mc.setScreen(null);
			mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
			PossessionBodyRules.Body expected = PossessionBodyRules.body(form);
			boolean client = near(mc.player.getBbWidth(), expected.width()) && near(mc.player.getBbHeight(), expected.height())
					&& near(mc.player.getEyeHeight(), expected.eyeHeight());
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				boolean serverSide = near(player.getBbWidth(), expected.width())
						&& near(player.getBbHeight(), expected.height()) && near(player.getEyeHeight(), expected.eyeHeight());
				Hemomancy.LOGGER.info("POSSESSION_REVIEW {} {} body room matches on both sides (client {}x{} eye {})",
						client && serverSide ? "PASS" : "FAIL", form, mc.player.getBbWidth(), mc.player.getBbHeight(),
						mc.player.getEyeHeight());
			});
		}
		if (local == 128) grab(mc, "possession-" + index + "-" + name + "-idle.png");
		if (local > 130 && local < 150) {
			Vec3 facing = Vec3.directionFromRotation(0f, mc.player.getYRot());
			mc.player.setDeltaMovement(facing.x * 0.2, mc.player.getDeltaMovement().y, facing.z * 0.2);
		}
		if (local == 148) grab(mc, "possession-" + index + "-" + name + "-moving.png");
		if (local == 170 && (form == SurrogateForm.BLAZE || form == SurrogateForm.GOLEM)) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				Possession possession = HemoCapabilityAccess.getPossession(player);
				if (form == SurrogateForm.GOLEM) {
					possession.setConcentration(possession.max() * 0.3f);
					possession.sync(player, true);
				}
				possession.setReadyAt(PossessionWheelRules.body(form).get(0), 0L);
				PossessionAbilities.act(player, PossessionWheelRules.body(form).get(0));
			});
		}
		if (local == 175 && (form == SurrogateForm.BLAZE || form == SurrogateForm.GOLEM)) {
			grab(mc, "possession-" + index + "-" + name + "-gift.png");
		}
		if (local == 200) mc.options.setCameraType(CameraType.FIRST_PERSON);
	}

	/** The Gardens' own blaze (charged, so its crimson fire shows) and a hurt golem, standing still in front of the reviewer. */
	private static void fauna(Minecraft mc, MinecraftServer server, UUID uuid, int index, int local) {
		if (local == 0 && step != index) {
			step = index;
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				var level = player.serverLevel();
				// An open stage in the air, so nothing of the terrain stands between the camera and the bodies.
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				// Easy, or peaceful would despawn the blaze; both stand without AI.
				for (String cmd : new String[]{"difficulty easy", "fill ~-6 ~30 ~-3 ~6 ~36 ~10 minecraft:air",
						"fill ~-6 ~29 ~-3 ~6 ~29 ~10 minecraft:polished_blackstone", "tp @s ~ ~30 ~ 0 10"})
					server.getCommands().performPrefixedCommand(source, cmd);
				Vec3 ahead = Vec3.directionFromRotation(0f, player.getYRot());
				for (int i = 0; i < 2; i++) {
					var type = i == 0 ? EntityInit.facsimile_blaze.get() : EntityInit.facsimile_golem.get();
					if (!(type.create(level) instanceof net.minecraft.world.entity.Mob mob)) continue;
					double side = i == 0 ? -1.5 : 1.5;
					mob.moveTo(player.getX() + ahead.x * 5 - ahead.z * side, player.getY(),
							player.getZ() + ahead.z * 5 + ahead.x * side, player.getYRot() + 180f, 0f);
					mob.setNoAi(true);
					if (mob instanceof FacsimileBlazeEntity blaze) ((BlazeChargeInvoker) blaze).hemomancy$setCharged(true);
					else mob.setHealth(mob.getMaxHealth() * 0.3f);
					level.addFreshEntity(mob);
				}
			});
		}
		if (local == 60) {
			grab(mc, "possession-" + index + "-fauna.png");
			var blazes = mc.level.getEntitiesOfClass(FacsimileBlazeEntity.class, mc.player.getBoundingBox().inflate(10));
			boolean crimson = !blazes.isEmpty() && blazes.stream().allMatch(CrimsonFireClientState::isActive);
			Hemomancy.LOGGER.info("POSSESSION_REVIEW {} the charged facsimile blaze burns crimson", crimson ? "PASS" : "FAIL");
		}
	}

	private static void grab(Minecraft mc, String file) {
		Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("POSSESSION_REVIEW captured {}", file);
	}

	private static boolean near(float a, float b) {
		return Math.abs(a - b) < 1.0E-3f;
	}

	/** Returns any earlier visit, then stands a fresh surrogate beside the reviewer and possesses it, skipping the scenes. */
	private static void possess(ServerPlayer player, SurrogateForm form) {
		possess(player, form, true);
	}

	private static boolean possess(ServerPlayer player, SurrogateForm form, boolean skip) {
		PossessionService.returnNow(player);
		player.removeEffect(EffectInit.mind_broken);
		var level = player.serverLevel();
		var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(form.surrogateId()));
		if (!(type.create(level) instanceof LivingEntity surrogate)) {
			Hemomancy.LOGGER.info("POSSESSION_REVIEW FAIL no surrogate for {}", form);
			return false;
		}
		surrogate.moveTo(player.getX() + 2.5, player.getY(), player.getZ(), 0f, 0f);
		if (surrogate instanceof ArmorStand stand) {
			stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
			stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		}
		level.addFreshEntity(surrogate);
		// The rite takes only a surrogate its caster holds with Blood Binding (an armor stand needs none).
		SurrogateTethers.bind(player, surrogate, 600);
		boolean began = PossessionService.begin(level, player, surrogate,
				new PossessionRules.Offerings(false, true, false, true));
		if (!skip) {
			Hemomancy.LOGGER.info("POSSESSION_REVIEW {} {} departure begins (refusal: {})", began ? "PASS" : "FAIL", form,
					began ? "none" : PossessionService.refusal(player, surrogate));
			return began;
		}
		PossessionSequences.skip(player);
		boolean right = began && HemoCapabilityAccess.possessionForm(player) == form && !PossessionSequences.isStaged(player)
				&& PossessionService.inGardens(player.level());
		Hemomancy.LOGGER.info("POSSESSION_REVIEW {} possessed {} (refusal: {})", right ? "PASS" : "FAIL", form,
				began ? "none" : PossessionService.refusal(player, surrogate));
		return right;
	}

	private static final SurrogateForm[] SCENE_FORMS = {SurrogateForm.WOLF, SurrogateForm.BLAZE};
	/** Per scene form: departure and arrival, a broken return, a clean return; then one skip pass. */
	private static final int SCENE_PASSES = SCENE_FORMS.length * 3 + 1;

	/** Track 3: each cinematic played out and captured at its beats, then a skip pass that checks the state. */
	private static void scenes(Minecraft mc, MinecraftServer server, UUID uuid, int index, int pass, int local) {
		if (pass == SCENE_PASSES - 1) {
			skips(server, uuid, index, local);
			return;
		}
		SurrogateForm form = SCENE_FORMS[pass / 3];
		String name = form.name().toLowerCase(Locale.ROOT);
		int kind = pass % 3;
		if (local == 0 && step != index) {
			step = index;
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				if (kind == 0) possess(player, form, false);
				else if (!HemoCapabilityAccess.isPossessing(player)) possess(player, form, true);
			});
		}
		if (kind == 0) {
			Sequence begin = Sequence.BEGIN;
			int[] beats = {20, PossessionSceneRules.DEPARTURE_DAEMON_START + 15, PossessionSceneRules.DEPARTURE_DAEMON_ARRIVES + 6,
					PossessionSceneRules.DEPARTURE_TENDRILS_CLOSE - 4, begin.cut() + 18,
					begin.cut() + PossessionSceneRules.ARRIVAL_SEIZE_END + 12, begin.cut() + PossessionSceneRules.ARRIVAL_INJECT_END + 6};
			capture(mc, beats, local, "possession-" + index + "-" + name + "-begin");
			if (local == begin.settle() + 10) {
				server.execute(() -> {
					ServerPlayer player = server.getPlayerList().getPlayer(uuid);
					boolean done = HemoCapabilityAccess.possessionForm(player) == form && !PossessionSequences.isStaged(player)
							&& PossessionService.inGardens(player.level());
					Hemomancy.LOGGER.info("POSSESSION_REVIEW {} {} departure and arrival hand over the body", done ? "PASS" : "FAIL", form);
				});
			}
			return;
		}
		boolean broken = kind == 1;
		Sequence back = Sequence.returning(broken);
		int start = 20;
		if (local == start) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				if (broken) PossessionService.eject(player, 200);
				else PossessionService.returnClean(player);
			});
		}
		int[] beats = broken
				? new int[]{start + 3, start + PossessionSceneRules.BROKEN_BURST + 4, start + 24, start + back.cut() + 16,
						start + back.cut() + PossessionSceneRules.BROKEN_BLAST + 1, start + back.cut() + PossessionSceneRules.BROKEN_SLAM + 2,
						start + back.cut() + PossessionSceneRules.BROKEN_REMANIFEST_END + 10}
				: new int[]{start + 10, start + PossessionSceneRules.CLEAN_COLLAPSE_END - 4, start + 42, start + back.cut() + 24,
						start + back.cut() + PossessionSceneRules.CLEAN_REFORM_END - 20, start + back.cut() + PossessionSceneRules.CLEAN_DRIFT_END - 2,
						start + back.cut() + PossessionSceneRules.CLEAN_REMANIFEST_END + 1};
		capture(mc, beats, local, "possession-" + index + "-" + name + (broken ? "-broken" : "-clean"));
		if (local == start + back.settle() + 10) {
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(uuid);
				boolean home = !HemoCapabilityAccess.isPossessing(player) && !PossessionSequences.isStaged(player)
						&& !PossessionService.inGardens(player.level());
				boolean dazed = player.hasEffect(EffectInit.mind_broken) == broken;
				boolean echoGone = player.serverLevel().getEntitiesOfClass(PossessionEchoEntity.class,
						player.getBoundingBox().inflate(8)).isEmpty();
				Hemomancy.LOGGER.info("POSSESSION_REVIEW {} {} {} return re-manifests the caster (home {}, mind broken {}, echo gone {})",
						home && dazed && echoGone ? "PASS" : "FAIL", form, broken ? "broken" : "clean", home, dazed, echoGone);
			});
		}
	}

	private static void capture(Minecraft mc, int[] beats, int local, String prefix) {
		for (int i = 0; i < beats.length; i++) {
			if (local == beats[i]) grab(mc, prefix + "-" + i + ".png");
		}
	}

	/** A skipped beginning and a skipped broken return end where letting them play ends. */
	private static void skips(MinecraftServer server, UUID uuid, int index, int local) {
		if (local != 0 || step == index) return;
		step = index;
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			PossessionService.returnNow(player);
			player.removeEffect(EffectInit.mind_broken);
			var loose = net.minecraft.world.entity.EntityType.WOLF.create(player.serverLevel());
			loose.moveTo(player.getX() + 2.5, player.getY(), player.getZ(), 0f, 0f);
			player.serverLevel().addFreshEntity(loose);
			var refusal = PossessionService.refusal(player, loose);
			boolean refused = !PossessionService.begin(player.serverLevel(), player, loose, PossessionRules.Offerings.NONE)
					&& refusal != null && refusal.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents key
					&& key.getKey().equals("message.hemomancy.possession.not_bound");
			Hemomancy.LOGGER.info("POSSESSION_REVIEW {} an unbound surrogate is refused", refused ? "PASS" : "FAIL");
			loose.discard();
			boolean began = possess(player, SurrogateForm.SPIDER, false);
			PossessionSequences.skip(player);
			boolean arrived = began && HemoCapabilityAccess.possessionForm(player) == SurrogateForm.SPIDER
					&& !PossessionSequences.isStaged(player) && PossessionService.inGardens(player.level())
					&& player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
							body -> body.getTags().contains(PossessionSequences.SCENE_BODY_TAG)).isEmpty();
			Hemomancy.LOGGER.info("POSSESSION_REVIEW {} a skipped beginning ends possessing, with no scene body left",
					arrived ? "PASS" : "FAIL");
			PossessionService.eject(player, 200);
			PossessionSequences.skip(player);
			boolean home = !HemoCapabilityAccess.isPossessing(player) && !PossessionSequences.isStaged(player)
					&& !PossessionService.inGardens(player.level()) && player.hasEffect(EffectInit.mind_broken)
					&& player.serverLevel().getEntitiesOfClass(PossessionEchoEntity.class, player.getBoundingBox().inflate(8)).isEmpty();
			Hemomancy.LOGGER.info("POSSESSION_REVIEW {} a skipped broken return ends home, Mind Broken, echo gone",
					home ? "PASS" : "FAIL");
		});
	}
}
