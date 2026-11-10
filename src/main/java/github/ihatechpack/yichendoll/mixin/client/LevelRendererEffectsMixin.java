package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import github.ihatechpack.yichendoll.client.DollVisuals;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererEffectsMixin {
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int yichendoll$outlineColor(Entity entity, Operation<Integer> original,
            @Local(argsOnly = true) DeltaTracker tracker) {
        if (entity instanceof LivingEntity living && DollEffectProtection.isWearing(living, "suifengyao")) {
            return DollVisuals.rainbow(entity.level().getGameTime(), tracker.getGameTimeDeltaPartialTick(false));
        }
        return original.call(entity);
    }

    @WrapOperation(method = "doesMobEffectBlockSky", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean yichendoll$visibleSky(LivingEntity entity, Holder<MobEffect> effect, Operation<Boolean> original) {
        return original.call(entity, effect) && !DollEffectProtection.ignores(entity, effect);
    }
}
