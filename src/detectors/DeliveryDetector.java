package detectors;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class DeliveryDetector extends BasicDetectionAlgorithm {

    private final String modality = "";
    private Map<String, Object> detectorParams = new HashMap<>();

    @Override
    public void init() {
        super.init();
        detectorParams.put("mode", "delivery");
    }

    @Override
    public double detect() {
        boolean delivery = (Boolean) sensoryMemory.getSensoryContent(modality, detectorParams);
        double activation = 0.0;
        if (delivery) {
            activation = 1.0;
        }
        return activation;
    }
}
