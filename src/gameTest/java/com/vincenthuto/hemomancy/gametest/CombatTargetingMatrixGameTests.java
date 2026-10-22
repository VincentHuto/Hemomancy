package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.Reach;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IrontoothMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
		matrix(h, "drudge blood_shot", "hostile ally puppet boss", caster -> {}, (helper, caster, target) -> {
			DrudgeEntity drudge = EntityInit.drudge.get().create(helper.getLevel());
			drudge.setOwnerUUID(caster.getUUID());
			drudge.setNoAi(true);
			drudge.moveTo(caster.position().add(-1.5D, 0.0D, 0.0D));
			helper.getLevel().addFreshEntity(drudge);
			try {
				return damaged(target, () -> ManipulationInit.blood_shot.get().getDrudgeAction().orElseThrow()
						.execute(drudge, helper.getLevel(), drudge.blockPosition(), 6));
			} finally {
				drudge.discard();
			}
		});
	}

	/** Silent Archon: Silent Severance's area burst. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_severance", timeoutTicks = 100)
	public static void armourSilentSeverance(GameTestHelper h) {
		matrix(h, "Silent Severance", "hostile pet ally puppet boss pvp-on pvp-off", caster -> {},
				(helper, caster, target) -> touched(target,
						() -> SilentArchonArmorAbilityHandler.activateSilentArchonSeverance(caster)));
	}

	/** Prismatic set: the flash when the wearer is struck, by a cow that is not a column. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_prismatic", timeoutTicks = 100)
	public static void armourPrismaticFlash(GameTestHelper h) {
		matrix(h, "Prismatic flash", "hostile ally puppet boss", caster -> {
			caster.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.prismatic_helm.get()));
			caster.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.prismatic_chestplate.get()));
			caster.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.prismatic_leggings.get()));
			caster.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.prismatic_boots.get()));
		}, (helper, caster, target) -> {
			var attacker = EntityType.COW.create(helper.getLevel());
			attacker.setNoAi(true);
			attacker.moveTo(caster.position().add(0.0D, 0.0D, 2.0D));
			helper.getLevel().addFreshEntity(attacker);
			caster.getPersistentData().remove("hemomancy:prismatic_flash_until");
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
		matrix(h, "Still Art autoimmune_edge", "hostile pet ally puppet boss pvp-on pvp-off", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Still Art: Lethean Mute's Monster filter. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_lethean", timeoutTicks = 100)
	public static void stillArtLetheanMute(GameTestHelper h) {
		StillArt art = StillArtInit.lethean_mute.get();
		matrix(h, "Still Art lethean_mute", "hostile ally puppet boss", caster -> learn(caster, art),
				(helper, caster, target) -> stillArt(helper, caster, target, art));
	}

	/** Morphling: Irontooth's primal Deep Tremor Sense damages the Monsters near the caster. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_irontooth", timeoutTicks = 100)
	public static void morphlingIrontoothPrimal(GameTestHelper h) {
		matrix(h, "morphling Irontooth primal damage", "hostile boss", caster -> {}, (helper, caster, target) -> {
			ItemStack stack = new ItemStack(ItemInit.morphling_irontooth.get());
			MorphlingItem.setPrimalized(stack);
			MorphlingItem.setLastAbilityTick(stack, MorphlingItem.PRIMAL_ABILITY_PREFIX + "DeepTremorSense",
					helper.getLevel().getGameTime() - 100_000L);
			return damaged(target, () -> ((IrontoothMorphlingItem) stack.getItem())
					.tryUse(caster, InteractionHand.MAIN_HAND, stack, helper.getLevel()));
		});
	}

	/** Morphling: the look target Bootlace and Emberfang act on. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_look", timeoutTicks = 100)
	public static void morphlingLookTarget(GameTestHelper h) {
		matrix(h, "morphling findLookTarget", "hostile pet ally puppet boss pvp-on pvp-off", caster -> {},
				(helper, caster, target) -> MorphlingItem.findLookTarget(caster, 6.0D) == target);
	}

	/** Scar: Glacier (Congeatio, tier 2) slows the Monsters around its wearer. */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_glacier", timeoutTicks = 100)
	public static void scarGlacierSlow(GameTestHelper h) {
		matrix(h, "scar Glacier slow", "hostile ally puppet boss", caster -> {}, (helper, caster, target) -> {
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
	 * Each {@link Reach} preset gives the verdict of the pre-step-9 filter it replaced, for every kind of body and with
	 * PvP on and off. STRICT is compared with canHarm's body as it stood before step 9.
	 */
	@GameTest(templateNamespace = NAMESPACE, template = ROOM, batch = "combat_matrix_reach", timeoutTicks = 100)
	public static void reachPresetsReproduceTheirLegacyFilters(GameTestHelper h) {
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
						boolean monster = body instanceof Monster;
						same(h, "MONSTER_FILTER " + what, monster && body.isAlive(),
								monster && ManipulationCombatHelper.canHarm(caster, body, Reach.MONSTER_FILTER));
						same(h, "MONSTER_FILTER_ANY_STATE " + what, monster && !body.isSpectator(),
								monster && ManipulationCombatHelper.canHarm(caster, body, Reach.MONSTER_FILTER_ANY_STATE));
						same(h, "UNGATED " + what, body != caster && body.isAlive(),
								ManipulationCombatHelper.canHarm(caster, body, Reach.UNGATED));
						same(h, "UNGATED_SURVIVAL " + what, body != caster && body.isAlive() && !body.isSpectator()
										&& !(body instanceof Player p && (p.isCreative() || p.isSpectator())),
								ManipulationCombatHelper.canHarm(caster, body, Reach.UNGATED_SURVIVAL));
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

	private static void matrix(GameTestHelper h, String row, String expected, Consumer<ServerPlayer> prepare, Strike strike) {
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
				for (Column column : Column.values()) {
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
