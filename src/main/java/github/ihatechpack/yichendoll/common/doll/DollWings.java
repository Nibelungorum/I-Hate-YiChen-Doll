package github.ihatechpack.yichendoll.common.doll;

import github.ihatechpack.yichendoll.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/** Shared persistent visibility and dye behavior for dolls that provide wings. */
public final class DollWings {
    private DollWings() {}

    public static boolean isVisible(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.COSMIC_WINGS_VISIBLE.get(), true);
    }

    public static int themeColor(ItemStack stack) {
        String dyeName = stack.get(ModDataComponents.COSMIC_WINGS_DYE.get());
        DyeColor dye = dyeName == null ? null : DyeColor.byName(dyeName, null);
        return dye == null ? 0xFFFFFF : dye.getTextColor();
    }

    public static InteractionResultHolder<ItemStack> toggle(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean visible = !isVisible(stack);
            stack.set(ModDataComponents.COSMIC_WINGS_VISIBLE.get(), visible);
            Component state = Component.translatable("message.ihateyichendoll.cosmic_wings."
                    + (visible ? "enabled" : "disabled"))
                    .withStyle(visible ? ChatFormatting.GREEN : ChatFormatting.RED);
            player.displayClientMessage(Component.translatable(
                    "message.ihateyichendoll.cosmic_wings.render").append(state), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void appendHoverText(ItemStack stack, List<Component> lines) {
        boolean visible = isVisible(stack);
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
}
