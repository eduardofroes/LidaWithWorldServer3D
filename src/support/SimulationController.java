/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package support;

import com.google.gson.Gson;
import ws3dproxy.model.Creature;
import ws3dproxy.model.World;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author rgudwin
 */
public class SimulationController {

    private Timer t;


    private Random r;

    private Creature creature;
    private Date initDate;

    private int defaultTime = 10;
    private double time = 0;
    private int counterToGenerateThings = 0;

    //private Agent agent;

    private File fileEnergySpent;
    private File fileCreatureScore;
    private File fileDrivesActivation;

    private List<Result> resultEnergySpent;
    private List<Result> resultDrivesActivation;
    private List<Result> resultCreatureScore;



    public SimulationController(String name) {
        String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        setResultEnergySpent(new ArrayList<>());
        setResultDrivesActivation(new ArrayList<>());
        setResultCreatureScore(new ArrayList<>());
        setR(new Random());
        setFileEnergySpent(new File("reportFiles/Lida_EnergySpent" + timeLog + ".txt"));
        setFileCreatureScore(new File("reportFiles/Lida_Score" + timeLog + ".txt"));
        setInitDate(new Date());

    }


    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public Creature getCreature() {
        return this.creature;
    }

    public void StartTimer() {
        setT(new Timer());
        MVTimerTask tt = new MVTimerTask(this);
        getT().scheduleAtFixedRate(tt, 0, 1000);
    }

    public synchronized void tick() {

        if ((getCounterToGenerateThings() /60) == 1) {
            createObjectsInWorld();
            setCounterToGenerateThings(0);
        }

        reportEnergySpent(getTime());
        reportCreatureScore(getTime());

        if((getTime() / 60) == getDefaultTime()){
            finalizeReport("Creature's Energy", "Time", "Energy", getResultEnergySpent(), getFileEnergySpent());
            finalizeReport("Creature's Score", "Time", "Score", getResultCreatureScore(), getFileCreatureScore());
            getT().cancel();
            getT().purge();
            //agent.stop();
        }

        setCounterToGenerateThings(getCounterToGenerateThings() + 1);
        setTime(getTime() + 1);
    }

    private void createObjectsInWorld(){
        try {
            Random random = new Random();
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
            World.createJewel(random.nextInt(6), getR().nextInt(800), getR().nextInt(600));
            World.createFood(random.nextInt(2), getR().nextInt(800), getR().nextInt(600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkTimeStop() {

        boolean bFinish = false;

        if ((double) ((new Date()).getTime() - getInitDate().getTime()) >= this.getDefaultTime() * 60 * Math.pow(10, 3)) {
            bFinish  = true;
        }

        return bFinish;
    }

    private void reportEnergySpent(double time) {
        this.getResultEnergySpent().add(new Result("Energy Spent", time, getCreature().getFuel()));
    }

    private void reportCreatureScore(double time) {
        this.getResultCreatureScore().add(new Result("Score Obtained", time, getCreature().s.score));
    }

    private void finalizeReport(String graphName, String xTitle, String yTitle, List<Result> results, File file) {
        Gson gson = new Gson();
        String sResults = gson.toJson(new Graph(graphName, xTitle, yTitle, results));
        writeInFile(sResults, file);
    }

    private void writeInFile(String line, File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(line);
            System.out.println(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Result> getResultEnergySpent() {
        return resultEnergySpent;
    }

    public void setResultEnergySpent(List<Result> resultEnergySpent) {
        this.resultEnergySpent = resultEnergySpent;
    }

    public List<Result> getResultDrivesActivation() {
        return resultDrivesActivation;
    }

    public void setResultDrivesActivation(List<Result> resultDrivesActivation) {
        this.resultDrivesActivation = resultDrivesActivation;
    }

    public List<Result> getResultCreatureScore() {
        return resultCreatureScore;
    }

    public void setResultCreatureScore(List<Result> resultCreatureScore) {
        this.resultCreatureScore = resultCreatureScore;
    }

    public Timer getT() {
        return t;
    }

    public void setT(Timer t) {
        this.t = t;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public int getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(int defaultTime) {
        this.defaultTime = defaultTime;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getCounterToGenerateThings() {
        return counterToGenerateThings;
    }

    public void setCounterToGenerateThings(int counterToGenerateThings) {
        this.counterToGenerateThings = counterToGenerateThings;
    }

    public File getFileEnergySpent() {
        return fileEnergySpent;
    }

    public void setFileEnergySpent(File fileEnergySpent) {
        this.fileEnergySpent = fileEnergySpent;
    }

    public File getFileCreatureScore() {
        return fileCreatureScore;
    }

    public void setFileCreatureScore(File fileCreatureScore) {
        this.fileCreatureScore = fileCreatureScore;
    }

    public File getFileDrivesActivation() {
        return fileDrivesActivation;
    }

    public void setFileDrivesActivation(File fileDrivesActivation) {
        this.fileDrivesActivation = fileDrivesActivation;
    }

}
