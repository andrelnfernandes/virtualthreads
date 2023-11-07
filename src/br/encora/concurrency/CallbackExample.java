package br.encora.concurrency;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class it's a simple example of a callback implementation in Java
 */
public class CallbackExample {

	public static void main(String[] args) {
		int value = 5;

		// Function call passing a callback
		ProcessDataAsynchronously(value, new Callback() {
			@Override
			public void onCompletion(String result) {
				System.out.println("Callback executed: " + result);
			}
		});

		// Other tasks to be performed while asynchronous processing occurs
		executingAnotherTasks();
		
		// The program terminates only after a callback and another tasks end up
		// except if we explicitly call 
		// System.exit(0);
	}

	private static void executingAnotherTasks() {
		long timesToExecuting = 20; // seconds to stay executing
		long lastTime = System.currentTimeMillis();
		int counter = 0;

		while (true) {
			if ((System.currentTimeMillis() - lastTime) > 1000) {
				lastTime = System.currentTimeMillis();
				System.out.println("Executing another task. Counter: " + (++counter));

				if (counter >= timesToExecuting) {
					System.out.println("Terminating another tasks... ");
					break;
				}
			}

		}

	}

	// Interface that defines the contract for the callback
	interface Callback {
		void onCompletion(String resultado);
	}

	// Function that takes a callback as an argument and invokes the callback after
	// some processing
	static void ProcessDataAsynchronously(int value, Callback callback) {
		// Simulates asynchronous
		Runnable callbackExample = () -> {
			// Processing logic
			int maxSecondsToWait = 10;

			long result = value * longLongTimeProcessing(maxSecondsToWait);

			// Invoke the callback passing the result
			callback.onCompletion("The result is: " + result);
		};

		// Old form with platform thread
		//Thread thread = new Thread(callbackExample);	
		//thread.start(); // Starts asynchronous execution
		
		// New form with platform thread
		//Thread.ofPlatform().start(callbackExample);
		
		// New form with platform thread
		 Thread.ofVirtual().start(callbackExample);
	}

	/**
	 * a function just to make the execution time and the result unpredictable
	 * 
	 * @param thread
	 * @param maxMilisecondsToWait
	 * @return
	 */
	private static long longLongTimeProcessing(int maxMilisecondsToWait) {
		long startTime = System.currentTimeMillis(); // the time before the Thread.sleep()
		int timeToSleep = 1000; // default time is 1 second
		if (maxMilisecondsToWait > 0) {
			Random random = new Random();
			// calculating a aleatory time to sleep
			timeToSleep = random.nextInt(maxMilisecondsToWait * 1000) + 1;
			System.out.println("waiting for " + timeToSleep + " milliseconds");
		}
		try {
			Thread.sleep(timeToSleep);
		} catch (InterruptedException e) {
			System.out.println("Error trying to sleep for " + timeToSleep + " milliseconds!!!!!");
		}
		long endTime = System.currentTimeMillis(); // the time after the Thread.sleep()
		return endTime - startTime;
	}

}
