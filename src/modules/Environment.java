package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;

import java.awt.*;
import java.util.*;
import java.util.List;

import support.SimulationController;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

public class Environment extends EnvironmentImpl {

    private static final int DEFAULT_TICKS_PER_RUN = 100;

    private WS3DProxy proxy;
    private Creature creature;
    private List<Leaflet> lstLeaflet;
    private List<Thing> buriedThings;
    private String currentAction;
    private Thing wallFront;
    private Thing deliverySpot;
    private Thing thingDirection;
    private Thing thingCollided;
    private Thing thingToGet;
    private World w;
    private int width;
    private int height;
    private int ticksPerRun;
    public double previousX;
    public double previousY;
    private boolean random;
    private boolean rotate;
    private boolean delivery;
    public Date previousDate;

    public Environment() {
        this.ticksPerRun = DEFAULT_TICKS_PER_RUN;
        this.proxy = new WS3DProxy();
        this.creature = null;
        this.thingDirection = null;
        this.wallFront = null;
        this.currentAction = "rotate";
        this.width = 800;
        this.height = 600;
        this.previousX = 100;
        this.previousY = 450;
        this.random = false;
        this.rotate = false;
        this.delivery = false;
        this.previousDate = null;
        this.buriedThings = new ArrayList<>();
    }

