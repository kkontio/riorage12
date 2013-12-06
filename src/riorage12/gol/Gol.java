package riorage12.gol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class holds the game of life cell grid and
 * is responsible for initializing and setting up
 * the game and synchronizing the worker threads.
 *
 * The implementation uses two short type arrays.
 * One for reading the current state of the game
 * and other for writing the next generation state.
 * This way we dont have to deal with contention amongst
 * worker threads.
 *
 * Padding of game row size + 2 is used at the
 * beginning and the end of the array. Padding of
 * one cell at the end of each virtual "row" is
 * also used.
 * 
 * This padding is for avoiding corner cases
 * in the main worker loop that is doing the heavy
 * lifting.
 */
public class Gol {
    AtomicInteger row_pos;
    CyclicBarrier barrier;
    final int LAST_INDEX;
    final int SIZE;
    final int STEPS;
    final int PADDING;
    final int ROW_PADDING;
    final int ROW_LENGTH;
    short[] read_grid;
    short[] write_grid;
    short[] temp_grid;
    private List<GolWorker> workers;
    private int threads;
    private int steps_taken;
    private long start_time;
    private long end_time;
    private String output_file;

    /**
     * Constructor for our game of life
     * @param size Cell grid size. Game will be set up with size x size grid.
     * @param steps Amout of steps to take.
     * @param threads Number of threads to use.
     */
    public Gol(int size, int steps, int threads) {
        this.threads = threads;
        this.SIZE = size;
        this.STEPS = steps;
        this.ROW_PADDING = 1;
        this.PADDING = this.SIZE + this.ROW_PADDING + 1;
        this.ROW_LENGTH = this.SIZE + this.ROW_PADDING;
        this.steps_taken = 0;
        this.read_grid = new short[this.ROW_LENGTH * this.SIZE + this.PADDING * 2];
        this.write_grid = new short[this.ROW_LENGTH * this.SIZE + this.PADDING * 2];
        this.LAST_INDEX = this.PADDING + this.ROW_LENGTH * this.SIZE;
        this.gridInit();
        this.gameInit();	
    }
    /**
     * Constructor for our game of life
     * @param size Cell grid size. Game will be set up with size x size grid.
     * @param steps Amout of steps to take.
     */
    public Gol(int size, int steps) {
        this(size, steps, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Method for setting individual cell status.
     *
     * Cell status is represented by short value 1/0.
     *
     * @param row Cell row.
     * @param col Cell column.
     * @param status Status to set.
     */
    public void initCellStatus(int row, int col, short status) {
        this.read_grid[row * this.ROW_LENGTH + col + this.PADDING] = status;
        this.write_grid[row * this.ROW_LENGTH + col + this.PADDING] = status;
    }
    
    /**
     * Returns status of a cell in the game of life grid.
     *
     * @param row Cell row.
     * @param col Cell column.
     * @return Cell status.
     */
    public short getCellStatus(int row, int col) {
        return this.read_grid[row * this.ROW_LENGTH + col + this.PADDING];
    }

    /**
     * Setter for output path and filename
     * @param output_file output path and filename
     */
    public void setOutput_file(String output_file) {
        this.output_file = output_file;
    }

    /**
     * Initializes the cell grids by setting all
     * cells to a dead state.
     */
    private void gridInit() {
        for (int i = 0; i < this.read_grid.length; i++) {
            this.read_grid[i] = 0;
            this.write_grid[i] = 0;
        }
    }

    /**
     * Initializes a CyclicBarrier to take care of our step synchronization.
     * Creates workers according to given number of threads.
     */
    private void gameInit() {
        //create CyclicBarrier
        this.barrier = new CyclicBarrier(this.threads, new Runnable() {
            @Override
            public void run() {
                stepDone();
            }
        });
        //initialize row position
        this.row_pos = new AtomicInteger(0);
        //create list of workers
        this.workers = new LinkedList();
        for (int i = 0; i < this.threads; i++) {
            this.workers.add(new GolWorker(this));
        }
    }

    /**
     * Starts the game and timing.
     */
    public void start() {
        System.out.println("Grid size: " + this.SIZE + "x" + this.SIZE + " Game steps: " + this.STEPS);
        System.out.println("Starting game with " + this.threads + " worker threads...");
        this.start_time = System.currentTimeMillis();
        for (GolWorker worker : workers) {
            (new Thread(worker)).start();
        }
    }

    /**
     * End of step method executed by CyclicBarrier to
     * swap the write and read cell grids and to
     * check whether there are more steps to take.
     */
    void stepDone() {
        this.steps_taken++;
        this.temp_grid = this.read_grid;
        this.read_grid = this.write_grid;
        this.write_grid = this.temp_grid;
        this.row_pos.set(0);
        //check if we're done
        if (this.steps_taken == this.STEPS) {
            this.end_time = System.currentTimeMillis();
            finished();
        }
    }

    /**
     * Prints out the used time and steps and saves the result.
     */
    private void finished() {
        System.out.println("Used " + (this.end_time - this.start_time) + "ms on " + this.steps_taken + " game steps.");
        System.out.print("Saving the results to: " + this.output_file + " ...");
        String filename = this.output_file;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            String s = this.SIZE + " " + this.steps_taken + "\n";
            out.write(s);
            for (int i = 0; i < this.SIZE; i++) {
                String output = "";
                for (int j = 0; j < this.SIZE; j++) {
                    output = output + this.getCellStatus(i, j) + " ";
                }
                output = output + "\n";
                out.write(output);
            }
            out.close();
            System.out.println("done.");
        } catch (IOException e) {
            System.out.println("cannot create file.");
            System.exit(0);
        }
        System.out.println("Bye!");
        System.exit(0);
    }
}
