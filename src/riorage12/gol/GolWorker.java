package riorage12.gol;

import java.util.concurrent.BrokenBarrierException;

/**
 * Worker class for game of life.
 * Processes row given to it by Gol class.
 */
public class GolWorker implements Runnable {
    private Gol game;
    
     /**
     * Worker constructor.
     * 
     * @param game Game to which this worker is tied.
     */
    public GolWorker(Gol game) {
        this.game = game;
    }
    
    /**
     * Starts the thread.
     * 
     * The worker loops through all the game steps
     * updating cell statuses as needed.
     */
    @Override
    public void run() {
        while (true) {
            //get next available row number and compute starting index
            int i = game.row_pos.getAndIncrement() *  game.ROW_LENGTH + game.PADDING;
            while (i < game.LAST_INDEX) {
                //process virtual row
                for (int j = 0; j < game.SIZE; j++) {
                    int live_neighbors =
                            game.read_grid[i - 1]
                            + game.read_grid[i - 1 + game.ROW_LENGTH]
                            + game.read_grid[i + game.ROW_LENGTH]
                            + game.read_grid[i + 1 + game.ROW_LENGTH]
                            + game.read_grid[i + 1]
                            + game.read_grid[i + 1 - game.ROW_LENGTH]
                            + game.read_grid[i - game.ROW_LENGTH]
                            + game.read_grid[i - 1 - game.ROW_LENGTH];

                    //under two live neighbors -> dead cell
                    if (live_neighbors < 2) {
                        game.write_grid[i] = 0;
                        i++;
                        continue;
                    }
                    //two live neighbors -> retain old status
                    if (live_neighbors == 2) {
                        game.write_grid[i] = game.read_grid[i];
                        i++;
                        continue;
                    }
                    //three live neighbors -> live cell
                    if (live_neighbors == 3) {
                        game.write_grid[i] = 1;
                        i++;
                        continue;
                    }
                    //over three live neighbors -> dead cell
                    if (live_neighbors > 3) {
                        game.write_grid[i] = 0;
                        i++;
                        continue;
                    }
                }
                //get next available row
                i = game.row_pos.getAndIncrement() * game.ROW_LENGTH + game.PADDING;
            } 
            //we're done for this step, wait for others to finish
            try {
                this.game.barrier.await();
            } catch (InterruptedException ex) {
                System.out.println('Worker thread interrupted. Exiting thread.');
                return;
            } catch (BrokenBarrierException ex) {
                System.out.println('Unexpected sync barrier breakdown. Exiting thread.');
                return;
            }
        }
    }
} //end class
