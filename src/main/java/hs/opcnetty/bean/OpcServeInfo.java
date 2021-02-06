package hs.opcnetty.bean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:37
 */
public class OpcServeInfo {
    /**opc serve id 不同的opc此id不可相同*/
    private int serveid;
    /**opc serve name*/
    private String servename;
    /**opc serve ip*/
    private String serveip;

    public int getServeid() {
        return serveid;
    }

    public void setServeid(int serveid) {
        this.serveid = serveid;
    }

    public String getServename() {
        return servename;
    }

    public void setServename(String servename) {
        this.servename = servename;
    }

    public String getServeip() {
        return serveip;
    }

    public void setServeip(String serveip) {
        this.serveip = serveip;
    }
}
