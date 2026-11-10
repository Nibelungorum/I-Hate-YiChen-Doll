package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Bypass blindness's critical-hit restriction without hiding the actual effect. */
@Mixin(Player.class)
public abstract class PlayerEffectProtectionMixin {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean yichendoll$allowCritical(Player player, Holder<MobEffect> effect, Operation<Boolean> original) {
        return original.call(player, effect) && !DollEffectProtection.ignores(player, effect);
    }
}
