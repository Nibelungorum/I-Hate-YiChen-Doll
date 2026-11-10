package github.ihatechpack.yichendoll.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.client.PortalVisualState;
import github.ihatechpack.yichendoll.common.doll.DollEffectProtection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerEffectsMixin implements PortalVisualState.Access {
    @Unique private final PortalVisualState yichendoll$portalVisuals = new PortalVisualState();

    @Override
    public PortalVisualState yichendoll$portalVisualState() { return yichendoll$portalVisuals; }

    @Inject(method = "handleConfusionTransitionEffect", at = @At("HEAD"))
    private void yichendoll$trackPortal(boolean useConfusion, CallbackInfo ci) {
        var player = (LocalPlayer) (Object) this;
        yichendoll$portalVisuals.tick(useConfusion && player.portalProcess != null
                && player.portalProcess.isInsidePortalThisTick());
    }

    @WrapOperation(method = {"aiStep", "canStartSprinting"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
    private boolean yichendoll$allowSprint(LocalPlayer player, Holder<MobEffect> effect, Operation<Boolean> original) {
        return original.call(player, effect) && !DollEffectProtection.ignores(player, effect);
    }
}
