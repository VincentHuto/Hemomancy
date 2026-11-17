package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.PhanstmalBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SheolicBastionBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryWorldEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.entity.summon.PhantasmalEchoEntity;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.event.ToggleableSkillEvents;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SaprovittaVestigiumItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleSpore;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.Reach;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.KinbudRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.KinbudSecondHead;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Step 9: who each representative combat effect reaches, one target at a time. A row lists the columns its effect
 * touched: a hostile mob, the caster's tamed pet, a mob on the caster's scoreboard team, the caster's own bound
 * puppet, a boss, and another survival player with PvP on and off. Approving a targeting decision in the ledger
 * (G-1, F-16, F-17, G-2b) changes a row's expectation in the same commit.
 */
@GameTestHolder("combat_targeting_validation")
@PrefixGameTestTemplate(false)
public final class CombatTargetingMatrixGameTests {
	static final String NAMESPACE = "combat_targeting_validation";
	private static final String ROOM = "combat_targeting_room";
	private static final Vec3 CASTER = new Vec3(8.5D, 2.0D, 8.5D);
	private static final Vec3 TARGET_OFFSET = new Vec3(2.5D, 0.0D, 0.0D);

	private CombatTargetingMatrixGameTests() {
	}

	private enum Column {
		HOSTILE("hostile"), PET("pet"), ALLY("ally"), PUPPET("puppet"), BOSS("boss"), PVP_ON("pvp-on"), PVP_OFF("pvp-off");

		final String label;

		Column(String label) {
			this.label = label;
		}
	}

	@FunctionalInterface
	private interface Strike {
		boolean reaches(GameTestHelper h, ServerPlayer caster, LivingEntity target);
	}

