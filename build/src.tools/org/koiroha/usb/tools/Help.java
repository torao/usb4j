/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Help.java,v 1.2 2009/05/14 02:22:34 torao Exp $
*/
package org.koiroha.usb.tools;

import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Help: ヘルプ表示コンポーネント
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * ヘルプ表示用のコンポーネントです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.2 $ $Date: 2009/05/14 02:22:34 $
 * @author takami torao
 * @since 2009/05/04 Java2 SE 5.0
 */
public class Help extends AbstractAction{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ヘルプリソース
	// ======================================================================
	/**
	 * ヘルプリソースです。
	 * <p>
	 */
	private final Resource rs = new Resource("org.koiroha.usb.tools.help");

	// ======================================================================
	// 親コンポーネント
	// ======================================================================
	/**
	 * 親コンポーネントです。
	 * <p>
	 */
	private final JFrame parent;

	// ======================================================================
	// ヘルプ表示ペイン
	// ======================================================================
	/**
	 * ヘルプ表示領域です。
	 * <p>
	 */
	private final JTextPane help = new JTextPane();

	// ======================================================================
	// ダイアログ
	// ======================================================================
	/**
	 * ヘルプ表示用のダイアログです。
	 * <p>
	 */
	private JDialog dialog = null;

	// ======================================================================
	// ダイアログ
	// ======================================================================
	/**
	 * ヘルプ表示用のダイアログです。
	 * <p>
	 */
	private final String fileName;

	// ======================================================================
	// ダイアログ
	// ======================================================================
	/**
	 * ヘルプ表示用のダイアログです。
	 * <p>
	 */
	private final Stack<URL> history = new Stack<URL>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param parent 親コンポーネント
	 * @param fileName ファイル名
	 */
	public Help(JFrame parent, String fileName) {
		this.parent = parent;
		this.fileName = fileName;
		help.setEditable(false);
		putValue(Action.NAME, rs.getString("menu.title", "Help"));
		putValue(Action.SMALL_ICON, rs.getIcon("help.png"));

		help.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_F5){
					URL url = help.getPage();
					Document doc = help.getDocument();
					doc.putProperty(Document.StreamDescriptionProperty, null);
					setURL(url);
				} else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE || (e.getKeyCode() == KeyEvent.VK_LEFT && e.isAltDown())){
					if(! history.isEmpty()){
						URL url = history.pop();
						setURL(url);
					}
				}
			}
		});
		help.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
					URL url = e.getURL();
					setURL(url);
				}
				return;
			}
		});
		return;
	}

	// ======================================================================
	// 処理の実行
	// ======================================================================
	/**
	 * ヘルプダイアログを表示します。
	 * <p>
	 * @param e アクションイベント
	*/
	public void actionPerformed(ActionEvent e) {
		if(dialog == null){
			JOptionPane pane = new JOptionPane();
			pane.setMessageType(JOptionPane.PLAIN_MESSAGE);
			pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			pane.setMessage(new JScrollPane(help));
			dialog = pane.createDialog(parent, rs.getString("help.title", "Help"));
			dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			dialog.setModal(false);
			dialog.setResizable(true);
		}
		URL url = rs.getLocaleResource(fileName);
		setURL(url);
		dialog.setVisible(true);
		return;
	}

	// ======================================================================
	// URL の設定
	// ======================================================================
	/**
	 * URL を設定します。
	 * <p>
	 * @param url URL
	*/
	private void setURL(URL url){
		try {
			help.setPage(url);
		} catch (IOException ex) {
			help.setText(ex.toString());
			ex.printStackTrace();
		}
		if(history.isEmpty() || ! history.peek().equals(url)){
			history.push(url);
		}
		return;
	}

}
