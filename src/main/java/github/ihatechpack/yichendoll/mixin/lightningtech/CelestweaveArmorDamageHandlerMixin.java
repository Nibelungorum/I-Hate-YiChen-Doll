package github.ihatechpack.yichendoll.mixin.lightningtech;

import github.ihatechpack.yichendoll.integration.lightningtech.CelestweaveBypass;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.moakiee.ae2lt.celestweave.CelestweaveArmorDamageHandler", remap = false)
public abstract class CelestweaveArmorDamageHandlerMixin {
    @Inject(method = {"onDamageTypeImmunity", "onShieldIncoming"}, at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipIncomingProtection(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (CelestweaveBypass.isPiercing(event.getEntity(), event.getSource())) ci.cancel();
    }

    @Inject(method = "onPre", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipReflect(LivingDamageEvent.Pre event, CallbackInfo ci) {
        if (CelestweaveBypass.isPiercing(event.getEntity(), event.getSource())) ci.cancel();
    }
}
