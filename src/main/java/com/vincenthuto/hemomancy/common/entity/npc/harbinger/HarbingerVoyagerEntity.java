package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueItemInquiryNodes;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVoyagerDialogueTrees;
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

public class HarbingerVoyagerEntity extends PathfinderMob implements ProgressionDialogueNpc {
	public final AnimationState idleAnimationState = new AnimationState();

	public HarbingerVoyagerEntity(EntityType<? extends HarbingerVoyagerEntity> type, Level level) {
		super(type, level);
		this.setInvulnerable(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D)
				.add(Attributes.FOLLOW_RANGE, 16.0D);
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
			DialogueTree tree = progressionDialogue(serverPlayer);
			tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer, "voyager", degree, 0f);
			tree = DialogueHubFactory.decorate(tree, "voyager", serverPlayer);

			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
	}

	@Override
	public DialogueTree progressionDialogue(ServerPlayer player) {
		if (hasClarityUnlocked(player)) return HarbingerVoyagerDialogueTrees.clarity(this.getId());
		if (isPurifying(player)) return HarbingerVoyagerDialogueTrees.purifying(this.getId());
		return HarbingerVoyagerDialogueTrees.forDegree(HemoCapabilityAccess.getPlayerDegreeNumber(player), this.getId());
	}

	@Override
	public String progressionDialogueId() {
		return "voyager";
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
