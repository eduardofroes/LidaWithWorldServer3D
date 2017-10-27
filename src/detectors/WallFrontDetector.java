/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package detectors;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;
import java.util.HashMap;
import java.util.Map;
import ws3dproxy.model.Thing;

/**
 *
 * @author Du
 */
public class WallFrontDetector extends BasicDetectionAlgorithm {

    private final String modality = "";
    private Map<String, Object> detectorParams = new HashMap<>();

    @Override
    public void init() {
        super.init();
        detectorParams.put("mode", "wallFront");
    }
    
    @Override
    public double detect() {
        Thing wallFront = (Thing) sensoryMemory.getSensoryContent(modality, detectorParams);
        double activation = 0.0;
        if (wallFront != null) {
            activation = 1.0;
        }
        return activation;
    }
    
}
