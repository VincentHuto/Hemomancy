package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Unclosing Eye — Canon Memory of Saint Seraphae.
 * Doctrine: Witness / Light
 *
 * <p>Seraphae's gaze strips concealment from every living thing within range —
 * concealment is suppressed, all entities glow, and the caster is revealed too.
 * There is no hiding from a witness this complete.
 *
 * <p>Unlike Crimson Sight (which scouts enemies while the caster stays dark),
 * the Unclosing Eye creates total mutual exposure. It is an anti-stealth weapon,
 * not a scouting tool.
 */
public class UnclosingEyeManip extends BloodManipulation {

	private static final int GLOWING_DURATION = 600;  // 30 seconds
	private static final int NIGHT_VISION_DURATION = 600;
	private static final double SCAN_RADIUS = 32.0;

	public UnclosingEyeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		ManipulationReactiveEvents.armUnclosingEye(player, GLOWING_DURATION);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(SCAN_RADIUS), e -> e != player && e.isAlive());

		int stripped = 0;
		for (LivingEntity target : targets) {
			if (target.isInvisible()) stripped++;
            com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(player, target,
                    com.vincenthuto.hemomancy.common.damage.SchoolState.ILLUMINATED, GLOWING_DURATION);
		}

		com.vincenthuto.hemomancy.common.damage.SchoolStates.apply(player, player,
                com.vincenthuto.hemomancy.common.damage.SchoolState.ILLUMINATED, GLOWING_DURATION);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, true));

		String msg = stripped > 0
				? "Seraphae's eye opens. Nothing is hidden. " + stripped + " concealment(s) exposed."
				: "Seraphae's eye opens. All is laid bare — including yourself.";
		player.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);

		world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);

		if (world instanceof ServerLevel sLevel) {
			ManipulationParticles.accent(sLevel, EnumBloodTendency.LUX, player.position().add(0, 2.5, 0), net.minecraft.world.phys.Vec3.ZERO);
		}
	        }
    }
}
