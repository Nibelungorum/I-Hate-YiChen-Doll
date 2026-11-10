package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;

@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID, value = Dist.CLIENT)
public final class DollScreenEffects {
    private DollScreenEffects() {}

    @SubscribeEvent
    public static void onScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE
                && DollEffectProtection.isWearing(event.getPlayer(), "cnansz")) {
            event.setCanceled(true);
        }
    }
}
