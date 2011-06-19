/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Ids2Xml.java,v 1.5 2009/05/14 02:36:36 torao Exp $
*/
package org.koiroha.usb.tools.ids2xml;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import org.koiroha.usb.util.ClassID;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// IDS2XML: IDS 変換クラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * IDS ファイルを {@link ClassID} クラスで読み込み可能な形式に変換するためのツールです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.5 $ $Date: 2009/05/14 02:36:36 $
 * @author takami torao
 * @since 2009/05/03 Java2 SE 5.0
 */
public class Ids2Xml {

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	private Ids2Xml() {
		return;
	}

	// ======================================================================
	// 変換処理の実行
	// ======================================================================
	/**
	 * 変換処理を実行します。
	 * <p>
	 * @param in IDS ファイルの入力ストリーム
	 * @param out XML ファイルの出力ストリーム
	 * @throws IOException 読み込みまたは書き込みに失敗した場合
	 */
	public static void convert(Reader in, Writer out) throws IOException{
		char[] buffer = new char[1024];
		while(true){
			int len = in.read(buffer);
			if(len < 0){
				break;
			}
			out.write(buffer, 0, len);
		}
		return;
	}

	// ======================================================================
	// アプリケーションの実行
	// ======================================================================
	/**
	 * アプリケーションを実行します。
	 * <p>
	 * @param args コマンドライン引数
	 * @throws IOException 読み込みまたは書き込みに失敗した場合
	 */
	public static void main(String[] args) throws IOException{
		Charset charset = Charset.defaultCharset();
		URL url = new URL("http://www.linux-usb.org/usb.ids");
		Writer out = new OutputStreamWriter(System.out);

		// コマンドライン引数の解析
		// TODO

		Reader in = ClassID.getIDSToClassIDStream(new InputStreamReader(url.openStream(), charset));
		convert(in, out);
		out.flush();
		in.close();
		return;
	}

}
