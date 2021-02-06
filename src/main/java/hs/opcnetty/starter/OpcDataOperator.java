package hs.opcnetty.starter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.opcnetty.bean.MeasurePoint;
import hs.opcnetty.bean.Point;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opc.OpcGroup;
import hs.opcnetty.opc.event.WriteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/2/5 18:18
 * opc数据操作类
 */
@Component
public class OpcDataOperator {

    @Autowired
    private OpcConnectManger opcConnectManger;

    /**
     * 读取所有注册过的数据
     *
     * @param opcserveid opcserveid
     */
    public JSONArray readAllRegisterItems(int opcserveid) {
        OpcGroup opcGroup = opcConnectManger.getOpcconnectpool().get(opcserveid);
        JSONArray opcdata = new JSONArray();
        if (opcGroup != null) {
            for (MeasurePoint measurepoint : opcGroup.getReadopcexecute().getRegisteredMeasurePoint().values()) {
                Instant valuegettime = measurepoint.getInstant();
                if (valuegettime != null) {
                    JSONObject cellopcdata = new JSONObject();
                    cellopcdata.put("tag", measurepoint.getPoint().getTag());
                    cellopcdata.put("value", measurepoint.getValue());
                    cellopcdata.put("timestamp", valuegettime.toEpochMilli());
                    opcdata.add(cellopcdata);
                }

            }
        }
        return opcdata;
    }

    /**
     * 读取指定注册过的数据点位
     *
     * @param pointlist
     */
    public JSONArray readSecialRegisterItems(List<Point> pointlist) {
        JSONArray opcdata = new JSONArray();
        for (Point point : pointlist) {
            OpcGroup opcGroup = opcConnectManger.getOpcconnectpool().get(point.getOpcserveid());
            if (opcGroup != null) {
                MeasurePoint measurepoint = opcGroup.getReadopcexecute().getRegisteredMeasurePoint().get(point.getTag());
                Instant valuegettime = measurepoint.getInstant();
                if ((measurepoint != null) && (valuegettime != null)) {
                    JSONObject cellopcdata = new JSONObject();
                    cellopcdata.put("tag", measurepoint.getPoint().getTag());
                    cellopcdata.put("value", measurepoint.getValue());
                    cellopcdata.put("timestamp", valuegettime.toEpochMilli());
                    opcdata.add(cellopcdata);
                }
            }
        }
        return opcdata;
    }


    /**
     * 反写数据至指定注册过的点位数
     *
     * @param point
     */
    public void writeSecialRegisterItems(Point point) {
        OpcGroup opcGroup = opcConnectManger.getOpcconnectpool().get(point.getOpcserveid());
        if (opcGroup != null) {
            WriteEvent writeevent = new WriteEvent();
            writeevent.setPoint(point);
            writeevent.setValue(point.getValue());
            opcGroup.getWriteopcexecute().addOPCEvent(writeevent);
        }
    }

}
