package modules;

import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ws3dproxy.model.Thing;

public class SensoryMemory extends SensoryMemoryImpl {

    private Map<String, Object> sensorParam;
    private Thing wallFront;
    private Thing thingDirection;
    private Thing thingCollided;
    private Thing thingToGet;
    private boolean random;
    private boolean rotate;
    private boolean delivery;

    public SensoryMemory() {
        this.sensorParam = new HashMap<>();
        this.thingCollided = null;
        this.wallFront = null;
        this.thingDirection = null;
        this.thingToGet = null;
        this.wallFront = null;
        this.random = false;
        this.rotate = false;
        this.delivery = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void runSensors() {
        sensorParam.clear();
        sensorParam.put("mode", "thingDirection");
        thingDirection = (Thing) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "thingToGet");
        thingToGet = (Thing) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "thingCollided");
        thingCollided = (Thing) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "wallFront");
        wallFront = (Thing) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "random");
        random = (Boolean) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "rotate");
        rotate = (Boolean) environment.getState(sensorParam);
        
        sensorParam.clear();
        sensorParam.put("mode", "delivery");
        delivery = (Boolean) environment.getState(sensorParam);
    }

    @Override
    public Object getSensoryContent(String modality, Map<String, Object> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
            case "thingDirection":
                requestedObject = thingDirection;
                break;

            case "thingCollided":
                requestedObject = thingCollided;
                break;

            case "thingToGet":
                requestedObject = thingToGet;
                break;

            case "wallFront":
                requestedObject = wallFront;
                break;

            case "random":
                requestedObject = random;
                break;

            case "delivery":
                requestedObject = delivery;
                break;

            case "rotate":
                requestedObject = rotate;
                break;

            default:
                break;
        }
        return requestedObject;
    }

    @Override
    public Object getModuleContent(Object... os) {
        return null;
    }

    @Override
    public void decayModule(long ticks) {
    }
}
