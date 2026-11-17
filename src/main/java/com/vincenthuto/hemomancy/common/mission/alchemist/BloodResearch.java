package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * What a player knows of a creature's blood (core-loop Track 5, CL-12). Until a player has examined a sample of a kind
 * under the Microscope its blood is an unread humour to them: tooltips, samples and a vessel's readout name nothing,
 * and a draw from it gives them only {@link #UNREAD_SHARE} of its Temper, because body and mind do not yet understand
 * it (NQ-03-1, NQ-03-2). The blood itself comes through in full. Knowledge belongs to the player, not the vial, so a
 * traded sample stays unread to someone who has not done the work. Examining three individuals (or one kept creature
 * at three Vigor levels) studies the kind: its yield is known.
 */
public final class BloodResearch {
	/** The share of a kind's Temper an unread draw gives, wild or kept. */
	public static final double UNREAD_SHARE = 0.5D;

	private BloodResearch() {
	}

	/** The share of a kind's Temper a draw gives: all of it once read, {@link #UNREAD_SHARE} before. */
	public static double temperShare(boolean read) {
		return read ? 1.0D : UNREAD_SHARE;
	}

	/** Whether {@code viewer} can read {@code type}'s blood; a Creative player reads everything. */
	public static boolean knows(@Nullable Player viewer, @Nullable EntityType<?> type) {
		if (viewer == null || type == null) return false;
		if (viewer.isCreative()) return true;
		return knows(viewer, BuiltInRegistries.ENTITY_TYPE.getKey(type));
	}

	public static boolean knows(@Nullable Player viewer, @Nullable ResourceLocation source) {
		if (viewer == null || source == null) return false;
		return viewer.isCreative() || HemoCapabilityAccess.clinicalBlood(viewer).knowsSource(source.toString());
	}

	/** Whether {@code viewer} has studied {@code source} at three individuals or Vigor levels. */
	public static boolean studied(@Nullable Player viewer, @Nullable ResourceLocation source) {
		if (viewer == null || source == null) return false;
		return viewer.isCreative() || HemoCapabilityAccess.clinicalBlood(viewer).studied(source.toString());
	}

	/** How many of the three individuals {@code viewer} has examined of {@code source}. */
	public static int individuals(@Nullable Player viewer, @Nullable ResourceLocation source) {
		if (viewer == null || source == null) return 0;
		return HemoCapabilityAccess.clinicalBlood(viewer).individuals(source.toString());
	}

	/**
	 * Whether {@code viewer} has met {@code source} (core-loop round two, P6): sampled one, kept one, bred one or looked
	 * at one through a vessel's screen, or examined its blood. A met kind is listed in the Living Bestiary, its blood
	 * unread until examined. A Creative player has met everything.
	 */
	public static boolean met(@Nullable Player viewer, @Nullable ResourceLocation source) {
		if (viewer == null || source == null) return false;
		if (viewer.isCreative()) return true;
		var progress = HemoCapabilityAccess.clinicalBlood(viewer);
		return progress.hasMet(source.toString()) || progress.knowsSource(source.toString());
	}

	/**
	 * Records that {@code player} met a creature of {@code type}, and tells their client when it was new. Only a kind
	 * with a blood profile is recorded, and only for a player the clinical lessons are open to (the Bestiary is theirs).
	 */
	public static void recordMet(@Nullable Player player, @Nullable EntityType<?> type) {
		if (!(player instanceof ServerPlayer server) || type == null) return;
		ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		if (!BloodProfileData.snapshot(false).json().containsKey(id) || !ClinicalBloodKnowledge.eligible(server)) return;
		if (HemoCapabilityAccess.clinicalBlood(server).recordMet(id.toString())) ClinicalBloodKnowledge.sync(server);
	}
}
