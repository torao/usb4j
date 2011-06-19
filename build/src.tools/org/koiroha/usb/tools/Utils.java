/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Utils.java,v 1.2 2009/05/14 02:22:34 torao Exp $
*/
package org.koiroha.usb.tools;

import java.awt.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Utils: ユーティリティクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * ツール用のユーティリティクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.2 $ $Date: 2009/05/14 02:22:34 $
 * @author takami torao
 * @since 2009/05/04 Java2 SE 5.0
 */
public class Utils {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	private Utils() {
		return;
	}

	// ======================================================================
	// コンポーネントのレイアウト
	// ======================================================================
	/**
	 * 指定されたコンポーネントを {@link GridBagLayout} を使用して配置します。
	 * <p>
	 * @param parent 親コンポーネント
	 * @param cmp 配置するコンポーネント
	 * @param x X位置
	 * @param y Y位置
	 * @param w グリッド幅
	 * @param h グリッド高さ
	 * @param anchor アンカー
	 * @param fill フィル
	 * @param wx 横方向ウェイト
	 * @param wy 縦方向ウェイト
	 */
	public static void layout(Container parent, Component cmp, int x, int y, int w, int h, int anchor, int fill, double wx, double wy){
		layout(parent, cmp, x, y, w, h, anchor, fill, wx, wy, 4, 4, 4, 4);
		return;
	}

	// ======================================================================
	// コンポーネントのレイアウト
	// ======================================================================
	/**
	 * 指定されたコンポーネントを {@link GridBagLayout} を使用して配置します。
	 * <p>
	 * @param parent 親コンポーネント
	 * @param cmp 配置するコンポーネント
	 * @param x X位置
	 * @param y Y位置
	 * @param w グリッド幅
	 * @param h グリッド高さ
	 * @param anchor アンカー
	 * @param fill フィル
	 * @param wx 横方向ウェイト
	 * @param wy 縦方向ウェイト
	 * @param pt 上パディング
	 * @param pr 右パディング
	 * @param pb 下パディング
	 * @param pl 左パディング
	 */
	public static void layout(Container parent, Component cmp, int x, int y, int w, int h, int anchor, int fill, double wx, double wy, int pt, int pr, int pb, int pl){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;		c.gridy = y;
		c.gridwidth = w;	c.gridheight = h;
		c.anchor = anchor;	c.fill = fill;
		c.weightx = wx;		c.weighty = wy;
		c.insets = new Insets(pt, pl, pb, pr);
		if(! (parent.getLayout() instanceof GridBagLayout)){
			parent.setLayout(new GridBagLayout());
		}
		parent.add(cmp, c);
		return;
	}

}
