package obj;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Group {

	private String displayName;
	private TreeSet<String> names;
	private String comments;
	private Vector<Group> constituents;

	public Group(String name) {
		displayName = name;
		names = new TreeSet<String>();
		names.add(name);
		comments = "";
		constituents = new Vector<Group>();
	}

	public Group(Set<String> names, String displayName, Vector<Group> constituents, String comments) {
		this.names = new TreeSet<String>(names);
		setDisplayName(displayName);
		this.constituents = new Vector<Group>(constituents);
		this.comments = comments;
	}

	// ---------------------------------modifying
	// methods----------------------------------

	/**
	 * Sets the displayed name of this group to the given name and add the given
	 * name to the list of names of this group, if it is not already on the list.
	 * 
	 * @param name
	 */
	public void setDisplayName(String name) {
		AnimeData.dataModified();
		displayName = name;
		if (!names.contains(name)) {
			names.add(name);
		}
	}

	/**
	 * Adds the given name to the list of names of this group, if it is not already
	 * on the list.
	 * 
	 * @param name
	 * @return true if the given name was not already on the list of names of this
	 *         group.
	 */
	public boolean addName(String name) {
		AnimeData.dataModified();
		return names.add(name);
	}

	/**
	 * If this group has at least 2 names, removes the given name from the list of
	 * names of this group. If the displayed name for this group is equal to the
	 * removed name, it is changed to the next name in the list of names of this
	 * group.
	 * 
	 * @param name
	 * @return true if the name was successfully removed from the list of names of
	 *         this group.
	 */
	public boolean removeName(String name) {
		boolean success = names.size() > 1;
		if (success) {
			AnimeData.dataModified();
			success = names.remove(name);
			if (!names.contains(displayName)) {
				displayName = names.first();
			}
		}
		return success;
	}

	/**
	 * Adds the given group to the list of constituents of this group, if it is not
	 * already on the list.
	 * 
	 * @param g
	 * @return true if the list of constituents of this group did not already
	 *         contain the given group.
	 */
	public boolean addConstituent(Group g) {
		boolean exists = constituents.contains(g);
		if (!exists) {
			AnimeData.dataModified();
			constituents.add(g);
		}
		return !exists;
	}

	/**
	 * Removes the given group from the list of constituents of this group, if it
	 * exists.
	 * 
	 * @param g
	 * @return true if the given group was successfully removed from the list of
	 *         constituents of this group.
	 */
	public boolean removeConstituent(Group g) {
		AnimeData.dataModified();
		return constituents.remove(g);
	}

	public void setComments(String c) {
		AnimeData.dataModified();
		comments = c;
	}

	// ------------------------getter Methods----------------------

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return a TreeSet of the names of this group. Changes to the vector are not
	 *         reflected in the group.
	 */
	public TreeSet<String> getNames() {
		TreeSet<String> tmp = new TreeSet<>(names);
		return tmp;
	}

	public String getComments() {
		return comments;
	}

	/**
	 * @return a List of all groups that, together, form this group. Changes to the
	 *         List are not reflected in the group.
	 */
	public List<Group> getConstituents() {
		List<Group> constitsCopy = new Vector<Group>(constituents);
		return constitsCopy;
	}

	public String toString() {
		return displayName;
	}

}
