package github.ihatechpack.yichendoll.mixin.common;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Raises only max health's declared ceiling, preserving its base, minimum and vanilla clamping. */
@Mixin(Attributes.class)
public abstract class MaxHealthAttributeMixin {
    @ModifyArgs(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V"))
    private static void yichendoll$healthCeiling(Args args) {
        if ("attribute.name.generic.max_health".equals(args.get(0))) {
            double existingMax = args.get(3);
            args.set(3, Math.max(existingMax, Integer.MAX_VALUE));
        }
    }
}
