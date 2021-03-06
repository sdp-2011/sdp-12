package main;

import javax.swing.*;

import main.gui.DrawablesListener;
import main.gui.GuiListener;
import main.processor.SimulatorProcessor;
import main.strategy.ProcessorListener;

public class Runner {
	
	/**
	 * Is debugging enabled 
	 */
	public static boolean DEBUG = true;

	/**
	 * Processor instance
	 */
	private Processor processor;
	
	/**
	 * Strategy instance
	 */
	private Strategy strategy;
	
	/**
	 * Executor instance
	 */
	private Executor executor;

	/**
	 * Is Runner running?
	 */
	private boolean running = false;
	
	/**
	 * Processing worker instance used to run processing in a separate thread
	 */
	private ProcessingWorker worker;

	/**
	 * Window instance controlling UI
	 */
	private Window window;

	/**
	 * Available processors
	 * TODO make this automatic
	 */
	public final String[] processors = { "File", "Local process", "Simulator" };
	
	/**
	 * Available strategies
	 * TODO make this automatic
	 */
	public final String[] strategies = {"GoToBall", "PFS", "Take penalty", "Basic"};
	/**
	 * Available executors
	 * TODO make this automatic
	 */
	public final String[] executors = { "Simulator", "Bluetooth", "Dull" };

	/**
	 * Robot blue
	 */
	private final String ROBOT_BLUE = "Blue";
	
	/**
	 * Robot yellow
	 */
	private final String ROBOT_YELLOW = "Yellow";
	
	/**
	 * Available robots
	 * TODO make this automatic
	 */
	public final String[] robots = { ROBOT_BLUE, ROBOT_YELLOW };
	
	/**
	 * Left Goal
	 */
	private final String LEFT_GOAL = "Left Goal";
	
	/**
	 * Robot yellow
	 */
	private final String RIGHT_GOAL = "Right Goal";
	
	/**
	 * Available goals
	 * TODO make this automatic
	 */
	public final String[] goals = { LEFT_GOAL, RIGHT_GOAL };
	

	/**
	 * Start Runner statically
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// we need actual runner instance as this is not a static-only class actually
		new Runner();
	}

	/**
	 * Runner instance
	 */
	public Runner() {
		// create a window
		window = new Window(this);
	}

	/**
	 * Start runner using 3 sub-parts
	 * 
	 * Might throw exception if configuration is wrong or any of the sub-parts fail to start
	 * 
	 * @param processor
	 * @param strategy
	 * @param executor
	 * @param our_robot
	 * @param goal
	 * @throws Exception
	 */
	public void startRunner(String processor, String strategy, String executor, String our_robot, String left_goal)
			throws Exception {
		// Disable button from being clicked 
		window.setButton("Running...", false);

		// set running to true so other parts of code can know Runner state
		running = true;

		// initialise all sub-components
		setProcessor(processor);
		setStrategy(strategy);
		setExecutor(executor);
		
		// if strategy exists, connect strategy with executor
		if (this.strategy != null) {
			this.strategy.setExecutor(this.executor);
		}
		
		// if processor exists, connect with strategy
		if (this.processor != null) {
			this.processor.addListener(new ProcessorListener(this.strategy));
		}

		// allow Runner to be stopped by settings button text
		window.setButton("Stop", true);
		// Inform window about running process
		// TODO handle this better
		if (!(this.processor instanceof SimulatorProcessor)) {
			this.processor.addListener(new GuiListener(window.getSimulator()));
		}
		this.strategy.setDrawablesListener(new DrawablesListener(window.getSimulator()));

		System.out.println("Processing starting");
		
		// start running processor
		this.processor.run(our_robot.equals(ROBOT_BLUE),left_goal.equals(LEFT_GOAL));
		
		stopRunner();
	}

	/**
	 * Set processor by it's type 
	 * 
	 * @param type
	 */
	private void setProcessor(String type) {

		// TODO refactor this
		if (type.equals("Local process")) {
			processor = new main.processor.LocalVision("../../Vision/trunk/ObjectDetectionML/ObjectDetectionML/src/build/your_project c predict_major outputToConsole show");
		} else if (type.equals("Simulator")) {
			processor = new main.processor.SimulatorProcessor(window.getSimulator());
		} else {
			processor = new main.processor.File("data/Outputlocs.txt");
		}
	}

