package github.ihatechpack.yichendoll.common.doll.behavior;

import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.common.doll.DollRuntime;
import net.minecraft.world.entity.Entity;

/** M_XM_ is a passive combat behavior; AE2LT hooks consume the shared equipment state. */
public final class MXmDollBehavior implements DollBehavior {
    public static final String ID = "m_xm_";
    public static final MXmDollBehavior INSTANCE = new MXmDollBehavior();

    private MXmDollBehavior() {}

    public static boolean isWearing(Entity entity) {
        return DollRuntime.isWearing(entity, ID);
    }
}
