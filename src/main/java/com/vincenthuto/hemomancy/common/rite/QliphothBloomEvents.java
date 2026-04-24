package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/**
 * Handles tick-based effects for Qliphoth Blooms established by the Bloom of
 * the Qliphoth rite. Players within the bloom's radius receive:
 * <ul>
 *   <li>Regeneration I effect (health regen)</li>
 *   <li>Enhanced blood regeneration (+5 blood/tick)</li>
 * </ul>
 * Manipulation cost reduction is handled directly in
 * {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation#performAction}.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QliphothBloomEvents {

	/** Interval in ticks between effect application (40 ticks = 2 seconds). */
	private static final int EFFECT_APPLICATION_INTERVAL_TICKS = 40;
	/** Duration of the Regeneration effect in ticks (slightly longer than interval for seamless coverage). */
	private static final int REGEN_DURATION = 50;
	/** Extra blood regeneration per tick when within a bloom's radius. */
	private static final double BLOOD_REGEN_PER_TICK = 5.0;

	/**
	 * Chance (1 in N) per bloom per effect tick that a Qliphoth Pome drops
	 * from the tree. At 40-tick intervals this averages ~1 pome every
	 * 1 in 80 checks ≈ every 3 minutes.
	 */
	private static final int POME_DROP_CHANCE = 80;
	/** Maximum number of pome item entities allowed near a bloom before it stops dropping more. */
	private static final int POME_MAX_NEARBY = 3;
	/** Radius (blocks) around the bloom center to check for existing pome item entities. */
	private static final double POME_SEARCH_RADIUS = 8.0;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		// Only process every EFFECT_INTERVAL ticks for performance
		if (sLevel.getGameTime() % EFFECT_APPLICATION_INTERVAL_TICKS != 0) return;

		QliphothBloomSavedData data = QliphothBloomSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();

		for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
			if (!bloom.dimension().equals(dimension)) continue;

			int blockRadius = bloom.chunkRadius() * 16;
			AABB bloomBounds = new AABB(bloom.center()).inflate(blockRadius, 64, blockRadius);

			List<ServerPlayer> players = sLevel.getEntitiesOfClass(ServerPlayer.class, bloomBounds,
					player -> player.isAlive());

			for (ServerPlayer player : players) {
				// Grant Regeneration I for seamless health regen
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						REGEN_DURATION, 0, true, false, true));

				// Grant enhanced blood regeneration
				player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
					if (volume.isActive()) {
						double maxBlood = volume.getMaxBloodVolume();
						if (volume.getBloodVolume() < maxBlood) {
							volume.fill(BLOOD_REGEN_PER_TICK);
							BloodVolumeEvents.syncVolume(player, volume);
						}
					}
				});
			}

			// ── Rare Qliphoth Pome drop ──
			trySpawnPome(sLevel, bloom);
		}
	}

	/**
	 * Attempts to spawn a Qliphoth Pome item entity near the bloom's tree.
	 * Only succeeds rarely (1 in {@link #POME_DROP_CHANCE}) and caps the number
	 * of pome items on the ground nearby to prevent accumulation.
	 * <p>
	 * Each dropped pome is tagged with the bloom's center position
	 * ({@code hemomancy:bloom_origin}) and the sequential husk index
	 * ({@code hemomancy:husk_index}, 0–8) so players can track which of the
	 * nine Qliphoth husks they are consuming. Once all nine have dropped the
	 * tree ceases production for this bloom's lifecycle.
	 * <p>
	 * The spawned item entity is invulnerable (won't burn in fire/lava) and has
	 * an unlimited lifespan so it never despawns. The bloom owner (if online)
	 * receives a one-shot Fungal Whisper dialogue naming the husk that fell.
	 */
	private static void trySpawnPome(ServerLevel level, QliphothBloomSavedData.BloomEntry bloom) {
		RandomSource rand = level.getRandom();
		if (rand.nextInt(POME_DROP_CHANCE) != 0) return;

		BlockPos center = bloom.center();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(level.getServer().overworld());
		int alreadyDropped = data.getPomesDropped(center);
		if (alreadyDropped >= QliphothBloomSavedData.MAX_POMES_PER_BLOOM) return;

		// Cap: don't drop if too many pome items already lying around the tree
		AABB searchBox = new AABB(center).inflate(POME_SEARCH_RADIUS, 10, POME_SEARCH_RADIUS);
		long existingPomes = level.getEntitiesOfClass(ItemEntity.class, searchBox,
				e -> e.isAlive() && e.getItem().is(ItemInit.qliphoth_pome.get())).size();
		if (existingPomes >= POME_MAX_NEARBY) return;

		// Build the tagged pome stack
		ItemStack pomeStack = new ItemStack(ItemInit.qliphoth_pome.get());
		net.minecraft.nbt.CompoundTag tag = pomeStack.getOrCreateTag();
		tag.putLong(QliphothPomeItem.BLOOM_ORIGIN_KEY, center.asLong());
		tag.putInt(QliphothPomeItem.HUSK_INDEX_KEY, alreadyDropped);

		// Register the drop in SavedData before spawning (so we don't double-count)
		data.incrementPomesDropped(center);
		// Sync the updated pomes-dropped count to all clients so the tree advances its growth stage
		CardinalRiteEvents.syncQliphothBlooms(level.getServer());

		// Spawn at the tree center with a small random horizontal offset,
		// a few blocks up (as if falling from the canopy), with slight outward velocity
		double spawnX = center.getX() + 0.5 + (rand.nextDouble() - 0.5) * 3.0;
		double spawnY = center.getY() + 4.0 + rand.nextDouble() * 3.0;
		double spawnZ = center.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 3.0;

		ItemEntity pomeEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, pomeStack);
		// Small random velocity so it tumbles away from the trunk
		pomeEntity.setDeltaMovement(
				(rand.nextDouble() - 0.5) * 0.1,
				0.05,
				(rand.nextDouble() - 0.5) * 0.1);
		// Slight pickup delay so it looks like it just dropped
		pomeEntity.setPickUpDelay(20);
		// Invulnerable: won't be destroyed by fire, lava, explosions, or void
		pomeEntity.setInvulnerable(true);
		// Unlimited lifespan: the husk will wait until it is claimed
		pomeEntity.lifespan = Integer.MAX_VALUE;
		level.addFreshEntity(pomeEntity);

		// ── Notify the bloom owner via Fungal Whisper dialogue ──
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(bloom.ownerUUID());
		if (owner != null) {
			PacketHandler.CHANNELBLOODVOLUME.send(
					PacketDistributor.PLAYER.with(() -> owner),
					new OpenDialoguePacket(FungalWhisperDialogueTrees.pomeDropped(alreadyDropped)));
		}
	}

	/**
	 * Checks if the given player is within any Qliphoth Bloom's radius.
	 * Used by manipulation cost reduction logic in BloodManipulation.
	 */
	public static boolean isInQliphothBloom(ServerPlayer player) {
		ServerLevel overworld = player.server.overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = player.level().dimension().location().toString();
		return data.isInBloomRange(player.blockPosition(), dimension);
	}
}
