package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;

/**
 * Completion handlers for Unstained cardinal rites.
 */
public class UnstainedCardinalRiteEvents {
	// â”€â”€ Unstained rite paths â”€â”€
	private static final String LETHEAN_BAPTISM_RITE = "cardinal_rite/lethean_baptism";
	private static final String SILVER_VEIL_RITE = "cardinal_rite/silver_veil";
	private static final String CLARITY_ASCENSION_RITE = "cardinal_rite/clarity_ascension";
	private static final String LETHEAN_JUDGMENT_RITE = "cardinal_rite/lethean_judgment";
	private static final String SILVER_DAWN_RITE = "cardinal_rite/silver_dawn";
	private static final String STILL_WATERS_RITE = "cardinal_rite/still_waters";
	private static final String PALE_CONSECRATION_RITE = "cardinal_rite/pale_consecration";
	private static final String SILTHMERES_REMEMBRANCE_RITE = "cardinal_rite/silthmeres_remembrance";
	private static final String LETHE_COVENANT_RITE = "cardinal_rite/lethe_covenant";
	private static final String LETHEAN_TIDE_RITE = "cardinal_rite/lethean_tide";
	private static final String PALE_VIGIL_RITE = "cardinal_rite/pale_vigil";
	private static final String LETHEAN_FONT_RITE = "cardinal_rite/lethean_font";
	private static final String CLOSED_VEIN_RITE = "cardinal_rite/closed_vein";
	private static final String ANTISEPTIC_GROUND_RITE = "cardinal_rite/antiseptic_ground";
	private static final String GLASS_LUNGS_RITE = "cardinal_rite/glass_lungs";
	private static final String MOON_WASHED_COPPER_RITE = "cardinal_rite/moon_washed_copper";
	/** Radius (in blocks) for Lethean Judgment anti-blood disruption. */
	private static final int LETHEAN_JUDGMENT_RADIUS = 16;
	/** Duration in ticks for Silver Veil effect (30 minutes = 36000 ticks). */
	private static final int SILVER_VEIL_DURATION_TICKS = 36000;
	/** Radius within which Pale Vigil blesses nearby Unstained. */
	private static final int PALE_VIGIL_RADIUS = 40;
	/** Clarity granted per player by the Pale Vigil burst. */
	private static final float PALE_VIGIL_CLARITY = 10.0f;
	/** Duration of Silver Ward / Verdigris Aura granted by Pale Vigil (30 min). */
	private static final int PALE_VIGIL_EFFECT_TICKS = 36000;
	/** Chunk radius of the Lethean Font domain. */
	private static final int LETHEAN_FONT_CHUNK_RADIUS = 8;
	/** Duration of the Lethean Font domain in ticks (1 hour). */
	private static final long LETHEAN_FONT_DOMAIN_TICKS = 72000L;
	/** Radius within which the Lethean Font burst blesses Unstained players. */
	private static final int LETHEAN_FONT_BURST_RADIUS = 50;
	/** Clarity granted per player by the Lethean Font burst. */
	private static final float LETHEAN_FONT_CLARITY = 20.0f;
	/** Duration of effects granted by the Lethean Font burst (1 hour). */
	private static final int LETHEAN_FONT_EFFECT_TICKS = 72000;
	private static final int CLOSED_VEIN_RADIUS = 24;
	private static final int CLOSED_VEIN_EFFECT_TICKS = 12000;
	private static final int ANTISEPTIC_GROUND_RADIUS = 12;
	private static final long ANTISEPTIC_GROUND_DURATION_TICKS = 18000L;
	private static final int GLASS_LUNGS_RADIUS = 24;
	private static final int GLASS_LUNGS_EFFECT_TICKS = 18000;
	private static final int MOON_WASHED_COPPER_RADIUS = 40;
	private static final int MOON_WASHED_COPPER_EFFECT_TICKS = 24000;

