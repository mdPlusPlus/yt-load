package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author mdPlusPlus
 * @since 2014-09-07
 */
public class JComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor{

	private JComboBox<Integer> comboBox;

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if(value instanceof JComboBox) {
			comboBox = (JComboBox<Integer>) value;
		}

		fireEditingStopped();

		return comboBox;
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox;
	}

}
