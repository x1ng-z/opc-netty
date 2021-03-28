package hs.opcnetty.Listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runlion.iot.driver.sdk.event.DriverConnectChangeEvent;
import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.bean.dto.OpcConnectDto;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.util.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/26 18:48
 */
@Component
@Slf4j
public class IotConnectChangeListener implements ApplicationListener<DriverConnectChangeEvent> {
    private OpcConnectManger opcConnectManger;

    @Autowired
    public IotConnectChangeListener(OpcConnectManger opcConnectManger) {
        this.opcConnectManger = opcConnectManger;
    }

    @Override
    public void onApplicationEvent(DriverConnectChangeEvent driverConnectChangeEvent) {
        long datasourceid=driverConnectChangeEvent.getDataSourceId();
        OpcConnectDto opcConnectDto=JSON.parseObject(driverConnectChangeEvent.getConnect(), OpcConnectDto.class);

        if(opcConnectDto.valid()){

            OpcServeInfo opcServeInfo=opcConnectManger.getOpcserveInfoByIotid(datasourceid);
            if(opcServeInfo==null){
                //new
                opcServeInfo=new OpcServeInfo();
                opcServeInfo.setServeip(opcConnectDto.getOpcIp());
                opcServeInfo.setServename(opcConnectDto.getOpcClsId());
                opcConnectManger.connectSetup(opcServeInfo);

            }else{
                //exist
                if(!(opcServeInfo.getServeip().equals(opcConnectDto.getOpcIp())&&opcServeInfo.getServename().equals(opcConnectDto.getOpcClsId()))){
                    opcConnectManger.closeSpecial(datasourceid);
                    opcServeInfo.setServename(opcConnectDto.getOpcClsId());
                    opcServeInfo.setServeip(opcConnectDto.getOpcIp());
                    opcConnectManger.connectSetup(opcServeInfo);
                }
            }
        }else {
            log.error("invalid params");
        }
    }
}
