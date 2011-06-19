/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LaF.java,v 1.4 2009/05/14 02:22:34 torao Exp $
*/
package org.koiroha.usb.tools.explorer;

import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.koiroha.usb.tools.Resource;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LaF: Look & Feel
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * GUI の Look & Feel を管理するためのクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 02:22:34 $
 * @author torao
 * @since 2009/05/02 Java2 SE 5.0
 */
public final class LaF {

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このパッケージのリソースです。
	 * <p>
	*/
	private static final Resource RS = new Resource(DeviceTreeModel.class);

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	private LaF() {
		return;
	}

	// ======================================================================
	// ポップアップメニューの参照
	// ======================================================================
	/**
	 * ポップアップメニューを参照します。
	 * <p>
	 * @param frame フレーム
	 * @return ポップアップメニュー
	 */
	public static JPopupMenu getPopupMenu(final USBExplorer frame){
		final Preferences pref = Preferences.userNodeForPackage(LaF.class);

		// フレームデコレーションの状態を参照
		boolean deco = pref.getBoolean("laf.decoration", false);

		// 使用可能なすべての Look & Feel をリスト表示
		JPopupMenu menu = new JPopupMenu();
		UIManager.LookAndFeelInfo[] list = UIManager.getInstalledLookAndFeels() ;
		ButtonGroup group = new ButtonGroup();
		boolean currentSupportDecoration = false;
		for(int i=0; i<list.length; i++){

			// Look & Feel のインスタンスを参照
			LookAndFeel laf = null;
			try{
				laf = (LookAndFeel)Class.forName(list[i].getClassName()).newInstance();
			} catch(Exception ex){
				ex.printStackTrace();
				continue;
			}

			// メニューの構築
			JRadioButtonMenuItem item = new JRadioButtonMenuItem();
			item.setText(list[i].getName());		// LookAndFeelInfoの名前のほうが若干詳細
			item.setToolTipText(laf.getDescription());
			group.add(item);
			menu.add(item);

			// 現在選択されている Look & Feel はチェックを付ける
			if(UIManager.getLookAndFeel() != null && UIManager.getLookAndFeel().getClass().getName().equals(list[i].getClassName())){
				item.setSelected(true);
				currentSupportDecoration = laf.getSupportsWindowDecorations();

			// 現在フレームデコレーションが有効になっているがこの L&F がデコレーションに対応していない場合
			} else if(deco && ! laf.getSupportsWindowDecorations()){
				item.setEnabled(false);

			// それ以外の場合は選択時に L&F を変更するアクションを設定
			} else {
				final String className = list[i].getClassName();
				item.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						changeLookAndFeel(frame, className);
						return;
					}
				});
			}
		}
		menu.addSeparator();

		// フレームデコレーション
		final JCheckBoxMenuItem decoration = new JCheckBoxMenuItem(RS.format("menu.frameDecoration"));
		decoration.setState(deco);
		decoration.setEnabled(currentSupportDecoration);
		decoration.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				boolean deco = decoration.getState();
				pref.putBoolean("laf.decoration", deco);
				try{
					pref.flush();
				} catch(Exception ex){
					ex.printStackTrace();
				}

				frame.dispose();
				frame.setUndecorated(deco);
				if(deco){
					frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
				} else {
					frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
				}
				frame.setVisible(true);
				return;
			}
		});
		menu.add(decoration);

		return menu;
	}

	// ======================================================================
	// Look and Feel の設定
	// ======================================================================
	/**
	 * システムの Look & Feel を設定します。
	 * <p>
	 */
	public static void init(){
		Preferences pref = Preferences.userNodeForPackage(LaF.class);
		String className = pref.get("laf.className", null);
		if(className != null){
			try {
				UIManager.setLookAndFeel(className);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		boolean decoration = pref.getBoolean("laf.decoration", false);
		JFrame.setDefaultLookAndFeelDecorated(decoration);
		JDialog.setDefaultLookAndFeelDecorated(decoration);
		return;
	}

	// ======================================================================
	// Look & Feel の変更
	// ======================================================================
	/**
	 * Look & Feel を変更します。
	 * <p>
	 * @param frame フレーム
	 * @param className Look & Feel 実装クラス名
	 */
	private static void changeLookAndFeel(USBExplorer frame, String className){
		try {
			UIManager.setLookAndFeel(className);
			SwingUtilities.updateComponentTreeUI(frame);
			frame.tree.setCellRenderer(new DeviceTreeModel.Renderer());
			frame.table.setDefaultRenderer(Object.class, new PropertyTableModel.Renderer());
			Preferences pref = Preferences.userNodeForPackage(LaF.class);
			pref.put("laf.className", className);
			pref.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return;
	}

}
