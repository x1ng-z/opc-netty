package hs.opcnetty.opcproxy.session;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:04
 */
@Component
public class SessionManager {
    private Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private Map<ChannelHandlerContext, Session> modulepool = new ConcurrentHashMap<>();

    public synchronized void addSessionModule(long nodeid, String function, ChannelHandlerContext ctx) {

        //check expried connect.
        List<ChannelHandlerContext> expriedconns = new ArrayList<>();
        for (Map.Entry<ChannelHandlerContext, Session> entry : modulepool.entrySet()) {
            if (!entry.getKey().equals(ctx)) {
                if ((entry.getValue().getOpcserveid() == nodeid) && (entry.getValue().getFunction().equals(function))) {
                    expriedconns.add(ctx);
                }
            }
        }
        for (ChannelHandlerContext expctx : expriedconns) {
            modulepool.remove(expctx);
        }
        if (!modulepool.containsKey(ctx)) {
            Session session = new Session();
            session.setCtx(ctx);
            session.setFunction(function);
            session.setOpcserveid(nodeid);
            modulepool.put(ctx, session);
        }
    }


    public synchronized Session removeSessionModule(ChannelHandlerContext ctx) {
        if (ctx != null) {
            return modulepool.remove(ctx);
        }
        return null;
    }

    public synchronized Session getSpecialSession(long opcserveid, String function) {
        for (Session session : modulepool.values()) {
            if (session.getOpcserveid() == opcserveid && session.getFunction().equals(function)) {
                return session;
            }
        }
        return null;
    }

    public Map<ChannelHandlerContext, Session> getModulepool() {
        return modulepool;
    }
}
