package github.ihatechpack.yichendoll.client;

import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.player.LocalPlayer;

public final class DollVisuals {
    private DollVisuals() {}

    public static float spinning(LocalPlayer player, boolean previous, float original) {
        if (!DollEffectProtection.isWearing(player, "yuanxi_19_new")) return original;
        var state = ((PortalVisualState.Access) player).yichendoll$portalVisualState();
        return previous ? state.previous() : state.current();
    }

    public static int rainbow(long ticks, float partialTick) {
        float phase = ((ticks % 200) + partialTick) * 6F / 200F;
        int sector = (int) phase;
        int up = Math.round((phase - sector) * 255);
        int down = 255 - up;
        return switch (sector % 6) {
            case 0 -> 0xFF0000 | up << 8;
            case 1 -> down << 16 | 0x00FF00;
            case 2 -> 0x00FF00 | up;
            case 3 -> down << 8 | 0x0000FF;
            case 4 -> up << 16 | 0x0000FF;
            default -> 0xFF0000 | down;
        };
    }
}
