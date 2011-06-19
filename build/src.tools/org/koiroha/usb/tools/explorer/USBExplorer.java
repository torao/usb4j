/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBExplorer.java,v 1.14 2009/05/18 20:34:18 torao Exp $
*/
package org.koiroha.usb.tools.explorer;

import static java.awt.GridBagConstraints.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.impl.libusb.LibUSB0;
import org.koiroha.usb.tools.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBExplorer: USB エクスプローラ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスを表示するためのデモンストレーションツールです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.14 $ $Date: 2009/05/18 20:34:18 $
 * @author torao
 * @since 2009/04/29 Java2 SE 5.0
 */
public class USBExplorer extends JFrame {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このパッケージのリソースです。
	 * <p>
	*/
	private final Resource RS = new Resource(DeviceTreeModel.class);

	// ======================================================================
	// ツリー表示
	// ======================================================================
	/**
	 * ツリー表示のコンポーネントです。
	 * <p>
	 */
	public final JTree tree = new JTree();

	// ======================================================================
	// ツリー表示
	// ======================================================================
	/**
	 * ツリー表示のコンポーネントです。
	 * <p>
	 */
	private final DeviceTreeModel treeModel;

	// ======================================================================
	// プロパティ表示
	// ======================================================================
	/**
	 * プロパティ表示のコンポーネントです。
	 * <p>
	 */
	public final JTable table = new JTable();

