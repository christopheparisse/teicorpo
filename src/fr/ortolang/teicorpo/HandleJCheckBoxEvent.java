package fr.ortolang.teicorpo;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class HandleJCheckBoxEvent extends JFrame implements ItemListener {

	private static final long serialVersionUID = 1L;

	JCheckBox tei;
	JCheckBox chat;
	JCheckBox trs;

	JButton button;

	HashSet<String> formats = new HashSet<String>();

	public HandleJCheckBoxEvent() {

		// set flow layout for the frame
		this.getContentPane().setLayout(new FlowLayout());
		this.setTitle("Choix du ou des formats de conversion");
		this.setSize(400, 100);
	    this.setLocationRelativeTo(null);

		tei = new JCheckBox("TEI", true);
		chat = new JCheckBox("CHAT");
		trs = new JCheckBox("TRANSCRIBER");
		
		button= new JButton("OK");

		tei.addItemListener(this);
		chat.addItemListener(this);
		trs.addItemListener(this);

		//Ajout des CheckBox dans le frame
		add(tei);
		add(chat);
		add(trs);

		add(button);

		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	@Override
	//Prise en compte du changement d'état des CheckBox 
	public void itemStateChanged(ItemEvent e) {
		if (tei.isSelected()) {
			formats.add("tei");
		}
		if (chat.isSelected()) {
			formats.add("chat");
		}
		if (trs.isSelected()) {
			formats.add("trs");
		}
	}
}