	/**
	 * Dispatches completion effects for Unstained cardinal rites.
	 *
	 * @return true when the supplied rite path belongs to an Unstained rite.
	 */
	public static boolean completeRite(ServerLevel sLevel, ServerPlayer caster, BlockPos center, String ritePath) {
		if (LETHEAN_BAPTISM_RITE.equals(ritePath)) {
			completeLetheanBaptism(sLevel, caster);
			return true;
		}
		if (SILVER_VEIL_RITE.equals(ritePath)) {
			completeSilverVeil(sLevel, caster);
			return true;
		}
		if (CLARITY_ASCENSION_RITE.equals(ritePath)) {
			completeClarityAscension(sLevel, caster);
			return true;
		}
		if (LETHEAN_JUDGMENT_RITE.equals(ritePath)) {
			completeLetheanJudgment(sLevel, caster, center);
			return true;
		}
		if (SILVER_DAWN_RITE.equals(ritePath)) {
			completeSilverDawn(sLevel, caster, center);
			return true;
		}
		if (STILL_WATERS_RITE.equals(ritePath)) {
			completeStillWaters(sLevel, caster, center);
			return true;
		}
		if (PALE_CONSECRATION_RITE.equals(ritePath)) {
			completePaleConsecration(sLevel, caster, center);
			return true;
		}
		if (SILTHMERES_REMEMBRANCE_RITE.equals(ritePath)) {
			completeSilthmereRemembrance(sLevel, caster, center);
			return true;
		}
		if (LETHE_COVENANT_RITE.equals(ritePath)) {
			completeLetheCovenantRite(sLevel, caster, center);
			return true;
		}
		if (LETHEAN_TIDE_RITE.equals(ritePath)) {
			completeLetheanTide(sLevel, caster);
			return true;
		}
		if (PALE_VIGIL_RITE.equals(ritePath)) {
			completePaleVigil(sLevel, caster, center);
			return true;
		}
		if (LETHEAN_FONT_RITE.equals(ritePath)) {
			completeLetheanFont(sLevel, caster, center);
			return true;
		}
		if (CLOSED_VEIN_RITE.equals(ritePath)) {
			completeClosedVein(sLevel, caster, center);
			return true;
		}
		if (ANTISEPTIC_GROUND_RITE.equals(ritePath)) {
			completeAntisepticGround(sLevel, caster, center);
			return true;
		}
		if (GLASS_LUNGS_RITE.equals(ritePath)) {
			completeGlassLungs(sLevel, caster, center);
			return true;
		}
		if (MOON_WASHED_COPPER_RITE.equals(ritePath)) {
			completeMoonWashedCopper(sLevel, caster, center);
			return true;
		}
		return false;
	}
	// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
	// Unstained Rite Completion Handlers
	// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

