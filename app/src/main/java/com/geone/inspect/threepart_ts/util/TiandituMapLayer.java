
package com.geone.inspect.threepart_ts.util;

import com.esri.android.map.TiledServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by admin on 15/10/15.
 * 天地图切片图层
 */

public class TiandituMapLayer extends TiledServiceLayer {
    /**地图图层*/
    public static final String URL_MAP="http://t0.tianditu.com/vec_c/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&format=&TileMatrixSet=c&TileMatrix=%1$s&TileRow=%2$s&TileCol=%3$s";
    /**注记图层*/
    public static final String URL_ZHUJI="http://t0.tianditu.com/cva_c/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=cva&style=default&format=&TileMatrixSet=c&TileMatrix=%1$s&TileRow=%2$s&TileCol=%3$s";

    //private TianDiTuTiledMapServiceType _mapType;
    private TileInfo tiandituTileInfo;
    //天地图切片服务地址
    private String mLayerUrl;

    public TiandituMapLayer(String layerurl,String layername){
        super("");

        try {
            getServiceExecutor().submit(new Runnable() {
                final TiandituMapLayer layer;

                @Override
                public final void run() {
                    layer.initLayer();
                }

                {
                    layer = TiandituMapLayer.this;
                }
            });
        }catch (RejectedExecutionException _ex){

        }

    }

    public TiandituMapLayer(String url){
        super("");
        this.mLayerUrl =url;
        try {
            getServiceExecutor().submit(new Runnable() {
                final TiandituMapLayer layer;

                @Override
                public final void run() {
                    layer.initLayer();
                }

                {
                    layer = TiandituMapLayer.this;
                }
            });
        }catch (RejectedExecutionException _ex){

        }

    }

    private String getTiandituUrl(int level,int col,int row){
       //http://t0.tianditu.com/vec_c/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&format=&TileMatrixSet=c  &TileMatrix=14&TileRow=2643&TileCol=13610
        return String.format(mLayerUrl,level+1,col,row);
//       return "http://t0.tianditu.com/vec_c/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&format=&TileMatrixSet=c&TileMatrix="+String.valueOf(level+1)+"&TileRow="+String.valueOf(col)+"&TileCol="+String.valueOf(row);
    }

    @Override
    protected byte[] getTile(int level, int col, int row) throws Exception {
        byte[] result = null;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String strUrl = getTiandituUrl(level,row,col);
            URL url = new URL(strUrl);

            HttpURLConnection httpUrl = null;
            BufferedInputStream bis = null;
            byte[] buf = new byte[1024];

            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());

            while (true){
                int bytes_read=bis.read(buf);
                if(bytes_read>0){
                    bos.write(buf,0,bytes_read);
                }else{
                    break;
                }
            }
            bis.close();
            httpUrl.disconnect();
            result = bos.toByteArray();

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    protected void initLayer(){

        this.buildTileInfo();
        this.setFullExtent(new Envelope(-180, -90, 180, 90));
        this.setDefaultSpatialReference(SpatialReference.create(4490)); //GCS2000
        this.setInitialExtent(new Envelope(118.89088134765625,31.8800830078125, 118.99088134765625, 32.1600830078125));
//        this.setInitialExtent(new Envelope(118.89088134765625,31.8800830078125, 118.99088134765625, 32.1600830078125));
        super.initLayer();
    }

    @Override
    public TileInfo getTileInfo(){

        return this.tiandituTileInfo;

    }

    private void buildTileInfo()

    {

        Point originalPoint=new Point(-180,90);

        double[] res={
                0.703125,
                0.3515625,
                0.17578125,
                0.087890625,
                0.0439453125,
                0.02197265625,
                0.010986328125,
                0.0054931640625,
                0.00274658203125,
                0.001373291015625,
                0.0006866455078125,
                0.00034332275390625,
                0.000171661376953125,
                8.58306884765625E-5,
                4.291534423828125E-5,
                2.1457672119140625E-5,
                1.0728836059570313E-5,
                5.3644180297851563E-6,
                1.3411045074462891E-6
        };

        double[] scale={
                295497593.05875003,
                147748796.52937502,
                73874398.264687508,
                36937199.132343754,
                18468599.566171877,
                9234299.7830859385,
                4617149.8915429693,
                2308574.9457714846,
                1154287.4728857423,
                577143.73644287116,
                288571.86822143558,
                144285.93411071779,
                72142.967055358895,
                36071.483527679447,
                18035.741763839724,
                9017.8708819198619,
                4508.9354409599309,
                2254.4677204799655,
                1127.2338602399827
        };

        int levels=19;
        int dpi=96;
        int tileWidth=256;
        int tileHeight=256;
        this.tiandituTileInfo=new TileInfo(originalPoint, scale, res, levels, dpi, tileWidth,tileHeight);
//        this.tiandituTileInfo=new TileInfo(originalPoint, scale, res, levels, dpi, tileWidth,tileHeight);
        this.setTileInfo(this.tiandituTileInfo);
    }

}
