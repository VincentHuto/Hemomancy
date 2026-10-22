package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.common.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AncestralCommunionDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.*;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.*;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedRitePreflight;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import com.vincenthuto.hutoslib.client.particle.data.DarkColorParticleData;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;

/**
 * What each Harbinger Cardinal Rite does when it resolves, keyed by recipe path.
 * <p>
 * Every effect may name a refusal: a condition the rite needs that the world or
 * caster does not currently meet. Refusals are checked when the rite is started,
 * again before its finale consumes any offering, and once more at completion, so
 * a rite that cannot resolve waits (or never starts) instead of eating its
 * offerings and "falling silent".
 */
public final class CardinalRiteCompletionEffects {
	/** Radius (in blocks) for Hungering Earth terrain corruption. */
	private static final int HUNGERING_EARTH_RADIUS = 16;
	/** Chunk radius for Sanguine Dominion blood domain. */
	private static final int DOMINION_CHUNK_RADIUS = 3;
	/** Chunk radius for Qliphoth Bloom effect zone. */
	private static final int QLIPHOTH_BLOOM_CHUNK_RADIUS = 3;
	/** Chunk radius for the Sanguine Fervor spawn-boost zone. */
	private static final int SANGUINE_FERVOR_CHUNK_RADIUS = 3;
	/** Duration in ticks for the Sanguine Fervor spawn-boost effect (5 minutes). */
	private static final long SANGUINE_FERVOR_DURATION_TICKS = 6000L;
	/** Blood cost per member for Scarlet Summons (from bloodline pool). */
	private static final float SUMMONS_COST_PER_MEMBER = 200f;

	/** When a refusal is evaluated. */
	public enum Stage {
		/** The rite is being started; transient conditions (members online) are not yet required. */
		ACTIVATION,
		/** The rite is about to consume its offerings and resolve. */
		FINALE
	}

	/**
	 * @param rite the active rite, or {@code null} while the rite is still being started
	 */
	public record Context(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe, BlockPos center) {
		public String path() {
			return recipe.getId().getPath();
		}
	}

	private record Effect(BiFunction<Context, Stage, Component> refusal, Predicate<Context> apply) {
	}

	private static final Map<String, Effect> EFFECTS = new HashMap<>();

