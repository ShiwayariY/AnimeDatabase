package obj;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import obj.AnimeData.AirStatus;


import tools.OpensTextFieldWindow;
import tools.TextFieldWindow;
import format.date.*;
import format.interval.*;

public class AnimeVisual implements ActionListener, KeyListener, MouseListener, OpensTextFieldWindow {
	
	//all other data is extracted from AnimeData
	private String name;
	
//	 ---------------------------------------- Constants ---------------------------------------- //
	
	private static final int COVERWIDTH = 170;
	private static String[] GHEADER = { "Group", "Start", "End" };

	// ---------------------------------------- Visualization ---------------------------------------- //

	private GridBagLayout gridBag = new GridBagLayout();
	private GridBagConstraints gridConst = new GridBagConstraints();
	private JPanel data = new JPanel(gridBag);
	
	private JLabel lName = new JLabel();
	private JLabel lEpisodes = new JLabel("Episodes");
	private JLabel lReleased = new JLabel("Released");
	private JLabel lDownloaded = new JLabel("Downloaded");
	private JLabel lWatched = new JLabel("Watched");
	private JLabel lAirstatus = new JLabel("Airstatus");
	private JLabel lStartDate = new JLabel("Started");
	private JLabel lFinDate = new JLabel("Finished");
	private JLabel lTimeSpan = new JLabel("Span");
	private JLabel lTimeSpanShow = new JLabel();
	private JLabel lHarddrive = new JLabel("Harddrive");
	JLabel[] labelList = {lName, lEpisodes, lReleased, lDownloaded, lWatched, lAirstatus,
			lStartDate, lFinDate, lTimeSpan, lTimeSpanShow, lHarddrive};

	private JTextField tEpisodes = new JTextField(5);
	private JTextField tReleased = new JTextField(5);
	private JTextField tDownloaded = new JTextField(5);
	private JTextField tWatched = new JTextField(5);
	private JTextField tAirstatus = new JTextField(5);
	private JComboBox cStartDate = new JComboBox();
	private JButton bFinDate = new JButton();
	
	
	private JTextField tHarddrive = new JTextField(3);
	JTextField[] textFieldList = { tEpisodes, tReleased, tDownloaded, tWatched,
	tAirstatus, tHarddrive };
	private JButton addDate = new JButton();
	private JButton removeDate = new JButton();
	
	private JButton save = new JButton();
	private JButton addGroup = new JButton();
	private JButton removeGroup = new JButton();
	private JButton moveUp = new JButton();
	private JButton moveDown = new JButton();
	private JButton plusSubbed = new JButton();
	private JButton minusSubbed = new JButton();
	
	private transient ImageIcon addIcon;
	private transient ImageIcon removeIcon;
	private transient ImageIcon plusIcon;
	private transient ImageIcon minusIcon;
	private transient ImageIcon upIcon;
	private transient ImageIcon downIcon;
	private transient ImageIcon saveIcon;
	private transient ImageIcon editIcon;

