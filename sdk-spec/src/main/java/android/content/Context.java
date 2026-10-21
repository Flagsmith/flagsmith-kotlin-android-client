package android.content;

import java.io.File;

public abstract class Context {
    public static final int MODE_PRIVATE = 0;

    public abstract Context getApplicationContext();

    public abstract File getCacheDir();

    public abstract SharedPreferences getSharedPreferences(String name, int mode);
}
