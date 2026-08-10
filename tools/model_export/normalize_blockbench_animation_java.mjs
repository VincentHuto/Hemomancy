import fs from "node:fs";
import { pathToFileURL } from "node:url";

const IMPORTS = `import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;`;

export function normalizeBlockbenchAnimationJava(source, { packageName, className }) {
	const body = source
		.replace(/^\uFEFF/, "")
		.replace(/public class \w+\s*\{/, `public final class ${className} {\n\tprivate ${className}() { }`)
		.replace(/AnimationDefinition\s+animation\.[A-Za-z0-9_]+\.([a-zA-Z0-9_]+)/g,
			(_match, animationName) => `AnimationDefinition ${animationName.toUpperCase()}`)
		.trim();
	return `package ${packageName};\n\n${IMPORTS}\n\n${body}\n`;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
	const [input, output, packageName, className] = process.argv.slice(2);
	if (!className) throw new Error("Usage: node normalize_blockbench_animation_java.mjs input.java output.java package class");
	const source = fs.readFileSync(input, "utf8");
	fs.writeFileSync(output, normalizeBlockbenchAnimationJava(source, { packageName, className }));
}
