package github.ihatechpack.yichendoll.common.doll.behavior;

import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.api.doll.DollContext;
import github.ihatechpack.yichendoll.common.doll.DollEnergy;
import github.ihatechpack.yichendoll.common.doll.DollFlight;

/** Creative flight costs 5 FE per active tick, exactly 100 FE per second. */
public final class HowxuDollBehavior implements DollBehavior {
    public static final HowxuDollBehavior INSTANCE = new HowxuDollBehavior();

    private HowxuDollBehavior() {}

    @Override
    public void onEquip(DollContext context) {
        DollFlight.updatePermission(context.player(), DollEnergy.available(context.stacks()) >= 5);
    }

    @Override
    public void onUnequip(DollContext context) {
        DollFlight.forget(context.player());
        DollFlight.updatePermission(context.player(), false);
    }

    @Override
    public void tick(DollContext context) {
        var player = context.player();
        if (player.getAbilities().flying) {
            DollEnergy.consume(context.stacks(), 5);
        }
        DollFlight.updatePermission(player, DollEnergy.available(context.stacks()) >= 5);
    }
}
