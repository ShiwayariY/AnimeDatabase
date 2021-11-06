package tools;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A Window-PopUp, containing a TextField with a Label in front of it, as well
 * as a Confirm and a Cancel Button. Confirm will send the String inside the
 * TextField to the readTextFieldString-Method of the Object that opened the
 * Window.
 * 
 * The opening Object must implement OpensTextFieldWindow.
 * 
 * @param OpensTextFieldWindow The Object that opens this Window.
 * @param windowTitle          The Title of the opened Window.
 * @param attributeToInput     The Text of the Label in front of the TextField.
 * @param refID                An Integer in order to call different Methods
 *                             with readTextFieldString. (use switch)
 * @param textFieldText        Text to write into the TextField.
 * 
 * @author Yonohana Shiwayari
 */
public class TextFieldWindow {

	public TextFieldWindow(final OpensTextFieldWindow openingObject, String windowTitle, String attributeToInput, final int refID,
			String textFieldText) {
		final JFrame f = new JFrame(windowTitle);
		try {
			f.setIconImage(ImageIO.read(getClass().getResource("/res/adb.jpg")));
		} catch (IOException e) {
		}
		final JTextField tAttr = new JTextField(10);
		final JLabel lAttr = new JLabel(attributeToInput + ":");
		final JButton confirm = new JButton("Confirm");
		final JButton cancel = new JButton("Cancel");

		lAttr.setBounds(5, 5, 80, 20);
		lAttr.setFont(new Font("Book Antiqua", Font.ROMAN_BASELINE, 18));
		tAttr.setBounds(65, 5, 142, 25);
		confirm.setBounds(5, 35, 95, 25);
		cancel.setBounds(110, 35, 95, 25);

		tAttr.setText(textFieldText);

		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(226, 103);
		f.setLocationRelativeTo(null);
		f.setLayout(null);
		f.add(lAttr);
		f.add(tAttr);
		f.add(confirm);
		f.add(cancel);
		final ActionListener confirmListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputAttr = tAttr.getText();
				openingObject.readTextFieldWindowString(inputAttr, refID);
				f.dispose();
			}
		};
		confirm.addActionListener(confirmListener);
		tAttr.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					confirmListener.actionPerformed(new ActionEvent(confirm, 0, confirm.getActionCommand()));
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					f.dispose();
				}
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

}
