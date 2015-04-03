/* MOD_V2.0
 * Copyright (c) 2012 OpenDA Association
 * All rights reserved.
 *
 * This file is part of OpenDA.
 *
 * OpenDA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * OpenDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenDA.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openda.application.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.openda.application.ApplicationRunner;
import org.openda.utils.InstanceStore;
import org.openda.utils.Results;

@SuppressWarnings("serial")
public class ApplicationScreen extends JFrame implements ActionListener {
   private final String                    pauseText    = "Pause";
   private final String                    resumeText   = "Resume";

   JMenuBar                                menubar      = null;
   JMenu                                   file         = null;
   JMenuItem                               fileOpen     = null;
   JMenuItem                               fileSave     = null;
   JMenuItem                               fileSaveAs   = null;
   JMenuItem                               fileExit     = null;
   WindowExitHandler                       exitHandler  = null;
   JMenu                                   control      = null;
   JMenuItem                               controlStart = null;
   JMenuItem                               controlPause = null;
   JMenuItem                               controlStop  = null;
   // Toolbar
   JToolBar                                bar          = null;
   JButton                                 openButton   = null;
   JButton                                 saveButton   = null;
   JButton                                 startButton  = null;
   JButton                                 stopButton   = null;
   JButton                                 pauseButton  = null;

   // Tabs
   JTabbedPane                             tabs         = null;
   JPanel                                  inputTab     = null;
   JPanel                                  controlTab   = null;
   OutputGui                               outputTab    = null;
   JPanel                                  plotTab      = null;

   // Input file
   File                                    input        = null;

   private static ApplicationScreen        myScreen     = null;

   private OpenDaUserSettings              openDaUserSettings;
   private static ApplicationRunner.Status lastStatus;

   public ApplicationScreen(File startUpDir, String fileName) {

      super((!fileName.equals("") ? fileName + " - " : "") + "OpenDaApplication");
      setSize(1000, 700);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      this.exitHandler = new WindowExitHandler(this);
      this.addWindowListener(this.exitHandler);
      setLayout(new BorderLayout());

      Results.setRunningInGui(true);

      // add menu
      this.menubar = new JMenuBar();
      this.file = new JMenu("File");
      this.fileOpen = new JMenuItem("Open", KeyEvent.VK_O);
      this.fileSave = new JMenuItem("Save", KeyEvent.VK_S);
      this.fileSaveAs = new JMenuItem("Save as", KeyEvent.VK_A);
      this.fileExit = new JMenuItem("Exit", KeyEvent.VK_X);

      this.fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
      this.fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

      this.file.add(this.fileOpen);
      this.file.add(this.fileSave);
      this.file.setMnemonic(KeyEvent.VK_F);
      // file.add(fileSaveAs);
      this.file.add(this.fileExit);
      this.menubar.add(this.file);
      this.control = new JMenu("Control");
      this.controlStart = new JMenuItem("Start"); // , KeyEvent.VK_F5);
      this.controlPause = new JMenuItem(this.pauseText, KeyEvent.VK_P);
      this.controlStop = new JMenuItem("Stop", KeyEvent.VK_T);

      this.controlStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
      this.controlPause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
      this.controlStop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.SHIFT_MASK));

      this.control.add(this.controlStart);
      this.control.add(this.controlPause);
      this.control.add(this.controlStop);
      this.control.setMnemonic(KeyEvent.VK_C);
      this.menubar.add(this.control);
      this.setJMenuBar(this.menubar);

      // Toolbar
      this.bar = new JToolBar();
      ImageIcon openIcon = new ImageIcon(this.getClass().getResource("Open24.gif"));
      this.openButton = new JButton("Open", openIcon);
      ImageIcon saveIcon = new ImageIcon(this.getClass().getResource("Save24.gif"));
      this.saveButton = new JButton("Save", saveIcon);
      ImageIcon startIcon = new ImageIcon(this.getClass().getResource("Play24.gif"));
      this.startButton = new JButton("Start", startIcon);
      ImageIcon stopIcon = new ImageIcon(this.getClass().getResource("Stop24.gif"));
      this.stopButton = new JButton("Stop", stopIcon);
      ImageIcon pauseIcon = new ImageIcon(this.getClass().getResource("Pause24.gif"));
      this.pauseButton = new JButton("Pause", pauseIcon);
      this.bar.add(this.openButton);
      this.bar.add(this.saveButton);
      this.bar.add(this.startButton);
      this.bar.add(this.stopButton);
      this.bar.add(this.pauseButton);
      this.add(this.bar, BorderLayout.NORTH);

      this.openButton.addActionListener(this);
      this.saveButton.addActionListener(this);
      this.startButton.addActionListener(this);
      this.stopButton.addActionListener(this);
      this.pauseButton.addActionListener(this);

      this.fileOpen.addActionListener(this);
      this.fileSave.addActionListener(this);
      this.fileExit.addActionListener(this);

      this.controlStart.addActionListener(this);
      this.controlStop.addActionListener(this);
      this.controlPause.addActionListener(this);

      this.tabs = new JTabbedPane();
      // add tab 1 : input
      this.inputTab = new InputGui();
      this.openDaUserSettings = new OpenDaUserSettings();
      // do we start from a specified file?
      if (fileName.length() > 0) {
         File possibleInput = new File(startUpDir, fileName);
         if (possibleInput.exists()) {
            this.input = possibleInput;
         }
      }
      // no possible input yet, use previous working dir if available ; else directory where started
      File workingDir = this.openDaUserSettings.getLastUsedDir();
      if (this.input == null) {
         if (workingDir == null) {
            workingDir = startUpDir;
         }
      }
      else {
         workingDir = startUpDir;
         this.openDaUserSettings.setLastUsedDir(workingDir);
      }

      ((InputGui) this.inputTab).setRootFile(workingDir.getAbsolutePath(), fileName);

      this.tabs.add("Input", this.inputTab);

      // add tab 2 : control
      // TODO: refactor the creation of the standard result writers
      // now this happens in ControlGui(), better bring the code over here

      this.outputTab = new OutputGui();
      this.plotTab = new JPanel();
      this.controlTab = new ControlGui(this.openDaUserSettings, this.outputTab, this.plotTab);
      this.tabs.add("Control", this.controlTab);
      // add tab 3 : output
      this.tabs.add("Output", this.outputTab);

      // add tab 4 : plot
      this.tabs.add("Cost function", this.plotTab);
      this.add(this.tabs, BorderLayout.CENTER);

      // UIModel uiModel = new UIModel();
      // MainFrameController mainFrameController =
      //          new MainFrameController(MainFrameController.APPLICATION_TYPE.UNCERTAINTY_GUI_ONLY);
      // MainPanel datoolsPanel = new MainPanel();
      // MainPanelController mainPanelController = new MainPanelController(mainFrameController, datoolsPanel, "");
      // VariationPerParameterTableController variationPerParameterTableController = new
      // VariationPerParameterTableController(mainPanelController, uiModel);

      // TODO: uncertaintyPanel = datoolsPanel.getVariationPerParameterPanel();
      // uncertaintyPanel = datoolsPanel.getPdfDefinitionPanel();
      // mainPanelController.loadUncertaintySpecificationFile("F:\\openda-svn\\openda\\tests\\l21triad\\swanModel\\config\\parameterUncertainties.xml ");
      // mainPanelController.setUncertaintyMethod(Uncertainties.PDF);
      // tabs.add("Uncertainties", uncertaintyPanel);

      myScreen = this;
      lastStatus = null;
      statusChangedHandler(ApplicationRunner.Status.FINISHED);
      ControlGui.statusChangedHandler(ApplicationRunner.Status.FINISHED);

      if (!fileName.equals("")) {
         File inputFile = new File(startUpDir, fileName);
         SelectCases.newInputFile(inputFile);
         InstanceStore.setInputFile(inputFile);
         this.outputTab.fillTableFromFile();
      }
      setVisible(true);
   }

   
   public void actionPerformed(ActionEvent event) {

      Object source = event.getSource();
      ControlGui controlGui = (ControlGui) this.controlTab;

      /*
       * input files
       */
      try {
         if ((source == this.fileOpen) || (source == this.openButton)) {
            File workingDir = this.openDaUserSettings.getLastUsedDir();
            if (workingDir == null) {
               workingDir = new File(".");
            }
            this.input = FileDialog.openInput(workingDir);
            // notify inputPanel TODO
            if (this.input != null) {
               String parent = this.input.getParent();
               String name = this.input.getName();
               this.openDaUserSettings.setLastUsedDir(this.input.getParentFile());
               ((InputGui) this.inputTab).setRootFile(parent, name);
               this.tabs.setSelectedIndex(0); // activate input tab
               setTitle(name + " - OpenDaApplication");

               // reset history of previous run
               lastStatus = null;
               ((ControlGui) this.controlTab).resetOutput();
               statusChangedHandler(ApplicationRunner.Status.FINISHED);
               ControlGui.statusChangedHandler(ApplicationRunner.Status.FINISHED);
               SelectCases.newInputFile(this.input);
               InstanceStore.setInputFile(this.input);
               this.outputTab.fillTableFromFile();
            }
         }
         else if ((source == this.fileSave) || (source == this.saveButton)) {
            ((InputGui) this.inputTab).saveInput();
            Results.putMessage("Files saved");
         }
         else if (source == this.fileExit) {
            this.exitHandler.tryConfirmedExit();
         }
         /*
          * run control
          */
         else if ((source == this.controlStart) || (source == this.startButton)) {
            // check for saving input files
            this.exitHandler.unsavedFilesCheckAndSave();
            // now start
            this.tabs.setSelectedIndex(1); // activate control tab
            Results.setWorkingDir(this.openDaUserSettings.getLastUsedDir());
            controlGui.startRun(this.input);
         }
         else if (source == this.controlStop || (source == this.stopButton)) {
            Results.putProgression("Stopping application");
            controlGui.stopRun();
         }
         else if (source == this.controlPause || (source == this.pauseButton)) {
            if (!(lastStatus == ApplicationRunner.Status.PAUSED)) {
               Results.putProgression("Pauzing application");
            }
            controlGui.pauseOrResumeRun();
         }
      }
      catch (Exception e) {
         Results.putMessage(e.getMessage());
         Results.putProgression(e.getMessage());
         JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error in user-interface", JOptionPane.ERROR_MESSAGE);
      }
   }

   public static void statusChangedHandler(ApplicationRunner.Status status) {
      if (myScreen == null) return;
      myScreen.updateStatusDependentButtons(status);
      lastStatus = status;
   }

   public void updateStatusDependentButtons(ApplicationRunner.Status status) {

      switch (status) {

      case FINISHED:
      case STOPPED:
      case ERROR:
         updateStartStopButtons(false);
         updatePauzedButtonStatus(false, this.pauseText);
         break;

      case RUNNING:
         if (lastStatus != null && lastStatus == ApplicationRunner.Status.PAUSED) {
            Results.putProgression("Resuming application");
         }
         //$FALL-THROUGH$
      case INITIALIZING:
      case INITIALIZED:
         updateStartStopButtons(true);
         updatePauzedButtonStatus(true, this.pauseText);
         break;

      case PAUSED:
         Results.putProgression("Application paused");
         updateStartStopButtons(true);
         updatePauzedButtonStatus(true, this.resumeText);
         break;
      }
   }

   private void updateStartStopButtons(boolean runHasBeenStarted) {
      this.controlStart.setEnabled(!runHasBeenStarted);
      this.startButton.setEnabled(!runHasBeenStarted);
      this.controlStop.setEnabled(runHasBeenStarted);
      this.stopButton.setEnabled(runHasBeenStarted);
   }

   private void updatePauzedButtonStatus(boolean enabled, String text) {
      this.controlPause.setEnabled(enabled);
      this.controlPause.setText(text);
      this.pauseButton.setEnabled(enabled);
      this.pauseButton.setText(text);
   }

   @SuppressWarnings("unused")
   public static void main(String[] arguments) {
      new ApplicationScreen(new File("."), "");
   }
}
