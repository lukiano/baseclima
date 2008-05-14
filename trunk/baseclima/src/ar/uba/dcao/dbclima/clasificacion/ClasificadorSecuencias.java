package ar.uba.dcao.dbclima.clasificacion;

import java.awt.Point;

import org.joone.helpers.factory.JooneTools;
import org.joone.io.MemoryInputSynapse;
import org.joone.io.MemoryOutputSynapse;
import org.joone.net.NeuralNet;

public class ClasificadorSecuencias {

  private static final boolean SYNC = true;

  private NeuralNet som;

  private int mapWidth;

  private int mapHeight;

  /**
   * @param mapWidth
   *          Width of the output map.
   * @param mapHeight
   *          Height of the output map.
   * @param charVectorDim
   *          Dimension of the char vector.
   */
  public ClasificadorSecuencias(int mapWidth, int mapHeight, int charVectorDim) {
    this.som = buildSOM(mapWidth, mapHeight, charVectorDim);
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
  }

  public double[][] train(double[][] dataSet, int epochs) {
    int trainingPatternsNumber = dataSet.length;

    /* Configure net for given input. */
    MemoryInputSynapse input = (MemoryInputSynapse) this.som.getAllInputs().get(0);
    input.setInputArray(dataSet);
    this.som.getMonitor().setTrainingPatterns(trainingPatternsNumber);

    /* Configure learning params. */
    this.som.getMonitor().setTotCicles(epochs);
    this.som.getMonitor().setLearning(true);

    MemoryOutputSynapse output = (MemoryOutputSynapse) this.som.getAllOutputs().get(0);

    double[][] rv = new double[trainingPatternsNumber][];
    this.som.go(SYNC);
    for (int i = 0; i < trainingPatternsNumber; ++i) {
      rv[i] = output.getNextPattern();
    }
    this.som.stop();

    return rv;
  }

  public Point query(double[] example) {
    MemoryInputSynapse input = (MemoryInputSynapse) this.som.getAllInputs().get(0);

    double[][] inputData = { example };
    input.setInputArray(inputData);

    /* Configure for quering. */
    this.som.getMonitor().setLearning(false);
    this.som.getMonitor().setTotCicles(1);

    /* Run. */
    this.som.go(SYNC);

    /* Get output. */
    MemoryOutputSynapse output = (MemoryOutputSynapse) this.som.getAllOutputs().get(0);
    double[] outData = output.getNextPattern();

    return getActiveNeuron(outData);
  }

  /**
   * Build self Organizing Map used in the classification.
   */
  private static NeuralNet buildSOM(int width, int height, int dim) {
    NeuralNet nnet;

    int[] params = { dim, width, height };
    nnet = JooneTools.create_unsupervised(params, JooneTools.WTA);

    /* Column selector. */
    String colSel = (dim > 1) ? "1-" + dim : "1";

    /* INPUT */
    MemoryInputSynapse inputStream = new MemoryInputSynapse();
    inputStream.setAdvancedColumnSelector(colSel);
    inputStream.setFirstRow(1);
    inputStream.setLastRow(0);

    nnet.getInputLayer().removeAllInputs();
    nnet.getInputLayer().addInputSynapse(inputStream);

    /* OUTPUT */
    MemoryOutputSynapse output = new MemoryOutputSynapse();
    nnet.getOutputLayer().removeAllOutputs();
    nnet.getOutputLayer().addOutputSynapse(output);

    return nnet;
  }

  private Point getActiveNeuron(double[] neurons) {
    int rvI = -1;
    for (int i = 0; i < neurons.length; i++) {
      if (neurons[i] == 1d) {
        rvI = i;
        break;
      }
    }

    int x = rvI % this.mapWidth;
    int y = rvI / this.mapHeight;

    return new Point(x, y);
  }
}
