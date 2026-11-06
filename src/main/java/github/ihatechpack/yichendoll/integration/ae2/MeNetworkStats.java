package github.ihatechpack.yichendoll.integration.ae2;

/** A native-type-free snapshot shared by every doll bound to the same ME grid. */
public record MeNetworkStats(double usage, int keyTypes, int nodes, long storedFE) {
    public static final MeNetworkStats EMPTY = new MeNetworkStats(0, 0, 0, 0);
}
