package textcollage;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * A panel that contains a large drawing area where strings
 * can be drawn.  The strings are represented by objects of
 * type DrawTextItem.  An input box under the panel allows
 * the user to specify what string will be drawn when the
 * user clicks on the drawing area.
 * @Author: ThankGod Ani Chiagozie
 */
public class DrawTextPanel extends JPanel {

	private ArrayList<DrawTextItem> theStrings = new ArrayList<>();  // change to an ArrayList<DrawTextItem> !
	private Color currentTextColor = Color.darkGray;  // Color applied to new strings.

	private Canvas canvas;  // the drawing area.
	private JTextField input;  // where the user inputs the string that will be added to the canvas
	private SimpleFileChooser fileChooser;  // for letting the user select files
	private JMenuBar menuBar; // a menu bar with command that affect this panel
	private MenuHandler menuHandler; // a listener that responds whenever the user selects a menu command
	private JMenuItem undoMenuItem;  // the "Remove Item" command from the edit menu

	/**
	 * An object of type Canvas is used for the drawing area.
	 * The canvas simply displays all the DrawTextItems that
	 * are stored in the ArrayList, strings.
	 */
	private class Canvas extends JPanel {
		Canvas() {
			setPreferredSize(new Dimension(800, 600));
			setBackground(Color.LIGHT_GRAY);
			setFont(new Font("Serif", Font.BOLD, 24));
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			if (theStrings.size() != 0)
				for (DrawTextItem item : theStrings) {
					item.draw(g);
				}
		}
	}

