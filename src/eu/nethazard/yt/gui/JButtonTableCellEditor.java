/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import eu.nethazard.yt.YTMediaList;

/*
 * revert "special code" to get general implementation
 */

public class JButtonTableCellEditor extends AbstractCellEditor implements TableCellEditor{

	private JButton button;
	
	//special code
	private List<YTMediaList> entries;
	//
	
	public JButtonTableCellEditor() {
		button = new JButton();
	}
	
	//special code
	public JButtonTableCellEditor(List<YTMediaList> entries){
		button = new JButton();
		this.entries = entries;
	}
	//
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		String label;
		if(value == null){
			label = "";
		}
		else{
			label = value.toString();
		}
		button.setText(label);
		
		//special code
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		dtm.removeRow(row);
		entries.remove(row);
		//correct indizes
		for(int i = row; i < table.getRowCount(); i++){
			//column 0 -> #
			table.setValueAt(new Integer(i + 1), i, 0);
		}
		//
		
		fireEditingStopped();
		//if you do not remove the row/button you return button and not null!
		return null;
	}
	
	@Override
	public Object getCellEditorValue() {
		return button.getText();
	}

}