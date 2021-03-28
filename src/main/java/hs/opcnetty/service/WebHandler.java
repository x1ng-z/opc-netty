package hs.opcnetty.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runlion.iot.common.vo.DriverConfigInfoVO;
import com.runlion.iot.driver.core.api.driver.dto.DataWriteDTO;
import com.runlion.iot.driver.core.api.plat.dto.PointUploadDTO;
import com.runlion.iot.driver.core.api.plat.vo.PointResultHttpVO;
import com.runlion.iot.driver.sdk.service.impl.AbstractIotDriverDataHandler;
import com.runlion.iot.driver.sdk.service.impl.IotDriverServiceImpl;
import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.bean.Point;
import hs.opcnetty.bean.vo.IotWriteDataVo;
import hs.opcnetty.bean.vo.IotWriteVo;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opc.OpcGroup;
import hs.opcnetty.opc.event.WriteEvent;
import hs.opcnetty.opcproxy.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/26 18:56
 */
@Component
public class WebHandler extends AbstractIotDriverDataHandler {
    private OpcConnectManger opcConnectManger;
    private String protocol;

    @Autowired
    public WebHandler(@Value("${driver.protocol}") String protocol, IotDriverServiceImpl iotDriverService, OpcConnectManger opcConnectManger) {
        this.protocol = protocol;
        this.opcConnectManger=opcConnectManger;

    }

    @Override
    public boolean isConnect(Long datasourceId) {
        return  opcConnectManger.isConnect(datasourceId);
    }

    @Override
    public JSONObject writeValue(List<DataWriteDTO> writeList) {
        for(DataWriteDTO dataWritecell:writeList){
            long iotid=dataWritecell.getDatasourceId();
            String tag=dataWritecell.getMeasurePoint();
            Float value=(Float)dataWritecell.getValue();

            OpcServeInfo opcServeInfo =opcConnectManger.getOpcserveInfoByIotid(iotid);
            OpcGroup opcGroup =opcConnectManger.getOpcGroupByIotid(iotid);

            if(opcServeInfo!=null&&opcGroup!=null){
                Point point=Point.builder().opcserveid(opcServeInfo.getServeid()).tag(tag).value(value).build();
                WriteEvent writeevent = new WriteEvent(point.getValue());
                writeevent.setPoint(point);
                writeevent.setValue(point.getValue());
                opcGroup.getWriteopcexecute().addOPCEvent(writeevent);
            }
        }

        IotWriteVo iotWriteVo=IotWriteVo.builder().status(200).message("").data(
                IotWriteDataVo.builder().count(writeList.size()).build()
        ).build();
        return (JSONObject) JSON.toJSON(iotWriteVo);


    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public DriverConfigInfoVO getDriverConfigInfo() {
        return super.getDriverConfigInfo();
    }

}
