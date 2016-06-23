package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author mdPlusPlus
 * @since 26.11.2015
 */
public class DisabledJButtonTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JButton button;


    public DisabledJButtonTableCellEditor(){
        button = new JButton();
        button.setEnabled(false);
    }

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

        fireEditingStopped();
        //if you do not remove the row/button you return button and not null!
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }

}
