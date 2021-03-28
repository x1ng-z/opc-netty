package hs.opcnetty.bean;

import com.alibaba.fastjson.JSON;
import com.runlion.iot.driver.core.api.plat.dto.PointUploadDTO;
import com.runlion.iot.driver.core.api.plat.dto.PointValueDTO;
import com.runlion.iot.driver.core.api.plat.vo.PointResultHttpVO;
import com.runlion.iot.driver.core.model.MeasurePointConfig;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opc.OpcGroup;
import hs.opcnetty.service.DataUpload;
import hs.opcnetty.service.WebHandler;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/28 10:24
 */
@Data
@Builder
@Slf4j
public class ClusterSchduleJob implements Runnable {
    @Override
    public void run() {
        try {
            for (Map.Entry<Long, Map<String, MeasurePointConfig>> entry : sameratemapping.entrySet()) {

                long iotid = entry.getKey();
                Map<String, MeasurePointConfig> measurePointConfigMap = entry.getValue();

                OpcGroup opcGroup = opcConnectManger.getOpcGroupByIotid(iotid);

                List<PointValueDTO> points = new ArrayList<>();
                for (String tag : measurePointConfigMap.keySet()) {
                    MeasurePoint registeropcpoint = opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().get(tag);
                    if (registeropcpoint != null) {
                        PointValueDTO pcell = PointValueDTO.builder().time(registeropcpoint.getInstant().plus(8, ChronoUnit.HOURS).toEpochMilli())
                                .measurePoint(tag)
                                .node(measurePointConfigMap.get(tag).getNode())
                                .value(registeropcpoint.getValue()).build();
                        points.add(pcell);
                    }
                }

                PointUploadDTO pointUploadDTO = PointUploadDTO.builder().points(points).build();
                PointResultHttpVO respon = dataUpload.pointUpload(pointUploadDTO);
                log.info(JSON.toJSONString(respon));

            }


            if (isNeedCancelSchdule()) {
                if (future != null) {
                    future.cancel(true);
                    future = null;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private ScheduledFuture<?> future;
    private OpcConnectManger opcConnectManger;
    private DataUpload dataUpload;
    /**
     * map<resourceid,map<tag,measurepoint>>
     */
    private final Map<Long, Map<String, MeasurePointConfig>> sameratemapping;

    public ClusterSchduleJob() {
        sameratemapping = new ConcurrentHashMap<>();
    }

    public synchronized void addmeasurepoint(Long resourceid, MeasurePointConfig point) {

        if (sameratemapping.containsKey(resourceid)) {
            Map<String, MeasurePointConfig> secmap = sameratemapping.get(resourceid);
            secmap.put(point.getMeasurePoint(), point);
        } else {
            Map<String, MeasurePointConfig> secmap = new ConcurrentHashMap<>();
            secmap.put(point.getMeasurePoint(), point);
            sameratemapping.put(resourceid, secmap);
        }
    }

    /**
     * delete change rate point
     */
    public boolean deleteChangeRateMeasurePoint(Long resourceid, MeasurePointConfig point) {
        String pendingdelete = null;

        Map<String, MeasurePointConfig> secmap = sameratemapping.get(resourceid);
        if (null != secmap) {
            MeasurePointConfig aimpoint = secmap.get(point.getMeasurePoint());
            if (null != aimpoint) {
                //find point.check it hava
                if (!aimpoint.getRate().equals(point.getRate())) {
                    pendingdelete = point.getMeasurePoint();
                }
            }
            //delete it
            if (null != pendingdelete) {
                secmap.remove(pendingdelete);
                return true;
            }
        }
        return false;
    }

    /***
     * delete needn't tag
     * */
    public boolean deleteNeedntRateMeasurePoint(Long resourceid, String tag) {
        Map<String, MeasurePointConfig> secmap = sameratemapping.get(resourceid);
        if (null != secmap) {
            return null != secmap.remove(tag);
        }
        return false;
    }


    public boolean isNeedCancelSchdule() {
        long totalsize = 0;
        for (Map<String, MeasurePointConfig> secmp : sameratemapping.values()) {
            totalsize += secmp.values().size();
        }
        return totalsize == 0;
    }
}
