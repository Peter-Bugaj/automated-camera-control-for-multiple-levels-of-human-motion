package IO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AReader implements IReader {

    /**Array holding the data**/
    protected float[][][][] data_per_frame = null;

    /**Tools for reading in the files**/
    protected FileInputStream fis;
    protected InputStreamReader isr;
    protected BufferedReader br;

    /**File location of the data**/
    protected String file_location = "";

    /**Class constructor**/
    public AReader(String file_name) {
        this.file_location = file_name;
    }

    /**Return the data read**/
    public float [][][][] getData() {
        int count = 0;
        while(true) {
            if(data_per_frame != null) {
                return data_per_frame;
            }
            count++;
            if(count == 10) {
                System.exit(1);
            }
            readData();
        }
    }

    /**Open the file**/
    protected void openFile(String fileName) {
        System.out.println("Opening file data: "+fileName);
        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            System.out.print("Failed to open data file\n");
            System.exit(1);
        }
    }

    /**Close the file**/
    protected void closeFile() {
        try {
            System.out.println("Closing file data");
            br.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            System.out.println("Failed to close data file");
            System.exit(1);
        }
    }
}