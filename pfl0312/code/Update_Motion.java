/*
 * Code generated by MIMOS tool.
 * Date: 2024/03/13 17:46:17
 * 
 * Based on the sensor data (odometry), update the particles' position and orientation.
 * If the difference is minor (less than 1e-10), the particles will not be updated.
 * 
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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Update_Motion {
    // private final static Logger logger =
    // LoggerFactory.getLogger(Update_Motion.class);
    /* Inputs: */
    double[] input1; // Odometry data
    double[] input2; // Particles

    /* Outputs: */
    double[] output1;

    static String outFilePath = "D:\\lxx\\thesis\\cases\\lxx\\pfl0312\\data\\outputs\\particles_motion.txt";

    /* Initialize Motion Model: */
    // Ref: void ParticleFilter::initializeMotionModel(). These numbers are tuned
    // for CMU's Wean Hall.
    double alpha1 = 0.001;
    double alpha2 = 0.001;
    double alpha3 = 0.1;
    double alpha4 = 0.8;

    double[] prevOdometry = new double[3];
    Random rand = new Random();

    public Update_Motion() {
        // Initialize the previous odometry data
        prevOdometry[0] = -94.234;
        prevOdometry[1] = -139.954;
        prevOdometry[2] = -1.342;
    }

    /* Node computation: */
    public boolean run() {
        output1 = new double[input2.length];
        double xDiff = input1[0] - prevOdometry[0];
        double yDiff = input1[1] - prevOdometry[1];
        double thetaDiff = input1[2] - prevOdometry[2];

        System.out.print("\nDiff: " + Math.abs(xDiff) + " " + Math.abs(yDiff) + " " + Math.abs(thetaDiff) + ". ");

        // Avoid the overheads if the difference is small
        // if (Math.abs(xDiff) + Math.abs(yDiff) + Math.abs(thetaDiff) < 1e-10) {
        //     System.out.println("Update_Motion: no motion since the difference is small.\n");
        //     output1 = input2;
        //     return true;
        // }

        double dRot1 = Math.atan2(yDiff, xDiff) - prevOdometry[2];
        double dTrans = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        double dRot2 = input1[2] - prevOdometry[2] - dRot1;

        double scaleH1 = Math.sqrt(alpha1 * dRot1 * dRot1 + alpha2 * dTrans * dTrans);
        double scaleTh = Math
                .sqrt(alpha3 * dTrans * dTrans + alpha4 * dRot1 * dRot1 + alpha4 * dRot2 * dRot2);
        double scaleH2 = Math.sqrt(alpha1 * dRot2 * dRot2 + alpha2 * dTrans * dTrans);

        double dRh1 = dRot1 - rand.nextGaussian() * scaleH1;
        double dTh = dTrans - rand.nextGaussian() * scaleTh;
        double dRh2 = dRot2 - rand.nextGaussian() * scaleH2;

        for (int i = 0; i < input2.length / 4; i++) {
            double thetaPrime = input2[4 * i + 2] + dRh1;

            output1[4 * i] = input2[4 * i] + dTh * Math.cos(thetaPrime);
            output1[4 * i + 1] = input2[4 * i + 1] + dTh * Math.sin(thetaPrime);
            output1[4 * i + 2] = wrapToPi(input2[4 * i + 2] + dRh1 + dRh2);
            output1[4 * i + 3] = input2[4 * i + 3];
        }
        System.out.println("Update_Motion: Motion updated.\n");
        System.arraycopy(input1, 0, prevOdometry, 0, input1.length);


        // Save the initial particles to a txt file for visualization
        File file = new File(outFilePath);
        try (PrintWriter pw = new PrintWriter(file)) {
            for (double value : output1) {
                pw.println(value);
            }
            //System.out.println("Output for Resampled Particles is Completed!\n========================\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /* Helper function */
    public static double wrapToPi(double angle) {
        while (angle > Math.PI)
            angle -= 2 * Math.PI;
        while (angle < -Math.PI)
            angle += 2 * Math.PI;
        return angle;
    }

    /* Setters (for inputs): */
    public void set_input1(double[] i) {
        input1 = i;
    }

    public void set_input2(double[] i) {
        input2 = i;
    }

    /* Getters (for outputs): */
    public double[] get_output1() {
        return output1;
    }
}
