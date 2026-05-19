package go;

import android.content.Context;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.logging.Logger;

public class Seq {
    private static Logger log = Logger.getLogger("GoSeq");
    private static final int NULL_REFNUM = 41;
    public static final Ref nullRef = new Ref(NULL_REFNUM, null);
    private static final GoRefQueue goRefQueue = new GoRefQueue();
    static final RefTracker tracker = new RefTracker();

    /* loaded from: tun2socks.aar:classes.jar:go/Seq$GoObject.class */
    public interface GoObject {
        int incRefnum();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: tun2socks.aar:classes.jar:go/Seq$GoRef.class */
    public static class GoRef extends PhantomReference<GoObject> {
        final int refnum;

        GoRef(int i, GoObject goObject, GoRefQueue goRefQueue) {
            super(goObject, goRefQueue);
            if (i <= 0) {
                this.refnum = i;
                return;
            }
            throw new RuntimeException("GoRef instantiated with a Java refnum " + i);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: tun2socks.aar:classes.jar:go/Seq$GoRefQueue.class */
    public static class GoRefQueue extends ReferenceQueue<GoObject> {
        private final Collection<GoRef> refs = Collections.synchronizedCollection(new HashSet());

        GoRefQueue() {
            Thread thread = new Thread(new Runnable() { // from class: go.Seq.GoRefQueue.1
                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        try {
                            GoRef goRef = (GoRef) GoRefQueue.this.remove();
                            GoRefQueue.this.refs.remove(goRef);
                            Seq.destroyRef(goRef.refnum);
                            goRef.clear();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.setName("GoRefQueue Finalizer Thread");
            thread.start();
        }

        void track(int i, GoObject goObject) {
            this.refs.add(new GoRef(i, goObject, this));
        }
    }

    /* loaded from: tun2socks.aar:classes.jar:go/Seq$Proxy.class */
    public interface Proxy extends GoObject {
    }

    /* loaded from: tun2socks.aar:classes.jar:go/Seq$Ref.class */
    public static final class Ref {
        public final Object obj;
        private int refcnt;
        public final int refnum;

        Ref(int i, Object obj) {
            if (i >= 0) {
                this.refnum = i;
                this.refcnt = 0;
                this.obj = obj;
                return;
            }
            throw new RuntimeException("Ref instantiated with a Go refnum " + i);
        }

        static /* synthetic */ int access$110(Ref ref) {
            int i = ref.refcnt;
            ref.refcnt = i - 1;
            return i;
        }

        void inc() {
            int i = this.refcnt;
            if (i != Integer.MAX_VALUE) {
                this.refcnt = i + 1;
                return;
            }
            throw new RuntimeException("refnum " + this.refnum + " overflow");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: tun2socks.aar:classes.jar:go/Seq$RefMap.class */
    public static final class RefMap {
        private int next = 0;
        private int live = 0;
        private int[] keys = new int[16];
        private Ref[] objs = new Ref[16];

        RefMap() {
        }

        private void grow() {
            Ref[] refArr;
            int i;
            int roundPow2 = roundPow2(this.live);
            int[] iArr = this.keys;
            if (roundPow2 * 2 > iArr.length) {
                iArr = new int[iArr.length * 2];
                refArr = new Ref[this.objs.length * 2];
            } else {
                refArr = this.objs;
            }
            int i2 = 0;
            int i3 = 0;
            while (true) {
                i = i3;
                int[] iArr2 = this.keys;
                if (i2 >= iArr2.length) {
                    break;
                }
                Ref[] refArr2 = this.objs;
                int i4 = i;
                if (refArr2[i2] != null) {
                    iArr[i] = iArr2[i2];
                    refArr[i] = refArr2[i2];
                    i4 = i + 1;
                }
                i2++;
                i3 = i4;
            }
            for (int i5 = i; i5 < iArr.length; i5++) {
                iArr[i5] = 0;
                refArr[i5] = null;
            }
            this.keys = iArr;
            this.objs = refArr;
            this.next = i;
            if (this.live != this.next) {
                throw new RuntimeException("bad state: live=" + this.live + ", next=" + this.next);
            }
        }

        private static int roundPow2(int i) {
            int i2 = 1;
            while (true) {
                int i3 = i2;
                if (i3 >= i) {
                    return i3;
                }
                i2 = i3 * 2;
            }
        }

        Ref get(int i) {
            int binarySearch = Arrays.binarySearch(this.keys, 0, this.next, i);
            if (binarySearch >= 0) {
                return this.objs[binarySearch];
            }
            return null;
        }

        void put(int i, Ref ref) {
            if (ref != null) {
                int binarySearch = Arrays.binarySearch(this.keys, 0, this.next, i);
                if (binarySearch >= 0) {
                    Ref[] refArr = this.objs;
                    if (refArr[binarySearch] == null) {
                        refArr[binarySearch] = ref;
                        this.live++;
                    }
                    if (this.objs[binarySearch] != ref) {
                        throw new RuntimeException("replacing an existing ref (with key " + i + ")");
                    }
                    return;
                }
                if (this.next >= this.keys.length) {
                    grow();
                    binarySearch = Arrays.binarySearch(this.keys, 0, this.next, i);
                }
                int i2 = binarySearch ^ (-1);
                int i3 = this.next;
                if (i2 < i3) {
                    int[] iArr = this.keys;
                    int i4 = i2 + 1;
                    System.arraycopy(iArr, i2, iArr, i4, i3 - i2);
                    Ref[] refArr2 = this.objs;
                    System.arraycopy(refArr2, i2, refArr2, i4, this.next - i2);
                }
                this.keys[i2] = i;
                this.objs[i2] = ref;
                this.live++;
                this.next++;
                return;
            }
            throw new RuntimeException("put a null ref (with key " + i + ")");
        }

        void remove(int i) {
            int binarySearch = Arrays.binarySearch(this.keys, 0, this.next, i);
            if (binarySearch >= 0) {
                Ref[] refArr = this.objs;
                if (refArr[binarySearch] != null) {
                    refArr[binarySearch] = null;
                    this.live--;
                }
            }
        }
    }

    /* loaded from: tun2socks.aar:classes.jar:go/Seq$RefTracker.class */
    static final class RefTracker {
        private static final int REF_OFFSET = 42;
        private int next = REF_OFFSET;
        private final RefMap javaObjs = new RefMap();
        private final IdentityHashMap<Object, Integer> javaRefs = new IdentityHashMap<>();

        RefTracker() {
        }

        void dec(int i) {
            synchronized (this) {
                if (i <= 0) {
                    Logger logger = Seq.log;
                    logger.severe("dec request for Go object " + i);
                } else if (i != Seq.nullRef.refnum) {
                    Ref ref = this.javaObjs.get(i);
                    if (ref != null) {
                        Ref.access$110(ref);
                        if (ref.refcnt <= 0) {
                            this.javaObjs.remove(i);
                            this.javaRefs.remove(ref.obj);
                        }
                        return;
                    }
                    throw new RuntimeException("referenced Java object is not found: refnum=" + i);
                }
            }
        }

        Ref get(int i) {
            synchronized (this) {
                if (i < 0) {
                    throw new RuntimeException("ref called with Go refnum " + i);
                } else if (i == Seq.NULL_REFNUM) {
                    return Seq.nullRef;
                } else {
                    Ref ref = this.javaObjs.get(i);
                    if (ref != null) {
                        return ref;
                    }
                    throw new RuntimeException("unknown java Ref: " + i);
                }
            }
        }

        int inc(Object obj) {
            synchronized (this) {
                if (obj == null) {
                    return Seq.NULL_REFNUM;
                }
                if (obj instanceof Proxy) {
                    return ((Proxy) obj).incRefnum();
                }
                Integer num = this.javaRefs.get(obj);
                Integer num2 = num;
                if (num == null) {
                    if (this.next != Integer.MAX_VALUE) {
                        int i = this.next;
                        this.next = i + 1;
                        num2 = Integer.valueOf(i);
                        this.javaRefs.put(obj, num2);
                    } else {
                        throw new RuntimeException("createRef overflow for " + obj);
                    }
                }
                int intValue = num2.intValue();
                Ref ref = this.javaObjs.get(intValue);
                Ref ref2 = ref;
                if (ref == null) {
                    ref2 = new Ref(intValue, obj);
                    this.javaObjs.put(intValue, ref2);
                }
                ref2.inc();
                return intValue;
            }
        }

        void incRefnum(int i) {
            synchronized (this) {
                Ref ref = this.javaObjs.get(i);
                if (ref != null) {
                    ref.inc();
                } else {
                    throw new RuntimeException("referenced Java object is not found: refnum=" + i);
                }
            }
        }
    }

    static {
        System.loadLibrary("gojni");
        init();
        Universe.touch();
    }

    private Seq() {
    }

    static void decRef(int i) {
        tracker.dec(i);
    }

    static native void destroyRef(int i);

    public static Ref getRef(int i) {
        return tracker.get(i);
    }

    public static int incGoObjectRef(GoObject goObject) {
        return goObject.incRefnum();
    }

    public static native void incGoRef(int i, GoObject goObject);

    public static int incRef(Object obj) {
        return tracker.inc(obj);
    }

    public static void incRefnum(int i) {
        tracker.incRefnum(i);
    }

    private static native void init();

    public static void setContext(Context context) {
        setContext((Object) context);
    }

    static native void setContext(Object obj);

    public static void touch() {
    }

    public static void trackGoRef(int i, GoObject goObject) {
        if (i <= 0) {
            goRefQueue.track(i, goObject);
            return;
        }
        throw new RuntimeException("trackGoRef called with Java refnum " + i);
    }
}