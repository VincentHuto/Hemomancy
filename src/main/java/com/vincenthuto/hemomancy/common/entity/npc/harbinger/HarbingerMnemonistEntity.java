package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.shared.NoeticDiscoveryProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
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
public class HarbingerMnemonistEntity extends PathfinderMob implements ProgressionDialogueNpc {
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
			DialogueTree tree = progressionDialogue(serverPlayer);
			tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer, "mnemonist", degree, 0f);
			tree = DialogueHubFactory.decorate(tree, "mnemonist", serverPlayer);

			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
	}

	@Override
	public DialogueTree progressionDialogue(ServerPlayer serverPlayer) {
		Player player = serverPlayer;
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer);
		boolean purifying = isPurifying(serverPlayer);
		boolean clarity = hasClarityUnlocked(serverPlayer);
		boolean claimed = serverPlayer.getPersistentData().getBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY);
		if (clarity) return HarbingerMnemonistDialogueTrees.clarity(this.getId());
		if (purifying) return HarbingerMnemonistDialogueTrees.purifying(this.getId());
		return HarbingerMnemonistDialogueTrees.forDegree(degree, this.getId(), canShowRecruitment(player, this),
				isNpcInPlayerBloodline(player, this),
				MnemonistStarterMemoryChoice.canClaim(degree, false, false, claimed),
				HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer),
				BoundSummonBehavior.hasEquippedMorphling(serverPlayer) && BoundSummonBehavior.hasActiveOwnedTether(serverPlayer),
				com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.has(serverPlayer,
						com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.D6_REFERRAL),
				com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.has(serverPlayer,
						com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments.D6_COUNSEL));
	}

	@Override
	public String progressionDialogueId() {
		return "mnemonist";
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
