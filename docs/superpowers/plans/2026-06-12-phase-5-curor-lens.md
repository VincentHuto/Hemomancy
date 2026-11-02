# Phase 5 Curor Lens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a held-up Curor Lens with a proper first-person lens overlay and living target diagnostics.

**Architecture:** Keep gameplay data local-client and opportunistic for this phase. The item owns held-use behavior, a pure rules class owns constants/formatting, and the client overlay raycasts and renders readable diagnostics.

**Tech Stack:** Java 21, NeoForge 1.21.1 item use APIs, `RegisterGuiLayersEvent`, JSON item model, PNG textures, plain Java sentinel tests.

---

## Tasks

- [ ] Add RED tests for `CurorLensRules` and Phase 5 resources.
- [ ] Implement `CurorLensRules`, `CurorLensItem`, and `CurorLensOverlay`.
- [ ] Register `curor_lens` and GUI overlay.
- [ ] Add 3D item model, item texture, overlay texture, recipe, lang, and docs.
- [ ] Run targeted tests and full build.
