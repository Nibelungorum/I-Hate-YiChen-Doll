package github.ihatechpack.yichendoll.integration.curios.client.loaded;

import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import github.ihatechpack.yichendoll.client.DollElytraLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only renderer registration, selected only when Curios is installed. */
public final class LoadedCuriosClientBridge implements CuriosClientBridge {
    @Override
    public boolean hasHeadDoll(LivingEntity entity) {
        return !DollItemRenderer.headDoll(entity).isEmpty();
    }

    @Override
    public void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        if (event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer renderer) {
            renderer.addLayer(new DollItemRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
            renderer.addLayer(new DollElytraLayer(renderer, event.getEntityModels()));
        }
        if (event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer renderer) {
            renderer.addLayer(new DollItemRenderer<>(renderer, event.getContext().getItemInHandRenderer()));
            renderer.addLayer(new DollElytraLayer(renderer, event.getEntityModels()));
        }
    }
}
