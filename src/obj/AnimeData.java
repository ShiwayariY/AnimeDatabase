package obj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import format.date.Date;
import format.date.Period;
import format.interval.EmptyRangeListException;
import format.interval.Interval;
import format.interval.RangeList;

public class AnimeData {

	public static enum AirStatus {
		FINISHED, AIRING, SOON
	};

	// the following defines the format in which an anime is saved
	// note the comments for how each value is saved
	public static final String AnimeTemplate = "" +
			"<Name>\n" +
			"\n" + // must be more than the empty string ""
			"<Sequel>\n" +
			"\n" + // can be "", in which case null is used
			"<Episodes>\n" +
			"\n" + // must be any integer >= 0, 0 is used for unknown episodes
			"<Comments>\n" +
			"\n" + // comments for given intervals of episodes, can be "" for no comments.
					// If there are comments, in the first line there must be the string
					// representation of the associated interval, followed by the comment
					// starting in a new line, whereas each line of the comment must be preceded
					// by a ":". Example:
					//
					// [1,2]
					// :comment line 1
					// :comment line 2
					// :and so on...
			"<Released>\n" +
			"\n" + // string representation of RangeList (RangeList.toString()), must be at least
					// "[]"
			"<Downloaded>\n" +
			"\n" + // string representation of RangeList (RangeList.toString()), must be at least
					// "[]"
			"<Watched>\n" +
			"\n" + // string representation of RangeList (RangeList.toString()), must be at least
					// "[]"
			"<Airstatus>\n" +
			"\n" + // any of "0" (finished), "1" (airing), "2" (soon)
			"<WatchedSpans>\n" +
			"\n" + // can be "" or a list of "startDate - endDate" seperated by \n, where the dates
					// are in the format Date.toString(). endDate can be "" (null is used), but not
					// startDate
			"<HDD>\n" +
			"\n" + // can be "", in which case null is used
			"<Groups>\n" +
			"\n" + // can be "" or a list of "group - RangeList" separated by \n, where group is
					// the
					// name and RangeList is in the format RangeList.toString() and is at least "[]"
			"<>\n";

	private static Vector<String> names = new Vector<String>(); // use the index of an anime in this vector to find data of this anime from
																// the other vectors
	private static Vector<String> sequels = new Vector<String>();
	private static Vector<Integer> episodes = new Vector<Integer>();
	private static Vector<HashMap<Interval, String>> comments = new Vector<>(); // new implementation: comments
	private static Vector<RangeList> released = new Vector<RangeList>();
	private static Vector<RangeList> downloaded = new Vector<RangeList>();
	private static Vector<RangeList> watched = new Vector<RangeList>();
	private static Vector<AirStatus> airStatus = new Vector<AirStatus>();
	private static Vector<Vector<Period>> periods = new Vector<Vector<Period>>();
	private static Vector<String> hdds = new Vector<String>();
	private static Vector<TreeMap<Integer, RangeList>> groups = new Vector<TreeMap<Integer, RangeList>>(); // map: groupID -> subbed eps

	private static TreeMap<Integer, Group> groupList = new TreeMap<>(); // map groupID -> Group

