package github.ihatechpack.yichendoll.integration.curios;

import net.neoforged.fml.ModList;

/** Selects the optional implementation without linking any Curios classes when absent. */
final class CuriosBridgeBootstrap {
    private static final CuriosBridge UNAVAILABLE = new CuriosBridge() {};
    private static CuriosBridge bridge;

    private CuriosBridgeBootstrap() {}

    static synchronized CuriosBridge get() {
        if (bridge == null) {
            if (!ModList.get().isLoaded("curios")) {
                bridge = UNAVAILABLE;
            } else {
                try {
                    bridge = (CuriosBridge) Class.forName(
                            "github.ihatechpack.yichendoll.integration.curios.loaded.LoadedCuriosBridge")
                            .getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Unable to load Curios integration", exception);
                }
            }
        }
        return bridge;
    }
}
