package github.ihatechpack.yichendoll.integration.appflux.loaded;

import appeng.api.stacks.KeyCounter;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import github.ihatechpack.yichendoll.integration.appflux.AppliedFluxBridge;

/** Reads the FE key only, not EU, AE power or the capacity of empty cells. */
public final class LoadedAppliedFluxBridge implements AppliedFluxBridge {
    @Override
    public long storedFE(Object nativeKeyCounter) {
        return Math.max(0, ((KeyCounter) nativeKeyCounter).get(FluxKey.of(EnergyType.FE)));
    }
}
