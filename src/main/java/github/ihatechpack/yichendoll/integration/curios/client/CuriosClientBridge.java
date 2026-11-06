package github.ihatechpack.yichendoll.integration.curios.client;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Loaded only by the client entry point; keeps renderer types out of the common bridge. */
public interface CuriosClientBridge {
    CuriosClientBridge UNAVAILABLE = event -> {};

    static CuriosClientBridge get() {
        if (!ModList.get().isLoaded("curios")) return UNAVAILABLE;
        try {
            return (CuriosClientBridge) Class.forName(
                    "github.ihatechpack.yichendoll.integration.curios.client.loaded.LoadedCuriosClientBridge")
                    .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load Curios client integration", exception);
        }
    }

    void addEntityLayers(EntityRenderersEvent.AddLayers event);
}
