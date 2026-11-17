package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineVigilBlock;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineVigilBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessels;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatAssembly;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.menagerieReview, stages core-loop Track 5 in a closed room of a disposable world, captures it into
 * build/menagerie-client-review/screenshots and stops. A real survival player at the Third Degree: a row of Specimen
 * Vats (a thriving blaze, a wan cow, a husk) beside a Specimen Jar; the blaze close up through the glass; the husk close up; a pig led
 * on the Tendon Line; held Absorption drawing from the blaze's vat; a vat's readout before and after its kind is
 * examined; a cow sample's tooltip unread, then read; a dormant and a lit Sanguine Vigil, near and close; and joined
 * vats, a 2x2 vessel keeping a horse and a 2x2x2 vessel beside it (UN-34, UN-35). Run with menagerieReviewClient and drop
 * {"op":"create","name":"MenagerieReview","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MenagerieClientReview {
	private static final String PROPERTY = "hemomancy.menagerieReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 100;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/** One staged moment; see FarmingClientReview. {@code tooltip} opens the inventory and hovers hotbar slot one. */
	private record Shot(String name, Supplier<Item> held, Vec3 stand, Vec3 aim, boolean use, boolean tooltip,
			boolean thirdPerson, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final List<Shot> SHOTS = List.of(
			new Shot("vats", () -> Items.AIR, new Vec3(0.5, 0, -1.0), new Vec3(0.5, 1.3, 4.5), false, false, false,
					new int[]{40}, MenagerieClientReview::vats),
			new Shot("blaze-close", () -> Items.AIR, new Vec3(-2.5, 0, 2.2), new Vec3(-2.5, 1.1, 4.5), false, false, false,
					new int[]{40}, MenagerieClientReview::vats),
			new Shot("leash", () -> Items.AIR, new Vec3(0.5, 0, -1.5), new Vec3(1.5, 0.6, 5.0), false, false, false,
					new int[]{30, 70}, MenagerieClientReview::leash),
			new Shot("husk-close", () -> Items.AIR, new Vec3(3.5, 0, 2.2), new Vec3(3.5, 0.6, 4.5), false, false, false,
					new int[]{40}, MenagerieClientReview::vats),
			new Shot("draw", ItemInit.blood_absorption::get, new Vec3(-2.5, 0, 1.6), new Vec3(-2.5, 1.0, 4.5), true, false, false,
					new int[]{40, 80}, MenagerieClientReview::vats),
			new Shot("readout", () -> Items.AIR, new Vec3(-2.5, 0, 1.6), new Vec3(-2.5, 1.0, 4.5), false, false, false,
					new int[]{40}, MenagerieClientReview::readout),
			new Shot("vial-unread", () -> Items.AIR, new Vec3(0.5, 0, 0.5), new Vec3(0.5, 1.0, 4.0), false, true, false,
					new int[]{40}, MenagerieClientReview::vialUnread),
			new Shot("vial-read", () -> Items.AIR, new Vec3(0.5, 0, 0.5), new Vec3(0.5, 1.0, 4.0), false, true, false,
					new int[]{40}, MenagerieClientReview::vialRead),
			// UN-34 and UN-35: a dormant and a lit Sanguine Vigil, a 2x2 vessel keeping a horse and a 2x2x2 vessel.
			new Shot("vigils", () -> Items.AIR, new Vec3(0.5, 0, 1.0), new Vec3(0.5, 1.0, 5.0), false, false, false,
					new int[]{40}, MenagerieClientReview::vigils),
			new Shot("vigil-dormant-close", () -> Items.AIR, new Vec3(-1.5, 0, 3.0), new Vec3(-1.5, 1.0, 5.5), false, false, false,
					new int[]{40}, MenagerieClientReview::vigils),
			new Shot("vigil-lit-close", () -> Items.AIR, new Vec3(2.5, 0, 3.0), new Vec3(2.5, 1.0, 5.5), false, false, false,
					new int[]{40}, MenagerieClientReview::vigils),
			new Shot("joined-vats", () -> Items.AIR, new Vec3(-0.5, 0, -1.5), new Vec3(-0.5, 1.5, 5.0), false, false, false,
					new int[]{40}, MenagerieClientReview::joinedVats),
			new Shot("vat-horse-close", () -> Items.AIR, new Vec3(-3.5, 0, 1.0), new Vec3(-3.5, 1.2, 5.0), false, false, false,
					new int[]{40}, MenagerieClientReview::joinedVats),
			new Shot("vat-cube-close", () -> Items.AIR, new Vec3(3.0, 0, 0.5), new Vec3(3.0, 2.0, 5.0), false, false, false,
					new int[]{40}, MenagerieClientReview::joinedVats));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("menagerie-client-review")) return;
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
				Hemomancy.LOGGER.info("MENAGERIE_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			if (mc.screen != null) mc.player.closeContainer();
			mc.options.setCameraType(current.thirdPerson() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			mc.gui.getChat().clearMessages(false);
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (current.tooltip() && local == 14) mc.setScreen(new InventoryScreen(mc.player));
		if (current.tooltip() && local >= 20 && local <= 40) hover(mc);
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "menagerie-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("MENAGERIE_REVIEW shot {} {} at {} using={} screen={}", shot, current.name(), at,
					mc.player.isUsingItem(), mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** Puts the cursor on hotbar slot one of the open inventory. */
	private static void hover(Minecraft mc) {
		if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
		var window = mc.getWindow();
		double x = (screen.getGuiLeft() + 16) * (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double y = (screen.getGuiTop() + 150) * (double) window.getScreenHeight() / window.getGuiScaledHeight();
		try {
			Field xpos = MouseHandler.class.getDeclaredField("xpos");
			Field ypos = MouseHandler.class.getDeclaredField("ypos");
			xpos.setAccessible(true);
			ypos.setAccessible(true);
			xpos.setDouble(mc.mouseHandler, x);
			ypos.setDouble(mc.mouseHandler, y);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.warn("MENAGERIE_REVIEW cannot move the cursor", e);
		}
	}

	/** A closed room of deepslate at night, active blood, Temper empty, the Third Degree and a researcher's standing. */
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
		player.getInventory().clearContent();
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>());
		known.setSelectedManip(BloodManipulation.BLANK);
		known.setActiveVerb(Verb.ABSORB);
		KnownManipulationEvents.syncPlayerEvent(player);
		var temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, 0.0F, TemperRules.MAX_CAP);
		temper.markSeen(EnumBloodTemper.FLAMMEUS);
		temper.markSeen(EnumBloodTemper.LUX);
		forgetResearch(player);
		Hemomancy.LOGGER.info("MENAGERIE_REVIEW staged at {}", row);
	}

	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		shot.scene().accept(level, player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		// No night vision: the room is lit, and a husk's darkness must read as it would in play.
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
	}

	// ── Scenes ──

	private static void clearRoom(ServerLevel level, ServerPlayer player) {
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
			double vigor, boolean husk) {
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState().setValue(SpecimenVatBlock.FACING, Direction.NORTH);
		level.setBlock(base, lower, 3);
		level.setBlock(base.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		SpecimenVatBlockEntity vat = (SpecimenVatBlockEntity) level.getBlockEntity(base);
		vat.buffer().setOwner(owner.getUUID());
		Mob mob = type.create(level);
		vat.kept().keep(SpecimenJarData.captureEntity(mob), mob.getMaxHealth(), husk ? 0.5D : vigor, level.getGameTime());
		mob.discard();
		vat.buffer().fill(husk ? 0.0D : 400.0D);
		if (husk) {
			vat.kept().setVigor(0.5D, level.getGameTime() - 2_000L);
			vat.kept().tick(level, base, vat);
		}
		vat.sendUpdates();
		return vat;
	}

	/** Three vats in a row (a thriving blaze, a wan cow, a husk) and a jar of a small creature beside them. */
	private static void vats(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		vat(level, player, row.offset(-3, 0, 4), EntityType.BLAZE, 92.0D, false);
		vat(level, player, row.offset(0, 0, 4), EntityType.COW, 38.0D, false);
		vat(level, player, row.offset(3, 0, 4), EntityType.PIG, 0.0D, true);
		BlockPos jar = row.offset(5, 0, 4);
		level.setBlock(jar, BlockInit.specimen_jar.get().defaultBlockState().setValue(SpecimenJarBlock.FACING, Direction.NORTH), 3);
		SpecimenJarBlockEntity jarEntity = (SpecimenJarBlockEntity) level.getBlockEntity(jar);
		Mob snail = (Mob) EntityInit.chalybeate_snail.get().create(level);
		jarEntity.setSpecimen(SpecimenJarData.captureEntity(snail));
		snail.discard();
		jarEntity.buffer().setOwner(player.getUUID());
		jarEntity.sendUpdates();
	}

	/** A pig on the player's leash, a few blocks off. */
	private static void leash(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		Pig pig = EntityType.PIG.create(level);
		Vec3 spot = at(new Vec3(2.0, 0.0, 6.0));
		pig.moveTo(spot.x, spot.y, spot.z, 200.0F, 0.0F);
		level.addFreshEntity(pig);
		MenagerieLeash.leash(player, pig);
	}

	/**
	 * Forgets every examined kind, so a reused world (or an earlier shot) cannot make the "unread" shots read: what a
	 * player knows of blood is saved on the player and would otherwise carry over from the last launch.
	 */
	private static void forgetResearch(ServerPlayer player) {
		HemoCapabilityAccess.clinicalBlood(player).deserializeNBT(player.registryAccess(), new CompoundTag());
		ClinicalBloodKnowledge.sync(player);
	}

	/** The blaze vat, and its readout sent as the server would on an empty-hand use: unread, then read. */
	private static void readout(ServerLevel level, ServerPlayer player) {
		vats(level, player);
		forgetResearch(player);
		SpecimenVatBlockEntity vat = (SpecimenVatBlockEntity) level.getBlockEntity(row.offset(-3, 0, 4));
		for (Component line : SpecimenVessels.readout(player, vat)) player.displayClientMessage(line, false);
		for (String donor : new String[]{"a", "b", "c"}) ClinicalBloodKnowledge.examined(player, vial("minecraft:blaze", donor));
		for (Component line : SpecimenVessels.readout(player, vat)) player.displayClientMessage(line, false);
	}

	private static void vialUnread(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		forgetResearch(player);
		player.getInventory().setItem(0, vial("minecraft:cow", "x"));
	}

	private static void vialRead(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		ClinicalBloodKnowledge.examined(player, vial("minecraft:cow", "x"));
		player.getInventory().setItem(0, vial("minecraft:cow", "x"));
	}

	/** Two Sanguine Vigils facing the player: the left one empty and dark, the right one holding blood and lit. */
	private static void vigils(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		vigil(level, player, row.offset(-1, 0, 5), 0.0D);
		vigil(level, player, row.offset(2, 0, 5), 1_000.0D);
	}

	private static void vigil(ServerLevel level, ServerPlayer owner, BlockPos pos, double blood) {
		BlockState state = BlockInit.sanguine_vigil.get().defaultBlockState()
				.setValue(SanguineVigilBlock.FACING, Direction.NORTH).setValue(SanguineVigilBlock.LIT, blood > 0.0D);
		level.setBlock(pos, state, 3);
		state.getBlock().setPlacedBy(level, pos, state, owner, new ItemStack(BlockInit.sanguine_vigil.get()));
		if (!(level.getBlockEntity(pos) instanceof SanguineVigilBlockEntity vigil)) return;
		var stored = ReservoirBlood.settled(vigil);
		stored.setOwner(owner.getUUID());
		stored.setActive(true);
		stored.setMaxBloodVolume(Math.max(stored.getMaxBloodVolume(), blood));
		stored.setBloodVolume(blood);
		vigil.setChanged();
	}

	/** A vessel four vats square keeping a horse, and a vessel of eight vats two high beside it. */
	private static void joinedVats(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		for (int x = -4; x <= -3; x++) for (int z = 4; z <= 5; z++) joinedVat(level, player, row.offset(x, 0, z));
		SpecimenVatBlockEntity square = (SpecimenVatBlockEntity) level.getBlockEntity(row.offset(-4, 0, 4));
		SpecimenVatBlockEntity runner = square.runner();
		Mob horse = EntityType.HORSE.create(level);
		runner.kept().keep(SpecimenJarData.captureEntity(horse), horse.getMaxHealth(), 80.0D, level.getGameTime());
		horse.discard();
		runner.buffer().fill(1_200.0D);
		runner.sendUpdates();
		for (int y = 0; y <= 2; y += 2)
			for (int x = 2; x <= 3; x++) for (int z = 4; z <= 5; z++) joinedVat(level, player, row.offset(x, y, z));
		SpecimenVatBlockEntity cube = ((SpecimenVatBlockEntity) level.getBlockEntity(row.offset(2, 0, 4))).runner();
		cube.buffer().fill(2_000.0D);
		cube.sendUpdates();
		Hemomancy.LOGGER.info("MENAGERIE_REVIEW vessels square={}x{} vats={} cube={}x{} vats={}", runner.width(), runner.layers(),
				runner.vats(), cube.width(), cube.layers(), cube.vats());
	}

	private static void joinedVat(ServerLevel level, ServerPlayer owner, BlockPos base) {
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState().setValue(SpecimenVatBlock.FACING, Direction.NORTH);
		level.setBlock(base, lower, 3);
		level.setBlock(base.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		((SpecimenVatBlockEntity) level.getBlockEntity(base)).buffer().setOwner(owner.getUUID());
		VatAssembly.placed(level, base);
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
