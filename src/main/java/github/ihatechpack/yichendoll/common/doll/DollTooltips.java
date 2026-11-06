package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.common.block.BlockDoll;
import github.ihatechpack.yichendoll.common.item.ItemDoll;
import github.ihatechpack.yichendoll.common.item.MeNetworkDollItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BooleanSupplier;

/** Common tooltip content with a client-installed input supplier; no client classes on the server. */
public final class DollTooltips {
    private static BooleanSupplier shiftDown = () -> false;

    private DollTooltips() {}

    public static void setShiftDown(BooleanSupplier supplier) {
        shiftDown = supplier;
    }

    public static void append(ItemStack stack, List<Component> lines) {
        if (!(stack.getItem() instanceof ItemDoll item)) return;
        if (!shiftDown.getAsBoolean()) {
            lines.add(Component.translatable("tooltip.ihateyichendoll.hold_shift",
                    Component.literal("Shift").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
            return;
        }
        String id = ((BlockDoll) item.getBlock()).get_doll().id();
        lines.add(Component.translatable("tooltip.ihateyichendoll.effect." + id).withStyle(ChatFormatting.DARK_GREEN));
    }
}
