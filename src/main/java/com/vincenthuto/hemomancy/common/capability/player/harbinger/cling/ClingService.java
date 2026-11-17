package com.vincenthuto.hemomancy.common.capability.player.harbinger.cling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncCling;
import com.vincenthuto.hemomancy.common.util.HeldTraveller;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Venous Cling's server side (core-loop Track 9). The server owns the cling: it roots the player, keeps the root while
 * the body still touches a face that holds, takes the upkeep once a second through {@link BloodAccounting#payUpkeep},
 * and lets go when the blood runs short, the face gives way, the body leaves it (a jump, or crawling off an edge onto
 * the ground), or something else takes the body (flight, a travel hold, a vehicle, sleep). It never touches
 * {@code FlightArbiter}: a cling is not flight, and the body stays against a block the whole time, so vanilla's
 * floating check never sees it in the air. Fall distance is cleared while it holds and when it lets go.
 *
 * <p>The owner's client moves the body along the face from walking input ({@link ClingRules#crawl}); this class only
 * judges where the body ended up.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ClingService {
	public static final BloodAccounting.Source UPKEEP = BloodAccounting.Source.of("venous_cling", Category.CAST, false);

	private ClingService() {
	}

	/** Whether the worn boots carry Hematic Cleats that are turned on; they do nothing until the player clings. */
	public static boolean wearsCleats(Player player) {
		return CleatsFitting.wornAndOn(player);
	}

	/** Roots {@code player} to {@code face} of the block at {@code pos}. */
	public static void root(ServerPlayer player, BlockPos pos, Direction face) {
		ClingState state = HemoCapabilityAccess.getCling(player);
		state.root(pos, face, wearsCleats(player), player.level().getGameTime());
		player.setDeltaMovement(Vec3.ZERO);
		player.hurtMarked = true;
		player.resetFallDistance();
		sync(player);
		Vec3 at = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5D));
		ManipulationVisuals.burst(player.serverLevel(), ManipulationVisuals.Form.ANIMUS_CONJURE, player.position()
				.add(0.0D, player.getBbHeight() * 0.5D, 0.0D), at, 0.45D, 10);
		player.level().playSound(null, at.x, at.y, at.z, SoundEvents.ROOTED_DIRT_PLACE, SoundSource.PLAYERS, 0.9F, 0.6F);
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_CLINGING);
	}

	/** Lets go, saying why when {@code reasonKey} is given. */
	public static void release(ServerPlayer player, @Nullable String reasonKey) {
		ClingState state = HemoCapabilityAccess.getCling(player);
		if (!state.isClinging()) return;
		state.clear();
		player.resetFallDistance();
		sync(player);
		if (reasonKey != null) {
			player.displayClientMessage(Component.translatable(reasonKey).withStyle(ChatFormatting.GRAY), true);
		}
		player.level().playSound(null, player.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 0.6F, 0.8F);
	}

	/** Sends the cling to the owner and everyone tracking them. */
	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayerAndTrackers(player, PacketSyncCling.of(player, HemoCapabilityAccess.getCling(player)));
	}

	/**
	 * The {@code PlayerStateSync} send after a login, respawn or dimension change: a cling never survives one, so it
	 * lets go first and then tells the owner.
	 */
	public static void resync(ServerPlayer player) {
		HemoCapabilityAccess.getCling(player).clear();
		PacketHandler.sendToPlayer(player, PacketSyncCling.loose(player.getId()));
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) serverTick(player);
	}

	/** One server tick of a cling: whether it still holds, its anchor as the body crawls, and the upkeep. */
	public static void serverTick(ServerPlayer player) {
		ClingState state = HemoCapabilityAccess.getCling(player);
		if (!state.isClinging()) return;
		if (!player.isAlive() || player.isSpectator() || player.isSleeping() || player.isPassenger()
				|| player.getAbilities().flying || player.isFallFlying() || HeldTraveller.isHeld(player)) {
			release(player, null);
			return;
		}
		// Crawling down onto the ground lets go; a cling rooted from the floor holds until the body first leaves it.
		if (!player.onGround()) state.markLeftGround();
		else if (state.surface() == ClingRules.Surface.WALL && state.leftGround()) {
			release(player, null);
			return;
		}
		Optional<BlockPos> anchor = ClingSurface.find(player, state.face());
		if (anchor.isEmpty()) {
			boolean broken = !ClingSurface.holds(player.level(), state.anchor(), state.face());
			release(player, broken ? "message.hemomancy.venous_cling.gave_way" : null);
			return;
		}
		state.moveAnchor(anchor.get());
		// The server runs the body's own physics between the client's move packets, so it holds the body too: as the
		// Holdfast cling does, the velocity set after this tick's travel is what the next travel uses, and it carries
		// no fall. The client's crawl moves the body; its move packets overwrite this.
		player.setDeltaMovement(ClingRules.crawl(state.face(), Vec3.ZERO, 0.0D));
		boolean cleats = wearsCleats(player);
		if (cleats != state.cleats()) {
			state.setCleats(cleats);
			sync(player);
		}
		player.resetFallDistance();
		long now = player.level().getGameTime();
		if (now < state.nextUpkeep()) return;
		BloodAccounting.Payment payment = BloodAccounting.payUpkeep(player,
				ClingRules.upkeepPerSecond(state.surface(), cleats), UPKEEP, ClingRules.UPKEEP_INTERVAL, true);
		if (!payment.satisfied()) {
			release(player, "message.hemomancy.venous_cling.spent");
			return;
		}
		state.setNextUpkeep(now + ClingRules.UPKEEP_INTERVAL);
	}

	/**
	 * A player who starts tracking another is told the other's cling and, from the same handler, where its Tendon
	 * Line is rooted: the two Animus holds that onlookers draw.
	 */
	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (!(event.getTarget() instanceof Player target) || !(event.getEntity() instanceof ServerPlayer watcher)) return;
		if (HemoCapabilityAccess.getCling(target).isClinging()) {
			PacketHandler.sendToPlayer(watcher, PacketSyncCling.of(target, HemoCapabilityAccess.getCling(target)));
		}
		com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting.sendTo(watcher, target);
	}
}
