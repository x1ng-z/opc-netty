package hs.opcnetty.opc.event;


import hs.opcnetty.opc.OpcExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 16:15
 */
public interface Event {
    public Logger logger = LoggerFactory.getLogger(Event.class);
    void execute(OpcExecute opcExecute);

}
