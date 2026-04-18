package com.vincenthuto.hemomancy.common.item.bloodline;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.rite.CrimsonLodgeEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class UnsignedLedgerItem extends Item {

	public static String TAG_STATE = "state";
	public static String TAG_BLOODLINE = "bloodline";

	public UnsignedLedgerItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (!stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("Unsigned").withStyle(ChatFormatting.GRAY));
			} else {
				if (stack.getTag().contains(TAG_BLOODLINE)) {
					Bloodline line = Bloodline.deserialize(stack.getTag().getCompound(TAG_BLOODLINE));
					tooltip.add(Component.literal("Signed with: " + line.getName())
							.withStyle(ChatFormatting.RED));
					tooltip.add(Component.literal("Members: " + line.getPlayerUUIDS().size())
							.withStyle(ChatFormatting.DARK_RED));
				}
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.hasTag() && stack.getTag().getBoolean(TAG_STATE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (stack.getItem() instanceof UnsignedLedgerItem) {
			CompoundTag compound = stack.getOrCreateTag();
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);

			// Sneak-right-click on a signed ledger: summon recruited NPCs within Lodge radius
			if (playerIn.isShiftKeyDown() && compound.getBoolean(TAG_STATE)) {
				if (!worldIn.isClientSide) {
					handleNpcSummon((ServerPlayer) playerIn, volume);
				}
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
			}

			if (!compound.getBoolean(TAG_STATE)) {
				// First use: leader signs the ledger, creating a new bloodline
				String bloodLineName = playerIn.getName().getString() + "'s Blood Line";
				ArrayList<UUID> uuids = new ArrayList<>();
				UUID bloodLineUUID = new UUID(playerIn.getUUID().getMostSignificantBits(),
						playerIn.level().getGameTime());
				Bloodline playerLine = new Bloodline(bloodLineName, playerIn.getUUID(), bloodLineUUID, uuids);

				playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, true);
				compound.put(TAG_BLOODLINE, playerLine.serialize());
				stack.setTag(compound);

				if (!worldIn.isClientSide) {
					volume.setBloodLine(playerLine);
					// Register in world-level saved data
					ServerLevel overworld = ((ServerLevel) worldIn).getServer().overworld();
					BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
					savedData.registerBloodline(playerLine);

					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new BloodVolumeServerPacket(volume));
					playerIn.displayClientMessage(
							Component.literal("You have founded: " + playerLine.getName())
									.withStyle(ChatFormatting.DARK_RED),
							true);

					// Award bloodline milestone
					SkillPointGainEvents.onBloodlineJoined((ServerPlayer) playerIn);
				}
			} else {
				// Second use: another player signs to join the bloodline
				if (!worldIn.isClientSide) {
					Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
					if (!savedLine.isValid()) {
						playerIn.displayClientMessage(
								Component.literal("This ledger's bloodline is corrupted")
										.withStyle(ChatFormatting.RED),
								true);
						return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
					}

					if (savedLine.hasMember(playerIn.getUUID())) {
						playerIn.displayClientMessage(Component.literal("Already in this bloodline"), true);
					} else {
						// Add to global saved data
						ServerLevel overworld = ((ServerLevel) worldIn).getServer().overworld();
						BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
						Bloodline globalLine = savedData.addMember(savedLine.getBloodlineUUID(),
								playerIn.getUUID());

						if (globalLine != null) {
							volume.setBloodLine(globalLine);
							// Update the item tag with the latest bloodline state
							compound.put(TAG_BLOODLINE, globalLine.serialize());
							stack.setTag(compound);

							playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
							PacketHandler.CHANNELBLOODVOLUME.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
									new BloodVolumeServerPacket(volume));
							playerIn.displayClientMessage(
									Component.literal("You have joined: " + globalLine.getName())
											.withStyle(ChatFormatting.DARK_RED),
									true);

							// Award bloodline milestone
							SkillPointGainEvents.onBloodlineJoined((ServerPlayer) playerIn);

							// Notify online bloodline members
							for (Player member : globalLine.getPlayers(worldIn)) {
								if (!member.getUUID().equals(playerIn.getUUID())) {
									member.displayClientMessage(
											Component.literal(playerIn.getName().getString()
													+ " has joined your bloodline!")
													.withStyle(ChatFormatting.DARK_RED),
											false);
								}
							}
						} else {
							playerIn.displayClientMessage(
									Component.literal("Bloodline not found in world data")
											.withStyle(ChatFormatting.RED),
									true);
						}
					}
				}
			}

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	/**
	 * Summons all recruited NPC Harbingers to the player's position. Only
	 * works when the player is within a consecrated Crimson Lodge radius.
	 * NPCs already nearby (within 16 blocks) are not re-summoned. NPCs
	 * that are loaded in the world are teleported; for NPCs not currently
	 * loaded, a new entity is spawned at the player's location.
	 */
	private static void handleNpcSummon(ServerPlayer player, IBloodVolume volume) {
		Bloodline bloodline = volume.getBloodLine();

		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.no_bloodline")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (bloodline.getNpcMemberCount() == 0) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.no_npcs")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Must be within a Crimson Lodge radius
		if (!CrimsonLodgeEvents.isInCrimsonLodge(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.not_in_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel sLevel = (ServerLevel) player.level();
		int summoned = 0;

		for (UUID npcUUID : bloodline.getNpcMemberUUIDs()) {
			// Check if the entity is already nearby — don't re-summon
			Entity existing = sLevel.getEntity(npcUUID);
			if (existing != null && existing.distanceTo(player) < 16.0) {
				continue;
			}

			if (existing != null) {
				// Teleport existing entity to player
				existing.teleportTo(player.getX() + (player.getRandom().nextDouble() - 0.5) * 3.0,
						player.getY(),
						player.getZ() + (player.getRandom().nextDouble() - 0.5) * 3.0);
				summoned++;
			} else {
				// Entity not loaded — spawn a transient copy near the player.
				// Determine type based on available registered entity types.
				spawnRecruitedNpc(sLevel, player, npcUUID);
				summoned++;
			}
		}

		if (summoned > 0) {
			sLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.success", summoned)
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.already_near")
							.withStyle(ChatFormatting.GRAY),
					false);
		}
	}

	/**
	 * Spawns a recruited NPC Harbinger near the player. Tries each Harbinger
	 * entity type and checks if an existing entity in the world matches the
	 * UUID. If no match is found in the world, spawns a Harbinger Vicar as a
	 * default representative of the recruited NPC.
	 */
	private static void spawnRecruitedNpc(ServerLevel level, ServerPlayer player, UUID npcUUID) {
		// Search all loaded entities in any dimension for a match first
		for (ServerLevel dim : level.getServer().getAllLevels()) {
			Entity found = dim.getEntity(npcUUID);
			if (found instanceof Mob mob) {
				mob.teleportTo(player.getX() + (player.getRandom().nextDouble() - 0.5) * 3.0,
						player.getY(),
						player.getZ() + (player.getRandom().nextDouble() - 0.5) * 3.0);
				return;
			}
		}

		// Not found — spawn a new Harbinger entity near the player
		// Cycle through known entity types to spawn the appropriate NPC
		EntityType<?>[] harbingerTypes = {
			EntityInit.harbinger_vicar.get(),
			EntityInit.harbinger_alchemist.get()
		};
		EntityType<?> type = harbingerTypes[Math.abs(npcUUID.hashCode()) % harbingerTypes.length];

		Entity spawned = type.create(level);
		if (spawned != null) {
			spawned.setUUID(npcUUID);
			spawned.setPos(player.getX() + (player.getRandom().nextDouble() - 0.5) * 3.0,
					player.getY(),
					player.getZ() + (player.getRandom().nextDouble() - 0.5) * 3.0);
			level.addFreshEntity(spawned);
		}
	}
}
