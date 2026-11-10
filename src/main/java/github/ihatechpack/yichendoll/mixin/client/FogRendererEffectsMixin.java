package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererEffectsMixin {
    @WrapOperation(method = "setupColor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private static boolean yichendoll$nightVisionUnaffected(LivingEntity entity, Holder<MobEffect> effect,
            Operation<Boolean> original) {
        return original.call(entity, effect) && !DollEffectProtection.ignores(entity, effect);
    }
}
