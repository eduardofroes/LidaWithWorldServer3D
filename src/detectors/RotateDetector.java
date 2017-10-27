package detectors;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class RotateDetector extends BasicDetectionAlgorithm {

    private final String modality = "";
    private Map<String, Object> detectorParams = new HashMap<>();

    @Override
    public void init() {
        super.init();
        detectorParams.put("mode", "rotate");
    }

    @Override
    public double detect() {
        boolean rotate = (Boolean) sensoryMemory.getSensoryContent(modality, detectorParams);
        double activation = 0.0;
        if (rotate) {
            activation = 1.0;
        }
        return activation;
    }
}