	static {
		effect("cardinal_rite/sanguine_attunement", ctx -> completeSanguineAttunement(ctx.caster()));
		effect("cardinal_rite/crimson_beacon", ctx -> completeCrimsonBeacon(ctx.level(), ctx.caster(), ctx.center()));
		effect("cardinal_rite/vascular_mending", ctx -> completeVascularMending(ctx.caster()));
		effect("cardinal_rite/hematic_fortification", ctx -> completeHematicFortification(ctx.caster()));
		effect("cardinal_rite/hungering_earth", ctx -> completeHungeringEarth(ctx.level(), ctx.caster(), ctx.center()));
		register("cardinal_rite/rooted_vein",
				(ctx, stage) -> rootedVeinRefusal(ctx.level(), ctx.center()),
				ctx -> {
					completeRootedVein(ctx.level(), ctx.center());
					return true;
				});
		register("cardinal_rite/scarlet_summons", CardinalRiteCompletionEffects::summonsRefusal,
				ctx -> {
					completeScarletSummons(ctx.level(), ctx.caster(), ctx.center());
					return true;
				});
		effect("cardinal_rite/sanguine_dominion",
				ctx -> completeSanguineDominion(ctx.level(), ctx.caster(), ctx.center()));
		register("cardinal_rite/eternal_covenant",
				(ctx, stage) -> ctx.caster().getPersistentData().getBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG)
						? Component.literal("The covenant is already sealed. Its boon has already been granted.")
						: null,
				ctx -> {
					completeEternalCovenant(ctx.caster());
					return true;
				});
		effect("cardinal_rite/ancestral_communion", ctx -> completeAncestralCommunion(ctx.level(), ctx.caster()));
		register("cardinal_rite/hematic_unbinding", (ctx, stage) -> leaderRefusal(ctx, "Hematic Unbinding"),
				ctx -> {
					completeHematicUnbinding(ctx.level(), ctx.caster());
					return true;
				});
		effect("cardinal_rite/pallid_shadow",
				ctx -> completePallidShadow(ctx.level(), ctx.caster(), ctx.center(), ctx.rite().getRiteSize()));
		register("cardinal_rite/bloom_of_qliphoth",
				(ctx, stage) -> bloomRefusal(ctx.level(), ctx.center()),
				ctx -> {
					completeBloomOfQliphoth(ctx.level(), ctx.caster(), ctx.center());
					return true;
				});
		effect("cardinal_rite/sanguine_fervor",
				ctx -> completeSanguineFervor(ctx.level(), ctx.caster(), ctx.center()));
		effect("cardinal_rite/covenant_vigil", ctx -> completeCovenantVigil(ctx.level(), ctx.caster(), ctx.rite()));
		// Degree advancement only; territorial consecration belongs to Founding Fane.
		effect("cardinal_rite/illuminatus_rite", ctx -> HarbingerAdvancementGranter.grantIfNotDone(ctx.caster(),
				HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED));
		register("cardinal_rite/sanguine_eclipse",
				(ctx, stage) -> BloodMoonSavedData.get(ctx.level().getServer().overworld()).isActive()
						? Component.literal("A Blood Moon already reigns over this world.")
						: null,
				ctx -> {
					completeSanguineEclipse(ctx.level(), ctx.caster());
					return true;
				});
		register("cardinal_rite/founding_fane",
				(ctx, stage) -> faneRefusal(ctx.level(), ctx.caster(), ctx.center().above(3)),
				ctx -> completeFoundingFane(ctx.level(), ctx.caster(), ctx.center()));
		register("cardinal_rite/bloodline_recall",
				(ctx, stage) -> {
					Bloodline line = BloodlineSavedData.get(ctx.level().getServer().overworld())
							.getBloodlineForPlayer(ctx.caster().getUUID());
					return line == null || !line.isValid()
							? Component.literal("The blood remembers nothing. You have no bloodline to recall.")
							: null;
				},
				ctx -> true);
		effect("cardinal_rite/exsanguination", ctx -> ctx.caster().displayClientMessage(
				Component.literal("The lifeblood crystallizes... Sanguine Quintessence is born.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false));
		effect("cardinal_rite/initiate_rite", ctx -> {
			giveOrDropAtRite(ctx.level(), ctx.caster(), ctx.center(), new ItemStack(ItemInit.sanguine_blob.get()));
			ctx.caster().displayClientMessage(
					Component.translatable("hemomancy.rite.initiate_rite.blob_granted")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		});
		effect("cardinal_rite/chamber_of_will", ctx -> ChamberVisitService.beginRiteVisit(ctx.caster()));
	}

	private CardinalRiteCompletionEffects() {
	}

	private static void effect(String path, Consumer<Context> apply) {
		register(path, (ctx, stage) -> null, ctx -> {
			apply.accept(ctx);
			return true;
		});
	}

	private static void register(String path, BiFunction<Context, Stage, Component> refusal,
			Predicate<Context> apply) {
		EFFECTS.put(path, new Effect(refusal, apply));
	}

	/**
	 * Why this rite cannot resolve right now, or {@code null}. Once the rite exists
	 * this includes the shared Harbinger gates (degree, helpers, Apotheos, path).
	 */
	public static Component refusal(Context ctx, Stage stage) {
		if (ctx.rite() != null && !ctx.recipe().isUnstained()) {
			Component common = commonRefusal(ctx);
			if (common != null) return common;
		}
		Effect effect = EFFECTS.get(ctx.path());
		return effect == null ? null : effect.refusal().apply(ctx, stage);
	}

	/** Applies the rite's effect; false aborts completion before the shared rewards. */
	public static boolean apply(Context ctx) {
		Effect effect = EFFECTS.get(ctx.path());
		return effect == null || effect.apply().test(ctx);
	}

	/** Shapes the rite's result item before it drops; bloodline ledgers are signed here. */
	public static ItemStack shapeResult(Context ctx, ItemStack result) {
		if (!(result.getItem() instanceof UnsignedLedgerItem)) return result;
		if ("cardinal_rite/bloodline_founding".equals(ctx.path())) {
			presignBloodlineLedger(ctx.level(), ctx.caster(), result);
			HarbingerAdvancementGranter.grantIfNotDone(ctx.caster(), HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND);
		} else if ("cardinal_rite/bloodline_recall".equals(ctx.path())) {
			recallBloodlineLedger(ctx.level(), ctx.caster(), result);
		}
		return result;
	}

	/** Degree, helper, Apotheos and path gates shared by every Harbinger rite. */
	static Component commonRefusal(Context ctx) {
		ServerPlayer caster = ctx.caster();
		CardinalRiteRecipe recipe = ctx.recipe();
		if (!RecipeDegreeGates.playerMeets(caster, recipe)) {
			return Component.literal("It requires " + RecipeDegreeGates.requirementLabel(recipe) + ".");
		}
		if (!CardinalRiteAllyService.hasRequiredHelpers(ctx.level(), ctx.rite())) {
			return Component.literal("Required bloodline helpers must still hold their stations.");
		}
		boolean apotheos = isApotheos(recipe);
		if (apotheos && !hasQliphothCommunion(caster)) {
			return Component.literal(
					"The Eighth Degree remains sealed. Consume all nine Qliphoth husks from a single bloom.");
		}
		if (apotheos && (HemoCapabilityAccess.getInitiatoryDegree(caster).getArchonPath() != EnumArchonPath.APOTHEOS_PENDING)) {
			return Component.literal("Apotheosis has not been chosen. "
					+ "Witness the first projection and answer the truth it leaves behind.");
		}
		boolean pathPermitsRite = HarbingerPathPermissions.canPerformRite(HemoCapabilityAccess.getInitiatoryDegree(caster).getTotalPomesConsumed(),
						HemoCapabilityAccess.getInitiatoryDegree(caster).getArchonPath(), apotheos);
		if (!pathPermitsRite) {
			return Component.literal("The void has claimed your will — only one path remains, and one way back.");
		}
		return null;
	}

	static boolean isApotheos(CardinalRiteRecipe recipe) {
		return recipe != null && "cardinal_rite/apotheos_rite".equals(recipe.getId().getPath());
	}

	static boolean hasQliphothCommunion(ServerPlayer player) {
		return HemoCapabilityAccess.getInitiatoryDegree(player).isQliphothCommunionDone();
	}

	private static Component leaderRefusal(Context ctx, String riteName) {
		Bloodline bloodline = BloodlineSavedData.get(ctx.level().getServer().overworld())
				.getBloodlineForPlayer(ctx.caster().getUUID());
		if (bloodline == null || !bloodline.isValid()) {
			return Component.literal("The " + riteName + " needs a bloodline, and you have none.");
		}
		if (!bloodline.getLeaderUUID().equals(ctx.caster().getUUID())) {
			return Component.literal("Only the bloodline leader may perform the " + riteName + ".");
		}
		return null;
	}

	private static Component summonsRefusal(Context ctx, Stage stage) {
		Component leader = leaderRefusal(ctx, "Scarlet Summons");
		if (leader != null || stage == Stage.ACTIVATION) return leader;
		Bloodline bloodline = BloodlineSavedData.get(ctx.level().getServer().overworld())
				.getBloodlineForPlayer(ctx.caster().getUUID());
		int online = 0;
		for (UUID member : bloodline.getPlayerUUIDS()) {
			if (!member.equals(ctx.caster().getUUID())
					&& ctx.level().getServer().getPlayerList().getPlayer(member) != null) online++;
		}
		if (online == 0) return Component.literal("No bloodline members are online to answer the summons.");
		if (bloodline.getBloodVolume() < online * SUMMONS_COST_PER_MEMBER * 0.5F) {
			return Component.literal("The bloodline pool lacks the blood to carry " + online + " kin here.");
		}
		return null;
	}

	static Component rootedVeinRefusal(ServerLevel level, BlockPos center) {
		return level.getBlockState(center.above()).canBeReplaced() ? null
				: Component.literal("Keep the block above the Cardinal Focus clear for the Earthen Vein.");
	}

	static void giveOrDropAtRite(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack stack) {
		if (player.addItem(stack)) return;
		ItemEntity drop = new ItemEntity(level,
				center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, stack);
		if (!level.addFreshEntity(drop)) player.drop(stack, false);
	}

	/** Validates the Bloom's root, filler column, and exclusion radius before payment. */
	static Component bloomRefusal(ServerLevel sLevel, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		// Check if a bloom already exists within the radius only one bloom per 3-chunk area
		QliphothBloomSavedData.BloomEntry overlapping = data.getOverlappingBloom(
				center.above(2), dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS);
		if (overlapping != null) {
			return Component.literal("A Qliphoth Bloom already exists within " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ " chunks of here. Only one bloom may exist per " + QLIPHOTH_BLOOM_CHUNK_RADIUS
							+ "-chunk radius.");
		}

		// The shared multiblock check covers fillers, not the root itself.
		Block bloomBlock = BlockInit.qliphoth_bloom.get();
		IMultiBlock multiBlock =
				(IMultiBlock) bloomBlock;
		BlockPos bloomPos = center.above(2);
		if (!sLevel.getBlockState(bloomPos).canBeReplaced()
				|| !multiBlock.canPlaceMultiBlock(sLevel, bloomPos)) {
			return Component.literal("The Qliphoth needs more room to bloom here.");
		}
		return null;
	}

	static Component faneRefusal(ServerLevel sLevel, ServerPlayer caster, BlockPos heartPos) {
		BlockState heartState = sLevel.getBlockState(heartPos);
		if (!heartState.is(BlockInit.consecrated_bloodwell.get())
				&& !heartState.isAir()
				&& !heartState.canBeReplaced()) {
			return Component.literal("The rite's heart was obstructed before the Consecrated Bloodwell could be manifested.");
		}
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(caster).getBloodLine();
		if (bloodline == null || !bloodline.isValid()) {
			return Component.literal("The Founding Fane requires an established bloodline.");
		}
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			return Component.literal("Only the bloodline Progenitor may consecrate a Founding Fane.");
		}
		Bloodline registered = BloodlineSavedData.get(sLevel.getServer().overworld())
				.getBloodline(bloodline.getBloodlineUUID());
		if (registered == null || !registered.getLeaderUUID().equals(caster.getUUID())) {
			return Component.literal("The Founding Fane requires a founded bloodline in this world.");
		}
		FoundingFaneSavedData data = FoundingFaneSavedData.get(sLevel);
		UUID heartOwner = data.findOwnerForHeart(heartPos);
		if (heartState.is(BlockInit.consecrated_bloodwell.get())
				&& !caster.getUUID().equals(heartOwner)) {
			return Component.literal("This Consecrated Bloodwell belongs to another Fane.");
		}
		for (var entry : data.getAllFootprints().entrySet()) {
			if (!entry.getKey().equals(caster.getUUID()) && entry.getValue().contains(heartPos)) {
				return Component.literal("Another Fane already claims this ground.");
			}
		}
		return null;
	}

	private static void completeRootedVein(ServerLevel level, BlockPos center) {
		level.setBlockAndUpdate(center.above(), BlockInit.earthen_vein.get().defaultBlockState());
		level.setBlockAndUpdate(center, BlockInit.venous_stone.get().defaultBlockState());
	}

	/**
	 * Rite of Sanguine Attunement (Degree 2, Minor):
	 * Resets all blood tendency alignment axes to zero.
	 */
	private static void completeSanguineAttunement(ServerPlayer caster) {
		var tendency = HemoCapabilityAccess.getBloodTendency(caster);
		for (EnumBloodTendency bt : EnumBloodTendency.values()) {
			tendency.setTendencyAlignment(bt, 0f);
		}
		BloodTendencyEvents.syncTendency(caster, tendency);
		caster.displayClientMessage(
				Component.literal("Your blood tendencies have been purged. You are a blank slate once more.")
						.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Crimson Beacon (Degree 3, Lesser):
	 * Registers a death waypoint at the rite center. On fatal damage, the
	 * player's body will be teleported here before death (one-time use).
	 */
	private static void completeCrimsonBeacon(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		CrimsonBeaconSavedData data = CrimsonBeaconSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();
		CrimsonBeaconSavedData.BeaconEntry previous = data.getBeacon(caster.getUUID());
		data.setBeacon(caster.getUUID(), center, dimension);

		caster.displayClientMessage(
				Component.literal(previous == null
						? "A Crimson Beacon is anchored here. Should you fall, your body will return."
						: "Your Crimson Beacon tears free of its former anchor and settles here.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Vascular Mending (Degree 3, Lesser):
	 * Fully restores all vein sections to maximum health (100).
	 */
	private static void completeVascularMending(ServerPlayer caster) {
		var vascular = HemoCapabilityAccess.getVascularSystem(caster);
		for (EnumVeinSections section : EnumVeinSections.values()) {
			java.util.Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
			sys.put(section, 100f);
			vascular.setVascularSystem(sys);
		}
		VascularSystemEvents.syncVascular(caster, vascular);
		caster.displayClientMessage(
				Component.literal("Purified blood surges through your veins. All vascular damage has been mended.")
						.withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Hungering Earth (Degree 3, Lesser):
	 * Corrupts natural terrain in a radius around the rite center, converting:
	 * <ul>
	 *   <li>Stone  Venous Stone</li>
	 *   <li>Cobblestone  Venous Stone</li>
	 *   <li>Deepslate  Infested Venous Stone</li>
	 *   <li>Dirt/Grass  Befouling Ash Trail (block below becomes venous stone)</li>
	 *   <li>Sand/Gravel  Polished Venous Stone</li>
	 * </ul>
	 */
	private static void completeHungeringEarth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		int radius = HUNGERING_EARTH_RADIUS;
		int converted = 0;

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				// Circular check
				if (x * x + z * z > radius * radius) continue;

				for (int y = -radius / 2; y <= radius / 2; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = sLevel.getBlockState(pos);
					Block block = state.getBlock();

					BlockState replacement = getHungeringEarthReplacement(block);
					if (replacement != null) {
						sLevel.setBlock(pos, replacement, 2);
						converted++;

						// Spawn occasional particles for visual feedback
						if (converted % 10 == 0) {
							sLevel.levelEvent(2001, pos, Block.getId(state));
						}
					}
				}
			}
		}

		caster.displayClientMessage(
				Component.literal("The earth hungers... " + converted + " blocks have been corrupted.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Returns the blood-corrupted replacement for a natural block, or null if
	 * the block should not be converted.
	 */
	private static BlockState getHungeringEarthReplacement(Block block) {
		if (block == Blocks.STONE || block == Blocks.COBBLESTONE || block == Blocks.ANDESITE
				|| block == Blocks.DIORITE || block == Blocks.GRANITE) {
			return BlockInit.venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
			return BlockInit.infested_venous_stone.get().defaultBlockState();
		}
		if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL
				|| block == Blocks.MYCELIUM || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
			return BlockInit.befouling_ash_trail.get().defaultBlockState();
		}
		if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.GRAVEL) {
			return BlockInit.polished_venous_stone.get().defaultBlockState();
		}
		return null;
	}

	/**
	 * Rite of the Scarlet Summons (Degree 5, Greater):
	 * Teleports all online bloodline members to the rite center. Draws blood
	 * from the shared bloodline pool proportional to the number of members.
	 */
	private static void completeScarletSummons(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.displayClientMessage(
					Component.literal("You have no bloodline to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the bloodline leader may perform the summons
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Scarlet Summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		List<ServerPlayer> onlineMembers = new ArrayList<>();
		for (UUID memberUUID : bloodline.getPlayerUUIDS()) {
			if (memberUUID.equals(caster.getUUID())) continue; // Skip the caster
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				onlineMembers.add(member);
			}
		}

		if (onlineMembers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No bloodline members are online to summon.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Draw blood from the shared pool
		float totalCost = onlineMembers.size() * SUMMONS_COST_PER_MEMBER;
		float drawn = bloodlineData.drawBlood(bloodline.getBloodlineUUID(), totalCost);
		if (drawn < totalCost * 0.5f) {
			caster.displayClientMessage(
					Component.literal("The bloodline pool lacks sufficient blood for the summons.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Teleport all online members
		int teleported = 0;
		for (ServerPlayer member : onlineMembers) {
			// Handle cross-dimension teleport
			if (!member.level().equals(sLevel)) {
				member.teleportTo(sLevel, center.getX() + 0.5, center.getY() + 1.5,
						center.getZ() + 0.5, member.getYRot(), member.getXRot());
			} else {
				member.teleportTo(center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5);
			}
			member.displayClientMessage(
					Component.literal("The blood calls! You have been summoned by " + caster.getName().getString() + ".")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
			sLevel.playSound(null, member.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			teleported++;
		}

		caster.displayClientMessage(
				Component.literal("The Scarlet Summons draws " + teleported + " blood-kin to your side.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of Sanguine Dominion (Degree 6, Greater):
	 * Establishes a persistent Blood Domain centered on the rite location.
	 * Within the domain: enemies take slow bleed damage, and the caster's
	 * manipulations cost less blood.
	 */
	private static void completeSanguineDominion(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineDominionSavedData data = SanguineDominionSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();

		SanguineDominionSavedData.DominionEntry entry = new SanguineDominionSavedData.DominionEntry(
				caster.getUUID(), center, dimension, DOMINION_CHUNK_RADIUS, sLevel.getGameTime());
		SanguineDominionSavedData.DominionEntry previous = data.replaceDominionForOwner(entry);

		int blockRadius = DOMINION_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal((previous == null
						? "A Blood Domain has been established! "
						: "Your Blood Domain abandons its former bounds and reforms here. ")
						+ blockRadius + " blocks in every direction now bow to your crimson will.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_SANGUINE_DOMAIN);
	}

	/**
	 * Rite of the Eternal Covenant (Degree 6, Greater):
	 * Permanently increases the caster's maximum blood volume. Can only be
	 * performed once per player.
	 */
	private static void completeEternalCovenant(ServerPlayer caster) {
		CompoundTag persistentData = caster.getPersistentData();
		if (persistentData.getBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG)) {
			caster.displayClientMessage(
					Component.literal("The covenant is already sealed. Its boon has already been granted.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		persistentData.putBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG, true);
		MaxBloodLedger.apply(caster, HemoCapabilityAccess.getBloodVolume(caster));

		caster.displayClientMessage(
				Component.literal("The Eternal Covenant is sealed! Your maximum blood volume has been permanently increased by "
						+ (int) MaxBloodLedger.ETERNAL_COVENANT_BONUS + ".")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED);
	}

	/**
	 * Rite of Ancestral Communion (Degree 7, Grand):
	 * Opens a dialogue with the fungal consciousness, granting unique lore.
	 * Each invocation uses a different dialogue variant from a pool.
	 */
	private static void completeAncestralCommunion(ServerLevel sLevel, ServerPlayer caster) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(caster);
		int variant = Math.floorMod(degree.getAncestralCommunions(), AncestralCommunionDialogueTrees.VARIANT_COUNT);
		degree.setAncestralCommunions(degree.getAncestralCommunions() + 1);
		InitiatoryDegreeEvents.syncDegree(caster, degree);
		DialogueTree tree = AncestralCommunionDialogueTrees.forVariant(variant);

		PacketHandler.sendToPlayer(caster, new OpenDialoguePacket(tree));

		caster.displayClientMessage(
				Component.literal("The ancient blood stirs... a voice rises from the depths.")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
				false);
		HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN);
	}

	/**
	 * Rite of Hematic Unbinding (Cross-tier, Lesser):
	 * Destroys the caster's bloodline, freeing all members. Any shared blood
	 * in the pool is returned proportionally to the remaining members.
	 */
	private static void completeHematicUnbinding(ServerLevel sLevel, ServerPlayer caster) {
		final String pendingLineKey = "hemomancy:pending_unbinding_bloodline";
		final String pendingUntilKey = "hemomancy:pending_unbinding_until";
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(overworld);
		Bloodline bloodline = bloodlineData.getBloodlineForPlayer(caster.getUUID());

		if (bloodline == null || !bloodline.isValid()) {
			caster.getPersistentData().remove(pendingLineKey);
			caster.getPersistentData().remove(pendingUntilKey);
			caster.displayClientMessage(
					Component.literal("You have no bloodline to unbind.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Only the leader can dissolve the bloodline
		if (!bloodline.getLeaderUUID().equals(caster.getUUID())) {
			caster.displayClientMessage(
					Component.literal("Only the bloodline leader may perform the Hematic Unbinding.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		CompoundTag playerData = caster.getPersistentData();
		UUID warnedLine = playerData.hasUUID(pendingLineKey)
				? playerData.getUUID(pendingLineKey) : null;
		long warningUntil = playerData.getLong(pendingUntilKey);
		if (HematicUnbindingRules.decision(bloodline.getBloodlineUUID(), warnedLine,
				warningUntil, sLevel.getGameTime()) == HematicUnbindingRules.Decision.WARN) {
			playerData.putUUID(pendingLineKey, bloodline.getBloodlineUUID());
			playerData.putLong(pendingUntilKey,
					sLevel.getGameTime() + HematicUnbindingRules.CONFIRMATION_TICKS);
			caster.displayClientMessage(
					Component.literal("The covenant loosens but holds. Performing Hematic Unbinding again "
									+ "within ten minutes will permanently dissolve " + bloodline.getName()
									+ ", remove its fanes, and free every member.")
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					false);
			return;
		}
		playerData.remove(pendingLineKey);
		playerData.remove(pendingUntilKey);

		String bloodlineName = bloodline.getName();
		float poolBlood = bloodline.getBloodVolume();
		int memberCount = bloodline.getPlayerUUIDS().size();
		float bloodPerMember = memberCount > 0 ? poolBlood / memberCount : 0;

		// Return blood to all online members and clear their bloodline reference
		for (UUID memberUUID : new ArrayList<>(bloodline.getPlayerUUIDS())) {
			ServerPlayer member = sLevel.getServer().getPlayerList().getPlayer(memberUUID);
			if (member != null) {
				// Return their share of the pool blood
				if (bloodPerMember > 0) {
					var volume = HemoCapabilityAccess.getBloodVolume(member);
					volume.fill(bloodPerMember);
					volume.setBloodLine(Bloodline.NOBLOODLINE);
					BloodVolumeEvents.syncVolume(member, volume);
				} else {
					var volume = HemoCapabilityAccess.getBloodVolume(member);
					volume.setBloodLine(Bloodline.NOBLOODLINE);
					BloodVolumeEvents.syncVolume(member, volume);
				}

				member.displayClientMessage(
						Component.literal("The bloodline " + bloodlineName + " has been dissolved. You are unbound.")
								.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
						false);
			}
		}

		// Remove the bloodline and any fanes owned by its members from world data.
		BloodlineDisbandHelper.removeOwnedFanes(sLevel.getServer(), bloodline);
		bloodlineData.disbandBloodline(bloodline.getBloodlineUUID());

		caster.displayClientMessage(
				Component.literal("The " + bloodlineName + " is dissolved. What was bound by blood is unbound.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Rite of the Pallid Shadow (Cross-tier, Grand):
	 * Targets the nearest non-caster player within the rite circle and strips
	 * their Unstained purification progress. A direct hematic assault against
	 * followers of Our Lady of Still Waters.
	 */
	private static void completePallidShadow(ServerLevel sLevel, ServerPlayer caster,
			BlockPos center, int riteSize) {
		int halfSize = (Math.max(1, riteSize) - 1) / 2;
		AABB bounds = new AABB(center).inflate(halfSize + 1);

		List<ServerPlayer> nearbyPlayers = sLevel.getEntitiesOfClass(ServerPlayer.class, bounds,
				target -> target.isAlive() && !target.getUUID().equals(caster.getUUID())
						&& PallidShadowRules.canTarget(
								sLevel.getServer().isPvpAllowed(),
								target.isCreative(), target.isSpectator(),
								caster.isAlliedTo(target),
								HemoCapabilityAccess.getUnstainedProgress(target).hasBegunPurification()));

		if (nearbyPlayers.isEmpty()) {
			caster.displayClientMessage(
					Component.literal("No target stands within the circle. The shadow dissipates.")
							.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Target the closest player
		ServerPlayer target = nearbyPlayers.get(0);
		double closestDist = target.distanceToSqr(center.getX(), center.getY(), center.getZ());
		for (ServerPlayer p : nearbyPlayers) {
			double dist = p.distanceToSqr(center.getX(), center.getY(), center.getZ());
			if (dist < closestDist) {
				target = p;
				closestDist = dist;
			}
		}

		final ServerPlayer victim = target;
		var unstained = HemoCapabilityAccess.getUnstainedProgress(victim);
		boolean hadProgress = PathMutualExclusionHelper.resetUnstainedProgress(victim, unstained);

		if (hadProgress) {
			victim.displayClientMessage(
					Component.literal("A shadow of crimson corruption washes over you... Your purification has been destroyed!")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
			caster.displayClientMessage(
					Component.literal("The Pallid Shadow consumes " + victim.getName().getString()
							+ "'s purity. Their purification is undone.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					false);
		} else {
			caster.displayClientMessage(
					Component.literal(victim.getName().getString()
							+ " had no purification to destroy. The shadow finds nothing.")
							.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					false);
		}

		// Visual/sound feedback
		sLevel.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.5f);
	}

	/** Places the Bloom and registers its nine-pome lifecycle after preflight succeeds. */
	private static void completeBloomOfQliphoth(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		QliphothBloomSavedData data = QliphothBloomSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();
		Block bloomBlock = BlockInit.qliphoth_bloom.get();
		IMultiBlock multiBlock = (IMultiBlock) bloomBlock;

		// Place the multi-block
		sLevel.setBlockAndUpdate(center.above(2), bloomBlock.defaultBlockState());
		net.minecraft.world.level.block.entity.BlockEntity be = sLevel.getBlockEntity(center.above(2));
		if (be instanceof com.vincenthuto.hemomancy.common.tile.harbinger.functional.QliphothBloomBlockEntity bloomBE) {
			bloomBE.setOwnerUUID(caster.getUUID());
			bloomBE.setChunkRadius(QLIPHOTH_BLOOM_CHUNK_RADIUS);
		}
		// Place filler blocks above
		multiBlock.placeFillers(sLevel, center.above(2), bloomBlock.defaultBlockState());

		// Register in SavedData
		QliphothBloomSavedData.BloomEntry entry = new QliphothBloomSavedData.BloomEntry(
				caster.getUUID(), center.above(2),dimension, QLIPHOTH_BLOOM_CHUNK_RADIUS, sLevel.getGameTime());
		data.addBloom(entry);

		// Sync to all nearby clients so the tree renders immediately
		HarbingerCardinalRiteEvents.syncQliphothBlooms(sLevel.getServer());

		int blockRadius = QLIPHOTH_BLOOM_CHUNK_RADIUS * 16;
		caster.displayClientMessage(
				Component.literal("The Qliphoth blooms! A dark tree takes root, empowering blood within "
						+ blockRadius + " blocks.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);

		// Fire the post-bloom Fungal Whisper so the Entity acknowledges the fruiting
		if (FungalWhisperDialogueTrees.shouldOfferMemoWhisper(caster, MemoDefinitions.QLIPHOTH_COMMUNION)) {
			PacketHandler.sendToPlayer(caster, new OpenDialoguePacket(FungalWhisperDialogueTrees.postBloom()));
		}
	}

	private static void completeHematicFortification(ServerPlayer caster) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(caster);
		degree.setHematicFortification(true);
		com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.onFortification(caster);
		InitiatoryDegreeEvents.syncDegree(caster, degree);
		caster.displayClientMessage(
				Component.literal("Your vascular lattice hardens. Future strain is reduced by fifteen percent.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	/**
	 * Rite of the Covenant Vigil (Grand): rewards the caster and every assigned
	 * ally who is still present at completion. The common effects make the
	 * covenant mechanically shared instead of treating the helper as a key used
	 * only to unlock the ceremony.
	 */
	private static void completeCovenantVigil(ServerLevel sLevel, ServerPlayer caster, ActiveCardinalRite rite) {
		grantCovenantVigilReward(caster);
		HarbingerAdvancementGranter.grantIfNotDone(caster,
				HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED);
		HarbingerChapterProgression.tryCompleteLivingCovenant(caster);
		for (UUID allyId : rite.getAllyRoles().keySet()) {
			if (!CardinalRiteAllyService.isAvailable(sLevel, rite, allyId)) continue;
			Entity entity = sLevel.getEntity(allyId);
			if (entity instanceof LivingEntity ally && ally.isAlive()) {
				grantCovenantVigilReward(ally);
			}
		}
		caster.displayClientMessage(
				Component.literal("The vigil closes around every survivor. The covenant guards and restores you.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	private static void grantCovenantVigilReward(LivingEntity participant) {
		participant.addEffect(new MobEffectInstance(
				MobEffects.DAMAGE_RESISTANCE,
				CovenantVigilRules.REWARD_DURATION_TICKS,
				CovenantVigilRules.RESISTANCE_AMPLIFIER,
				false, true, true));
		participant.addEffect(new MobEffectInstance(
				MobEffects.REGENERATION,
				CovenantVigilRules.REWARD_DURATION_TICKS,
				CovenantVigilRules.REGENERATION_AMPLIFIER,
				false, true, true));
		if (participant instanceof ServerPlayer player) {
			player.displayClientMessage(
					Component.literal("Your shared vigil lingers: resistance and regeneration for ten minutes.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
		}
	}

	/**
	 * Rite of Sanguine Fervor (Lesser):
	 * Registers a time-limited spawn-boost zone centred on the rite. For
	 * {@link #SANGUINE_FERVOR_DURATION_TICKS} ticks (5 minutes) natural mob
	 * spawns within a {@link #SANGUINE_FERVOR_CHUNK_RADIUS}-chunk radius are
	 * force-allowed regardless of the global mob cap, greatly increasing local
	 * mob density and making the area ideal for farming.
	 */
	private static void completeSanguineFervor(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineFervorSavedData data = SanguineFervorSavedData.get(overworld);
		String dimension = sLevel.dimension().location().toString();
		long expiryTick = sLevel.getGameTime() + SANGUINE_FERVOR_DURATION_TICKS;

		SanguineFervorSavedData.FervorEntry entry = new SanguineFervorSavedData.FervorEntry(
				caster.getUUID(), center, dimension, SANGUINE_FERVOR_CHUNK_RADIUS, expiryTick);
		data.addEntry(entry);

		int blockRadius = SANGUINE_FERVOR_CHUNK_RADIUS * 16;
		long durationMinutes = SANGUINE_FERVOR_DURATION_TICKS / 1200;
		caster.displayClientMessage(
				Component.literal("The blood heats the earth! Mobs will swarm within "
						+ blockRadius + " blocks for " + durationMinutes + " minutes.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Pre-signs a bloodline ledger with the caster's newly founded bloodline.
	 * Creates the bloodline, registers it in world data, sets the caster's capability,
	 * and writes the signed state onto the ledger item so it can be redistributed.
	 */
	private static void presignBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		String bloodLineName = caster.getName().getString() + "'s Blood Line";
		UUID bloodLineUUID = new UUID(caster.getUUID().getMostSignificantBits(), sLevel.getGameTime());
		ArrayList<UUID> uuids = new ArrayList<>();
		Bloodline playerLine = new Bloodline(bloodLineName, caster.getUUID(), bloodLineUUID, uuids);

		// Register bloodline in world-level saved data
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		savedData.registerBloodline(playerLine);

		// Set the caster's bloodline capability
		var volume = HemoCapabilityAccess.getBloodVolume(caster);
		volume.setBloodLine(playerLine);
		BloodVolumeEvents.syncVolume(caster, volume);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(caster);
		degree.setHasFoundedBloodline(true);
		InitiatoryDegreeEvents.syncDegree(caster, degree);

		// Write signed state and bloodline data onto the ledger
		CompoundTag compound = ledgerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, playerLine.serialize());
		ledgerStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

		caster.displayClientMessage(
				Component.literal("You have founded: " + playerLine.getName())
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	/**
	 * Recalls a lost bloodline ledger by looking up the caster's existing bloodline
	 * from world data and writing it onto the result item. This is a penitent rite 
	 * the covenant does not forget, but it demands a price for carelessness.
	 * Returns false if the caster has no bloodline to recall.
	 */
	private static boolean recallBloodlineLedger(ServerLevel sLevel, ServerPlayer caster, ItemStack ledgerStack) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline existingLine = savedData.getBloodlineForPlayer(caster.getUUID());

		if (existingLine == null || !existingLine.isValid()) {
			return false;
		}

		// Write the existing bloodline data onto the new ledger as a signed copy
		CompoundTag compound = ledgerStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		compound.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		compound.put(UnsignedLedgerItem.TAG_BLOODLINE, existingLine.serialize());
		ledgerStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

		caster.displayClientMessage(
				Component.literal("The covenant remembers. Your ledger for " + existingLine.getName()
						+ " has been restored.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
		return true;
	}

	/** The Fungal Entity remarks on spine growth at Degrees 5-7; Apotheos has its own ending. */
	static void triggerSpineProgressionWhisper(ServerLevel level, ServerPlayer player, int targetDegree) {
		if (targetDegree < 5 || targetDegree > 7) return;
		PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.spineGrowth(targetDegree)));
	}

	private static void completeSanguineEclipse(ServerLevel sLevel, ServerPlayer caster) {
		ServerLevel overworld = sLevel.getServer().overworld();
		BloodMoonSavedData bloodMoonData = BloodMoonSavedData.get(overworld);
		if (bloodMoonData.isActive()) {
			caster.displayClientMessage(
					Component.literal("A Blood Moon already reigns over this world...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}
		long gameTime = overworld.getGameTime();
		bloodMoonData.start(gameTime + 11900L);
		for (ServerPlayer p : overworld.getPlayers(ServerPlayer::isAlive)) {
			p.sendSystemMessage(Component.translatable("hemomancy.blood_moon.start")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
		}
		PacketHandler.sendToAll(new PacketSyncBloodMoon(true));
		caster.displayClientMessage(
				Component.literal("The ritual tears the veil the Blood Moon rises!")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	private static boolean completeFoundingFane(ServerLevel sLevel, ServerPlayer caster, BlockPos center) {
		BlockPos heartPos = center.above(3);
		Component refusal = faneRefusal(sLevel, caster, heartPos);
		if (refusal != null) {
			caster.displayClientMessage(refusal.copy().withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return false;
		}
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(caster).getBloodLine();
		UUID faneOwner = bloodline.getLeaderUUID();
		FoundingFaneSavedData faneData = FoundingFaneSavedData.get(sLevel);
		if (!sLevel.getBlockState(heartPos).is(BlockInit.consecrated_bloodwell.get())
				&& !sLevel.setBlock(heartPos, BlockInit.consecrated_bloodwell.get().defaultBlockState(), 3)) {
			caster.displayClientMessage(
					Component.literal("The rite falters before the Consecrated Bloodwell can take form.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return false;
		}
		boolean isReconsecrating = false;
		for (ServerLevel oldLevel : sLevel.getServer().getAllLevels()) {
			FoundingFaneSavedData oldData = FoundingFaneSavedData.get(oldLevel);
			BlockPos oldHeart = oldData.getHeart(faneOwner);
			if (oldHeart == null) continue;
			isReconsecrating = true;
			for (BlockPos stakePos : oldData.removeStakesAndGet(faneOwner)) {
				if (oldLevel.getBlockState(stakePos).is(BlockInit.hematic_stake.get())) {
					oldLevel.removeBlock(stakePos, false);
				}
			}
			if (oldLevel != sLevel || !oldHeart.equals(heartPos)) {
				if (oldLevel.getBlockState(oldHeart).is(BlockInit.consecrated_bloodwell.get())) {
					oldLevel.removeBlock(oldHeart, false);
				}
				oldData.remove(faneOwner);
			}
		}
		faneData.consecrateHeart(faneOwner, heartPos);
		com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(
				caster, BlockInit.consecrated_bloodwell.get());
		if (isReconsecrating) {
			caster.displayClientMessage(
					Component.literal("Your Founding Fane has been moved to this location.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
		} else {
			for (ServerPlayer p : sLevel.getPlayers(ServerPlayer::isAlive)) {
				p.sendSystemMessage(Component.literal(caster.getDisplayName().getString()
						+ " has consecrated a Founding Fane.")
						.withStyle(ChatFormatting.DARK_RED));
			}
			caster.displayClientMessage(
					Component.literal("This ground is now consecrated. All Harbingers within will feel its power.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
			HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED);
			HarbingerAdvancementGranter.grantIfNotDone(caster,
					HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
		}
		sLevel.sendParticles(HemoParticleData.bloodCell(new ParticleColor(170, 8, 20)),
				heartPos.getX() + 0.5, heartPos.getY() + 1.0, heartPos.getZ() + 0.5,
				300, FoundingFaneSavedData.FANE_RADIUS * 0.3, 3.0, FoundingFaneSavedData.FANE_RADIUS * 0.3, 0.01);
		return true;
	}
}