	/**
	 * Set strategy by it's type 
	 * 
	 * @param type
	 */
	private void setStrategy(String type) {
		
		// TODO refactor this
		if (type.equals("PFS")) {
			// wheel radius = 2.48
			// gear ratio = 10 / 3
			// second parameter is 2.48 * 10 / 3
			// TODO use config for settings
			strategy = new main.strategy.PFStrategy(15.2, 8.27);
		} else if (type.equals("Take penalty")) {
			strategy = new main.strategy.TakePenalty();
		} else if (type.equals("Basic")) {
			strategy = new main.strategy.BasicStrategy();
		} else if (type.equals("GoToBall")){
			strategy = new main.strategy.GoToBall();
		}		
	}

	/**
	 * Set executor by it's type
	 * 
	 * Might throw exception if executor cannot be created
	 * 
	 * @param type
	 * @throws Exception
	 */
	private void setExecutor(String type) throws Exception {

		// TODO refactor this to use automatic discovery
		if (type.equals("Bluetooth")) {
			// TODO move settings to config file
			executor = new main.executor.Bluetooth("Roboto", "00:16:53:0b:b5:a3");
		} else if (type.equals("Dull")) {
			executor = new main.executor.Dull();
		} else if (type.equals("Simulator")) {
			executor = new main.executor.Simulator(window.getSimulator());
		}
	}

	/**
	 * Stop runner from being executed anymore
	 * 
	 * 
	 */
	public void stopRunner() {
		
		// first disable the button
		window.setButton("Stopping...", false);
		
		// cancel worker thread
		if (worker != null)
			worker.cancel(true);

		// stop processing data and kill the executor
		if (processor != null)
			processor.stop();
		if (executor != null)
			executor.exit();

		// set running to false to notify later code that Runner has been
		// successfully stopped 
		running = false;
		
		// re-enable button for start Runner again
		window.setButton("Run", true);
		System.out.println("Runner stopped");
	}

	/**
	 * Toggle runner, so if it's not started yet - start it, if started - stop it
	 * 
	 * @param processor
	 * @param strategy
	 * @param executor
	 * @param our_robot
	 * @return true for started, false for stopped
	 */
	public boolean toggle(String processor, String strategy, String executor, String our_robot, String left_goal) {
		
		// is it running now?
		if (running) {
			if (Runner.DEBUG) {
				System.out.println("Stopping runner");
			}
			// if yes - stop it
			stopRunner();
			return false;
		}
		
		if (Runner.DEBUG) {
			System.out.println("Starting runner");
		}

		// create a worker thread and execute it
		// this is needed so processor doesn't block UI
		worker = new ProcessingWorker(processor, strategy, executor, our_robot, left_goal);
		worker.execute();

		return true;
	}

	public Processor getProcessor() {
		return processor;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public Executor getExecutor() {
		return executor;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * ProcessingWorker class containing logic to start worker in separate thread
	 * using SwingWorker class
	 */
	class ProcessingWorker extends SwingWorker<String, Object> {

		protected String processor;
		protected String strategy;
		protected String executor;
		protected String our_robot;
		protected String left_goal;

		public ProcessingWorker(String processor, String strategy, String executor, String our_robot, String left_goal) {
			this.processor = processor;
			this.strategy = strategy;
			this.executor = executor;
			this.our_robot = our_robot;
			this.left_goal = left_goal;

			if (Runner.DEBUG) {
				System.out.printf("Creating worker for Processor: %s, Strategy: %s, Executor: %s\n", 
					processor, strategy, executor);
			}
		}

		@Override
		public String doInBackground() {
			try {
				// try starting runner
				startRunner(processor, strategy, executor, our_robot, left_goal);
			} catch (Exception e) {
				// runner cannot be started
				System.out.println("Runner cannot be started: " + e.getMessage());
				// stop it, this is unneeded in most cases, but just makes sure all processes are reset
				stopRunner();
			}

			// worker interface processes an Object and returns a String
			// hence a String here
			return "";
		}

		@Override
		protected void done() {

		}
	}
}
