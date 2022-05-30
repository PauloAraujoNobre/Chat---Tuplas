package chat.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import chat.tuples.ChatRoom;
import chat.tuples.Lookup;
import chat.tuples.User;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class MainWindow extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	//CONNECTION
	private JavaSpace space;
	
	//UI
	private JPanel chatList;
	private JScrollPane chatScrollPane;
	
	private ButtonGroup roomButtonGroup;	
	
	private JButton confirmButton;
	private JButton newChatRoomButton;
	private JButton updateButton;
	
	private JTextField usernameField;
	private JTextField roomField;
	
	private JCheckBox isNewRoom;
	
	private JLabel errorLabel;
	
	private JFrame roomsWindown;
	
	public MainWindow() {		
		initComponents();
		
		Lookup finder = new Lookup(JavaSpace.class);
		this.space = (JavaSpace) finder.getService();
		
		initSpace();
		
		setUpGUI();
		setUpNewChatRoomButton();
		setUpUpdateRoomButton();
//		searchRoomsButton();
	}
	
	private void initComponents() {
		this.chatList = new JPanel();
		this.chatScrollPane = new JScrollPane();
		
		this.usernameField = new JTextField();
		this.roomField = new JTextField();
		
		this.updateButton = new JButton();
		this.confirmButton = new JButton();
		this.newChatRoomButton = new JButton();		
		
		this.roomButtonGroup = new ButtonGroup();
		
		this.isNewRoom = new JCheckBox();
		
		this.errorLabel = new JLabel();
		
		this.roomsWindown = new JFrame("Rooms");
	}

	public void initSpace() {
		try {
			System.out.println("Procurando pelo servico JavaSpace...");

			if (space == null) {
				System.out.println("O servico JavaSpace nao foi encontrado. Encerrando...");
				System.exit(-1);
			}

			System.out.println("O servico JavaSpace foi encontrado.");
			System.out.println(space);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setUpGUI() {
		this.setResizable(false);
		this.setSize(370, 370);
		this.setTitle("Chat");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(new JLabel());	
		
		JLabel titleLabel = new JLabel("Create User");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setBounds(130, 0, 200, 60);
		
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
		usernameLabel.setBounds(20, 45, 200, 60);
		
		usernameField.setBounds(20, 90, 195, 25);
		
		updateButton.setText("Search");
		updateButton.setBounds(225, 90, 110, 23);
		
		JLabel isNewRoomLabel = new JLabel("New room?");
		isNewRoomLabel.setFont(new Font("Arial", Font.BOLD, 14));
		isNewRoomLabel.setBounds(20, 120, 90, 60);
		
		isNewRoom.setBounds(110, 138, 25, 25);
		ActionListener al = new ActionListener() 
		{
			public void actionPerformed(ActionEvent ae) {
				updateRoomField();
			}
		};
		isNewRoom.addActionListener(al);
		
		roomField.setBounds(20, 165, 195, 25);
		roomField.setVisible(false);
		
		newChatRoomButton.setText("Create Room");
		newChatRoomButton.setBounds(225, 165, 110, 23);
		newChatRoomButton.setVisible(false);
		this.add(titleLabel);
		this.add(chatScrollPane);
		this.add(usernameLabel);
		this.add(usernameField);
		this.add(updateButton);
		this.add(confirmButton);
		this.add(newChatRoomButton);
		this.add(isNewRoomLabel);
		this.add(isNewRoom);
		this.add(roomField);
		this.setVisible(true);
	}

	public void updateRoomField() {
		roomField.setVisible(isNewRoom.isSelected() ? true : false);
		newChatRoomButton.setVisible(isNewRoom.isSelected() ? true : false);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (!validateFields()) {
			return;
		}

		String roomName = roomButtonGroup.getSelection().getActionCommand();
		String username = usernameField.getText();
		
		if (!verifyIfUserExists(username, roomName)) {
			this.removeAll();
			this.setVisible(false);
			
			roomsWindown.removeAll();
			roomsWindown.setVisible(false);
			
			new ChatWindow(username, roomName, space);	
			
			User user = new User();
			user.name = username;
			user.roomName = roomName;
			
			try {
				space.write(user, null, Lease.FOREVER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Already exist user with this nickname: " + username + " in room " + roomName,
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private boolean validateFields() {
		boolean isValid = true;
		
		if (usernameField.getText().isEmpty()) {
			usernameField.setBorder(new LineBorder(Color.RED, 1));

			isValid = false;
		} else {
			usernameField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
		}
		 
		if (!validateButtonGroup(roomButtonGroup)) {
			isValid = false;
			
			errorLabel.setText("Você deve escolher uma sala antes de confirmar!");
			errorLabel.setForeground(Color.RED);
			errorLabel.setFont(new Font("Arial", Font.BOLD, 12));
			errorLabel.setBounds(55, 455, 300, 60);
			
			this.add(errorLabel);	
		} else {
			errorLabel.setForeground(this.getBackground());
		} 					

		return isValid;
	}
	
	private boolean validateButtonGroup(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();

			if (button.isSelected()) {
				return true;
			}
		}

		return false;
	}
	
	private void setUpNewChatRoomButton() {
		ActionListener al = new ActionListener() 
		{
			public void actionPerformed(ActionEvent ae) {
				
				while (true) {
					String username = usernameField.getText();
					String roomName = roomField.getText();
					
					if (roomField == null) {
						break;
					}
					
					if (roomName.isEmpty()) {
						JOptionPane.showMessageDialog(null, "RoomName dont be able null", "Erro",
								JOptionPane.ERROR_MESSAGE);
						break;
					} 
						
					if (username.isEmpty()) {
						JOptionPane.showMessageDialog(null, "UserName dont be able null", "Erro",
								JOptionPane.ERROR_MESSAGE);
						break;
					} 

					if (!username.isEmpty() && !roomName.isEmpty()) {
						
						if (!verifyIfRoomExists(roomName)) {
							MainWindow.this.removeAll();
							MainWindow.this.setVisible(false);

							new ChatWindow(username, roomName, space);

							ChatRoom chatRoom = new ChatRoom();
							chatRoom.name = roomName;
							
							User user = new User();
							user.name = username;
							user.roomName = roomName;
							
							try {
								space.write(chatRoom, null, Lease.FOREVER);
								space.write(user, null, Lease.FOREVER);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							break;
						} else {
							JOptionPane.showMessageDialog(null, "Already existe room with name: " + roomName, "Erro",
									JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
				}
			}
		};
		
		newChatRoomButton.addActionListener(al);
	}	
	
	private void setUpUpdateRoomButton() {
		ActionListener al = new ActionListener() 
		{
			public void actionPerformed(ActionEvent ae) {
				updateAvailableRooms();
			}
		};
		
		updateButton.addActionListener(al);
	}
	
	private void updateAvailableRooms() {		
		ChatRoom template = new ChatRoom();
		ChatRoom chatRoom;
		
		List<ChatRoom> rooms = new ArrayList<ChatRoom>();
		roomButtonGroup = new ButtonGroup();
		chatList.removeAll();
		
		try {
			while (true) {
				chatRoom = (ChatRoom) space.take(template, null, 3 * 1000);
				
				if (chatRoom != null) {
					rooms.add(chatRoom);
				} else {					
					break;
				}	
			}
			
			if (rooms.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Do not existe rooms createds, try create one",
						"Aviso", JOptionPane.INFORMATION_MESSAGE);
			} else {
				for (ChatRoom room : rooms) {				
					JRadioButton radioButton = new JRadioButton(room.name);
					radioButton.setFont(new Font("Arial", Font.PLAIN, 12));
					radioButton.setBackground(new Color(0, 0, 0, 0));
					radioButton.setOpaque(false);
					radioButton.setActionCommand(room.name);

					roomButtonGroup.add(radioButton);
					chatList.add(radioButton);

					SwingUtilities.updateComponentTreeUI(chatList);
					
					space.write(room, null, Lease.FOREVER);
					
					setRoomWindowInterface();
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setRoomWindowInterface() {
		roomsWindown.setResizable(false);
		roomsWindown.setSize(330, 400);
		roomsWindown.setTitle("Chat");
		roomsWindown.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		roomsWindown.setContentPane(new JLabel());	
		
		chatList.setBackground(Color.white);
		chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
		
		chatScrollPane.setViewportView(chatList);
		chatScrollPane.setBounds(20, 30, 270, 270);
		
		confirmButton.setText("Confirm");
		confirmButton.setBounds(185, 310, 100, 30);
		confirmButton.addActionListener(this);
		
		roomsWindown.setVisible(true);
//		roomsWindown.add(chatList);
		roomsWindown.add(chatScrollPane);
		roomsWindown.add(confirmButton);
	}
	
	private boolean verifyIfRoomExists(String roomName) {
		ChatRoom template = new ChatRoom();
		template.name = roomName;
		
		ChatRoom chatRoom;
		
		try {
			chatRoom = (ChatRoom) space.read(template, null, 1000);
			
			if (chatRoom != null) {
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean verifyIfUserExists(String username, String roomName) {
		User template = new User();
		template.name = username;
		template.roomName = roomName;
		
		User user;
		
		try {
			user = (User) space.read(template, null, 1000);
			
			if (user != null) {
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
