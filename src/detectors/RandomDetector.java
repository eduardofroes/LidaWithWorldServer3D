package detectors;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;
import ws3dproxy.model.Thing;

import java.util.HashMap;
import java.util.Map;

public class RandomDetector extends BasicDetectionAlgorithm {

    private final String modality = "";
    private Map<String, Object> detectorParams = new HashMap<>();

    @Override
    public void init() {
        super.init();
        detectorParams.put("mode", "random");
    }

    @Override
    public double detect() {
        boolean random = (Boolean) sensoryMemory.getSensoryContent(modality, detectorParams);
        double activation = 0.0;
        if (random) {
            activation = 1.0;
        }
        return activation;
    }
}
