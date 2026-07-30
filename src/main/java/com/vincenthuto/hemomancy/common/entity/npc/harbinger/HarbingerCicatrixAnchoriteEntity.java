package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueItemInquiryNodes;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
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

public class HarbingerCicatrixAnchoriteEntity extends PathfinderMob {
	public final AnimationState idleAnimationState = new AnimationState();

	public HarbingerCicatrixAnchoriteEntity(EntityType<? extends HarbingerCicatrixAnchoriteEntity> type,
			Level level) {
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
			boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player)
					.map(volume -> volume.isActive())
					.orElse(false);
			boolean purifying = isPurifying(player);
			boolean clarity = hasClarityUnlocked(player);
			boolean veinMasonFirstLesson = HarbingerAdvancementGranter.isVeinMasonFirstLesson(serverPlayer);
			boolean veinMasonFirstScarLearned = HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(serverPlayer);
			boolean veinMasonFirstEffigyPattern = HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(serverPlayer);
			boolean veinMasonFirstEffigyLoadout = HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer);
			boolean veinMasonRewardClaimed = HarbingerAdvancementGranter.isVeinMasonRewardClaimed(serverPlayer);
			DialogueTree tree = HarbingerCicatrixAnchoriteDialogueTrees.forState(this.getId(), degree, activeBlood,
					purifying, clarity, veinMasonFirstLesson, veinMasonFirstScarLearned,
					veinMasonFirstEffigyPattern, veinMasonFirstEffigyLoadout, veinMasonRewardClaimed);
			tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer,
					"cicatrix_anchorite", degree, 0f);
			tree = DialogueHubFactory.decorate(tree, "cicatrix_anchorite", serverPlayer);

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
}
