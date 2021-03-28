package hs.opcnetty.opc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.opcnetty.bean.MeasurePoint;
import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.opc.bridge.ExecutePythonBridge;
import hs.opcnetty.opc.event.Event;
import hs.opcnetty.opcproxy.command.CommandImp;
import hs.opcnetty.opcproxy.session.Session;
import hs.opcnetty.opcproxy.session.SessionManager;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 15:45
 */
@Data
public class OpcExecute implements Runnable {
    private Logger logger = LoggerFactory.getLogger(OpcExecute.class);

    public static final String FUNCTION_READ = "read";
    public static final String FUNCTION_WRITE = "write";


    private ExecutePythonBridge executePythonBridge;
    private String exename;
    private String ip;
    private String port;
    private String opcsevename;
    private String opcseveip;
    private String opcsevid;
    private OpcServeInfo serveInfo;
    private SessionManager sessionManager;
    private long writetimestamp = System.currentTimeMillis();
    private String function;

    private Map<String, MeasurePoint> registeredMeasurePointpool = new ConcurrentHashMap();
    private Map<String, MeasurePoint> waittoregistertagpool = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();



    private AtomicInteger reconnectcount = new AtomicInteger(0);

    private volatile ConnectStatus currentConnectStatus;//连接状态
    private volatile Map<ConnectStatus, Long> satusTimeStamp = new ConcurrentHashMap();//连接状态时间戳

    public ConnectStatus getCurrentConnectStatus() {
        return currentConnectStatus;
    }

    public void updateConnextStatus(ConnectStatus connectStatus) {
        currentConnectStatus = connectStatus;
        satusTimeStamp.put(connectStatus, System.currentTimeMillis());
    }

    public synchronized void addwaitaddIteambuf(MeasurePoint m) {
        waittoregistertagpool.put(m.getPoint().getTag(), m);
    }

    public boolean addOPCEvent(Event event) {
        return eventLinkedBlockingQueue.offer(event);
    }


    public OpcExecute(String function, OpcServeInfo serveInfo, String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid, SessionManager sessionManager) {
        this.function = function;
        this.serveInfo = serveInfo;
        this.exename = exename;// System.getProperty("user.dir") + "\\" +
        this.ip = ip;
        this.port = port;
        this.opcsevename = opcsevename;
        this.opcseveip = opcseveip;
        this.opcsevid = opcsevid;
        this.sessionManager = sessionManager;
        executePythonBridge = new ExecutePythonBridge(exename, ip, port, opcsevename, opcseveip, opcsevid, function);
    }


