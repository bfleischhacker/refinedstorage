package com.raoulvdberge.refinedstorage.integration.jei;

import java.util.*;
import java.util.function.BiPredicate;

/** Plans one crafting-matrix fill, just as the server transfers one item per slot. */
final class MissingIngredientPlanner<T> {
    static final class Supply<T> {
        final T value;
        final int count;
        Supply(T value, int count) { this.value = value; this.count = Math.max(0, count); }
    }

    final List<Integer> missing = new ArrayList<>();
    final List<Integer> craftableSlots = new ArrayList<>();
    final Map<Integer, Integer> requests = new LinkedHashMap<>();

    MissingIngredientPlanner(List<List<T>> slots, List<Supply<T>> supplies,
                             List<T> patterns, BiPredicate<T, T> matches) {
        int[] assigned = new int[slots.size()];
        Arrays.fill(assigned, -1);
        int[] patternForSlot = new int[slots.size()];
        Arrays.fill(patternForSlot, -1);
        for (int slot = 0; slot < slots.size(); slot++) {
            for (T alternative : slots.get(slot)) {
                for (int i = 0; i < patterns.size(); i++) {
                    if (matches.test(alternative, patterns.get(i))) { patternForSlot[slot] = i; break; }
                }
                if (patternForSlot[slot] != -1) { break; }
            }
        }
        // Prefer stocking slots without patterns, then augment earlier choices when substitutes overlap.
        // A stack is a capacity, not a boolean; no unit can satisfy two recipe slots.
        for (int pass = 0; pass < 2; pass++) {
            for (int slot = 0; slot < slots.size(); slot++) {
                if ((patternForSlot[slot] == -1) == (pass == 0) && !slots.get(slot).isEmpty()) {
                    allocate(slot, slots, supplies, matches, assigned, new boolean[slots.size()]);
                }
            }
        }
        for (int slot = 0; slot < slots.size(); slot++) {
            List<T> alternatives = slots.get(slot);
            if (alternatives.isEmpty()) { continue; }
            if (assigned[slot] != -1) { continue; }
            int pattern = patternForSlot[slot];
            if (pattern == -1) {
                missing.add(slot + 1);
            } else {
                craftableSlots.add(slot + 1);
                requests.merge(pattern, 1, Integer::sum);
            }
        }
    }

    private boolean allocate(int slot, List<List<T>> slots, List<Supply<T>> supplies,
                             BiPredicate<T, T> matches, int[] assigned, boolean[] visited) {
        if (visited[slot]) { return false; }
        visited[slot] = true;
        for (T alternative : slots.get(slot)) {
            for (int supply = 0; supply < supplies.size(); supply++) {
                if (supplies.get(supply).count == 0 || !matches.test(alternative, supplies.get(supply).value)) { continue; }
                int used = 0;
                for (int allocation : assigned) { if (allocation == supply) { used++; } }
                if (used < supplies.get(supply).count) { assigned[slot] = supply; return true; }
                for (int previous = 0; previous < assigned.length; previous++) {
                    if (assigned[previous] == supply &&
                        allocate(previous, slots, supplies, matches, assigned, visited)) {
                        assigned[slot] = supply;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
