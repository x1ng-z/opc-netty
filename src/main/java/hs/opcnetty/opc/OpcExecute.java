package hs.opcnetty.opc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.opcnetty.bean.MeasurePoint;
import hs.opcnetty.bean.OpcServeInfo;
import hs.opcnetty.opc.bridge.ExecutePythonBridge;
import hs.opcnetty.opc.event.Event;
import hs.opcnetty.opc.event.RegisterEvent;
import hs.opcnetty.opc.event.UnRegisterEvent;
import hs.opcnetty.opc.event.WriteEvent;
import hs.opcnetty.opcproxy.command.CommandImp;
import hs.opcnetty.opcproxy.session.Session;
import hs.opcnetty.opcproxy.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 15:45
 */
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

    private Map<String, MeasurePoint> registeredMeasurePoint = new ConcurrentHashMap();
    private Map<String, MeasurePoint> waittoregistertag = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();

    private int reconnectcount = 0;

    public synchronized void addwaitaddIteambuf(MeasurePoint m) {
        waittoregistertag.put(m.getPoint().getTag(), m);
    }

    public boolean addOPCEvent(Event event) {
        return eventLinkedBlockingQueue.offer(event);
    }


    public OpcExecute(String function, OpcServeInfo serveInfo, String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid, SessionManager sessionManager) {
        this.function = function;
        this.serveInfo = serveInfo;
        this.exename = System.getProperty("user.dir") + "\\" + exename;
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
            setReconnectcount(1);
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info("********" + opcsevename + opcseveip + " connect success");
                    for (MeasurePoint measurePoint : registeredMeasurePoint.values()) {
                        sendAddItemCmd(measurePoint.getPoint().getTag());
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
//            registeredMeasurePoint.clear();

        } else {
            logger.info(" connect status is hold");
        }
    }


    public synchronized void reconnect() {
        logger.info("**** reconnect " + function);
        if (!isOpcServeOnline()) {
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info(opcsevename + opcseveip + " reconnect success");

                    if (waittoregistertag.size() != registeredMeasurePoint.size()) {
                        for (MeasurePoint measurePoint : waittoregistertag.values()) {
                            sendAddItemCmd(measurePoint.getPoint().getTag());
                        }
                    } else if (registeredMeasurePoint.size() > 0) {
//                        for(MeasurePoint measurePoint:registeredMeasurePoint.values()){
//                            sendAddItemCmd(measurePoint.getPoint().getTag());
//                        }
                        sendPatchAddItemCmd(registeredMeasurePoint.values());
                    }

                    break;
                } else {
                    logger.info("try reconnect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }
//            registeredMeasurePoint.clear();

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
            MeasurePoint measurePoint = registeredMeasurePoint.get(key);
            if (measurePoint != null) {
                float value = datajson.getFloatValue(key);
                measurePoint.setValue(value);
                measurePoint.setInstant(Instant.now());
            }
        }


    }


    public void sendWriteItemCmd(String tag, float value) {

        JSONArray jsonArray = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        msg.put("value", value);
        jsonArray.add(msg);
        try {
            sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                    CommandImp.WRITE.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

    }


    public void sendRemoveItemCmd(String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                    CommandImp.REMOVEITEM.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void dealRemoveResult(JSONObject datajson) {
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            if (1 == datajson.getInteger(key)) {
                registeredMeasurePoint.remove(key);
            }
        }
    }

    public boolean sendAddItemCmd(String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            if (sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx() != null) {
                sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                        CommandImp.ADDITEM.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
                );
            } else {
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
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
                MeasurePoint point = waittoregistertag.get(key);
                registeredMeasurePoint.put(key, point);
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
                            if ((event != null) && (event.getPoint() != null)) {
                                if (event instanceof WriteEvent) {
                                    if (registeredMeasurePoint.containsKey(event.getPoint().getTag())) {
                                        WriteEvent writeevent = (WriteEvent) event;
                                        sendWriteItemCmd(writeevent.getPoint().getTag(), writeevent.getValue());

                                    }
                                } else if (event instanceof RegisterEvent) {
                                    RegisterEvent registerevent = (RegisterEvent) event;
                                    if (!registeredMeasurePoint.containsKey(registerevent.getPoint().getTag())) {
                                        MeasurePoint measurePoint = new MeasurePoint();
                                        measurePoint.setPoint(registerevent.getPoint());

                                        waittoregistertag.put(registerevent.getPoint().getTag(), measurePoint);
                                        if (sendAddItemCmd(registerevent.getPoint().getTag())) {
                                            eventLinkedBlockingQueue.offer(event);
                                        }
                                        ;
                                        logger.info("***** even " + registerevent.getPoint().getTag());
                                    }
                                } else if (event instanceof UnRegisterEvent) {
                                    if (registeredMeasurePoint.containsKey(event.getPoint().getTag())) {
                                        UnRegisterEvent unregisterevent = (UnRegisterEvent) event;
                                        sendRemoveItemCmd(unregisterevent.getPoint().getTag());
                                    }

                                }
                            }
                        }
                    }
                }


                synchronized (this) {
                    if (registeredMeasurePoint.size() > 0 && function.equals(OpcExecute.FUNCTION_READ)) {
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

    public ExecutePythonBridge getExecutePythonBridge() {
        return executePythonBridge;
    }

    public Map<String, MeasurePoint> getRegisteredMeasurePoint() {
        return registeredMeasurePoint;
    }

    public synchronized int getReconnectcount() {
        return reconnectcount;
    }

    public synchronized void setReconnectcount(int reconnectcount) {
        this.reconnectcount = reconnectcount;
    }

    public synchronized void minsReconnectcount() {
        reconnectcount--;
    }
}
