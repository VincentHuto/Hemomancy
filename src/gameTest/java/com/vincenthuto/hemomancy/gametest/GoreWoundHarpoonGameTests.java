package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.projectile.GoreWoundHarpoonEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("gore_wound_harpoon_validation")
@PrefixGameTestTemplate(false)
public final class GoreWoundHarpoonGameTests {
    private static final String ROOM = "empty";

    @GameTest(template = ROOM, batch = "gore_wound_harpoon")
    public static void bowsAndCrossbowsSelectHarpoonAsAmmunition(GameTestHelper h) {
        var shooter = h.makeMockPlayer(GameType.SURVIVAL);
        ItemStack harpoons = new ItemStack(ItemInit.gore_wound_harpoon.get(), 3);
        shooter.getInventory().setItem(0, harpoons);
        h.assertTrue(harpoons.is(ItemTags.ARROWS), "Harpoon must be in the runtime arrows item tag");
        h.assertTrue(shooter.getProjectile(new ItemStack(Items.BOW)).is(ItemInit.gore_wound_harpoon.get()),
                "Vanilla bow must select the harpoon from inventory");
        h.assertTrue(shooter.getProjectile(new ItemStack(Items.CROSSBOW)).is(ItemInit.gore_wound_harpoon.get()),
                "Vanilla crossbow must select the harpoon from inventory");
        h.assertTrue(shooter.getProjectile(new ItemStack(ItemInit.living_crossbow.get()))
                .is(ItemInit.gore_wound_harpoon.get()), "Living Crossbow must select the harpoon from inventory");
        h.succeed();
    }

    @GameTest(template = ROOM, batch = "gore_wound_harpoon", timeoutTicks = 60)
    public static void groupedWitherSkeletonsDoNotMakeHarpoonPierce(GameTestHelper h) {
        var shooter = h.makeMockPlayer(GameType.SURVIVAL);
        shooter.setPos(h.absolutePos(new BlockPos(1, 4, 3)).getCenter());
        var first = h.spawn(EntityType.WITHER_SKELETON, new BlockPos(4, 4, 3));
        var second = h.spawn(EntityType.WITHER_SKELETON, new BlockPos(5, 4, 3));
        first.setNoAi(true);
        second.setNoAi(true);
        first.setNoGravity(true);
        second.setNoGravity(true);
        GoreWoundHarpoonEntity arrow = ((com.vincenthuto.hemomancy.common.item.harbinger.tool.living.GoreWoundHarpoonItem)
                ItemInit.gore_wound_harpoon.get()).createArrow(h.getLevel(),
                new ItemStack(ItemInit.gore_wound_harpoon.get()), shooter, new ItemStack(Items.BOW));
        h.assertTrue(arrow.getPierceLevel() == 0, "A tethering harpoon must not enter the piercing collision loop");
        arrow.setPos(h.absolutePos(new BlockPos(2, 5, 3)).getCenter());
        arrow.shoot(1.0D, 0.0D, 0.0D, 2.0F, 0.0F);
        h.getLevel().addFreshEntity(arrow);
        h.succeedWhen(() -> {
            if (!arrow.isRemoved()) arrow.tick();
            h.assertTrue(first.getHealth() < first.getMaxHealth()
                            && second.getHealth() == second.getMaxHealth()
                            && arrow.targetId() == first.getId(),
                    "A hit must tether only the first skeleton without piercing through the group");
        });
    }

