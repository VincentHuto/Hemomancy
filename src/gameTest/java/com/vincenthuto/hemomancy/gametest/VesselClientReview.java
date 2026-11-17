package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.BroodRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenBrood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.vesselReview, stages core-loop round two P6 in a closed room of a disposable world, captures it into
 * build/vessel-client-review/screenshots and stops. A real survival player at the Third Degree: a blaze vat's screen
 * unread, read and studied; a husk's screen and a preserved jar's; a pair of cows gestating and a lone pig's refused
 * brood button; two cow vats linked by the brood's thread beside an empty one, and the calf just born in it; the Living
 * Bestiary with a met-unread kind and a studied one; and a Living Syringe drawing a vat. Run with vesselReviewClient and
 * drop {"op":"create","name":"VesselReview","seed":"1"} into operator/inbox after startup ({"op":"open",...} after).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VesselClientReview {
	private static final String PROPERTY = "hemomancy.vesselReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 90;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/** What the client does during a shot, past opening the server's scene. */
	private enum Client { NONE, HOVER_BUTTON, BESTIARY, BESTIARY_SCROLLED, USE }

	private record Shot(String name, Supplier<Item> held, Vec3 stand, Vec3 aim, boolean thirdPerson, Client client,
			int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 FRONT = new Vec3(0.5, 0, 0.6);
	private static final Vec3 AT_ROW = new Vec3(0.5, 1.2, 4.5);

	private static final List<Shot> SHOTS = List.of(
			new Shot("vat-unread", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					(level, player) -> blazeScreen(level, player, 0)),
			new Shot("vat-read", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					(level, player) -> blazeScreen(level, player, 1)),
			new Shot("vat-studied", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					(level, player) -> blazeScreen(level, player, 3)),
			new Shot("husk-screen", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					VesselClientReview::huskScreen),
			new Shot("preserved-jar", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					VesselClientReview::preservedScreen),
			new Shot("brood-gestating", () -> Items.AIR, FRONT, AT_ROW, false, Client.NONE, new int[]{40},
					(level, player) -> broodScreen(level, player, 60)),
			new Shot("brood-refused", () -> Items.AIR, FRONT, AT_ROW, false, Client.HOVER_BUTTON, new int[]{45},
					VesselClientReview::refusedScreen),
			new Shot("thread", () -> Items.AIR, new Vec3(2.0, 0, -0.5), new Vec3(0.0, 1.8, 4.5), false, Client.NONE,
					new int[]{30, 60}, (level, player) -> threadScene(level, player, false)),
			new Shot("thread-side", () -> Items.AIR, new Vec3(-3.5, 0, 0.5), new Vec3(-0.5, 2.2, 4.5), false, Client.NONE,
					new int[]{40}, (level, player) -> threadScene(level, player, false)),
			new Shot("birth", () -> Items.AIR, new Vec3(2.0, 0, -0.5), new Vec3(0.5, 1.6, 4.5), false, Client.NONE,
					new int[]{6, 14, 40}, (level, player) -> threadScene(level, player, true)),
			new Shot("bestiary-met", () -> Items.AIR, FRONT, AT_ROW, false, Client.BESTIARY, new int[]{50},
					(level, player) -> bestiary(level, player, false)),
			new Shot("bestiary-studied", () -> Items.AIR, FRONT, AT_ROW, false, Client.BESTIARY, new int[]{50},
					(level, player) -> bestiary(level, player, true)),
			new Shot("bestiary-studied-notes", () -> Items.AIR, FRONT, AT_ROW, false, Client.BESTIARY_SCROLLED, new int[]{50},
					(level, player) -> bestiary(level, player, true)),
			new Shot("syringe", ItemInit.living_syringe::get, new Vec3(-2.5, 0, 2.6), new Vec3(-2.5, 1.0, 4.5), false,
					Client.USE, new int[]{20, 40}, VesselClientReview::syringeScene));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("vessel-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.getToasts().clear();
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("VESSEL_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			if (mc.screen != null) mc.player.closeContainer();
			if (mc.screen != null) mc.setScreen(null);
			mc.options.setCameraType(current.thirdPerson() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			mc.gui.getChat().clearMessages(false);
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (current.client() == Client.HOVER_BUTTON && local >= 20) hoverButton(mc);
		boolean bestiary = current.client() == Client.BESTIARY || current.client() == Client.BESTIARY_SCROLLED;
		if (bestiary && local == 16) openBestiary(mc);
		if (bestiary && local >= 20) moveMouse(mc, 2, 2);
		// Scroll the dossier down to the yield and notes, over the detail panel.
		if (current.client() == Client.BESTIARY_SCROLLED && local == 30 && mc.screen != null) {
			var window = mc.getWindow();
			mc.screen.mouseScrolled(window.getGuiScaledWidth() * 0.72D, window.getGuiScaledHeight() * 0.6D, 0.0D, -6.0D);
		}
		if (current.client() == Client.USE && local == PRESS_TICK) KeyMapping.click(mc.options.keyUse.getKey());
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "vessel-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("VESSEL_REVIEW shot {} {} at {} screen={}", shot, current.name(), at,
					mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
		}
		if (local == SHOT_TICKS - 1) shot++;
	}

	/** The brood button's middle on the open vessel screen. */
	private static void hoverButton(Minecraft mc) {
		if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
		moveMouse(mc, screen.getGuiLeft() + 300 - 10 - 42, screen.getGuiTop() + 194 + 12 + 8);
	}

	private static void moveMouse(Minecraft mc, double guiX, double guiY) {
		var window = mc.getWindow();
		double x = guiX * (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double y = guiY * (double) window.getScreenHeight() / window.getGuiScaledHeight();
		try {
			Field xpos = MouseHandler.class.getDeclaredField("xpos");
			Field ypos = MouseHandler.class.getDeclaredField("ypos");
			xpos.setAccessible(true);
			ypos.setAccessible(true);
			xpos.setDouble(mc.mouseHandler, x);
			ypos.setDouble(mc.mouseHandler, y);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.warn("VESSEL_REVIEW cannot move the cursor", e);
		}
	}

	/** The Progress screen on its Bestiary tab; the first met creature is selected by default. */
	private static void openBestiary(Minecraft mc) {
		HarbingerProgressScreen screen = new HarbingerProgressScreen();
		mc.setScreen(screen);
		try {
			Class<?> tab = null;
			for (Class<?> inner : HarbingerProgressScreen.class.getDeclaredClasses()) {
				if (inner.isEnum() && inner.getSimpleName().equals("Tab")) tab = inner;
			}
			Object bestiary = null;
			for (Object constant : tab.getEnumConstants()) {
				if (((Enum<?>) constant).name().equals("BESTIARY")) bestiary = constant;
			}
			Method switchTab = HarbingerProgressScreen.class.getDeclaredMethod("switchTab", tab);
			switchTab.setAccessible(true);
			switchTab.invoke(screen, bestiary);
		} catch (ReflectiveOperationException | RuntimeException e) {
			Hemomancy.LOGGER.warn("VESSEL_REVIEW cannot open the Bestiary tab", e);
		}
	}

	/** A closed room of deepslate at night, active blood, the Third Degree and no research. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 18000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		ServerLevel level = player.serverLevel();
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-10, -2, -4), row.offset(10, 8, 12))) {
			boolean shell = pos.getY() <= row.getY() - 1 || pos.getY() == row.getY() + 8 || pos.getX() == row.getX() - 10
					|| pos.getX() == row.getX() + 10 || pos.getZ() == row.getZ() - 4 || pos.getZ() == row.getZ() + 12;
			level.setBlock(pos, !shell ? Blocks.AIR.defaultBlockState() : pos.getY() <= row.getY() - 1
					? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		com.vincenthuto.hemomancy.common.capability.PlayerStateSync.Entry.DEGREE.send(player);
		player.getInventory().clearContent();
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>());
		known.setSelectedManip(BloodManipulation.BLANK);
		KnownManipulationEvents.syncPlayerEvent(player);
		forgetResearch(player);
		Hemomancy.LOGGER.info("VESSEL_REVIEW staged at {}", row);
	}

	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.removeEffect(MobEffects.NIGHT_VISION);
		player.setHealth(player.getMaxHealth());
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		shot.scene().accept(level, player);
	}

	// ── Scenes ──

	private static void clearRoom(ServerLevel level) {
		for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(row).inflate(16.0D))) MenagerieLeash.release(mob, false);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-9, 0, -3), row.offset(9, 7, 11))) {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
		}
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(16.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		for (int x = -8; x <= 8; x += 4) {
			level.setBlock(row.offset(x, 6, 5), Blocks.LIGHT.defaultBlockState(), 3);
			level.setBlock(row.offset(x, 3, 1), Blocks.LIGHT.defaultBlockState(), 3);
		}
	}

	private static SpecimenVatBlockEntity vat(ServerLevel level, ServerPlayer owner, BlockPos base, EntityType<? extends Mob> type,
			double vigor, double buffer) {
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState().setValue(SpecimenVatBlock.FACING, Direction.NORTH);
		level.setBlock(base, lower, 3);
		level.setBlock(base.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		SpecimenVatBlockEntity vat = (SpecimenVatBlockEntity) level.getBlockEntity(base);
		if (owner != null) vat.buffer().setOwner(owner.getUUID());
		if (type != null) {
			Mob mob = type.create(level);
			vat.kept().keep(SpecimenJarData.captureEntity(mob), mob.getMaxHealth(), vigor, level.getGameTime());
			mob.discard();
		}
		vat.buffer().setBloodVolume(buffer);
		vat.sendUpdates();
		return vat;
	}

	private static void forgetResearch(ServerPlayer player) {
		HemoCapabilityAccess.clinicalBlood(player).deserializeNBT(player.registryAccess(), new CompoundTag());
		ClinicalBloodKnowledge.sync(player);
	}

	/** The blaze's vat screen after {@code examined} individuals have been examined. */
	private static void blazeScreen(ServerLevel level, ServerPlayer player, int examined) {
		clearRoom(level);
		forgetResearch(player);
		SpecimenVatBlockEntity vat = vat(level, player, row.offset(0, 0, 4), EntityType.BLAZE, 86.0D, 520.0D);
		String[] donors = {"a", "b", "c"};
		for (int i = 0; i < examined; i++) ClinicalBloodKnowledge.examined(player, vial("minecraft:blaze", donors[i]));
		player.openMenu(vat, vat.getBlockPos());
	}

	private static void huskScreen(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		forgetResearch(player);
		SpecimenVatBlockEntity vat = vat(level, player, row.offset(0, 0, 4), EntityType.PIG, 0.5D, 0.0D);
		vat.kept().setVigor(0.5D, level.getGameTime() - 2_000L);
		vat.kept().tick(level, vat.getBlockPos(), vat);
		vat.sendUpdates();
		player.openMenu(vat, vat.getBlockPos());
	}

	/** A jar a structure set down: no keeper, a preserved specimen whose kind the player has read. */
	private static void preservedScreen(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		forgetResearch(player);
		BlockPos at = row.offset(0, 0, 4);
		level.setBlock(at, BlockInit.specimen_jar.get().defaultBlockState().setValue(SpecimenJarBlock.FACING, Direction.NORTH), 3);
		SpecimenJarBlockEntity jar = (SpecimenJarBlockEntity) level.getBlockEntity(at);
		Mob snail = (Mob) EntityInit.chalybeate_snail.get().create(level);
		jar.setSpecimen(SpecimenJarData.captureEntity(snail));
		snail.discard();
		ClinicalBloodKnowledge.examined(player, vial("hemomancy:chalybeate_snail", "a"));
		jar.sendUpdates();
		player.openMenu(jar, at);
	}

	/** Two cow vats paired and fed {@code seconds} fed seconds into their brood; the bearer's screen. */
	private static void broodScreen(ServerLevel level, ServerPlayer player, int seconds) {
		clearRoom(level);
		forgetResearch(player);
		for (String donor : new String[]{"a", "b", "c"}) ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", donor));
		SpecimenVatBlockEntity bearer = vat(level, player, row.offset(0, 0, 4), EntityType.COW, 100.0D, 600.0D);
		vat(level, player, row.offset(1, 0, 4), EntityType.COW, 100.0D, 600.0D);
		SpecimenBrood.press(level, bearer, player);
		for (int i = 0; i < seconds; i++) SpecimenBrood.second(level, bearer.getBlockPos(), bearer);
		bearer.sendUpdates();
		player.openMenu(bearer, bearer.getBlockPos());
	}

	/** A lone pig: nothing of its kind beside it, so the brood button is refused. */
	private static void refusedScreen(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		forgetResearch(player);
		SpecimenVatBlockEntity vat = vat(level, player, row.offset(0, 0, 4), EntityType.PIG, 80.0D, 300.0D);
		player.openMenu(vat, vat.getBlockPos());
	}

	/**
	 * Two cow vats paired half way through their brood, with an empty vat beside the mate; when {@code born}, the brood
	 * is paid to term at once so the calf quickens in the empty vat while the camera watches.
	 */
	private static void threadScene(ServerLevel level, ServerPlayer player, boolean born) {
		clearRoom(level);
		SpecimenVatBlockEntity bearer = vat(level, player, row.offset(-1, 0, 4), EntityType.COW, 100.0D, 600.0D);
		SpecimenVatBlockEntity mate = vat(level, player, row.offset(0, 0, 4), EntityType.COW, 100.0D, 600.0D);
		vat(level, player, row.offset(1, 0, 4), null, 0.0D, 0.0D);
		SpecimenBrood.press(level, bearer, player);
		int seconds = born ? BroodRules.BROOD_SECONDS : 60;
		for (int i = 0; i < seconds; i++) SpecimenBrood.second(level, bearer.getBlockPos(), bearer);
		bearer.sendUpdates();
		mate.sendUpdates();
	}

	/** The Bestiary with one met kind (a blaze, kept), studied or not. */
	private static void bestiary(ServerLevel level, ServerPlayer player, boolean studied) {
		clearRoom(level);
		forgetResearch(player);
		BloodResearch.recordMet(player, EntityType.BLAZE);
		if (studied) {
			for (String donor : new String[]{"a", "b", "c"}) ClinicalBloodKnowledge.examined(player, vial("minecraft:blaze", donor));
		}
	}

	/** A loaded Living Syringe aimed at a kept blaze, whose kind refuses a vial. */
	private static void syringeScene(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		vat(level, player, row.offset(-3, 0, 4), EntityType.BLAZE, 80.0D, 400.0D);
		ItemStack rack = new ItemStack(ItemInit.vial_rack.get());
		VialRackItem.ensureInitialized(rack);
		player.getInventory().setItem(1, rack);
		ItemStack syringe = player.getMainHandItem();
		syringe.use(level, player, InteractionHand.MAIN_HAND);
	}

	/** A filled, identified vial of {@code type} from {@code donor}. */
	private static ItemStack vial(String type, String donor) {
		ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, type);
		tag.putBoolean(BloodVialItem.TAG_STATE, true);
		tag.putString(BloodSampleData.TAG_DONOR, donor);
		vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		BloodSampleData.identify(vial);
		return vial;
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
