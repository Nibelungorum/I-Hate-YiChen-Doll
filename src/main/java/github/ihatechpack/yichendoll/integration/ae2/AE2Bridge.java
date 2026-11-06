package github.ihatechpack.yichendoll.integration.ae2;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Never links AE2 classes in a common item, tooltip or event signature. */
public interface AE2Bridge {
    AE2Bridge UNAVAILABLE = new AE2Bridge() {};

    static AE2Bridge get() {
        return Holder.INSTANCE;
    }

    default void initialize() {}
    default void clear() {}
    default MeNetworkStats sample(ServerPlayer player, ItemStack stack) { return MeNetworkStats.EMPTY; }

    final class Holder {
        private static final AE2Bridge INSTANCE = load();

        private static AE2Bridge load() {
            if (!ModList.get().isLoaded("ae2")) return UNAVAILABLE;
            try {
                return (AE2Bridge) Class.forName(
                        "github.ihatechpack.yichendoll.integration.ae2.loaded.LoadedAE2Bridge")
                        .getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to load AE2 integration", exception);
            }
        }
    }
}
