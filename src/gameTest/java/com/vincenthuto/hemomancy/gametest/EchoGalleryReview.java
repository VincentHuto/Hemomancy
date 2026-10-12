package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCast;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCasts;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * With hemomancy.echoGallery, replays every registered echo cast in a disposable world: a base-sized echo (0.42) on
 * the left and a life-size one (1.0) on the right, both casting toward +x before a fixed camera. Each cast is
 * captured three times (opening, mid-cast, and after charged casts release) into build/loom-client-review/screenshots/echo-*.png.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EchoGalleryReview {
	private static final int START = 200, SPACING = 48;
	private static final int[] FRAMES = {8, 22, 38};
	private static int ticks;
	private static BlockPos row;
	private static List<String> names;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.echoGallery")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("loom-client-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null || mc.level == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == 40) {
			names = new ArrayList<>(EchoCasts.registered());
			names.sort(String::compareTo);
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				var level = player.serverLevel();
				row = player.blockPosition().offset(0, 0, 10);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
						"weather clear", "gamerule doMobSpawning false", "kill @e[type=item]"})
					server.getCommands().performPrefixedCommand(source, cmd);
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(-12, -1, -12), row.offset(12, 8, 8)))
					level.setBlock(pos, pos.getY() < row.getY() ? Blocks.SMOOTH_STONE.defaultBlockState()
							: Blocks.AIR.defaultBlockState(), 2);
				// Cleared looms drop their contents; sweep them so the floor stays empty.
				server.getCommands().performPrefixedCommand(source, "kill @e[type=item]");
				Vec3 eye = Vec3.atBottomCenterOf(row).add(-1.2, 2.1, -4.4), look = Vec3.atBottomCenterOf(row).add(-0.9, 1.0, 0.5);
				float yaw = (float) Math.toDegrees(Math.atan2(-(look.x - eye.x), look.z - eye.z));
				float pitch = (float) -Math.toDegrees(Math.atan2(look.y - eye.y, Math.hypot(look.x - eye.x, look.z - eye.z)));
				player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
				Hemomancy.LOGGER.info("ECHO_GALLERY staged {} casts at {}", names.size(), row);
			});
		}
		if (ticks < START || row == null || names == null) return;
		int local = (ticks - START) % SPACING, index = (ticks - START) / SPACING;
		if (index >= names.size()) {
			mc.stop();
			return;
		}
		String name = names.get(index);
		if (local == 0) {
			var manip = ManipulationInit.MANIPS_TYPE_REGISTRY.get(Hemomancy.rloc(name));
			if (manip == null) return;
			Vec3 forward = new Vec3(1, 0, 0);
			for (float figure : new float[]{0.42f, 1.0f}) {
				Vec3 feet = Vec3.atBottomCenterOf(row).add(figure < 1 ? -3.5 : 0.5, 0, 0.5);
				Vec3 hands = feet.add(forward.scale(0.25 * figure)).add(0, 1.25 * figure, 0);
				EchoCasts.forManipulation(name).release(new EchoCast.EchoContext(mc.level, hands, feet, forward, figure, manip));
			}
		}
		for (int frame = 0; frame < FRAMES.length; frame++)
			if (local == FRAMES[frame]) {
				String file = "echo-" + String.format("%03d", index) + "-" + name + "-" + (char) ('a' + frame) + ".png";
				Screenshot.grab(mc.gameDirectory, file, mc.getMainRenderTarget(), message -> {});
			}
	}
}
