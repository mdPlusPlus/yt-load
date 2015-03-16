/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import eu.nethazard.yt.Config;
import eu.nethazard.yt.YTMediaList;

public class GUI extends JFrame implements ClipboardOwner{
	
	private final int BUTTON_WIDTH = 100;
	
	private YTDownloadTableScrollPane table;
	
	private JTextField outputField;
	private JButton browseButton;

	public GUI(){
		super();
		
		//setting plaf
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initFrame();
		getContentPane().add(initTargetBar(), BorderLayout.NORTH);
		getContentPane().add(initDownloadBar(), BorderLayout.SOUTH);
		
		table = new YTDownloadTableScrollPane();
		getContentPane().add(table, BorderLayout.CENTER);
		
		setUpClipboard();
		
		setVisible(true);
	}
	
	private void initFrame(){				
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(new Rectangle((int) (screensize.getWidth() / 2), (int) (screensize.getHeight() / 2)));
		setLocation((int) (screensize.getWidth() / 4), (int) (screensize.getHeight() / 4));
		
		setTitle("YouTube Downloader " + Config.getGUIVersion());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
	}
	
	private JComponent initTargetBar(){
		JFrame selfReference = this;
		
		JPanel bottomBar = new JPanel();
		bottomBar.setLayout(new BorderLayout());
		
		JLabel outputLabel= new JLabel("save to:");
		outputField = new JTextField();
		browseButton = new JButton("browse");
		
		outputLabel.setPreferredSize(new Dimension(BUTTON_WIDTH, (int) outputLabel.getPreferredSize().getHeight()));
		
		outputField.setText(System.getProperty("user.dir"));
		outputField.setEditable(false);
		
		browseButton.setPreferredSize(new Dimension(BUTTON_WIDTH, (int) browseButton.getPreferredSize().getHeight()));
		browseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(outputField.getText()));
				fc.setDialogTitle("select output folder");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);
				if(fc.showOpenDialog(selfReference) == JFileChooser.APPROVE_OPTION){
					File selected = fc.getSelectedFile();
					outputField.setText(selected.getAbsolutePath());
				}
			}
		});
		
		bottomBar.add(outputLabel, BorderLayout.WEST);
		bottomBar.add(outputField, BorderLayout.CENTER);
		bottomBar.add(browseButton, BorderLayout.EAST);
		
		return bottomBar;
	}

	private JComponent initDownloadBar(){
		JPanel downloadBar = new JPanel();
		downloadBar.setLayout(new BorderLayout());
		
		JButton downloadButton = new JButton("download");
		downloadButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, StatusUpdate> worker = new SwingWorker<Void, StatusUpdate>(){
					@Override
					protected Void doInBackground() throws Exception {
						// TODO Auto-generated method stub
						try{
							List<YTMediaList> toDownload = table.getEntries();
							if(toDownload != null && !toDownload.isEmpty()){
								JTable tbl = table.getJTable();
								for(int i = 0; i < toDownload.size(); i++){
									//TODO
									publish(new StatusUpdate("downloading", i));
									YTMediaList current = toDownload.get(i);
									boolean toMp3 = ((Boolean) tbl.getValueAt(i, 1)).booleanValue(); //column 1 -> toMp3?
									if(toMp3){
										//download best audio and convert to mp3
										current.getBestAudio().downloadAndConvertToMp3(outputField.getText());
									}
									else{
										//download best audio + video and mux them
										current.downloadAndMuxBest(outputField.getText());
									}
									publish(new StatusUpdate("finished", i));
								}
							}
						}
						catch(IOException | InterruptedException ex){
							//TODO
							ex.printStackTrace();
						}
						
						return null;
					}
					
					@Override
					protected void process(List<StatusUpdate> statusUpdates) {
						for(StatusUpdate s : statusUpdates){
							JTable tbl = table.getJTable();
							tbl.setValueAt(s.getStatus(), s.getRow(), 6);
						}
					}
					
				};
				worker.execute();
			}
		});
		
		downloadBar.add(downloadButton, BorderLayout.CENTER);
		
		return downloadBar;
	}
	
	private void setUpClipboard(){
		Clipboard sysClip = this.getToolkit().getSystemClipboard();
		sysClip.setContents(sysClip.getContents(this), this);
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		System.out.println("lost clipboard!");
		
		try{
			Transferable current = clipboard.getContents(this);
			//current = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
			if(current != null){
				DataFlavor[] flavors = current.getTransferDataFlavors();
				if(flavors.length > 0){
					DataFlavor bestFlavor = DataFlavor.selectBestTextFlavor(flavors);
					if(bestFlavor != null){
						try {
							Reader r = bestFlavor.getReaderForText(current);
							BufferedReader br = new BufferedReader(r);
							String read;
							while((read = br.readLine()) != null){
								//TODO impelemnt as SwingWorker?
								table.addEntry(read);
							}
						}
						catch (UnsupportedFlavorException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						System.err.println("could not select best text flavor");
					}
					
					clipboard.setContents(current, this);
				}
				else{
					System.err.println("no data flavors available!?");
				}
			}
			else{
				System.err.println("no content on clipboard");
			}
		}
		catch(IllegalStateException e){
			if(e.getMessage().equals("cannot open system clipboard")){
				//e.printStackTrace();
				System.err.println(e.getMessage());
				System.err.println("retry");
				this.lostOwnership(clipboard, contents);
			}
			else{
				throw e;
			}
		}
			
	}
	
	private class StatusUpdate{
		private String status;
		private int row;
		
		public StatusUpdate(String status, int row){
			this.status = status;
			this.row = row;
		}
		
		public String getStatus(){
			return status;
		}
		
		public int getRow(){
			return row;
		}
		
	}
}