    @Override
    public void init() {
        super.init();
        ticksPerRun = (Integer) getParam("environment.ticksPerRun", DEFAULT_TICKS_PER_RUN);
        taskSpawner.addTask(new BackgroundTask(ticksPerRun));

        try {
            System.out.println("Reseting the WS3D World ...");

            w = World.getInstance();
            w.reset();

            creature = proxy.createCreature(100, 450, 0);

            Random rand = new Random();

            World.createFood(rand.nextInt(2), rand.nextInt(width), rand.nextInt(height));
            World.createFood(rand.nextInt(2), rand.nextInt(width), rand.nextInt(height));
            World.createFood(rand.nextInt(2), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createJewel(rand.nextInt(6), rand.nextInt(width), rand.nextInt(height));
            World.createDeliverySpot(rand.nextInt(width), rand.nextInt(height));

            deliverySpot = World.getWorldEntities().stream().filter(x -> x.getCategory() == Constants.categoryDeliverySPOT).findFirst().get();

            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 20, y + 20);

            x = rand.nextInt(width);
            y = rand.nextInt(height);
            World.createBrick(4, x, y, x + 20, y + 20);

            creature.start();

            previousDate = new Date();

            // Create and Populate SimulationController
            SimulationController simulationController = new SimulationController("SimulationController");
            simulationController.setCreature(creature);
            simulationController.StartTimer();

            System.out.println("DemoLIDA has started...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the currentAction
     */
    public String getCurrentAction() {
        return currentAction;
    }

    /**
     * @param currentAction the currentAction to set
     */
    public void setCurrentAction(String currentAction) {
        this.currentAction = currentAction;
    }

    /**
     * @return the leaflet
     */
    public List<Leaflet> getLstLeaflet() {
        return lstLeaflet;
    }

    /**
     * @param leaflet the leaflet to set
     */
    public void setLstLeaflet(List<Leaflet> leaflet) {
        this.lstLeaflet = leaflet;
    }

    private class BackgroundTask extends FrameworkTaskImpl {

        public BackgroundTask(int ticksPerRun) {
            super(ticksPerRun);
        }

        @Override
        protected void runThisFrameworkTask() {
            updateEnvironment();
            performAction(getCurrentAction());
        }
    }

    @Override
    public void resetState() {
        setCurrentAction("rotate");
    }

    @Override
    public Object getState(Map<String, ?> params) {
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

    public void updateEnvironment() {
        creature.updateState();

        thingDirection = null;
        thingToGet = null;
        thingCollided = null;
        wallFront = null;
        random = false;
        delivery = false;
        rotate = false;

        List<Thing> things = orderByDistance(creature.getThingsInVision());

        if (!verifyIfLeafletIsCompleted(creature.getLeaflets())) {
            for (Thing thing : things) {
                if (thing.getCategory() != Constants.categoryBRICK
                        && thing.getCategory() != Constants.categoryDeliverySPOT) {

                    if (creature.calculateDistanceTo(thing) <= Constants.OFFSET) {
                        if ((thing.getCategory() == Constants.categoryPFOOD || thing.getCategory() == Constants.categoryNPFOOD)) {

                            if (creature.getFuel() <= 400) {
                                thingToGet = thing;
                            } else {
                                thingCollided = thing;
                            }

                        } else {
                            if (thing.getCategory() == Constants.categoryJEWEL) {
                                if (jewelInLeaflet(thing)) {
                                    thingToGet = thing;
                                } else {
                                    thingCollided = thing;
                                }
                            }
                        }
                    } else {
                        if ((thing.getCategory() == Constants.categoryPFOOD || thing.getCategory() == Constants.categoryNPFOOD)) {
                            if (creature.getFuel() <= 400) {
                                thingDirection = thing;
                            }

                        } else if (thing.getCategory() == Constants.categoryJEWEL) {
                            if (jewelInLeaflet(thing)) {
                                thingDirection = thing;
                            }
                        }
                    }
                } else {

                    double distance = creature.calculateDistanceTo(thing);
                    double gap = 65;

                    if (distance <= gap) {
                        wallFront = thing;
                    }

                }
                break;
            }
        } else {

            things.forEach(t -> {
                if (creature.calculateDistanceTo(deliverySpot) <= 65) {
                    if (t.getCategory() == Constants.categoryBRICK) {
                        thingCollided = t;
                        return;
                    }
                }
            });

            if (thingCollided == null) {
                if (creature.calculateDistanceTo(deliverySpot) <= 65) {
                    delivery = true;
                } else {
                    thingDirection = deliverySpot;
                }
            }
        }

        if (wallFront == null
                && thingDirection == null
                && thingCollided == null
                && delivery == false) {
            if(randomMovement()){
                random = true;
            } else {
                rotate = true;
            }

        }
    }

    public List<Thing> orderByDistance(List<Thing> things) {

        Comparator<Thing> comparator = new Comparator<Thing>() {
            @Override
            public int compare(Thing thing1, Thing thing2) {
                int nearThing = creature.calculateDistanceTo(thing2) < creature.calculateDistanceTo(thing1) ? 1 : 0;
                return nearThing;
            }
        };
        Collections.sort(things, comparator);

        return things;
    }

    private boolean randomMovement() {

        Date now = new Date();

        if (previousX != creature.getPosition().getX() && previousY != creature.getPosition().getY()) {
            previousX = creature.getPosition().getX();
            previousY = creature.getPosition().getY();
            previousDate = now;
            return false;
        } else {

            double diff = now.getTime() - previousDate.getTime();

            if (diff >= 20000) {
                previousX = creature.getPosition().getX();
                previousY = creature.getPosition().getY();
                previousDate = now;
                return true;
            } else {
                return false;
            }
        }
    }

    private double randomAngle(int random) {
        if (random == 1) {
            return (creature.getPitch() + Math.toRadians(-90));
        } else {
            return (creature.getPitch() + Math.toRadians(90));
        }
    }

    private boolean verifyIfLeafletIsCompleted(List<Leaflet> leaflets) {

        if (leaflets.stream().filter(l -> l.isCompleted()).findAny().orElse(null) != null)
            return true;
        else {
            return false;
        }

    }

    private boolean jewelInLeaflet(Thing jewel) {

        for (Leaflet l : creature.getLeaflets()) {
            if (l.ifInLeaflet(jewel.getMaterial().getColorName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void processAction(Object action) {
        String actionName = (String) action;
        setCurrentAction(actionName.substring(actionName.indexOf(".") + 1));
    }


    private void performAction(String currentAction) {
        try {
            Random rand = new Random();
            switch (currentAction) {

                case "rotate":
                    creature.rotate(3);
                    break;

                case "random":

                    int targetx = rand.nextInt(800);
                    int targety = rand.nextInt(600);
                    creature.moveto(3, targetx, targety);
                    break;

                case "avoid":

                    int random = rand.nextInt(2);
                    double angle = randomAngle(random);

                    creature.move(-3, -3, creature.getPitch());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    creature.rotate(3);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    creature.move(3, 3, angle);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                case "move":
                    creature.moveto(3, thingDirection.getX1(), thingDirection.getY1());
                    break;

                case "delivery":
                    for (Leaflet leaflet : creature.getLeaflets()) {
                        try {
                            if (leaflet.isCompleted()) {
                                creature.deliverLeaflet(leaflet.getID().toString());
                            }
                        } catch (Exception e) {

                        }
                    }
                    break;

                case "bury":
                    if (thingCollided.getCategory() == Constants.categoryNPFOOD || thingCollided.getCategory() == Constants.categoryPFOOD){
                        buriedThings.add(thingCollided);
                        creature.hideIt(thingToGet.getName());
                    } else {
                        creature.hideIt(thingToGet.getName());
                    }

                    break;

                case "get":
                    creature.move(0.0, 0.0, 0.0);

                    if (thingToGet.getCategory() == Constants.categoryJEWEL) {
                        creature.putInSack(thingToGet.getName());
                    } else if (thingToGet.getCategory() == Constants.categoryNPFOOD || thingToGet.getCategory() == Constants.categoryPFOOD) {
                        if(buriedThings.stream().filter(t -> t.getName().equals(thingToGet.getName())).findAny().orElse(null) != null)
                        {
                            buriedThings.removeIf(t -> t.getName().equals(thingToGet.getName()));
                        }

                        creature.eatIt(thingToGet.getName());
                    }


                    this.resetState();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
