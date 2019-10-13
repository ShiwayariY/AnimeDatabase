package format.date;

import java.lang.IllegalArgumentException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Date implements Comparable<Date> {

	// 0 is used for unknown Day,Month,Year
	// DAYS_IN_MONTH[0] is for unknown month -> unknown number of days, maximum 31.
	private static final int[] DAYS_IN_MONTH = { 31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int day, month, year;

	/**
	 * Create a new date. Day, month or year = 0 is used for unknown dates.
	 * 
	 * @param day
	 * @param month
	 * @param year
	 * @throws InvalidDateException
	 */
	public Date(int day, int month, int year) throws InvalidDateException {
		if (!isValidDate(day, month, year)) {
			throw new InvalidDateException();
		}
		this.day = day;
		this.month = month;
		this.year = year;
	}

	/**
	 * Tries to parse a date from the given string. The format dd.mm.yyyy is used,
	 * where 00 or 0000 are used for unknown day, month, year.
	 * 
	 * @param date
	 * @throws NumberFormatException if the format does not match.
	 */
	public Date(String date) throws NumberFormatException {
		Pattern p = Pattern.compile("([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})");
		Matcher m = p.matcher(date);
		if (m.matches()) {
			day = Integer.parseInt(m.group(1));
			month = Integer.parseInt(m.group(2));
			year = Integer.parseInt(m.group(3));
		} else {
			throw new NumberFormatException("Invalid date format: " + date);
		}
	}

	/**
	 * Creates a new Date as a deep copy of the given Date.
	 * 
	 * @param d
	 */
	public Date(Date d) {
		this.day = d.day;
		this.month = d.month;
		this.year = d.year;
	}

	/**
	 * Checks if the given date exists. Day, month or year = 0 is used for unknown
	 * dates.
	 * 
	 * @return true if the date is valid.
	 */
	private boolean isValidDate(int day, int month, int year) {
		boolean valid = true;
		if ((month < 0 || month > 12 || day < 0 || year < 0)
				|| ((month != 2) && (day > DAYS_IN_MONTH[month]))
				|| ((month == 2) && (day > (DAYS_IN_MONTH[2] + (isLeapYear(year) ? 1 : 0))))) {
			valid = false;
		}
		return valid;
	}

	/**
	 * @return true if the given year is a leap year.
	 */
	private boolean isLeapYear(int year) {
		return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
	}

	/**
	 * @return Date of the next day.
	 * @throws UnknownDateException
	 */
	public Date nextDay() throws UnknownDateException {
		int d = day, m = month, y = year;
		if (isUnknownDate()) {
			throw new UnknownDateException();
		}
		d++;
		if (!isValidDate(d, m, y)) {
			d = 1;
			m++;
			if (!isValidDate(d, m, y)) {
				m = 1;
				y++;
			}
		}
		Date date = null;
		try {
			date = new Date(d, m, y);
		} catch (InvalidDateException e) {
		} // Never occurs due to the algorithm in this method.
		return date;
	}

	/**
	 * @return Number of days elapsed between this Date and endDate.
	 * @throws IllegalArgumentException if endDate is earlier than this date
	 * @throws UnknownDateException     if either date is unknown
	 */
	public int elapsedDaysUntil(Date endDate) throws IllegalArgumentException, UnknownDateException {
		if (this.isUnknownDate() || endDate.isUnknownDate()) {
			throw new UnknownDateException();
		}
		if (this.compareTo(endDate) > 0) {
			throw new IllegalArgumentException(endDate + " is earlier than " + this.toString());
		}
		int days = 0;
		int d = day, m = month, y = year;
		Date currDate = null;
		try {
			currDate = new Date(d, m, y);
		} catch (InvalidDateException e) {
		} // Never occurs, because it is a copy of this object.
		while (currDate.compareTo(endDate) != 0) {
			currDate = currDate.nextDay();
			days++;
		}
		return days;
	}

	/**
	 * Compares two dates. A later date is considered as bigger. Unknown dates
	 * (date, month or year are 0) can also be compared.
	 */
	public int compareTo(Date d) {
		int ret = 0;
		if ((year == d.year) && (month == d.month) && (day == d.day)) {
			return 0;
		} else {
			ret = 1;
		}
		if ((year < d.year)
				|| ((year == d.year) && ((month < d.month) || ((month == d.month) && (day < d.day))))) {
			ret = -1;
		}
		return ret;
	}

	/**
	 * Checks the dates for equality.
	 */
	public boolean equals(Date d) {
		boolean equal = false;
		try {
			equal = (compareTo(d) == 0);
		} catch (NullPointerException e) {
		}
		return equal;
	}

	/**
	 * @return a string representation of this date in the format dd.mm.yyyy
	 */
	public String toString() {
		return String.format("%02d.%02d.%04d", day, month, year);
	}

	/**
	 * Checks if either day, month or year of the date is unknown.
	 */
	public boolean isUnknownDate() {
		return (day == 0 || month == 0 || year == 0);
	}

}
