package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.client.DollVisuals;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiEffectsMixin {
    @WrapOperation(method = "renderCameraOverlays", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/player/LocalPlayer;oSpinningEffectIntensity:F"))
    private float yichendoll$previousPortal(LocalPlayer player, Operation<Float> original) {
        return DollVisuals.spinning(player, true, original.call(player));
    }

    @WrapOperation(method = "renderCameraOverlays", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/player/LocalPlayer;spinningEffectIntensity:F"))
    private float yichendoll$currentPortal(LocalPlayer player, Operation<Float> original) {
        return DollVisuals.spinning(player, false, original.call(player));
    }

    @WrapOperation(method = "renderCameraOverlays", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean yichendoll$normalPortalOverlay(LocalPlayer player, Holder<MobEffect> effect,
            Operation<Boolean> original) {
        return original.call(player, effect) && !DollEffectProtection.ignores(player, effect);
    }
}
