/**
 * Meal Class - This class is the driver that fixes the time length of party, 
 * logging status, and creates philosopher threads
 * @author Gajjan Jasani & Ethan Roland
 * @version 4/28/2017
 */
public class Meal {

	/** Named constant for number of diners */
	private static final int DINERS = 5;

	/**
	 * Entry point to the program
	 * @param args 0 time length of party
	 * @param args 1 logging T/F
	 */
	public static void main(String[] args) {
		
		int time = 0; //length of party
        boolean toFile = false; // logging T/F
        // philosopher holder
		DiningPhilosopher[] dp = new DiningPhilosopher[DINERS]; 
		Thread[] tdp = new Thread[DINERS]; // threads holder for philosopher
		Philosopher monitor; // instance of monitor
		// Checking the command line args
		if(args.length != 2 || !(args[1].toUpperCase() != "T" || 
								args[1].toUpperCase() != "F")){
			System.out.println("Usage: party_length(seconds) logging(T/F)");
			System.exit(0);
		}
		String logging = args[1];
		try{
			time = Integer.parseInt(args[0]);
		} catch (NumberFormatException nfe){
			System.out.println("Usage: party_length(seconds) logging(T/F)");
			System.exit(0);
		}
		if(time < 0){
			System.out.println("Error: time must be a positive value");
			System.exit(0);
		}
		if(logging.equals("T")){
			if(time <= 0){
                System.out.println("Error: if you wish to use logging,"
                +" you must have a positive time value >0");
                System.exit(0);
            }
            toFile = true;
		}
		monitor = new Philosopher(toFile); // initializing monitor
		for(int i=0; i < DINERS; i++){ // Creating 5 diners with monitor
			dp[i] = new DiningPhilosopher(monitor, i);
            tdp[i] = new Thread(dp[i]);
		}
		for(int i=0; i < DINERS; i++){ // starting threads concurrently
			tdp[i].start();//made starting easier
		}
		
		deathChecker(tdp); // Checking for dead threads 
		
		if(time > 0){//attempting deferred cancellation
			try {
				Thread.sleep(time *1000);
				System.out.println("Alright, time's up, "
						+ "let's wrap up this party please.");
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			finally{
				for(int i = 0; i < DINERS; i++){
                    tdp[i].interrupt();
                    try{
                        tdp[i].join();//this is so we wait for all of them to finish before calling System.exit(0)
                    }catch(InterruptedException e){
                        System.err.println(e.getMessage());
                    }
                }
				System.exit(0);
            }
		}
	}
	/**
	 * Helper method for checking for dead threads. Checks every 3 seconds
	 * @param tdp arrays of philosopher threads
	 */
	private static void deathChecker(Thread[] tdp){
		Thread deathChecker = new Thread() {
		    public void run() {
		    	while(!Thread.interrupted()) {
			    	for(int i=0; i < DINERS; i++){
			    		// end the party immediately if a philosopher dies
						if(tdp[i].isAlive() == false){
							System.out.println("Philosophers "+i+" just "
									+ "had a heart attack! Party is over!");
							System.exit(1);
						}
			    	}
			    	try { // if no dead thread, sleep for three seconds
						Thread.sleep(3*1000);
					} catch (InterruptedException e) {
						System.err.println(e.getMessage());
					}
			    }
		    }
		};
		deathChecker.start();
	}
}
