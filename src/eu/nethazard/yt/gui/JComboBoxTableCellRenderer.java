package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author mdPlusPlus
 * @since 2014-09-07
 */
public class JComboBoxTableCellRenderer implements TableCellRenderer{

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columns) {
		if(value instanceof JComboBox) {
			return (JComboBox<Integer>) value;
		} else {
			return null;
		}
	}

}
