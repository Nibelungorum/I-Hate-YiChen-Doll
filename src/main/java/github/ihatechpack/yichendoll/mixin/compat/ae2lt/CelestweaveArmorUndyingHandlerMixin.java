package github.ihatechpack.yichendoll.mixin.compat.ae2lt;

import github.ihatechpack.yichendoll.integration.ae2lt.CelestweaveBypass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.moakiee.ae2lt.celestweave.CelestweaveArmorUndyingHandler", remap = false)
public abstract class CelestweaveArmorUndyingHandlerMixin {
    @Inject(method = "onIncomingFatalDamage", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipIncomingUndying(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (CelestweaveBypass.isPiercing(event.getEntity(), event.getSource())) ci.cancel();
    }

    @Inject(method = "onFatalDamage", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipFatalUndying(LivingDamageEvent.Pre event, CallbackInfo ci) {
        if (CelestweaveBypass.isPiercing(event.getEntity(), event.getSource())) ci.cancel();
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipDeathProtection(LivingDeathEvent event, CallbackInfo ci) {
        if (CelestweaveBypass.isPiercing(event.getEntity(), event.getSource())
                || CelestweaveBypass.shouldBypass(event.getEntity())) ci.cancel();
    }

    @Inject(method = {"tryProtectForcedDeath", "protectBeforeDeathSideEffect"}, at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipForcedDeathProtection(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (CelestweaveBypass.shouldBypass(player)) cir.setReturnValue(false);
    }

    @Inject(method = "wasProtectedThisTick", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$ignorePreviousProtection(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (CelestweaveBypass.shouldBypass(entity)) cir.setReturnValue(false);
    }

    @Inject(method = "tryTrigger", at = @At("HEAD"), cancellable = true)
    private static void yichendoll$skipUndyingTrigger(ServerPlayer player, long now, DamageContainer damage,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (CelestweaveBypass.shouldBypass(player)) cir.setReturnValue(false);
    }
}
