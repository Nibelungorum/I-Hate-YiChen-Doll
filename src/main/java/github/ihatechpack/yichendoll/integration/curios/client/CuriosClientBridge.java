package github.ihatechpack.yichendoll.integration.curios.client;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only safe boundary. No Curios renderer class is linked unless the mod is present. */
public interface CuriosClientBridge {
    CuriosClientBridge UNAVAILABLE = event -> {};

    static CuriosClientBridge get() {
        return Holder.INSTANCE;
    }

    void addEntityLayers(EntityRenderersEvent.AddLayers event);

    default boolean hasHeadDoll(LivingEntity entity) {
        return false;
    }

    final class Holder {
        private static final CuriosClientBridge INSTANCE = load();

        private static CuriosClientBridge load() {
            if (!ModList.get().isLoaded("curios")) return UNAVAILABLE;
            try {
                return (CuriosClientBridge) Class.forName(
                        "github.ihatechpack.yichendoll.integration.curios.client.loaded.LoadedCuriosClientBridge")
                        .getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to load Curios client integration", exception);
            }
        }
    }
}
