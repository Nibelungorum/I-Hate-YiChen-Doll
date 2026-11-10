package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Both vanilla potion-fog implementations inherit this package-private interface's default method. */
@Mixin(targets = "net.minecraft.client.renderer.FogRenderer$MobEffectFogFunction")
public interface MobEffectFogMixin {
    @WrapOperation(method = "isEnabled", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean yichendoll$ignorePotionFog(LivingEntity entity, Holder<MobEffect> effect,
            Operation<Boolean> original) {
        return original.call(entity, effect) && !DollEffectProtection.ignores(entity, effect);
    }
}
