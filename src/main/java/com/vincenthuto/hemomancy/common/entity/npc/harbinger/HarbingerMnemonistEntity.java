package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueItemInquiryNodes;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MnemonistStarterMemoryChoice;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.NoeticDiscoveryProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A patient keeper of blood-memory practice stationed within Harbinger outposts.
 * The Mnemonist teaches crude memories, active manipulation slots, and the later
 * tools used to order and weave refined memories.
 */
public class HarbingerMnemonistEntity extends PathfinderMob {
	public final AnimationState idleAnimationState = new AnimationState();

	public HarbingerMnemonistEntity(EntityType<? extends HarbingerMnemonistEntity> type, Level level) {
		super(type, level);
		this.setInvulnerable(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.GENERIC_KILL)) {
			return super.hurt(source, amount);
		}
		if (source.getEntity() instanceof Player player && player.isCreative()) {
			return super.hurt(source, amount);
		}
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void tick() {
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);
		}
		super.tick();
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (NoeticDiscoveryProgression.recognizeFromMnemonist(serverPlayer)) {
				serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.translatable(
						"hemomancy.dialogue.mnemonist.conductive_mark_recognized"), false);
			}
			DialogueTree tree;

			boolean purifying = isPurifying(player);
			boolean clarity = hasClarityUnlocked(player);
			boolean claimed = player.getPersistentData().getBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY);
			boolean canClaimStarter = MnemonistStarterMemoryChoice.canClaim(degree, purifying, clarity, claimed);
			if (clarity) {
				tree = HarbingerMnemonistDialogueTrees.clarity(this.getId());
			} else if (purifying) {
				tree = HarbingerMnemonistDialogueTrees.purifying(this.getId());
			} else {
				tree = HarbingerMnemonistDialogueTrees.forDegree(degree, this.getId(), canShowRecruitment(player, this),
						isNpcInPlayerBloodline(player, this), canClaimStarter,
						HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer),
						BoundSummonBehavior.hasEquippedMorphling(serverPlayer)
								&& BoundSummonBehavior.hasActiveOwnedTether(serverPlayer));
			}
			tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer, "mnemonist", degree, 0f);
			tree = DialogueHubFactory.decorate(tree, "mnemonist", serverPlayer);

			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
	}

	private static boolean hasClarityUnlocked(Player player) {
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(IUnstainedProgress::hasClarityUnlocked)
				.orElse(false);
	}

	private static boolean isPurifying(Player player) {
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(IUnstainedProgress::hasBegunPurification)
				.orElse(false);
	}

	private static boolean hasBloodline(Player player) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.getBloodLine().isValid())
				.orElse(false);
	}

	private static boolean isNpcInPlayerBloodline(Player player, Entity npc) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.getBloodLine().isValid() && vol.getBloodLine().hasNpcMember(npc.getUUID()))
				.orElse(false);
	}

	private static boolean canShowRecruitment(Player player, Entity npc) {
		return HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.getBloodLine().isValid()
						&& (vol.getBloodLine().hasNpcMember(npc.getUUID())
						|| HarbingerRecruitmentRules.canRecruitNpc(vol.getBloodLine(), npc)))
				.orElse(false);
	}
}
