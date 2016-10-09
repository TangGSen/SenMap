package sen.com.testmap;

import android.content.Context;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.overlay.DrivingRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;

/**
 * Created by Administrator on 2016/9/30.
 */

public class CustomerDriveStyle extends DrivingRouteOverlay {


    public CustomerDriveStyle(Context context, AMap aMap, DrivePath drivePath, LatLonPoint latLonPoint, LatLonPoint latLonPoint1) {
        super(context, aMap, drivePath, latLonPoint, latLonPoint1);
    }

    @Override
    protected BitmapDescriptor getEndBitmapDescriptor() {
        BitmapDescriptor reBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.marker);
        return reBitmapDescriptor;
    }
    /*修改起点marker样式*/
    @Override
    protected BitmapDescriptor getStartBitmapDescriptor() {
        BitmapDescriptor reBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.marker);
        return reBitmapDescriptor;
    }
}
