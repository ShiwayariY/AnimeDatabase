package format.date;

public class Period implements Comparable<Period> {
	Date from, to;
	
	/**
	 * Constructs a period from the given dates. Unknown dates are allowed.
	 * null is allowed for the date 'to'
	 * @param from
	 * @param to
	 * @throws IllegalArgumentException if from is later than to
	 */
	public Period(Date from, Date to) {
		this.from = from;
		//following if throws NullPointerException if from is null:
		if(from.isUnknownDate() || to == null || to.isUnknownDate() || from.compareTo(to) <= 0) {
			this.to = to;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Creates a new Period as a deep copy from the given period.
	 * @param p
	 */
	public Period(Period p) {
		this.from = new Date(p.from);
		if(p.to != null) {
			this.to = new Date(p.to);
		} else {
			this.to = null;
		}
	}
	
	/**
	 * @return the number of days passed from the starting date to the end date.
	 * @throws UnknownDateException if the Period contains an unknown date.
	 */
	public int days() throws UnknownDateException {
		int days = 0;
		//if 'to' is null, a NullPointerException is thrown.
		days = from.elapsedDaysUntil(to);
		return days;
	}
	
	/**
	 * @return the start date of this period
	 */
	public Date startDate() {
		return from;
	}
	
	/**
	 * @return the end date of this period
	 */
	public Date endDate() {
		return to;
	}
	
	/**
	 * Set the start date of this period.
	 * @param stDate
	 * @throws IllegalArgumentException if the given start
	 * date is later than the existing end date.
	 */
	public void setStartDate(Date stDate) {
		if(stDate.isUnknownDate() || to == null || to.isUnknownDate() || stDate.compareTo(to) <= 0) {
			from = stDate;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Set the end date of this period. null is allowed.
	 * @param endDate
	 * @throws IllegalArgumentException if the given end
	 * date is earlier than the existing start date.
	 */
	public void setEndDate(Date endDate) {
		if(from.isUnknownDate() || endDate == null || endDate.isUnknownDate() || from.compareTo(endDate) <= 0) {
			to = endDate;
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Compares the start dates of the periods, if they are not equal.
	 * If the start dates are equal, compares the end dates of the periods.
	 */
	public int compareTo(Period p) {
		int ret = from.compareTo(p.startDate());
		if(ret == 0) {
			ret = to.compareTo(p.endDate());
		}
		return ret;
	}
}
