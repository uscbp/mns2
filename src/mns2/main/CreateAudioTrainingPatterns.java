package mns2.main;

import mns2.comp.AuditoryProcessor;

import java.io.*;

import sim.util.Elib;

/**
 * Created by IntelliJ IDEA.
 * User: JABONA
 * Date: Nov 8, 2005
 * Time: 9:06:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateAudioTrainingPatterns
{
    // Pattern file output stream
    static public DataOutputStream out=null;
    static private int outdim;
    static private int hiddim=20;
    static private int indim;
    static private int rindim=5;
    static private int routdim=5;
    static private double learningrate=0.1;
    static private double momentum=0.0;
    static private double learningincrease=0.01;
    static private double learningdecrease=0.1;
    static private double sequences[][][];

    public static void main(String[] args)
    {
        String patternFilename = args[0];
        try
        {
            // If this is the first pattern
            if (out==null)
            {
                // Open file and write header
                out = Elib.openfileWRITE(patternFilename);
                if (out==null)
                {
                    System.err.println("Error while creating file:"+patternFilename);
                    return;
                }
                outdim=args.length-1;
                sequences=new double[args.length][][];

                for(int i=1; i<args.length; i++)
                {
                    sequences[i-1] = AuditoryProcessor.getAuditoryPatternFromMatlab(args[i]);
                }
                sequences[args.length-1]=new double[sequences[args.length-2].length][sequences[args.length-2][0].length];
                indim=sequences[0][0].length;
                writeHeader();
            }

            // Write input and output sequences for pattern
            writeInputOutputSequences();

            close();
        }
        catch (Exception e) {}
    }

    /**
     * Write pattern file header
     * @throws IOException
     */
    static private void writeHeader() throws IOException
    {
        //System.out.println("Writing header");
        writeout(out,"<!-- This pattern file is generated by reach& grasp simulator HV (by Erhan Oztop -April'00)-->\n");
        writeout(out, "<PatternSet>\n");
        writeout(out, "<RequiredNetworkSettings>\n");
        writeout(out,"<OutputDim>"+outdim+"</OutputDim>\n");
        writeout(out, "<HiddenDim>"+hiddim+"</HiddenDim>\n");
        writeout(out, "<InputDim>"+indim+"</InputDim>\n");
        writeout(out, "<RecurrentInputDim>"+rindim+"</RecurrentInputDim>\n<RecurrentOutputDim>"+routdim+"</RecurrentOutputDim>\n");
        writeout(out, "</RequiredNetworkSettings>\n");
        writeout(out, "<OptionalNetworkSettings>\n");
        writeout(out,"<!--these are optional network settings. If not supplied defaults will be used-->\n");
        writeout(out,"<BPTTLearningRate>"+learningrate+"</BPTTLearningRate>\n");
        writeout(out,"<Momentum>"+momentum+"</Momentum>\n");
        writeout(out,"<LearningIncrease>"+learningincrease+"</LearningIncrease>\n");
        writeout(out,"<LearningDecrease>"+learningdecrease+"</LearningDecrease>\n");
        writeout(out, "</OptionalNetworkSettings>\n");
    }

    /**
     * Writes string to output stream
     * @param out - DataOutputStream to write to
     * @param s - String to write
     * @throws java.io.IOException
     */
    static public void writeout(final DataOutputStream out, final String s) throws IOException
    {
        System.err.println("=====> "+ s);
        out.writeBytes(s);
    }

    /**
     * Writes pattern input and output sequences to file
     * @throws IOException
     */
    static public void writeInputOutputSequences() throws IOException
    {
        // Write sequence header
        writeout(out,"<Sequences>\n");
        for(int i=0; i<sequences.length; i++)
        {
            // Get input values sequence
            final double[][] input = sequences[i];
            // Length of sequence
            int len=input.length;

            writeout(out, "<Sequence>\n");
            writeout(out, "<SequenceLength>"+(len)+"</SequenceLength>\n");
            // Write input sequence
            writeInputSequencePattern(input);
            writeOutputPattern(len, i);
            writeout(out, "</Sequence>\n");
            /*
            System.out.println("Writing shuffled example...");
            writeout(out, "<Sequence>\n");
            writeout(out, "<SequenceLength>"+len+"</SequenceLength>\n");
            writeShuffledPattern(input);
            writeOutputPattern(len, -11);
            writeout(out, "</Sequence>\n");
            */
        }
    }

    /**
     * Constructs input sequence in XML form
     */
    static public void writeInputSequencePattern(double[][] input)
    {
        try
        {
            writeout(out, "<InputSequence>\n");
            for(int i=0; i<input.length; i++)
            {
                String s = "<Input>";
                for(int j=0; j<input[i].length; j++)
                    s += "<Value>" + input[i][j] + "</Value>";
                s += "</Input>\n";
                writeout(out, s);
            }
            writeout(out, "</InputSequence>\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Forms a randomly shuffled version of the input sequence in XML format
     */
    static public void writeShuffledPattern(final double[][] input)
    {
        try
        {
            writeout(out, "<InputSequence>\n");
            double newInput[][] = new double[input.length][];
            for(int i=0; i<input.length; i++)
            {
                newInput[i] = new double[input[i].length];
                System.arraycopy(input[i], 0, newInput[i], 0, input[i].length);
            }
            for(int i=0; i<newInput.length; i++)
            {
                // Calculate 2 random indices
                final int i1 = (int)(Math.random() * (newInput.length-1));
                final int i2 = (int)(Math.random() * (newInput.length-1));

                // Swap the rows at these indices
                final double[] temp = new double[newInput[i].length];
                for(int j=0; j<newInput[i].length; j++)
                    temp[j] = newInput[i1][j];
                for(int j=0; j<newInput[i].length; j++)
                    input[i1][j] = input[i2][j];
                for(int j=0; j<newInput[i].length; j++)
                    input[i2][j] = temp[j];
            }

            // Put scrambled input sequence in XML formatted string
            for(int i=0; i<newInput.length; i++)
            {
                String s = "<Input>";
                for(int j=0; j<newInput[i].length; j++)
                    s += "<Value>" + newInput[i][j] + "</Value>";
                s += "</Input>\n";
                writeout(out, s);
            }
            writeout(out, "</InputSequence>\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The mirror response coding - Constructs positive example output sequence in XML format
     */
    static public void writeOutputPattern(final int len, final int targetIndex)
    {
        try
        {
            writeout(out,"<OutputSequence>\n");
            for(int j=0; j<len; j++)
            {
                String s = "<Output>";
                for (int i=0;i<outdim;i++)
                {
                    s+="<Value>";
                    if (i==targetIndex)
                        s+=1;
                    else
                        s+=0;
                    s+="</Value>";
                }
                s += "</Output>\n";
                writeout(out, s);
            }
            writeout(out, "</OutputSequence>\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Closes pattern file output stream
     */
    static public void close()
    {
        try
        {
            if (out!=null)
            {
                writeout(out, "</Sequences>\n</PatternSet>\n");
                out.close();
            }
        }
        catch(IOException e) {}
        out=null;
    }
}