    @GameTest(template = ROOM, batch = "gore_wound_harpoon", timeoutTicks = 60)
    public static void ordinaryShotTethersAndPullsWoundedMob(GameTestHelper h) {
        Cow shooter = h.spawn(EntityType.COW, new BlockPos(1, 2, 3));
        Cow target = h.spawn(EntityType.COW, new BlockPos(5, 2, 3));
        shooter.setNoAi(true);
        target.setNoAi(true);
        target.setNoGravity(true);
        GoreWoundHarpoonEntity arrow = ((com.vincenthuto.hemomancy.common.item.harbinger.tool.living.GoreWoundHarpoonItem)
                ItemInit.gore_wound_harpoon.get()).createArrow(h.getLevel(),
                new ItemStack(ItemInit.gore_wound_harpoon.get()), shooter, new ItemStack(net.minecraft.world.item.Items.BOW));
        Vec3 start = h.absolutePos(new BlockPos(2, 2, 3)).getCenter();
        arrow.setPos(start);
        arrow.shoot(1.0D, 0.0D, 0.0D, 2.0F, 0.0F);
        h.getLevel().addFreshEntity(arrow);
        h.succeedWhen(() -> {
            if (!arrow.isRemoved()) arrow.tick();
            h.assertTrue(arrow.targetId() == target.getId()
                    && target.getHealth() < target.getMaxHealth() && target.getDeltaMovement().x < 0.0D,
                    "Harpoon must damage, tether and pull: target=" + arrow.targetId()
                            + " health=" + target.getHealth() + " vx=" + target.getDeltaMovement().x
                            + " removed=" + arrow.isRemoved() + " pos=" + arrow.position());
        });
    }

    @GameTest(template = ROOM, batch = "gore_wound_harpoon", timeoutTicks = 60)
    public static void livingCrossbowShotReturnsAfterBlockImpact(GameTestHelper h) {
        var shooter = h.makeMockPlayer(GameType.SURVIVAL);
        shooter.setPos(h.absolutePos(new BlockPos(1, 2, 3)).getCenter());
        h.setBlock(new BlockPos(4, 2, 3), Blocks.STONE);
        GoreWoundHarpoonEntity arrow = ((com.vincenthuto.hemomancy.common.item.harbinger.tool.living.GoreWoundHarpoonItem)
                ItemInit.gore_wound_harpoon.get()).createArrow(h.getLevel(),
                new ItemStack(ItemInit.gore_wound_harpoon.get()), shooter,
                new ItemStack(ItemInit.living_crossbow.get()));
        arrow.setPos(h.absolutePos(new BlockPos(2, 2, 3)).getCenter());
        arrow.shoot(1.0D, 0.0D, 0.0D, 2.0F, 0.0F);
        h.getLevel().addFreshEntity(arrow);
        h.succeedWhen(() -> {
            if (!arrow.isRemoved()) arrow.tick();
            h.assertTrue(arrow.isRemoved()
                    && shooter.getInventory().countItem(ItemInit.gore_wound_harpoon.get()) == 1,
                    "Living Crossbow return: removed=" + arrow.isRemoved()
                            + " returning=" + arrow.isReturning()
                            + " count=" + shooter.getInventory().countItem(ItemInit.gore_wound_harpoon.get())
                            + " pos=" + arrow.position());
        });
    }

    @GameTest(template = ROOM, batch = "gore_wound_harpoon", timeoutTicks = 90)
    public static void livingCrossbowShotReturnsAfterMiss(GameTestHelper h) {
        var shooter = h.makeMockPlayer(GameType.SURVIVAL);
        shooter.setPos(h.absolutePos(new BlockPos(1, 4, 3)).getCenter());
        GoreWoundHarpoonEntity arrow = ((com.vincenthuto.hemomancy.common.item.harbinger.tool.living.GoreWoundHarpoonItem)
                ItemInit.gore_wound_harpoon.get()).createArrow(h.getLevel(),
                new ItemStack(ItemInit.gore_wound_harpoon.get()), shooter,
                new ItemStack(ItemInit.living_crossbow.get()));
        arrow.setPos(h.absolutePos(new BlockPos(2, 4, 3)).getCenter());
        arrow.shoot(0.0D, 1.0D, 0.0D, 2.0F, 0.0F);
        h.getLevel().addFreshEntity(arrow);
        h.succeedWhen(() -> {
            if (!arrow.isRemoved()) arrow.tick();
            h.assertTrue(arrow.isRemoved()
                    && shooter.getInventory().countItem(ItemInit.gore_wound_harpoon.get()) == 1,
                    "A missed Living Crossbow harpoon must return one item within 90 ticks");
        });
    }
}
