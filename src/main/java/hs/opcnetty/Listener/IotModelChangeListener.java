package hs.opcnetty.Listener;

import com.runlion.iot.driver.sdk.event.DriverModelChangeEvent;
import hs.opcnetty.opc.OpcConnectManger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/26 18:52
 */
@Component
public class IotModelChangeListener implements ApplicationListener<DriverModelChangeEvent> {

    @Override
    public void onApplicationEvent(DriverModelChangeEvent driverModelChangeEvent) {
        //do nothing
    }
}
