package github.ihatechpack.yichendoll.client;

/** Portal-only intensity, tracked even when unequipped; vanilla keeps its nausea state. */
public final class PortalVisualState {
    private float previous;
    private float current;

    public void tick(boolean insidePortal) {
        previous = current;
        current = Math.max(0, Math.min(1, current + (insidePortal ? 0.0125F : -0.05F)));
    }

    public float current() { return current; }
    public float previous() { return previous; }

    public interface Access {
        PortalVisualState yichendoll$portalVisualState();
    }
}
