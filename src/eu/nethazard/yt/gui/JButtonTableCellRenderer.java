package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author mdPlusPlus
 * @since 2014-09-07
 */
public class JButtonTableCellRenderer implements TableCellRenderer{

	private JButton button;

	public JButtonTableCellRenderer() { button = new JButton(); }

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columns) {
		String label;
		if(value == null){
			label = "";
		}
		else{
			label = value.toString();
		}
		button.setText(label);

		return button;
	}

}