	/**
	 * Rite of Lethean Baptism (Minor, 0 blood):
	 * Entry rite that formally begins the Unstained path. Grants starting
	 * purity and sets the purification flag.
	 */
	private static void completeLetheanBaptism(ServerLevel sLevel, ServerPlayer caster) {
		HemoCapabilityAccess.getUnstainedProgress(caster).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification()) {
				unstained.setBegunPurification(true);
			}
			unstained.addPurity(5.0f);
			UnstainedProgressEvents.syncProgress(caster, unstained);
		});

		caster.displayClientMessage(
				Component.literal("The still waters wash over you. The Unstained path has begun.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);
		sLevel.sendParticles(ParticleTypes.END_ROD,
				caster.getX(), caster.getY() + 1.0, caster.getZ(),
				30, 0.5, 1.0, 0.5, 0.05);
	}

	/**
	 * Rite of the Silver Veil (Lesser, 0 blood):
	 * Grants the Silver Ward mob effect for 30 minutes and adds 10 purity.
	 * Requires purity >= 25 (Tainted stage).
	 */
	private static void completeSilverVeil(ServerLevel sLevel, ServerPlayer caster) {
		HemoCapabilityAccess.getUnstainedProgress(caster).ifPresent(unstained -> {
			if (unstained.getPurity() < 25.0f) {
				caster.displayClientMessage(
						Component.literal("Your soul is not yet pure enough to bear the Silver Veil.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}
			unstained.addPurity(10.0f);
			UnstainedProgressEvents.syncProgress(caster, unstained);

			// Apply Silver Ward effect (amplifier 1, 30 minutes)
			caster.addEffect(new MobEffectInstance(
					EffectInit.silver_ward, SILVER_VEIL_DURATION_TICKS, 1, false, true, true));

			caster.displayClientMessage(
					Component.literal("A veil of pale silver light surrounds you. Blood magic cannot touch you.")
							.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
					false);
			sLevel.sendParticles(ParticleTypes.END_ROD,
					caster.getX(), caster.getY() + 1.0, caster.getZ(),
					50, 1.0, 1.5, 1.0, 0.02);
		});
	}
	/**
	 * Rite of Clarity Ascension (Greater, 0 blood):
	 * Unlocks the clarity phase for a fully purified Unstained player.
	 * Requires purity = 100 (Purified stage).
	 */
	private static void completeClarityAscension(ServerLevel sLevel, ServerPlayer caster) {
		HemoCapabilityAccess.getUnstainedProgress(caster).ifPresent(unstained -> {
			if (!unstained.isPurified()) {
				caster.displayClientMessage(
						Component.literal("You must achieve full purity before ascending to clarity.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}
			if (unstained.hasClarityUnlocked()) {
				caster.displayClientMessage(
						Component.literal("Clarity has already been unlocked within you.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}

			unstained.setClarityUnlocked(true);
			UnstainedProgressEvents.syncProgress(caster, unstained);
			if (PathMutualExclusionHelper.enforceHarbingerResetOnClarity(caster, unstained)) {
				caster.displayClientMessage(
						Component.literal("The Hematic Order falls silent within you. Your former rank is washed away.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
			}
			if (KnownStillArtEvents.grantArt(caster, StillArtInit.silver_rebuke.get())) {
				caster.displayClientMessage(
						Component.literal("A first Still Art settles into you: Silver Rebuke.")
								.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
						false);
			}

			HemoCapabilityAccess.getEquipment(caster).ifPresent(scars -> {
				ItemStack charmStack = scars.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX);
				if (charmStack.is(ItemInit.charm_of_vascularium.get())) {
					scars.setStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX, ItemStack.EMPTY);
					caster.displayClientMessage(
							Component.literal("You feel the parasitic necklace burn away, starved of nutrients from your now silvery vital humor.")
									.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
							false);
				}
			});

			caster.displayClientMessage(
					Component.literal("The veil parts. True sight is yours â€” clarity has been unlocked.")
							.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
					false);
			caster.displayClientMessage(
					Component.literal("Blood magic is no longer your domain. Walk the path of enlightenment.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			sLevel.sendParticles(ParticleTypes.END_ROD,
					caster.getX(), caster.getY() + 1.0, caster.getZ(),
					80, 1.5, 2.0, 1.5, 0.03);

			com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.grantIfNotDone(
					caster, com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED);
		});
	}

	private static void completeClosedVein(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(CLOSED_VEIN_RADIUS);
		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(ServerPlayer.class, area, ServerPlayer::isAlive);
		int[] blessed = { 0 };

		for (ServerPlayer target : nearbyPlayers) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasBegunPurification()) return;
				target.removeEffect(EffectInit.blood_loss);
				target.addEffect(new MobEffectInstance(EffectInit.silver_ward, CLOSED_VEIN_EFFECT_TICKS, 1, false, true, true));
				if (progress.hasClarityUnlocked()) {
					progress.addClarity(2.0f);
					UnstainedProgressEvents.syncProgress(target, progress);
				}
				target.displayClientMessage(Component.literal("The vein closes. The old leak is denied.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
				blessed[0]++;
			});
		}

		for (Monster monster : sLevel.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 1, false, true, true));
			monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 0, false, true, true));
		}

		if (HemoCapabilityAccess.getUnstainedProgress(caster)
				.map(progress -> progress.hasClarityUnlocked())
				.orElse(false)
				&& KnownStillArtEvents.grantArt(caster, StillArtInit.lethean_mute.get())) {
			caster.displayClientMessage(Component.literal("A Still Art settles into the closed vessel: Lethean Mute.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		}

		caster.displayClientMessage(Component.literal("The Rite of the Closed Vein blesses " + blessed[0] + " Unstained soul(s).")
				.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), false);
		sLevel.playSound(null, center, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.2f, 0.8f);
		sLevel.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				120, CLOSED_VEIN_RADIUS * 0.25, 2.0, CLOSED_VEIN_RADIUS * 0.25, 0.015);
	}

	private static void completeAntisepticGround(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		PaleConsecrationSavedData data = PaleConsecrationSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + ANTISEPTIC_GROUND_DURATION_TICKS;

		PaleConsecrationSavedData.ConsecrationEntry entry = new PaleConsecrationSavedData.ConsecrationEntry(
				caster.getUUID(), center, dimension, ANTISEPTIC_GROUND_RADIUS, expiryTick);
		data.addEntry(entry);

		if (KnownStillArtEvents.grantArt(caster, StillArtInit.still_pulse.get())) {
			caster.displayClientMessage(Component.literal("A Still Art beats once, then quiets: Still Pulse.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		}
		if (KnownStillArtEvents.grantArt(caster, StillArtInit.pale_diagnosis.get())) {
			caster.displayClientMessage(Component.literal("A diagnostic stillness opens behind your eyes: Pale Diagnosis.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		}

		caster.addEffect(new MobEffectInstance(EffectInit.verdigris_aura, 12000, 1, false, true, true));
		caster.displayClientMessage(Component.literal("Antiseptic ground takes hold for 15 minutes.")
				.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		sLevel.playSound(null, center, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0f, 1.4f);
		sLevel.sendParticles(ParticleTypes.SCRAPE, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				120, ANTISEPTIC_GROUND_RADIUS * 0.35, 0.5, ANTISEPTIC_GROUND_RADIUS * 0.35, 0.01);
	}

	private static void completeGlassLungs(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(GLASS_LUNGS_RADIUS);
		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(ServerPlayer.class, area, ServerPlayer::isAlive);
		int[] blessed = { 0 };

		for (ServerPlayer target : nearbyPlayers) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasBegunPurification()) return;
				target.removeEffect(MobEffects.POISON);
				target.removeEffect(MobEffects.WITHER);
				target.clearFire();
				target.setAirSupply(target.getMaxAirSupply());
				target.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, GLASS_LUNGS_EFFECT_TICKS, 0, false, true, true));
				target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 2400, 0, false, true, true));
				if (progress.hasClarityUnlocked()) {
					progress.addClarity(3.0f);
					UnstainedProgressEvents.syncProgress(target, progress);
				}
				blessed[0]++;
			});
		}

		if (HemoCapabilityAccess.getUnstainedProgress(caster)
				.map(progress -> progress.getClarity() >= 50.0f)
				.orElse(false)) {
			if (KnownStillArtEvents.grantArt(caster, StillArtInit.memory_shear.get())) {
				caster.displayClientMessage(Component.literal("A Still Art thins the breath of hostile memory: Memory Shear.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
			}
			if (KnownStillArtEvents.grantArt(caster, StillArtInit.absolving_step.get())) {
				caster.displayClientMessage(Component.literal("A Still Art learns the shape of escape: Absolving Step.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
			}
		}

		caster.displayClientMessage(Component.literal("Glass lungs open in " + blessed[0] + " Unstained body(s).")
				.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), false);
		sLevel.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
		sLevel.sendParticles(ParticleTypes.SNOWFLAKE, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				140, GLASS_LUNGS_RADIUS * 0.25, 2.5, GLASS_LUNGS_RADIUS * 0.25, 0.015);
	}

	private static void completeMoonWashedCopper(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		boolean isNight = sLevel.getDayTime() % 24000L >= 13000L && sLevel.getDayTime() % 24000L <= 23000L;
		float clarityGain = isNight ? 10.0f : 5.0f;
		AABB area = new AABB(center).inflate(MOON_WASHED_COPPER_RADIUS);
		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(ServerPlayer.class, area, ServerPlayer::isAlive);
		int[] blessed = { 0 };

		for (ServerPlayer target : nearbyPlayers) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasClarityUnlocked()) return;
				progress.addClarity(clarityGain);
				UnstainedProgressEvents.syncProgress(target, progress);
				target.addEffect(new MobEffectInstance(EffectInit.verdigris_aura, MOON_WASHED_COPPER_EFFECT_TICKS, 2, false, true, true));
				target.addEffect(new MobEffectInstance(EffectInit.silver_ward, MOON_WASHED_COPPER_EFFECT_TICKS, 1, false, true, true));
				blessed[0]++;
			});
		}

		if (KnownStillArtEvents.grantArt(caster, StillArtInit.quietus_bell.get())) {
			caster.displayClientMessage(Component.literal("A Still Art rings in moon-washed copper: Quietus Bell.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		}
		if (HemoCapabilityAccess.getUnstainedProgress(caster)
				.map(progress -> progress.isEnlightened())
				.orElse(false)
				&& KnownStillArtEvents.grantArt(caster, StillArtInit.autoimmune_edge.get())) {
			caster.displayClientMessage(Component.literal("The immune edge answers. Handle it carefully.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		}

		String moonText = isNight ? "under the moon" : "without moonlight";
		caster.displayClientMessage(Component.literal("Moon-washed copper blesses " + blessed[0] + " clarity-bearer(s) " + moonText + ".")
				.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		sLevel.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.4f, isNight ? 1.7f : 1.25f);
		sLevel.sendParticles(ParticleTypes.WAX_OFF, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				200, MOON_WASHED_COPPER_RADIUS * 0.22, 3.5, MOON_WASHED_COPPER_RADIUS * 0.22, 0.018);
	}

	/**
	 * Rite of Lethean Judgment (Grand, 0 blood):
	 * Offensive rite that disrupts all blood-active Hemomancers within 16
	 * blocks, applying Hemolysis and stripping active blood effects.
	 */
	private static void completeLetheanJudgment(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(LETHEAN_JUDGMENT_RADIUS);
		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(
				ServerPlayer.class, area, p -> p != caster);

		int[] affected = {0};
		for (ServerPlayer target : nearbyPlayers) {
			HemoCapabilityAccess.getBloodVolume(target).ifPresent(volume -> {
				if (volume.isActive()) {
					// Apply Hemolysis effect (amplifier 2, 30 seconds)
					target.addEffect(new MobEffectInstance(
							EffectInit.hemolysis, 600, 2, false, true, true));

					// Disrupt vascular system
					HemoCapabilityAccess.getVascularSystem(target).ifPresent(vascular -> {
						Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
						for (EnumVeinSections section : EnumVeinSections.values()) {
							float current = sys.getOrDefault(section, 100f);
							sys.put(section, Math.max(0f, current - 30f));
						}
						vascular.setVascularSystem(sys);
						VascularSystemEvents.syncVascular(target, vascular);
					});

					target.displayClientMessage(
							Component.literal("A wave of silver light burns through your veins!")
									.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
							false);
					affected[0]++;
				}
			});
		}

		String msg = affected[0] > 0
				? "Lethean judgment descends. " + affected[0] + " hemomancer(s) have been purged."
				: "Lethean judgment descends, but no blood-wielders were found nearby.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);
		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				100, LETHEAN_JUDGMENT_RADIUS * 0.5, 2.0, LETHEAN_JUDGMENT_RADIUS * 0.5, 0.01);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  SILVER DAWN â€” Persistent Cleansed Zone
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Radius of the Silver Dawn cleansing zone in blocks. */
	private static final int SILVER_DAWN_RADIUS = 8;
	/** Duration of the Verdigris Aura granted by Silver Dawn (10 minutes). */
	private static final int SILVER_DAWN_AURA_DURATION = 12000;
	/** Amplifier of the Verdigris Aura granted by Silver Dawn. */
	private static final int SILVER_DAWN_AURA_AMPLIFIER = 2;

	/**
	 * Lazy block-conversion map for Silver Dawn / Consecration.
	 * Maps blood-faction blocks to their cleansed equivalents.
	 * Unstained rites have zero blood cost by design â€” they draw
	 * from purity and clarity, not from the hemomancer's reservoir.
	 */
	private static Map<Block, Block> SILVER_DAWN_CONVERSIONS;

	private static Map<Block, Block> getSilverDawnConversions() {
		if (SILVER_DAWN_CONVERSIONS == null) {
			SILVER_DAWN_CONVERSIONS = Map.of(
					BlockInit.venous_stone.get(), BlockInit.cleansed_stone.get(),
					BlockInit.sanguine_glass.get(), BlockInit.cleansed_sanguine_glass.get(),
					BlockInit.infested_venous_stone.get(), BlockInit.cleansed_stone.get(),
					BlockInit.hematic_iron_block.get(), BlockInit.pale_silver_block.get()
			);
		}
		return SILVER_DAWN_CONVERSIONS;
	}

	/**
	 * Rite of the Silver Dawn: converts blood-faction blocks in a radius
	 * around the altar into their cleansed equivalents and grants a
	 * long-duration Verdigris Aura to the caster.
	 */
	private static void completeSilverDawn(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		// Require clarity to perform this rite
		boolean hasClarityUnlocked = HemoCapabilityAccess.getUnstainedProgress(caster)
				.map(p -> p.hasClarityUnlocked()).orElse(false);
		if (!hasClarityUnlocked) {
			caster.displayClientMessage(
					Component.literal("You must have unlocked Clarity to perform the Rite of the Silver Dawn.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Convert blood-faction blocks in radius
		int converted = 0;
		Map<Block, Block> conversions = getSilverDawnConversions();

		for (int x = -SILVER_DAWN_RADIUS; x <= SILVER_DAWN_RADIUS; x++) {
			for (int y = -SILVER_DAWN_RADIUS / 2; y <= SILVER_DAWN_RADIUS / 2; y++) {
				for (int z = -SILVER_DAWN_RADIUS; z <= SILVER_DAWN_RADIUS; z++) {
					BlockPos pos = center.offset(x, y, z);
					Block block = sLevel.getBlockState(pos).getBlock();
					Block replacement = conversions.get(block);
					if (replacement != null) {
						sLevel.setBlock(pos, replacement.defaultBlockState(), 3);
						converted++;
					}
				}
			}
		}

		// Grant extended Verdigris Aura
		caster.addEffect(new MobEffectInstance(
				EffectInit.verdigris_aura, SILVER_DAWN_AURA_DURATION, SILVER_DAWN_AURA_AMPLIFIER, false, true, true));

		// Grant purity/clarity boost
		HemoCapabilityAccess.getUnstainedProgress(caster).ifPresent(progress -> {
			progress.addClarity(5.0f);
			UnstainedProgressEvents.syncProgress(caster, progress);
		});

		String msg = converted > 0
				? "Silver dawn breaks. " + converted + " block(s) have been cleansed."
				: "Silver dawn breaks, but no blood-stained blocks were found nearby.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);

		// Visual burst
		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				150, SILVER_DAWN_RADIUS * 0.5, 3.0, SILVER_DAWN_RADIUS * 0.5, 0.02);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				80, SILVER_DAWN_RADIUS * 0.4, 1.5, SILVER_DAWN_RADIUS * 0.4, 0.01);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  STILL WATERS â€” 5-minute zone of reduced magic damage
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Radius of the Still Waters zone in blocks. */
	private static final int STILL_WATERS_RADIUS = 16;
	/** Duration of the Still Waters zone in ticks (5 minutes). */
	private static final long STILL_WATERS_DURATION_TICKS = 6000L;

	/**
	 * Rite of Still Waters (Minor, 0 blood):
	 * Creates a 5-minute zone around the altar within which all magic damage
	 * is reduced by 30%, countering Sanguine Dominion bleeds and other threats.
	 */
	private static void completeStillWaters(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		StillWatersSavedData data = StillWatersSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + STILL_WATERS_DURATION_TICKS;

		StillWatersSavedData.StillWatersEntry entry = new StillWatersSavedData.StillWatersEntry(
				caster.getUUID(), center, dimension, STILL_WATERS_RADIUS, expiryTick);
		data.addEntry(entry);

		long durationMinutes = STILL_WATERS_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The waters grow still. Magic damage is reduced by 30% within "
						+ STILL_WATERS_RADIUS + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				60, STILL_WATERS_RADIUS * 0.3, 1.5, STILL_WATERS_RADIUS * 0.3, 0.005);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  PALE CONSECRATION â€” 10-minute zone of hostile mob denial
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Radius of the Pale Consecration zone in blocks. */
	private static final int PALE_CONSECRATION_RADIUS = 8;
	/** Duration of the Pale Consecration zone in ticks (10 minutes). */
	private static final long PALE_CONSECRATION_DURATION_TICKS = 12000L;

	/**
	 * Rite of Pale Consecration (Lesser, 0 blood):
	 * Consecrates the ground within a radius. Hostile mobs inside take periodic
	 * damage and Slowness I. Lasts 10 minutes.
	 */
	private static void completePaleConsecration(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		PaleConsecrationSavedData data = PaleConsecrationSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + PALE_CONSECRATION_DURATION_TICKS;

		PaleConsecrationSavedData.ConsecrationEntry entry = new PaleConsecrationSavedData.ConsecrationEntry(
				caster.getUUID(), center, dimension, PALE_CONSECRATION_RADIUS, expiryTick);
		data.addEntry(entry);

		long durationMinutes = PALE_CONSECRATION_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The ground is consecrated. Hostile creatures will be seared within "
						+ PALE_CONSECRATION_RADIUS + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				80, PALE_CONSECRATION_RADIUS * 0.4, 1.5, PALE_CONSECRATION_RADIUS * 0.4, 0.01);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				40, PALE_CONSECRATION_RADIUS * 0.3, 0.5, PALE_CONSECRATION_RADIUS * 0.3, 0.005);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  SILTHMERE'S REMEMBRANCE â€” one-time burst purity + Silver Ward
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Radius within which Unstained players receive the Remembrance burst. */
	private static final int REMEMBRANCE_RADIUS = 32;
	/** Purity granted per Unstained player by the burst. */
	private static final float REMEMBRANCE_PURITY = 5.0f;
	/** Silver Ward refresh duration in ticks (15 minutes). */
	private static final int REMEMBRANCE_SILVER_WARD_DURATION = 18000;

	/**
	 * Rite of Silthmere's Remembrance (Greater, 0 blood):
	 * A one-time burst. All Unstained players within 32 blocks immediately gain
	 * +5 purity and have their Silver Ward refreshed or applied (amplifier 1).
	 */
	private static void completeSilthmereRemembrance(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(REMEMBRANCE_RADIUS);
		List<ServerPlayer> nearby = sLevel.getEntitiesOfClass(
				ServerPlayer.class, area, p -> true);

		int[] affected = {0};
		for (ServerPlayer target : nearby) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasBegunPurification()) return;

				progress.addPurity(REMEMBRANCE_PURITY);
				UnstainedProgressEvents.syncProgress(target, progress);

				target.addEffect(new MobEffectInstance(
						EffectInit.silver_ward, REMEMBRANCE_SILVER_WARD_DURATION, 1,
						false, true, true));

				target.displayClientMessage(
						Component.literal("Silthmere's memory washes over you. Purity blooms within.")
								.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
						false);
				affected[0]++;
			});
		}

		String msg = affected[0] > 0
				? "Silthmere remembers. " + affected[0] + " Unstained soul(s) have been blessed."
				: "Silthmere's remembrance echoes, but no Unstained walk near enough to hear it.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				120, REMEMBRANCE_RADIUS * 0.3, 3.0, REMEMBRANCE_RADIUS * 0.3, 0.02);
	}

	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
	//  LETHE COVENANT â€” grand 30-minute Unstained domain
	// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

	/** Chunk radius of the Lethe Covenant domain. */
	private static final int LETHE_COVENANT_CHUNK_RADIUS = 5;
	/** Duration of the Lethe Covenant domain in ticks (30 minutes). */
	private static final long LETHE_COVENANT_DURATION_TICKS = 36000L;

	/**
	 * Rite of the Lethe Covenant (Grand, 0 blood):
	 * Establishes a grand Unstained domain for 30 minutes within a 5-chunk radius.
	 * The domain suppresses mob spawns, shields Silver Ward players from bleed,
	 * and slowly grows the purity of Unstained players inside it.
	 */
	private static void completeLetheCovenantRite(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		LetheCovenantSavedData data = LetheCovenantSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + LETHE_COVENANT_DURATION_TICKS;

		LetheCovenantSavedData.CovenantEntry entry = new LetheCovenantSavedData.CovenantEntry(
				caster.getUUID(), center, dimension, LETHE_COVENANT_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		int blockRadius = LETHE_COVENANT_CHUNK_RADIUS * 16;
		long durationMinutes = LETHE_COVENANT_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The Lethe Covenant is sealed! A domain of stillness spreads "
						+ blockRadius + " blocks in every direction for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
				false);
		caster.displayClientMessage(
				Component.literal("Spawns are halved. Bleed cannot touch those warded in silver. Purity grows.")
						.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
				false);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				200, blockRadius * 0.2, 4.0, blockRadius * 0.2, 0.02);
	}
	/**
	 * Rite of the Lethean Tide (Greater, 0 blood, Unstained):
	 * Forcibly ends an active Blood Moon, broadcasts a cleansing message,
	 * and grants the caster +10 purity. Requires purity >= 50.
	 */
	private static void completeLetheanTide(ServerLevel sLevel, ServerPlayer caster) {
		HemoCapabilityAccess.getUnstainedProgress(caster).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification() || unstained.getPurity() < 50f) {
				caster.displayClientMessage(
						Component.literal("The tide will not answer â€” your purity is insufficient.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}

			ServerLevel overworld = sLevel.getServer().overworld();
			BloodMoonSavedData bloodMoonData = BloodMoonSavedData.get(overworld);
			if (!bloodMoonData.isActive()) {
				caster.displayClientMessage(
						Component.literal("There is no Blood Moon to cleanse.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
				return;
			}

			bloodMoonData.stop();
			for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
				p.sendSystemMessage(Component.translatable("hemomancy.lethean_tide.end")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
			}
			PacketDistributor.sendToAllPlayers(new PacketSyncBloodMoon(false));

			unstained.addPurity(10f);
			UnstainedProgressEvents.syncProgress(caster, unstained);

			sLevel.sendParticles(ParticleTypes.END_ROD,
					caster.getX(), caster.getY() + 1.0, caster.getZ(),
					120, 7.0, 5.0, 7.0, 0.04);
			caster.displayClientMessage(
					Component.literal("The Lethean Tide rises â€” the Blood Moon is washed from the sky.")
							.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
					false);
		});
	}

	// ════════════════════════════════════════════════════════════════════════
	//  PALE VIGIL — clarity burst for all nearby Unstained (Vigilant tier)
	// ════════════════════════════════════════════════════════════════════════

	/**
	 * Rite of the Pale Vigil (Greater, 0 blood, level 7 / Vigilant):
	 * Our Lady's gaze falls on all Unstained within 40 blocks. Each player
	 * whose clarity has been unlocked gains +10 clarity, Silver Ward (amp 2,
	 * 30 min), and Verdigris Aura (amp 2, 30 min).
	 */
	private static void completePaleVigil(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		AABB area = new AABB(center).inflate(PALE_VIGIL_RADIUS);
		List<ServerPlayer> nearby = sLevel.getEntitiesOfClass(ServerPlayer.class, area, p -> true);

		int[] affected = { 0 };
		for (ServerPlayer target : nearby) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasClarityUnlocked()) return;

				progress.addClarity(PALE_VIGIL_CLARITY);
				UnstainedProgressEvents.syncProgress(target, progress);

				target.addEffect(new MobEffectInstance(
						EffectInit.silver_ward, PALE_VIGIL_EFFECT_TICKS, 2, false, true, true));
				target.addEffect(new MobEffectInstance(
						EffectInit.verdigris_aura, PALE_VIGIL_EFFECT_TICKS, 2, false, true, true));

				target.displayClientMessage(
						Component.literal("The Pale Vigil watches. Clarity deepens within you.")
								.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
						false);
				affected[0]++;
			});
		}

		String msg = affected[0] > 0
				? "The Pale Vigil holds. " + affected[0] + " soul(s) draw clarity from its light."
				: "The Pale Vigil shines, but no clarity-bearers stand near enough to receive it.";
		caster.displayClientMessage(
				Component.literal(msg).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), false);

		com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.grantIfNotDone(
				caster, com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.ADV_VIGILANT);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				160, PALE_VIGIL_RADIUS * 0.25, 4.0, PALE_VIGIL_RADIUS * 0.25, 0.02);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				80, PALE_VIGIL_RADIUS * 0.2, 2.0, PALE_VIGIL_RADIUS * 0.2, 0.01);
	}

	// ════════════════════════════════════════════════════════════════════════
	//  LETHEAN FONT — pinnacle domain, 8-chunk / 1-hour (Enlightened tier)
	// ════════════════════════════════════════════════════════════════════════

	/**
	 * Rite of the Lethean Font (Grand, 0 blood, level 8 / Enlightened):
	 * Opens the primordial source of Lethe's purifying waters. Establishes a
	 * Lethe Covenant domain spanning 8 chunks for 1 hour (stronger than the
	 * base covenant). All clarity-bearing Unstained within 50 blocks receive
	 * +20 clarity, Silver Ward (amp 3, 1 hour), and Verdigris Aura (amp 3,
	 * 1 hour). The full inverse of Sanguine Dominion.
	 */
	private static void completeLetheanFont(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		LetheCovenantSavedData data = LetheCovenantSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + LETHEAN_FONT_DOMAIN_TICKS;

		LetheCovenantSavedData.CovenantEntry entry = new LetheCovenantSavedData.CovenantEntry(
				caster.getUUID(), center, dimension, LETHEAN_FONT_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		// Clarity burst to all nearby clarity-bearers
		AABB area = new AABB(center).inflate(LETHEAN_FONT_BURST_RADIUS);
		List<ServerPlayer> nearby = sLevel.getEntitiesOfClass(ServerPlayer.class, area, p -> true);

		int[] affected = { 0 };
		for (ServerPlayer target : nearby) {
			HemoCapabilityAccess.getUnstainedProgress(target).ifPresent(progress -> {
				if (!progress.hasClarityUnlocked()) return;

				progress.addClarity(LETHEAN_FONT_CLARITY);
				UnstainedProgressEvents.syncProgress(target, progress);

				target.addEffect(new MobEffectInstance(
						EffectInit.silver_ward, LETHEAN_FONT_EFFECT_TICKS, 3, false, true, true));
				target.addEffect(new MobEffectInstance(
						EffectInit.verdigris_aura, LETHEAN_FONT_EFFECT_TICKS, 3, false, true, true));

				target.displayClientMessage(
						Component.literal("Lethe's source flows through you. The font of all clarity is open.")
								.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
						false);
				affected[0]++;
			});
		}

		int blockRadius = LETHEAN_FONT_CHUNK_RADIUS * 16;
		long durationMinutes = LETHEAN_FONT_DOMAIN_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The Lethean Font is open! A domain of ultimate stillness spreads "
						+ blockRadius + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
				false);
		if (affected[0] > 0) {
			caster.displayClientMessage(
					Component.literal(affected[0] + " soul(s) have been blessed by the Font.")
							.withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
					false);
		}

		com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.grantIfNotDone(
				caster, com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.ADV_ENLIGHTENED_SEEKER);

		sLevel.sendParticles(ParticleTypes.END_ROD,
				center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
				300, blockRadius * 0.15, 5.0, blockRadius * 0.15, 0.025);
		sLevel.sendParticles(ParticleTypes.SCRAPE,
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
				150, blockRadius * 0.1, 3.0, blockRadius * 0.1, 0.015);
	}

}
