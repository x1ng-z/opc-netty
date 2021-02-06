package hs.opcnetty.opc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.bean.Point;
import hs.opcnetty.opc.event.RegisterEvent;
import hs.opcnetty.opcproxy.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 13:38
 */
@Component
public class OpcConnectManger /*implements Runnable*/ {
    private Logger logger = LoggerFactory.getLogger(OpcConnectManger.class);

    private Map<Integer, OpcServeInfo> opcservepool = new ConcurrentHashMap();
    private Map<Integer, OpcGroup> opcconnectpool = new ConcurrentHashMap();

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
        OpcExecute readopcexecute = new OpcExecute(OpcExecute.FUNCTION_READ, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), serveInfo.getServeid() + "", sessionManager);
        OpcExecute writeopcexecute = new OpcExecute(OpcExecute.FUNCTION_WRITE, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), serveInfo.getServeid() + "", sessionManager);

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


    public void closeSpecialOpcserve(OpcServeInfo serveInfo){
        OpcGroup opcGroup=opcconnectpool.get(serveInfo.getServeid());
        if(opcGroup!=null){
            opcGroup.getReadopcexecute().sendStopItemsCmd();
            opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

            opcGroup.getWriteopcexecute().sendStopItemsCmd();
            opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
        }
    }




    @PreDestroy
    void close() {
        logger.info("opc connect try to shutdown");
        for (OpcGroup opcGroup : opcconnectpool.values()) {
            opcGroup.getReadopcexecute().sendStopItemsCmd();
            opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

            opcGroup.getWriteopcexecute().sendStopItemsCmd();
            opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
        }
    }


    public Map<Integer, OpcServeInfo> getOpcservepool() {
        return opcservepool;
    }

    public Map<Integer, OpcGroup> getOpcconnectpool() {
        return opcconnectpool;
    }

}
