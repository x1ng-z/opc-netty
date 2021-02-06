package hs.opcnetty.starter;

import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.opc.OpcConnectManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/2/5 18:17
 *
 * opc服务操作类
 */
@Component
public class OpcConnectServeOperator {

    @Autowired
    private OpcConnectManger opcConnectManger;


    /**
     * 注册opc服务
     *
     * @param serveInfo 内容参看类文件
     */
    public void registerOpcServe(OpcServeInfo serveInfo){
        opcConnectManger.connectSetup(serveInfo);
    }

    /**
     * 解注册opc服务
     *
     * @param serveInfo 内容参看类文件
     */
    public void unregisterOpcServe(OpcServeInfo serveInfo){
        opcConnectManger.closeSpecialOpcserve(serveInfo);
    }




}
