package hs.opcnetty.opc;

import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.opcproxy.session.Session;
import hs.opcnetty.opcproxy.session.SessionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 13:38
 */
@Component
@Slf4j
@Data
public class OpcConnectManger {

    private Map<Integer, OpcServeInfo> opcservepool = new ConcurrentHashMap();
    private Map<Integer, OpcGroup> opcconnectpool = new ConcurrentHashMap();
    private AtomicInteger atomicInteger=new AtomicInteger(0);

    private SessionManager sessionManager;

    private String opcservedir;
    private String nettypoty;


    private ExecutorService executorService;

    @Autowired
    public OpcConnectManger(@Value("${opcservedir}") String opcservedir,
                            @Value("${nettyport}") String nettypoty,
                            ExecutorService executorService, SessionManager sessionManager) {
        this.executorService = executorService;
        this.sessionManager = sessionManager;
        this.nettypoty = nettypoty;
        this.opcservedir = opcservedir;
    }


    private OpcGroup connectinit(OpcServeInfo serveInfo) {

        OpcGroup opcGroup = new OpcGroup();
        OpcExecute readopcexecute = new OpcExecute(OpcExecute.FUNCTION_READ, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), Integer.toString(serveInfo.getServeid()), sessionManager);
        OpcExecute writeopcexecute = new OpcExecute(OpcExecute.FUNCTION_WRITE, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), Integer.toString(serveInfo.getServeid()), sessionManager);

        opcGroup.setReadopcexecute(readopcexecute);
        opcGroup.setWriteopcexecute(writeopcexecute);
        return opcGroup;
    }


    public void connectSetup(OpcServeInfo serveInfo) {
        OpcGroup opcGroup = connectinit(serveInfo);
        executorService.execute(opcGroup.getReadopcexecute());
        executorService.execute(opcGroup.getWriteopcexecute());
        opcconnectpool.put(serveInfo.getServeid(), opcGroup);
        opcservepool.put(serveInfo.getServeid(), serveInfo);
    }



    public void closeSpecial(OpcServeInfo serveInfo) {
        OpcServeInfo opcServeInfo = getOpcserveInfoByIotid(serveInfo.getIotid());
        if (opcServeInfo != null) {
            //stop
            OpcGroup opcGroup = getOpcGroupByIotid(serveInfo.getIotid());
            if (opcGroup != null) {
                opcGroup.getReadopcexecute().sendStopItemsCmd();
                opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

                opcGroup.getWriteopcexecute().sendStopItemsCmd();
                opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
            }
            //clear cache
            opcservepool.remove(opcServeInfo.getServeid());
            opcconnectpool.remove(opcServeInfo.getServeid());
        }
    }

    public OpcServeInfo getOpcserveInfoByIotid(long iotdatasourceid) {
        for (OpcServeInfo opcServeInfo : opcservepool.values()) {

            if (opcServeInfo.getIotid() == iotdatasourceid) {
                return opcServeInfo;
            }
        }
        return null;
    }

    public OpcGroup getOpcGroupByIotid(long iotdatasourceid) {
        for (OpcGroup opcgroup : opcconnectpool.values()) {
            if (opcgroup.getIotid() == iotdatasourceid) {
                return opcgroup;
            }
        }
        return null;
    }


    public void closeSpecial(long iotid) {
        OpcServeInfo opcServeInfo = getOpcserveInfoByIotid(iotid);
        if (opcServeInfo != null) {
            //stop
            OpcGroup opcGroup = getOpcGroupByIotid(iotid);
            if (opcGroup != null) {
                opcGroup.getReadopcexecute().sendStopItemsCmd();
                opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

                opcGroup.getWriteopcexecute().sendStopItemsCmd();
                opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
            }
            //clear cache
            opcservepool.remove(opcServeInfo.getServeid());
            opcconnectpool.remove(opcServeInfo.getServeid());
        }
    }

    public boolean isConnect(long iotid) {
        OpcServeInfo opcServeInfo = getOpcserveInfoByIotid(iotid);
        Session readexe = sessionManager.getSpecialSession(opcServeInfo.getServeid(), OpcExecute.FUNCTION_READ);

        Session writeexe = sessionManager.getSpecialSession(opcServeInfo.getServeid(), OpcExecute.FUNCTION_WRITE);

        if ((writeexe != null) && (readexe != null)) {
            return true;
        } else {
            return false;
        }

    }

    @PreDestroy
    void close() {
        log.info("opc connect try to shutdown");
        for (OpcGroup opcGroup : opcconnectpool.values()) {
            opcGroup.getReadopcexecute().sendStopItemsCmd();
            opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

            opcGroup.getWriteopcexecute().sendStopItemsCmd();
            opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
        }
    }


}
