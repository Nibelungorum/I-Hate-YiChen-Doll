package github.ihatechpack.yichendoll.mixin.lightningtech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.common.MXmDollManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.moakiee.ae2lt.logic.railgun.RailgunFireService", remap = false)
public abstract class RailgunFireServiceMixin {
    @WrapOperation(
            method = "applyAll(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/List;Lcom/moakiee/ae2lt/logic/railgun/DamageContext;Lnet/minecraft/world/item/ItemStack;Lcom/moakiee/ae2lt/item/railgun/RailgunChargeTier;Lcom/moakiee/ae2lt/logic/railgun/RailgunFireService$ExecutionScope;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private static boolean yichendoll$ignoreRailgunDamage(LivingEntity target, DamageSource source, float amount, Operation<Boolean> original) {
        return !MXmDollManager.isWearing(target) && original.call(target, source, amount);
    }
}