    public boolean isOpcServeOnline() {

        Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
        if (session != null) {
            if (session.getCtx() != null) {
                currentConnectStatus = ConnectStatus.CONNECTED;
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }


    /**
     * 这里有个主意点
     * 在opc连接成功后，有些opc服务器需要断开，再重新连接读到的数据才是正确的，因此
     * 在连接的时候，设置重连次数为1，这样在opc读数据成功那一次，要进行数据更新的时候，
     * 把进行重连操作，使得读取数据不异常！
     */
    public synchronized void connect() {
        if (!isOpcServeOnline()) {
            logger.info("*********need connect " + function);
//            setReconnectcount(1);
            reconnectcount.set(1);
            executePythonBridge.stop();
            executePythonBridge.execute();
            updateConnextStatus(ConnectStatus.CONNECTTING);
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    updateConnextStatus(ConnectStatus.CONNECTED);
                    logger.info("********" + opcsevename + opcseveip + " connect success");
                    registeredMeasurePointpool.clear();
                    if (waittoregistertagpool.size() > 0) {
                        sendPatchAddItemCmd(waittoregistertagpool.values());
                    }
                    break;
                } else {
                    logger.info("*******try connect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }

        } else {
            logger.info(" connect status is hold");
        }
    }


    public synchronized void reconnect() {
        logger.info("**** reconnect " + function);
        if (!isOpcServeOnline()) {
            executePythonBridge.stop();
            executePythonBridge.execute();
            updateConnextStatus(ConnectStatus.CONNECTTING);
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    updateConnextStatus(ConnectStatus.CONNECTED);
                    logger.info(opcsevename + opcseveip + function + " reconnect success");
                    logger.info(opcsevename + opcseveip + function + "registeredMeasurePoint size=" + registeredMeasurePointpool.size());
                    registeredMeasurePointpool.clear();
                    if (waittoregistertagpool.size() > 0) {
                        sendPatchAddItemCmd(waittoregistertagpool.values());
                    }

                    break;
                } else {
                    logger.info("try reconnect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }
        } else {
            logger.info(" connect status is hold");
        }
    }


    public void sendReadAllItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "read");
        try {
            Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
            session.getCtx().writeAndFlush(
                    CommandImp.READ.build(msg.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void dealReadAllItemsResult(JSONObject datajson) {
        if (!datajson.getString("msg").equals("success")) {
            logger.info("read error close it!");
            if (isOpcServeOnline()) {
                Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
                if (session != null) {
                    sessionManager.removeSessionModule(session.getCtx());
                    if (session.getCtx() != null) {
                        session.getCtx().close();
                    }
                }
                return;
            }
        }

        JSONObject influxwritedata = new JSONObject();
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            MeasurePoint measurePoint = registeredMeasurePointpool.get(key);
            if (measurePoint != null) {
                float value = datajson.getFloatValue(key);
                measurePoint.setValue(value);
                measurePoint.setInstant(Instant.now());
            }
        }


    }


    public void dealRemoveResult(JSONObject datajson) {
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            if (1 == datajson.getInteger(key)) {
                registeredMeasurePointpool.remove(key);
            }
        }
    }


    public void dealWriteResult(JSONObject datajson) {

        if (datajson.containsKey("timestamp")) {
            long writetimestamp = datajson.getLongValue("timestamp");
            //是否为旧的数据
            if (writetimestamp < satusTimeStamp.get(ConnectStatus.CONNECTED) || writetimestamp < satusTimeStamp.get(ConnectStatus.CONNECTTING)) {
                return;
            } else {
                for (String key : datajson.keySet()) {
                    if (key.equals("function")) {
                        continue;
                    }
                    if (0 == datajson.getInteger(key)) {
                        //反写数据异常为0则进行重连写
                        if (!ConnectStatus.CONNECTTING.equals(getCurrentConnectStatus())) {
                            reconnect();
                        }
                        return;
                    }
                }
            }
        }

    }


    public void sendPatchAddItemCmd(Collection<MeasurePoint> measurePointCollection) {
        JSONArray jsonArray = new JSONArray();
        for (MeasurePoint mp : measurePointCollection) {
            JSONObject msg = new JSONObject();
            msg.put("tag", mp.getPoint().getTag());
            jsonArray.add(msg);
        }
        try {

            logger.info("BATCHADDITEM " + jsonArray.toJSONString().getBytes("utf-8").length);
            logger.info(jsonArray.toJSONString());


            int indexwaitsendnum = 0;
            JSONArray waitsenddata = new JSONArray();
            //c++最长字符串长度16380
            for (int indexsplit = 0; indexsplit < jsonArray.size(); indexsplit++) {

                if (waitsenddata.toJSONString().getBytes("utf-8").length < 16380) {
                    waitsenddata.add(jsonArray.getJSONObject(indexsplit));
                    indexwaitsendnum++;
                } else {
                    waitsenddata.remove(indexwaitsendnum - 1);
                    if (waitsenddata.size() > 0) {
                        sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                                CommandImp.BATCHADDITEM.build(waitsenddata.toJSONString().getBytes("utf-8"), serveInfo.getServeid()));
                    }
                    waitsenddata = new JSONArray();
                    indexwaitsendnum = 0;
                    indexsplit--;
                }

            }

            //最后一批点号注册
            if (waitsenddata.size() > 0) {
                sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                        CommandImp.BATCHADDITEM.build(waitsenddata.toJSONString().getBytes("utf-8"), serveInfo.getServeid()));
            }


        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void dealAddItemResult(JSONObject datajson) {
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            if (1 == datajson.getInteger(key)) {
                MeasurePoint point = waittoregistertagpool.get(key);
                registeredMeasurePointpool.put(key, point);
            }
        }
    }


    public void sendStopItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "stop");
        Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
        if (session == null) {
            return;
        }
        try {
            session.getCtx().writeAndFlush(
                    CommandImp.STOP.build(msg.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        //移除session
        sessionManager.removeSessionModule(session.getCtx());
        session.getCtx().close();
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            logger.info("*****OPC RUN");
            try {
                connect();
                synchronized (this) {
                    if (isOpcServeOnline()) {
                        while (eventLinkedBlockingQueue.size() != 0) {
                            Event event = eventLinkedBlockingQueue.poll();
                            if ((event != null)) {
                                event.execute(this);
                            }
                        }
                    }
                }


                synchronized (this) {
                    if (registeredMeasurePointpool.size() > 0 && function.equals(OpcExecute.FUNCTION_READ)) {
                        sendReadAllItemsCmd();
                    }
                }

                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }


    public enum ConnectStatus {
        //已经连接
        CONNECTED,
        //断开连接
        DISCONNECTED,
        //正在连接
        CONNECTTING;

        private long eventimestamp;


    }


    public Session getMySession() {
        return sessionManager.getSpecialSession(serveInfo.getServeid(), function);
    }

    public void minsReconnectcount() {
        reconnectcount.decrementAndGet();
    }

}
