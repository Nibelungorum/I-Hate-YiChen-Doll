package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.registry.ModDataComponents;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Linkable through AE2's native wireless AP slot without inheriting AE2's powered-item hierarchy. */
public final class MeNetworkDollItem extends ItemDoll {
    public MeNetworkDollItem(Block block, LevelInfo.Level level) {
        super(block, level);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        boolean linked = stack.has(ModDataComponents.ME_LINK.get());
        lines.add(Component.translatable(linked
                ? "tooltip.ihateyichendoll.me_linked" : "tooltip.ihateyichendoll.me_unlinked")
                .withStyle(linked ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}
