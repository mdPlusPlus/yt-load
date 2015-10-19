/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;


import eu.nethazard.yt.Config;
import eu.nethazard.yt.YTMediaList;
import eu.nethazard.yt.YTMediaUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;

public class YTDownloadTableScrollPane extends JScrollPane{

	private List<Object[]> mediaListEntries;
	private JTable table;
	private DefaultTableModel model;
	
	public YTDownloadTableScrollPane(){
		//super();
		mediaListEntries = new LinkedList<Object[]>(); //YTMediaList, JComboBox, JComboBox
		
		table = new JTable();

		String[] columnNames = {"#",
								"video",
								"audio",
								"title",
								"length",
								"YouTube ID",
								"URL",
								"status",
								"remove"};
		
		model = new DefaultTableModel(new Object[][]{}, columnNames){
			@Override
			public boolean isCellEditable(int row, int col){
				if(col == 1 || col == 2 || col == 8){
					// 1 -> "video"
					// 2 -> "audio"
					// 8 -> "remove"
					return true;
				}
				else{
					return false;
				}
			}
		};
		table.setModel(model);

		table.getTableHeader().setEnabled(false); //TODO implement actual sorting

		//set min and max column width
		table.getColumnModel().getColumn(0).setMaxWidth(20); //#
		table.getColumnModel().getColumn(0).setMinWidth(20); //#
		table.getColumnModel().getColumn(1).setMaxWidth(44); //video
		table.getColumnModel().getColumn(1).setMinWidth(44); //video
		table.getColumnModel().getColumn(2).setMaxWidth(44); //audio
		table.getColumnModel().getColumn(2).setMinWidth(44); //audio
		//getColumn(3) - title?
		table.getColumnModel().getColumn(4).setMaxWidth(60); //length
		table.getColumnModel().getColumn(4).setMinWidth(60); //length
		table.getColumnModel().getColumn(5).setMaxWidth(100); //YouTube ID
		table.getColumnModel().getColumn(5).setMinWidth(100); //YouTube ID
		table.getColumnModel().getColumn(6).setMaxWidth(280); //URL
		table.getColumnModel().getColumn(6).setMinWidth(280); //URL
		table.getColumnModel().getColumn(7).setMaxWidth(80); //status
		table.getColumnModel().getColumn(7).setMinWidth(80); //status
		table.getColumnModel().getColumn(8).setMaxWidth(78); //remove
		table.getColumnModel().getColumn(8).setMinWidth(78); //remove
		//table.getColumn("remove").setMaxWidth(78);
		//table.getColumn("remove").setMinWidth(78);


		table.getColumnModel().getColumn(1).setCellRenderer(new JComboBoxTableCellRenderer());
		table.getColumnModel().getColumn(1).setCellEditor(new JComboBoxTableCellEditor());

		table.getColumnModel().getColumn(2).setCellRenderer(new JComboBoxTableCellRenderer());
		table.getColumnModel().getColumn(2).setCellEditor(new JComboBoxTableCellEditor());
		
		table.getColumnModel().getColumn(8).setCellRenderer(new JButtonTableCellRenderer());
		table.getColumnModel().getColumn(8).setCellEditor(new JButtonTableCellEditor(mediaListEntries));


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
				List<Integer> videoList = yt.getAvailableVideoMp4Itags();
				videoList.add(-1);
				List<Integer> audioList = yt.getAvailableAudioMp4Itags();
				audioList.add(-1);
				Integer[] video = videoList.toArray(new Integer[videoList.size()]);
				Integer[] audio = audioList.toArray(new Integer[audioList.size()]);
				JComboBox<Integer> videoComboBox = new JComboBox<Integer>(video);
				JComboBox<Integer> audioComboBox = new JComboBox<Integer>(audio);
				String title = yt.getTitle();
				String length = YTMediaUtil.convertSeconds(yt.getLength());
				String id = yt.getID();
				String url = yt.getURL().toString();
				String status = "";
				String remove = "remove";

				Object[] rowEntries = new Object[]{number, videoComboBox, audioComboBox, title, length, id, url, status, remove};

				if(Config.VERBOSE) {
					System.out.println("add: " + number + " videoComboBox audioComboBox " + title + " " + length + " " + id + " " +  url + " " + status + " " + remove);
					yt.printAvailableItags();
				}
				
				//TODO is already in table?

				mediaListEntries.add(new Object[]{yt, videoComboBox, audioComboBox});
				model.addRow(rowEntries);

				result = true;
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

	public List<Object[]> getEntries(){
		return mediaListEntries; //YTMediaList, JComboBox, JComboBox
	}
}
