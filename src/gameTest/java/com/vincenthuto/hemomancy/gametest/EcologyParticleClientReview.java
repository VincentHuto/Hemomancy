package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * With hemomancy.ecologyParticleReview, lines up a Choir Keeper, a Luminal Cicada, a Peacock Spider, a Mortarbound in a
 * stone wall, a Will and a Drudge on a dark floor, then shows the keeper eating an Eye of Ender, the cicada's flash,
 * the spider's fan, the Mortarbound emerging when struck, a projected Will banished and a Drudge's death burst. The
 * cicada flash and the Will banishment are their own private methods, called by reflection; the Drudge burst is its
 * entity event. Then a Blood Lantern Jelly's motes, a Chalybeate Snail harvested with a knapper and a Prism Cuttle's
 * flash, both their own private methods, by reflection; the jelly and the cuttle float in glass tanks. The camera
 * player is a flying creative player so the spider sees someone. Shots land in
 * build/ecology-particle-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EcologyParticleClientReview {
	private static final UUID STRIKER = UUID.fromString("5b1d7e2a-3c94-4f08-a6e1-7d20c9b84f35");
	private static final byte DRUDGE_BURST_EVENT = 61;
	private static int ticks;
	private static int shot;
	private static BlockPos row;
	private static ChoirKeeperEntity keeper;
	private static ItemEntity eye;
	private static LuminalCicadaEntity cicada;
	private static MortarboundEntity mortarbound;
	private static WillEntity will;
	private static DrudgeEntity drudge;
	private static ChalybeateSnailEntity snail;
	private static PrismCuttleEntity cuttle;
	/** Eye offset from the row origin, the point looked at, and the scene action fired just before the capture. */
	private static final Object[][] SHOTS = {
			{new double[]{0.5, 2.4, -3.4, 0.5, 1.4, 0.5}, "keeper_eats_eye"},
			{new double[]{4.5, 2.0, -2.4, 4.5, 0.4, 0.5}, "cicada_flash"},
			{new double[]{8.5, 2.0, -2.4, 8.5, 0.6, 0.5}, "none"},
			{new double[]{12.5, 2.0, -3.4, 12.5, 1.2, 0.5}, "mortarbound_struck"},
			{new double[]{16.5, 2.0, -3.4, 16.5, 1.0, 0.5}, "will_banished"},
			{new double[]{20.5, 2.0, -3.4, 20.5, 0.9, 0.5}, "drudge_burst"},
			{new double[]{24.5, 1.6, -2.4, 24.5, 0.5, 0.5}, "none"},
			{new double[]{28.5, 1.4, -2.2, 28.5, 0.3, 0.5}, "snail_harvest"},
			{new double[]{32.5, 1.6, -2.4, 32.5, 0.4, 0.5}, "cuttle_flash"}};

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.ecologyParticleReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("ecology-particle-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == 40) {
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				var level = player.serverLevel();
				row = player.blockPosition().offset(0, 0, 8);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"gamemode creative", "time set 6000", "gamerule doDaylightCycle false",
						"weather clear", "gamerule doMobSpawning false"})
					server.getCommands().performPrefixedCommand(source, cmd);
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(-4, -1, -8), row.offset(36, 8, 6)))
					level.setBlock(pos, pos.getY() < row.getY() ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
							: Blocks.AIR.defaultBlockState(), 2);
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(10, 0, 1), row.offset(14, 3, 1)))
					level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);

				keeper = place(level, EntityInit.choir_keeper.get().create(level), row, false);
				cicada = place(level, EntityInit.luminal_cicada.get().create(level), row.offset(4, 0, 0), true);
				place(level, EntityInit.peacock_spider.get().create(level), row.offset(8, 0, 0), true);
				mortarbound = place(level, EntityInit.mortarbound.get().create(level), row.offset(12, 0, 0), false);
				will = place(level, EntityInit.will.get().create(level), row.offset(16, 0, 0), true);
				drudge = place(level, EntityInit.drudge.get().create(level), row.offset(20, 0, 0), true);
				for (int pool : new int[]{24, 32}) tank(level, row.offset(pool, 0, 0));
				place(level, EntityInit.blood_lantern_jelly.get().create(level), row.offset(24, 0, 0), true);
				snail = place(level, EntityInit.chalybeate_snail.get().create(level), row.offset(28, 0, 0), true);
				cuttle = place(level, EntityInit.prism_cuttle.get().create(level), row.offset(32, 0, 0), true);
				Hemomancy.LOGGER.info("ECOLOGY_PARTICLE_REVIEW placed keeper {}, cicada {}, mortarbound {}, will {}, drudge {}",
						keeper.isAlive(), cicada.isAlive(), mortarbound.isAlive(), will.isAlive(), drudge.isAlive());
			});
		}
		if (ticks < 80 || row == null) return;
		int local = (ticks - 80) % 60;
		if (local == 0) {
			if (shot == SHOTS.length) { mc.stop(); return; }
			double[] at = (double[]) SHOTS[shot][0];
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
				double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
				float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
				float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
				player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
			});
		}
		if (shot < SHOTS.length && local == 30) {
			String action = (String) SHOTS[shot][1];
			server.execute(() -> act(server.overworld(), action));
		}
		if (local == 40 && shot < SHOTS.length) {
			Screenshot.grab(mc.gameDirectory, "ecology-particle-" + shot + "-" + SHOTS[shot][1] + ".png",
					mc.getMainRenderTarget(), message -> {});
			if (shot == 0) Hemomancy.LOGGER.info("ECOLOGY_PARTICLE_REVIEW eye eaten: {}", eye != null && !eye.isAlive());
			Hemomancy.LOGGER.info("ECOLOGY_PARTICLE_REVIEW captured {}", shot);
			shot++;
		}
	}

	private static void act(ServerLevel level, String action) {
		switch (action) {
			case "keeper_eats_eye" -> {
				eye = new ItemEntity(level, keeper.getX(), keeper.getY(), keeper.getZ(),
						new ItemStack(Items.ENDER_EYE));
				eye.setNoGravity(true);
				level.addFreshEntity(eye);
				try {
					Field activity = ChoirKeeperEntity.class.getDeclaredField("ACTIVITY");
					activity.setAccessible(true);
					@SuppressWarnings("unchecked")
					EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) activity.get(null);
					keeper.getEntityData().set(accessor, 1);
					Field target = ChoirKeeperEntity.class.getDeclaredField("targetEye");
					target.setAccessible(true);
					target.set(keeper, eye);
				} catch (ReflectiveOperationException e) {
					Hemomancy.LOGGER.error("ECOLOGY_PARTICLE_REVIEW could not set the keeper chasing", e);
				}
			}
			case "cicada_flash" -> privateEffect(cicada, LuminalCicadaEntity.class, "flash");
			case "mortarbound_struck" -> {
				boolean hurt = mortarbound.hurt(level.damageSources().playerAttack(striker(level, mortarbound)), 0.5F);
				Hemomancy.LOGGER.info("ECOLOGY_PARTICLE_REVIEW mortarbound struck: {}, alive {}", hurt, mortarbound.isAlive());
			}
			case "will_banished" -> privateEffect(will, WillEntity.class, "spawnProjectedBanishmentBurst");
			case "drudge_burst" -> level.broadcastEntityEvent(drudge, DRUDGE_BURST_EVENT);
			case "snail_harvest" -> {
				var hand = new ItemStack(ItemInit.hematic_iron_knapper.get());
				privateEffect(snail, ChalybeateSnailEntity.class, "harvestSclerites",
						new Class<?>[]{Player.class, InteractionHand.class, ItemStack.class}, striker(level, snail), InteractionHand.MAIN_HAND, hand);
			}
			case "cuttle_flash" -> privateEffect(cuttle, PrismCuttleEntity.class, "flashAt",
					new Class<?>[]{Player.class}, striker(level, cuttle));
			default -> {}
		}
		Hemomancy.LOGGER.info("ECOLOGY_PARTICLE_REVIEW {} fired", action);
	}

	/** A glass tank of still water, two deep, so the swimmers keep their breath. */
	private static void tank(ServerLevel level, BlockPos centre) {
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, 0, -2), centre.offset(2, 1, 2))) {
			boolean wall = Math.abs(pos.getX() - centre.getX()) == 2 || Math.abs(pos.getZ() - centre.getZ()) == 2;
			level.setBlock(pos, wall ? Blocks.GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
		}
	}

	private static <T extends Mob> T place(ServerLevel level, T mob, BlockPos at, boolean frozen) {
		mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 180, 0);
		mob.setNoAi(frozen);
		mob.setPersistenceRequired();
		level.addFreshEntity(mob);
		return mob;
	}

	/** A stand-in player a step in front of the entity, for moments that need someone to act on them. */
	private static Player striker(ServerLevel level, Mob near) {
		var striker = FakePlayerFactory.get(level, new GameProfile(STRIKER, "WallStriker"));
		striker.moveTo(near.getX(), near.getY(), near.getZ() - 1.5, 0, 0);
		return striker;
	}

	private static void privateEffect(Object entity, Class<?> type, String effect) {
		privateEffect(entity, type, effect, new Class<?>[0]);
	}

	/** Calls the entity's own private particle method, so the scene shows exactly what the moment spawns. */
	private static void privateEffect(Object entity, Class<?> type, String effect, Class<?>[] parameters, Object... arguments) {
		try {
			Method method = type.getDeclaredMethod(effect, parameters);
			method.setAccessible(true);
			method.invoke(entity, arguments);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.error("ECOLOGY_PARTICLE_REVIEW could not run {} on {}", effect, type.getSimpleName(), e);
		}
	}
}
