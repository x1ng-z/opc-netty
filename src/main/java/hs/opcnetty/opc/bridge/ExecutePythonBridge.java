package hs.opcnetty.opc.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutePythonBridge {
    private Logger logger = LoggerFactory.getLogger(ExecutePythonBridge.class);
    public Process p = null;
    Thread result = null;
    Thread error = null;
    private String ip;
    private String port;
    private String opcsevename;
    private String opcseveip;
    private String opcsevid;
    private String exename;
    private String function;

    public ExecutePythonBridge(String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid, String function) {
        this.exename = exename;
        this.ip = ip;
        this.port = port;
        this.opcsevename = opcsevename;
        this.opcseveip = opcseveip;
        this.opcsevid = opcsevid;
        this.function = function;

    }

    public boolean stop() {
        if (p != null) {
            p.destroy();
            p = null;
            return true;
        }

        return true;
    }

    public boolean execute() {
        if (p != null) {
            return true;
        }
        try {
            p = Runtime.getRuntime().exec(new String[]{exename, ip,
                    port,
                    opcsevename,
                    opcseveip,
                    opcsevid,
                    function});
            result = new Thread(new InputStreamRunnable(p.getInputStream(), "Result", null));
            result.setDaemon(true);
            result.start();
            error = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream", null));
            error.setDaemon(true);
            error.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("exename " + exename);
            return false;
        }
        return true;
    }

}


