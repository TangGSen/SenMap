package sen.com.testmap;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Administrator on 2016/10/8.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);

    }
}
