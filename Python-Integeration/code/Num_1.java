/*
 * Code generated by MIMOS tool.
 * Date: 2024/04/12 12:13:06
 */

import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Num_1
{
    //private final static Logger logger = LoggerFactory.getLogger(Num_1.class);
    /* Inputs:  */
    int iter = 0;
    int freq = 20;
    int amplitude = 5;

    /* Outputs:  */
    double output1;

    /* Node computation: */
    public boolean run() {
        // TODO: put your code here
        output1 = amplitude* Math.sin(iter*2*Math.PI/freq);
        iter++;
        return true;
    }

    /* Setters (for inputs): */

    /* Getters (for outputs): */
    public double get_output1() {
        return output1;
    }
}