package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;

/**
 * The Rite of Visitation: an Apotheos carried bodily into the Gardens from a rite space, onto their heart-node when
 * they have one. The usual Spine return point is stored, so the Spine or a Podium brings them home.
 */
public final class VisitationService {
	private static final ParticleColor PALE = new ParticleColor(226, 206, 200);

	private VisitationService() {}

	@Nullable
	public static Component refusal(ServerPlayer caster) {
		if (!ApotheosBodyEvents.isApotheos(caster)) return Component.translatable("message.hemomancy.visitation.not_apotheos");
		if (caster.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS))
			return Component.translatable("message.hemomancy.visitation.already");
		if (caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS) == null)
			return Component.translatable("message.hemomancy.possession.no_gardens");
		return null;
	}

	public static boolean visit(ServerPlayer caster) {
		ServerLevel gardens = caster.server.getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		if (gardens == null || refusal(caster) != null) return false;
		caster.serverLevel().sendParticles(HemoParticleData.sporiticSpore(PALE), caster.getX(), caster.getY() + 1.0,
				caster.getZ(), 30, 0.4, 0.8, 0.4, 0.03);
		FungalGardenTravelHelper.storeReturnPosition(caster);
		BlockPos node = HeartNodeSavedData.get(gardens).nodeOf(caster.getUUID());
		boolean onNode = node != null && VisitationRules.landOnNode(
				gardens.getBlockState(node).is(BlockInit.heart_node.get()),
				gardens.getBlockState(node.above()).getCollisionShape(gardens, node.above()).isEmpty(),
				gardens.getBlockState(node.above(2)).getCollisionShape(gardens, node.above(2)).isEmpty());
		BlockPos landing = onNode ? node.above() : node != null
				? FungalGardenTravelHelper.findSafePos(gardens, node.getX(), node.getZ())
				: FungalGardenTravelHelper.findSafePos(gardens, caster.getBlockX(), caster.getBlockZ());
		caster.teleportTo(gardens, landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5, caster.getYRot(),
				caster.getXRot());
		gardens.playSound(null, landing, SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 1.0f, 0.6f);
		caster.displayClientMessage(Component.translatable(onNode ? "message.hemomancy.visitation.home"
				: "message.hemomancy.visitation.arrived").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
		return true;
	}
}
