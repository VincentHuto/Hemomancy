# Phase 4 Tendon Line Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `tendon_line`, a reusable anchored climbing line for controlled vertical/traversal movement.

**Architecture:** Use stack-local `CUSTOM_DATA` for anchor storage and a pure rules class for movement constants/math. The item starts a normal held-use action and applies server-side motion toward the stored anchor during `onUseTick`.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft item use/tick APIs, JSON resources, PNG item texture, plain Java sentinel tests.

---

## Tasks

- [ ] Add RED tests for `TendonLineRules` and Phase 4 resources, then verify `testClasses` fails before production code exists.
- [ ] Implement `TendonLineRules`.
- [ ] Implement `TendonLineItem` with stack-stored anchor data, anchor clear/set/use behavior, and controlled pull ticking.
- [ ] Register `tendon_line` in `ItemInit`.
- [ ] Add model, texture, recipe, lang, and reference docs.
- [ ] Run targeted sentinel tests and full Gradle build.
