/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Console.java,v 1.7 2009/05/18 11:02:19 torao Exp $
*/
package org.koiroha.usb.tools;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Console: コンソールコンポーネント
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Logging API の出力内容を表示するためのコンポーネントです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/18 11:02:19 $
 * @author torao
 * @since 2009/05/03 Java2 SE 5.0
 */
public class Console extends JPanel {

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このパッケージのリソースです。
	 * <p>
	*/
	private static final Resource RS = new Resource(Console.class);

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 出力要素バッファ
	// ======================================================================
	/**
	 * ログ出力要素の文字列を格納するためのバッファです。
	 * <p>
	 */
	private String[] lines = new String[1000];

	// ======================================================================
	// バッファ格納位置
	// ======================================================================
	/**
	 * バッファ内で次のログを格納する位置です。
	 * <p>
	 */
	private int current = 0;

	// ======================================================================
	// 更新処理実行スレッド
	// ======================================================================
	/**
	 * バッファ内のログからコンポーネントを更新するための実行インスタンスです。
	 * <p>
	 */
	private Runnable exec = null;

	// ======================================================================
	// 更新処理実行スレッド
	// ======================================================================
	/**
	 * バッファ内のログからコンポーネントを更新するための実行インスタンスです。
	 * <p>
	 */
	private final JTextArea console = new JTextArea();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public Console() {

		// ツールバーの構築
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		JButton clear = new JButton();
		clear.setIcon(RS.getIcon("clear-normal.png"));
		clear.setRolloverIcon(RS.getIcon("clear-hover.png"));
		clear.setToolTipText(RS.getString("button.clear.tooltip"));
		toolbar.add(clear);

		// コンソールの初期化
		console.setEditable(false);
		console.setFont(new Font("monospaced", Font.PLAIN, 12));

		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, toolbar);
		this.add(BorderLayout.CENTER, new JScrollPane(console));

		clear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				synchronized(console){
					current = 0;
					console.setText("");
					return;
				}
			}
		});
		return;
	}

	// ======================================================================
	// ログの追加
	// ======================================================================
	/**
	 * 指定されたログを内部バッファに追加します。コンポーネントの更新は行われません。
	 * <p>
	 * @param log ログ
	 */
	private void store(String log) {
		synchronized(this){
			if(current == lines.length){
				System.arraycopy(lines, 1, lines, 0, current-1);
				lines[lines.length-1] = log;
			} else {
				lines[current] = log;
				current ++;
			}
		}
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	private void flush() {

		// 既に表示処理が起動している場合は何もしない
		if(exec != null){
			return;
		}

		// コンソールの更新処理
		exec = new Updator();

		// EDT 内で実行されている場合はこのまま出力
		if(SwingUtilities.isEventDispatchThread()){
			exec.run();
		} else {
			SwingUtilities.invokeLater(exec);
		}
		return;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Updator: 表示更新処理
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 内部バッファで表示を更新する処理クラスです。
	 * <p>
	 */
	private class Updator implements Runnable{

		// ==================================================================
		// ログ表示の更新
		// ==================================================================
		/**
		 * ログを更新します。
		 * <p>
		*/
		public void run(){

			// 内部バッファから文字列を構築
			StringBuilder buffer = new StringBuilder();
			for(int i=0; i<current; i++){
				buffer.append(lines[i]);
			}

			// ※setText() は時間がかかるためここで null を設定しておく
			exec = null;

			// 表示の更新
			synchronized(console){
				console.setText(buffer.toString());
			}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Handler: ログハンドラ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * Logging API のログ出力をこのコンソールに表示するためのハンドラです。
	 * <p>
	 */
	public class Handler extends java.util.logging.Handler{

		// ==================================================================
		// ログの出力
		// ==================================================================
		/**
		 * 指定されたログを出力します。
		 * <p>
		 * @param record 出力するログ
		*/
		@Override
		public void publish(LogRecord record) {
			store(getFormatter().format(record));
			Console.this.flush();
			return;
		}

		// ==================================================================
		// フラッシュ
		// ==================================================================
		/**
		 * バッファ内のログを表示します。
		 * <p>
		*/
		@Override
		public void flush() {
			Console.this.flush();
			return;
		}

		// ==================================================================
		// クローズ
		// ==================================================================
		/**
		 * 何も行いません。
		 * <p>
		*/
		@Override
		public void close(){
			return;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Formatter: ログフォーマッター
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * Logging API のログフォーマットを行うクラスです。
	 * <p>
	 */
	public class Formatter extends java.util.logging.Formatter{

		// ==================================================================
		// クローズ
		// ==================================================================
		/**
		 * 何も行いません。
		 * <p>
		*/
		@Override
		public String format(LogRecord record) {

			final int len = 12;
			String name = record.getLoggerName();
			int sep = name.lastIndexOf('.');
			if(sep >= 0){
				name = name.substring(sep + 1);
			}
			if(name.length() > len){
				name = name.substring(0, len);
			}

			StringBuilder buffer = new StringBuilder();
			buffer.append(String.format("[%1$tY/%1$tm/%1$td %1$tT] %2$7s %3$-" + len + "s - %4$s%n",
				new Date(record.getMillis()), record.getLevel().getName(), name, record.getMessage()));
			if(record.getThrown() != null){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.flush();
				buffer.append(sw);
			}
			return buffer.toString();
		}

	}

}