	/** The shared manipulation damage gate, {@code canHarm} itself. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_manipulation", timeoutTicks = 100)
	public static void manipulationSharedHurt(GameTestHelper h) {
		matrix(h, "ManipulationCombatHelper.hurt", "hostile boss pvp-on", caster -> {},
				(helper, caster, target) -> damaged(target, () -> ManipulationCombatHelper.hurt(
						ManipulationInit.hematic_flare.get(), caster, target, helper.getLevel(), 2.0F)));
	}

	/** A Drudge's nearest-Monster work lookup (Blood Shot), owned by the caster. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_drudge", timeoutTicks = 100)
	public static void drudgeBloodShot(GameTestHelper h) {
		matrix(h, "drudge blood_shot", "hostile boss", caster -> {},
				(helper, caster, target) -> drudgeWork(helper, caster, target, ManipulationInit.blood_shot.get()));
	}

	private static boolean drudgeWork(GameTestHelper h, ServerPlayer caster, LivingEntity target, BloodManipulation work) {
		DrudgeEntity drudge = EntityInit.drudge.get().create(h.getLevel());
		drudge.setOwnerUUID(caster.getUUID());
		drudge.setNoAi(true);
		drudge.moveTo(caster.position().add(-1.5D, 0.0D, 0.0D));
		h.getLevel().addFreshEntity(drudge);
		try {
			return damaged(target, () -> work.getDrudgeAction().orElseThrow()
					.execute(drudge, h.getLevel(), drudge.blockPosition(), 6));
		} finally {
			drudge.discard();
		}
	}

	/** Silent Archon: Silent Severance's area burst. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_severance", timeoutTicks = 100)
	public static void armourSilentSeverance(GameTestHelper h) {
		matrix(h, "Silent Severance", "hostile boss pvp-on", caster -> {},
				(helper, caster, target) -> touched(target,
						() -> SilentArchonArmorAbilityHandler.activateSilentArchonSeverance(caster)));
	}

	/** Prismatic set: the flash when the wearer is struck, by a cow that is not a column. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_prismatic", timeoutTicks = 100)
	public static void armourPrismaticFlash(GameTestHelper h) {
		matrix(h, "Prismatic flash", "hostile boss", caster -> {
			caster.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.prismatic_helm.get()));
			caster.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.prismatic_chestplate.get()));
			caster.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.prismatic_leggings.get()));
			caster.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.prismatic_boots.get()));
		}, (helper, caster, target) -> {
			var attacker = EntityType.COW.create(helper.getLevel());
			attacker.setNoAi(true);
			attacker.moveTo(caster.position().add(0.0D, 0.0D, 2.0D));
			helper.getLevel().addFreshEntity(attacker);
			com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns.of(caster).setUntil(com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns.PRISMATIC_FLASH, 0L);
			try {
				return touched(target, () -> ArmorSetBonusHandler.onPlayerHurt(new LivingDamageEvent.Pre(caster,
						new DamageContainer(caster.damageSources().mobAttack(attacker), 1.0F))));
			} finally {
				attacker.discard();
			}
		});
	}

	/** Still Art: Autoimmune Edge's area cut. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_autoimmune", timeoutTicks = 100)
	public static void stillArtAutoimmuneEdge(GameTestHelper h) {
		StillArt art = StillArtInit.autoimmune_edge.get();
		matrix(h, "Still Art autoimmune_edge", "hostile boss pvp-on", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Still Art: Lethean Mute's Monster filter. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_lethean", timeoutTicks = 100)
	public static void stillArtLetheanMute(GameTestHelper h) {
		StillArt art = StillArtInit.lethean_mute.get();
		matrix(h, "Still Art lethean_mute", "hostile boss", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/**
	 * Kinbud's Second Head: body, not a summon, so it answers to {@code canHarmFor} with the host as its source and
	 * strikes only hostiles (monsters, or mobs hunting the host). Players are never its target.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_second_head", timeoutTicks = 100)
	public static void kinbudSecondHead(GameTestHelper h) {
		matrix(h, "Kinbud Second Head", "hostile boss", caster -> {}, (helper, caster, target) -> damaged(target, () -> {
			KinbudSecondHead.bud(caster, 1, KinbudRules.HEAD_TICKS);
			KinbudSecondHead.tick(caster, 60, 0);
			KinbudSecondHead.clear(caster.getUUID());
		}));
	}

	/**
	 * Dead Man's Rope's Red Rope: a looked-at ally is hauled in as a rescue and anything the host may harm is pulled and
	 * rooted, so it reaches allies and PvP-on players but never a PvP-off player.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_red_rope", timeoutTicks = 100)
	public static void deadMansRopeRedRope(GameTestHelper h) {
		matrix(h, "Dead Man's Rope Red Rope", "hostile pet ally puppet boss pvp-on", caster -> {},
				(helper, caster, target) -> touched(target, () -> {
					ItemStack rope = new ItemStack(MorphlingStrains.DEAD_MANS_ROPE.item().get());
					MorphlingItem.setPrimalized(rope);
					((IMorphling) rope.getItem()).tryUse(caster, InteractionHand.MAIN_HAND, rope, helper.getLevel());
				}));
	}

	/** Scar: Glacier (Congeatio, tier 2) slows the Monsters around its wearer. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_glacier", timeoutTicks = 100)
	public static void scarGlacierSlow(GameTestHelper h) {
		matrix(h, "scar Glacier slow", "hostile boss", caster -> {}, (helper, caster, target) -> {
			caster.tickCount = 40;
			return touched(target, () -> ScarInit.scar_glacier.get().onWornTick(caster, ItemStack.EMPTY));
		});
	}

	/** Scar: Thorn (Ferric) answers whoever struck its wearer. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_thorn", timeoutTicks = 100)
	public static void scarThornRetaliation(GameTestHelper h) {
		matrix(h, "scar Thorn retaliation", "hostile pet ally puppet boss pvp-on", caster -> {},
				(helper, caster, target) -> damaged(target, () -> ScarInit.scar_thorn.get().onPlayerDefend(caster, target)));
	}

	/** Still Art: Silver Rebuke pushes and slows what stands in front of the caster. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_silver_rebuke", timeoutTicks = 100)
	public static void stillArtSilverRebuke(GameTestHelper h) {
		StillArt art = StillArtInit.silver_rebuke.get();
		matrix(h, "Still Art silver_rebuke", "hostile boss pvp-on", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Still Art: Still Pulse's Monster filter. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_still_pulse", timeoutTicks = 100)
	public static void stillArtStillPulse(GameTestHelper h) {
		StillArt art = StillArtInit.still_pulse.get();
		matrix(h, "Still Art still_pulse", "hostile boss", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Still Art: Memory Shear's nearest-Monster pick. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_memory_shear", timeoutTicks = 100)
	public static void stillArtMemoryShear(GameTestHelper h) {
		StillArt art = StillArtInit.memory_shear.get();
		matrix(h, "Still Art memory_shear", "hostile boss", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Still Art: Quietus Bell's Monster filter. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_quietus_bell", timeoutTicks = 100)
	public static void stillArtQuietusBell(GameTestHelper h) {
		StillArt art = StillArtInit.quietus_bell.get();
		matrix(h, "Still Art quietus_bell", "hostile boss", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Fungal scar: the Saprovitta Vestigium's trail cuts the Monsters its moving wearer passes. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_saprovitta", timeoutTicks = 100)
	public static void scarSaprovittaTrail(GameTestHelper h) {
		matrix(h, "scar Saprovitta trail", "hostile boss", CombatTargetingMatrixGameTests::wearSaprovitta,
				(helper, caster, target) -> damaged(target, () -> saprovittaTrail(caster, target)));
	}

	/** Sheolic set: Crimson Retribution answers whoever struck its wearer. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_sheolic", timeoutTicks = 100)
	public static void armourSheolicRetribution(GameTestHelper h) {
		matrix(h, "Sheolic retribution", "hostile boss pvp-on", CombatTargetingMatrixGameTests::wearSheolic,
				(helper, caster, target) -> touched(target, () -> sheolicRetribution(caster, target)));
	}

	/** Technique: Crimson Wake slows the enemies a sprinting, wounded caster runs past. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_crimson_wake", timeoutTicks = 100)
	public static void techniqueCrimsonWake(GameTestHelper h) {
		matrix(h, "Crimson Wake", "hostile boss", caster -> HemoCapabilityAccess.getSkillProgress(caster)
				.setSkill(SkillPointInit.skill_crimson_wake, EnumSkillStates.UNLOCKED, 1), (helper, caster, target) -> touched(target, () -> {
					target.moveTo(caster.position().add(1.0D, 0.0D, 0.0D));
					caster.setSprinting(true);
					caster.setHealth(caster.getMaxHealth() * 0.4F);
					caster.tickCount = 4;
					ToggleableSkillEvents.playerTick(new PlayerTickEvent.Post(caster));
				}));
	}

	/** Unstained tool: the Pale Silver Bell's weakening toll. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_pale_bell", timeoutTicks = 100)
	public static void paleSilverBell(GameTestHelper h) {
		matrix(h, "Pale Silver Bell", "hostile boss", caster -> {
			HemoCapabilityAccess.getUnstainedProgress(caster).setBegunPurification(true);
			caster.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.pale_silver_bell.get()));
		}, (helper, caster, target) -> touched(target,
				() -> caster.getMainHandItem().use(helper.getLevel(), caster, InteractionHand.MAIN_HAND)));
	}

	/** Harbinger tool: the Sporitic Thurible's aura at full swing. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_thurible", timeoutTicks = 100)
	public static void sporiticThuribleAura(GameTestHelper h) {
		matrix(h, "Sporitic Thurible aura", "hostile boss", caster -> {},
				(helper, caster, target) -> touched(target, () -> invoke(SporiticThuribleEvents.class, "applyAura",
						new Class<?>[] {ServerPlayer.class, SporiticThuribleSpore.class, double.class},
						caster, SporiticThuribleSpore.values()[0], 1.0D)));
	}

	/** Thelemic memory: the moving Monsters Predatory Eyes would mark. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_predatory_eyes", timeoutTicks = 100)
	public static void thelemicPredatoryEyes(GameTestHelper h) {
		matrix(h, "Thelemic Predatory Eyes", "hostile boss", caster -> {}, (helper, caster, target) -> {
			target.setDeltaMovement(0.1D, 0.0D, 0.0D);
			return ThelemicMemoryWorldEvents.predatoryEyesPrey(caster, 16.0D, false).contains(target);
		});
	}

	/**
	 * Phantasmal set: Masquerade of the Forgotten sends two more echoes in to fight when a Monster is near, so eight
	 * echoes come out with four aggressive instead of two.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_phantasmal", timeoutTicks = 100)
	public static void armourPhantasmalHostilesNear(GameTestHelper h) {
		matrix(h, "Phantasmal hostiles near", "hostile boss", caster -> {}, (helper, caster, target) -> {
			PhanstmalBloodlustArmorAbilityHandler.activateMasqueradeOfTheForgotten(caster);
			List<PhantasmalEchoEntity> echoes = helper.getLevel().getEntitiesOfClass(PhantasmalEchoEntity.class,
					caster.getBoundingBox().inflate(8.0D));
			long aggressive = echoes.stream()
					.filter(echo -> echo.getBehaviorMode() == PhantasmalEchoEntity.BehaviorMode.AGGRESSIVE).count();
			echoes.forEach(PhantasmalEchoEntity::discard);
			return aggressive > 2;
		});
	}

	/** Unstained rite: the Rite of the Closed Vein slows and weakens the Monsters around its centre. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_closed_vein", timeoutTicks = 100)
	public static void unstainedClosedVeinRite(GameTestHelper h) {
		matrix(h, "Unstained Closed Vein rite", "hostile boss",
				caster -> HemoCapabilityAccess.getUnstainedProgress(caster).setBaselineRestored(true),
				(helper, caster, target) -> touched(target, () -> invoke(UnstainedCardinalRiteEvents.class, "completeClosedVein",
						new Class<?>[] {ServerLevel.class, ServerPlayer.class, BlockPos.class},
						helper.getLevel(), caster, caster.blockPosition())));
	}

	/** A Drudge's Activation Potential, owned by the caster. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_drudge_potential", timeoutTicks = 100)
	public static void drudgeActivationPotential(GameTestHelper h) {
		matrix(h, "drudge activation_potential", "hostile boss", caster -> {},
				(helper, caster, target) -> drudgeWork(helper, caster, target, ManipulationInit.activation_potential.get()));
	}

	/** Silent Severance's hit names its wearer (F-16). */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_credit_severance", timeoutTicks = 100)
	public static void silentSeveranceKillCredit(GameTestHelper h) {
		credit(h, "Silent Severance", "hostile", caster -> {},
				(helper, caster, target) -> touched(target, () -> SilentArchonArmorAbilityHandler.activateSilentArchonSeverance(caster)));
	}

