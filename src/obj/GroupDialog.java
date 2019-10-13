package obj;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class GroupDialog extends JPanel implements DocumentListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel namesL = new JLabel("Names:");
	private JTextArea namesTA = new JTextArea(3, 30);
	private JScrollPane namesSP = new JScrollPane(namesTA);

	private JLabel displayL = new JLabel("Displayed name:");
	private JComboBox<String> displayCB = new JComboBox<String>();

	private JLabel commentsL = new JLabel("Comments:");
	private JTextArea commentsTA = new JTextArea(4, 30);
	private JScrollPane commentsSP = new JScrollPane(commentsTA);

	private JTable subT = new JTable(0, 1);
	private DefaultTableModel subM = (DefaultTableModel) subT.getModel();
	private JScrollPane subSP = new JScrollPane(subT);
	private JComboBox<Group> subCB = new JComboBox<Group>(AnimeData.groups());
	private JButton subAddB = new JButton("Add");
	private JButton subRemoveB = new JButton("Remove");

	private JButton confirmB = new JButton("Create Group");
	private JButton cancelB = new JButton("Cancel");

	private Font font = new Font("Book Antiqua", Font.ROMAN_BASELINE, 18);

	private Vector<Group> constits = new Vector<>();

	private JDialog owner;

	private boolean confirmed = false; // should only be true after the dialog is closed and confirmed, i.e. the group
										// should be created.

	public GroupDialog(JDialog owner) {
		this.owner = owner;

		namesL.setFont(font);
		displayL.setFont(font);
		commentsL.setFont(font);

		setLayout(null);
		namesTA.setToolTipText("Enter each group name in a new line.");
		namesTA.getDocument().addDocumentListener(this);

		subT.setToolTipText("A list of groups that form this group.");

		String[] colIDs = { "Sub-Groups" };
		subM.setColumnIdentifiers(colIDs);
		subAddB.setFont(font);
		subRemoveB.setFont(font);
		subAddB.addActionListener(this);
		subRemoveB.addActionListener(this);

		namesL.setBounds(10, 5, 100, 20);
		namesSP.setBounds(10, 35, 425, 100);
		displayL.setBounds(10, 145, 150, 20);
		displayCB.setBounds(150, 145, 280, 25);
		int suby = 30;
		subSP.setBounds(10, 180 + suby, 425, 100);
		subCB.setBounds(10, 290 + suby, 230, 25);
		subAddB.setBounds(250, 290 + suby, 70, 25);
		subRemoveB.setBounds(330, 290 + suby, 100, 25);
		int commy = suby + 30;
		commentsL.setBounds(10, 320 + commy, 100, 20);
		commentsSP.setBounds(10, 345 + commy, 425, 100);

		add(namesL);
		add(namesSP);
		add(displayL);
		add(displayCB);
		add(subSP);
		add(subCB);
		add(subAddB);
		add(subRemoveB);
		add(commentsL);
		add(commentsSP);

		confirmB.setFont(font);
		confirmB.setActionCommand("Create");
		confirmB.addActionListener(this);
		confirmB.setBounds(50, 540, 150, 25);
		add(confirmB);
		cancelB.setFont(font);
		cancelB.setActionCommand("Cancel");
		cancelB.addActionListener(this);
		cancelB.setBounds(250, 540, 150, 25);
		add(cancelB);
	}

	public GroupDialog(JDialog owner, Group g) {
		this(owner);

		String namesStr = "";
		boolean firstLoop = true;
		for (String name : g.getNames()) {
			namesStr += (firstLoop ? "" : "\n") + name;
			if (firstLoop) {
				firstLoop = false;
			}
		}
		namesTA.setText(namesStr);

		displayCB.setSelectedItem(g.getDisplayName());

		for (Group constit : g.getConstituents()) {
			addConstituent(constit);
		}

		commentsTA.setText(g.getComments());

		confirmB.setText("Save");
		cancelB.setText("Close");
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		namesChanged(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		namesChanged(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		namesChanged(e);
	}

	private void namesChanged(DocumentEvent e) {
		Document d = e.getDocument();
		try {
			String[] names = d.getText(0, d.getLength()).split("\n");

			displayCB.removeAllItems();
			for (int i = 0; i < names.length; i++) {
				if (names[i].length() > 0) {
					displayCB.addItem(names[i]);
				}
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	private void addConstituent(Group constit) {
		if (!constits.contains(constit) && constit != null) {
			constits.add(constit);
			while (subM.getRowCount() > 0) {
				subM.removeRow(0);
			}
			for (Group g : constits) {
				subM.addRow(new Group[] { g });
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == subAddB) {
			Group selected = (Group) subCB.getSelectedItem();
			addConstituent(selected);
		} else if (e.getSource() == subRemoveB) {
			int selectedRow = subT.getSelectedRow();
			if (selectedRow != -1) {
				constits.remove((Group) subT.getValueAt(selectedRow, 0));
				subM.removeRow(selectedRow);
			}
		} else if (e.getActionCommand().equals("Create")) {
			if (getNames().size() > 0) {
				confirmed = true;
				owner.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "The group must have at least one name!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals("Cancel")) {
			owner.dispose();
		}

	}

	// getter Methods
	/**
	 * @return true if the group should be created/modified, false otherwise.
	 */
	public boolean getStatus() {
		return confirmed;
	}

	/**
	 * @return a Vector of the constituents of the created group.
	 */
	public Vector<Group> getConstituents() {
		return constits;
	}

	/**
	 * @return a Vector of the names of the created group.
	 */
	public TreeSet<String> getNames() {
		TreeSet<String> names = new TreeSet<>();
		Document d = namesTA.getDocument();
		try {
			String[] namesArray = d.getText(0, d.getLength()).split("\n");
			for (int i = 0; i < namesArray.length; i++) {
				if (namesArray[i].length() > 0) {
					names.add(namesArray[i]);
				}
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		return names;
	}

	/**
	 * @return the display name of the created group.
	 */
	public String getDisplayName() {
		return (String) displayCB.getSelectedItem();
	}

	/**
	 * @return the comment for the created group.
	 */
	public String getComment() {
		return commentsTA.getText();
	}

}