	/**
	 * An object of type MenuHandler is registered as the ActionListener
	 * for all the commands in the menu bar.  The MenuHandler object
	 * simply calls doMenuCommand() when the user selects a command
	 * from the menu.
	 */
	private class MenuHandler implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			doMenuCommand(evt.getActionCommand());
		}
	}

	/**
	 * Creates a DrawTextPanel.  The panel has a large drawing area and
	 * a text input box where the user can specify a string.  When the
	 * user clicks the drawing area, the string is added to the drawing
	 * area at the point where the user clicked.
	 */
	public DrawTextPanel() {
		fileChooser = new SimpleFileChooser();
		undoMenuItem = new JMenuItem("Remove Item");
		undoMenuItem.setEnabled(false);
		menuHandler = new MenuHandler();
		setLayout(new BorderLayout(3, 3));
		setBackground(Color.BLACK);
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		canvas = new Canvas();
		add(canvas, BorderLayout.CENTER);
		JPanel bottom = new JPanel();
		bottom.add(new JLabel("Text to add: "));
		input = new JTextField("Hello World!", 40);
		bottom.add(input);
		add(bottom, BorderLayout.SOUTH);
		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				doMousePress(e);
			}
		});
	}

	/**
	 * This method is called when the user clicks the drawing area.
	 * A new string is added to the drawing area.  The center of
	 * the string is at the point where the user clicked.
	 *
	 * @param e the mouse event that was generated when the user clicked
	 */
	public void doMousePress(MouseEvent e) {
		String text = input.getText().trim();
		if (text.length() == 0) {
			input.setText("Hello World!");
			text = "Hello World!";
		}
		DrawTextItem s = new DrawTextItem(text, e.getX(), e.getY());
		s.setTextColor(currentTextColor);  // Default is null, meaning default color of the canvas (black).

		theStrings.add(s);
		undoMenuItem.setEnabled(true);
		canvas.repaint();
	}

	/**
	 * Returns a menu bar containing commands that affect this panel.  The menu
	 * bar is meant to appear in the same window that contains this panel.
	 */
	public JMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new JMenuBar();

			String commandKey; // for making keyboard accelerators for menu commands
			if (System.getProperty("mrj.version") == null)
				commandKey = "control ";  // command key for non-Mac OS
			else
				commandKey = "meta ";  // command key for Mac OS

			JMenu fileMenu = new JMenu("File");
			menuBar.add(fileMenu);
			JMenuItem saveItem = new JMenuItem("Save...");
			saveItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "N"));
			saveItem.addActionListener(menuHandler);
			fileMenu.add(saveItem);
			JMenuItem openItem = new JMenuItem("Open...");
			openItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "O"));
			openItem.addActionListener(menuHandler);
			fileMenu.add(openItem);
			fileMenu.addSeparator();
			JMenuItem saveImageItem = new JMenuItem("Save Image...");
			saveImageItem.addActionListener(menuHandler);
			fileMenu.add(saveImageItem);

			JMenu editMenu = new JMenu("Edit");
			menuBar.add(editMenu);
			undoMenuItem.addActionListener(menuHandler); // undoItem was created in the constructor
			undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "Z"));
			editMenu.add(undoMenuItem);
			editMenu.addSeparator();
			JMenuItem clearItem = new JMenuItem("Clear");
			clearItem.addActionListener(menuHandler);
			editMenu.add(clearItem);

			JMenu optionsMenu = new JMenu("Options");
			menuBar.add(optionsMenu);
			JMenuItem colorItem = new JMenuItem("Set Text Color...");
			colorItem.setAccelerator(KeyStroke.getKeyStroke(commandKey + "T"));
			colorItem.addActionListener(menuHandler);
			optionsMenu.add(colorItem);
			JMenuItem bgColorItem = new JMenuItem("Set Background Color...");
			bgColorItem.addActionListener(menuHandler);
			optionsMenu.add(bgColorItem);
			JMenuItem randomCollage = new JMenuItem("Create Random Collage");
			randomCollage.addActionListener(menuHandler);
			optionsMenu.add(randomCollage);
		}
		return menuBar;
	}

	/**
	 * Carry out one of the commands from the menu bar.
	 *
	 * @param command the text of the menu command.
	 */
	private void doMenuCommand(String command) {
		switch (command) {
			case "Save...":  // save all the string info to a file
				File textFile = fileChooser.getOutputFile(this, "Select file to save work", "mywork.txt");
				if (textFile == null)
					return;

				saveContent(textFile);
				break;

			case "Open...":  // read a previously saved file, and reconstruct the list of strings
				File inputFile = fileChooser.getInputFile(this, "Select file to open previous work");
				if (inputFile == null)
					return;

				try (Scanner in = new Scanner(inputFile)) {
					loadContent(in);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "It was not possible to open file, please, make sure to choose a valid file.");
					e.printStackTrace();
					return;
				}
				break;

			case "Clear":   // remove all strings and formatting
				theStrings = new ArrayList<>();
				undoMenuItem.setEnabled(false);
				canvas.setBackground(Color.LIGHT_GRAY);
				canvas.repaint();
				break;

			case "Remove Item":  // remove the most recently added string
				theStrings.remove(theStrings.size() - 1);
				canvas.repaint();
				break;

			case "Set Text Color...": {
				Color c = JColorChooser.showDialog(this, "Select Text Color", currentTextColor);
				if (c != null)
					currentTextColor = c;
				break;
			}
			case "Set Background Color...": {
				Color c = JColorChooser.showDialog(this, "Select Background Color", canvas.getBackground());
				if (c != null) {
					canvas.setBackground(c);
					canvas.repaint();
				}
				break;
			}
			case "Save Image...":   // save a PNG image of the drawing area
				File imageFile = fileChooser.getOutputFile(this, "Select Image File Name", "textimage.png");
				if (imageFile == null)
					return;
				try {
					BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
							BufferedImage.TYPE_INT_RGB);
					Graphics g = image.getGraphics();
					g.setFont(canvas.getFont());
					canvas.paintComponent(g);  // draws the canvas onto the BufferedImage, not the screen!
					boolean ok = ImageIO.write(image, "PNG", imageFile); // write to the file
					if (!ok)
						throw new Exception("PNG format not supported (this shouldn't happen!).");
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Sorry, an error occurred while trying to save the image:\n" + e);
				}
				break;
			case "Create Random Collage":
				File randomCollageFile = fileChooser.getInputFile(this, "Select file to generate random collage");
				if (randomCollageFile == null)
					return;

				try (Scanner in = new Scanner(randomCollageFile)) {
					createRandomCollage(in);

				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "It was not possible to open file, please, make sure to choose a valid file.");
					e.printStackTrace();
					return;
				}
				break;
		}
	}

	private void createRandomCollage(Scanner in) {
		ArrayList<String> words = new ArrayList<>();
		in.useDelimiter("[^a-zA-Z]+");

		while (in.hasNext()) {
			String tk = in.next();
			words.add(tk);
		}

		ArrayList<DrawTextItem> randomItems = new ArrayList<>();

		for (int i = 0; i <= 20; i++) {
			String text = words.get((int) (Math.random() * (words.size() - 1)));
			int x = (int) (Math.random() * 800);
			int y = (int) (Math.random() * 600);

			DrawTextItem newItem = new DrawTextItem(text, x, y);
			newItem.setTextColor(randomColor());
			randomItems.add(newItem);
		}

		canvas.setBackground(randomColor());
		theStrings = randomItems;
		canvas.repaint();
	}

	private void saveContent(File textFile) {
		try (PrintWriter output = new PrintWriter(textFile)) {
			// it saves background color
			output.write(canvas.getBackground().getRed() + "\n");
			output.write(canvas.getBackground().getGreen() + "\n");
			output.write(canvas.getBackground().getBlue() + "\n");

			for (DrawTextItem item : theStrings) {
				// it saves text and color of each item
				output.write(item.getString() + "\n");
				output.write(item.getX() + "\n");
				output.write(item.getY() + "\n");
				output.write(item.getTextColor().getRed() + "\n");
				output.write(item.getTextColor().getGreen() + "\n");
				output.write(item.getTextColor().getBlue() + "\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("It was not possible to save work to file. Please, input an existing file.");
			e.printStackTrace();
		}
	}

	private void loadContent(Scanner in) {
		try {
			Color bkColor = new Color(Integer.parseInt(in.nextLine()),
					Integer.parseInt(in.nextLine()),
					Integer.parseInt(in.nextLine()));
			ArrayList<DrawTextItem> content = new ArrayList<>();

			while (in.hasNext()) {
				String text = in.nextLine();
				int x = Integer.parseInt(in.nextLine());
				int y = Integer.parseInt(in.nextLine());
				int red = Integer.parseInt(in.nextLine());
				int green = Integer.parseInt(in.nextLine());
				int blue = Integer.parseInt(in.nextLine());

				DrawTextItem newItem = new DrawTextItem(text, x, y);
				newItem.setTextColor(new Color(red, green, blue));
				content.add(newItem);
			}

			canvas.setBackground(bkColor);
			theStrings = content;
			canvas.repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "It was not possible to serialize content. Please, make sure to input a " +
					"file in a valid format.");
		}
	}

	private Color randomColor() {
		return new Color((int) (Math.random() * 255),
				(int) (Math.random() * 255),
				(int) (Math.random() * 255));
	}
}