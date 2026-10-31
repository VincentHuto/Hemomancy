package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * With hemomancy.morphlingStrainReview, dresses the local player in each house strain and captures it: every strain
 * at all six stages from the back, side and front, then every active state the strain models and world effects
 * answer (fronds open, roots, sail, purses full, strands to a partner and a block, Lifeline, dissolve, sealed bell,
 * Whale Fall, Second Heads, Kindled, both field patches), underwater and night variants, and a helmeted player at
 * Primal for the head replacements. Shots go to build/morphling-strain-review/screenshots;
 * tools/review/morphling_strain_contact.py builds the Apex contact sheet from them. Run with
 * morphlingStrainReviewClient. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MorphlingStrainClientReview {
	private enum View {
		BACK(0.9, -3.4, 0), SIDE(3.4, 0.2, 90), FRONT(-0.6, 3.4, 180), HIGH_BACK(1.6, -4.6, 0);

		final double dx, dz;
		final float yaw;

		View(double dx, double dz, float yaw) {
			this.dx = dx;
			this.dz = dz;
			this.yaw = yaw;
		}
	}

	private enum Extra { NONE, WATER, NIGHT, HELMET, PARTNER, ANCHOR, LIFELINE, BLOOM, WHALE_FALL }

	private record Scene(String name, MorphlingStrain strain, int stage, int flags, List<View> views, Extra extra) {
	}

	private static final int SETTLE = 14, PER_VIEW = 7;
	private static final List<Scene> SCENES = scenes();
	private static int ticks, scene = -1, sceneStart, captured;
	private static BlockPos origin;
	private static ArmorStand camera;
	private static int partnerId = -1;

	private static List<Scene> scenes() {
		List<Scene> all = new ArrayList<>();
		List<View> three = List.of(View.BACK, View.SIDE, View.FRONT);
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			for (int stage = 0; stage <= 5; stage++) {
				all.add(new Scene(strain.id() + "-" + MorphlingItem.MATURITY_NAMES[stage].toLowerCase(), strain, stage,
						MorphlingActiveState.HOME, three, Extra.NONE));
			}
		}
		state(all, MorphlingStrains.SUNBURST, "lantern-fronds", 4, MorphlingActiveState.LANTERN_FRONDS, Extra.NONE);
		state(all, MorphlingStrains.SUNBURST, "bloom", 5, MorphlingActiveState.BLOOM, Extra.BLOOM);
		state(all, MorphlingStrains.SUNBURST, "arbor", 5, MorphlingActiveState.ARBOR, Extra.NONE);
		state(all, MorphlingStrains.SUNBURST, "night-away", 4, 0, Extra.NIGHT);
		state(all, MorphlingStrains.HOLDFAST, "anchored", 5, MorphlingActiveState.ANCHORED, Extra.NONE);
		state(all, MorphlingStrains.HOLDFAST, "breakwater", 5, MorphlingActiveState.BREAKWATER, Extra.NONE);
		state(all, MorphlingStrains.HOLDFAST, "cling", 4, MorphlingActiveState.CLING, Extra.NONE);
		state(all, MorphlingStrains.MERMAIDS_PURSE, "purses-empty", 3, 0, Extra.NONE);
		state(all, MorphlingStrains.MERMAIDS_PURSE, "purses-full", 3, MorphlingActiveState.withFill(0, 1.0), Extra.NONE);
		state(all, MorphlingStrains.MERMAIDS_PURSE, "sail-raised", 5, MorphlingActiveState.SAIL_RAISED, Extra.NONE);
		state(all, MorphlingStrains.MERMAIDS_PURSE, "sail-underwater", 5, MorphlingActiveState.SAIL_RAISED
				| MorphlingActiveState.HOME, Extra.WATER);
		state(all, MorphlingStrains.DEAD_MANS_ROPE, "link-partner", 4, MorphlingActiveState.HOME, Extra.PARTNER);
		state(all, MorphlingStrains.DEAD_MANS_ROPE, "link-anchor", 4, MorphlingActiveState.HOME, Extra.ANCHOR);
		state(all, MorphlingStrains.DEAD_MANS_ROPE, "reeling", 4, MorphlingActiveState.REELING, Extra.ANCHOR);
		state(all, MorphlingStrains.DEAD_MANS_ROPE, "lifeline", 4, MorphlingActiveState.LIFELINE, Extra.LIFELINE);
		state(all, MorphlingStrains.DEAD_MANS_ROPE, "night-train", 5, MorphlingActiveState.HOME, Extra.NIGHT);
		state(all, MorphlingStrains.INK_CAP, "folded-away", 4, MorphlingActiveState.FOLDED_AWAY, Extra.NONE);
		state(all, MorphlingStrains.INK_CAP, "deliquesce", 5, MorphlingActiveState.DELIQUESCE, Extra.NONE);
		state(all, MorphlingStrains.INK_CAP, "helmet-apex", 4, 0, Extra.HELMET);
		state(all, MorphlingStrains.INK_CAP, "helmet-primal", 5, 0, Extra.HELMET);
		state(all, MorphlingStrains.HUSHBELL, "fold-shut", 5, MorphlingActiveState.FOLD_SHUT, Extra.NONE);
		state(all, MorphlingStrains.HUSHBELL, "cryptobiosis-underwater", 5, MorphlingActiveState.CRYPTOBIOSIS, Extra.WATER);
		state(all, MorphlingStrains.MARROWPLUME, "whale-fall", 5, MorphlingActiveState.WHALE_FALL_ROOTED, Extra.WHALE_FALL);
		state(all, MorphlingStrains.MARROWPLUME, "plume-cloud", 4, MorphlingActiveState.PLUME_CLOUD, Extra.NONE);
		state(all, MorphlingStrains.KINBUD, "second-head", 4, MorphlingActiveState.SECOND_HEAD, Extra.NONE);
		state(all, MorphlingStrains.KINBUD, "second-head-twin", 4, MorphlingActiveState.SECOND_HEAD
				| MorphlingActiveState.SECOND_HEAD_TWIN | MorphlingActiveState.KINDLED, Extra.NONE);
		state(all, MorphlingStrains.KINBUD, "helmet-primal", 5, 0, Extra.HELMET);
		state(all, MorphlingStrains.KINBUD, "night-kindled", 5, MorphlingActiveState.KINDLED, Extra.NIGHT);
		String only = System.getProperty("hemomancy.morphlingStrainReview.only", "");
		return only.isEmpty() ? all : all.stream().filter(s -> Arrays.stream(only.split(",")).anyMatch(s.name::contains)).toList();
	}

	private static void state(List<Scene> all, MorphlingStrain strain, String name, int stage, int flags, Extra extra) {
		all.add(new Scene("state-" + strain.id() + "-" + name, strain, stage, flags,
				List.of(View.BACK, View.SIDE, View.FRONT, View.HIGH_BACK), extra));
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.morphlingStrainReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("morphling-strain-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		if (++ticks == 40) arena(server, uuid);
		if (ticks < 100 || origin == null) return;
		if (scene >= 0 && scene < SCENES.size()) dress(mc, SCENES.get(scene));
		int local = ticks - sceneStart;
		if (scene < 0 || local >= SETTLE + SCENES.get(scene).views.size() * PER_VIEW) {
			scene++;
			sceneStart = ticks;
			if (scene >= SCENES.size()) {
				Hemomancy.LOGGER.info("MORPHLING_STRAIN_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			start(mc, server, uuid, SCENES.get(scene));
			return;
		}
		Scene s = SCENES.get(scene);
		if (s.extra == Extra.LIFELINE && local == 7) {
			// The Lifeline anchors where the host stood when it came on; the host then steps two blocks over.
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				player.teleportTo(player.serverLevel(), origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5, 0, 0);
			});
		}
		if (local >= SETTLE && (local - SETTLE) % PER_VIEW == 0) {
			View view = s.views.get((local - SETTLE) / PER_VIEW);
			aim(mc, view, s);
		}
		if (local >= SETTLE && (local - SETTLE) % PER_VIEW == PER_VIEW - 1) {
			View view = s.views.get((local - SETTLE) / PER_VIEW);
			shot(mc, s, view);
		}
	}

	private static void arena(MinecraftServer server, UUID uuid) {
		server.execute(() -> {
			var player = server.getPlayerList().getPlayer(uuid);
			var level = player.serverLevel();
			origin = new BlockPos(player.getBlockX(), 160, player.getBlockZ());
			var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
			for (String cmd : new String[] {"gamemode creative", "time set 6000", "gamerule doDaylightCycle false",
					"weather clear", "gamerule doMobSpawning false", "effect give @s minecraft:water_breathing infinite 0 true"})
				server.getCommands().performPrefixedCommand(source, cmd);
			for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-9, -1, -9), origin.offset(9, 6, 9))) {
				int dy = pos.getY() - origin.getY();
				level.setBlock(pos, dy == -1 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
						: dy == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
			}
			player.getAbilities().flying = false;
			player.teleportTo(level, origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5, 0, 0);
			Hemomancy.LOGGER.info("MORPHLING_STRAIN_REVIEW arena at {}", origin);
		});
	}

	private static void start(Minecraft mc, MinecraftServer server, UUID uuid, Scene s) {
		server.execute(() -> {
			var player = server.getPlayerList().getPlayer(uuid);
			var level = player.serverLevel();
			var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
			server.getCommands().performPrefixedCommand(source, s.extra == Extra.NIGHT ? "time set 18000" : "time set 6000");
			for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, 1, -4), origin.offset(4, 4, 4))) {
				boolean water = s.extra == Extra.WATER && Math.abs(pos.getX() - origin.getX()) < 4
						&& Math.abs(pos.getZ() - origin.getZ()) < 4;
				level.setBlock(pos, water ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
			}
			if (s.extra == Extra.ANCHOR) level.setBlock(origin.offset(-2, 3, 2), Blocks.STONE.defaultBlockState(), 2);
			level.getEntitiesOfClass(ArmorStand.class, new net.minecraft.world.phys.AABB(origin).inflate(10)).forEach(e -> e.discard());
			level.getEntitiesOfClass(MorphlingFieldEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(10)).forEach(e -> e.discard());
			partnerId = -1;
			if (s.extra == Extra.PARTNER) {
				ArmorStand partner = EntityType.ARMOR_STAND.create(level);
				partner.setPos(origin.getX() + 3.5, origin.getY() + 1, origin.getZ() + 2.5);
				partner.setShowArms(true);
				level.addFreshEntity(partner);
				partnerId = partner.getId();
			}
			if (s.extra == Extra.BLOOM) MorphlingFieldEntity.spawn(level, player, MorphlingFieldEntity.Kind.BLOOM,
					origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5, 3.0F, 20 * 60);
			if (s.extra == Extra.WHALE_FALL) MorphlingFieldEntity.spawn(level, player, MorphlingFieldEntity.Kind.WHALE_FALL,
					origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5, 3.0F, 20 * 60);
			player.setItemSlot(EquipmentSlot.HEAD, s.extra == Extra.HELMET ? new ItemStack(Items.IRON_HELMET) : ItemStack.EMPTY);
			double startX = s.extra == Extra.LIFELINE ? origin.getX() - 1.5 : origin.getX() + .5;
			player.teleportTo(level, startX, origin.getY() + 1, origin.getZ() + .5, 0, 0);
		});
		mc.options.setCameraType(CameraType.FIRST_PERSON);
	}

	/** The equipped stack and synced state are set on the client copy every tick, as a tracker would receive them. */
	private static void dress(Minecraft mc, Scene s) {
		ItemStack stack = new ItemStack(s.strain.item().get());
		CompoundTag tag = new CompoundTag();
		tag.putFloat("EnzymePower", MorphlingItem.MATURITY_THRESHOLDS[Math.min(4, s.stage)]);
		if (s.stage >= 5) tag.putBoolean(MorphlingItem.PRIMALIZED_KEY, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		var equipped = HemoCapabilityAccess.getEquippedMorphling(mc.player);
		if (!ItemStack.isSameItemSameComponents(equipped.getEquippedMorphling(), stack)) equipped.setEquippedMorphling(stack);
		int flags = s.flags;
		if (s.extra == Extra.LIFELINE && ticks - sceneStart < 3) flags &= ~MorphlingActiveState.LIFELINE;
		equipped.setActiveFlags(flags);
		int[] ids = s.extra == Extra.PARTNER && partnerId >= 0 ? new int[] {partnerId} : new int[0];
		long[] anchors = s.extra == Extra.ANCHOR ? new long[] {origin.offset(-2, 3, 2).asLong()} : new long[0];
		equipped.setLinks(ids, anchors);
	}

	private static void aim(Minecraft mc, View view, Scene s) {
		if (camera == null) camera = EntityType.ARMOR_STAND.create(mc.level);
		Vec3 target = new Vec3(origin.getX() + .5, origin.getY() + 1, origin.getZ() + .5);
		double height = view == View.HIGH_BACK ? 2.6 : 1.25;
		camera.setPos(target.x + view.dx, target.y + height - 1.7, target.z + view.dz);
		double lookX = target.x - (target.x + view.dx), lookZ = target.z - (target.z + view.dz);
		float yaw = (float) (Math.toDegrees(Math.atan2(-lookX, lookZ)));
		float distance = (float) Math.sqrt(lookX * lookX + lookZ * lookZ);
		float pitch = (float) Math.toDegrees(Math.atan2(height - 0.95, distance));
		camera.setYRot(yaw);
		camera.setYHeadRot(yaw);
		camera.yHeadRotO = yaw;
		camera.setXRot(pitch);
		camera.setOldPosAndRot();
		mc.setCameraEntity(camera);
	}

	private static void shot(Minecraft mc, Scene s, View view) {
		String file = "morphling-" + String.format("%03d", scene) + "-" + s.name + "-" + view.name().toLowerCase() + ".png";
		Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {
		});
		captured++;
		Hemomancy.LOGGER.info("MORPHLING_STRAIN_REVIEW shot {}", file);
	}
}
