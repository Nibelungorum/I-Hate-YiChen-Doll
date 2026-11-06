package github.ihatechpack.yichendoll.integration.curios.client.loaded;

import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only renderer registration, selected only when Curios is installed. */
public final class LoadedCuriosClientBridge implements CuriosClientBridge {
    @Override
    public void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer renderer) {
            renderer.addLayer(new DollItemRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
        }
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer renderer) {
            renderer.addLayer(new DollItemRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
        }
    }
}
