package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.ihatechpack.yichendoll.common.doll.DollFlight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Flight-only hook; no damage context or AE2LT death state is attached to LivingEntity. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFlightMixin {
    @ModifyExpressionValue(method = "updateFallFlying", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean yichendoll$canGlide(boolean original) {
        return original || DollFlight.canGlide((LivingEntity) (Object) this);
    }

    @WrapOperation(method = "updateFallFlying", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;elytraFlightTick(Lnet/minecraft/world/entity/LivingEntity;I)Z"))
    private boolean yichendoll$glideTick(ItemStack stack, LivingEntity entity, int ticks, Operation<Boolean> original) {
        return DollFlight.canGlide(entity) ? DollFlight.glideTick(entity) : original.call(stack, entity, ticks);
    }
}
