/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 */
package eu.nethazard.yt.gui;

import eu.nethazard.yt.Config;
import eu.nethazard.yt.YTMediaList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GUI extends JFrame implements ClipboardOwner{

	private final int BUTTON_WIDTH = 100;

	private YTDownloadTableScrollPane table;

	private JTextField outputField;
	private JButton browseButton;

	private List<JComponent> disableWhileDownloadingGroup;

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

		disableWhileDownloadingGroup = new LinkedList<>();

		initFrame();
		getContentPane().add(initTargetBar(), BorderLayout.NORTH);
		getContentPane().add(initDownloadBar(), BorderLayout.SOUTH);
		getContentPane().add(initTable(), BorderLayout.CENTER);

		setUpClipboard();

		setVisible(true);
	}

	private void initFrame(){
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(new Rectangle((int) (screensize.getWidth() / 2), (int) (screensize.getHeight() / 2)));
		setLocation((int) (screensize.getWidth() / 4), (int) (screensize.getHeight() / 4));

		setTitle("YouTube Downloader " + ConfigGUI.GUI_VERSION);
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

		disableWhileDownloadingGroup.add(browseButton);

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

		bottomBar.add(outputLabel,  BorderLayout.WEST);
		bottomBar.add(outputField,  BorderLayout.CENTER);
		bottomBar.add(browseButton, BorderLayout.EAST);

		return bottomBar;
	}

	private JComponent initDownloadBar(){
		JPanel downloadBar = new JPanel();
		downloadBar.setLayout(new BorderLayout());

		JButton downloadButton = new JButton("download");

		disableWhileDownloadingGroup.add(downloadButton); //TODO relabeling instead of disabling

		downloadButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, StatusUpdate> worker = new SwingWorker<Void, StatusUpdate>(){
					@Override
					protected Void doInBackground() throws Exception {
						try{
							//clear status first
							for (int i = 0; i<table.getJTable().getRowCount(); i++) {
								table.getJTable().getModel().setValueAt("", i, 7); // 7 -> status
							}

							//disable certain elements
							Iterator<JComponent> it = disableWhileDownloadingGroup.iterator();
							while (it.hasNext()) {
								it.next().setEnabled(false);
							}

							table.getJTable().getColumnModel().getColumn(8).setCellEditor(new DisabledJButtonTableCellEditor());
							table.getJTable().getColumnModel().getColumn(8).setCellRenderer(new DisabledJButtonTableCellRenderer());
							//table.getJTable().repaint();
							((DefaultTableModel) table.getJTable().getModel()).fireTableDataChanged();


							List<Object[]> toDownload = table.getEntries(); //YTMediaList, JComboBox, JComboBox
							if(toDownload != null && !toDownload.isEmpty()){
								for(int i = 0; i < toDownload.size(); i++){
									YTMediaList current = (YTMediaList) (toDownload.get(i)[0]);
									publish(new StatusUpdate("downloading", i));

									JComboBox<Integer> videoComboBox = (JComboBox<Integer>) (toDownload.get(i)[1]);
									int videoValue = ((Integer) videoComboBox.getSelectedItem()).intValue();
									//System.out.println("videoValue: " + videoValue);
									JComboBox<Integer> audioComboBox = (JComboBox<Integer>) (toDownload.get(i)[2]);
									int audioValue = ((Integer) audioComboBox.getSelectedItem()).intValue();
									//System.out.println("audioValue: " + audioValue);

									current.downloadAndMuxIfPossible(outputField.getText(), videoValue, audioValue);

									publish(new StatusUpdate("finished", i));
								}
							}

							//TODO still buggy
							table.getJTable().getColumnModel().getColumn(8).setCellEditor(new JButtonTableCellEditor(table.getEntries()));
							table.getJTable().getColumnModel().getColumn(8).setCellRenderer(new JButtonTableCellRenderer());
							//table.getJTable().repaint();
							((DefaultTableModel) table.getJTable().getModel()).fireTableDataChanged();

							it = disableWhileDownloadingGroup.iterator();
							while (it.hasNext()) {
								it.next().setEnabled(true);
							}

							if(Config.VERBOSE) {
								System.out.println("All jobs finished.");
							}
						}
						catch(IOException ex){
							//TODO
							ex.printStackTrace();
						}

						return null;
					}

					@Override
					protected void process(List<StatusUpdate> statusUpdates) {
						for(StatusUpdate s : statusUpdates){
							JTable tbl = table.getJTable();
							tbl.setValueAt(s.getStatus(), s.getRow(), 7); //column 7
						}
					}

				};
				worker.execute();
			}
		});

		downloadBar.add(downloadButton, BorderLayout.CENTER);

		return downloadBar;
	}

	private JComponent initTable(){
		table = new YTDownloadTableScrollPane();

		//TODO move to YTDownloadScrollPane and select file
		table.getJTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				Point p = me.getPoint();
				int row = table.getJTable().rowAtPoint(p);
				if (me.getClickCount() == 2) {
					if(Config.VERBOSE) {
						System.out.println("Double-click at row " + row);
					}
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(outputField.getText()));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		});

		return table;
	}

	private void setUpClipboard(){
		Clipboard sysClip = this.getToolkit().getSystemClipboard();
		sysClip.setContents(sysClip.getContents(this), this);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {

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
								//TODO implement as SwingWorker?
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

