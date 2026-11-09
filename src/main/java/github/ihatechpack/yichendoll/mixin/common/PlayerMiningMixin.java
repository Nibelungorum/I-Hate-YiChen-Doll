package github.ihatechpack.yichendoll.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.ihatechpack.yichendoll.common.doll.DollRuntime;
import github.ihatechpack.yichendoll.integration.curios.CuriosBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Skip only vanilla's airborne mining penalty while flying with howxu. */
@Mixin(Player.class)
public abstract class PlayerMiningMixin {
    @ModifyExpressionValue(method = "getDigSpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean yichendoll$miningOnGround(boolean original) {
        var player = (Player) (Object) this;
        if (original || !player.getAbilities().flying) return original;
        return player instanceof ServerPlayer
                ? DollRuntime.isWearing(player, "howxu")
                : !CuriosBridge.get().activeStacks(player, "howxu").isEmpty();
    }
}
