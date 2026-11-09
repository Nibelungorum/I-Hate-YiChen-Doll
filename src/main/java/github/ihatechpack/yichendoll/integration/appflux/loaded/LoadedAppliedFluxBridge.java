package github.ihatechpack.yichendoll.integration.appflux.loaded;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import github.ihatechpack.yichendoll.integration.appflux.AppliedFluxBridge;
import net.minecraft.server.level.ServerPlayer;

/** Reads the FE key only, not EU, AE power or the capacity of empty cells. */
public final class LoadedAppliedFluxBridge implements AppliedFluxBridge {
    @Override
    public boolean consumeFE(Object nativeStorage, ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        var storage = (MEStorage) nativeStorage;
        var key = FluxKey.of(EnergyType.FE);
        var source = IActionSource.ofPlayer(player);
        if (storage.extract(key, amount, Actionable.SIMULATE, source) < amount) return false;
        return storage.extract(key, amount, Actionable.MODULATE, source) == amount;
    }

    @Override
    public long storedFE(Object nativeKeyCounter) {
        return Math.max(0, ((KeyCounter) nativeKeyCounter).get(FluxKey.of(EnergyType.FE)));
    }
}