	// ======================================================================
	// プロパティ表示
	// ======================================================================
	/**
	 * プロパティ表示のコンポーネントです。
	 * <p>
	 */
	private final PropertyTableModel tableModel = new PropertyTableModel();

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * デバイスをオープンしてコントロールパネルを開くためのボタンです。
	 * <p>
	 */
	private final JButton open = new JButton();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param console コンソール
	 */
	public USBExplorer(Console console) {
		this.setTitle(RS.format("title"));

		// ツリーコンポーネントの設定
		treeModel = new DeviceTreeModel();
		tree.setModel(treeModel);
		tree.setCellRenderer(new DeviceTreeModel.Renderer());

		// ツールバーの構築
		JToolBar toolbar = new JToolBar();
		open.setEnabled(false);
		open.setIcon(RS.getIcon("open.png"));
		open.setToolTipText(RS.format("menu.open"));
		toolbar.add(open);
		JButton refresh = new JButton();
		refresh.setIcon(RS.getIcon("refresh-normal.png"));
		refresh.setRolloverIcon(RS.getIcon("refresh-hover.png"));
		refresh.setToolTipText(RS.format("menu.refresh"));
		toolbar.add(refresh);
		toolbar.addSeparator();
		final JButton laf = new JButton();
		laf.setIcon(RS.getIcon("laf-normal.png"));
		laf.setRolloverIcon(RS.getIcon("laf-hover.png"));
		laf.setToolTipText(RS.format("menu.lookAndFeel"));
		toolbar.add(laf);

		// テーブルコンポーネントの設定
		table.setModel(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(140);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(320);
		table.setDefaultRenderer(Object.class, new PropertyTableModel.Renderer());

		// 左右の分割ペイン
		JSplitPane split1 = new JSplitPane();
		split1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		split1.setLeftComponent(new JScrollPane(tree));
		split1.setRightComponent(new JScrollPane(table));
		split1.setResizeWeight(0.0);
		split1.setDividerLocation(200);

		// 上下の分割ペイン
		JSplitPane split2 = new JSplitPane();
		split2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split2.setTopComponent(split1);
		split2.setBottomComponent(console);
		split2.setResizeWeight(1.0);
		split2.setDividerLocation(320);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(BorderLayout.NORTH, toolbar);
		this.getContentPane().add(BorderLayout.CENTER, split2);

		// 表示位置の設定
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(800, 480);
		this.setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);

		// ツリーの選択変更時にプロパティへ記述子を設定
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e) {
				select();
				return;
			}
		});

		// デバイスのオープン/クローズアクション
		open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				open();
				return;
			}
		});

		// デバイスの再読込アクション
		refresh.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				treeModel.refresh(true);
				tableModel.setDescriptor(null, null);
				return;
			}
		});

		// Look & Feel の変更
		laf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = LaF.getPopupMenu(USBExplorer.this);
				menu.show(laf, 0, laf.getSize().height);
				return;
			}
		});

		// ウィンドウ終了時にすべてのデバイスを開放
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				treeModel.release();
				return;
			}
		});


		// メニューバーの構築
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		JMenu help = new JMenu(RS.format("menu.help"));
		menubar.add(help);

		JMenuItem about = new JMenuItem(RS.format("menu.about"));
		help.add(about);
		about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				about();
				return;
			}
		});

		return;
	}

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * 現在せん託されているデバイスをオープンします.
	 * <p>
	*/
	private void open(){

		// 選択されているデバイスオブジェクトの参照
		TreePath path = tree.getSelectionPath();
		if(path == null){
			return;
		}
		Object value = path.getLastPathComponent();
		if(! (value instanceof Device)){
			return;
		}
		Device device = (Device)value;

		try{

			// デバイスのクローズ
			if(device.isOpen()){
				device.close();
				select();
				treeModel.changeState(path);
				return;
			}

			// デバイスのオープン
			DeviceDescriptor desc = device.getDescriptor();
			int result = JOptionPane.showConfirmDialog(this,
				RS.format("dialog.openDevice",
					Resource.getClassID().getVendor(desc.getVendorId()),
					Resource.getClassID().getProduct(desc.getVendorId(), desc.getProductId())),
				RS.format("dialog.title.warning"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(result != JOptionPane.OK_OPTION){
				return;
			}
			device.open();
			select();
			treeModel.changeState(path);

			// コントロールパネルの表示
			JDialog dialog = new ControlPanel(this, device);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

		} catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.toString(),
					RS.format("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
		} finally {
			try{
				device.close();
			} catch(USBException ex){
				ex.printStackTrace();
			}
		}
		return;
	}

	// ======================================================================
	// ノード選択の変更
	// ======================================================================
	/**
	 * ノードの選択が変更したときに呼び出されます.
	 * <p>
	*/
	private void select(){
		TreePath path = tree.getSelectionPath();
		if(path == null){
			open.setIcon(RS.getIcon("open.png"));
			open.setEnabled(false);
			return;
		}

		// 選択されているコンポーネントを参照
		Object value = path.getLastPathComponent();
		if(value instanceof java.util.List<?>){
			tableModel.setProperty(System.getProperties());
		} else if(value instanceof Bus){
			tableModel.setDescriptor(null, null);
		} else if(value instanceof Device){
			tableModel.setDescriptor((Device)value, ((Device)value).getDescriptor());
		} else if(value instanceof Configuration){
			Device device = (Device)path.getPathComponent(2);
			tableModel.setDescriptor(device, ((Configuration)value).getDescriptor());
		} else if(value instanceof Interface){
			Device device = (Device)path.getPathComponent(2);
			tableModel.setDescriptor(device, null);
		} else if(value instanceof AltSetting){
			Device device = (Device)path.getPathComponent(2);
			tableModel.setDescriptor(device, ((AltSetting)value).getDescriptor());
		} else if(value instanceof Endpoint){
			Device device = (Device)path.getPathComponent(2);
			tableModel.setDescriptor(device, ((Endpoint)value).getDescriptor());
		} else if(value instanceof Descriptor){
			Device device = (Device)path.getPathComponent(2);
			tableModel.setDescriptor(device, (Descriptor)value);
		}

		// デバイスオープンボタンの状態設定
		if(value instanceof Device){
			open.setEnabled(true);
			if(((Device)value).isOpen()){
				open.setIcon(RS.getIcon("close.png"));
			} else {
				open.setIcon(RS.getIcon("open.png"));
			}
		} else {
			open.setIcon(RS.getIcon("open.png"));
			open.setEnabled(false);
		}

		return;
	}

	// ======================================================================
	// About ダイアログの表示
	// ======================================================================
	/**
	 * About ダイアログを表示します。
	 * <p>
	*/
	private void about(){
		JPanel panel = new JPanel();

		JLabel label = new JLabel(RS.format("dialog.about.message"));
		Utils.layout(panel, label, 0, 0, 2, 1, WEST, HORIZONTAL, 0, 0, 0, 0, 0, 0);

		JSeparator sep = new JSeparator();
		sep.setOrientation(SwingConstants.HORIZONTAL);
		Utils.layout(panel, sep, 0, 1, 2, 1, WEST, HORIZONTAL, 0, 0, 0, 0, 0, 0);

		label = new JLabel(RS.format("dialog.about.user"));
		JLabel user = new JLabel(System.getProperty("user.name", ""));
		Utils.layout(panel, label, 0, 2, 1, 1, WEST, HORIZONTAL, 0, 0, 0, 0, 0, 0);
		Utils.layout(panel, user,  1, 2, 1, 1, WEST, HORIZONTAL, 1, 0, 0, 0, 0, 5);

		StringBuilder libs = new StringBuilder();
		for(String name: USBServiceManager.getLibraryNames()){
			if(libs.length() != 0){
				libs.append(", ");
			}
			libs.append(name);
		}
		label = new JLabel(RS.format("dialog.about.library"));
		JLabel lib = new JLabel(libs.toString());
		Utils.layout(panel, label, 0, 3, 1, 1, WEST, HORIZONTAL, 0, 0, 2, 0, 0, 0);
		Utils.layout(panel, lib,   1, 3, 1, 1, WEST, HORIZONTAL, 1, 0, 2, 0, 0, 5);

		JOptionPane.showMessageDialog(this, panel, RS.format("menu.about"), JOptionPane.PLAIN_MESSAGE);
		return;
	}

	// ======================================================================
	// アプリケーションの実行
	// ======================================================================
	/**
	 * アプリケーションを開始します。
	 * <p>
	 * @param args コマンドライン引数
	*/
	public static void main(String[] args){
		final Console console = new Console();
		initLog(console, Arrays.asList(args).contains("--debug"));
		LibUSB0.set_debug(5);
		LaF.init();

		// 別スレッドからコンソール出力を行うため EDT 内でオープンしないとデッドロックが発生する
		Runnable r = new Runnable(){
			public void run(){
				JFrame frame = new USBExplorer(console);
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
				return;
			}
		};
		SwingUtilities.invokeLater(r);

		return;
	}

	// ======================================================================
	// ログ出力の初期化
	// ======================================================================
	/**
	 * ログ出力を初期化します。
	 * <p>
	 * @param console ログコンソール
	 * @param debug デバッグモードの場合 true
	 */
	public static void initLog(Console console, boolean debug){
		Handler handler = console.new Handler();
		Formatter formatter = console.new Formatter();
		handler.setFormatter(formatter);

		// ルートログ出力先の設定
		Logger root = Logger.getLogger("");
		for(Handler h: root.getHandlers()){
			root.removeHandler(h);
		}
		root.addHandler(handler);
		root.setLevel(Level.INFO);

		// koiroha.org ログ出力の設定
		Logger logger = Logger.getLogger("org.koiroha.usb");
		logger.setLevel(Level.ALL);

		// デバッグモードの場合
		if(debug){
			handler = new ConsoleHandler();
			handler.setFormatter(formatter);
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
		}
		return;
	}

}
