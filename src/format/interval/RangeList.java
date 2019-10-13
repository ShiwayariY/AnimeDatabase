package format.interval;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Shiwayari Yonohana
 * Representation of a set of Integer Intervals.
 * Intervals are united/split automatically upon
 * specific method calls.
 */
public class RangeList implements Iterable<Interval> {
	
	private TreeSet<Interval> ranges;
	
	/**
	 * Creates an empty RangeList.
	 */
	public RangeList() {
		ranges = new TreeSet<Interval>();
	}
	
	/**
	 * Creates a RangeList containing the interval i.
	 */
	public RangeList(Interval i) {
		ranges = new TreeSet<Interval>();
		ranges.add(i);
	}
	
	/**
	 * Parses a RangeList from a string in a format like "[[a,b],[c,d]]"
	 * where the inner [] are the intervals. The algorithm also works
	 * if any intervals overlap. If the String equals "[]", an empty RangeList
	 * is created.
	 * @param s
	 * @throws IllegalArgumentException if the format does not match,
	 * or any interval is invalid, e.g. a > b
	 */
	public RangeList(String s) {
		Pattern all = Pattern.compile("\\[(\\[\\d+,\\d+\\](,\\[\\d+,\\d+\\])*){0,1}\\]");
		if(all.matcher(s).matches()) {
			Pattern p = Pattern.compile("\\[\\d+,\\d+\\]");
			Matcher m = p.matcher(s);
			ranges = new TreeSet<Interval>();
			while(m.find()) {
				addInterval(new Interval(m.group()));
			}
		} else {
			throw new IllegalArgumentException(s + "is not a string representation of RangeList.");
		}
	}
	
	/**
	 * Adds a copy of interval to the RangeList, i.e. changes to the interval are
	 * not reflected in the RangeList.
	 */
	public void addInterval(Interval interval) {
		Interval combinedInterval = new Interval(interval.getLeft(), interval.getRight());
		for(Interval i : new TreeSet<Interval>(ranges)) {
			if(combinedInterval.unite(i)) {
				ranges.remove(i);
			}
		}
		ranges.add(combinedInterval);	
	}
	
	/**
	 * Removes all values contained in interval from this RangeList.
	 * Changes to the Interval are not reflected in the RangeList. 
	 */
	public void subtractInterval(Interval interval) {
		int added = 0;
		for (Interval i : new TreeSet<Interval>(ranges)) {
			if (i.intersects(interval)) {
				ranges.remove(i);
				for (Interval j : i.subtraction(interval)) {
					ranges.add(j);
					added++;
				}
				if (added == 2) {
					//One interval can intersect at most 2 intervals
					//that are not contained in the interval.
					break;
				}
			}
		}
	}

	/**
	 * @return The union of the specified RangeList and r.
	 * Changes on the union do not effect the used RangeLists.
	 */
	public RangeList union(RangeList r) {
		RangeList union = new RangeList();
		for (Interval i : r.ranges) {
			union.addInterval(i);
		}
		for (Interval i : ranges) {
			union.addInterval(i);
		}
		return union;
		
	}

	/**
	 * @return Returns the RangeList resulting when all values
	 * contained in r are removed from the specified RangeList.
	 * Changes on the subtraction do not effect the used RangeLists.
	 */
	public RangeList subtraction(RangeList r) {
		RangeList subt = copy();
		for (Interval i : r.ranges) {
			subt.subtractInterval(i);
		}		
		return subt;
	}
	
	/**
	 * @return The intersection of the specified RangeList and r.
	 * Changes on the intersection do not effect the used RangeLists.
	 */
	public RangeList intersection(RangeList r) {
		RangeList intersect = new RangeList();
		for (Interval a : r.ranges) {
			for (Interval b : ranges) {
				if (a.intersects(b)) {
					intersect.addInterval(a.intersection(b));
				}
			}
		}
		return intersect;
	}

