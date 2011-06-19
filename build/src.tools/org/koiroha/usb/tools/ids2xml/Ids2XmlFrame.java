/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Ids2XmlFrame.java,v 1.5 2009/05/14 02:36:36 torao Exp $
*/
package org.koiroha.usb.tools.ids2xml;

import static java.awt.GridBagConstraints.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.*;

import org.koiroha.usb.tools.*;
import org.koiroha.usb.util.ClassID;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// IDS2XML: IDS 変換クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * IDS ファイルを {@link ClassID} クラスで読み込み可能な XML 形式に変換するためのツールです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.5 $ $Date: 2009/05/14 02:36:36 $
 * @author takami torao
 * @since 2009/05/03 Java2 SE 5.0
 */
public class Ids2XmlFrame extends JFrame{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 入力 IDS ファイル URL
	// ======================================================================
	/**
	 * 変換元の IDS ファイル URL です。
	 * <p>
	 */
	private final JTextField ids = new JTextField();

	// ======================================================================
	// IDS ファイルの内容
	// ======================================================================
	/**
	 * IDS ファイルの内容です。
	 * <p>
	 */
	private final JTextArea idsFile = new JTextArea();

	// ======================================================================
	// XML ファイルの内容
	// ======================================================================
	/**
	 * XML ファイルの内容です。
	 * <p>
	 */
	private final JTextArea cidFile = new JTextArea();

	// ======================================================================
	// 実行ボタン
	// ======================================================================
	/**
	 * 実行ボタンです。
	 * <p>
	 */
	private final JButton exec = new JButton("OK");

	// ======================================================================
	// 保存ボタン
	// ======================================================================
	/**
	 * 保存ボタンです。
	 * <p>
	 */
	private final JButton save = new JButton("Save");

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public Ids2XmlFrame() {
		setTitle("IDS to ClassID XML Converter");
		JPanel panel = new JPanel();
		Utils.layout(this.getContentPane(), panel, 0, 0, 1, 1, CENTER, BOTH, 1, 1, 5, 5, 5, 5);

		// IDS ファイルの URL
		JLabel label = new JLabel("USB ID's URL");
		JButton idsbtn = new JButton("Local File...");
		ids.setText("http://www.linux-usb.org/usb.ids");
		Utils.layout(panel, label,  0, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, ids,    1, 0, 1, 1, CENTER, HORIZONTAL, 1, 0);
		Utils.layout(panel, idsbtn, 2, 0, 1, 1, EAST, NONE, 0, 0);

		// IDS ファイルの内容表示
		JPanel idsp = new JPanel();
		idsFile.setEditable(false);
		idsFile.setTabSize(4);
		Utils.layout(idsp, new JLabel("IDS File"), 0, 0, 1, 1, CENTER, BOTH, 0, 0, 0, 0, 2, 0);
		Utils.layout(idsp, new JScrollPane(idsFile), 0, 1, 1, 1, CENTER, BOTH, 1, 1, 0, 0, 0, 0);

		// ClassID XML ファイルの内容表示
		JPanel cidp = new JPanel();
		cidFile.setEditable(false);
		cidFile.setTabSize(4);
		Utils.layout(cidp, new JLabel("ClassID XML File"), 2, 0, 1, 1, CENTER, BOTH, 0, 0, 0, 0, 2, 0);
		Utils.layout(cidp, new JScrollPane(cidFile), 2, 1, 1, 1, CENTER, BOTH, 1, 1, 0, 0, 0, 0);

		// ファイル内容表示の分割ペイン
		JSplitPane preview = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		preview.setDividerLocation(0.5);
		preview.setResizeWeight(0.5);
		preview.setOneTouchExpandable(true);
		preview.setLeftComponent(idsp);
		preview.setRightComponent(cidp);
		Utils.layout(panel, preview, 0, 1, 3, 1, CENTER, BOTH, 1, 1);

		// ヘルプボタン
		Help help = new Help(this, "ids2xml.html");
		JButton helpbtn = new JButton(help);

		// 保存ボタン
		JPanel button = new JPanel();
		save.setEnabled(false);
		Utils.layout(button, exec, 0, 0, 1, 1, CENTER, NONE, 0, 0);
		Utils.layout(button, save, 1, 0, 1, 1, CENTER, NONE, 0, 0);
		Utils.layout(button, helpbtn, 2, 0, 1, 1, CENTER, NONE, 0, 0);
		Utils.layout(panel, button, 0, 3, 3, 1, CENTER, NONE, 0, 0);

		// 表示位置の設定
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(640, 400);
		this.setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);

		idsbtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				File file = selectFile(false);
				if(file != null){
					try{
						ids.setText(file.toURI().toURL().toString());
					} catch(IOException ex){/* */}
				}
				return;
			}
		});

		cidFile.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				updateState();
			}
			public void insertUpdate(DocumentEvent e) {
				updateState();
			}
			public void removeUpdate(DocumentEvent e) {
				updateState();
			}
		});

		exec.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				idsFile.setText("");
				cidFile.setText("");
				load();
				return;
			}
		});

		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				save();
				return;
			}
		});

		return;
	}

	// ======================================================================
	// 結果の保存
	// ======================================================================
	/**
	 * 変換結果を保存します。
	 * <p>
	 */
	private void load(){
		try{

			// IDS ファイルの読み込み
			URL url = new URL(ids.getText());
			InputStream is = url.openStream();
			Reader in = new InputStreamReader(is, "UTF-8");
			final String ids = read(in);

			// ClassID XML に変換
			in = ClassID.getIDSToClassIDStream(new StringReader(ids));
			final String cid = read(in);

			// EDT 内でテキスト更新処理
			Runnable r = new Runnable(){
				public void run() {
					idsFile.setText(ids);
					idsFile.setCaretPosition(0);
					cidFile.setText(cid);
					cidFile.setCaretPosition(0);
					return;
				}
			};
			SwingUtilities.invokeLater(r);

		} catch(IOException ex){
			idsFile.setText(ex.toString());
		}
		return;
	}

	// ======================================================================
	// 文字列の読み込み
	// ======================================================================
	/**
	 * 指定された入力ストリームから文字列を読み込みます。
	 * <p>
	 * @param in 入力ストリーム
	 * @return 読み込んだ文字列
	 * @throws IOException 読み込みに失敗した場合
	 */
	private String read(Reader in) throws IOException{
		StringBuilder buffer = new StringBuilder();
		char[] buf = new char[1024];
		while(true){
			int len = in.read(buf);
			if(len < 0)	break;
			buffer.append(buf, 0, len);
		}
		return buffer.toString();
	}

	// ======================================================================
	// 結果の保存
	// ======================================================================
	/**
	 * 変換結果を保存します。
	 * <p>
	 */
	private void save(){
		String content = cidFile.getText();
		if(content.length() == 0){
			return;
		}
		File file = selectFile(true);
		if(file == null){
			return;
		}

		// 変換内容の出力
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
			Writer out = new OutputStreamWriter(os, "UTF-8");
			out.write(content);
			out.flush();
		} catch(IOException ex){
			JOptionPane.showMessageDialog(this, ex.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
		} finally {
			try{
				if(os != null)	os.close();
			} catch(IOException ex){/* */}
		}
		return;
	}

	// ======================================================================
	// ファイルの選択
	// ======================================================================
	/**
	 * ファイルを選択します。
	 * <p>
	 * @param save 保存用の場合 true
	 * @return 選択されたファイル
	 */
	private File selectFile(boolean save){
		Preferences pref = Preferences.userNodeForPackage(Ids2XmlFrame.class);
		JFileChooser dialog = new JFileChooser();
		dialog.setCurrentDirectory(new File(pref.get("dir", ".")));
		int result = -1;
		if(save){
			result = dialog.showSaveDialog(this);
		} else {
			result = dialog.showOpenDialog(this);
		}
		if(result != JFileChooser.APPROVE_OPTION){
			return null;
		}
		File file = dialog.getSelectedFile();
		pref.put("dir", file.getParent());
		try{
			pref.flush();
		} catch(Exception ex){ ex.printStackTrace(); }
		return file;
	}

	// ======================================================================
	// 状態の更新
	// ======================================================================
	/**
	 * コンポーネントの表示状態を更新します。
	 * <p>
	 */
	private void updateState(){
		save.setEnabled(cidFile.getText().length() > 0);
		return;
	}

	// ======================================================================
	// アプリケーションの実行
	// ======================================================================
	/**
	 * アプリケーションを実行します。
	 * <p>
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		Runnable r = new Runnable(){
			public void run(){
				JFrame frame = new Ids2XmlFrame();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
				return;
			}
		};
		SwingUtilities.invokeLater(r);
		return;
	}

}
