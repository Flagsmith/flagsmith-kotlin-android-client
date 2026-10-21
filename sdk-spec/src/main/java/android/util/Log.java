package android.util;

public final class Log {
    public static int d(String tag, String msg) {
        return println("D", tag, msg);
    }

    public static int i(String tag, String msg) {
        return println("I", tag, msg);
    }

    public static int e(String tag, String msg) {
        return println("E", tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return println("E", tag, msg + "\n" + tr);
    }

    private static int println(String priority, String tag, String msg) {
        System.err.println(priority + "/" + tag + ": " + msg);
        return 0;
    }
}