	/** Autoimmune Edge's cut names its caster (F-16). */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_credit_autoimmune", timeoutTicks = 100)
	public static void autoimmuneEdgeKillCredit(GameTestHelper h) {
		StillArt art = StillArtInit.autoimmune_edge.get();
		credit(h, "Still Art autoimmune_edge", "hostile", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** The Saprovitta trail's cut names its wearer (F-16). */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_credit_saprovitta", timeoutTicks = 100)
	public static void saprovittaTrailKillCredit(GameTestHelper h) {
		credit(h, "scar Saprovitta trail", "hostile", CombatTargetingMatrixGameTests::wearSaprovitta,
				(helper, caster, target) -> damaged(target, () -> saprovittaTrail(caster, target)));
	}

	/** Crimson Retribution's burn names its wearer (F-16). */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_credit_sheolic", timeoutTicks = 100)
	public static void sheolicRetributionKillCredit(GameTestHelper h) {
		credit(h, "Sheolic retribution", "hostile", CombatTargetingMatrixGameTests::wearSheolic,
				(helper, caster, target) -> touched(target, () -> sheolicRetribution(caster, target)));
	}

	/**
	 * Living weapon: the Living Sickle's Short Reap spin. Its vanilla-melee filter admits the caster's puppet, but the
	 * hit is a weapon-school hit, so {@code SchoolCombatEvents.prepare} cancels it there.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_sickle", timeoutTicks = 100)
	public static void livingSickleSpin(GameTestHelper h) {
		matrix(h, "Living Sickle spin", "hostile boss pvp-on", caster -> caster.setItemInHand(InteractionHand.MAIN_HAND,
				new ItemStack(ItemInit.living_sickle.get())), (helper, caster, target) -> damaged(target,
				() -> caster.getMainHandItem().use(helper.getLevel(), caster, InteractionHand.MAIN_HAND)));
	}

	/**
	 * Brined Court Track 9: what a defending Court member (the Tidewarden, and the Edge-swimmer when built)
	 * turns on near home. Monsters nobody owns; never a player, a player's pet or puppet, or a player's team-mate.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_court", timeoutTicks = 100)
	public static void courtMemberDefence(GameTestHelper h) {
		matrix(h, "Court member defence", "hostile boss", caster -> {}, (helper, caster, target) -> {
			var member = EntityInit.court_members.get(com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.TIDEWARDEN)
					.get().create(helper.getLevel());
			member.setNoAi(true);
			member.moveTo(caster.position().add(-1.5D, 0.0D, 0.0D));
			helper.getLevel().addFreshEntity(member);
			try {
				return member.defendsAgainst(target);
			} finally {
				member.discard();
			}
		});
	}

	/**
	 * Each {@link Reach} preset gives its verdict for every kind of body, with PvP on and off. The two Monster filters
	 * reach every monster but the caster's allies (G-2b); STRICT is compared with canHarm's body as it stood before
	 * step 9.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_reach", timeoutTicks = 100)
	public static void reachPresetsGiveTheirVerdicts(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			var server = h.getLevel().getServer();
			boolean pvp = server.isPvpAllowed();
			ServerPlayer caster = DuctilisGameTests.player(h);
			caster.moveTo(h.absoluteVec(CASTER), -90.0F, 0.0F);
			var board = h.getLevel().getScoreboard();
			PlayerTeam team = board.addPlayerTeam("reach_" + caster.getId());
			board.addPlayerToTeam(caster.getScoreboardName(), team);
			List<LivingEntity> bodies = new ArrayList<>();
			try {
				for (Column column : Column.values()) bodies.add(spawn(h, column, caster, team));
				ServerPlayer creative = DuctilisGameTests.player(h);
				creative.setGameMode(GameType.CREATIVE);
				ServerPlayer spectator = DuctilisGameTests.player(h);
				spectator.setGameMode(GameType.SPECTATOR);
				Mob dying = EntityType.HUSK.create(h.getLevel());
				dying.setNoAi(true);
				dying.moveTo(caster.position().add(1.0D, 0.0D, 1.0D));
				h.getLevel().addFreshEntity(dying);
				dying.setHealth(0.0F);
				var stand = EntityType.ARMOR_STAND.create(h.getLevel());
				stand.moveTo(caster.position().add(-1.0D, 0.0D, 1.0D));
				h.getLevel().addFreshEntity(stand);
				bodies.addAll(List.of(creative, spectator, dying, stand, caster));
				for (boolean pvpOn : new boolean[] {true, false}) {
					server.setPvpAllowed(pvpOn);
					for (LivingEntity body : bodies) {
						String what = body.getType().getDescriptionId() + " pvp=" + pvpOn;
						boolean monster = body instanceof Monster && !ManipulationCombatHelper.allied(caster, body);
						same(h, "MONSTER_FILTER " + what, monster && body.isAlive(),
								body instanceof Monster && ManipulationCombatHelper.canHarm(caster, body, Reach.MONSTER_FILTER));
						same(h, "MONSTER_FILTER_ANY_STATE " + what, monster && !body.isSpectator(),
								body instanceof Monster && ManipulationCombatHelper.canHarm(caster, body, Reach.MONSTER_FILTER_ANY_STATE));
						same(h, "STRICT " + what, legacyCanHarm(caster, body), ManipulationCombatHelper.canHarm(caster, body));
					}
				}
				h.succeed();
			} finally {
				server.setPvpAllowed(pvp);
				board.removePlayerTeam(team);
				bodies.forEach(LivingEntity::discard);
			}
		});
	}

	private static boolean legacyCanHarm(Player player, LivingEntity target) {
		if (!target.isAlive() || target.isSpectator() || ManipulationCombatHelper.allied(player, target)) return false;
		if (target instanceof Player other) {
			return !other.isCreative() && player.canHarmPlayer(other)
					&& (player.getServer() == null || player.getServer().isPvpAllowed());
		}
		return true;
	}

	private static void same(GameTestHelper h, String what, boolean legacy, boolean routed) {
		h.assertTrue(legacy == routed, what + ": legacy filter " + legacy + ", routed " + routed);
	}

	/**
	 * Brined Court Track 10: Hydra's rising head is a presence, never a target. Nothing can pick it, the widest area
	 * effect in the matrix (Silent Severance) leaves it, and it is no living entity, so no column's selector can choose
	 * it. Marimo is moss blocks and has no entity at all.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_court_presence", timeoutTicks = 100)
	public static void courtPresenceIsNeverATarget(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer caster = DuctilisGameTests.player(h);
			caster.setGameMode(GameType.SURVIVAL);
			caster.moveTo(h.absoluteVec(CASTER), -90.0F, 0.0F);
			var head = com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead.rise(h.getLevel(),
					h.absoluteVec(CASTER.add(TARGET_OFFSET)), caster);
			try {
				Vec3 eye = caster.getEyePosition();
				Vec3 end = eye.add(caster.getLookAngle().scale(8.0D));
				var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(caster, eye, end,
						caster.getBoundingBox().expandTowards(end.subtract(eye)).inflate(2.0D),
						entity -> !entity.isSpectator() && entity.isPickable(), 64.0D);
				h.assertTrue(hit == null || hit.getEntity() != head, "a look or swing never picks the head");
				SilentArchonArmorAbilityHandler.activateSilentArchonSeverance(caster);
				h.assertTrue(head.isAlive(), "an area effect leaves the head");
				h.assertFalse(head.isAttackable(), "the head is never attackable");
				h.assertFalse((Object) head instanceof LivingEntity, "no living-target selector can choose the head");
				h.succeed();
			} finally {
				head.discard();
				caster.discard();
			}
		});
	}

	/**
	 * Brined Court Track 10, Tier 2: the two followers who fight are ordinary targets during their fight only. The Bloom
	 * Warden and the Kindled Champion take no harm through their opening, take it in their fight (from a strike and
	 * from the widest area effect in the matrix), and take none once the fight is over and they re-anchor or yield.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_court_followers", timeoutTicks = 100)
	public static void courtFollowersAreTargetsOnlyInTheirFight(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer caster = DuctilisGameTests.player(h);
			caster.setGameMode(GameType.SURVIVAL);
			caster.moveTo(h.absoluteVec(CASTER), -90.0F, 0.0F);
			var level = h.getLevel();
			var warden = com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity.wake(caster,
					com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.GROWTH, caster.blockPosition());
			var champion = com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity.callOut(level,
					net.minecraft.core.BlockPos.containing(h.absoluteVec(CASTER.add(TARGET_OFFSET))), 90.0F, "kindled_elder", caster);
			try {
				var strike = level.damageSources().playerAttack(caster);
				h.assertFalse(warden.hurt(strike, 2.0F), "the Warden is untouchable through its opening");
				h.assertFalse(champion.hurt(strike, 2.0F), "the Champion is untouchable through its opening");
				warden.forcePose(com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Phase.UPROOTED,
						com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Move.NONE);
				champion.forcePose(0, com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules.Move.NONE);
				h.assertTrue(ManipulationCombatHelper.canHarm(caster, warden) && ManipulationCombatHelper.canHarm(caster, champion),
						"both pass the strict rule");
				float wardenBefore = warden.getHealth(), championBefore = champion.getHealth();
				h.assertTrue(warden.hurt(strike, 2.0F) && warden.getHealth() < wardenBefore, "the Warden takes a strike in its fight");
				h.assertTrue(champion.hurt(strike, 2.0F) && champion.getHealth() < championBefore, "the Champion takes one in its fight");
				warden.invulnerableTime = 0;
				champion.invulnerableTime = 0;
				float wardenMid = warden.getHealth(), championMid = champion.getHealth();
				SilentArchonArmorAbilityHandler.activateSilentArchonSeverance(caster);
				h.assertTrue(warden.getHealth() <= wardenMid && champion.getHealth() <= championMid, "an area effect may reach them");
				warden.end(com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Outcome.GONE);
				champion.end(com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules.Outcome.GONE, null);
				warden.invulnerableTime = 0;
				champion.invulnerableTime = 0;
				h.assertFalse(warden.hurt(strike, 2.0F), "after its fight the Warden is no target");
				h.assertFalse(champion.hurt(strike, 2.0F), "after its fight the Champion is no target");
				h.succeed();
			} finally {
				warden.discard();
				champion.discard();
				caster.discard();
			}
		});
	}

	private static void matrix(GameTestHelper h, String row, String expected, Consumer<ServerPlayer> prepare, Strike strike) {
		matrix(h, row, expected, prepare, strike, Column.values());
	}

	/**
	 * Kill credit for an area hit: the hostile column reads "hostile" when the hit leaves the caster as its kill credit
	 * (blood on kill, Temper, skill points and purity follow it), "none" when nobody is credited.
	 */
	private static void credit(GameTestHelper h, String row, String expected, Consumer<ServerPlayer> prepare, Strike strike) {
		matrix(h, row + " kill credit", expected, prepare, (helper, caster, target) -> {
			strike.reaches(helper, caster, target);
			return target.getKillCredit() == caster;
		}, Column.HOSTILE);
	}

	private static void matrix(GameTestHelper h, String row, String expected, Consumer<ServerPlayer> prepare, Strike strike,
			Column... columns) {
		h.runAfterDelay(2, () -> {
			var server = h.getLevel().getServer();
			boolean pvp = server.isPvpAllowed();
			ServerPlayer caster = DuctilisGameTests.player(h);
			caster.setGameMode(GameType.SURVIVAL);
			caster.moveTo(h.absoluteVec(CASTER), -90.0F, 0.0F);
			caster.setYHeadRot(-90.0F);
			var blood = HemoCapabilityAccess.getBloodVolume(caster);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(5000.0D);
			blood.setActive(true);
			var board = h.getLevel().getScoreboard();
			PlayerTeam team = board.addPlayerTeam("matrix_" + caster.getId());
			board.addPlayerToTeam(caster.getScoreboardName(), team);
			List<String> reached = new ArrayList<>();
			try {
				prepare.accept(caster);
				for (Column column : columns) {
					server.setPvpAllowed(column != Column.PVP_OFF);
					LivingEntity target = spawn(h, column, caster, team);
					try {
						if (strike.reaches(h, caster, target)) reached.add(column.label);
					} finally {
						target.discard();
					}
				}
				String actual = reached.isEmpty() ? "none" : String.join(" ", reached);
				Hemomancy.LOGGER.info("COMBAT_MATRIX {} -> {}", row, actual);
				h.assertTrue(actual.equals(expected), row + " reached [" + actual + "], expected [" + expected + "]");
				h.succeed();
			} finally {
				server.setPvpAllowed(pvp);
				board.removePlayerTeam(team);
				StillArt.clearSessionState();
				CastingAnimationManager.logout(caster);
				caster.discard();
			}
		});
	}

	private static LivingEntity spawn(GameTestHelper h, Column column, ServerPlayer caster, PlayerTeam team) {
		Vec3 at = caster.position().add(TARGET_OFFSET);
		if (column == Column.PVP_ON || column == Column.PVP_OFF) {
			ServerPlayer other = DuctilisGameTests.player(h);
			other.setGameMode(GameType.SURVIVAL);
			other.moveTo(at, 90.0F, 0.0F);
			clearSpawnInvulnerability(other);
			return other;
		}
		Mob mob = switch (column) {
			case HOSTILE, ALLY -> EntityType.HUSK.create(h.getLevel());
			case PET -> EntityType.WOLF.create(h.getLevel());
			case PUPPET -> EntityInit.gorebound_hulk.get().create(h.getLevel());
			case BOSS -> EntityType.WARDEN.create(h.getLevel());
			default -> throw new IllegalStateException(column.name());
		};
		mob.setNoAi(true);
		mob.moveTo(at, 90.0F, 0.0F);
		h.getLevel().addFreshEntity(mob);
		if (column == Column.PET && mob instanceof net.minecraft.world.entity.animal.Wolf wolf) wolf.tame(caster);
		if (column == Column.ALLY) h.getLevel().getScoreboard().addPlayerToTeam(mob.getScoreboardName(), team);
		if (column == Column.PUPPET && mob instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon puppet) {
			puppet.hemomancy$setOwnerUUID(caster.getUUID());
		}
		mob.setHealth(mob.getMaxHealth());
		return mob;
	}

	/** A fresh player ignores all damage for its first 60 ticks; the matrix asks what the effect does afterwards. */
	private static void clearSpawnInvulnerability(ServerPlayer player) {
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(player, 0);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void learn(ServerPlayer caster, StillArt art) {
		var progress = HemoCapabilityAccess.getUnstainedProgress(caster);
		progress.setClarityUnlocked(true);
		progress.setClarity(100.0F);
		HemoCapabilityAccess.getKnownStillArts(caster).learnArt(art);
	}

	private static void wearSaprovitta(ServerPlayer caster) {
		HemoCapabilityAccess.getScarState(caster).setFungalScar(new ItemStack(ItemInit.saprovitta_vestigium.get()));
	}

	/** One trail pulse with the target inside its reach: a moving wearer on a pulse tick. */
	private static void saprovittaTrail(ServerPlayer caster, LivingEntity target) {
		target.moveTo(caster.position().add(1.0D, 0.0D, 0.0D));
		caster.setDeltaMovement(0.2D, 0.0D, 0.0D);
		caster.tickCount = SaprovittaVestigiumItem.TRAIL_INTERVAL_TICKS * 2;
		ScarsEntityEventHandler.playerTickScars(new PlayerTickEvent.Post(caster));
	}

	private static void wearSheolic(ServerPlayer caster) {
		caster.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.sheolic_blood_lust_helm.get()));
		caster.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.sheolic_blood_lust_chest.get()));
		caster.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.sheolic_blood_lust_legs.get()));
		caster.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.sheolic_blood_lust_boots.get()));
	}

	/** The wearer is struck by the target, which the retribution then answers. */
	private static void sheolicRetribution(ServerPlayer caster, LivingEntity target) {
		var source = target instanceof Player striker ? caster.damageSources().playerAttack(striker)
				: caster.damageSources().mobAttack(target);
		SheolicBastionBloodlustArmorAbilityHandler.onPlayerDamage(new LivingDamageEvent.Pre(caster, new DamageContainer(source, 1.0F)));
	}

	/** Calls a site's private static method as it stands, for a site the matrix cannot reach from outside. */
	private static void invoke(Class<?> owner, String name, Class<?>[] parameters, Object... arguments) {
		try {
			Method method = owner.getDeclaredMethod(name, parameters);
			method.setAccessible(true);
			method.invoke(null, arguments);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static boolean stillArt(GameTestHelper h, ServerPlayer caster, LivingEntity target, StillArt art) {
		StillArt.clearSessionState();
		return touched(target, () -> art.performAction(caster, h.getLevel(), ItemStack.EMPTY, caster.blockPosition()));
	}

	/** The strike lowered the target's health. */
	private static boolean damaged(LivingEntity target, Runnable strike) {
		float health = target.getHealth();
		strike.run();
		return target.getHealth() < health;
	}

	/** The strike damaged, moved, ignited or applied an effect to the target. */
	private static boolean touched(LivingEntity target, Runnable strike) {
		float health = target.getHealth();
		Set<Holder<MobEffect>> effects = new HashSet<>(target.getActiveEffectsMap().keySet());
		Vec3 motion = target.getDeltaMovement();
		int fire = target.getRemainingFireTicks();
		strike.run();
		return target.getHealth() < health || !effects.containsAll(target.getActiveEffectsMap().keySet())
				|| target.getDeltaMovement().distanceToSqr(motion) > 1.0E-8D || target.getRemainingFireTicks() > fire;
	}
}
