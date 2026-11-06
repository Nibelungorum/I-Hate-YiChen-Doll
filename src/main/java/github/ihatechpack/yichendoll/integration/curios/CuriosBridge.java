package github.ihatechpack.yichendoll.integration.curios;

import github.ihatechpack.yichendoll.api.doll.EquippedDoll;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Map;

/** Common-side boundary: no Curios API or client classes may appear in this interface. */
public interface CuriosBridge {
    static CuriosBridge get() {
        return CuriosBridgeBootstrap.get();
    }

    default void commonSetup() {}

    default void registerCapabilities(RegisterCapabilitiesEvent event) {}

    default Map<String, EquippedDoll> equipment(ServerPlayer player) {
        return Map.of();
    }
}
