package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import github.ihatechpack.yichendoll.common.doll.behavior.PotionDollBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Let normal equipment refresh re-grant owned effects instead of making unowned clone copies. */
@Mixin(ServerPlayer.class)
public abstract class PotionEffectCloneMixin {
    @WrapOperation(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private static boolean yichendoll$copyUnownedEffect(ServerPlayer destination, MobEffectInstance copied,
            Operation<Boolean> original, @Local(argsOnly = true) ServerPlayer source,
            @Local(ordinal = 0) MobEffectInstance sourceEffect) {
        return !PotionDollBehavior.ownsEffect(source, sourceEffect) && original.call(destination, copied);
    }
}
