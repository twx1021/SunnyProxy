package go;

/* loaded from: tun2socks.aar:classes.jar:go/Universe.class */
public abstract class Universe {

    /* loaded from: tun2socks.aar:classes.jar:go/Universe$proxyerror.class */
    private static final class proxyerror extends Exception implements Seq.Proxy, error {
        private final int refnum;

        proxyerror(int i) {
            this.refnum = i;
            Seq.trackGoRef(i, this);
        }

        @Override // go.error
        public native String error();

        @Override // java.lang.Throwable
        public String getMessage() {
            return error();
        }

        @Override // go.Seq.GoObject
        public final int incRefnum() {
            Seq.incGoRef(this.refnum, this);
            return this.refnum;
        }
    }

    static {
        Seq.touch();
        _init();
    }

    private Universe() {
    }

    private static native void _init();

    public static void touch() {
    }
}