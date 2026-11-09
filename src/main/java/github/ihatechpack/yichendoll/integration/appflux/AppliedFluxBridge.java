package github.ihatechpack.yichendoll.integration.appflux;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

/** Only the AE2 loaded adapter passes native storage and statistics through this optional boundary. */
public interface AppliedFluxBridge {
    long storedFE(Object nativeKeyCounter);

    default boolean consumeFE(Object nativeStorage, ServerPlayer player, long amount) { return false; }

    static AppliedFluxBridge get() {
        return Holder.INSTANCE;
    }

    final class Holder {
        private static final AppliedFluxBridge INSTANCE = load();

        private static AppliedFluxBridge load() {
            if (!ModList.get().isLoaded("ae2") || !ModList.get().isLoaded("appflux")) return counter -> 0;
            try {
                return (AppliedFluxBridge) Class.forName(
                        "github.ihatechpack.yichendoll.integration.appflux.loaded.LoadedAppliedFluxBridge")
                        .getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to load Applied Flux integration", exception);
            }
        }
    }
}
