import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "src/main/resources/assets/hemomancy/models/item"
DISPATCHER = "living_staff.json"


def main() -> None:
    failures = []
    models = sorted(MODEL_DIR.glob("living_staff*.json"))

    for model_path in models:
        model = json.loads(model_path.read_text(encoding="utf-8"))
        if model_path.name != DISPATCHER and "overrides" in model:
            failures.append(
                f"{model_path.name}: independent model must not define overrides"
            )

    dispatcher = MODEL_DIR / DISPATCHER
    model = json.loads(dispatcher.read_text(encoding="utf-8"))
    if "overrides" not in model:
        failures.append(f"{DISPATCHER}: dispatcher is missing overrides")

    if failures:
        raise SystemExit("\n".join(failures))
    print(f"Validated {len(models)} living staff models with one dispatcher")


if __name__ == "__main__":
    main()
