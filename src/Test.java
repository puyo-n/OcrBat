
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

public class Test extends JFrame {

	private JFrame frame;
	private JPanel capture_panel;

	private Robot robot;
	private BufferedImage image;

	private Point initialClick;
	static String width;
	static String height;


	public Test() {
		EventQueue.invokeLater(() -> {
			Toolkit.getDefaultToolkit().setDynamicLayout(false);
			// JFrame.setDefaultLookAndFeelDecorated(true);
			frame = new JFrame();

			frame.setUndecorated(true);
			frame.setBackground(new Color(0x0, true));
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setAlwaysOnTop(true); // 常に前に表示

			capture_panel = new JPanel();
			capture_panel.setBackground(new Color(.5f, .8f, .5f, .0f));
			capture_panel.setBorder(new LineBorder(Color.RED, 2));

			// p.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			frame.getContentPane().add(capture_panel);

			frame.getContentPane().add(moveFrame(), BorderLayout.NORTH);

			// final JLabel label = new JLabel("Resize Here");
			// label.setBorder(BorderFactory.createLineBorder(Color.RED,3));
			// frame.add(label, BorderLayout.SOUTH);
//			JButton button5 = new JButton("ボタン");
//			button5.setMargin(new Insets(0, 2, 2, 2));
//			frame.add(button5);

			frame.setSize(320, 240);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			repaint();
			new ComponentResizer(frame);
		});

	}

	private JPanel moveFrame() {
		JPanel panel = new JPanel();

		panel.setBackground(new Color(.2f, .2f, .7f, .5f));
		panel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				initialClick = e.getPoint();
				getComponentAt(initialClick);
			}
		});
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {

				// get location of Window
				int thisX = frame.getLocation().x;
				int thisY = frame.getLocation().y;

				// Determine how much the mouse moved since the initial click
				int xMoved = e.getX() - initialClick.x;
				int yMoved = e.getY() - initialClick.y;

				// Move window to this position
				int X = thisX + xMoved;
				int Y = thisY + yMoved;
				frame.setLocation(X, Y);
				width = String.valueOf(frame.getWidth());
				height = String.valueOf(frame.getHeight());

			}
		});

		// 閉じるボタン
		JButton close_btn = new JButton("close");
		close_btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
		close_btn.setFont(new Font("SansSerif", Font.BOLD, 9));
		close_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				System.exit(0);
			}
		});
		panel.add(close_btn);

		// captureボタン
		JButton capture_btn = new JButton("capture");
		capture_btn.setFont(new Font("SansSerif", Font.BOLD, 9));
		capture_btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					capture();
				} catch (Exception e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			}
		});
		panel.add(capture_btn, BorderLayout.PAGE_END);

		JTextField text1 = new JTextField(width + " * " + height);
		panel.add(text1);
		pack();
		// BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		// panel.setLayout(layout);

		return panel;
	}

	

	private void capture() throws Exception {
		try {
			// キャプチャの範囲
			Rectangle bounds = new Rectangle(
					frame.getLocation().x+capture_panel.getLocation().x,
					frame.getLocation().y+capture_panel.getLocation().y,
					capture_panel.getWidth(), capture_panel.getHeight());
			System.out.println(width + "\t" + height);
			System.out.println("frame:"+frame.getBounds());
			System.out.println("capture_panel:"+capture_panel.getBounds());
			System.out.println();

			// これで画面キャプチャ
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(bounds);

			// 以下、出力処理
			String outputPath = "c:\\tmp";
			// String fileName = "test_" + format.format(new Date()) + ".jpg";
			String outputFileName = "test.jpg";
			File file = new File(outputPath +File.separator+ outputFileName);
			
			// 拡張子取得
			int extensionIndex = outputFileName.lastIndexOf(".");
			//第二引数がなければ、substringメソッドはそのインデックスから最後までの文字列を返す
			//このため、別に拡張子の文字数が確定していなくても拡張子を抽出できる
			String extension = outputFileName.substring(extensionIndex);
			outputFileName = outputFileName.substring(0, extensionIndex);
			int branch = 0;

			//わざわざファイルリストを取得しなくても、これでそのファイル名が存在するかは判定可能
			while(file.exists()) {  
			    branch++;
			    //これで順次Fileを作成しながら判定を繰り返せば、飛び番のファイル名にも対応可能
			    file = new File(outputPath +File.separator+ outputFileName + "_" + branch + extension);
		   
			}
//		    file = new File(outputPath +File.separator+ outputFileName + "_" + format.format(new Date()) + extension);
			
			 System.out.println("ファイル名："+file);
			ImageIO.write(image, "jpg", file);

			ocr(file);
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
	public void ocr(File file) throws IOException, TesseractException {
	
	    //画像読み込み
        File target = file;
        BufferedImage image = ImageIO.read(target);
 
        //解析
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\pleiades\\workspace\\Tess4J\\tessdata");
        tesseract.setLanguage("eng");
        List<Word> wordList = tesseract.getWords(image, TessPageIteratorLevel.RIL_BLOCK);
        String str = tesseract.doOCR(image);
        System.out.println(format.format(new Date()));
    	System.out.println("-------------------------");
        //結果出力
//        System.out.println(wordList);
        System.out.println(str);
    	System.out.println("-------------------------");
	}

	public static void main(String[] args) {

		new Test();

		// JFrame frame = new SimpleTransFrame3("Capture Test");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		// frame.getContentPane().setLayout(new FlowLayout());
		// frame.getContentPane().add(new JButton("Button"));
		//
		// frame.setBounds(100, 100, 400, 200);
		// frame.setVisible(true);
	}
}