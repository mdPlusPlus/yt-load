package eu.nethazard.yt.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by mdPlusPlus on 26.11.2015 at 16:57).
 */
public class DisabledJButtonTableCellRenderer implements TableCellRenderer {

    private JButton button;

    public DisabledJButtonTableCellRenderer() {
        button = new JButton();
        button.setEnabled(false);
    }

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