package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.ihatechpack.yichendoll.common.doll.DollEnergy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/** Pays after Unbreaking decides the actual loss, before the item can break. */
@Mixin(ItemStack.class)
public abstract class ItemStackDurabilityMixin {
    @ModifyExpressionValue(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"))
    private int yichendoll$protectDurability(int damage, int requested, ServerLevel level,
                                           LivingEntity owner, Consumer<Item> onBreak) {
        return owner instanceof ServerPlayer player
                ? DollEnergy.protectDurability(player, (ItemStack) (Object) this, damage) : damage;
    }
}
