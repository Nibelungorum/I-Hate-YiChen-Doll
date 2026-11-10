package github.ihatechpack.yichendoll.mixin.common;

import github.ihatechpack.yichendoll.common.doll.behavior.PotionDollBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Entity writes NeoForgeData before player-specific effect data, so reconcile at save entry. */
@Mixin(Entity.class)
public abstract class PotionOwnershipSaveMixin {
    @Inject(method = "saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"))
    private void yichendoll$beforePlayerSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> callback) {
        if ((Object) this instanceof ServerPlayer player) PotionDollBehavior.beforeSave(player);
    }
}
