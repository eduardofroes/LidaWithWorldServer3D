package modules;

import edu.memphis.ccrg.lida.environment.EnvironmentImpl;
import edu.memphis.ccrg.lida.framework.tasks.FrameworkTaskImpl;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
    private String previousAction;
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
        this.previousAction = "";
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

        List<Thing> thingsSorted = new ArrayList<>();
        List<Thing> thingsInVision = creature.getThingsInVision();

        Collections.sort(thingsInVision, (t1, t2) -> Double.compare(creature.calculateDistanceTo(t1), creature.calculateDistanceTo(t2)));

        thingsSorted.addAll(thingsInVision);
        thingsSorted.addAll(buriedThings);

        thingToGet = null;
        thingCollided = null;
        wallFront = null;
        delivery = false;
        rotate = false;
        random = false;

        double gap = 60;

        if (!verifyIfLeafletIsCompleted(creature.getLeaflets())) {
            for (Thing thing : thingsSorted) {
                if (thing.getCategory() != Constants.categoryBRICK
                        && thing.getCategory() != Constants.categoryDeliverySPOT) {

                    if (creature.calculateDistanceTo(thing) <= gap) {
                        if ((thing.getCategory() == Constants.categoryPFOOD || thing.getCategory() == Constants.categoryNPFOOD)) {
                            if (creature.getFuel() <= 400) {
                                thingToGet = thing;
                            } else {
                                if (thing.hidden == false) {
                                    thingCollided = thing;
                                }
                            }
                            break;
                        } else {
                            if (thing.getCategory() == Constants.categoryJEWEL) {
                                if (jewelInLeaflet(thing)) {
                                    thingDirection = null;
                                    thingToGet = thing;
                                } else {
                                    thingCollided = thing;
                                }
                                break;
                            }
                        }
                    } else {
                        if ((thing.getCategory() == Constants.categoryPFOOD || thing.getCategory() == Constants.categoryNPFOOD)) {
                            if (creature.getFuel() <= 400) {
                                thingDirection = thing;
                                break;
                            }


                        } else if (thing.getCategory() == Constants.categoryJEWEL) {
                            if (jewelInLeaflet(thing)) {
                                thingDirection = thing;
                                break;
                            }
                        }
                    }
                } else {

                    double distance = creature.calculateDistanceTo(thing);

                    if (distance <= gap) {
                        wallFront = thing;
                        break;
                    }

                }

            }
        } else {

            for (Thing t : thingsSorted) {
                if (creature.calculateDistanceTo(t) <= 65) {
                    if (t.getCategory() == Constants.categoryBRICK || t.getCategory() == Constants.categoryDeliverySPOT) {
                        wallFront = t;
                        thingDirection = null;
                        break;
                    } else {
                        if ((t.getCategory() == Constants.categoryPFOOD || t.getCategory() == Constants.categoryNPFOOD)) {
                            if (creature.getFuel() <= 400) {
                                thingToGet = t;
                            } else {
                                if (t.hidden == false)
                                    thingCollided = t;
                            }
                            break;
                        } else {
                            if (t.getCategory() == Constants.categoryJEWEL) {
                                if (jewelInLeaflet(t)) {
                                    thingToGet = t;
                                } else {
                                    thingCollided = t;
                                }
                            }
                            break;
                        }
                    }
                }
            }

            if (thingCollided == null && thingToGet == null) {
                if (creature.calculateDistanceTo(deliverySpot) <= 65) {
                    delivery = true;
                } else {
                    thingDirection = deliverySpot;
                }
            }
        }

        if (wallFront == null
                && thingToGet == null
                && thingDirection == null
                && thingCollided == null
                && delivery == false) {
            if (randomMovement()) {
                random = true;
            } else {
                rotate = true;
            }

        }
    }

    public List<Thing> organizeThings(List<Thing> things) {

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
            int randValue = rand.nextInt(2);
            double angle = randomAngle(randValue);

            switch (currentAction) {

                case "rotate":
                    creature.rotate(3);
                    rotate = false;
                    break;

                case "random":
                    if (!previousAction.equals(currentAction)) {
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

                        random = false;
                    }
                    break;


                case "avoid":

                    if (!previousAction.equals(currentAction)) {
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
                        wallFront = null;
                    }

                    break;

                case "move":
                    if (thingDirection != null) {
                        creature.moveto(3, thingDirection.getX1(), thingDirection.getY1());
                    }
                    break;

                case "delivery":
                    if (!previousAction.equals(currentAction)) {
                        for (Leaflet leaflet : creature.getLeaflets()) {
                            try {
                                if (leaflet.isCompleted()) {
                                    creature.deliverLeaflet(leaflet.getID().toString());
                                }
                            } catch (Exception e) {

                            }
                        }
                        delivery = false;
                    }
                    break;

                case "bury":
                    if (!previousAction.equals(currentAction)) {
                        if (thingCollided != null) {
                            if (thingCollided.getCategory() == Constants.categoryNPFOOD || thingCollided.getCategory() == Constants.categoryPFOOD) {
                                thingCollided.hidden = true;
                                buriedThings.add(thingCollided);
                                creature.hideIt(thingCollided.getName());
                            } else {
                                creature.hideIt(thingCollided.getName());
                            }

                            thingCollided = null;
                        }
                    }
                    break;

                case "get":
                    if (!previousAction.equals(currentAction)) {
                        if (thingToGet != null) {
                            creature.move(0.0, 0.0, 0.0);

                            if (thingToGet.getCategory() == Constants.categoryJEWEL) {
                                creature.putInSack(thingToGet.getName());
                            } else if (thingToGet.getCategory() == Constants.categoryNPFOOD || thingToGet.getCategory() == Constants.categoryPFOOD) {
                                if (buriedThings.stream().filter(t -> t.getName().equals(thingToGet.getName())).findAny().orElse(null) != null) {
                                    buriedThings.removeIf(t -> t.getName().equals(thingToGet.getName()));
                                }

                                creature.eatIt(thingToGet.getName());
                            }

                            thingDirection = null;
                            thingToGet = null;
                            this.resetState();
                        }
                    }
                    break;
                default:
                    break;
            }

            previousAction = currentAction;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
