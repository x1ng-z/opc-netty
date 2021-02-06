package hs.opcnetty.starter;

import hs.opcnetty.bean.Point;
import hs.opcnetty.opc.OpcConnectManger;
import hs.opcnetty.opc.OpcGroup;
import hs.opcnetty.opc.event.RegisterEvent;
import hs.opcnetty.opc.event.UnRegisterEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/2/5 18:17
 * opc服务点号操作类
 */
@Component
public class OpcItemOperator {

    @Autowired
    private OpcConnectManger opcConnectManger;



    /**
     * 添加点位
     * @param opcpointlist Point参数详细查看类文件
     * */
    public void addItems(List<Point> opcpointlist){
        for(Point point:opcpointlist){
            RegisterEvent registerEvent=new RegisterEvent();
            registerEvent.setPoint(point);
            OpcGroup opcGroup =opcConnectManger.getOpcconnectpool().get(point.getOpcserveid());
            if(opcGroup!=null){
                opcGroup.getReadopcexecute().addOPCEvent(registerEvent);
                if(1==point.getWriteable()){
                    opcGroup.getWriteopcexecute().addOPCEvent(registerEvent);
                }
            }
        }
    }

    /**
     * 移除点位
     * @param opcpointlist Point参数详细查看类文件
     * */
    public void removeItems(List<Point> opcpointlist){
        for(Point point:opcpointlist){
            UnRegisterEvent registerEvent=new UnRegisterEvent();
            registerEvent.setPoint(point);
            OpcGroup opcGroup =opcConnectManger.getOpcconnectpool().get(point.getOpcserveid());
            if(opcGroup!=null){
                opcGroup.getReadopcexecute().addOPCEvent(registerEvent);
                if(1==point.getWriteable()){
                    opcGroup.getWriteopcexecute().addOPCEvent(registerEvent);
                }
            }
        }
    }



}
