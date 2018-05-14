import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

public class Embro extends JPanel implements ActionListener, KeyListener, ComponentListener, MouseListener, MouseMotionListener {

	private final int LOAD = 0, COMPOSING = 1, WORKING = 2, maxSelectableHeight = 50, selectableSpacing = 10, menuHeight = 25, selectableBorder = 3;
	private int sourceHeight, width, height, paletteSize, numberWidth, paletteOffset, progress, lastProgress, numLocalImages, numProjects, maxXOffset;
	private int sourceWidth = 0, xOffset = 0, yOffset = 0, zoom = 1, paletteIndex = 0, screenWidth = 800, screenHeight = 600, mode = LOAD, selection = -1, view = 0, viewPan = 1;
	private int[][][] rgbArray, rawArray, pixelatedArray;
	private int[][] palette, paletteIndices, highlighted;
	private int[] paletteTotals;
	private boolean holdingCtrl = false, blankable = true, dropperSet = false;
	private Color dropper;
	private Point mousePos = new Point(0, 0);
	private Rectangle screen = new Rectangle(1, 1, screenWidth, screenHeight);
	private Rectangle[] selectables;
	private File folder = new File(System.getProperty("user.dir"));
	private File[] allFiles = folder.listFiles();
	private String name, doNotChange = "keep", knotText = "Enter the number of knots across the width of the image.", colText = "Enter the maximum number of colours/strings to use.";
	private String[] localImages = new String[(int) folder.length()], localProjects = new String[(int) folder.length()];
	private BufferedImage rawImage;
	private JButton button = new JButton("Load");
	private TextField loadField = new TextField("Enter image file path to load it."), otherField = new TextField("");
	private final Timer t = new Timer(10, this);
	public static final long serialVersionUID = 1;

