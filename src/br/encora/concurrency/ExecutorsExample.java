package br.encora.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ExecutorsExample {
	private enum thread_service_type {
		SINGLE_THREAD_EXECUTOR, FIXED_THREAD_POOL, THREAD_PER_TASK_EXECUTOR, VIRTUAL_THREAD_PER_TASK_EXECUTOR,
		DELETE_FILES
	};

	public static void main(String[] args) {
		int qtTasksToExecute = 5_000;
		final int qThreadsPool = 10;
		boolean deleteFiles = false;
		final thread_service_type typeExecutor =
				// thread_service_type.FIXED_THREAD_POOL; // use qThreadsPool to define the size of pool of platform threads
				thread_service_type.VIRTUAL_THREAD_PER_TASK_EXECUTOR; // Project loom. Use virtual threads without a defined size pool
                // thread_service_type.THREAD_PER_TASK_EXECUTOR; // unbounded size pool of platform threads
                // thread_service_type.SINGLE_THREAD_EXECUTOR; // only one platform threads

		long startTime = System.currentTimeMillis();
		StringBuffer executionLog = new StringBuffer(); // Thread Safe
		
		if(qtTasksToExecute > 5_000 && typeExecutor == thread_service_type.THREAD_PER_TASK_EXECUTOR && !deleteFiles) {
			logConsole("Warning!! The use of THREAD_PER_TASK_EXECUTOR with a huge quantity of task will probably freeze you system.");
			logConsole("Thread type Executor     : " + typeExecutor);
			logConsole("Quantity Tasks to execute: " + qtTasksToExecute);
			logConsole("not executed.");
			return;
		}

		logConsole("Thread type Executor     : " + typeExecutor);
		logConsole("Quantity Tasks to execute: " + qtTasksToExecute);
		logConsole("QT Max Threads           : " + qThreadsPool
				+ " - (This information make sense only for thread_service_type.FIXED_THREAD_POOL)");

		try (var executor = getNewExecutor(typeExecutor, qThreadsPool, deleteFiles)) {
			IntStream.range(1, qtTasksToExecute + 1).forEach(i -> executor.submit(() -> {
				if (deleteFiles || typeExecutor.equals(thread_service_type.DELETE_FILES)) {
					executionLog.append(Utils.deleteFile(i));
				} else {
					executionLog.append( // thread safe call
							Utils.generateFile(i, null, true, 25, typeExecutor.toString()));
				}

				return i;
			}));
		}

		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		executionLog.append("Total execution time for " + qtTasksToExecute + " tasks using a " + typeExecutor + ": "
				+ "  - fixed threads for use in FIXED_THREAD_POOL: " + qThreadsPool + "\n"
				+ Utils.sdfTimeExec.format(executionTime));
		System.out.println(executionLog);
	}

	private static void logConsole(String string) {
		System.out.println(string);
	}

	private static ExecutorService getNewExecutor(thread_service_type singleThreadExecutor, int qThreadsPool,
			boolean deleteFiles) {
		ExecutorService executor = null;
		
		if(deleteFiles) {
			return Executors.newVirtualThreadPerTaskExecutor();
		}

		switch (singleThreadExecutor) {
		case SINGLE_THREAD_EXECUTOR: {
			// Creates an Executor that uses a single worker thread
			executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().factory());
			break;
		}
		case FIXED_THREAD_POOL: {
			// Creates a thread pool that reuses a fixed number of threads, if the thread
			// factory is not informed, the platform thread will be used
			executor = Executors.newFixedThreadPool(qThreadsPool);
			break;
		}
		case THREAD_PER_TASK_EXECUTOR: {
			// Creates an Executor that starts a new Thread for each task.
			// The number of threads created by the Executor is unbounded.
			executor = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory());
			break;
		}
		case VIRTUAL_THREAD_PER_TASK_EXECUTOR:
		case DELETE_FILES: {
			// Creates an Executor that starts a new virtual Thread for each task.
			// The number of threads created by the Executor is unbounded.
			executor = Executors.newVirtualThreadPerTaskExecutor();
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + singleThreadExecutor);
		}

		return executor;
	}

}
