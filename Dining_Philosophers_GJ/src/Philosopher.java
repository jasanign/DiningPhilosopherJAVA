import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.io.PrintStream;
import java.io.FileNotFoundException;

/**
 * Philosopher Class - This class is the monitor that controls the philosophers'
 * access to the chop-sticks properly
 * @author Gajjan Jasani & Ethan Roland
 * @version 4/28/2017
 */
public class Philosopher implements PhilosopherInterface {
	/** Named constants for philosophers' sleep */
    private static final long min = 1000, max = 5000;
    /** Print stream to write to a file */
    private PrintStream writer = null;
    /** enum for fixed states of a philosopher */
	private enum State {THINKING, HUNGRY, EATING};
	/** Array to hold the state of each philosopher */
	private State[] states = new State[PhilosopherInterface.DINERS];
	/** Lock for controlling states and access to chop-sticks concurrently */
	ReentrantLock key = new ReentrantLock(true);
	/** Array for holding conditions */
	Condition[] self = new Condition[PhilosopherInterface.DINERS];
	/** log controller */
	boolean printToFile = false;
	
	/**
	 * Modified constructor for the Philosopher class
	 * @param toFile log controller
	 */
	public Philosopher(boolean toFile) {
		
		if(toFile){ // if true, writing log to a file
            printToFile = toFile;
            try{
                writer = new PrintStream("log.txt");
            }catch(FileNotFoundException e){
                System.err.println(e.getMessage());
            }
        }
		// initializing Philosopher states and conditions
		for(int i=0; i<PhilosopherInterface.DINERS; i++){
			states[i] = State.THINKING;
			self[i] = key.newCondition();
		}
	}
	/**
	 * TakeChopstick method: this method checks if a Philosopher is hungry and
	 * looking for chop-sticks, and calls checkChopsticks() for finding 
	 * chop-sticks
	 * @param i Philosopher id
	 */
	@Override
	public void takeChopsticks(int i) {
		key.lock();
		this.states[i] = State.HUNGRY;
	    printResult("Philosopher " + i + " is " + states[i]);
	    checkChopsticks(i);
	    if (this.states[i] != State.EATING) {
	    	try {
	            this.self[i].await(); // wait if chop-sticks are not available
	        } catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
	        }
	    }
        key.unlock();	
	}

	/**
	 * ReplaceChopstick method: Checks if a Philosopher is hungry and
	 * has access to right and left chop-sticks
	 * @param i Philosopher id
	 */
	@Override
	public void replaceChopsticks(int i) {
		key.lock();
		states[i] = State.THINKING;
	    checkChopsticks((i+PhilosopherInterface.DINERS-1) 
	    								% PhilosopherInterface.DINERS);
	    checkChopsticks((i+1) % PhilosopherInterface.DINERS);
		key.unlock();
	}

	/**
	 * checkChopsticks method: checking if current philosopher is hungry and
	 * his right and left philosophers are eating or not
	 * @param i Philosopher id
	 */
	public void checkChopsticks(int i){
		if(states[(i + PhilosopherInterface.DINERS-1) 
		          		% PhilosopherInterface.DINERS] != State.EATING 
		   && states[i] == State.HUNGRY 
		   && states[(i + 1) % PhilosopherInterface.DINERS] != State.EATING){
			
			states[i] = State.EATING;
	        self[i].signal();
	    }
	}
	/**
	 * think method: make a philosopher think randomly between 1 to 5 seconds
	 * @param i Philosopher id
	 */
	public void think(int i){
		printResult("Philosopher " + i + " is " + states[i]);
	    try{
	    	Thread.sleep(ThreadLocalRandom.current().nextLong(min ,max));

	    }catch(InterruptedException e){
	    	Thread.currentThread().interrupt();
	    }
	}
	/**
	 * eat method: make a philosopher eat randomly between 1 to 5 seconds
	 * @param i Philosopher id
	 */
	public void eat(int i){
		printResult("Philosopher " + i + " is " + states[i]);
	    try{
	    	Thread.sleep(ThreadLocalRandom.current().nextLong(min ,max));

	    }catch(InterruptedException e){
	    	Thread.currentThread().interrupt();
	    }
	}
    /**
     * printResult method: controls synchronous access to the log file or 
     * prints on concole
     * @param result string to print/log
     */
    private synchronized void printResult(String result){
    	
    	if(!printToFile){
            System.out.println(result);
        }else{
            writer.print(result);
            writer.println();
        }
    }	
}

/**
 * DiningPhilosopher class: This class creates instances of philosophers threads
 * and makes them think and eat
 * @author Gajjan Jasani & Ethan Roland
 * @version 4/28/2017
 *
 */
class DiningPhilosopher implements Runnable {

	/** instance of the monitor class */
	private Philosopher monitor;
	/** Philosopher id */
	int i = 0;
	/**
	 * Modified constructor of DiningPhilosopher
	 * @param monitor monitor
	 * @param i Philosopher id
	 */
	public DiningPhilosopher(Philosopher monitor, int i) {
	    this.monitor = monitor;
	    this.i = i;
	}	
	@Override
	public void run() {
		// making philosopher eat and think until interrupted somehow
	    while(!Thread.interrupted()) {
	    	monitor.think(i);
	    	monitor.takeChopsticks(i);
            monitor.eat(i);
	        monitor.replaceChopsticks(i);
	    }
	    System.out.println("Philosopher "+i+" has left.");
	}
}
