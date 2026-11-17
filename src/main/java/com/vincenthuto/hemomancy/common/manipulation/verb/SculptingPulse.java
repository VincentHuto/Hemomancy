package com.vincenthuto.hemomancy.common.manipulation.verb;

/**
 * One paid pulse of a Sculpting. {@code paid} is the blood that left the player; {@code workScale} is how much work the
 * tool does per pulse (1 for a bare hand; the Living Staff's rate skills raise it without raising the price);
 * {@code index} counts pulses from 0 in this session.
 */
public record SculptingPulse(Verb verb, double paid, double workScale, int index) {
}
