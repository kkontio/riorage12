package riorage12;

import java.io.File;
import java.util.Scanner;
import riorage12.gol.Gol;

/**
 * Class for reading competition data set input.
 */
public class Competition {
    private static Scanner reader;
    private static File file;
    private static String data_set_path;
    private static int rows;
    private static int steps;
    private static int threads;
    private static Gol game;
    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Invalid arguments.");
            System.exit(1);
        }
        
        //data set path is given by the start.sh script as the first parameter
        data_set_path = args[0];
        System.out.println("Using data set at: " + data_set_path);
        file = new File(data_set_path);
        
        try {
            reader = new Scanner(file);
        } catch (Exception e) {
            System.out.println("File not found.");
            System.exit(1);
        }
        
        //number of threads is given by the start.sh as the third parameter
        try {
            threads = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("Given number of threads is not an integer.");
            System.exit(1);
        }
        
        try {
            //first figure of the input is the N for N*N cell grid
            rows = reader.nextInt();
            //second figure is the number of steps to take P
            steps = reader.nextInt();
            if (threads <= 0)
                game = new Gol(rows, steps);
            else
                game = new Gol(rows, steps, threads);
            //then read in the initial game state
            System.out.print("Initializing game state... ");
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < rows; j++) {
                    game.initCellStatus(i, j, reader.nextShort());
                }
            }
            System.out.println("done.");
        } catch (Exception e) {
            System.out.println("Malformed dataset.");
            System.exit(1);
        }
        //output file is given by the start.sh as the second parameter
        game.setOutput_file(args[1]);
        game.start();
    }
}
