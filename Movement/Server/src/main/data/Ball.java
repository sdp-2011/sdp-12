package main.data;

/**
 * A ball 
 */
public class Ball extends Point {

	public Ball(int X, int Y) {
		super(X, Y);
	}

	/**
	 * Calculates the new X,Y co-ordinates for a point behind the ball.
	 * Currently does this in relation to the goal at the far end of the pitch
	 * TODO: Create method that takes into account which goal we are aiming towards.
	 * 
	 * @param ballGoalAngle
	 * @param ball
	 * @param dirPoint
	 * @param gap
	 */
	public void calculatePosBehindBall(double ballGoalAngle, Ball ball, Point dirPoint, int gap) {
		// Set the distance behind ball we want to move.
		
		// Need to work out sin and cos distances to get new X and Y positions
		double xOffset = gap*Math.cos(ballGoalAngle);
		double yOffset = gap*Math.sin(ballGoalAngle);
			
		// Sets the position for the robot to move to
		dirPoint.setX(ball.getX()+xOffset);
		dirPoint.setY(ball.getY()+yOffset);
	}
}
