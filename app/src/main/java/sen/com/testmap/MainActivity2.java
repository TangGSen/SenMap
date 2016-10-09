package sen.com.testmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener, RouteSearch.OnRouteSearchListener, AMap.InfoWindowAdapter {

    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int PERMISSON_REQUESTCODE = 0;
    private GeocodeSearch geocoderSearch;
    private MapView mapView;
    private AMap aMap;
    private String startFristAddress="";
    private String startSecondAddress ="";
    private String targetFristAddress="";
    private String targetSecondAddress="";

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //路线规划
    private RouteSearch routeSearch;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;


    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器

    private double lat;
    private double lon;
    //可以通过类implement方式实现AMapLocationListener接口，也可以通过创造接口类对象的方法实现
//以下为后者的举例：
    AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
//                    //可在其中解析amapLocation获取相应内容。
//                    StringBuffer buffer = new StringBuffer();
//                    buffer.append(amapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表);
//                    buffer.append(amapLocation.getLatitude());//获取纬度
//                    buffer.append(amapLocation.getLongitude());//获取经度
//                    buffer.append(amapLocation.getAccuracy());//获取精度信息
//                    buffer.append(amapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                    buffer.append(amapLocation.getCountry());//国家信息
//                    buffer.append(amapLocation.getProvince());//省信息
//                    buffer.append(amapLocation.getCity());//城市信息
//                    buffer.append(amapLocation.getDistrict());//城区信息
//                    buffer.append(amapLocation.getStreet());//街道信息
//                    buffer.append(amapLocation.getStreetNum());//街道门牌号信息
//                    buffer.append(amapLocation.getCityCode());//城市编码
//                    buffer.append(amapLocation.getAdCode());//地区编码
//                    buffer.append(amapLocation.getAoiName());//获取当前定位点的AOI信息
//
//                    Log.e("sen", buffer.toString());
                    startFristAddress = amapLocation.getAddress();
                    startSecondAddress = amapLocation.getAoiName();

                    // 设置当前地图显示为当前位置
                    lat = amapLocation.getLatitude();
                    lon = amapLocation.getLongitude();
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 19));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(lat, lon));
                    markerOptions.title("我的位置");
                    markerOptions.visible(true);
                    markerOptions.anchor(0.5f, 0.5f);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    aMap.addMarker(markerOptions);


                    //搜索目标地址编码
                    targetSecondAddress ="中关村当代商城7层";
                    seachTarget(targetSecondAddress, amapLocation.getCityCode());
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };



    //搜索地理位置

    private void seachTarget(String target, String citycode) {
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);


        GeocodeQuery query = new GeocodeQuery(target, citycode);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck) {
            checkPermissions(needPermissions);
        }

        mapView.onResume();
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁

        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
    }


    /**
     * @since 2.5.0
     */
    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }


    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }


    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton(R.string.setting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化地图控件
        mapView = (MapView) findViewById(R.id.amap);
        //必须要写
        mapView.onCreate(savedInstanceState);


        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mAMapLocationListener);

        init();


    }

    /**
     * * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        setUpMap();

    }

    private void setUpMap() {
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setOnceLocation(true);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setInfoWindowAdapter(this);


    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
//                geoMarker.setPosition(AMapUtil.convertToLatLng(address
//                        .getLatLonPoint()));
                targetFristAddress = address.getFormatAddress();
                String addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
                        + address.getFormatAddress();
                //然后规划路线
                planningRoute(address.getLatLonPoint());
                Log.e("sen", addressName);
            } else {
                ToastUtil.showerror(this, rCode);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    //规划路线
    private void planningRoute(LatLonPoint latLonPoint) {
        Log.e("sen", "lat:" + lat + "lon:" + lon);
        LatLonPoint startPoint = AMapUtil.convertToLatLonPoint(new LatLng(lat, lon));
        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, latLonPoint);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
        routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询

    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {


    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (i == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null && driveRouteResult.getPaths().size() > 0) {
                aMap.clear();
                DrivePath drivePath = driveRouteResult.getPaths().get(0);
                CustomerDriveStyle drivingRouteOverlay = new CustomerDriveStyle(this, aMap, drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();

                aMap.getMapScreenMarkers().get(0).setTitle("计算路线结果1");
                drivingRouteOverlay.setNodeIconVisibility(false);
                drivingRouteOverlay.setThroughPointIconVisibility(false);


           //     aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driveRouteResult.getStartPos().getLatitude(), driveRouteResult.getStartPos().getLongitude()), 12));
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driveRouteResult.getTargetPos().getLatitude(), driveRouteResult.getTargetPos().getLongitude()), 18));
//              Marker marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
//                        .icon(BitmapDescriptorFactory
//                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                marker.setPosition(AMapUtil.convertToLatLng( driveRouteResult.getStartPos()));
//                marker.setPosition(AMapUtil.convertToLatLng( driveRouteResult.getTargetPos()));
               // aMap.getMapScreenMarkers().get(aMap.getMapScreenMarkers().size()-1).showInfoWindow();
                aMap.getMapScreenMarkers().get(aMap.getMapScreenMarkers().size()-1).showInfoWindow();
                ToastUtil.show(this,  aMap.getMapScreenMarkers().size()+"");



            } else {
                ToastUtil.show(this, "很抱歉,没有计算路线结果");
            }
        } else if (i == 27) {
            ToastUtil.show(this, "网络异常");
        } else if (i == 32) {
//            ToastUtil.show(this, R.string.error_key);
            ToastUtil.show(this, "出现异常");
        } else {
            ToastUtil.show(this, "出现异常");
//            ToastUtil.show(this, getString(R.string.error_other) + i);
        }
    }


    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

//    *监听自定义infowindow窗口的infocontents事件回调


    @Override
    public View getInfoContents(Marker marker) {

        View infoContent = getLayoutInflater().inflate(R.layout.custom_info_windown, null);
        render(marker, infoContent);
        return infoContent;
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {

        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_windown, null);

        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.inforwindow_text));
        TextView second_text = ((TextView) view.findViewById(R.id.second_text));
        if (!TextUtils.isEmpty(title)) {
            if (title.equals("起点")){
                titleUi.setText(startFristAddress);
                second_text.setText(startSecondAddress);
            }else{
                titleUi.setText(targetFristAddress);
                second_text.setText(targetSecondAddress);
            }

        } else {
            titleUi.setText("");
        }
      // marker.setVisible(true);
    }
}