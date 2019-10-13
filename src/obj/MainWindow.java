package obj;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import tools.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import obj.AnimeData.AirStatus;
import format.date.Date;
import format.date.InvalidDateException;
import format.date.Period;
import format.date.UnknownDateException;
import format.interval.Interval;
import format.interval.RangeList;

public class MainWindow extends JFrame implements ActionListener, KeyListener, OpensTextFieldWindow {

	// For other classes to change the title of this Window:
	public static MainWindow thisWindow;

	// Constants
	final static long serialVersionUID = 114942L;
	public static final Color BGCOLOR = new Color(255, 255, 255);
	public static final Color SEQUELCONNECTCOLOR = new Color(130, 130, 130);

	public static enum AnimeSort {
		NAME, EP, STDATE, FINDATE
	};

	public static enum GroupSort {
		NAME, ANIME, EP
	} // NAME sorts by displayed name,
		// ANIME by # of subbed animes, EP by # of subbed eps

	public static enum Order {
		ASC, DESC
	};

	public static enum View {
		ANIME, TOWATCH, TODOWNLOAD, DATESWATCHED, DATESWATCHING, HDD, GROUPS
	};

	// Variables
	private static AnimeSort animeSort = AnimeSort.NAME;
	private static GroupSort groupSort = GroupSort.NAME;
	private static Order order = Order.ASC;
	private static View view = View.ANIME;
	private static String searchFilter = "";
	private static boolean searchBeginOnly = false;
	private static boolean hddSequelCombine = false;
	private static File saveFile;

	// ---------------------------------------- Window Components
	// ---------------------------------------- //

	private static JScrollPane pScroll = new JScrollPane();
	private static GridBagLayout gridBag = new GridBagLayout();
	private static GridBagConstraints gridConst = new GridBagConstraints();
	private static JPanel main = new JPanel(gridBag);
	private static JCheckBoxMenuItem airStFin = new JCheckBoxMenuItem("Show Finished", true);
	private static JCheckBoxMenuItem airStAir = new JCheckBoxMenuItem("Show Airing", true);
	private static JCheckBoxMenuItem airStSoon = new JCheckBoxMenuItem("Show Soon", true);
	private static JMenuItem[] aReg = { new JMenuItem("All"), new JMenuItem("A"), new JMenuItem("B"), new JMenuItem("C"),
			new JMenuItem("D"), new JMenuItem("E"), new JMenuItem("F"), new JMenuItem("G"), new JMenuItem("H"), new JMenuItem("I"),
			new JMenuItem("J"), new JMenuItem("K"), new JMenuItem("L"), new JMenuItem("M"), new JMenuItem("N"), new JMenuItem("O"),
			new JMenuItem("P"), new JMenuItem("Q"), new JMenuItem("R"), new JMenuItem("S"), new JMenuItem("T"), new JMenuItem("U"),
			new JMenuItem("V"), new JMenuItem("W"), new JMenuItem("X"), new JMenuItem("Y"), new JMenuItem("Z") };

	Image windowIcon;

	// ---------------------------------------- Focus handling
	private static FocusTraversalPolicy defaultFocusTraversalPolicy;
	private static int focusedComponentIndex = -1; // bad design .. AnimeVisual component knows nothing about AnimeData

	// ---------------------------------------- Frame Constructor
	// ---------------------------------------- //
	public MainWindow(String title) {

		super(title);
//		UIManager.getDefaults().put("ComboBox.selectionBackground", new ColorUIResource(BGCOLOR));
		try {
			windowIcon = ImageIO.read(getClass().getResource("/res/adb.jpg"));
			this.setIconImage(windowIcon);
		} catch (IOException e) {
		}
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height - 30);
		setLocation(0, 0);
		setJMenuBar(createMenuBar());
		setLayout(new GridLayout(1, 1));
		getContentPane().add(pScroll);
		pScroll.setViewportView(main);
		pScroll.getVerticalScrollBar().setUnitIncrement(30);
		main.setBackground(BGCOLOR);
		gridConst.anchor = GridBagConstraints.WEST;
		gridConst.fill = GridBagConstraints.NONE;
		gridConst.gridx = 0;
		gridConst.insets = new Insets(0, 0, 10, 10);

		addKeyListener(this);
		setFocusable(true);

		defaultFocusTraversalPolicy = getFocusTraversalPolicy();