	private transient Image cover;
	@SuppressWarnings("serial")
	public JPanel pImage = new JPanel() {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {
				int w = cover.getWidth(this);
				int h = cover.getHeight(this);
				g.drawImage(cover, 0, 0, COVERWIDTH, (COVERWIDTH * h) / w, this);
			} catch(NullPointerException e) {}
		}
	};

	private DefaultTableModel tableModel = new DefaultTableModel(GHEADER, 0);
	@SuppressWarnings("serial")
	private JTable groups = new JTable(tableModel) {
		public boolean isCellEditable(int x, int y) {
			return false;
		}
	};

	private JScrollPane pGroups = new JScrollPane();

	private JPopupMenu animeMenu;

	public AnimeVisual(String name) {
		this.name = name;
		
		int eps = AnimeData.episodes(name);
		if(eps == 0) {
			tEpisodes.setText("Unknown");
		} else {
			tEpisodes.setText(Integer.toString(eps));
		}
		
		tReleased.setText(Integer.toString(AnimeData.releasedNum(name)));
		
		tDownloaded.setText(Integer.toString(AnimeData.downloadedNum(name)));
		
		tWatched.setText(Integer.toString(AnimeData.watchedNum(name)));
		
		AirStatus as = AnimeData.airStatus(name);
		String aStr = "";
		switch(as) {
		case FINISHED:
			aStr = "Finished";
			break;
		case AIRING:
			aStr = "Airing";
			break;
		case SOON:
			aStr = "Soon";
			break;
		}
		tAirstatus.setText(aStr);
		
		showDates();
		
		tHarddrive.setText(AnimeData.hdd(name));
		
		showGroups();
		
		createMenus();
	}
	
	// ---------------------------------------- Listeners ---------------------------------------- //

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Save")) {
			save.setActionCommand("Edit");
			save.setIcon(editIcon);
			save();
		}
		if(e.getActionCommand().equals("Edit")) {
			save.setActionCommand("Save");
			save.setIcon(saveIcon);
			edit();
		}
		if(e.getActionCommand().equals("addGroup")) {
			addGroup();
		}
		if(e.getActionCommand().equals("removeGroup")) {
			removeGroup(groups.getSelectedRow());
		}
		if(e.getActionCommand().equals("up")) {
			moveGroup(groups.getSelectedRow(), groups.getSelectedRow() - 1);
		}
		if(e.getActionCommand().equals("down")) {
			moveGroup(groups.getSelectedRow(), groups.getSelectedRow() + 1);
		}
		if(e.getActionCommand().equals("plus")){
			changeSubbed(groups.getSelectedColumn(), groups.getSelectedRow(), 1);
		}
		if(e.getActionCommand().equals("minus")){
			changeSubbed(groups.getSelectedColumn(), groups.getSelectedRow(), -1);
		}				
		if(e.getActionCommand().equals("editName")) {
			new TextFieldWindow(this, "Enter Name", "Name", 0, name);
		}	
		if(e.getActionCommand().equals("loadCover")) {
			loadCover(true);
		}
		if(e.getActionCommand().equals("setSequel")) {
			chooseSequel();
		}
		if(e.getActionCommand().equals("resetSequel")) {
			resetSequel();
		}
		if(e.getActionCommand().equals("selectDate")) {
			selectDate();
		}
		if(e.getActionCommand().equals("addDate")) {
			new TextFieldWindow(this, "Enter Start Date", "Date", 1, "");
		}
		if(e.getActionCommand().equals("removeDate")) {
			removeDate();
		}
		if(e.getActionCommand().equals("setFinDate")) {
			if(cStartDate.getSelectedIndex() != -1) {
				new TextFieldWindow(this, "Enter Finish Date", "Date", 2, "");
			}
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && save.getActionCommand().equals("Save")) {
			save.setActionCommand("Edit");
			save.setIcon(editIcon);
			save();
		}
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3) {
			animeMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	public void mouseEntered(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}

	// ---------------------------------------- Menu ---------------------------------------- //
	
	private void createMenus() {
		animeMenu = new JPopupMenu();

		for (int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].addKeyListener(this);
		}
		save.addActionListener(this);
		save.setActionCommand("Edit");
		addGroup.addActionListener(this);
		addGroup.setActionCommand("addGroup");
		removeGroup.addActionListener(this);
		removeGroup.setActionCommand("removeGroup");
		moveUp.addActionListener(this);
		moveUp.setActionCommand("up");
		moveDown.addActionListener(this);
		moveDown.setActionCommand("down");
		plusSubbed.addActionListener(this);
		plusSubbed.setActionCommand("plus");
		minusSubbed.addActionListener(this);
		minusSubbed.setActionCommand("minus");
		cStartDate.addActionListener(this);
		cStartDate.setActionCommand("selectDate");
		addDate.addActionListener(this);
		addDate.setActionCommand("addDate");
		removeDate.addActionListener(this);
		removeDate.setActionCommand("removeDate");
		bFinDate.addActionListener(this);
		bFinDate.setActionCommand("setFinDate");

		// Panel Context Menu:
		data.addMouseListener(this);
			
		JMenuItem editName = new JMenuItem("Edit Name");
		JMenuItem loadCover = new JMenuItem("Load Cover");
		JMenuItem setSequel = new JMenuItem("Set Sequel");
		JMenuItem resetSequel = new JMenuItem("Reset Sequel");
		
		editName.setActionCommand("editName");
		loadCover.setActionCommand("loadCover");
		setSequel.setActionCommand("setSequel");
		resetSequel.setActionCommand("resetSequel");
		
		editName.addActionListener(this);
		loadCover.addActionListener(this);
		setSequel.addActionListener(this);
		resetSequel.addActionListener(this);
		
		animeMenu.add(loadCover);
		animeMenu.addSeparator();
		animeMenu.add(setSequel);
		animeMenu.add(resetSequel);
		animeMenu.addSeparator();
		animeMenu.add(editName);
	}
	
	// ---------------------------------------- Create Visualization ---------------------------------------- //

	private void visualize() {
		format();
		
		gridConst.weightx = 100;
		gridConst.weighty = 100;
		gridConst.insets = new Insets(0, 10, 0, 10);
		makeVis(lName, 0, 0, 6, 2);
		gridConst.anchor = GridBagConstraints.WEST;
		makeVis(lEpisodes, 3, 3, 2, 1);
		makeVis(lReleased, 3, 4, 2, 1);
		makeVis(lDownloaded, 3, 6, 2, 1);
		makeVis(lWatched, 3, 7, 2, 1);
		makeVis(lAirstatus, 3, 9, 2, 1);
		
		gridConst.insets = new Insets(0, 25, 0, 0);
		makeVis(lStartDate, 0, 12, 3, 1);
		gridConst.insets = new Insets(0, 0, 0, 10);
		gridConst.anchor = GridBagConstraints.EAST;
		makeVis(lFinDate, 0, 12, 3, 1);
		
		gridConst.insets = new Insets(0, 0, 0, 0);
		gridConst.anchor = GridBagConstraints.CENTER;
		makeVis(lTimeSpan, 5, 12, 1, 1);
		makeVis(lTimeSpanShow, 5, 13, 1, 1);
		
		gridConst.insets = new Insets(0, 0, 0, 5);
		gridConst.anchor = GridBagConstraints.EAST;
		makeVis(tEpisodes, 5, 3, 1, 1);
		makeVis(tReleased, 5, 4, 1, 1);
		makeVis(tDownloaded, 5, 6, 1, 1);
		makeVis(tWatched, 5, 7, 1, 1);
		makeVis(tAirstatus, 5, 9, 1, 1);
		
		gridConst.insets = new Insets(0, 15, 0, 0);
		gridConst.anchor = GridBagConstraints.WEST;
		cStartDate.setPreferredSize(new Dimension(90, 20));
		for(Component c : cStartDate.getComponents()) {
			if(c instanceof JButton) { c.setVisible(false);	} //removes arrow button
		}		
		cStartDate.setBackground(MainWindow.BGCOLOR);
		makeVis(cStartDate, 0, 13, 3, 1);
		gridConst.anchor = GridBagConstraints.EAST;
		gridConst.insets = new Insets(0, 0, 5, 0);
		makeVis(addDate, 3, 13, 1, 1);
		gridConst.anchor = GridBagConstraints.WEST;
		gridConst.insets = new Insets(0, 3, 5, 0);
		makeVis(removeDate, 4, 13, 1, 1);
		
		gridConst.insets = new Insets(0, 0, 0, 0);
		gridConst.anchor = GridBagConstraints.EAST;
		bFinDate.setBackground(MainWindow.BGCOLOR);
		bFinDate.setMargin(new Insets(0, 0, 0, 0));
		bFinDate.setPreferredSize(new Dimension(70, 20));
		makeVis(bFinDate, 0, 13, 3, 1);
		
		gridConst.insets = new Insets(0, 20, 0, 0);
		gridConst.anchor = GridBagConstraints.CENTER;
		makeVis(lHarddrive, 4, 15, 2, 1);		
		makeVis(tHarddrive, 4, 16, 2, 1);

		gridConst.insets = new Insets(0,0,5,5);
		gridConst.anchor = GridBagConstraints.EAST;
		makeVis(save, 4, 19, 2, 1);

		gridConst.insets = new Insets(0, 0, 0, 0);
		gridConst.anchor = GridBagConstraints.NORTHWEST;
		makeVis(pImage, 0, 2, 3, 9);
		
		gridConst.anchor = GridBagConstraints.CENTER;
		gridConst.insets = new Insets(0, 3, 5, 0);
		makeVis(pGroups, 0, 15, 4, 4);
		makeVis(addGroup, 0, 19, 1, 1);
		makeVis(removeGroup, 1, 19, 1, 1);

		gridConst.anchor = GridBagConstraints.EAST;
		gridConst.insets = new Insets(0, 0, 5, 0);
		makeVis(plusSubbed, 2, 19, 1, 1);
		gridConst.anchor = GridBagConstraints.WEST;
		gridConst.insets = new Insets(0, 3, 5, 0);
		makeVis(minusSubbed, 3, 19, 1, 1);
		
		gridConst.insets = new Insets(20, 0, 0, 0);
		gridConst.anchor = GridBagConstraints.SOUTHWEST;
		makeVis(moveUp, 4, 15, 1, 2);
		gridConst.insets = new Insets(4, 0, 0, 0);
		gridConst.anchor = GridBagConstraints.NORTHWEST;
		makeVis(moveDown, 4, 17, 1, 2);
		
		for (int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].setEditable(false);
			textFieldList[i].setBackground(MainWindow.BGCOLOR);
		}
	}

	private void makeVis(Component comp, int x, int y, int width, int height) {
		gridConst.gridx = x;
		gridConst.gridy = y;
		gridConst.gridheight = height;
		gridConst.gridwidth = width;
		gridBag.setConstraints(comp, gridConst);
		data.add(comp);
	}

	private void format() {
		showName(name);
		
		int[] height = new int[20];
		for (int i = 0; i < 20; i++) {
			height[i] = 20;
		}
		gridBag.rowHeights = height;
		
		data.setBackground(MainWindow.BGCOLOR);
		data.setBorder(BorderFactory.createEtchedBorder());
		for (int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].setHorizontalAlignment(JTextField.CENTER);
		}		
		pImage.setBackground(MainWindow.BGCOLOR);
		pImage.setMinimumSize(new Dimension(COVERWIDTH, 50));
		pImage.setPreferredSize(new Dimension(COVERWIDTH, 50));
		pImage.setMaximumSize(new Dimension(COVERWIDTH, 50));
		
		TableColumnModel cm = groups.getColumnModel();
		for (int i = 1; i < 3; i++) {
			cm.getColumn(i).setMinWidth(35);
			cm.getColumn(i).setMaxWidth(35);
			cm.getColumn(i).setPreferredWidth(35);
		}
		groups.getTableHeader().setReorderingAllowed(false);
		groups.setCellSelectionEnabled(true);
		pGroups.setViewportView(groups);
		pGroups.setPreferredSize(new Dimension(220, 80));

		addDate.setPreferredSize(new Dimension(25, 25));
		removeDate.setPreferredSize(new Dimension(25, 25));
		
		addGroup.setPreferredSize(new Dimension(65, 25));
		removeGroup.setPreferredSize(new Dimension(65, 25));
		moveUp.setPreferredSize(new Dimension(15, 28));
		moveDown.setPreferredSize(new Dimension(15, 28));
		plusSubbed.setPreferredSize(new Dimension(25, 25));
		minusSubbed.setPreferredSize(new Dimension(25, 25));
		
		save.setPreferredSize(new Dimension(70, 25));
		
		initialize();
	}
	
	private void showName(String name){
		lName.setText(name);
		int fontSize = 20;
		lName.setFont(new Font("Book Antiqua", Font.BOLD, fontSize));
		while(lName.getPreferredSize().getWidth() > 300) {
			fontSize--;
			lName.setFont(new Font("Book Antiqua", Font.BOLD, fontSize));
		}
	}
	
	private void initialize() {
		loadCover(false);
		deleteTextFieldBorders();
		setIcons();		
	}
	
	private void deleteTextFieldBorders(){
		for (int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].setBorder(null);
		}
	}
	
	private void setIcons(){
		addIcon = new ImageIcon(getClass().getResource("res/addIcon.jpg"));
		removeIcon = new ImageIcon(getClass().getResource("res/removeIcon.jpg"));
		plusIcon = new ImageIcon(getClass().getResource("res/plusIcon.jpg"));
		minusIcon = new ImageIcon(getClass().getResource("res/minusIcon.jpg"));
		upIcon = new ImageIcon(getClass().getResource("res/upIcon.jpg"));
		downIcon = new ImageIcon(getClass().getResource("res/downIcon.jpg"));
		saveIcon = new ImageIcon(getClass().getResource("res/saveIcon.jpg"));
		editIcon = new ImageIcon(getClass().getResource("res/editIcon.jpg"));
		
		addDate.setIcon(plusIcon);
		removeDate.setIcon(minusIcon);
		
		addGroup.setIcon(addIcon);
		removeGroup.setIcon(removeIcon);
		plusSubbed.setIcon(plusIcon);
		minusSubbed.setIcon(minusIcon);
		moveUp.setIcon(upIcon);
		moveDown.setIcon(downIcon);
		if(save.getActionCommand().equals("Save")){
			save.setIcon(saveIcon);			
		}
		if(save.getActionCommand().equals("Edit")){
			save.setIcon(editIcon);	
		}	
	}

	public JPanel getVisual() {
		visualize();
		return data;
	}

	public void readTextFieldWindowString(String toRead, int refID){
		switch(refID){
		case 0:
			//edit name
			if(AnimeData.newName(name, toRead)) {
				name = toRead;
				showName(name);
			}
			break;
		case 1:
			//add period
			try {
				Date stDate = new Date(toRead);
				if(AnimeData.addPeriod(name, stDate)) {
					showDates();
					cStartDate.setSelectedItem(stDate); //listener is called
				}
			} catch(NumberFormatException e) {}
			break;
		case 2:
			//set fin date
			try {
				Date finDate = new Date(toRead);
				Date stDate = (Date) cStartDate.getSelectedItem();
				if(AnimeData.setFinDate(name, stDate, finDate)) {
					showDates();
					cStartDate.setSelectedItem(stDate);
				}
			} catch(NumberFormatException e) {}
			break;
		}
	}
	
	private void loadCover(boolean refreshView) {
		try {
			String absPath = new File("").getAbsolutePath();
			cover = getScaledInstance(ImageIO.read(new File(absPath + "\\covers\\" + name + ".jpg")) , COVERWIDTH);
			int w = cover.getWidth(pImage);
			int h = cover.getHeight(pImage);
			pImage.setMinimumSize(new Dimension(COVERWIDTH, (COVERWIDTH * h) / w));
			pImage.setPreferredSize(new Dimension(COVERWIDTH, (COVERWIDTH * h) / w));
			pImage.setMaximumSize(new Dimension(COVERWIDTH, (COVERWIDTH * h) / w));
			if(refreshView) {
				MainWindow.refreshView();
			} else {
				pImage.repaint();
			}			
		} catch(IOException e) {
			cover = null;
		}
	}
	
	private static Image getScaledInstance(Image image, int width){
		double w = image.getWidth(null);
		double scaleFactor = width / w;;
		int newWidth = (int) (image.getWidth(null) * scaleFactor);
		int newHeight = (int) (image.getHeight(null) * scaleFactor);
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphic = scaledImage.createGraphics();
		imageGraphic.drawImage(image, 0, 0, newWidth, newHeight, null);
		return scaledImage;
	}
	
	private void save() {
		int ep = 0;
		try {
			ep = Integer.parseInt(tEpisodes.getText());
			if(ep < 1) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {	tEpisodes.setText("Unknown"); }
		AnimeData.setEpisodes(name, ep);
		
		int rl = 0;
		try {
			rl = Integer.parseInt(tReleased.getText());
			if(rl < 0) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {	tReleased.setText("0");	}
		AnimeData.setReleased(name, rl); //TODO fix inconsistent function ~ RangeList
		
		int dl = 0;
		try {
			dl = Integer.parseInt(tDownloaded.getText());
			if(dl < 0) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {	tDownloaded.setText("0"); }
		AnimeData.setDownloaded(name, dl); //TODO fix inconsistent function ~ RangeList
		
		int wt = 0;
		try {
			wt = Integer.parseInt(tWatched.getText());
			if(wt < 0) { throw new NumberFormatException(); }
		} catch (NumberFormatException e) {	tWatched.setText("0"); }
		AnimeData.setWatched(name, wt); //TODO fix inconsistent function ~ RangeList

		String as = tAirstatus.getText();
		if((rl == ep && ep > 0) || as.equals("0") || as.equals("Finished")) {
			AnimeData.setAirStatus(name, AirStatus.FINISHED);
			tAirstatus.setText("Finished");
		} else if (as.equals("1") || as.equals("Airing")) {
			AnimeData.setAirStatus(name, AirStatus.AIRING);
			tAirstatus.setText("Airing");
		} else {
			AnimeData.setAirStatus(name, AirStatus.SOON);
			tAirstatus.setText("Soon");
		}

		String hdd = tHarddrive.getText();
		if(hdd.equals("")) {
			hdd = null;
		}
		AnimeData.setHdd(name, hdd);
		
		//Set non-Editable-----------------------------------------------
		for (int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].setEditable(false);
			textFieldList[i].setBackground(MainWindow.BGCOLOR);
		}
		
		data.validate();
	}

	private void edit() {
		for(int i = 0; i < textFieldList.length; i++) {
			textFieldList[i].setEditable(true);
			textFieldList[i].setBackground(new Color(240, 240, 240));
		}
	}
	
	private void showDates() {
		Vector<Period> periods = AnimeData.watchedSpans(name);
		Vector<Date> stDates = new Vector<Date>();
		for(Period p : periods) {
			stDates.add(p.startDate());
		}
		cStartDate.setModel(new DefaultComboBoxModel(stDates));
		cStartDate.setSelectedIndex(cStartDate.getItemCount() - 1); //-1 means no selection (no exception)
		
		selectDate();
		
		showTimeSpan();
	}
	
	private void selectDate() {
		int selected = cStartDate.getSelectedIndex();
		try {
			Date finDate = AnimeData.watchedSpans(name).get(selected).endDate();
			bFinDate.setText(finDate.toString());
		} catch(ArrayIndexOutOfBoundsException e) {
			//selected = -1;
			bFinDate.setText("");
		} catch(NullPointerException e) {
			//endDate is null
			bFinDate.setText("");
		}
		showTimeSpan();
	}
	
	private void showTimeSpan() {
		int days = 0;
		try {
			Period toShow = AnimeData.watchedSpans(name).get(cStartDate.getSelectedIndex());
			days = toShow.startDate().elapsedDaysUntil(toShow.endDate()) + 1;
			int d = days % 7;
			int w = (days - d) / 7;
			lTimeSpanShow.setText(w + "w " + d + "d");
		} catch(ArrayIndexOutOfBoundsException e) {
			lTimeSpanShow.setText(""); //if no dates exist
		} catch(UnknownDateException e) {
			lTimeSpanShow.setText("-"); //if any date is unknown
		} catch(NullPointerException e) {
			lTimeSpanShow.setText(""); //if startdate exists, but not findate
		}
	}
	
	private void removeDate() {
		Date toRemove = (Date) cStartDate.getSelectedItem();
		AnimeData.removePeriod(name, toRemove);
		showDates();
	}
	
	private void chooseSequel() {
		final JFrame f = new JFrame("Choose Sequel");
		final JButton confirm = new JButton("Confirm");
		final JButton cancel = new JButton("Cancel");
		
		Vector<String> chooseList = new Vector<String>(AnimeData.nameList());
		for(String rel : AnimeData.relations(name)) {
			chooseList.remove(rel);
		}
		Vector<String> temp = new Vector<String>();
		for(String s : chooseList) {
			if(!AnimeData.hasPrequel(s)) {
				temp.add(s);
			}
		}
		chooseList = temp;
		final JComboBox choosePrequel = new JComboBox(chooseList);
		choosePrequel.setBounds(5, 5, 200, 25);
		confirm.setBounds(20, 35, 80, 30);
		cancel.setBounds(110, 35, 80, 30);
		
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(217, 98);
		f.setLocationRelativeTo(null);
		f.setBackground(MainWindow.BGCOLOR);
		f.setLayout(null);
		f.add(choosePrequel);
		f.add(confirm);
		f.add(cancel);
		confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnimeData.setSequel(name, (String) choosePrequel.getSelectedItem());
				f.dispose();
				MainWindow.refreshView();
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

	private void resetSequel() {
		AnimeData.setSequel(name, null);
		MainWindow.refreshView();
	}
	
	private void showGroups() {
		HashMap<Group, RangeList> groups = AnimeData.groups(name);
		while(tableModel.getRowCount() > 0) {
			tableModel.removeRow(0);
		}
		for(Group g : groups.keySet()) {
			for(Interval i : groups.get(g)) {
				Object[] row = {g, i.getLeft(), i.getRight()}; //= {Group, int, int}; displays toString() values.
				tableModel.addRow(row);
			}
		}
	}

	private void addGroup() {
		final JFrame f = new JFrame("Add Group");
		final JComboBox<Group> group = new JComboBox<Group>(AnimeData.groups());
		final JTextField start = new JTextField();
		final JTextField end = new JTextField();
		final JButton confirm = new JButton("Confirm");
		final JButton cancel = new JButton("Cancel");

		group.setBounds(5, 5, 200, 25);
		start.setBounds(215, 5, 30, 25);
		end.setBounds(255, 5, 30, 25);
		confirm.setBounds(50, 40, 80, 25);
		cancel.setBounds(160, 40, 80, 25);

		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(300, 95);
		f.setLocationRelativeTo(null);
		f.setLayout(null);
		f.add(group);
		f.add(start);
		f.add(end);
		f.add(confirm);
		f.add(cancel);
		final ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int inStart = Integer.parseInt(start.getText());
					int inEnd = Integer.parseInt(end.getText());
					if(inStart <= inEnd && inStart >= 0) {
						AnimeData.addGroup(name, (Group) group.getSelectedItem(), inStart, inEnd);
						showGroups();
					}
				} catch (NumberFormatException ex) {
					//invalid input -> do nothing, close window
				}
				f.dispose();		
			}
		};
		confirm.addActionListener(al);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		final KeyListener kl = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					al.actionPerformed(new ActionEvent(confirm, 0, confirm.getActionCommand()));
				}
			}
		};
		start.addKeyListener(kl);
		end.addKeyListener(kl);
		f.validate();
		f.setVisible(true);
		f.setResizable(false);
	}

	private void removeGroup(int i) {
		try {
			if(i != -1) {
				Group group = (Group) tableModel.getValueAt(i, 0);
				int start = (int) tableModel.getValueAt(i, 1);
				int end = (int) tableModel.getValueAt(i, 2);
				AnimeData.removeGroup(name, group, start, end);
				showGroups();
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			//group does not exist -> do nothing
			//NumberFormatException should not occur because only integers are allowed for start, end
		}
	}

	private void moveGroup(int from, int to) {
		//TODO add moving function in animeData
		if(to >= 0 && from >= 0 && to < tableModel.getRowCount() && from < tableModel.getRowCount()) {
			tableModel.moveRow(from, from, to);
			groups.changeSelection(to, 0, false, false);
		} else {
			groups.clearSelection();
		}
	}
	
	/**
	 * Adds value to the start(if x = 1) or end(if x = 2) of the interval at position y.  
	 * @param x
	 * @param y
	 * @param value
	 */
	private void changeSubbed(int x, int y, int value) {
		if((x == 1 || x == 2) && y != -1) {
			int oldStart = (int) tableModel.getValueAt(y, 1);
			int newStart = oldStart + (x == 1 ? value : 0);
			int oldEnd = (int) tableModel.getValueAt(y, 2);
			int newEnd = oldEnd + (x == 2 ? value : 0);
			
			if(newStart > 0 && newStart <= newEnd) { //ensures nothing happens if invalid values result.
				int rl = AnimeData.releasedNum(name); //TODO fix inconsistent functionality ~ rangelist
				if(x == 2 && oldEnd == rl) {
					tReleased.setText(Integer.toString(newEnd));
					AnimeData.setReleased(name, newEnd);
				}
				
				Group group = (Group) tableModel.getValueAt(y, 0);
				AnimeData.removeGroup(name, group, oldStart, oldEnd);
				AnimeData.addGroup(name, group, newStart, newEnd);
				groups.changeSelection(x, y, false, false);
				showGroups();
			}
		}
	}

}

