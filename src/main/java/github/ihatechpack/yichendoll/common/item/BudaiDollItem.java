package github.ihatechpack.yichendoll.common.item;

import github.ihatechpack.yichendoll.registry.ModDataComponents;
import github.ihatechpack.yichendoll.util.LevelInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Budai doll with a persistent, per-stack energy shield visibility toggle. */
public final class BudaiDollItem extends MeNetworkDollItem {
    public BudaiDollItem(Block block, LevelInfo.Level level) {
        super(block, level);
    }

    public static boolean shieldVisible(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BUDAI_SHIELD_VISIBLE.get(), true);
    }

    private static Component state(boolean visible) {
        return Component.translatable("message.ihateyichendoll.budai_shield."
                + (visible ? "enabled" : "disabled"))
                .withStyle(visible ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("message.ihateyichendoll.budai_shield.render")
                .append(state(shieldVisible(stack))));
        lines.add(Component.translatable("tooltip.ihateyichendoll.budai_shield.toggle")
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean visible = shieldVisible(stack);
            stack.set(ModDataComponents.BUDAI_SHIELD_VISIBLE.get(), !visible);
            player.displayClientMessage(Component.translatable(
                    "message.ihateyichendoll.budai_shield.render").append(state(!visible)), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