	public static void toFile(File saveFile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));

		for (int i = 0; i < names.size(); i++) {

			String seq = sequels.get(i);
			if (seq == null) {
				seq = "";
			}

			// new implementation: comments
			String commentStr = "";
			HashMap<Interval, String> allComments = comments.get(i); // for current anime
			for (Interval interval : allComments.keySet()) {
				commentStr += interval.toString() + "\n";
				String[] commentLines = allComments.get(interval).split("\n", 0);
				for (int j = 0; j < commentLines.length; j++) {
					commentStr += ":" + commentLines[j] + "\n";
				}
			}
			if (commentStr.length() == 0) {
				commentStr = "\n";
			}
			// new implementation: comments

			int airStat = 0;
			switch (airStatus.get(i)) {
			case FINISHED:
				airStat = 0;
				break;
			case AIRING:
				airStat = 1;
				break;
			case SOON:
				airStat = 2;
				break;
			}

			StringBuilder spans = new StringBuilder();
			boolean hasNoSpans = true;
			for (Period p : periods.get(i)) {
				hasNoSpans = false;
				spans.append(p.startDate().toString());
				spans.append(" - ");
				Date endDate = p.endDate();
				if (endDate != null) {
					spans.append(endDate.toString());
				}
				spans.append("\n");
			}
			if (hasNoSpans) {
				spans.append("\n");
			}

			StringBuilder groupsStr = new StringBuilder();
			TreeMap<Integer, RangeList> currGroups = groups.get(i);
			boolean hasNoGroup = true;
			for (Integer id : currGroups.keySet()) {
				hasNoGroup = false;
				groupsStr.append(id);
				groupsStr.append(" - ");
				groupsStr.append(currGroups.get(id).toString());
				groupsStr.append("\n");
				// TODO append subbed specials: "RangeList;\nspecial1name\nspeicl2name\n...
			}
			if (hasNoGroup) {
				groupsStr.append("\n");
			}

			bw.write("" +
					"<Name>\n" + names.get(i) +
					"\n" +
					"<Sequel>\n" + seq +
					"\n" +
					"<Episodes>\n" + episodes.get(i) +
					"\n" +
					"<Comments>\n" + commentStr + // new implementation: comments
					"" + // new implementation: comments
					"<Released>\n" + released.get(i) +
					"\n" +
					"<Downloaded>\n" + downloaded.get(i) +
					"\n" +
					"<Watched>\n" + watched.get(i) +
					"\n" +
					"<Airstatus>\n" + airStat +
					"\n" +
					"<WatchedSpans>\n" + spans.toString() +
					"" + // ending \n is contained in spans
					"<HDD>\n" + hdds.get(i) +
					"\n" +
					"<Groups>\n" + groupsStr.toString() +
					"" + // ending \n is contained in groupsStr
					"<>\n");

			bw.flush();
		}

		for (int id : groupList.keySet()) {
			String nameStr = "";
			for (String name : groupList.get(id).getNames()) {
				nameStr += name + "\n";
				// a group always has at least 1 name, because it always has a displayname,
				// so nameStr always ends with \n
			}

			String constitStr = "";
			for (Group constit : groupList.get(id).getConstituents()) {
				constitStr += getGroupID(constit) + "\n";
			}
			if (constitStr.length() == 0) {
				constitStr = "\n";
			}

			String[] commentLines = groupList.get(id).getComments().split("\n", 0); // <-- trailing empty lines are discarded!
			String noPrecedingEmptyStringCommentStr = "";
			int i = 0;
			for (; i < commentLines.length; i++) {
				// this is to discard preceding empty lines
				if (commentLines[i].length() > 0) {
					break;
				}
			}
			for (; i < commentLines.length; i++) {
				// used ":" as delimiter for each new line in the comment, this is to prevent
				// that a comment can be parsed as 'data' if it takes the same format that the
				// 'data' is saved in.
				noPrecedingEmptyStringCommentStr += ":" + commentLines[i] + "\n";
			}
			if (noPrecedingEmptyStringCommentStr.length() == 0) {
				noPrecedingEmptyStringCommentStr = "\n";
			}

			bw.write(
					"<SubberID>\n" +
				       			id + "\n" +
							"<Display>\n" +
							groupList.get(id).getDisplayName() + "\n" +
						       	"<Names>\n" +
							nameStr +
							"<Comments>\n" +
							noPrecedingEmptyStringCommentStr +
							"<Constituents>\n" +
							constitStr +
							"<>\n");
		}
		bw.flush();

		bw.close();
		MainWindow w = MainWindow.thisWindow;
		String t = w.getTitle();
		if (t.endsWith("*")) {
			w.setTitle(t.substring(0, t.length() - 1));
		}
	}

	/**
	 * 
	 * @param saveFile
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static void loadFile(File saveFile) throws IOException {
		names.clear();
		sequels.clear();
		episodes.clear();
		comments.clear();
		released.clear();
		downloaded.clear();
		watched.clear();
		airStatus.clear();
		periods.clear();
		hdds.clear();
		groups.clear();

		BufferedReader br = new BufferedReader(new FileReader(saveFile));
		StringBuilder saveDataB = new StringBuilder();
		String saveData = "", line = "";
		while ((line = br.readLine()) != null) {
			saveDataB.append(line + "\n");
		}
		br.close();

		saveData = saveDataB.toString();
		String animeRegex = "<Name>\n" +
				"(.+?)\n" +
				"<Sequel>\n" +
				"(.*?)\n" +
				"<Episodes>\n" +
				"(\\d+?)\n" +
				"<Comments>\n" + // new implementation: comments
				"(.*?\n)" + // new implementation: comments
				"<Released>\n" +
				"(.+?)\n" +
				"<Downloaded>\n" +
				"(.+?)\n" +
				"<Watched>\n" +
				"(.+?)\n" +
				"<Airstatus>\n"	+
			        "([012])\n" +
				"<WatchedSpans>\n" +
				"(.*?\n)" + // "<Times>\n\\d{0,1}\n" +
				"<HDD>\n" +
				"(.*?)\n" +
				"<Groups>\n" +
				"(.*?\n)" +
				"<>\n";
		Pattern animeP = Pattern.compile(animeRegex, Pattern.DOTALL);
		Matcher m = animeP.matcher(saveData);
		while (m.find()) {
			String currAnime = m.group(1);
			names.add(currAnime);

			String seq = m.group(2);
			if (seq.equals("")) {
				sequels.add(null);
			} else {
				sequels.add(seq);
			}

			episodes.add(Integer.parseInt(m.group(3)));

			// new implementation: comments
			Pattern commentBlockP = Pattern.compile("(\\[\\d+,\\d+\\])\n((:.+\n?(?!\\[))+)");
			Matcher commentBlockM = commentBlockP.matcher(m.group(4));
			HashMap<Interval, String> currComments = new HashMap<>();
			while (commentBlockM.find()) {
				String commStr = "";
				String[] commLines = commentBlockM.group(2).split("\n?:", 0);
				for (int i = 0; i < commLines.length; i++) {
					if (commLines[i].length() != 0) {
						commStr += (i == 0 ? "" : "\n") + commLines[i];
					}
				}
				currComments.put(new Interval(commentBlockM.group(1)), commStr);
			}
			comments.add(currComments);
			// new implementation: comments --> following group(#) numbers # are different!

			released.add(new RangeList(m.group(5)));

			downloaded.add(new RangeList(m.group(6)));

			watched.add(new RangeList(m.group(7)));

			switch (Integer.parseInt(m.group(8))) {
			case 0:
				airStatus.add(AirStatus.FINISHED);
				break;
			case 1:
				airStatus.add(AirStatus.AIRING);
				break;
			case 2:
				airStatus.add(AirStatus.SOON);
				break;
			}

			Pattern spanP = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}) - (.*?)\n", Pattern.DOTALL);
			Matcher spanM = spanP.matcher(m.group(9));
			Vector<Period> spans = new Vector<Period>();
			while (spanM.find()) {
				Date endDate = null;
				try {
					endDate = new Date(spanM.group(2));
				} catch (NumberFormatException e) {
					// no endDate specified -> null (is used as a valid value!)
				}
				spans.add(new Period(new Date(spanM.group(1)), endDate));
			}
			periods.add(spans);

			String hdd = m.group(10);
			if (hdd.equals("")) {
				hdds.add(null);
			} else {
				hdds.add(hdd);
			}

			Pattern groupP = Pattern.compile("(.+?) - (.+?)\n", Pattern.DOTALL);
			Matcher groupM = groupP.matcher(m.group(11));
			TreeMap<Integer, RangeList> currGroups = new TreeMap<>();
			while (groupM.find()) {
				Integer currGroupID = Integer.valueOf(groupM.group(1));
				if (currGroups.containsKey(currGroupID)) {
					// this occurs if saved data is corrupt
					throw new IllegalArgumentException("The anime '" + currAnime
							+ "' has multiple lists of subbed episodes for one or more groups.");
				}
				currGroups.put(currGroupID, new RangeList(groupM.group(2)));
			}
			groups.add(currGroups);
		}

		String groupRegex = "<SubberID>\n" +
				"([0-9]+?)\n" +
				"<Display>\n" +
				"(.+?)\n" +
				"<Names>\n" +
				"(.+?\n)" +
				"<Comments>\n" +
				"(.*?)\n" +
				"<Constituents>\n" +
				"(.*?\n)" +
				"<>\n";
		Pattern groupP = Pattern.compile(groupRegex, Pattern.DOTALL);
		Matcher groupM = groupP.matcher(saveData);

		// saves IDs of constituents for each group; must be done because the
		// constituent Groups must first be
		// created before adding them as instance of Group to other groups as
		// constituents.
		HashMap<Group, Vector<Integer>> constituentIDs = new HashMap<>();

		while (groupM.find()) {
			int currID = Integer.valueOf(groupM.group(1));

			Group g = new Group(groupM.group(2));

			Pattern nameP = Pattern.compile("(.+?)\n");
			Matcher nameM = nameP.matcher(groupM.group(3));

			while (nameM.find()) {
				g.addName(nameM.group(1));
			}

			String modCommentStr = groupM.group(4); // includes preceding ":" for each line
			String commentStr = "";

			if (modCommentStr.length() != 0) {
				String[] modCommentLines = modCommentStr.split("\n?:", 0); // trailing empty lines are discarded
				boolean lineAdded = false;
				for (int i = 0; i < modCommentLines.length; i++) {
					String toAdd = modCommentLines[i];
					if ((!lineAdded && toAdd.length() > 0) || lineAdded) {// preceding empty lines are discarded
						commentStr += (lineAdded ? "\n" : "") + toAdd;
						lineAdded = true;
					}
				}
			}
			g.setComments(commentStr);

			Pattern constitP = Pattern.compile("([0-9]+?)\n");
			Matcher constitM = constitP.matcher(groupM.group(5));
			Vector<Integer> constits = new Vector<>();
			while (constitM.find()) {
				constits.add(Integer.valueOf(constitM.group(1)));
			}
			constituentIDs.put(g, constits);

			groupList.put(currID, g);
		}
		for (Group g : constituentIDs.keySet()) {
			for (int ID : constituentIDs.get(g)) {
				g.addConstituent(groupList.get(ID));
			}
		}
	}

	// GetterMethods--------------------------------------------------------------------

	/**
	 * @return an unmodifyable SortedSet containing all anime names in ascending
	 *         order.
	 */
	public static SortedSet<String> nameList() {
		return Collections.unmodifiableSortedSet(new TreeSet<String>(names));
	}

	public static int episodes(String anime) {
		int i = names.indexOf(anime);
		int eps = -1;
		if (i != -1) {
			eps = episodes.get(i);
		}
		return eps;
	}

	// new implementation: comments
	/**
	 * @param anime
	 * @return a deep copy of the Map of all comments for this anime mapped to their
	 *         episode intervals.
	 */
	public static HashMap<Interval, String> comments(String anime) {
		HashMap<Interval, String> commentsCopy = new HashMap<>();
		int j = names.indexOf(anime);
		if (j != -1) {
			HashMap<Interval, String> comms = comments.get(j);
			for (Interval i : comms.keySet()) {
				commentsCopy.put(new Interval(i.getLeft(), i.getRight()), comms.get(i));
			}
		}
		return commentsCopy;
	}
	// new implementation: comments

	/**
	 * @param anime
	 * @return a copy of the RangeList of released episodes of the given anime, so
	 *         that no change to the RangeList affect the internal RangeList. If the
	 *         anime does not exist, null is returned.
	 */
	public static RangeList released(String anime) {
		int i = names.indexOf(anime);
		RangeList rl = null;
		if (i != -1) {
			rl = released.get(i).copy();
		}
		return rl;
	}

	/**
	 * @param anime
	 * @return the number of episodes in the RangeList of released episodes or -1 if
	 *         the anime does not exist.
	 */
	public static int releasedNum(String anime) {
		int i = names.indexOf(anime);
		int rlnum = -1;
		if (i != -1) {
			rlnum = released.get(i).size();
		}
		return rlnum;
	}

	/**
	 * @param anime
	 * @return a copy of the RangeList of downloaded episodes of the given anime, so
	 *         that no change to the RangeList will affect the internal RangeList.
	 *         If the anime does not exist, null is returned.
	 */
	public static RangeList downloaded(String anime) {
		int i = names.indexOf(anime);
		RangeList dl = null;
		if (i != -1) {
			dl = downloaded.get(i).copy();
		}
		return dl;
	}

	/**
	 * @param anime
	 * @return the number of episodes in the RangeList of downloaded episodes or -1
	 *         if the anime does not exist.
	 */
	public static int downloadedNum(String anime) {
		int i = names.indexOf(anime);
		int dlnum = -1;
		if (i != -1) {
			dlnum = downloaded.get(i).size();
		}
		return dlnum;
	}

	/**
	 * @param anime
	 * @return a copy of the RangeList of watched episodes of the given anime, so
	 *         that no change to the RangeList will affect the internal RangeList.
	 *         If the anime does not exist, null is returned.
	 */
	public static RangeList watched(String anime) {
		int i = names.indexOf(anime);
		RangeList wt = null;
		if (i != -1) {
			wt = watched.get(i).copy();
		}
		return wt;
	}

	/**
	 * @param anime
	 * @return the number of episodes in the RangeList of watched episodes or -1 if
	 *         the anime does not exist.
	 */
	public static int watchedNum(String anime) {
		int i = names.indexOf(anime);
		int wtnum = -1;
		if (i != -1) {
			wtnum = watched.get(i).size();
		}
		return wtnum;
	}

	/**
	 * @param anime
	 * @return the hdd of the given anime. If the anime does not exist, or has no
	 *         hdd specified, null is returned.
	 */
	public static String hdd(String anime) {
		int i = names.indexOf(anime);
		String hdd = null;
		if (i != -1) {
			hdd = hdds.get(i);
		}
		return hdd;
	}

	/**
	 * @param anime
	 * @return a deep copy of the list of Periods of the given anime or null if the
	 *         anime does not exist. Neither changes to the Vector nor to the
	 *         contained Period objects will affect the internal Vector. The
	 *         returned Vector will be sorted in ascending order.
	 */
	public static Vector<Period> watchedSpans(String anime) {
		int i = names.indexOf(anime);
		Vector<Period> spans = null;
		if (i != -1) {
			spans = new Vector<Period>();
			Vector<Period> intern = periods.get(i);
			for (Period p : intern) {
				spans.add(new Period(p));
			}
			Collections.sort(spans);
		}
		return spans;
	}

	/**
	 * @param anime
	 * @return the air status of the given anime or null if the anime does not
	 *         exist.
	 */
	public static AirStatus airStatus(String anime) {
		int i = names.indexOf(anime);
		AirStatus as = null;
		if (i != -1) {
			as = airStatus.get(i);
		}
		// enum value is automatically copied
		return as;
	}

	/**
	 * @param anime
	 * @return a list of groups mapped to their subbed episodes of the given anime,
	 *         or null if the anime does not exist. Neither changes to the Map nor
	 *         to the contained RangeList objects affect the internal Map.
	 */
	public static HashMap<Group, RangeList> groups(String anime) {
		int i = names.indexOf(anime);
		HashMap<Group, RangeList> grps = null;
		if (i != -1) {
			grps = new HashMap<Group, RangeList>();
			TreeMap<Integer, RangeList> intern = groups.get(i);
			for (Integer groupID : intern.keySet()) {
				grps.put(groupList.get(groupID), intern.get(groupID).copy());
			}
		}
		return grps;
	}

	/**
	 * @return a vector containing all existing groups sorted case-insensitive
	 *         alphabetically by their displayed name.
	 */
	public static Vector<Group> groups() {
		Vector<Group> grps = new Vector<>();
		for (int ID : groupList.keySet()) {
			grps.add(groupList.get(ID));
		}
		Comparator<Group> displayNameComparator = new Comparator<Group>() {
			@Override
			public int compare(Group g1, Group g2) {
				return g1.getDisplayName().compareToIgnoreCase(g2.getDisplayName());
			}
		};
		Collections.sort(grps, displayNameComparator);
		return grps;
	}

	public static boolean exists(String anime) {
		return names.contains(anime);
	}

	/**
	 * @param anime
	 * @return the name of the sequel of the given anime, or null if the anime does
	 *         not exist or has no sequel.
	 */
	public static String sequel(String anime) {
		String seq = null;
		int i = names.indexOf(anime);
		if (i != -1) {
			seq = sequels.get(i);
		}
		return seq;
	}

	/**
	 * @param anime
	 * @return the name of the prequel of the given anime, or null if the anime does
	 *         not exist or has no prequel.
	 */
	public static String prequel(String anime) {
		String preq = null;
		int i = sequels.indexOf(anime);
		if (i != -1) {
			preq = names.get(i);
		}
		return preq;
	}

	/**
	 * @param anime
	 * @return true if the given anime has a sequel, else false. Also returns false
	 *         if the given anime does not exist.
	 */
	public static boolean hasSequel(String anime) {
		boolean hasSequel = false;
		int i = names.indexOf(anime);
		if (i != -1 && sequels.get(i) != null) {
			hasSequel = true;
		}
		return hasSequel;
	}

	/**
	 * @param anime
	 * @return true if the given anime has a sequel, else false. Also returns false
	 *         if the given anime does not exist.
	 */
	public static boolean hasPrequel(String anime) {
		boolean hasPrequel = false;
		if (exists(anime) && sequels.indexOf(anime) != -1) {
			hasPrequel = true;
		}
		return hasPrequel;
	}

	/**
	 * @param anime
	 * @return a vector containing all anime related to the given anime in ascending
	 *         order (first, sequel, sequel2, ..). In general, the given anime will
	 *         not be the first element of the resulting vector. Returns an empty
	 *         vector if the given anime does not exist.
	 */
	public static Vector<String> relations(String anime) {
		Vector<String> relations = new Vector<String>();
		if (exists(anime)) {
			relations.add(anime);

			String preq = anime;
			while ((preq = prequel(preq)) != null) {
				relations.add(0, preq);
			}

			String seq = anime;
			while ((seq = sequel(seq)) != null) {
				relations.add(seq);
			}
		}
		return relations;
	}

	public static JPanel visual(String anime) {
		AnimeVisual vis = new AnimeVisual(anime);
		return vis.getVisual();
	}

	/**
	 * @param g
	 * @return the ID of the given group, or -1 if the group is not in the list of
	 *         all groups.
	 */
	private static int getGroupID(Group g) {
		int groupID = -1;
		for (int ID : groupList.keySet()) {
			if (groupList.get(ID) == g) {
				groupID = ID;
			}
		}
		return groupID;
	}

	// ModifyingMethods-----------------------------------------------------------------

	/**
	 * Adds a * to the main windows title to indicate a possible change in data
	 * which has not yet been saved.
	 */
	public static void dataModified() {
		MainWindow w = MainWindow.thisWindow;
		String t = null;
		if (w != null) {
			t = w.getTitle();
		}
		if (t != null && !t.endsWith("*")) {
			w.setTitle(t + "*");
		}
	}

	/**
	 * @param anime
	 * @return true if the anime did not exist and was successfully created.
	 */
	public static boolean createAnime(String anime) {
		boolean exists = exists(anime);
		if (!exists) {
			dataModified();
			names.add(anime);
			sequels.add(null);
			episodes.add(new Integer(0));
			comments.add(new HashMap<Interval, String>());
			released.add(new RangeList(new Interval(1, 1)));
			downloaded.add(new RangeList());
			watched.add(new RangeList());
			airStatus.add(AirStatus.AIRING);
			periods.add(new Vector<Period>());
			hdds.add("0");
			groups.add(new TreeMap<Integer, RangeList>());
		}
		return !exists;
	}

	/**
	 * Deletes the given anime.
	 * 
	 * @param anime
	 * @return true if the anime did exist.
	 */
	public static boolean deleteAnime(String anime) {
		int index = names.indexOf(anime);
		if (index != -1) {
			dataModified();
			names.remove(index);
			sequels.remove(index);
			episodes.remove(index);
			comments.remove(index);
			released.remove(index);
			downloaded.remove(index);
			watched.remove(index);
			airStatus.remove(index);
			periods.remove(index);
			hdds.remove(index);
			groups.remove(index);
		}
		return index != -1;
	}

	/**
	 * Adds the given group to the group list, if it is not already in the list, and
	 * assigns an unused ID to it (ID is only used internally by this class).
	 * 
	 * @param g
	 */
	public static void createGroup(Group g) {
		if (!groupList.containsValue(g)) {
			dataModified();
			Set<Integer> ids = groupList.keySet();
			// searching for the lowest unused ID:
			int newID = 1;
			for (Integer i : ids) {
				if (i - newID > 0) {
					if (!ids.contains(newID)) {
						break;
					} else {
						newID = i; // should never occur
					}
				}
				newID++;
			}
			groupList.put(newID, g);
		}
	}

	/**
	 * Deletes the given group from the group list, from each anime, and from each
	 * other groups that contain the given group as constituent. (All references are
	 * deleted!)
	 * 
	 * @param g the group to delete
	 */
	public static void deleteGroup(Group g) {
		int id = getGroupID(g);
		dataModified();
		for (TreeMap<Integer, RangeList> subbers : groups) {
			if (subbers.containsKey(id)) {
				subbers.remove(id);
			}
		}
		for (Group group : groupList.values()) {
			if (group.getConstituents().contains(g)) {
				group.removeConstituent(g);
			}
		}
		groupList.remove(id);
	}

	/**
	 * @param anime   name of the anime to assign a new name to
	 * @param newName
	 * @return true if anime did exist and newName did not exist before, i.e. the
	 *         replacement was successful
	 */
	public static boolean newName(String anime, String newName) {
		boolean valid = exists(anime) && !exists(newName);
		if (valid) {
			dataModified();
			names.set(names.indexOf(anime), newName);
			int preqIndex = sequels.indexOf(anime);
			if (preqIndex != -1) {
				sequels.set(preqIndex, newName);
			}
		}
		return valid;
	}

	/**
	 * Set the sequel of the given anime. Set to null for no sequel.
	 * 
	 * @param anime
	 * @param sequel
	 * @return true if the sequel was set successfully, i.e. the anime and sequel
	 *         exist and the sequel did not already have a prequel.
	 */
	public static boolean setSequel(String anime, String sequel) {
		int i = names.indexOf(anime);
		boolean valid = (i != -1) && exists(sequel) && !hasPrequel(sequel);
		if (valid || sequel == null) {
			dataModified();
			sequels.set(i, sequel);
		}
		return valid;
	}

	/**
	 * Set the number of episodes of the given anime. Set to 0 for unknown number of
	 * episodes.
	 * 
	 * @param anime
	 * @param eps   for any value < 0, this method does nothing but return false
	 * @return false if the anime does not exist or if eps < 0.
	 */
	public static boolean setEpisodes(String anime, int eps) {
		int i = names.indexOf(anime);
		boolean valid = (i != -1) && (eps >= 0);
		if (valid) {
			dataModified();
			episodes.set(i, eps);
		}
		return valid;
	}

	// new implementation: comments
	/**
	 * Adds the given comment to the given episode interval of the given anime. If
	 * the interval already has a comment associated to it, it is overwritten. If
	 * the given comment is the empty string, the entry for the given interval is
	 * removed.
	 * 
	 * @param anime
	 * @param epInt
	 * @param comment
	 * @return true if the given anime exists.
	 */
	public static boolean setComment(String anime, Interval epInt, String comment) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			if (comment != "") {
				comments.get(i).put(new Interval(epInt.getLeft(), epInt.getRight()), comment);
			} else {
				comments.get(i).remove(epInt);
			}
		}
		return i != -1;
	}
	// new implementation: comments

	// TODO remove inconsistent functionality ~ rangelist
	/**
	 * Set the released episodes of the given anime to all episodes from 1 to rl.
	 * For rl = 0, the released-RangeList is overwritten with an empty RangeList.
	 * 
	 * @param anime
	 * @param rl    for any value < 0, this method does nothing but return false
	 * @return false if the anime does not exist or if rl < 0.
	 */
	public static boolean setReleased(String anime, int rl) {
		int i = names.indexOf(anime);
		boolean valid = (i != -1) && (rl >= 0);
		if (valid) {
			dataModified();
			if (rl > 0) {
				released.set(i, new RangeList(new Interval(1, rl)));
			} else {
				released.set(i, new RangeList());
			}
		}
		return valid;
	}

	// TODO remove inconsistent functionality ~ rangelist
	/**
	 * Set the downloaded episodes of the given anime to all episodes from 1 to dld.
	 * For dld = 0, the downloaded-RangeList is overwritten with an empty RangeList.
	 * 
	 * @param anime
	 * @param dld   for any value < 0, this method does nothing but return false
	 * @return false if the anime does not exist or if dld < 0.
	 */
	public static boolean setDownloaded(String anime, int dld) {
		int i = names.indexOf(anime);
		boolean valid = (i != -1) && (dld >= 0);
		if (valid) {
			dataModified();
			if (dld > 0) {
				downloaded.set(i, new RangeList(new Interval(1, dld)));
			} else {
				downloaded.set(i, new RangeList());
			}
		}
		return valid;
	}

	// TODO remove inconsistent functionality ~ rangelist
	/**
	 * Set the watched episodes of the given anime to all episodes from 1 to wt. For
	 * wt = 0, the watched-RangeList is overwritten with an empty RangeList.
	 * 
	 * @param anime
	 * @param wt    for any value < 0, this method does nothing but return false
	 * @return true if the anime exists.
	 */
	public static boolean setWatched(String anime, int wt) {
		int i = names.indexOf(anime);
		boolean valid = (i != -1) && (wt >= 0);
		if (valid) {
			dataModified();
			if (wt > 0) {
				watched.set(i, new RangeList(new Interval(1, wt)));
			} else {
				watched.set(i, new RangeList());
			}
		}
		return valid;
	}

	/**
	 * Set the air status of the given anime.
	 * 
	 * @param anime
	 * @param airStat
	 * @return true if the anime exists.
	 */
	public static boolean setAirStatus(String anime, AirStatus airStat) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			airStatus.set(i, airStat);
		}
		return i != -1;
	}

	/**
	 * Set the HDD of the given anime. To remove the HDD from the given anime, use
	 * null for hdd.
	 * 
	 * @param anime
	 * @param hdd
	 * @return true if the anime exists.
	 */
	public static boolean setHdd(String anime, String hdd) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			hdds.set(i, hdd);
		}
		return i != -1;
	}

	/* OLD 1
	 * Sets the only period of the given anime to be the given
	 * period or removes all periods if the given period is null.
	 * @param anime
	 * @param p
	 * @return true if the anime exists.
	 
	public static boolean setPeriod(String anime, Period p) {
		int i = names.indexOf(anime);
		if(i != -1) {
			Vector<Period> newP = new Vector<Period>();
			if(p != null) {
				newP.add(p);
			}
			periods.set(i, newP);
		}
		return i != -1;
	}*/

	/**
	 * Adds a new period with the given startDate to the given anime, if the
	 * startDate does not already exist.
	 * 
	 * @param anime
	 * @param startDate
	 * @return true if the period was added successfully.
	 */
	public static boolean addPeriod(String anime, Date startDate) {
		boolean success = true;
		int i = names.indexOf(anime);
		if (i != -1) {
			Vector<Period> spans = periods.get(i);
			for (Period p : spans) {
				if (p.startDate().equals(startDate)) {
					success = false;
					break;
				}
			}
			if (success) {
				dataModified();
				spans.add(new Period(startDate, null));
			}
		} else {
			success = false;
		}
		return success;
	}

	/**
	 * Sets the end date of the period with the given start date of the given anime
	 * to finDate.
	 * 
	 * @param anime
	 * @param startDate
	 * @param finDate
	 * @return true if the end date was set successfully, i.e. the anime and start
	 *         date exist, and finDate is a valid end date for the existing start
	 *         date.
	 */
	public static boolean setFinDate(String anime, Date startDate, Date finDate) {
		boolean success = false;
		int i = names.indexOf(anime);
		if (i != -1) {
			for (Period p : periods.get(i)) {
				if (p.startDate().equals(startDate)) {
					try {
						p.setEndDate(finDate);
						dataModified();
						success = true;
					} catch (IllegalArgumentException e) {
					}
				}
			}
		}
		return success;
	}

	/**
	 * Removes the period with the given startDate from the given anime.
	 * 
	 * @param anime
	 * @param date
	 * @return true if the period was successfully removed, i.e. the anime and
	 *         period did exist.
	 */
	public static boolean removePeriod(String anime, Date startDate) {
		boolean success = false;
		int i = names.indexOf(anime);
		if (i != -1) {
			Vector<Period> spans = periods.get(i);
			Period toRemove = null;
			for (Period p : spans) {
				if (p.startDate().equals(startDate)) {
					toRemove = p;
					break;
				}
			}
			if (toRemove != null) {
				dataModified();
				spans.remove(toRemove);
				success = true;
			}
		}
		return success;
	}

	/**
	 * Adds the interval [start, end] to the list of subbed episodes of the given
	 * group. If there is already an entry for the group, the new interval is united
	 * with all the existing intervals.
	 * 
	 * @param anime
	 * @param groupID
	 * @param start
	 * @param end
	 * @return true if the anime exists.
	 */
	public static boolean addGroup(String anime, Group group, int start, int end) {
		int i = names.indexOf(anime);
		int groupID = getGroupID(group);
		if (i != -1 && groupID != -1) {
			dataModified();
			TreeMap<Integer, RangeList> grps = groups.get(i);
			if (grps.containsKey(groupID)) {
				grps.get(groupID).addInterval(new Interval(start, end));
			} else {
				grps.put(groupID, new RangeList(new Interval(start, end)));
			}
		}
		return i != -1;
	}

	/**
	 * Removes the interval [start, end] from the list of subbed episodes of the
	 * given group. If the group does not exist, do nothing. If no episodes are left
	 * after removing, the group is removed from the list.
	 * 
	 * @param anime
	 * @param group
	 * @param start
	 * @param end
	 * @return true if the given anime and group exist.
	 */
	public static boolean removeGroup(String anime, Group group, int start, int end) {
		int i = names.indexOf(anime);
		int groupID = getGroupID(group);
		boolean success = false;
		if (i != -1 && groupID != -1) {
			dataModified();
			TreeMap<Integer, RangeList> grps = groups.get(i);
			if (grps.containsKey(groupID)) {
				dataModified();
				success = true;
				RangeList subbed = grps.get(groupID);
				subbed.subtractInterval(new Interval(start, end));
				if (subbed.isEmpty()) {
					grps.remove(groupID);
				}
			}
		}
		return success;
	}

	// TODO fix inconsistent functionality
	/**
	 * Adds the next biggest episode to the list of watched episodes, i.e. adds the
	 * integer which is equal to [the biggest integer in the watched-RangeList + 1]
	 * to the watched-RangeList. If the watched-RangeList is empty, the interval
	 * [1,1] is added. Does nothing if the given anime does not exist.
	 * 
	 * @param anime
	 * @return true if the anime does exist.
	 */
	public static boolean watchedNext(String anime) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			RangeList wt = watched.get(i);
			int next;
			try {
				next = wt.getBiggest() + 1;
			} catch (EmptyRangeListException e) {
				// no episodes watched -> 0
				next = 1;
			}
			wt.addInterval(new Interval(next, next));
		}
		return i != -1;
	}

	// TODO fix inconsistent functionality
	/**
	 * Adds the next biggest episode to the list of downloaded episodes, i.e. adds
	 * the integer which is equal to [the biggest integer in the
	 * downloaded-RangeList + 1] to the downloaded-RangeList. If the
	 * downloaded-RangeList is empty, the interval [1,1] is added. Does nothing if
	 * the given anime does not exist.
	 * 
	 * @param anime
	 * @return true if the anime does exist.
	 */
	public static boolean downloadedNext(String anime) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			RangeList dld = downloaded.get(i);
			int next;
			try {
				next = dld.getBiggest() + 1;
			} catch (EmptyRangeListException e) {
				// no episodes downloaded -> 0
				next = 1;
			}
			dld.addInterval(new Interval(next, next));
		}
		return i != -1;
	}

	public static void incrementReleased(String anime) {
		int i = names.indexOf(anime);
		if (i != -1) {
			dataModified();
			int released = releasedNum(anime);
			setReleased(anime, released + 1);
			TreeMap<Integer, RangeList> animeGroups = groups.get(i);
			for (Integer groupID : animeGroups.keySet()) {
				RangeList subbed = animeGroups.get(groupID);
				try {
					if (subbed.getBiggest() == released) {
						subbed.addInterval(new Interval(released + 1, released + 1));
					}
				} catch (EmptyRangeListException e) {
					// ignore
				}
			}
		}
	}

}
