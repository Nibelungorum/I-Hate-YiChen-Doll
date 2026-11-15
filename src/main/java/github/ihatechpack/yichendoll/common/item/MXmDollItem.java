package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.registry.ModDataComponents;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** M_XM_ doll with a persistent, per-stack cosmic wing visibility toggle. */
public final class MXmDollItem extends MeNetworkDollItem {
    public MXmDollItem(Block block, LevelInfo.Level level) {
        super(block, level);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        boolean visible = stack.getOrDefault(ModDataComponents.COSMIC_WINGS_VISIBLE.get(), true);
        Component state = Component.translatable("message.ihateyichendoll.cosmic_wings."
                + (visible ? "enabled" : "disabled"))
                .withStyle(visible ? ChatFormatting.GREEN : ChatFormatting.RED);
        lines.add(Component.translatable("message.ihateyichendoll.cosmic_wings.render").append(state));

        String dyeName = stack.get(ModDataComponents.COSMIC_WINGS_DYE.get());
        DyeColor dye = dyeName == null ? null : DyeColor.byName(dyeName, null);
        Component color = dye == null
                ? Component.translatable("tooltip.ihateyichendoll.cosmic_wings.undyed")
                : Component.translatable("color.minecraft." + dye.getName())
                        .withStyle(Style.EMPTY.withColor(dye.getTextColor()));
        lines.add(Component.translatable("tooltip.ihateyichendoll.cosmic_wings.theme").append(color));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean visible = !stack.getOrDefault(ModDataComponents.COSMIC_WINGS_VISIBLE.get(), true);
            stack.set(ModDataComponents.COSMIC_WINGS_VISIBLE.get(), visible);
            Component state = Component.translatable("message.ihateyichendoll.cosmic_wings."
                    + (visible ? "enabled" : "disabled"))
                    .withStyle(visible ? ChatFormatting.GREEN : ChatFormatting.RED);
            player.displayClientMessage(Component.translatable(
                    "message.ihateyichendoll.cosmic_wings.render").append(state), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
