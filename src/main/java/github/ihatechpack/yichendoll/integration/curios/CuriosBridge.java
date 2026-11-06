package github.ihatechpack.yichendoll.integration.curios;

import github.ihatechpack.yichendoll.api.doll.EquippedDoll;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Map;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Common-side boundary: no Curios API or client classes may appear in this interface. */
public interface CuriosBridge {
    static CuriosBridge get() {
        return CuriosBridgeBootstrap.get();
    }

    default void commonSetup() {}

    default void registerCapabilities(RegisterCapabilitiesEvent event) {}

    /** Client prediction only; the server uses DollRuntime's cached slot membership. */
    default List<ItemStack> activeStacks(LivingEntity entity, String dollId) {
        return List.of();
    }

    default Map<String, EquippedDoll> equipment(ServerPlayer player) {
        return Map.of();
    }
}
