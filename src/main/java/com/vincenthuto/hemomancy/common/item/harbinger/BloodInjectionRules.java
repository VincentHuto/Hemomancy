package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.Predicate;

/** Pure response parsing and selection; effect application belongs to the vial transaction. */
public final class BloodInjectionRules {
    public static final int USE_TICKS = 16, SATURATION_TICKS = 400;
    public static final float RESISTANCE_MULTIPLIER = 0.75F, HEALING_MULTIPLIER = 0.5F, MINING_MULTIPLIER = 1.25F;
    public static final double ARMOR = 4, KNOCKBACK_RESISTANCE = 0.1;
    private BloodInjectionRules() {}

    public record Benefit(ResourceLocation effect, int duration, int amplifier, double radius) {
        public String identity() { return effect == null ? "reveal" : effect.toString(); }
    }
    public record Contribution(Benefit benefit, List<Benefit> drawbacks) {
        public Contribution { drawbacks = List.copyOf(drawbacks); }
    }
    public record Response(EnumBloodTendency tendency, ResourceLocation property, List<Contribution> contributions) {
        public Response { contributions = List.copyOf(contributions); }
        public String origin() { return tendency == null ? property.toString() : tendency.name(); }
    }
    public record Result(List<Benefit> benefits, List<Benefit> drawbacks) {
        public Result { benefits = List.copyOf(benefits); drawbacks = List.copyOf(drawbacks); }
        public boolean usable() { return !benefits.isEmpty(); }
    }

    public static Response parse(JsonObject root, Predicate<ResourceLocation> knownEffect) {
        try {
            if (root.has("tendency") == root.has("property"))
                throw new IllegalArgumentException("Exactly one tendency or property is required");
            var tendency = root.has("tendency") ? EnumBloodTendency.valueOf(root.get("tendency").getAsString()) : null;
            var property = root.has("property") ? id(root.get("property").getAsString()) : null;
            var shared = parseDrawbacks(root, knownEffect);
            var contributions = new ArrayList<Contribution>();
            for (var element : root.getAsJsonArray("benefits")) {
                var json = element.getAsJsonObject();
                var drawbacks = new ArrayList<>(shared);
                drawbacks.addAll(parseDrawbacks(json, knownEffect));
                contributions.add(new Contribution(parseBenefit(json, knownEffect, true), drawbacks));
            }
            if (contributions.size() > 16) throw new IllegalArgumentException("Too many benefits");
            var positives = new HashSet<String>();
            contributions.forEach(c -> positives.add(c.benefit().identity()));
            for (var c : contributions) for (var d : c.drawbacks())
                if (positives.contains(d.identity())) throw new IllegalArgumentException("Contradictory benefit/drawback");
            if (contributions.isEmpty() && !shared.isEmpty()) throw new IllegalArgumentException("Drawback without benefit");
            return new Response(tendency, property, contributions);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid blood response: " + exception.getMessage(), exception);
        }
    }

    private static ResourceLocation id(String text) {
        if (!text.contains(":")) throw new IllegalArgumentException("Expected namespaced ID: " + text);
        return ResourceLocation.parse(text);
    }

    private static List<Benefit> parseDrawbacks(JsonObject root, Predicate<ResourceLocation> knownEffect) {
        var result = new ArrayList<Benefit>();
        if (root.has("drawbacks")) for (var entry : root.getAsJsonArray("drawbacks"))
            result.add(parseBenefit(entry.getAsJsonObject(), knownEffect, false));
        if (result.size() > 16) throw new IllegalArgumentException("Too many drawbacks");
        return result;
    }

    private static Benefit parseBenefit(JsonObject json, Predicate<ResourceLocation> knownEffect, boolean allowReveal) {
        if (json.has("effect") == json.has("operation")) throw new IllegalArgumentException("Expected effect or operation");
        ResourceLocation effect = json.has("effect") ? id(json.get("effect").getAsString()) : null;
        if (effect != null && !knownEffect.test(effect)) throw new IllegalArgumentException("Unknown effect " + effect);
        if (effect == null && (!allowReveal || !"reveal".equals(json.get("operation").getAsString())))
            throw new IllegalArgumentException("Unsupported operation");
        int duration = integer(json, "duration", 200);
        int amplifier = integer(json, "amplifier", 0);
        double radius = json.has("radius") ? json.get("radius").getAsDouble() : 8;
        if (duration <= 0 || duration > 72000 || amplifier < 0 || amplifier > 4
                || !Double.isFinite(radius) || radius <= 0 || radius > 32 || effect == null && amplifier != 0)
            throw new IllegalArgumentException("Response values out of bounds");
        return new Benefit(effect, duration, amplifier, radius);
    }

    private static int integer(JsonObject root, String key, int fallback) {
        return root.has(key) ? root.get(key).getAsBigDecimal().intValueExact() : fallback;
    }

    /** Input is already ordered by tendency, then by property ID. Deduplicate before allocating three slots. */
    public static Result select(List<Response> ordered) {
        var merged = new LinkedHashMap<String, Benefit>();
        for (var response : ordered) for (var c : response.contributions())
            merged.merge(c.benefit().identity(), c.benefit(), BloodInjectionRules::merge);
        var chosen = merged.values().stream().limit(3).toList();
        var identities = new HashSet<String>();
        chosen.forEach(b -> identities.add(b.identity()));
        var drawbacks = new LinkedHashMap<String, Benefit>();
        for (var response : ordered) for (var c : response.contributions())
            if (identities.contains(c.benefit().identity()))
                for (var d : c.drawbacks()) drawbacks.merge(d.identity(), d, BloodInjectionRules::merge);
        return new Result(chosen, new ArrayList<>(drawbacks.values()));
    }

    private static Benefit merge(Benefit a, Benefit b) {
        return new Benefit(a.effect(), Math.max(a.duration(), b.duration()),
                Math.max(a.amplifier(), b.amplifier()), Math.max(a.radius(), b.radius()));
    }
}

