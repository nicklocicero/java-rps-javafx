package edu.cnm.deepdive.rps.controller;

import edu.cnm.deepdive.rps.model.Terrain;
import edu.cnm.deepdive.rps.view.TerrainView;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;

public class Controller {

  private static final int STEPS_PER_ITERATION = 100;
  private static final long MAX_SLEEP_PER_ITERATION = 10;
  @FXML
  private Text colorsLabel;
  @FXML
  private ResourceBundle resources;
  @FXML
  private CheckBox fitCheckbox;
  @FXML
  private Text iterationsLable;
  @FXML
  private ScrollPane viewScroller;
  @FXML
  private TerrainView terrainView;
  @FXML
  private Slider speedSlider;
  @FXML
  private Slider mixingSlider;
  @FXML
  private Button start;
  @FXML
  private Button stop;
  @FXML
  private Button reset;

  private double defaultViewHeight;
  private double defaultViewWidth;
  private double fitViewHeight;
  private double fitViewWidth;
  private String iterationFormat;
  private String colorsFormat;
  private Terrain terrain;
  private boolean running;
  private Runner runner;
  private final Object lock = new Object();
  private Timer timer;

  @FXML
  private void initialize() {
    terrain = new Terrain(new Random());
    defaultViewWidth = terrainView.getWidth();
    defaultViewHeight = terrainView.getHeight();
    fitViewWidth = viewScroller.getPrefWidth();
    fitViewHeight = viewScroller.getPrefHeight();
    iterationFormat = iterationsLable.getText();
    colorsFormat = colorsLabel.getText();
    terrainView.setSource(terrain.getCells());
    draw();
    timer = new Timer();
  }

  @FXML
  private void fitView(ActionEvent actionEvent) {
    if (fitCheckbox.isSelected()) {
      terrainView.setWidth(fitViewWidth);
      terrainView.setHeight(fitViewHeight);
    } else {
      terrainView.setWidth(defaultViewWidth);
      terrainView.setHeight(defaultViewHeight);
    }
    if (!running) {
      draw();
    }
  }

  @FXML
  private void start(ActionEvent actionEvent) {
    running = true;
    start.setDisable(true);
    stop.setDisable(false);
    reset.setDisable(true);
    timer.start();
    runner = new Runner();
    runner.start();
  }

  @FXML
  private void stop(ActionEvent actionEvent) {
    running = false;
    runner = null;
    start.setDisable(false);
    stop.setDisable(true);
    reset.setDisable(false);
    timer.stop();
  }

  @FXML
  private void reset(ActionEvent actionEvent) {
    terrain.reset();
    draw();
  }

  private void draw() {
    synchronized (lock) {
      terrainView.draw();
      iterationsLable.setText(String.format(iterationFormat, terrain.getIterations() / 1000));
      colorsLabel.setText(String.format(colorsFormat,
          terrain.getCount()[0], terrain.getCount()[1], terrain.getCount()[2]));
    }
  }

  private class Timer extends AnimationTimer {

    @Override
    public void handle(long now) {
      draw();
    }
  }

  private class Runner extends Thread {

    @Override
    public void run() {
      while (running) {
        synchronized (lock) {
          terrain.mix((terrain.getCells().length / 100 * (int) mixingSlider.getValue()));
          terrain.iterate(STEPS_PER_ITERATION);
        }
        try {
          Thread.sleep(1 + MAX_SLEEP_PER_ITERATION - (long) speedSlider.getValue());
        } catch (InterruptedException e) {
          // DANCE AROUND! (not gonna fix it)
        }
      }
    }
  }
}