	public Embro() {  
		setLayout(null);
		addKeyListener(this);
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setPreferredSize(new java.awt.Dimension(screenWidth, screenHeight));
		this.setFocusTraversalKeysEnabled(false);
		add(loadField);
		loadField.addActionListener(this);
		loadField.addMouseListener(this);
		loadField.setBounds(0, 0, (int) (0.8 * screenWidth), menuHeight);
		add(otherField);
		otherField.addActionListener(this);
		otherField.addMouseListener(this);
		otherField.setBounds((int) (0.4 * screenWidth), 0, (int) (0.4 * screenWidth), menuHeight);
		otherField.setVisible(false);
		add(button);
		button.addActionListener(this);
		button.setBounds((int) (0.8 * screenWidth), 0, (int) (0.2 * screenWidth), menuHeight);
		numLocalImages = 0;
		numProjects = 0;
		for (int i = 0; i < allFiles.length; i++) {
			if (isValidFileType(allFiles[i].getName())) {
				localImages[numLocalImages] = allFiles[i].getName();
				System.out.println("Discovered local image: " + localImages[numLocalImages]);
				numLocalImages++;
			}
		}
		File pjDir = new File(folder + "\\Projects");
		if (pjDir.exists()) {
			File[] allProjects = pjDir.listFiles(); 
			for (int i = 0; i < allProjects.length; i++) {
				if (allProjects[i].isDirectory()) {
					File savedImage = new File(folder + "\\Projects\\" + allProjects[i].getName() + "\\" + allProjects[i].getName() + ".png");
					if (savedImage.exists()) {
						localProjects[numProjects] = allProjects[i].getName();
						System.out.println("Discovered local project: " + localProjects[numProjects]);
						numProjects++;
					}
				}
			}
		}
		selectables = new Rectangle[numProjects + numLocalImages];
		int selectableHeight = (int) Math.min(maxSelectableHeight, screenHeight / Math.max(numLocalImages, numProjects));
		int selectableWidth = (int) (0.5 * (screenWidth - 3 * selectableSpacing));
		for (int i = 0; i < numLocalImages; i++)
			selectables[i] = new Rectangle(2 * selectableSpacing + selectableWidth, i * selectableHeight + 2 * menuHeight + selectableSpacing, selectableWidth, selectableHeight);
		for (int i = 0; i < numProjects; i++)
			selectables[i + numLocalImages] = new Rectangle(selectableSpacing, i * selectableHeight + 2 * menuHeight + selectableSpacing, selectableWidth, selectableHeight);
		t.start();
	}
	public void actionPerformed(ActionEvent e) {
		switch (mode) { 
			case LOAD:
				if (e.getSource() == button && selection != -1) {	//selection is -1 when no local file has been selected, 0 to numLocalImages when a local image is picked, and numLocalImages to numLocalImages + numProjects when a local project is selected. 
					if (selection >= numLocalImages) { 		//a pre-existing project
						selection -= numLocalImages;
						name = localProjects[selection];
						loadField.setVisible(false);
						otherField.setVisible(false);
						button.setVisible(false);
						System.out.println("Calling loadProject with name: " + folder.getPath() + "\\Projects\\" + name + "\\" + name);
						loadProject(folder.getPath() + "\\Projects\\" + name + "\\" + name);
						mode = WORKING;
					} else {	//a local image
						File input = new File(folder + "\\" + localImages[selection]);
						rawImage = loadImage(input);
						if (rawImage == null) {
							loadField.setText("Failed to load " + input.getName());
							blankable = true;
						} else	//image loaded from path successfully
							ingestImage(rawImage);
						name = input.getName().substring(0, input.getName().lastIndexOf('.'));
						mode = COMPOSING;
						loadField.setBounds(0, 0, (int) (0.4 * screenWidth), menuHeight);
						loadField.setText(knotText);
						otherField.setText(colText);
						otherField.setVisible(true);
						button.setText("Generate");
						blankable = true;
						System.out.println("Embarking on processing local image named: " + name);
					}
				}
				else if (e.getSource() == loadField || e.getSource() == button && selection == -1) {
					File input = new File(loadField.getText());
					if (input.exists()) {
						if (isValidFileType(input.getName())) {	//is a supported image type
							rawImage = loadImage(input);
							if (rawImage == null) {
								loadField.setText("Failed to load " + input.getName());
								blankable = true;
							} else	//image loaded from path successfully
								ingestImage(rawImage);
							name = input.getName().substring(0, input.getName().lastIndexOf('.'));
							mode = COMPOSING;
							loadField.setBounds(0, 0, (int) (0.4 * screenWidth), menuHeight);
							loadField.setText(knotText);
							otherField.setText(colText);
							otherField.setVisible(true);
							blankable = true;
							screen = new Rectangle(-menuHeight, -menuHeight, screenWidth + 3 * menuHeight, screenHeight + 3 * menuHeight);	//because zoom isn'tt set yet, and will be set on resize
							System.out.println("Embarking on processing file named: " + name);
						} else {
							loadField.setText("That file type is not supported, try saving it as a .PNG.");
							blankable = true;
						}
							
					} else {
						loadField.setText("File not found at that location. Use the full path.");
						blankable = true;
					}
				}
			break;
			case COMPOSING:
				if (e.getSource() == button && button.getText().equals("Continue")) {	//have completed composing
					mode = WORKING;
					rawArray = null;
					pixelatedArray = null;
					loadField.setVisible(false);
					otherField.setVisible(false);
					button.setVisible(false);
					System.out.println("CREATING PROJECT in directory: " + folder + "\\Projects\\" + name);
					createProject(folder + "\\Projects\\" + name);
					repaint();
					requestFocus();
					return;
				}
				if (e.getSource() == loadField || e.getSource() == otherField || e.getSource() == button) {
					xOffset = 0;
					yOffset = 0;
					String temp;
					if (loadField.getText().toLowerCase().equals(doNotChange)) {	//keeping image size
						sourceWidth = rawImage.getWidth();
						sourceHeight = (int) (1.0 * rawImage.getHeight() * sourceWidth / rawImage.getWidth());
						loadField.setText("" + sourceWidth);
						zoom = (int) (1.0 * screenWidth / sourceWidth);
						pixelatedArray = new int[sourceWidth][sourceHeight][3];
						for (int i = 0; i < rawImage.getWidth(); i++)
							for (int j = 0; j < rawImage.getHeight(); j++) { 
								Color col = new Color(rawImage.getRGB(i, j), true);
								pixelatedArray[i][j][0] = col.getRed();
								pixelatedArray[i][j][1] = col.getGreen();
								pixelatedArray[i][j][2] = col.getBlue();
							}
					} else {
						temp = cleanNumbers(loadField.getText());
						if (sourceWidth == 0 || !temp.equals("" + sourceWidth)) {
							System.out.println("Generating pixelated image from number: " + temp);
							try {
								sourceWidth = Integer.parseInt(temp);	//generate pixelated image
								if (sourceWidth < 1 || sourceWidth > rawImage.getWidth()) {
									loadField.setText(sourceWidth < 1 ? "That number not possible." : "Cannot be bigger than reference image");
									return;
								}
								loadField.setText("" + sourceWidth);
							}
							catch (Exception ex) {
								System.out.println("Invalid input to pixel field: " + temp);
								loadField.setText("Invalid number.");
								return;
							}
							zoom = (int) (1.0 * screenWidth / sourceWidth);
							sourceHeight = (int) (1.0 * rawImage.getHeight() * sourceWidth / rawImage.getWidth());
							System.out.println("Creating Pixelated Array: " + sourceWidth + " x " + sourceHeight);
							pixelatedArray = new int[sourceWidth][sourceHeight][3];
							int a, b;
							double scaleFactor = 1.0 * sourceWidth / rawImage.getWidth();
							int[][] normalisation = new int[sourceWidth][sourceHeight];
							for (int i = 0; i < rawImage.getWidth(); i++) {	//sum included pixels
								for (int j = 0; j < rawImage.getHeight(); j++) {
									a = (int) (scaleFactor * i);
									b = (int) (scaleFactor * j);
									b = b >= sourceHeight ? sourceHeight - 1 : b;	//round off edge pixels that put it oversize, might be corrected more accurately 
									Color col = new Color(rawImage.getRGB(i, j), true);
									pixelatedArray[a][b][0] += col.getRed();
									pixelatedArray[a][b][1] += col.getGreen();
									pixelatedArray[a][b][2] += col.getBlue();
									normalisation[a][b]++;
								}
							}
							for (int i = 0; i < sourceWidth; i++)	//normalise intensity
								for (int j = 0; j < sourceHeight; j++) {
									pixelatedArray[i][j][0] = (int) (pixelatedArray[i][j][0] / (normalisation[i][j] == 0 ? 1 : normalisation[i][j]));
									pixelatedArray[i][j][1] = (int) (pixelatedArray[i][j][1] / (normalisation[i][j] == 0 ? 1 : normalisation[i][j]));
									pixelatedArray[i][j][2] = (int) (pixelatedArray[i][j][2] / (normalisation[i][j] == 0 ? 1 : normalisation[i][j]));
								}
						}
					}
					temp = cleanNumbers(otherField.getText());
					if (!otherField.getText().equals(colText) || paletteSize != 0 && !temp.equals("" + paletteSize)) {
						int[][] pixels = new int [sourceWidth][sourceHeight];
						for (int x = 0; x < sourceWidth; x++)	//insert the pixelated image into pixels
							for (int y = 0; y < sourceHeight; y++)					
								pixels[x][y] = ((0xff<<24) | (pixelatedArray[x][y][0]<<16) | (pixelatedArray[x][y][1]<<8) | pixelatedArray[x][y][2]);
						if (!otherField.getText().toLowerCase().equals(doNotChange)) {
							try {
								paletteSize = Integer.parseInt(temp);	//generate palletised image
								if (paletteSize < 1 || paletteSize > 255) {
									otherField.setText("Invalid number.");
									return;
								}
							}
							catch (Exception exception) {
								System.out.println("Invalid input to pixel field: " + temp);
								return;
							}
							int[] newPalette = Quantize.quantizeImage(pixels, paletteSize);	//map pixels to this new paletteSize
							for (int x = 0; x < sourceWidth; x++)	//insert new palette into modified pixels
								for (int y = 0; y < sourceHeight; y++)
									pixels[x][y] = newPalette[pixels[x][y]];
							System.out.println("paletteSize target: " + paletteSize + ", result: " + newPalette.length);
							paletteSize = newPalette.length;
							otherField.setText("" + paletteSize);
						}
						rgbArray = new int[sourceWidth][sourceHeight][3];
						for (int x = 0; x < sourceWidth; x++)	//insert pixels into rgbArray
							for (int y = 0; y < sourceHeight; y++) {
			                    int pixel = pixels[x][y];
			                    rgbArray[x][y][0] = (pixel >> 16) & 0xFF;
			                    rgbArray[x][y][1] = (pixel >>  8) & 0xFF;
			                    rgbArray[x][y][2] = (pixel >>  0) & 0xFF;
							}
						button.setText("Continue");
						dropper = new Color(rgbArray[0][0][0], rgbArray[0][0][1], rgbArray[0][0][2]);
					}
				}
			break;		
		}
		t.stop();
		repaint();
//		requestFocus();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setBackground(Color.GRAY);
		switch(mode) { 
			case LOAD:
				if (selectables[0] == null)
					return;
				g.setColor(Color.WHITE);
				g.setFont(new Font(g.getFont().getName(), 1, menuHeight - 5));
				g.drawString("Projects:", 2 * selectableSpacing, 2 * menuHeight);
				g.drawString("Local Images:", (int) (3 * selectableSpacing + selectables[0].getWidth()), 2 * menuHeight);
				for (int i = 0; i < selectables.length; i++) {
					g.setColor(Color.BLACK);
					g.fillRect((int) selectables[i].getX(), (int) selectables[i].getY(), (int) selectables[i].getWidth(), (int) selectables[i].getHeight());
					if (selection != i) { 
						g.setColor(Color.WHITE);
						g.fillRect((int) selectables[i].getX() + selectableBorder, (int) selectables[i].getY() + selectableBorder, (int) selectables[i].getWidth() - 2 * selectableBorder, (int) selectables[i].getHeight() - 2 * selectableBorder);
					}
					g.setColor(selection == i ? Color.WHITE : Color.BLACK);
					if (i < numLocalImages)
						g.drawString(localImages[i], (int) selectables[i].getX() + selectableSpacing, (int) selectables[i].getY() - selectableSpacing + 2 * menuHeight - 5);
					else
						g.drawString(localProjects[i - numLocalImages], (int) selectables[i].getX() + selectableSpacing, (int) selectables[i].getY() - selectableSpacing + 2 * menuHeight - 5);
					
				}
				
			break;
			case COMPOSING:
				if (sourceWidth == 0) {	//only loaded raw image
					System.out.println("Drawing raw image.");
					g.drawImage(rawImage, -xOffset, menuHeight - yOffset, this);
				} else if (paletteSize == 0) {	//not set palette size, only pixelated
					System.out.println("Drawing pixelated image.");
					for (int i = 0; i < sourceWidth; i++)
						for (int j = 0; j < sourceHeight; j++) {
							g.setColor(new Color(pixelatedArray[i][j][0], pixelatedArray[i][j][1], pixelatedArray[i][j][2]));
							g.fillRect(i * zoom - xOffset, j * zoom + menuHeight - yOffset, zoom, zoom);
						}
				} else {	//has set pixelation and palette size 
					System.out.println("Drawing quantized image.");
					Point pixel;
					for (int i = 0; i < sourceWidth; i++)	//draw the quantized image
						for (int j = 0; j < sourceHeight; j++) {
							pixel = new Point(i * zoom - xOffset, j * zoom + menuHeight - yOffset);
							if (screen.contains(pixel)) {
								g.setColor(new Color(rgbArray[i][j][0], rgbArray[i][j][1], rgbArray[i][j][2]));
								g.fillRect(pixel.x, pixel.y, zoom, zoom);
							}
						}
				}
				if (button.getText().equals("Continue") && mousePos.x != 0 && mousePos.y != 0 && screen.contains(mousePos)) {
					g.setColor(dropper);
					g.fillOval((int) (mousePos.x - 0.4 * zoom), (int) (mousePos.y - 0.4 * zoom), (int) (0.8 * zoom), (int) (0.8 * zoom));
				}
			break;
			case WORKING:
				width = zoom * sourceWidth;
				height = zoom * sourceHeight;
				Font theFont = g.getFont();
				g.setFont(new Font(theFont.getName(), 1, zoom - 3));
				FontMetrics fm = g.getFontMetrics();
				Rectangle2D fontRect = fm.getStringBounds("" + sourceWidth, g);
				numberWidth = (int) fontRect.getWidth() + 7;
				paletteOffset = (int) (numberWidth * (paletteSize + 1.5));
				Rectangle pixel, screen = new Rectangle(-zoom, -zoom, screenWidth + 3 * zoom, screenHeight + 3 * zoom);
				for (int i = 0; i < sourceWidth; i++)	//draw the design & gridlines
					for (int j = 0; j < sourceHeight; j++) {
						pixel = new Rectangle(paletteOffset + i * zoom - xOffset, j * zoom - yOffset, zoom, zoom);
						if (screen.contains(pixel)) {
							g.setColor(new Color(rgbArray[i][j][0], rgbArray[i][j][1], rgbArray[i][j][2]));
							g.fillRect(pixel.x + 1, pixel.y + 1, pixel.width - 1, pixel.height - 1);
							g.setColor(contrastWith(g.getColor()));
							g.drawLine(pixel.x, pixel.y, pixel.x + zoom, pixel.y);
							g.drawLine(pixel.x, pixel.y + 1, pixel.x, pixel.y + zoom);
							if(highlighted[i][j] == 1)
								g.drawLine(pixel.x, pixel.y, pixel.x + zoom, pixel.y + zoom);
							if(highlighted[i][j] == 2)
								g.drawLine(pixel.x, pixel.y + zoom, pixel.x + zoom, pixel.y);
						}
					}
				for (int i = 0; i < paletteSize; i++) {			//DISPLAY PALETTE
					g.setColor(new Color(palette[i][0], palette[i][1], palette[i][2]));
					g.fillRect(i * numberWidth - xOffset, -yOffset, numberWidth, height);
					int highlight = (int) (1 + progress / sourceWidth) * zoom - yOffset;					
					g.setColor(contrastWith(g.getColor()));
					g.drawLine(i * numberWidth - xOffset, highlight, (i + 1) * numberWidth - xOffset, highlight);
				}
				int progressTally = 0, thisX = 0;
				maxXOffset = 0;
				Point keyProgress = new Point(-1 , -1);
				for (int j = 0; j < sourceHeight; j++) {
					int[] totals = new int[paletteSize];
					if (j * zoom - yOffset < screenHeight) {	//below screen exclusion, keeping above in to make counting work
						int numStrings = 0;
						for (int i = 0; i < sourceWidth; i++)	//count number of knots per colour
							totals[paletteIndices[i][j]]++;
						for (int p = 0; p < paletteSize; p++)
							if (totals[p] != 0) {
								g.setColor(contrastWith(new Color(palette[p][0], palette[p][1], palette[p][2])));
								g.drawString("" + totals[p], p * numberWidth - xOffset + (int) (0.5 * (numberWidth - fm.getStringBounds("" + totals[p], g).getWidth())), (j + 1) * zoom - yOffset - 2);
								numStrings++;
							}
						g.setColor(Color.WHITE);
						g.drawString("" + numStrings, paletteOffset - zoom - xOffset, (j + 1) * zoom - yOffset - 2); 
						int i = 0, currentColourIndex = paletteIndices[0][j], currentTally = -1, startPoint = 0;
						while (i < sourceWidth) {		//draw knot streaks
							while (paletteIndices[i][j] == currentColourIndex && i < sourceWidth - 1) {
								currentTally++;
								i++;
							}
							if (i == sourceWidth - 1)
								currentTally++;
							if (progressTally <= progress && progressTally + currentTally + 1 > progress)
								keyProgress = new Point(width + (int) (startPoint * numberWidth + 1.5 * zoom), j * zoom);
							progressTally += currentTally + 1;
							g.setColor(new Color(palette[currentColourIndex][0], palette[currentColourIndex][1], palette[currentColourIndex][2]));
							thisX = (int) (startPoint * numberWidth + 1.5 * zoom);
							g.fillRect(paletteOffset + width + thisX - xOffset, j * zoom - yOffset, numberWidth, zoom);
							if (thisX  + numberWidth > maxXOffset)
								maxXOffset = thisX + numberWidth;
							g.setColor(contrastWith(g.getColor()));
							g.drawString("" + (currentTally + 1), paletteOffset + width + (int) (startPoint * numberWidth + 1.5 * zoom  + 0.5 * (numberWidth - fm.getStringBounds("" + (currentTally + 1), g).getWidth())) - xOffset, (j + 1) * zoom - yOffset - 3);
							if ((int) ((progressTally - 1) / sourceWidth) == (int) (progress / sourceWidth))
								g.drawLine(paletteOffset + width + (int) (startPoint * numberWidth + 1.5 * zoom) - xOffset, (j + 1) * zoom - yOffset - 1, paletteOffset + width + (int) ((startPoint + 1) * numberWidth + 1.5 * zoom) - xOffset, (j + 1) * zoom - yOffset - 1);
							currentColourIndex = paletteIndices[i][j];
							currentTally = 0;
							startPoint++;
							i++;
						}
					}
				}
				g.setColor(Color.BLACK);	//highlighting, progress & border, etc
				g.drawLine(paletteOffset + width - xOffset, -yOffset, paletteOffset + width - xOffset, height - yOffset);
				g.drawLine(paletteOffset - xOffset, height - yOffset, paletteOffset + width - xOffset, height - yOffset);
				g.drawRect(paletteOffset + (int) keyProgress.getX() - 1 - xOffset, (int) keyProgress.getY() - 2 - yOffset, numberWidth + 2, zoom + 2);
				g.drawRect(paletteOffset + (int) keyProgress.getX() + 1 - xOffset, (int) keyProgress.getY() - yOffset, numberWidth - 2, zoom - 2);
				g.drawRect(paletteOffset + (progress % sourceWidth) * zoom - 1 - xOffset, (int) (progress / sourceWidth) * zoom - 1 - yOffset, zoom + 2, zoom + 2);
				g.drawRect(paletteOffset + (progress % sourceWidth) * zoom + 1 - xOffset, (int) (progress / sourceWidth) * zoom + 1 - yOffset, zoom - 2, zoom - 2);
				g.drawLine(paletteSize * numberWidth - xOffset, (int) keyProgress.getY() + zoom - yOffset, paletteOffset - xOffset, (int) keyProgress.getY() + zoom - yOffset);
				g.drawLine(paletteOffset + width - xOffset, (int) keyProgress.getY() + zoom - yOffset, paletteOffset + width + (int) (1.5 * zoom) - xOffset, (int) keyProgress.getY() + zoom - yOffset);
				for (int i = 0; i < sourceWidth; i++)
					for (int j = 0; j < sourceHeight; j++)
						if (progress > i + j * sourceWidth)
							g.drawLine(paletteOffset + i * zoom - xOffset, (int) ((j + 0.5) * zoom) - yOffset, paletteOffset + (i + 1) * zoom - xOffset - 1, (int) ((j + 0.5) * zoom - yOffset));
						else break;
				g.setColor(Color.RED);
				g.drawRect(paletteOffset + (progress % sourceWidth) * zoom - xOffset, (int) (progress / sourceWidth) * zoom - yOffset, zoom, zoom);
				g.drawRect(paletteOffset + (int) keyProgress.getX() - xOffset, (int) keyProgress.getY() - 1 - yOffset, numberWidth, zoom);
				saveProgress();
			break;
		}
		requestFocus();
	}
	private int paletteContains(int i, int j) {
		for (int f = 0; f <= paletteIndex; f++) {
			int match = 0;
			for (int g = 0; g < 3; g++)
				if (palette[f][g] == rgbArray[i][j][g])
					match++;
			if (match == 3)
				return f;
		}
		return -1;
	}
	private Color contrastWith(Color c) {
		if (c.getRed() + c.getGreen() + c.getBlue() > 0.25 * 3 * 255)
			return Color.BLACK;
		return Color.WHITE;
	}
	public void keyTyped(KeyEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() != KeyEvent.VK_Z || !holdingCtrl)
			holdingCtrl = false;
	}
	public void keyPressed(KeyEvent e) {
		if (mode == LOAD)
			return;
		double dz;	//proportion of change done to zoom, is applied to offsets.
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				yOffset -= (int) (((mode == WORKING || mode == COMPOSING && sourceWidth != 0) ? (4.0 / zoom) : 0.25) * screenWidth);
				if (yOffset < 0)
					yOffset = 0;
				repaint();
			break;
			case KeyEvent.VK_DOWN:
				if (mode == WORKING || mode == COMPOSING && sourceWidth != 0) {
					yOffset += (int) (4 * screenWidth / zoom);
					if (yOffset > zoom * sourceHeight - screenHeight + (mode == COMPOSING ? menuHeight : 0))
						yOffset = zoom * sourceHeight - screenHeight + (mode == COMPOSING ? menuHeight : 0);
				} else {
					yOffset += (int) (0.25 * screenWidth);
					if (yOffset > rawImage.getHeight() - screenHeight + (mode == COMPOSING ? menuHeight : 0))
						yOffset = rawImage.getHeight() - screenHeight + (mode == COMPOSING ? menuHeight : 0);
				}
				repaint();
			break;
			case KeyEvent.VK_LEFT:
				xOffset -= (int) (((mode == WORKING || mode == COMPOSING && sourceWidth != 0) ? (4.0 / zoom) : 0.25) * screenHeight);
				if (xOffset < 0)
					xOffset = 0;
				repaint();
			break;
			case KeyEvent.VK_RIGHT:
				if (mode == WORKING) {	//allows browsing to the end of the longest tally streak it's rendered yet 
					xOffset += (int) (4 * screenWidth / zoom);
					if (xOffset > paletteOffset + width + maxXOffset - screenWidth)
						xOffset = paletteOffset + width + maxXOffset - screenWidth;
				} else if (mode == COMPOSING && sourceWidth != 0) {	//keeps it on the composed image
					xOffset += (int) (4 * screenWidth / zoom);
					if (xOffset > zoom * sourceWidth - screenWidth)
						xOffset = zoom * sourceWidth - screenWidth;
				} else {	//keeps the raw image from going off screen
					xOffset += (int) (0.25 * screenHeight);
					if (xOffset > rawImage.getWidth() - screenWidth)
						xOffset = rawImage.getWidth() - screenWidth;
				}
				repaint();
			break;
			case KeyEvent.VK_PAGE_DOWN:
				dz = zoom;
				zoom = (int) (zoom * 0.5);
				if (zoom < 2)
					zoom = 2;
				dz = zoom / dz;
				xOffset *= dz;
				yOffset *= dz;
				repaint();
			break;
			case KeyEvent.VK_PAGE_UP:
				dz = zoom;
				zoom *= 2;
				if (zoom > 256)
					zoom = 256;
				dz = zoom / dz;
				xOffset *= dz;
				yOffset *= dz;
				repaint();
			break;
			case KeyEvent.VK_HOME:
				if (mode != LOAD && mode != COMPOSING && sourceWidth != 0)
					zoom = 16;
				xOffset = 0;
				yOffset = 0;
				repaint();
			break;
			case KeyEvent.VK_TAB:
				view = view + viewPan;
				if (view % 2 == 0)
					viewPan = -viewPan;
				xOffset = view == 0 ? 0 : view == 1 ? paletteOffset : paletteOffset + sourceWidth * zoom;
				repaint();
			break;
			case KeyEvent.VK_SPACE:
				if (mode == WORKING) {
					lastProgress = progress;
					progress++;
					if (progress > sourceHeight * sourceWidth)
						progress = sourceHeight * sourceWidth;	//Fireworks
				}
				repaint();
			break;
			case KeyEvent.VK_ENTER:
				lastProgress = progress;
				progress = sourceWidth * (int) ((progress + sourceWidth) / sourceWidth);
				if (progress > sourceHeight * sourceWidth)
					progress = sourceHeight * sourceWidth;	//Fireworks
				repaint();
			break;
			case KeyEvent.VK_CONTROL:
				holdingCtrl = true;
			break;
			case KeyEvent.VK_Z:
				if (mode == WORKING && holdingCtrl) {
					if (progress == lastProgress) {
						progress--;
						if (progress < 0)
							progress = 0;
						lastProgress = progress;
					} else
						progress = lastProgress;
					repaint();
				}
			break;
		}
	}
	public void componentResized(ComponentEvent e) {
		Dimension newSize = ((Component) e.getSource()).getSize();
		screenWidth = newSize.width;
		screenHeight = newSize.height;
		screen = new Rectangle(-zoom, -zoom, screenWidth + 3 * zoom, screenHeight + 3 * zoom);
		repaint();
	}
	private void saveImage() {
		int[] imagePixels = new int [sourceWidth * sourceHeight];
		int index = 0;
		for (int y = 0; y < sourceHeight; y++)
			for (int x = 0; x < sourceWidth; x++)
				imagePixels[index++] = ((0xff<<24) | (rgbArray[x][y][0]<<16) | (rgbArray[x][y][1]<<8) | rgbArray[x][y][2]);
		Image img = createImage (new MemoryImageSource(sourceWidth, sourceHeight, imagePixels, 0, sourceWidth));
		int[] pixels = new int[sourceWidth * sourceHeight];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, sourceWidth, sourceHeight, pixels, 0, sourceWidth);
		try {
			pg.grabPixels();
		}
		catch(InterruptedException ie) {}
		BufferedImage bimg = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_RGB);
		bimg.setRGB(0, 0, sourceWidth, sourceHeight, pixels, 0, sourceWidth);
		File output = new File(folder + "\\Projects\\" + name + "\\" + name + ".png");
		try {
			ImageIO.write(bimg, "png", output);
		}
		catch (IOException e) {
			System.out.println("" + e.toString());
		}
	}
	private void saveProgress() {
		try {
			BufferedWriter bW = new BufferedWriter(new FileWriter(folder + "\\Projects\\" + name + "\\" + name + ".txt"));
			bW.write("This file saves your progress through the project.");
			bW.newLine();
			bW.write("" + progress + " knots.");
			String line;
			for (int y = 0; y < sourceHeight; y++) {
				bW.newLine();
				line = "";
				for (int x = 0; x < sourceWidth; x++)
					line += highlighted[x][y] + ",";
				bW.write(line);
			}
			bW.close();
		}
		catch (IOException e) {}
	}
	private void createProject(String fileName) {
		System.out.println("CREATING THIS: " + fileName);
		File dir = new File(fileName), projectsDir = new File(folder + "\\Projects");
		if (!projectsDir.exists()) {
			try {
				projectsDir.mkdir();
		        System.out.println("Created project directory: " + projectsDir.getPath() + projectsDir.getName());
		    }
		    catch(SecurityException se) {
		    	System.out.println("Didn't make new directory: " + se.toString());
		        return;
		    }
		}
		try {
	        dir.mkdir();
	        System.out.println("Created new directory: " + dir.getName() + dir.getName());
	    }
	    catch(SecurityException se) {
	    	System.out.println("Didn't make new directory: " + se.toString());
	        return;
	    }
		progress = 0; 
		highlighted = new int[sourceWidth][sourceHeight];
		for (int i = 0; i < sourceWidth; i++)			//initialise highlighting to off
			for (int j = 0; j < sourceHeight; j++)
				highlighted[i][j] = 0;
		saveProgress();
		saveImage();
		loadProject(fileName + "\\" + dir.getName());
	}
	private void loadProject(String fileName) {
		System.out.println("Loading project with image name: " + fileName + ".png");
		BufferedImage bimg;
		try {
			File image = new File(fileName + ".png");
			if (image.exists())
				bimg = loadImage(image);
			else {
				System.out.println("Image not found in project folder: " + image);
				return;
			}
		}
		catch (Exception e) {
			System.out.println("Loading failed." + e.toString());
			return;
		}
		System.out.println("Loaded project sourceImage successfully, processing progress.");
		zoom = 16;
		sourceWidth = bimg.getWidth();
		sourceHeight = bimg.getHeight();
		width = zoom * sourceWidth;
		height = zoom * sourceHeight;
		rgbArray = new int[sourceWidth][sourceHeight][3];
		highlighted = new int[sourceWidth][sourceHeight];
		if (new File(fileName + ".txt").exists()) {
			try {	//load save progress file
				BufferedReader bR = new BufferedReader(new FileReader(fileName + ".txt"));
				bR.readLine();
				String temp = bR.readLine();
				progress = Integer.parseInt(temp.substring(0, temp.indexOf(' ')));
				for (int y = 0; y < sourceHeight; y++) {	 //ingest saved highlighting
					temp = bR.readLine();
					for (int x = 0; x < sourceWidth; x++)
						highlighted[x][y] = Integer.parseInt(String.valueOf(temp.charAt(2 * x)));
				}
				bR.close();
				lastProgress = progress;
			}
			catch (IOException e) {
				loadField.setText("Failed to load progress save file.");
				System.out.println("Progress/highlighting load fail. " + e.toString());			
			}
		} else {
			progress = 0;
		}
		for (int i = 0; i < sourceWidth; i++) {			//LOAD IMAGE DATA
			for (int j = 0; j < sourceHeight; j++) {
				Color col = new Color(bimg.getRGB(i, j), true);
				rgbArray[i][j][0] = col.getRed();
				rgbArray[i][j][1] = col.getGreen();
				rgbArray[i][j][2] = col.getBlue();
			}
		}
		paletteSize = 255; //set to max size the quantiser will return, it gets pruned after populating palette
		paletteIndices  = new int[sourceWidth][sourceHeight];
		palette = new int[paletteSize][4];
		paletteTotals = new int[paletteSize];
		for (int i = 0; i < paletteSize; i++) {
			palette[i][0] = -1;
			palette[i][1] = -1;
			palette[i][2] = -1;
		}
		for (int j = 0; j < sourceHeight; j++) {			//GENERATE PALETTE
			for (int i = 0; i < sourceWidth; i++) {
				int test = paletteContains(i, j);
				if (test == -1) {
					palette[paletteIndex][0] = rgbArray[i][j][0];
					palette[paletteIndex][1] = rgbArray[i][j][1];
					palette[paletteIndex][2] = rgbArray[i][j][2];
					System.out.println("Added a colour to palette at: " + paletteIndex + ", {" + rgbArray[i][j][0] + ", " + rgbArray[i][j][1] + ", " + rgbArray[i][j][2] + "}");
					paletteIndices[i][j] = paletteIndex;
					paletteIndex++;
				} else {
					paletteIndices[i][j] = test;
					paletteTotals[test]++;
				}
			}
		}
		paletteSize = paletteIndex;
		paletteOffset = (int) (zoom * (paletteSize + 1.5));
	}
	private BufferedImage loadImage(File input) {
		BufferedImage bimg = null;
		try {
			System.out.println("Trying to load " + input.getPath() + "___" + input.getName());
			bimg = ImageIO.read(input);
			System.out.println("Loaded successfully.");
		}
		catch (Exception e) {
			System.out.println("Failed to load " + input.getName() + "  " + e.toString());
		}
		return bimg;
	}
	private Boolean isValidFileType(String fileName) {
		String type = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		String[] validTypes = {"png", "gif", "jpg", "bmp", "jpeg", "jpe", "jfif", "dib"};
		for (int i = 0; i < validTypes.length; i++)
			if (type.equals(validTypes[i]))
				return true;
		return false;
	}
	private String cleanNumbers(String input) {
		String output = "";
		char[] validChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0; i < input.length(); i++)
			for (int j = 0; j < validChars.length; j++)
				if (input.charAt(i) == validChars[j]) {
					output += input.charAt(i);
					break;
				}
		return output;
	}
	private void ingestImage(BufferedImage input) { 
		rawArray = new int[rawImage.getWidth()][rawImage.getHeight()][3];
		for (int i = 0; i < input.getWidth(); i++)
			for (int j = 0; j <  input.getHeight(); j++) {
				Color col = new Color(input.getRGB(i, j), true);
				rawArray[i][j][0] = col.getRed();
				rawArray[i][j][1] = col.getGreen();
				rawArray[i][j][2] = col.getBlue();
			}
	}
	public void mouseReleased(MouseEvent e) {
		if (mode == WORKING) {	//highlighting
			int x = (int) ((e.getX() - paletteOffset + xOffset) / zoom);
			int y = (int) ((e.getY() + yOffset) / zoom);
			if (x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
				System.out.println("Highlighting off-image");
				return;
			}
			if (highlighted[x][y] == 0)	//not highlighted
				highlighted[x][y] = e.getButton() == MouseEvent.BUTTON1 ? 1 : 2; 
			else 	
				highlighted[x][y] = e.getButton() == MouseEvent.BUTTON1 ? highlighted[x][y] - 1 : (highlighted[x][y] + 1) % 3;
			System.out.println("Toggled highlighting at: " + x + ", " + y);
			repaint();
			saveProgress();
			return;
		}
		if (mode == LOAD && e.getY() >= 2 * menuHeight) {	//selecting projects and local images
			mousePos = new Point(e.getX(), e.getY());
			for (int i = 0; i < selectables.length; i++)
				if (selectables[i].contains(mousePos)) {
					selection = i;
					repaint();
					return;
				}
		}
		if (blankable && e.getSource() == loadField) {
			loadField.setText("");
			blankable = false;
			return;
		}
		if (e.getSource() == otherField) {
			otherField.setText("");
			return;
		}
		if (button.getText().equals("Continue")) {
			int x = (int) ((e.getX() + xOffset) / zoom);
			int y = (int) ((e.getY() + yOffset - menuHeight) / zoom);
			if (x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight)
				return;
			if (e.getButton() == MouseEvent.BUTTON1 && dropperSet) {
				rgbArray[x][y][0] = dropper.getRed();
				rgbArray[x][y][1] = dropper.getGreen();
				rgbArray[x][y][2] = dropper.getBlue();
				System.out.println("Placed colour: " + dropper.toString() + " at " + x + ", " + y);
			}
			else if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
				dropper = new Color(rgbArray[x][y][0], rgbArray[x][y][1], rgbArray[x][y][2]);
				System.out.println("Picked colour: " + dropper.toString() + " at " + x + ", " + y);
				dropperSet = true;
			}
			repaint();
		}
	}
	public void mouseMoved(MouseEvent e) {
		if (mode == COMPOSING && button.getText().equals("Continue")) {
			mousePos = new Point(e.getX(), e.getY());
			System.out.println("Dragged, updating mousePos: " + mousePos.toString()); 
			repaint();
		}
	}
	public void mouseDragged(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public static void main(String[] args) {
		JFrame frame = new JFrame("Embro!");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Embro());
		frame.pack();
		frame.setVisible(true);
	}
}