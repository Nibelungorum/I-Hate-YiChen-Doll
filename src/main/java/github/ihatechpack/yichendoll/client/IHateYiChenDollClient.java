package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.integration.curios.client.CuriosClientBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * @description: TODO
 * @author: HowXu
 * @date: 2026/1/11 14:31
 */
@Mod(value = IHateYiChenDoll.MOD_ID,dist = Dist.CLIENT)
public class IHateYiChenDollClient {

    public IHateYiChenDollClient(IEventBus modBus) {
        modBus.addListener(CuriosClientBridge.get()::addEntityLayers);
    }

}