	/**
	 * @return A deep copy of the specified RangeList so that neither
	 * changes to the Intervals of the RangeList nor changes to the
	 * RangeList itself are reflected in the copy.
	 */
	public RangeList copy() {
		RangeList copy = new RangeList();
		for (Interval i : ranges) {
			copy.ranges.add(new Interval(i.getLeft(), i.getRight()));
		}
		return copy;
	}
	
	/**
	 * @return The total number of different integers contained
	 * in all intervals of the specified RangeList.
	 */
	public int size() {
		int size = 0;
		for (Interval i : ranges) {
			size += i.size();
		}
		return size;		
	}

	/**
	 * @return a string representation of this RangeList in the format "[[a,b],[c,d]]"
	 * where the inner [-,-] are the intervals and a,b,c,d,.. the interval limits
	 */
	public String toString() {
		String s = "[";
		for (Interval i : ranges) {
			s += i.toString() + ",";
		}
		if (s.endsWith(",")) {
			s = s.substring(0, s.length() - 1);
		}
		return s + "]"; 
	}
	
	/**
	 * @return true if this RangeList contains the RangeList r, false otherwise.
	 */
	public boolean contains(RangeList r) {
		return this.union(r).equals(this);
	}

	/**
	 * @return True if both RangeLists contain exactly the same values.
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equal = true;
		if (obj.getClass() == getClass()) {
			Iterator<Interval> it1 = ranges.iterator();
			Iterator<Interval> it2 = ((RangeList) obj).ranges.iterator(); 
			for(; it1.hasNext() && it2.hasNext();) {
				if (!it1.next().equals(it2.next())) {
					equal = false;
					break;
				}
			}
			if (it1.hasNext() || it2.hasNext()) {
				equal = false;
			}
		} else {
			equal = false;
		}
		return equal;
	}

	@Override
	public int hashCode() {
		int hashC = 0;
		for (Interval i : ranges) {
			hashC += i.hashCode();
		}
		return hashC;
	}

	/**
	 * @return the iterator for the RangeList for read-only purposes.
	 * The RangeList cannot be modified by using this iterator.  
	 */
	@Override
	public Iterator<Interval> iterator() {
		return Collections.unmodifiableSortedSet(ranges).iterator();
	}
	
	/**
	 * @return the smallest integer in this RangeList.
	 * @throws EmptyRangeListException if RangeList is empty.
	 */
	public int getSmallest() throws EmptyRangeListException {
		if(!isEmpty()) {
			return ranges.first().getLeft();
		} else { throw new EmptyRangeListException(); }		
	}
	
	/**
	 * @return the biggest integer in this RangeList.
	 * @throws EmptyRangeListException if RangeList is empty.
	 */
	public int getBiggest() throws EmptyRangeListException {
		if(!isEmpty()) {
			return ranges.last().getRight();
		} else { throw new EmptyRangeListException(); }
	}
	
	/**
	 * @return true if the RangeList is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return (ranges.isEmpty());
	}
	

/* Test Code:
public static void main(String[] args) {

	Random r = new Random();
	RangeList l = new RangeList();
	for (int i = 0; i < 20; i++) {
		while (true) {
			try {
				int n = r.nextInt(100);
				Interval j = new Interval(n - r.nextInt(10), n);
				Thread.sleep(10);
				l.addInterval(j);
				break;
			} catch (Exception e) {}
			try{
				Thread.sleep(10);
			} catch (Exception e) {}
		}
	}

	RangeList m = new RangeList();
	for (int i = 0; i < 20; i++) {
		while (true) {
			try {
				int n = r.nextInt(100);
				Interval j = new Interval(n - r.nextInt(10), n);
				Thread.sleep(10);
				m.addInterval(j);
				break;
			} catch (Exception e) {}
			try{
				Thread.sleep(10);
			} catch (Exception e) {}
		}
	}
	System.out.println(l);
	System.out.println(m);
	System.out.println(l.intersection(m));

}*/

}
