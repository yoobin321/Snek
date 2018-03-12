public enum Direction {
	UP, DOWN, LEFT, RIGHT;

	public boolean isOpposite(Direction d) {
		boolean result = false;

		switch (this) {
			case DOWN:
				result = d == Direction.UP;
				break;

			case LEFT:
				result = d == Direction.RIGHT;
				break;

			case RIGHT:
				result = d == Direction.LEFT;
				break;

			case UP:
				result = d == Direction.DOWN;
				break;
		}

		return result;
	}
}