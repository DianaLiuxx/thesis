/*
 * Code generated by MIMOS tool.
 * Date: 2024/03/12 16:55:54
 * 
 * Resample the particles based on their weights.
 * 
 * Inputs: 
 * - Particles before resample. A list of NUMPARTICLES*4 doubles, (x, y, theta, weight).
 * Outputs:
 * - Particles after resample. A list of NUMPARTICLES*4 doubles, (x, y, theta, weight).
 */

import java.util.Random;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
import java.io.IOException;

public class Resample {
    /* Inputs: */
    double[] input1;

    /* Outputs: */
    double[] output1;
    static String outFilePath = "D:\\lxx\\thesis\\cases\\lxx\\pfl0312\\data\\outputs\\particles_resample.txt";

    Random rand = new Random();

    /* Node computation: */
    public boolean run() {
        // System.out.println("[Resample] starts runing.");

        // Low-variance sampling
        double wSum = 0;
        for (int i = 0; i < input1.length / 4; i++) {
            wSum += input1[4 * i + 3];
        }
        for (int i = 0; i < input1.length / 4; i++) {
            input1[4 * i + 3] = input1[4 * i + 3] / wSum;
        }

        output1 = new double[input1.length];
        List<double[]> newParticles = new ArrayList<>();

        double M1 = 1.0 / (input1.length / 4);
        double r = rand.nextDouble() * (4 / input1.length);
        double c = input1[3];
        int i = 0;

        for (int m = 0; m < input1.length / 4; m++) {
            double u = r + m * M1;
            while (u > c) {
                i++;
                c += input1[4 * i + 3]; // Inherently prevent an out-of-bounds error
            }

            double[] newParticle = new double[4];
            newParticle[0] = input1[i * 4];
            newParticle[1] = input1[i * 4 + 1];
            newParticle[2] = input1[i * 4 + 2];
            newParticle[3] = input1[i * 4 + 3];
            newParticles.add(newParticle);
        }

        int ii = 0;
        for (double[] newParticle : newParticles) {
            output1[4 * ii] = newParticle[0];
            output1[4 * ii + 1] = newParticle[1];
            output1[4 * ii + 2] = newParticle[2];
            output1[4 * ii + 3] = newParticle[3];
            ii++;
        }
        //System.out.println("[Resample] finished.");

        // Save the resampled particles to a txt file for visualization
        File file = new File(outFilePath);
        try (PrintWriter pw = new PrintWriter(file)) {
            for (double value : output1) {
                pw.println(value);
            }
            System.out.println("Output for Resampled Particles is Completed!\n========================\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /* Setters (for inputs): */
    public void set_input1(double[] i) {
        input1 = i;
    }

    /* Getters (for outputs): */
    public double[] get_output1() {
        return output1;
    }
}
