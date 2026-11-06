package github.ihatechpack.yichendoll.registry;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.network.FlightBoostPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = IHateYiChenDoll.MOD_ID)
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToServer(FlightBoostPayload.TYPE, FlightBoostPayload.STREAM_CODEC, FlightBoostPayload::handle);
    }
}
