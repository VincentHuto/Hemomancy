package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MortalDisplayActivationMessageTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");
	private static final Path LANG_FILE = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");

	private MortalDisplayActivationMessageTest() {
	}

	public static void main(String[] args) throws IOException {
		mortalDisplayDoesNotSendObsoleteActivationMessage();
		livingAdornmentMessageRemainsServerSide();
	}

	private static void mortalDisplayDoesNotSendObsoleteActivationMessage() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/block/harbinger/functional/MortalDisplayBlock.java");
		String language = Files.readString(LANG_FILE).replace("\r\n", "\n");
		assertDoesNotContain("mortal display block", source, "hemomancy.mortal_display.activated");
		assertDoesNotContain("language resources", language, "\"hemomancy.mortal_display.activated\"");
	}

	private static void livingAdornmentMessageRemainsServerSide() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/block/harbinger/functional/MortalDisplayBlock.java");
		int charmMessage = source.indexOf("hemomancy.mortal_display.charm_equipped");
		if (charmMessage < 0) {
			throw new AssertionError("living adornment message key is missing");
		}
		int clientReturn = source.lastIndexOf("if (worldIn.isClientSide) return", charmMessage);
		if (clientReturn < 0) {
			throw new AssertionError("living adornment message is not behind the server-side early return");
		}
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(JAVA_ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