		setResizable(true);
		setVisible(true);
	}

	// ---------------------------------------- Menu Creation
	// ---------------------------------------- //

	public void actionPerformed(ActionEvent e) {
		// File Menu
		if (e.getActionCommand().equals("createAnime")) {
			new TextFieldWindow(this, "New Anime", "Name", 0, "");
		}
		if (e.getActionCommand().equals("createGroup")) {
			createGroup();
		}
		if (e.getActionCommand().equals("deleteAnime")) {
			delete();
		}
		if (e.getActionCommand().equals("deleteGroup")) {
			deleteGroup();
		}
		if (e.getActionCommand().equals("save")) {
			save();
		}
		if (e.getActionCommand().equals("saveAs")) {
			saveAs();
		}
		if (e.getActionCommand().equals("load")) {
			load();
		}
		// Sort Menu
		if (e.getActionCommand().equals("sortAscending")) {
			changeOrder(Order.ASC);
		}
		if (e.getActionCommand().equals("sortDescending")) {
			changeOrder(Order.DESC);
		}
		if (e.getActionCommand().equals("sortName")) {
			changeAnimeSort(AnimeSort.NAME);
		}
		if (e.getActionCommand().equals("sortEpisodes")) {
			changeAnimeSort(AnimeSort.EP);
		}
		if (e.getActionCommand().equals("sortStDate")) {
			changeAnimeSort(AnimeSort.STDATE);
		}
		if (e.getActionCommand().equals("sortFinDate")) {
			changeAnimeSort(AnimeSort.FINDATE);
		}
		// TODO group sorts
		// View Menu
		if (e.getActionCommand().equals("viewAnimes")) {
			changeView(View.ANIME);
		}
		if (e.getActionCommand().equals("viewtoWatch")) {
			changeView(View.TOWATCH);
		}
		if (e.getActionCommand().equals("viewtoDownload")) {
			changeView(View.TODOWNLOAD);
		}
		if (e.getActionCommand().equals("viewDatesWatched")) {
			changeView(View.DATESWATCHED);
		}
		if (e.getActionCommand().equals("viewDatesWatching")) {
			changeView(View.DATESWATCHING);
		}
		if (e.getActionCommand().equals("viewHdd")) {
			changeView(View.HDD);
		}
		if (e.getActionCommand().endsWith("viewGroups")) {
			changeView(View.GROUPS);
		}
		// Open Search
		if (e.getActionCommand().equals("openSearch")) {
			searchBeginOnly = false;
			new TextFieldWindow(this, "Search", "String", 2, "");
		}
		if (e.getActionCommand().equals("openBeginSearch")) {
			searchBeginOnly = true;
			new TextFieldWindow(this, "Search", "String", 2, "");
		}
		if (e.getActionCommand().equals("airStatusSearch")) {
			refreshView();
		}
		// Alphabetic Register
		if (e.getActionCommand().equals("AlphabeticRegister")) {
			JMenuItem i = (JMenuItem) e.getSource();
			searchFilter = i.getText();
			if (searchFilter.equals("All")) {
				searchFilter = "";
			}
			searchBeginOnly = true;
			for (int j = 1; j < aReg.length; j++) {
				aReg[j].setAccelerator(null);
			}
			for (int j = 0; j < aReg.length; j++) {
				if (aReg[j].getText().equals(searchFilter)) {
					if (j > 1) {
						aReg[j - 1]
								.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(','), KeyEvent.CTRL_DOWN_MASK));
					}
					if (j < 26) {
						aReg[j + 1]
								.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('.'), KeyEvent.CTRL_DOWN_MASK));
					}
					aReg[j].setAccelerator(null);
					break;
				}
				if (j == aReg.length - 1) {
					aReg[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('.'), KeyEvent.CTRL_DOWN_MASK));
					aReg[aReg.length - 1]
							.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(','), KeyEvent.CTRL_DOWN_MASK));
				}
			}
			refreshView();
		}
	}

	private JMenuBar createMenuBar() {

		JMenuBar menubar = new JMenuBar();

		// Initializing components
		JMenu file = new JMenu("File");
		JMenu create = new JMenu("Create");
		JMenuItem createAnime = new JMenuItem("Create Anime..");
		JMenuItem createGroup = new JMenuItem("Create Group..");
		JMenuItem delete = new JMenu("Delete");
		JMenuItem deleteAnime = new JMenuItem("Delete Anime..");
		JMenuItem deleteGroup = new JMenuItem("Delete Group..");
		JMenuItem save = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save As..");
		JMenuItem load = new JMenuItem("Load..");

		JMenu sort = new JMenu("Sort");
		JMenuItem sortAscending = new JMenuItem("Ascending");
		JMenuItem sortDescending = new JMenuItem("Descending");
		JMenuItem sortName = new JMenuItem("Name");
		JMenuItem sortEpisodes = new JMenuItem("Episodes");
		JMenu sortDates = new JMenu("Dates");
		JMenuItem sortStDate = new JMenuItem("Start");
		JMenuItem sortFinDate = new JMenuItem("Finish");
		// TODO group sorts
		JMenuItem openSearch = new JMenuItem("Search");
		JMenuItem openBeginSearch = new JMenuItem("Search Beginning");

		JMenu view = new JMenu("View");
		JMenuItem viewAnimes = new JMenuItem("Animes");
		JMenuItem viewtoWatch = new JMenuItem("To Watch");
		JMenuItem viewtoDownload = new JMenuItem("To Download");
		JMenu viewDates = new JMenu("Dates");
		JMenuItem viewDatesWatched = new JMenuItem("Watched");
		JMenuItem viewDatesWatching = new JMenuItem("Watching");
		JMenuItem viewHdd = new JMenuItem("HDD's");
		JMenuItem viewGroups = new JMenuItem("Groups");

		// File Menu - make hierarchy
		file.add(create);
		create.add(createAnime);
		createAnime.setMnemonic(KeyEvent.VK_A);
		create.add(createGroup);
		createGroup.setMnemonic(KeyEvent.VK_G);
		file.add(delete);
		delete.add(deleteAnime);
		delete.add(deleteGroup);
		file.addSeparator();
		file.add(save);
		file.add(saveAs);
		file.add(load);

		// Sort Menu - make hierarchy
		sort.add(sortAscending);
		sort.add(sortDescending);
		sort.addSeparator();
		sort.add(sortName);
		sort.add(sortEpisodes);
		sort.add(sortDates);
		sortDates.add(sortStDate);
		sortDates.add(sortFinDate);
		sort.addSeparator();
		sort.add(airStFin);
		sort.add(airStAir);
		sort.add(airStSoon);
		sort.addSeparator();
		// TODO group sorts
		sort.addSeparator();
		sort.add(openSearch);
		sort.add(openBeginSearch);

		// View Menu - make hierarchy
		view.add(viewAnimes);
		view.add(viewtoWatch);
		view.add(viewtoDownload);
		view.add(viewDates);
		viewDates.add(viewDatesWatched);
		viewDates.add(viewDatesWatching);
		view.add(viewHdd);
		view.add(viewGroups);

		// File Menu - set actions
		file.setPreferredSize(new Dimension(50, 20));
		file.setMnemonic(KeyEvent.VK_F);
		createAnime.addActionListener(this);
		createAnime.setActionCommand("createAnime");
		createGroup.addActionListener(this);
		createGroup.setActionCommand("createGroup");
		deleteAnime.addActionListener(this);
		deleteAnime.setActionCommand("deleteAnime");
		deleteGroup.addActionListener(this);
		deleteGroup.setActionCommand("deleteGroup");
		save.addActionListener(this);
		save.setActionCommand("save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		saveAs.addActionListener(this);
		saveAs.setActionCommand("saveAs");
		load.addActionListener(this);
		load.setActionCommand("load");

		// Sort Menu - set actions
		sort.setPreferredSize(new Dimension(50, 20));
		sort.setMnemonic(KeyEvent.VK_S);
		sortAscending.addActionListener(this);
		sortAscending.setActionCommand("sortAscending");
		sortDescending.addActionListener(this);
		sortDescending.setActionCommand("sortDescending");
		sortName.addActionListener(this);
		sortName.setActionCommand("sortName");
		sortEpisodes.addActionListener(this);
		sortEpisodes.setActionCommand("sortEpisodes");
		sortStDate.addActionListener(this);
		sortStDate.setActionCommand("sortStDate");
		sortFinDate.addActionListener(this);
		sortFinDate.setActionCommand("sortFinDate");
		airStFin.addActionListener(this);
		airStFin.setActionCommand("airStatusSearch");
		airStAir.addActionListener(this);
		airStAir.setActionCommand("airStatusSearch");
		airStSoon.addActionListener(this);
		airStSoon.setActionCommand("airStatusSearch");
		// TODO group sorts
		openSearch.addActionListener(this);
		openSearch.setActionCommand("openSearch");
		openSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		openBeginSearch.addActionListener(this);
		openBeginSearch.setActionCommand("openBeginSearch");
		openBeginSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK));

		// View Menu - set actions
		view.setPreferredSize(new Dimension(50, 20));
		view.setMnemonic(KeyEvent.VK_V);
		viewAnimes.addActionListener(this);
		viewAnimes.setActionCommand("viewAnimes");
		viewAnimes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
		viewtoWatch.addActionListener(this);
		viewtoWatch.setActionCommand("viewtoWatch");
		viewtoWatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
		viewtoDownload.addActionListener(this);
		viewtoDownload.setActionCommand("viewtoDownload");
		viewtoDownload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
		viewDatesWatched.addActionListener(this);
		viewDatesWatched.setActionCommand("viewDatesWatched");
		viewDatesWatching.addActionListener(this);
		viewDatesWatching.setActionCommand("viewDatesWatching");
		viewHdd.addActionListener(this);
		viewHdd.setActionCommand("viewHdd");
		viewGroups.addActionListener(this);
		viewGroups.setActionCommand("viewGroups");
		viewGroups.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));

		// Alphabetic Register
		JMenu aRegMenu = new JMenu("Starts with..");
		for (int i = 0; i < aReg.length; i++) {
			aReg[i].setActionCommand("AlphabeticRegister");
			aReg[i].addActionListener(this);
			aRegMenu.add(aReg[i]);
		}
		aReg[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('-'), KeyEvent.CTRL_DOWN_MASK));

		menubar.add(file);
		menubar.add(sort);
		menubar.add(view);
		menubar.add(aRegMenu);
		return menubar;
	}

	// ---------------------------------------- Main
	// ---------------------------------------- //

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		try {
			String title = "";
			if (args.length == 1) {
				saveFile = new File(new File("").getAbsolutePath() + "\\" + args[0]);
				title = " - " + saveFile.getName().replaceAll(".ads", "");
				loadSaveFile();
			}
			thisWindow = new MainWindow("AnimeDB" + title);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------------- File Menu
	// ----------------------------------------

	public void readTextFieldWindowString(String toRead, int refID) {
		switch (refID) {
		case 0:
			AnimeData.createAnime(toRead);
			break;
		case 2:
			searchFilter = toRead;
			focusedComponentIndex = -1;
			break;
		}
		refreshView();
	}

	private void createGroup() {
		JDialog createDialog = new JDialog(this);
		createDialog.setTitle("New Group");
		createDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		GroupDialog d = new GroupDialog(createDialog);
		createDialog.setContentPane(d);
		createDialog.setSize(450, 600);
		createDialog.setLocationRelativeTo(null);
		createDialog.setResizable(false);
		createDialog.setVisible(true);

		if (d.getStatus() == true) {
			Group g = new Group(d.getNames(), d.getDisplayName(), d.getConstituents(), d.getComment());
			AnimeData.createGroup(g);
		}

	}

	private void delete() {
		final JComboBox<String> cNameList = new JComboBox<String>(new Vector<String>(AnimeData.nameList()));
		final JFrame f = new JFrame("Choose Name");
		final JButton confirm = new JButton("Delete");
		final JButton cancel = new JButton("Cancel");
		cNameList.setBounds(5, 5, 200, 25);
		confirm.setBounds(5, 35, 95, 25);
		cancel.setBounds(110, 35, 95, 25);

		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(215, 92);
		f.setLocationRelativeTo(null);
		f.setLayout(null);
		f.add(cNameList);
		f.add(confirm);
		f.add(cancel);
		confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnimeData.deleteAnime(cNameList.getSelectedItem().toString());
				f.dispose();
				refreshView();
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		f.validate();
		f.setVisible(true);
		f.setResizable(false);
	}

	private void deleteGroup() {
		final JComboBox<Group> cNameList = new JComboBox<Group>(AnimeData.groups());
		final JFrame f = new JFrame("Choose Group");
		final JButton confirm = new JButton("Delete");
		final JButton cancel = new JButton("Cancel");
		cNameList.setBounds(5, 5, 200, 25);
		confirm.setBounds(5, 35, 95, 25);
		cancel.setBounds(110, 35, 95, 25);

		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(215, 92);
		f.setLocationRelativeTo(null);
		f.setLayout(null);
		f.add(cNameList);
		f.add(confirm);
		f.add(cancel);
		confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnimeData.deleteGroup((Group) cNameList.getSelectedItem());
				f.dispose();
				refreshView();
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		f.validate();
		f.setVisible(true);
		f.setResizable(false);
	}

	private void saveAs() {
		File saveDir = new File(System.getProperty("user.dir"));
		saveDir.mkdir();
		JFileChooser fc = new JFileChooser(saveDir);
		fc.setLocale(Locale.ENGLISH);
		fc.setApproveButtonText("Save");
		switch (fc.showOpenDialog(null)) {
		case JFileChooser.APPROVE_OPTION:
			saveFile = fc.getSelectedFile();
			save();
			break;
		case JFileChooser.CANCEL_OPTION:
			break;
		}
	}

	private void save() {
		if (saveFile != null) {
			try {
				AnimeData.toFile(saveFile);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(thisWindow, "Error: Could not save.", "Error", JOptionPane.ERROR_MESSAGE, null);
			}
		} else {
			saveAs();
		}
	}

	private void load() {
		File loadDir = new File(System.getProperty("user.dir"));
		loadDir.mkdir();
		JFileChooser fc = new JFileChooser(loadDir);
		fc.setLocale(Locale.ENGLISH);
		fc.setApproveButtonText("Load");
		switch (fc.showOpenDialog(null)) {
		case JFileChooser.APPROVE_OPTION:
			try {
				saveFile = fc.getSelectedFile();
				setTitle("AnimeDB - " + saveFile.getName().replaceAll(".ads", ""));
				loadSaveFile();
			} catch (NullPointerException e) {
			}
			break;
		case JFileChooser.CANCEL_OPTION:
			break;
		}
	}

	private static void loadSaveFile() {
		try {
			AnimeData.loadFile(saveFile);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(thisWindow, "Error: Could not load.", "Error", JOptionPane.ERROR_MESSAGE, null);
		}
	}

	// ---------------------------------------- Refresh Methods
	// ---------------------------------------- //

	private void changeAnimeSort(AnimeSort filter) {
		animeSort = filter;
		refreshView();
	}

	private void changeGroupSort(GroupSort filter) {
		groupSort = filter;
		refreshView();
	}

	private void changeOrder(Order neworder) {
		order = neworder;
		refreshView();
	}

	private void changeView(View toview) {
		view = toview;
		if (view == View.ANIME) {
			airStFin.setEnabled(true);
			airStAir.setEnabled(true);
			airStSoon.setEnabled(true);
		} else {
			airStFin.setSelected(true);
			airStAir.setSelected(true);
			airStSoon.setSelected(true);
			airStFin.setEnabled(false);
			airStAir.setEnabled(false);
			airStSoon.setEnabled(false);
		}
		refreshView();
	}

	/**
	 * @param sortOption
	 * @param orderOption
	 * @return a Vector containing all anime names, where the ordering is specified
	 *         by sortOption and orderOption.
	 */
	private static Vector<String> sortedAnimes(AnimeSort sortOption, Order orderOption) {
		Vector<String> sortedAnimes = null;

		SortedSet<String> nameList = AnimeData.nameList();
		if (sortOption == AnimeSort.NAME) {
			sortedAnimes = new Vector<String>(nameList);
		}
		if (sortOption == AnimeSort.EP) {
			TreeMap<Integer, TreeSet<String>> sorted = new TreeMap<Integer, TreeSet<String>>();
			for (String animeName : nameList) {
				int key = AnimeData.episodes(animeName);
				if (!sorted.containsKey(key)) {
					sorted.put(key, new TreeSet<String>());
				}
				sorted.get(key).add(animeName);
			}

			sortedAnimes = new Vector<String>();
			for (TreeSet<String> set : sorted.values()) {
				for (String anime : set) {
					sortedAnimes.add(anime);
				}
			}
		}
		if (sortOption == AnimeSort.STDATE || sortOption == AnimeSort.FINDATE) {
			TreeMap<Date, TreeSet<String>> sorted = new TreeMap<Date, TreeSet<String>>();
			boolean hasNoDate = false;
			for (String animeName : nameList) {
				Date key = null;
				try {
					Period lastEntry = AnimeData.watchedSpans(animeName).lastElement();
					if (sortOption == AnimeSort.STDATE) {
						key = lastEntry.startDate();
					} else if (sortOption == AnimeSort.FINDATE) {
						key = lastEntry.endDate();
					}
					if (!sorted.containsKey(key)) {
						sorted.put(key, new TreeSet<String>());
					}
					sorted.get(key).add(animeName);
				} catch (NoSuchElementException e) {
					hasNoDate = true;
					// if no dates exist
				} catch (NullPointerException e) {
					hasNoDate = true;
					// if startdate exists, but not findate
				}
				if (hasNoDate) {
					try {
						hasNoDate = false;
						;
						// put all without a date at Date 00.00.0000
						Date noDate = new Date(0, 0, 0);
						if (!sorted.containsKey(noDate)) {
							sorted.put(noDate, new TreeSet<String>());
						}
						sorted.get(noDate).add(animeName);
					} catch (InvalidDateException e) {
					} // never occurs since 00.00.0000 is valid
				}
			}
			sortedAnimes = new Vector<String>();
			for (TreeSet<String> set : sorted.values()) {
				for (String anime : set) {
					sortedAnimes.add(anime);
				}
			}
		}
		if (orderOption == Order.DESC) {
			Collections.reverse(sortedAnimes);
		}
		return sortedAnimes;
	}

	/**
	 * @param sortOption
	 * @param orderOption
	 * @return a Vector containing all groups, where the ordering is specified by
	 *         sortOption and orderOption.
	 */
	private static Vector<Group> sortedGroups(GroupSort sortOption, Order orderOption) {
		Vector<Group> sorted = null;

		Vector<Group> groupList = AnimeData.groups();
		if (sortOption == GroupSort.NAME) {
			sorted = groupList;
		} else if (sortOption == GroupSort.EP || sortOption == GroupSort.ANIME) {
			TreeMap<Integer, Vector<Group>> sortedBlocks = new TreeMap<Integer, Vector<Group>>();
			for (Group g : groupList) {
				int key = 0;
				for (String anime : AnimeData.nameList()) {
					HashMap<Group, RangeList> subbed = AnimeData.groups(anime);
					if (subbed.containsKey(g)) {
						if (sortOption == GroupSort.ANIME) {
							// TODO option to not count movies?
							key++;
						} else if (sortOption == GroupSort.EP) {
							// TODO count specials
							key += subbed.get(g).size();
						}
					}
				}

				if (!sortedBlocks.containsKey(key)) {
					sortedBlocks.put(key, new Vector<Group>());
				}
				sortedBlocks.get(key).add(g);
			}

			sorted = new Vector<Group>();
			for (Vector<Group> gs : sortedBlocks.values()) {
				for (Group g : gs) {
					sorted.add(g);
				}
			}
		}

		if (orderOption == Order.DESC) {
			Collections.reverse(sorted);
		}
		return sorted;
	}

	/**
	 * @return a Vector containing the names of those anime, that pass the currently
	 *         set search filter. The name-filter is applied case-insensitive. The
	 *         ordering is specified by the currently set sort and order settings.
	 */
	private static Vector<String> searchAnimes() {
		Vector<String> sorted = sortedAnimes(animeSort, order);

		Vector<String> searchCache = new Vector<String>();
		while (!sorted.isEmpty()) {
			String toProcess = sorted.firstElement();
			AirStatus airStatus = AnimeData.airStatus(toProcess);
			if (((searchBeginOnly && toProcess.toLowerCase().startsWith(searchFilter.toLowerCase()))
					|| (!searchBeginOnly && toProcess.toLowerCase().indexOf(searchFilter.toLowerCase()) != -1))
					&& ((airStFin.isSelected() && airStatus == AirStatus.FINISHED)
							|| (airStAir.isSelected() && airStatus == AirStatus.AIRING)
							|| (airStSoon.isSelected() && airStatus == AirStatus.SOON))) {
				searchCache.add(toProcess);
			}
			sorted.removeElementAt(0);
		}
		return searchCache;
	}

	/**
	 * @return a Vector containing the groups that pass the currently set search
	 *         filter. The name-filter is applied case-insensitive. The ordering is
	 *         specified by the currently set sort and order settings.
	 */
	private static Vector<Group> searchGroups() {
		Vector<Group> sorted = sortedGroups(groupSort, order);

		Vector<Group> searchCache = new Vector<>();
		while (!sorted.isEmpty()) {
			Group toProcess = sorted.firstElement();
			String currName = toProcess.getDisplayName();
			if ((searchBeginOnly && currName.toLowerCase().startsWith(searchFilter.toLowerCase()))
					|| (!searchBeginOnly && currName.toLowerCase().indexOf(searchFilter.toLowerCase()) != -1)) {
				searchCache.add(toProcess);
			}
			sorted.removeElementAt(0);
		}
		return searchCache;
	}

	private static void scrollAnimeViewToComponent(JPanel scrollTo) {
		try {
			scrollTo.scrollRectToVisible(new Rectangle(scrollTo.getSize()));
//			main.scrollRectToVisible(scrollTo.getBounds());
		} catch (ArrayIndexOutOfBoundsException e) {
//			ignore
		}
	}

	private static Vector<Component> animeViewComponentList() {
		Vector<Component> all = new Vector<>();
		for (Component row : main.getComponents()) {
			for (Component anime : ((JPanel) row).getComponents()) {
				all.add(anime);
			}
		}
		return all;
	}

	private static void setAnimeViewFocusTraversalPolicy() {
		thisWindow.setFocusTraversalPolicy(new FocusTraversalPolicy() {

			public Component getLastComponent(Container aContainer) {
				Vector<Component> all = animeViewComponentList();
				if (all.size() > 0) {
					focusedComponentIndex = all.size() - 1;
					Component focusedComponent = all.lastElement();
					scrollAnimeViewToComponent((JPanel) focusedComponent);
					return focusedComponent;
				}
				return main;
			}

			public Component getFirstComponent(Container aContainer) {
				Vector<Component> all = animeViewComponentList();
				if (all.size() > 0) {
					focusedComponentIndex = 0;
					Component focusedComponent = all.firstElement();
					scrollAnimeViewToComponent((JPanel) focusedComponent);
					return focusedComponent;
				}
				return main;
			}

			public Component getDefaultComponent(Container aContainer) {
				return getFirstComponent(aContainer);
			}

			public Component getComponentBefore(Container aContainer, Component aComponent) {
				Vector<Component> all = animeViewComponentList();
				int i = all.indexOf(aComponent);
				if (i != -1) {
					int beforeIndex = (i + all.size() - 1) % all.size();
					focusedComponentIndex = beforeIndex;
					Component focusedComponent = all.get(beforeIndex);
					scrollAnimeViewToComponent((JPanel) focusedComponent);
					return focusedComponent;
				}
				return main;
			}

			public Component getComponentAfter(Container aContainer, Component aComponent) {
				Vector<Component> all = animeViewComponentList();
				int i = all.indexOf(aComponent);
				if (i != -1) {
					int afterIndex = (i + all.size() + 1) % all.size();
					focusedComponentIndex = afterIndex;
					Component focusedComponent = all.get(afterIndex);
					scrollAnimeViewToComponent((JPanel) focusedComponent);
					return focusedComponent;
				}
				return main;
			}
		});
	}

	/**
	 * Constructs the "ANIME" view on the 'main' JPanel. Only the animes in the
	 * given Vector are used, and are used in the order they appear in the Vector.
	 * 
	 * @param container
	 * @param animes
	 */
	private static void makeAnimeView(Vector<String> animes) {
		int mostRecentComponentIndex = -1;
		while (!animes.isEmpty()) {
			JPanel row = new JPanel(new GridLayout(1, 0, 20, 0));
			row.setBackground(SEQUELCONNECTCOLOR);
			gridConst.gridy = main.getComponentCount();
			gridBag.setConstraints(row, gridConst);
			main.add(row);
			String firstFound = animes.firstElement();
			for (String s : AnimeData.relations(firstFound)) {
				JPanel visual = AnimeData.visual(s);
				row.add(visual);
				mostRecentComponentIndex++;
				if (focusedComponentIndex == -1 && s.equals(firstFound))
					focusedComponentIndex = mostRecentComponentIndex;
				animes.remove(s);
			}
		}
		setAnimeViewFocusTraversalPolicy();
	}

	/**
	 * Constructs a list view, i.e. either "TOWATCH" or "TODOWNLOAD" on the 'main'
	 * JPanel. Only the animes in the given Vector are used, and are used in the
	 * order they appear in the Vector.
	 * 
	 * Calling this method when the current view is neither "TOWATCH" nor
	 * "TODOWNLOAD" produces undefined results.
	 * 
	 * @param container
	 * @param animes
	 */
	private static void makeListView(Vector<String> animes) {
		GridBagLayout toDoLayout = new GridBagLayout();
		GridBagConstraints toDoConst = new GridBagConstraints();
		toDoConst.gridheight = 1;
		toDoConst.gridwidth = 1;
		toDoConst.gridy = 0;
		toDoConst.insets = new Insets(0, 10, 0, 10);
		toDoConst.anchor = GridBagConstraints.CENTER;
		JPanel toDo = new JPanel(toDoLayout);
		toDo.setBackground(BGCOLOR);
		JLabel nameHeader = new JLabel();
		if (view == View.TOWATCH) {
			nameHeader.setText("To Watch");
		} else {
			nameHeader.setText("To Download");
		}
		JLabel fromHeader = new JLabel("From");
		JLabel toHeader = new JLabel("To");
		JLabel[] headerList = { nameHeader, fromHeader, toHeader };
		nameHeader.setPreferredSize(new Dimension(800, 60));
		fromHeader.setPreferredSize(new Dimension(100, 60));
		toHeader.setPreferredSize(new Dimension(100, 60));

		for (int i = 0; i < headerList.length; i++) {
			toDoConst.gridx = i;
			headerList[i].setBorder(BorderFactory.createEtchedBorder());
			headerList[i].setHorizontalAlignment(JLabel.CENTER);
			headerList[i].setVerticalAlignment(JLabel.CENTER);
			headerList[i].setFont(new Font("Book Antiqua", Font.BOLD, 25));
			toDoLayout.setConstraints(headerList[i], toDoConst);
			toDo.add(headerList[i]);
		}

		int count = 0;
		for (String toProcess : animes) {
			final int watched = AnimeData.watchedNum(toProcess);
			final int downloaded = AnimeData.downloadedNum(toProcess);
			final int released = AnimeData.releasedNum(toProcess);
			if ((view == View.TOWATCH && watched < downloaded) || (view == View.TODOWNLOAD && downloaded < released)) {
				count++;
				final JLabel name = new JLabel(toProcess);
				final JLabel from = new JLabel();
				JLabel to = new JLabel();

				int referenceVar = 0;
				String referenceStr = "";
				int higherReferenceVar = 0;
				ActionListener incrementListener = null;
				if (view == View.TOWATCH) {
					referenceVar = watched;
					referenceStr = "watched";
					higherReferenceVar = downloaded;
					incrementListener = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AnimeData.watchedNext(name.getText());
							refreshView();
						}
					};
				} else if (view == View.TODOWNLOAD) {
					referenceVar = downloaded;
					referenceStr = "downloaded";
					higherReferenceVar = released;
					incrementListener = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AnimeData.downloadedNext(name.getText());
							refreshView();
						}
					};
				}
				final ActionListener finalIncListener = incrementListener;
				final String finalRefStr = referenceStr;
				from.setText(Integer.toString(referenceVar + 1));
				if ((referenceVar + 1) < higherReferenceVar) {
					to.setText(Integer.toString(higherReferenceVar));
				}
				name.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						if (me.getButton() == MouseEvent.BUTTON3) {
							JPopupMenu popupMenu = new JPopupMenu();
							JMenuItem increment = new JMenuItem(name.getText() + " - " + from.getText() + " " + finalRefStr);
							popupMenu.add(increment);
							increment.addActionListener(finalIncListener);
							popupMenu.show(me.getComponent(), me.getX(), me.getY());
						}
					}
				});
				JLabel[] data = { name, from, to };

				toDoConst.insets = new Insets(10, 0, 0, 0);
				toDoConst.anchor = GridBagConstraints.SOUTH;
				for (int j = 0; j < data.length; j++) {
					toDoConst.gridx = j;
					toDoConst.gridy = count;
					data[j].setHorizontalAlignment(JLabel.CENTER);
					data[j].setFont(new Font("Book Antiqua", Font.BOLD, 25));
					toDoLayout.setConstraints(data[j], toDoConst);
					toDo.add(data[j]);
				}
			}
		}
		gridConst.gridy = 0;
		gridBag.setConstraints(toDo, gridConst);
		main.add(toDo);

		thisWindow.setFocusTraversalPolicy(defaultFocusTraversalPolicy);
	}

	/**
	 * Constructs a date view, i.e. either "DATESWATCHED" or "DATESWATCHING" on the
	 * 'main' JPanel. Only the animes in the given Vector are used, and are used in
	 * the order they appear in the Vector.
	 * 
	 * Calling this method when the current view is neither "DATESWATCHED" nor
	 * "DATESWATCHING" produces undefined results.
	 * 
	 * @param container
	 * @param animes
	 */
	private static void makeDateView(Vector<String> animes) {
		GridBagLayout datesLayout = new GridBagLayout();
		GridBagConstraints datesConst = new GridBagConstraints();
		datesConst.gridheight = 1;
		datesConst.gridwidth = 1;
		datesConst.gridy = 0;
		datesConst.insets = new Insets(0, 10, 0, 10);
		datesConst.anchor = GridBagConstraints.CENTER;
		JPanel dates = new JPanel(datesLayout);
		dates.setBackground(BGCOLOR);
		JLabel nameHeader = new JLabel("Name");
		JLabel fromHeader = new JLabel("From");
		JLabel toHeader = new JLabel("To");
		JLabel spanHeader = new JLabel("Span");
		JLabel[] headerList = null;
		if (view == View.DATESWATCHED) {
			headerList = new JLabel[4];
			headerList[2] = toHeader;
			headerList[3] = spanHeader;
		} else if (view == View.DATESWATCHING) {
			headerList = new JLabel[2];
		}
		headerList[0] = nameHeader;
		headerList[1] = fromHeader;

		nameHeader.setPreferredSize(new Dimension(800, 60));
		fromHeader.setPreferredSize(new Dimension(200, 60));
		toHeader.setPreferredSize(new Dimension(200, 60));
		spanHeader.setPreferredSize(new Dimension(100, 60));

		for (int i = 0; i < headerList.length; i++) {
			datesConst.gridx = i;
			headerList[i].setBorder(BorderFactory.createEtchedBorder());
			headerList[i].setHorizontalAlignment(JLabel.CENTER);
			headerList[i].setVerticalAlignment(JLabel.CENTER);
			headerList[i].setFont(new Font("Book Antiqua", Font.BOLD, 25));
			datesLayout.setConstraints(headerList[i], datesConst);
			dates.add(headerList[i]);
		}
		int count = 0;
		for (String toProcess : animes) {
			Vector<Period> dateList = AnimeData.watchedSpans(toProcess);
			Period lastEntry = null;
			try {
				lastEntry = dateList.lastElement();
			} catch (NoSuchElementException e) {
			} // no periods exist -> anime not yet watched
			boolean watched = false;
			for (Period p : dateList) {
				if (p.endDate() != null) {
					watched = true;
					break;
				}
			}
			if (lastEntry != null && ((view == View.DATESWATCHED && watched) || (view == View.DATESWATCHING && !watched))) {
				count++;
				JLabel name = new JLabel(toProcess);
				JLabel from = new JLabel();
				JLabel to = new JLabel();
				JLabel span = new JLabel();

				from.setText(lastEntry.startDate().toString());
				if (watched) {
					to.setText(lastEntry.endDate().toString());
					try {
						int d = lastEntry.days() + 1;
						int rest = d % 7;
						span.setText((d - rest) / 7 + "w " + rest + "d");
					} catch (UnknownDateException e) {
						// does not occur <- watched is true
					}
				}

				JLabel[] data = null;
				if (view == View.DATESWATCHED) {
					data = new JLabel[4];
					data[2] = to;
					data[3] = span;
				} else if (view == View.DATESWATCHING) {
					data = new JLabel[2];
				}
				data[0] = name;
				data[1] = from;

				datesConst.insets = new Insets(10, 0, 0, 0);
				datesConst.anchor = GridBagConstraints.SOUTH;
				for (int j = 0; j < data.length; j++) {
					datesConst.gridx = j;
					datesConst.gridy = count;
					data[j].setHorizontalAlignment(JLabel.CENTER);
					data[j].setFont(new Font("Book Antiqua", Font.BOLD, 25));
					datesLayout.setConstraints(data[j], datesConst);
					dates.add(data[j]);
				}
			}
		}
		gridConst.gridy = 0;
		gridBag.setConstraints(dates, gridConst);
		main.add(dates);

		thisWindow.setFocusTraversalPolicy(defaultFocusTraversalPolicy);
	}

	/**
	 * Constructs the "HDD" view on the 'main' JPanel. Only the animes in the given
	 * Vector are used, and are used in the order they appear in the Vector.
	 * 
	 * @param container
	 * @param animes
	 */
	private static void makeHDDView(Vector<String> animes) {
		GridBagLayout hddLayout = new GridBagLayout();
		GridBagConstraints hddConst = new GridBagConstraints();
		hddConst.gridheight = 1;
		hddConst.gridwidth = 2;
		hddConst.gridy = 0;
		hddConst.insets = new Insets(0, 10, 0, 10);
		hddConst.anchor = GridBagConstraints.CENTER;
		JPanel hdd = new JPanel(hddLayout);
		hdd.setBackground(BGCOLOR);
		hdd.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu hddMenu = new JPopupMenu();
					JMenuItem combineOption = new JMenuItem("Switch Sequel-combining");
					hddMenu.add(combineOption);
					combineOption.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							switchHddSequelCombine();
						}
					});
					hddMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		});

		// sort into hdds, anime with non-specified hdd (null) are discarded
		TreeMap<String, Vector<String>> hdds = new TreeMap<String, Vector<String>>();
		for (String toProcess : animes) {
			String currHdd = AnimeData.hdd(toProcess);
			if (currHdd != null) {
				if (!hdds.containsKey(currHdd)) {
					hdds.put(currHdd, new Vector<String>());
				}
				hdds.get(currHdd).add(toProcess);
			}
		}

		int i = 0;
		hddConst.fill = GridBagConstraints.HORIZONTAL;
		for (String hddName : hdds.keySet()) {
			JLabel name = new JLabel(hddName);
			name.setPreferredSize(new Dimension(300, 50));
			name.setBorder(BorderFactory.createEtchedBorder());
			name.setHorizontalAlignment(JLabel.CENTER);
			name.setVerticalAlignment(JLabel.CENTER);
			name.setFont(new Font("Book Antiqua", Font.BOLD, 15));
			hddConst.gridx = 2 * i;
			i++;
			hddLayout.setConstraints(name, hddConst);
			hdd.add(name);
		}
		hddConst.gridwidth = 1;
		hddConst.fill = GridBagConstraints.NONE;

		i = 0; // counts at which hdd animes are currently added
		for (String hddName : hdds.keySet()) {
			if (hddSequelCombine) {
				Vector<String> animeInHdd = hdds.get(hddName);
				Vector<String> combinedAnimeInHdd = new Vector<String>();
				hdds.put(hddName, combinedAnimeInHdd);
				while (!animeInHdd.isEmpty()) {
					int totalEpNum = 0; // total number of eps of all relations of current anime in current hdd
					String firstRelation = null; // name of uppermost relation of current anime in current hdd
					for (String relation : AnimeData.relations(animeInHdd.firstElement())) {
						// check for each relation if it is contained in current hdd
						if (animeInHdd.contains(relation)) {
							if (firstRelation == null) {
								firstRelation = relation;
							}
							totalEpNum += AnimeData.episodes(relation);
							animeInHdd.remove(relation);
						}
					}
					combinedAnimeInHdd.add(firstRelation + "<" + totalEpNum); // temp save totalEpNum at the string end
				}
			}

			int count = 0;
			for (String animeInHdd : hdds.get(hddName)) {
				String animeName;
				int epNum;
				if (hddSequelCombine) {
					int delimiterIndex = animeInHdd.lastIndexOf('<');
					animeName = animeInHdd.substring(0, delimiterIndex);
					epNum = Integer.parseInt(animeInHdd.substring(delimiterIndex + 1));
				} else {
					animeName = animeInHdd;
					epNum = AnimeData.episodes(animeName);
				}
				JLabel anime = new JLabel(animeName);
				JLabel eps = new JLabel(Integer.toString(epNum));
				anime.setFont(new Font("Book Antiqua", Font.BOLD, 15));
				eps.setFont(new Font("Book Antiqua", Font.BOLD, 15));
				anime.setHorizontalAlignment(JLabel.CENTER);
				eps.setHorizontalAlignment(JLabel.CENTER);
				hddConst.insets = new Insets(10, 0, 0, 0);
				hddConst.anchor = GridBagConstraints.SOUTH;
				hddConst.gridx = 2 * i;
				count++;
				hddConst.gridy = count;
				hddLayout.setConstraints(anime, hddConst);
				hddConst.insets = new Insets(10, 5, 0, 15);
				hddConst.gridx = 2 * i + 1;
				hddLayout.setConstraints(eps, hddConst);
				hdd.add(anime);
				hdd.add(eps);
			}
			i++;
		}
		gridConst.gridy = 0;
		gridBag.setConstraints(hdd, gridConst);
		main.add(hdd);

		thisWindow.setFocusTraversalPolicy(defaultFocusTraversalPolicy);
	}

	/**
	 * Constructs the "GROUP" view on the 'main' JPanel. Only the groups in the
	 * given Vector are used, and are used in the order they appear in the Vector.
	 * 
	 * @param container
	 * @param groups
	 */
	private static void makeGroupView(Vector<Group> groups) {
		GridBagLayout toDoLayout = new GridBagLayout();
		GridBagConstraints toDoConst = new GridBagConstraints();
		toDoConst.gridheight = 1;
		toDoConst.gridwidth = 1;
		toDoConst.gridy = 0;
		toDoConst.insets = new Insets(0, 10, 0, 10);
		toDoConst.anchor = GridBagConstraints.CENTER;
		JPanel toDo = new JPanel(toDoLayout);
		toDo.setBackground(BGCOLOR);
		JLabel nameHeader = new JLabel("Group");
		JLabel animeHeader = new JLabel("Total Subbed Anime");
		JLabel epHeader = new JLabel("Total Subbed Episodes");
		JLabel[] headerList = { nameHeader, animeHeader, epHeader };
		nameHeader.setPreferredSize(new Dimension(800, 60));
		animeHeader.setPreferredSize(new Dimension(300, 60));
		epHeader.setPreferredSize(new Dimension(300, 60));

		for (int i = 0; i < headerList.length; i++) {
			toDoConst.gridx = i;
			headerList[i].setBorder(BorderFactory.createEtchedBorder());
			headerList[i].setHorizontalAlignment(JLabel.CENTER);
			headerList[i].setVerticalAlignment(JLabel.CENTER);
			headerList[i].setFont(new Font("Book Antiqua", Font.BOLD, 25));
			toDoLayout.setConstraints(headerList[i], toDoConst);
			toDo.add(headerList[i]);
		}

		int count = 0;
		for (final Group toProcess : groups) {
			count++;
			JLabel nameL = new JLabel(toProcess.getDisplayName());

			nameL.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (me.getButton() == MouseEvent.BUTTON3) {
						JPopupMenu popupMenu = new JPopupMenu();
						JMenuItem edit = new JMenuItem("Details / Edit");
						JMenuItem subbedAnime = new JMenuItem("Show subbed animes");
						JMenuItem alliance = new JMenuItem("Show Alliances");
						popupMenu.add(edit);
						popupMenu.add(subbedAnime);
						popupMenu.add(alliance);

						edit.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								modifyGroup(toProcess);
								refreshView();
							}
						});
						subbedAnime.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								showSubbedAnimes(toProcess);
							}
						});
						alliance.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								showAlliances(toProcess);
							}
						});
						popupMenu.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			});

			int animeNum = 0;
			int epNum = 0;
			for (String anime : AnimeData.nameList()) { // TODO very inefficient? #s already calculated in sort method..
				HashMap<Group, RangeList> subbed = AnimeData.groups(anime);
				if (subbed.containsKey(toProcess)) {
					animeNum++;
					epNum += subbed.get(toProcess).size();
				}
			}
			if (toProcess.getConstituents().size() > 0) {
				nameL.setForeground(Color.BLUE);
			}
			JLabel[] data = { nameL, new JLabel(Integer.toString(animeNum)), new JLabel(Integer.toString(epNum)) };

			toDoConst.insets = new Insets(10, 0, 0, 0);
			toDoConst.anchor = GridBagConstraints.SOUTH;
			for (int j = 0; j < data.length; j++) {
				toDoConst.gridx = j;
				toDoConst.gridy = count;
				data[j].setHorizontalAlignment(JLabel.CENTER);
				data[j].setFont(new Font("Book Antiqua", Font.BOLD, 25));
				toDoLayout.setConstraints(data[j], toDoConst);
				toDo.add(data[j]);
			}
		}
		gridConst.gridy = 0;
		gridBag.setConstraints(toDo, gridConst);
		main.add(toDo);

		thisWindow.setFocusTraversalPolicy(defaultFocusTraversalPolicy);
	}

	/**
	 * This method should only be called by the makeGroupView method. Opens a
	 * GroupDialog to view and/or modify the group.
	 */
	private static void modifyGroup(Group g) {
		JDialog createDialog = new JDialog(thisWindow);
		createDialog.setTitle("Group Details");
		createDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		GroupDialog d = new GroupDialog(createDialog, g);
		createDialog.setContentPane(d);
		createDialog.setSize(450, 600);
		createDialog.setLocationRelativeTo(null);
		createDialog.setResizable(false);
		createDialog.setVisible(true);

		if (d.getStatus()) {
			Set<String> oldNames = g.getNames(); // names before change
			Set<String> newNames = d.getNames(); // names after change
			for (String name : newNames) {
				g.addName(name);
			} // <-- does nothing if a name is already in oldNames
			oldNames.removeAll(newNames);
			for (String name : oldNames) {
				g.removeName(name);
			}
			// g.getNames() is now equal to newNames

			g.setDisplayName(d.getDisplayName());

			for (Group constit : g.getConstituents()) {
				g.removeConstituent(constit);
			}
			for (Group constit : d.getConstituents()) {
				g.addConstituent(constit);
			}

			g.setComments(d.getComment());
		}
	}

	/**
	 * This method should only be called be the makeGroupView method. Opens a window
	 * showing all animes subbed by the given group.
	 * 
	 * @param g
	 */
	private static void showSubbedAnimes(Group g) {
		// TODO show subbed specials
		JTable subbedT = new JTable(0, 1);
		subbedT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableModel subbedM = (DefaultTableModel) subbedT.getModel();
		Vector<String> identifiers = new Vector<>();
		identifiers.add("Anime");
		Vector<Vector<String>> rows = new Vector<>();
		int maxIntervalNum = 0;
		for (String anime : AnimeData.nameList()) {
			HashMap<Group, RangeList> groups = AnimeData.groups(anime);
			if (groups.containsKey(g)) {
				Vector<String> row = new Vector<>();
				row.add(anime);
				for (Interval i : groups.get(g)) {
					int l = i.getLeft();
					int r = i.getRight();
					row.add(l + (r == l ? "" : (" - " + r)));
				}
				if (maxIntervalNum < row.size() - 1) {
					maxIntervalNum = row.size() - 1;
				}
				rows.add(row);
			}
		}
		for (int i = 0; i < maxIntervalNum; i++) {
			identifiers.add("");
		}
		subbedM.setDataVector(rows, identifiers);
		subbedT.getColumnModel().getColumn(0).setPreferredWidth(300);
		JScrollPane subbedSP = new JScrollPane();
		subbedSP.setViewportView(subbedT);

		JDialog infoDialog = new JDialog(thisWindow);
		infoDialog.setTitle("Anime subbed by: " + g.getDisplayName());
		infoDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		infoDialog.setContentPane(subbedSP);
		infoDialog.setSize(600, 600);
		infoDialog.setLocationRelativeTo(null);
		infoDialog.setResizable(true);
		infoDialog.setVisible(true);
	}

	/**
	 * This method should only be called be the makeGroupView method. Opens a window
	 * showing all alliances in which this group participates.
	 * 
	 * @param g
	 */
	private static void showAlliances(Group g) {
		JTable allianceT = new JTable(0, 1);
		allianceT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableModel allianceM = (DefaultTableModel) allianceT.getModel();
		Vector<String> identifiers = new Vector<>();
		identifiers.add("Alliance Name");
		identifiers.add("Members");
		Vector<Vector<String>> rows = new Vector<>();
		int maxConstitNum = 0;
		for (Group group : AnimeData.groups()) {
			List<Group> constits = group.getConstituents();
			if (constits.contains(g) || (group == g && constits.size() > 0)) {
				Vector<String> row = new Vector<>();
				row.add(group.getDisplayName());
				for (Group constit : constits) {
					row.add(constit.getDisplayName());
				}
				if (maxConstitNum < row.size() - 1) {
					maxConstitNum = row.size() - 1;
				}
				rows.add(row);
			}
		}
		for (int i = 1; i < maxConstitNum; i++) {
			// start at i = 1, because first column already has "Member" as header.
			identifiers.add("");
		}
		allianceM.setDataVector(rows, identifiers);
		allianceT.getColumnModel().getColumn(0).setPreferredWidth(300);
		JScrollPane allianceSP = new JScrollPane();
		allianceSP.setViewportView(allianceT);

		JDialog infoDialog = new JDialog(thisWindow);
		infoDialog.setTitle("Anime subbed by: " + g.getDisplayName());
		infoDialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		infoDialog.setContentPane(allianceSP);
		infoDialog.setSize(600, 600);
		infoDialog.setLocationRelativeTo(null);
		infoDialog.setResizable(true);
		infoDialog.setVisible(true);
	}

	public static void refreshView() {
		main.removeAll();
		if (view == View.ANIME) {
			makeAnimeView(searchAnimes());
		} else if (view == View.TOWATCH || view == View.TODOWNLOAD) {
			makeListView(searchAnimes());
		} else if (view == View.DATESWATCHED || view == View.DATESWATCHING) {
			makeDateView(searchAnimes());
		} else if (view == View.HDD) {
			makeHDDView(searchAnimes());
		} else if (view == View.GROUPS) {
			makeGroupView(searchGroups());
		}
		main.repaint();
		pScroll.validate();

		if (view == View.ANIME) {
			Vector<Component> animePanels = animeViewComponentList();
			if (!animePanels.isEmpty()) {
				Component focusedAnime = animePanels.get(focusedComponentIndex);
				focusedAnime.requestFocusInWindow();
				scrollAnimeViewToComponent((JPanel) focusedAnime);
			}
		}
	}

	// Help Methods View = HDD
	private static void switchHddSequelCombine() {
		hddSequelCombine = !hddSequelCombine;
		refreshView();
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_PLUS) {

		} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {

		}
	}

	public void keyReleased(KeyEvent e) {
	}

}