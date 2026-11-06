package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.integration.curios.core.CuriosExtension;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 14:31
 */
@Mod(value = IHateYiChenDoll.MOD_ID,dist = Dist.CLIENT)
@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID,value = Dist.CLIENT) // 分离注册
public class IHateYiChenDollClient {

    @SubscribeEvent
    public static void onCuriosRenderRegistry(EntityRenderersEvent.AddLayers event){
        CuriosExtension.addEntityLayers(event);
    }

}
