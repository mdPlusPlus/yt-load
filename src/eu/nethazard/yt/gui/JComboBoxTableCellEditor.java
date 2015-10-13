/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class JComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor{

	private JComboBox<Integer> comboBox;

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		comboBox = (JComboBox<Integer>) value;

		fireEditingStopped();

		return (JComboBox<Integer>) value;
	}
	
	@Override
	public Object getCellEditorValue() {
		return comboBox;
	}

}