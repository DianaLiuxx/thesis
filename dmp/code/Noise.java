/*
 * Code generated by MIMOS tool.
 * Date: 2024/05/12 19:27:59
 */

import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

public class Noise {
    int iter = 0;
    int noisePeriod = 100; // The period that noise happened
    Random rand = new Random();

    /* Inputs: */

    /* Outputs: */
    double output1;

    /* Node computation: */
    public boolean run() {
        if (iter == noisePeriod) {
            iter = 0;
            output1 = (rand.nextInt() % 100) / (20.0 * 100.0); // The noise value is Within 5%
        } else {
            iter++;
            output1 = 0;
        }
        output1 = 0; // Disable noise
        return true;
    }

    /* Setters (for inputs): */

    /* Getters (for outputs): */
    public double get_output1() {
        return output1;
    }
}