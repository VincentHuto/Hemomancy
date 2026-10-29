package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.HallowedGroundRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.MausoleumWardRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoad;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchors;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * A Saint's mausoleum is consecrated ground. Its stone will not give to pick, blast or piston, and every refusal strikes
 * the ward; past the threshold the ward turns back anyone below the eighth degree. It burns an Apotheos, the body the
 * hive speaks through, spore load first, then flesh.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MausoleumEvents {
	public static final TagKey<Structure> HALLOWED = TagKey.create(Registries.STRUCTURE,
			Hemomancy.rloc("hallowed_mausoleums"));
	private static final ParticleColor CHAR = new ParticleColor(40, 34, 30);
	/** Players nearer than this to a strike see it. */
	private static final double STRIKE_RANGE = 32.0;
	/** Anchors farther than this from a player cannot hold them back. */
	private static final double ANCHOR_REACH = 64.0;
	/** A visitor held at the threshold sees its flare and the reason at most this often. */
	private static final int THRESHOLD_FLARE_TICKS = 10;
	private static final Map<UUID, Long> LAST_THRESHOLD_FLARE = new HashMap<>();

	private MausoleumEvents() {}

	public static void clearSessionState() {
		LAST_THRESHOLD_FLARE.clear();
	}

	public static void logout(Player player) {
		LAST_THRESHOLD_FLARE.remove(player.getUUID());
	}

	public static boolean hallowed(ServerLevel level, BlockPos pos) {
		return level.structureManager().getStructureWithPieceAt(pos, HALLOWED).isValid();
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level) || event.getPlayer().isCreative()) return;
		if (hallowed(level, event.getPos())) {
			event.setCanceled(true);
			event.getPlayer().displayClientMessage(Component.translatable("message.hemomancy.mausoleum.consecrated")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
			strike(level, event.getPlayer() instanceof ServerPlayer server ? server : null, event.getPos(),
					struckFace(event.getPlayer(), event.getPos()), WardStrikePacket.Kind.BREAK);
		}
	}

	/** Nothing is built in a Saint's tomb either: what could not be removed would block it for everyone. */
	@SubscribeEvent
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Player player
				&& !player.isCreative() && hallowed(level, event.getPos())) {
			event.setCanceled(true);
			player.displayClientMessage(Component.translatable("message.hemomancy.mausoleum.consecrated")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
			// The strike lands on the stone the block was set against, not on the empty cell.
			BlockPos struck = event.getPos();
			Direction face = Direction.getNearest(player.getEyePosition().subtract(Vec3.atCenterOf(struck)));
			if (player.pick(player.blockInteractionRange() + 1.0, 1.0f, false) instanceof BlockHitResult hit
					&& hit.getType() == HitResult.Type.BLOCK
					&& hit.getBlockPos().relative(hit.getDirection()).equals(struck)) {
				struck = hit.getBlockPos();
				face = hit.getDirection();
			}
			strike(level, player instanceof ServerPlayer server ? server : null, struck, face,
					WardStrikePacket.Kind.PLACE);
		}
	}

	/** Pistons do not move consecrated stone. */
	@SubscribeEvent
	public static void onPiston(net.neoforged.neoforge.event.level.PistonEvent.Pre event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		var structure = event.getStructureHelper();
		if (structure == null || !structure.resolve()) return;
		for (BlockPos pos : structure.getToPush()) {
			if (hallowed(level, pos)) {
				event.setCanceled(true);
				strike(level, null, pos, event.getDirection().getOpposite(), WardStrikePacket.Kind.PISTON);
				return;
			}
		}
		for (BlockPos pos : structure.getToDestroy()) {
			if (hallowed(level, pos)) {
				event.setCanceled(true);
				strike(level, null, pos, event.getDirection().getOpposite(), WardStrikePacket.Kind.PISTON);
				return;
			}
		}
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		// A blast that breaks nothing (a wind charge, a creeper without griefing) still works its buttons and doors.
		var interaction = event.getExplosion().getBlockInteraction();
		if (interaction != net.minecraft.world.level.Explosion.BlockInteraction.DESTROY
				&& interaction != net.minecraft.world.level.Explosion.BlockInteraction.DESTROY_WITH_DECAY) return;
		Vec3 centre = event.getExplosion().center();
		BlockPos nearest = null;
		for (Iterator<BlockPos> it = event.getAffectedBlocks().iterator(); it.hasNext(); ) {
			BlockPos pos = it.next();
			if (!hallowed(level, pos)) continue;
			it.remove();
			if (nearest == null || pos.distToCenterSqr(centre) < nearest.distToCenterSqr(centre)) nearest = pos;
		}
		// One blast, one strike: on the consecrated block nearest its heart, on the face that took it.
		if (nearest != null) {
			strike(level, null, nearest, Direction.getNearest(centre.subtract(Vec3.atCenterOf(nearest))),
					WardStrikePacket.Kind.EXPLOSION);
		}
	}

	/**
	 * The ward answers a refusal: the actor sees the strike on {@code pos}'s {@code face} even when nobody else is near
	 * (bystanders get it through {@link PacketHandler#sendNear}), and the stone tolls.
	 */
	public static void strike(ServerLevel level, @Nullable ServerPlayer actor, BlockPos pos, Direction face,
			WardStrikePacket.Kind kind) {
		WardStrikePacket packet = WardStrikePacket.strike(pos, face, kind);
		if (actor != null) PacketHandler.sendToPlayer(actor, packet);
		PacketHandler.sendNear(level, actor, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, STRIKE_RANGE,
				packet);
		level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.6f, 0.5f);
	}

	/** The face of {@code pos} a player is striking: the one they look at, else the one toward their eyes. */
	private static Direction struckFace(Player player, BlockPos pos) {
		if (player.pick(player.blockInteractionRange() + 1.0, 1.0f, false) instanceof BlockHitResult hit
				&& hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
			return hit.getDirection();
		}
		return Direction.getNearest(player.getEyePosition().subtract(Vec3.atCenterOf(pos)));
	}

	/** Below the eighth degree, the ward line turns a visitor back a block and flares where they touched it. */
	@SubscribeEvent
	public static void onThresholdTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) holdAtThreshold(player);
	}

	/** Turns {@code player} back if they touch a ward line, or stand past one, without the right to; returns whether. */
	public static boolean holdAtThreshold(ServerPlayer player) {
		if (player.isCreative() || player.isSpectator() || !player.isAlive()) return false;
		var anchors = WardAnchors.loaded(player.level());
		if (anchors.isEmpty() || ProgressionGates.hasDegree(player, MausoleumWardRules.WARD_DEGREE)) {
			return false;
		}
		for (WardAnchorBlockEntity anchor : anchors) {
			AABB slab = anchor.threshold();
			if (slab == null || !anchor.settled()
					|| anchor.getBlockPos().distToCenterSqr(player.position()) > ANCHOR_REACH * ANCHOR_REACH) continue;
			if (!player.getBoundingBox().intersects(slab) && !anchor.inWard(player.position())) continue;
			turnBack(player, anchor, slab);
			return true;
		}
		return false;
	}

	private static void turnBack(ServerPlayer player, WardAnchorBlockEntity anchor, AABB slab) {
		Direction inward = anchor.inward();
		Vec3 back = MausoleumWardRules.pushBack(player.position(), slab, inward);
		BlockPos touched = BlockPos.containing(
				Math.max(slab.minX, Math.min(slab.maxX - 1.0E-3, player.getX())),
				Math.max(slab.minY, Math.min(slab.maxY - 1.0E-3, player.getY() + 1.0)),
				Math.max(slab.minZ, Math.min(slab.maxZ - 1.0E-3, player.getZ())));
		// A mount would carry its rider straight back over the line.
		player.stopRiding();
		player.teleportTo(back.x, back.y, back.z);
		player.setDeltaMovement(Vec3.atLowerCornerOf(inward.getOpposite().getNormal()).scale(0.25));
		player.hurtMarked = true;
		long now = player.serverLevel().getGameTime();
		Long last = LAST_THRESHOLD_FLARE.get(player.getUUID());
		if (last != null && now - last < THRESHOLD_FLARE_TICKS) return;
		LAST_THRESHOLD_FLARE.put(player.getUUID(), now);
		strike(player.serverLevel(), player, touched, inward.getOpposite(), WardStrikePacket.Kind.THRESHOLD);
		player.displayClientMessage(Component.translatable("message.hemomancy.mausoleum_ward.refused")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0 || player.isCreative()
				|| player.isSpectator() || !HallowedGroundRules.burns(ApotheosBodyEvents.isApotheos(player))
				|| !hallowed(player.serverLevel(), player.blockPosition())) return;
		SporeLoad spores = HemoCapabilityAccess.getSporeLoad(player);
		if (!spores.spend(HallowedGroundRules.SPORE_DRAIN_PER_SECOND)) {
			spores.spend(spores.load());
			player.hurt(HemoDamageTypes.hallowedBurn(player.level()), HallowedGroundRules.BURN_DAMAGE);
			if (player.tickCount % 100 == 0) {
				player.displayClientMessage(Component.translatable("message.hemomancy.hallowed_ground")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
			}
		}
		player.serverLevel().sendParticles(HemoParticleData.glow(CHAR), player.getX(), player.getY() + 1.0,
				player.getZ(), 6, 0.3, 0.6, 0.3, 0.02);
		// The char on the body itself (the ward's glyphs over its hyphae) is drawn by everyone who sees it.
		PacketHandler.sendTracking(player, new WardStrikePacket(player.blockPosition(), Direction.UP,
				WardStrikePacket.Kind.BURN, player.getId()));
	}
}
