package format.interval;

import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Shiwayari Yonohana
 * Representation of Intervals consisting of Integers.
 */
public class Interval implements Comparable<Interval> {
	
	private int left, right;
	
	/**
	 * @param left
	 * @param right
	 * @throws IllegalArgumentException if left > right.
	 */
	public Interval(int left, int right) {
		if (left > right) {
			throw new IllegalArgumentException();
		}
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Parses an interval from a string in the format [a, b] with integers a and b.
	 * @param s
	 * @throws IllegalArgumentException if the format does not match or a > b
	 */
	public Interval(String s) {
		Pattern p = Pattern.compile("\\[(\\d+),(\\d+)\\]");
		Matcher m = p.matcher(s);
		if(m.matches()) {
			int a = Integer.parseInt(m.group(1));
			int b = Integer.parseInt(m.group(2));
			if(a > b) {
				throw new IllegalArgumentException(s + "is not a valid Interval.");
			} else {
				left = a;
				right = b;
			}
		} else {
			throw new IllegalArgumentException(s + "is not a string representation of Interval.");
		}
	}

	/**
	 * @return True if the specified interval contains i.
	 */
	public boolean contains(Interval i) {
		return (left <= i.left && right >= i.right);
	}
	
	/**
	 * @return True if the interval contains val.
	 */
	public boolean contains(int val) {
		return ((val >= left) && (val <= right));
	}
	
	/**
	 * @return True if the intersection with i is not empty.
	 */
	public boolean intersects(Interval i) {
		return (left <= i.right) && (right >= i.left);
	}

	/**
	 * @return True if the next (smaller or greater) value just outside of the specified interval is
	 * equal to the smallest or greatest value in i. Also returns false if intersects(i) returns true.  
	 */
	public boolean adjacentTo(Interval i) {
		return ((right + 1) == i.left) || ((left - 1) == i.right);
	}
	
	/**
	 * @return The intersection of the intervals or null if the intersection is empty.
	 */
	public Interval intersection(Interval i) {
		Interval intersect = null;
		if (intersects(i)) {
			intersect = new Interval(Math.max(left, i.left), Math.min(right, i.right));
		}
		return intersect;
	}

	/**
	 * Unites this interval with the interval i, but only if the resulting union is also an
	 * interval and not a set, so that the union contains all values greater than its smallest and
	 * smaller than its greatest value.
	 * @return True if the union was successful, which is only the case if either intersects(i) or
	 * adjacentTo(i) returns true. 
	 */
	public boolean unite(Interval i) {
		boolean success = false;
		if (adjacentTo(i) || intersects(i)) {
			left = Math.min(left, i.left);
			right = Math.max(right, i.right);
			success = true;
		}
		return success;
	}
	
	/**
	 * Subtracts all values contained in i from this interval. Does not modify the
	 * specified interval, but returns a TreeSet containing the resulting intervals. 
	 * @return A TreeSet containing the resulting intervals. Will be empty if i
	 * contains this interval; otherwise, will contain one or two intervals.
	 */
	public TreeSet<Interval> subtraction(Interval i) {
		TreeSet<Interval> subtraction = new TreeSet<Interval>();
		boolean containsLeft = i.contains(left);
		boolean containsRight = i.contains(right);
		if (containsLeft || containsRight) {
			//double checked because otherwise the case (containsLeft && containsRight) == true is not handled correctly in (*)
			if (!containsRight) {
				subtraction.add(new Interval(i.right + 1, right));
			}
			if (!containsLeft) {
				subtraction.add(new Interval(left, i.left - 1));
			}
		} else if (contains(i)) {
			//(*) see above
			subtraction.add(new Interval(left, i.left - 1));
			subtraction.add(new Interval(i.right + 1 , right));
		} else {
			subtraction.add(new Interval(left, right));
		}
		return subtraction;
	}
	
	/**
	 * @return The number of different Integers this interval contains.
	 */
	public int size() {
		return (right - left + 1);
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof Interval) {
			Interval i = (Interval) obj;
			if ((left == i.left) && (right == i.right)) {
				equal = true;
			}
		}
		return equal;
	}
	
	@Override
	public int hashCode() {
		return left * 37 + right;
	}

	/**
	 * Compares the two smallest values of both intervals.
	 */
	public int compareTo(Interval i) {
		int ret = 0;
		if (left > i.left) {
			ret = 1;
		} else if (left < i.left) {
			ret = -1;
		}
		return ret;
	}

	/**
	 * @return a string representation of this interval in
	 * the format "[a,b]" where a,b are the interval limits.
	 */
	public String toString() {
		return "[" + left + "," + right + "]";
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getRight() {
		return right;
	}
}