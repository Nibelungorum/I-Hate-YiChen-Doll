package github.ihatechpack.yichendoll.common.doll.behavior;

import github.ihatechpack.yichendoll.api.doll.DollBehavior;
import github.ihatechpack.yichendoll.common.doll.DollRuntime;
import net.minecraft.world.entity.Entity;

/** M_XM_FREE owns the AE2LT combat bypass; the linked M_XM_ has separate ME bonuses. */
public final class MXmFreeDollBehavior implements DollBehavior {
    public static final String ID = "m_xm_free";
    public static final MXmFreeDollBehavior INSTANCE = new MXmFreeDollBehavior();

    private MXmFreeDollBehavior() {}

    public static boolean isWearing(Entity entity) {
        return DollRuntime.isWearing(entity, ID);
    }
}
