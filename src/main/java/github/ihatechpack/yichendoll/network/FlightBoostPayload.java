package github.ihatechpack.yichendoll.network;

import github.ihatechpack.yichendoll.Res;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A request only: the client supplies no energy amount, speed or claimed equipment. */
public record FlightBoostPayload(boolean boosting) implements CustomPacketPayload {
    public static final Type<FlightBoostPayload> TYPE = new Type<>(Res.rl("flight_boost"));
    public static final StreamCodec<ByteBuf, FlightBoostPayload> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(FlightBoostPayload::new, FlightBoostPayload::boosting);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlightBoostPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) DollFlight.setBoosting(player, payload.boosting());
        });
    }
}
