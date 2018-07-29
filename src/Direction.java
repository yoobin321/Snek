public enum Direction {
	UP, DOWN, LEFT, RIGHT, DEFAULT;

	public static Direction getOpposite(Direction d) {
		switch (d) {
			case DOWN:
				return Direction.UP;

			case LEFT:
				return Direction.RIGHT;

			case RIGHT:
				return Direction.LEFT;

			default:
				return Direction.DOWN;
		}
	}
}