package main.strategy;

import main.Strategy;
import main.Executor;
import main.data.Point;
import main.data.Robot;

public abstract class AbstractStrategy implements Strategy {

	protected Executor executor = null;

	public void setExecutor(Executor executor) {
		this.executor  = executor;
	}
	
	/**
	 * Move robot from robotX and robotY to a Point position in a pitch
	 * 
	 * Point can be a Ball, Pitch or other Robot
	 * 
	 * @param robot
	 * @param point
	 */
	protected void moveToPoint(Robot robot, Point point) {
		
		double dirAngle = -1*Math.atan2(point.getY()-robot.getY(), point.getX()-robot.getX());
		double dy = robot.getY() - point.getY();
		double dx = robot.getX() - point.getX();
		double distance = Math.sqrt(dx*dx + dy*dy);
		System.out.println(getClass().getSimpleName() + "::Distance " + distance);
		
		dirAngle = Math.toDegrees(dirAngle);
		System.out.println(getClass().getSimpleName() + "::dirAngle " + dirAngle);
		System.out.println(getClass().getSimpleName() + "::dAngle " + Math.abs(dirAngle - robot.getT()));
		int a=1;
		int b=1;
		if(dirAngle > robot.getT()){
			//System.out.println("Turning left");
			a=-1;
			b=1;
		} 
		if (dirAngle < robot.getT())
		{
			//System.out.println("Turning right");
			a=1;
			b=-1;
			
		}
		if(Math.abs(dirAngle -robot.getT())<30){
			a = (int) (1*distance)/35;
			b = (int) (1*distance)/35;		
		}
		executor.rotateWheels(a*50, b*50);
	}
}
