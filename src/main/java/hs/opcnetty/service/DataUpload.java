package hs.opcnetty.service;

import com.runlion.iot.driver.core.api.plat.dto.PointUploadDTO;
import com.runlion.iot.driver.core.api.plat.vo.PointResultHttpVO;
import com.runlion.iot.driver.sdk.service.impl.IotDriverServiceImpl;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/28 16:03
 */
@Component
public class DataUpload {
    private IotDriverServiceImpl iotDriverService;


    public DataUpload(IotDriverServiceImpl iotDriverService) {
        this.iotDriverService = iotDriverService;
    }


    public PointResultHttpVO pointUpload(PointUploadDTO dto){
        return iotDriverService.pointUpload(dto);
    }

}
