package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionChannelRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Which verb a player is channelling, and how much it holds them still. A cell hand is its verb; the Living Staff
 * channels the player's active verb (unless it is threading an engram). Safe on both sides.
 */
public final class VerbChannel {
	private VerbChannel() {
	}

	/** The player's active verb: what the Staff channels and which cell hand the verb key conjures. */
	public static Verb activeVerb(Player player) {
		if (player.level().isClientSide) {
			Optional<Verb> sent = com.vincenthuto.hemomancy.common.util.ClientBridge.get().onlookerVerb(player);
			if (sent.isPresent()) return sent.get();
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		return known == null ? Verb.PROJECT : known.getActiveVerb();
	}

	/** The verb {@code user} is channelling now, if any. */
	public static Optional<Verb> channelling(LivingEntity user) {
		if (user == null || !user.isUsingItem()) return Optional.empty();
		return verbOf(user, user.getUseItem());
	}

	/** The verb {@code stack} channels for {@code user}: a cell hand's own, or the Staff's active verb. */
	public static Optional<Verb> verbOf(LivingEntity user, ItemStack stack) {
		if (stack.getItem() instanceof BloodAbsorptionItem) return Optional.of(Verb.ABSORB);
		if (stack.getItem() instanceof BloodProjectionItem) return Optional.of(Verb.PROJECT);
		if (stack.getItem() instanceof LivingStaffItem && user instanceof Player player
				&& !EngramTracing.isTracing(player)) {
			return Optional.of(activeVerb(player));
		}
		return Optional.empty();
	}

	public static boolean isChannelling(LivingEntity user, Verb verb) {
		return channelling(user).map(channelled -> channelled == verb).orElse(false);
	}

	/**
	 * How fast the channelling player may move: plain Absorption roots them (lifted by its movement skills); plain
	 * Projection does not; a Sculpting that claims what the player aims at decides for itself ({@link VerbSculpting#roots}).
	 * A selected Sculpting that names its own movement (a strand) sets it outright while its verb is channelled.
	 */
	public static double movementMultiplier(Player player) {
		Optional<Verb> verb = channelling(player);
		if (verb.isEmpty()) return 1.0D;
		OptionalDouble declared = declaredMovement(player, verb.get());
		if (declared.isPresent()) return declared.getAsDouble();
		boolean roots = SculptingDispatcher.currentClaim(player, verb.get())
				.map(claim -> claim.selected().sculpting().roots())
				.orElse(verb.get() == Verb.ABSORB);
		return roots ? BloodAbsorptionItem.getChannelMovementMultiplier(player) : 1.0D;
	}

	/**
	 * Whether the selected Sculpting sets the channel's movement itself ({@link VerbSculpting#movement}), which replaces
	 * vanilla's use-item slowdown: the client scales its input even at full speed.
	 */
	public static boolean declaresMovement(Player player) {
		return channelling(player).map(verb -> declaredMovement(player, verb).isPresent()).orElse(false);
	}

	private static OptionalDouble declaredMovement(Player player, Verb verb) {
		return SculptingDispatcher.selected(player, verb)
				.map(selected -> selected.sculpting().movement(player))
				.filter(OptionalDouble::isPresent)
				.map(movement -> OptionalDouble.of(Math.max(0.0D, Math.min(1.0D, movement.getAsDouble()))))
				.orElse(OptionalDouble.empty());
	}

	/** The client's input scale for {@link #movementMultiplier}. */
	public static double clientInputMultiplier(Player player) {
		return BloodAbsorptionChannelRules.clientInputMultiplier(movementMultiplier(player));
	}

	/**
	 * Holds a rooted channelling player to {@link #movementMultiplier}; called each use tick on both sides. A
	 * declared movement is an input scale only, so it is not damped again here.
	 */
	public static void applyMovement(Player player) {
		if (declaresMovement(player)) return;
		double multiplier = movementMultiplier(player);
		if (multiplier >= 1.0D) return;
		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x * multiplier, movement.y, movement.z * multiplier);
		if (multiplier <= 0.0D) {
			player.xxa = 0.0F;
			player.zza = 0.0F;
		}
	}
}
