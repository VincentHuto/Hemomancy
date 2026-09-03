package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class CircusRingmasterGameTests {
	private CircusRingmasterGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void perchedRingmasterCannotBeMovedOrHarmed(GameTestHelper helper) {
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		Zombie attacker = EntityType.ZOMBIE.create(helper.getLevel());
		helper.assertTrue(ringmaster != null && attacker != null, "Ringmaster fixture must create");
		ringmaster.setPos(helper.absolutePos(new BlockPos(4, 4, 4)).getCenter());
		attacker.setPos(helper.absolutePos(new BlockPos(5, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(ringmaster);
		helper.getLevel().addFreshEntity(attacker);
		var position = ringmaster.position();
		float health = ringmaster.getHealth();
		ringmaster.push(1.0D, 1.0D, 1.0D);
		boolean hurt = ringmaster.hurt(ringmaster.damageSources().mobAttack(attacker), 8.0F);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(!hurt && ringmaster.getHealth() == health,
					"Perched Ringmaster must ignore ordinary damage");
			helper.assertTrue(ringmaster.position().equals(position) && ringmaster.getDeltaMovement().lengthSqr() == 0.0D,
					"Perched Ringmaster must remain fixed to the rafter");
			helper.assertTrue(!ringmaster.isPickable() && !ringmaster.isAttackable() && ringmaster.getTarget() == null,
					"Perched Ringmaster must not expose interaction or combat");
			ringmaster.discard();
			attacker.discard();
			helper.succeed();
		});
	}
}
