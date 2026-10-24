package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BloodNeedleManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 20;
	// Arrow damage is ceil(speed * base). These bases keep each needle in the starter band:
	// baseline 2-4 at speed 4-8, fan ~3 at speed 6, lance 6 at speed 8.
	private static final double VOLLEY_BASE_DAMAGE = 0.4D;
	private static final double FAN_BASE_DAMAGE = 0.4D;
	private static final double LANCE_BASE_DAMAGE = 0.75D;
	private final Mode mode;

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		this(name, cost, alignLevel, xpCost, type, rank, tendency, section, Mode.BASELINE);
	}

	public BloodNeedleManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section, Mode mode) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
		this.mode = mode;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		getAction(player, world, heldItemMainhand, position, getRequiredChargeTicks());
	        }
    }

	@Override
	public int getRequiredChargeTicks() {
		return mode == Mode.LANCE ? 30 : CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : chargeTicks / getRequiredChargeTicks())) {

		Vec3 vector3d = player.getLookAngle();
		Vector3 vector3f = new Vector3(vector3d);
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, getRequiredChargeTicks());
		int count = switch (mode) {
			case FAN -> Math.max(1, (int) Math.ceil(24 * strength));
			case LANCE -> 3;
			default -> Math.max(1, (int) Math.ceil((world.random.nextInt(11) + 10) * strength));
		};
		double baseDamage = switch (mode) {
			case FAN -> FAN_BASE_DAMAGE;
			case LANCE -> LANCE_BASE_DAMAGE;
			default -> VOLLEY_BASE_DAMAGE;
		} * SkillPointHelper.getCrimsonMasteryMultiplier(player);
		for (int i = 0; i < count; i++) {
			BloodNeedleEntity needle = BloodNeedleEntity.fromManipulation(world, player, heldItemMainhand);
			needle.setBaseDamage(baseDamage);
			needle.setForged(true);
			needle.setDamageTendency(getTend());
			needle.setSecondaryDamageTendency(getSecondaryTend());
			if (mode == Mode.LANCE) {
				needle.configurePiercing((byte) 3);
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, 8.0F, 0.25F);
			} else if (mode == Mode.FAN) {
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, 6.0F, 22.0F);
			} else {
				needle.shoot(vector3f.x, vector3f.y, vector3f.z, world.random.nextInt(5) + 4,
						world.random.nextInt(20) - world.random.nextInt(20));
			}
			world.addFreshEntity(needle);
		}
	        }
    }

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		if (mode == Mode.LANCE && chargeTicks < getRequiredChargeTicks()) {
			player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.blood_needle_manip.blood_needle_lance_requires_a_full"), true);
			return false;
		}
		return super.canPerformAction(player, heldItemMainhand, chargeTicks);
	}

	public enum Mode {
		BASELINE,
		FAN,
		LANCE
	}

}
