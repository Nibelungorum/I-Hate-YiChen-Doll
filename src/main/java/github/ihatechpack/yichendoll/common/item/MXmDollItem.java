package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.common.doll.DollWings;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** M_XM_ doll with persistent, per-stack cosmic wing visibility and dye data. */
public final class MXmDollItem extends MeNetworkDollItem {
    public MXmDollItem(Block block, LevelInfo.Level level) {
        super(block, level);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        DollWings.appendHoverText(stack, lines);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return DollWings.toggle(level, player, hand);
    }
}
