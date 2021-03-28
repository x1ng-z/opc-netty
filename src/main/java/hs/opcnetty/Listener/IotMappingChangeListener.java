package hs.opcnetty.Listener;

import com.runlion.iot.driver.core.model.MeasurePointConfig;
import com.runlion.iot.driver.sdk.event.DriverMappingChangeEvent;
import hs.opcnetty.bean.ClusterSchduleJob;
import hs.opcnetty.bean.MeasurePoint;
import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.bean.Point;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opc.OpcGroup;
import hs.opcnetty.opc.event.RegisterEvent;
import hs.opcnetty.service.DataUpload;
import hs.opcnetty.service.WebHandler;
import hs.opcnetty.util.RelationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/26 18:51
 */
@Component
public class IotMappingChangeListener implements ApplicationListener<DriverMappingChangeEvent> {

    private final OpcConnectManger opcConnectManger;
    //map<rate,clusterschdulejob>
    private final Map<Integer, ClusterSchduleJob> rateClassPoints;

    private ThreadPoolTaskScheduler taskScheduler;
    private DataUpload dataUpload;

    @Autowired
    public IotMappingChangeListener(OpcConnectManger opcConnectManger,
                                    ThreadPoolTaskScheduler taskScheduler,
                                    DataUpload dataUpload) {
        this.opcConnectManger = opcConnectManger;
        this.rateClassPoints = new ConcurrentHashMap<>();
        this.taskScheduler = taskScheduler;
        this.dataUpload=dataUpload;
    }

    @Override
    public void onApplicationEvent(DriverMappingChangeEvent driverMappingChangeEvent) {


        long iotid = driverMappingChangeEvent.getDataSourceId();
        List<MeasurePointConfig> measurePointConfigs = driverMappingChangeEvent.getPointConfigList();

        //operate opc executor

        OpcGroup opcGroup = opcConnectManger.getOpcGroupByIotid(iotid);
        OpcServeInfo opcServeInfo = opcConnectManger.getOpcserveInfoByIotid(iotid);
        if ((opcGroup != null) && (opcServeInfo != null)) {

            Set<String> thistimetags = new HashSet<>();
            for (MeasurePointConfig pointconfig : measurePointConfigs) {

                //update opc point
                Point point = Point.builder().rate(pointconfig.getRate()).opcserveid(opcServeInfo.getServeid()).tag(pointconfig.getMeasurePoint()).build();
                RegisterEvent registerEvent = new RegisterEvent();
                registerEvent.setPoint(point);

                opcGroup.getWriteopcexecute().addOPCEvent(registerEvent);
                opcGroup.getReadopcexecute().addOPCEvent(registerEvent);
                thistimetags.add(pointconfig.getMeasurePoint());

                //check and delete change rate point
                for (ClusterSchduleJob job : rateClassPoints.values()) {
                    if (job.deleteChangeRateMeasurePoint(iotid, pointconfig)) {
                        break;
                    }
                }

                //add or update job point
                if (rateClassPoints.containsKey(pointconfig.getRate())) {
                    rateClassPoints.get(pointconfig.getRate()).addmeasurepoint(iotid, pointconfig);
                } else {
                    ClusterSchduleJob clusterSchduleJob = ClusterSchduleJob.builder()
                            .opcConnectManger(opcConnectManger)
                            .dataUpload(dataUpload)
                            .build();
                    clusterSchduleJob.addmeasurepoint(iotid, pointconfig);
                }


            }
            //remove needn't register tags
            Set<String> neednttags = RelationUtil.different(opcGroup.getReadopcexecute().getWaittoregistertagpool().keySet(), thistimetags);
            for (String dtag : neednttags) {
                opcGroup.getReadopcexecute().getWaittoregistertagpool().remove(dtag);
                opcGroup.getWriteopcexecute().getWaittoregistertagpool().remove(dtag);

                //delete neednt tag in clusterSchduleJob
                for (ClusterSchduleJob job : rateClassPoints.values()) {
                    if (job.deleteNeedntRateMeasurePoint(iotid, dtag)) {
                        break;
                    }
                }
            }


            //check is need remove/stop or start
            List<Integer> pendingdel = new ArrayList<>();
            for (Map.Entry<Integer, ClusterSchduleJob> entry : rateClassPoints.entrySet()) {
                if (null == entry.getValue().getFuture()) {
                    if (entry.getValue().isNeedCancelSchdule()) {
                        pendingdel.add(entry.getKey());
                    } else {
                        Trigger periodicTrigger = new PeriodicTrigger(entry.getKey() <= 0 ? 1 : entry.getKey(), TimeUnit.SECONDS);
                        ScheduledFuture<?> future = taskScheduler.schedule(entry.getValue(), periodicTrigger);
                        entry.getValue().setFuture(future);
                    }
                }
            }


        }

    }
}
