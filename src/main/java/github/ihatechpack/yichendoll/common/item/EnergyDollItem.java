package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.common.doll.DollEnergy;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** A chargeable doll whose stored FE belongs to its individual ItemStack. */
public final class EnergyDollItem extends ItemDoll {
    public EnergyDollItem(Block block, LevelInfo.Level level) {
        super(block, level);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * DollEnergy.stored(stack) / DollEnergy.CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return DollEnergy.stored(stack) > 0 ? 0x00FF7F : 0x778899;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.ihateyichendoll.energy",
                DollEnergy.stored(stack), DollEnergy.CAPACITY).withStyle(ChatFormatting.GREEN));
    }
}
