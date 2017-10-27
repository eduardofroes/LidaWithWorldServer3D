/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guipanels;

import edu.memphis.ccrg.lida.framework.ModuleName;
import edu.memphis.ccrg.lida.framework.gui.panels.GuiPanelImpl;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import modules.Environment;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/**
 *
 * @author Du
 */
public class StatusPanel extends GuiPanelImpl {

    private Environment environment;
    private HashMap<String, Integer[]> mapLeaflet;

    /**
     * Creates new form StatusPanel
     */
    public StatusPanel() {
        initComponents();

    }

    @Override
    public void initPanel(String[] param) {

        initMapLeaflet();

        environment = (Environment) agent.getSubmodule(ModuleName.Environment);

        if (environment != null) {
            refresh();
        }
    }
    
    
    public void initMapLeaflet(){
        Integer[] a = new Integer[2];
        mapLeaflet = new HashMap<>();
        mapLeaflet.put(Constants.colorBLUE, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorGREEN, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorMAGENTA, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorORANGE, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorRED, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorWHITE, new Integer[]{0,0});
        mapLeaflet.put(Constants.colorYELLOW, new Integer[]{0,0});
    }
    
    
    public void showLeaflet(){
        
        txtLeaflet.setText("");
        
        String logLeaflet = "";
        
        for (Map.Entry<String, Integer[]> jewel : mapLeaflet.entrySet()) {
             logLeaflet += String.format("%1$10s", jewel.getKey()) + "\t #" + jewel.getValue()[0] + "\t #Collected:" + jewel.getValue()[1] + "\n";
        }
        
        txtLeaflet.setText(logLeaflet);
    }

    @Override
    public void refresh() {
        if (environment != null) {
            
            lblActResp.setText(environment.getCurrentAction());
            List<Leaflet> lstLeaflet = environment.getLstLeaflet();

            if (lstLeaflet != null) {
          
                initMapLeaflet();
                
                for (Leaflet leaflet : lstLeaflet) {
                    
                    Map<String, Integer[]> jewels = leaflet.getItems();
                    Integer[] a = new Integer[]{1,0};
                    
                    for (Map.Entry<String, Integer[]> jewel : jewels.entrySet()) {
                        mapLeaflet.put(jewel.getKey(), new Integer[]{
                                       mapLeaflet.get(jewel.getKey())[0] + jewel.getValue()[0], 
                                       mapLeaflet.get(jewel.getKey())[1] + jewel.getValue()[1]
                        });
                    }
                }
                
                showLeaflet();
            } 
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblActResp = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLeaflet = new javax.swing.JTextPane();

        setPreferredSize(new java.awt.Dimension(400, 300));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        jLabel1.setText("Action:");
        jLabel1.setName("lblAction"); // NOI18N

        lblActResp.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        lblActResp.setForeground(new java.awt.Color(204, 0, 0));
        lblActResp.setText("---------");
        lblActResp.setName("lblActResp"); // NOI18N

        txtLeaflet.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        txtLeaflet.setForeground(new java.awt.Color(204, 0, 0));
        jScrollPane1.setViewportView(txtLeaflet);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblActResp)
                .addContainerGap(52, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblActResp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName("lblAction");
        lblActResp.getAccessibleContext().setAccessibleName("lblActResp");
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblActResp;
    private javax.swing.JTextPane txtLeaflet;
    // End of variables declaration//GEN-END:variables
}
