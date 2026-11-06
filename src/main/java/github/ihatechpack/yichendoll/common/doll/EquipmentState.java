package github.ihatechpack.yichendoll.common.doll;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Per-player equipment transitions and reconciliation timing, independent of Curios. */
final class EquipmentState<T> {
    private static final int CHECK_INTERVAL = 40;
    private Map<String, T> snapshot = Map.of();
    private long lastRefresh;
    private boolean initialized;

    boolean needsRefresh(long tick) {
        return !initialized || tick < lastRefresh || tick - lastRefresh >= CHECK_INTERVAL;
    }

    Map<String, T> snapshot() {
        return snapshot;
    }

    void replace(Map<String, T> equipment, long tick, Consumer<T> onEquip, Consumer<T> onUnequip) {
        Map<String, T> previous = snapshot;
        Map<String, T> next = Collections.unmodifiableMap(new LinkedHashMap<>(equipment));
        snapshot = next;
        lastRefresh = tick;
        initialized = true;
        previous.forEach((id, value) -> {
            if (!next.containsKey(id)) onUnequip.accept(value);
        });
        next.forEach((id, value) -> {
            if (!previous.containsKey(id)) onEquip.accept(value);
        });
    }

    void clear() {
        snapshot = Map.of();
        initialized = false;
    }
}
