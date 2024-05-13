/*
 * Code generated by MIMOS tool.
 * Date: 2024/03/21 17:28:54
 * 
 * Input:
 * - Robot's control inputs: distance traveled by the robot and its rotation
 * 
 * Output:
 * - Robot's state: x, y, theta
 */

import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

public class Predict {
    /* Inputs: */
    double[] input3; // Control inputs
    double[] input1; // State of the robot and Landmarks
    double[] input2; // Matrix P

    /* Outputs: */
    double[] output1; // X
    double[] output2; // P

    int numLandmarks = 6; // six landmarks
    final int STATE_ENTRIES = 3; // X, Y, Theta
    final int NUM_MEAS_TYPE = 2; // Range and bearing angle
    int numEntries;

    double[] robotState = new double[3];
    double[][] G = new double[3][3];
    double[][] GT = new double[3][3];
    double[][] L = new double[3][3];
    double[][] LT = new double[3][3];

    double[][] P;
    double[][] controlCov;
    double[][] measurementCov;

    public Predict() {
        // Control covariance matrix
        // Uncertainty parameters
        double sigX2 = 0.25 * 0.25;
        double sigY2 = 0.1 * 0.1;
        double sigAlpha2 = 0.1 * 0.1; // Rotation

        controlCov = new double[STATE_ENTRIES][STATE_ENTRIES];
        controlCov[0][0] = sigX2;
        controlCov[1][1] = sigY2;
        controlCov[2][2] = sigAlpha2;

    }

    /* Node computation: */
    public boolean run() {
        System.out.println("Predict.java: run() is called.");

        numEntries = STATE_ENTRIES + NUM_MEAS_TYPE * numLandmarks;
        output1 = new double[numEntries];
        output2 = new double[numEntries * numEntries];

        // Change input2 back to matrix P
        P = new double[numEntries][numEntries];
        for (int i = 0; i < numEntries; i++) {
            for (int j = 0; j < numEntries; j++) {
                P[i][j] = input2[i * numEntries + j];
            }
        }

        double xT = input1[0], yT = input1[1], thetaT = input1[2];
        robotState[0] = xT + input3[0] * Math.cos(thetaT);
        robotState[1] = yT + input3[0] * Math.sin(thetaT);
        robotState[2] = thetaT + input3[1];

        // Initialize G matrix
        G[0][0] = 1;
        G[0][1] = 0;
        G[0][2] = -input3[0] * Math.sin(thetaT);
        G[1][0] = 0;
        G[1][1] = 1;
        G[1][2] = input3[0] * Math.cos(thetaT);
        G[2][0] = 0;
        G[2][1] = 0;
        G[2][2] = 1;

        matrixTranspose(G, GT, STATE_ENTRIES, STATE_ENTRIES);

        // Initialize L matrix
        L[0][0] = Math.cos(thetaT);
        L[0][1] = -Math.sin(thetaT);
        L[0][2] = 0;
        L[1][0] = Math.sin(thetaT);
        L[1][1] = Math.cos(thetaT);
        L[1][2] = 0;
        L[2][0] = 0;
        L[2][1] = 0;
        L[2][2] = 1;

        matrixTranspose(L, LT, STATE_ENTRIES, STATE_ENTRIES);

        double[][] temp1 = new double[STATE_ENTRIES][STATE_ENTRIES];
        double[][] temp2 = new double[STATE_ENTRIES][STATE_ENTRIES];
        double[][] temp3 = new double[STATE_ENTRIES][STATE_ENTRIES];
        double[][] temp4 = new double[STATE_ENTRIES][STATE_ENTRIES];

        matrixMultiplication(G, P, temp1, STATE_ENTRIES, STATE_ENTRIES, STATE_ENTRIES);
        matrixMultiplication(temp1, GT, temp2, STATE_ENTRIES, STATE_ENTRIES, STATE_ENTRIES);
        matrixMultiplication(L, controlCov, temp3, STATE_ENTRIES, STATE_ENTRIES, STATE_ENTRIES);
        matrixMultiplication(temp3, LT, temp4, STATE_ENTRIES, STATE_ENTRIES, STATE_ENTRIES);
        matrixAddition(temp2, temp4, P, STATE_ENTRIES, STATE_ENTRIES);

        for (int i = 0; i < STATE_ENTRIES; i++) {
            output1[i] = robotState[i];
        }
        for (int i = STATE_ENTRIES; i < numEntries; i++) {
            output1[i] = input1[i];
        }

        
        for (int i = 0; i < numEntries; i++) {
            for (int j = 0; j < numEntries; j++) {
                output2[i * numEntries + j] = P[i][j];
            }
        }

        return true;
    }

    /* Help methods: */
    public static void matrixTranspose(double[][] A, double[][] B, int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                B[j][i] = A[i][j];
            }
        }
    }

    public static void matrixMultiplication(double[][] A, double[][] B, double[][] res, int rowA, int rowB, int colB) {
        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < colB; j++) {
                res[i][j] = 0;
                for (int k = 0; k < rowB; k++) {
                    res[i][j] += A[i][k] * B[k][j];
                }
            }
        }
    }

    public static void matrixAddition(double[][] A, double[][] B, double[][] res, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res[i][j] = A[i][j] + B[i][j];
            }
        }
    }

    /* Setters (for inputs): */
    public void set_input1(double[] i) {
        input1 = i;
    }

    public void set_input2(double[] i) {
        input2 = i;
    }

    public void set_input3(double[] i) {
        input3 = i;
    }

    /* Getters (for outputs): */
    public double[] get_output1() {
        return output1;
    }

    public double[] get_output2() {
        return output2;
    }
}