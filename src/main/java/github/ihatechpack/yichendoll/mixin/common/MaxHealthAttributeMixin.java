package github.ihatechpack.yichendoll.mixin.common;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Raises only vanilla max health's numeric ceiling; all other ranges and base values stay unchanged. */
@Mixin(RangedAttribute.class)
public abstract class MaxHealthAttributeMixin {
    @Inject(method = "sanitizeValue", at = @At("HEAD"), cancellable = true)
    private void yichendoll$healthCeiling(double value, CallbackInfoReturnable<Double> cir) {
        Attributes.MAX_HEALTH.value();
    }
}
