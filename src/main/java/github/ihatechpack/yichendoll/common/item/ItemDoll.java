package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.IHateYiChenDoll;
import github.ihatechpack.yichendoll.common.block.BlockDoll;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import java.util.List;

public class ItemDoll extends BlockItem {

    public ItemDoll(Block block, LevelInfo.Level level) {
        super(block, new Properties().rarity(LevelInfo.getRarity(level)));
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ((BlockDoll) this.getBlock()).get_doll().appendHoverText(tooltipComponents);
    }
}
