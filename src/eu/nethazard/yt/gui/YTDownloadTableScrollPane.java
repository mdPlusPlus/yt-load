/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import eu.nethazard.yt.Config;
import eu.nethazard.yt.YTMediaList;
import eu.nethazard.yt.YTMediaUtil;

/*
 * TODO:
 *  - remove button
 */
public class YTDownloadTableScrollPane extends JScrollPane{
	
	private List<YTMediaList> mediaListEntries;
	private JTable table;
	private DefaultTableModel model;
	
	public YTDownloadTableScrollPane(){
		//super();
		mediaListEntries = new LinkedList<YTMediaList>();
		
		table = new JTable(){
			//for Booleans being displayed as Checkboxes
			public Class<?> getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
		};
			
		String[] columnNames = {"#",
								"to mp3?",
								"title",
								"length",
								"YouTube ID",
								"URL",
								"status",
								"remove"};
		
		model = new DefaultTableModel(new Object[][]{}, columnNames){
			@Override
			public boolean isCellEditable(int row, int col){
				if(col == 1 || col == 7){
					// 1 -> "to mp3?"
					// 7 -> "remove"
					return true;
				}
				else{
					return false;
				}
			}
		};
		table.setModel(model);

		//TODO replace 'to mp3?' with video and audio selection and act accordingly
			
		//set min and max column width
		table.getColumnModel().getColumn(0).setMaxWidth(20); //#
		table.getColumnModel().getColumn(0).setMinWidth(20); //#
		table.getColumnModel().getColumn(1).setMaxWidth(55); //to mp3? //min is auto-set to checkbox width
		//getColumn(2) - title?
		table.getColumnModel().getColumn(3).setMaxWidth(60); //length
		table.getColumnModel().getColumn(3).setMinWidth(60); //length
		table.getColumnModel().getColumn(4).setMaxWidth(100); //YouTube ID
		table.getColumnModel().getColumn(4).setMinWidth(100); //YouTube ID
		table.getColumnModel().getColumn(5).setMaxWidth(280); //URL
		table.getColumnModel().getColumn(5).setMinWidth(280); //URL
		table.getColumnModel().getColumn(6).setMaxWidth(80); //status
		table.getColumnModel().getColumn(6).setMinWidth(80); //stauts
		table.getColumnModel().getColumn(7).setMaxWidth(78); //remove
		table.getColumnModel().getColumn(7).setMinWidth(78); //remove
		//table.getColumn("remove").setMaxWidth(78);
		//table.getColumn("remove").setMinWidth(78);		
		
		table.getColumnModel().getColumn(7).setCellRenderer(new JButtonTableCellRenderer());
		table.getColumnModel().getColumn(7).setCellEditor(new JButtonTableCellEditor(mediaListEntries));

		
		
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
		setViewportView(table);
	}
	
	
	public boolean addEntry(String str){
		//TODO implement as SwingWorker?
		boolean result = false;
		
		if(YTMediaUtil.isYouTubeURL(str)){
			YTMediaList yt = YTMediaList.fromString(str);
			if(yt != null){
				int number = table.getRowCount() + 1;
				boolean toMp3 = false;
				String title = yt.getTitle();
				String length = YTMediaUtil.convertSeconds(yt.getLength());
				String id = yt.getID();
				String url = yt.getURL().toString();
				String status = "";
				String remove = "remove";
				
				Object[] rowEntries = new Object[]{number, toMp3, title, length, id, url, status, remove};

				if(Config.VERBOSE) {
					System.out.println("add: " + number + " " + toMp3 + " " + title + " " + length + " " + id + " " +  url + " " + status + " " + remove);
				}

				yt.printAvailableItags();
				
				//TODO is already in table?
				
				mediaListEntries.add(yt);
				model.addRow(rowEntries);
			}
			
		}
		else{
			System.err.println("not a YouTube URL: " + str);
		}
		
		return result;
	}
	
	public JTable getJTable(){
		return table;
	}
	
	public List<YTMediaList> getEntries(){
		return mediaListEntries;
	}
}
