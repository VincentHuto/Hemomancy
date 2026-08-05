package com.vincenthuto.hemomancy.common.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;
import org.junit.jupiter.api.Test;

class QliphothTreeStageTest {

	@Test
	void treeStageArgumentIsNestedUnderTheQliphothCommand() {
		CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
		HemoCommand.register(dispatcher);

		var qliphoth = dispatcher.getRoot().getChild("hemo").getChild("qliphoth");
		var tree = qliphoth.getChild("tree");
		assertNotNull(tree);
		assertNotNull(tree.getChild("stage"));
	}

	@Test
	void namedAndNumericGrowthStagesResolveToTheirPomeCounts() {
		assertEquals(0, QliphothTreeStage.parse("initial").pomesDropped());
		assertEquals(4, QliphothTreeStage.parse("4").pomesDropped());
		assertEquals(9, QliphothTreeStage.parse("fully_grown").pomesDropped());
		assertEquals(9, QliphothTreeStage.parse("max").pomesDropped());
	}

	@Test
	void prunedStageUsesTheSeveredTreeState() {
		QliphothTreeStage stage = QliphothTreeStage.parse("pruned");

		assertEquals(9, stage.pomesDropped());
		assertEquals(SeveredQliphothState.OPEN, stage.severedState());
		assertEquals(SeveredQliphothState.SEALED, QliphothTreeStage.parse("sealed").severedState());
}

	@Test
	void invalidStagesAreRejected() {
		assertNull(QliphothTreeStage.parse("10"));
		assertNull(QliphothTreeStage.parse("not_a_stage"));
	}
}
