/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Ids2XmlReader.java,v 1.4 2009/05/14 17:03:59 torao Exp $
*/
package org.koiroha.usb.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Ids2XmlReader: IDS XML 変換フィルタストリーム
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * IDS ファイルを透過的に XML 形式で読み込むためのフィルタストリームです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 17:03:59 $
 * @author takami torao
 * @since 2009/05/03 Java2 SE 5.0
 */
class Ids2XmlReader extends Reader {

	// ======================================================================
	// ベンダー行パターン
	// ======================================================================
	/**
	 * ベンダー行のパターンです。
	 * <p>
	 */
	private static final Pattern PATTERN_VENDORID = Pattern.compile("([0-9a-f]+)\\s+(.*)");

	// ======================================================================
	// クラス行パターン
	// ======================================================================
	/**
	 * ベンダー以外のクラス行のパターンです。
	 * <p>
	 */
	private static final Pattern PATTERN_CLASS    = Pattern.compile("([A-Z]+)\\s+([0-9a-f]*)\\s+(.*)");

	// ======================================================================
	// サブクラス行パターン
	// ======================================================================
	/**
	 * サブクラス行のパターンです。
	 * <p>
	 */
	private static final Pattern PATTERN_SUBCLASS = Pattern.compile("\t([0-9a-fA-F]+)\\s+(.*)");

	// ======================================================================
	// プロトコル行パターン
	// ======================================================================
	/**
	 * プロトコル行のパターンです。
	 * <p>
	 */
	private static final Pattern PATTERN_PROTOCOL = Pattern.compile("\t\t([0-9a-fA-F]+)\\s+(.*)");

	// ======================================================================
	// 言語
	// ======================================================================
	/**
	 * ISO 言語コードです。
	 * <p>
	 */
	private static final String[] LANGUAGE = Locale.getISOLanguages();

	// ======================================================================
	// 国コード
	// ======================================================================
	/**
	 * ISO 国コードです。
	 * <p>
	 */
	private static final String[] COUNTRY = Locale.getISOCountries();

	// ======================================================================
	// 入力ストリーム
	// ======================================================================
	/**
	 * IDS ファイルを読み込む入力ストリームです。
	 * <p>
	 */
	private final BufferedReader in;

	// ======================================================================
	// 内部バッファ
	// ======================================================================
	/**
	 * 次回の読み出しで返す文字列のバッファです。
	 * <p>
	 */
	private final StringBuilder buffer = new StringBuilder();

	// ======================================================================
	// EOF フラグ
	// ======================================================================
	/**
	 * EOF フラグです。
	 * <p>
	 */
	private boolean eof = false;

	// ======================================================================
	// 要素スタック
	// ======================================================================
	/**
	 * 要素の階層構造を保持するためのスタックです。
	 * <p>
	 */
	private final Stack<Entry> stack = new Stack<Entry>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param in IDS ファイルの入力ストリーム
	 */
	public Ids2XmlReader(Reader in) {
		this.in = new BufferedReader(in);
		write("<?xml version=\"1.0\"?>\n");
		write("<usbclassid xmlns=\"" + ClassID.XMLNS + "\">");
		return;
	}

	// ======================================================================
	// 文字の読み込み
	// ======================================================================
	/**
	 * 内部バッファから次の文字を読み込みます。
	 * <p>
	 * @return 読み込んだ文字
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read() throws IOException {
		if(! fill()){
			assert(buffer.length() == 0);
			return -1;
		}
		assert(buffer.length() > 0);

		// 内部バッファの先頭の文字を返す
		char ch = buffer.charAt(0);
		buffer.deleteCharAt(0);
		return ch;
	}

	// ======================================================================
	// バッファへの読み込み
	// ======================================================================
	/**
	 * 指定されたバッファへ文字列を読み込みます。
	 * <p>
	 * @param cbuf 文字列バッファ
	 * @param off バッファ内の開始位置
	 * @param len 読み込む長さ
	 * @return 実際に読み込んだ長さ
	 * @throws IOException 読み込みに失敗した場合
	*/
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if(! fill()){
			assert(buffer.length() == 0);
			return -1;
		}
		assert(buffer.length() > 0);

		// 先頭の文字をバッファに設定
		len = Math.min(buffer.length(), len);
		buffer.getChars(0, len, cbuf, off);
		buffer.delete(0, len);
		return len;
	}

	// ======================================================================
	// ストリームのクローズ
	// ======================================================================
	/**
	 * ストリームをクローズします。
	 * <p>
	 * @throws IOException クローズに失敗した場合
	*/
	@Override
	public void close() throws IOException {
		in.close();
		return;
	}

	// ======================================================================
	// データの読み込み
	// ======================================================================
	/**
	 * 内部バッファへの読み込みを行います。このメソッドが true を返した場合、内部バッファには
	 * 1 文字以上の文字が格納されています。
	 * <p>
	 * @return EOF に達した場合 false
	 * @throws IOException
	*/
	private boolean fill() throws IOException{
		if(buffer.length() > 0){
			return true;
		}

		String line = null;
		do {

			// 次の行を読み込み
			do {
				line = in.readLine();
			} while(line != null && (line.length() == 0 || line.charAt(0) == '#'));

			// EOF に達している場合
			if(line == null){
				if(eof){
					return false;
				}
				writeEndElement(0);
				write("\n</usbclassid>");
				eof = true;
				return true;
			}

		} while(! parseLine(line));

		return true;
	}

	// ======================================================================
	// 行の解析
	// ======================================================================
	/**
	 * 指定された行を解析します。行を認識できなかった場合は false を返します。
	 * <p>
	 * @param line 解析する行
	 * @return 行を認識できなかった場合 false
	*/
	private boolean parseLine(String line){

		// ベンダー ID に一致した場合
		Matcher matcher = PATTERN_VENDORID.matcher(line);
		if(matcher.matches()){
			String id = matcher.group(1);
			String label = matcher.group(2);
			writeStartElement(0, "vendor", id, label, false);
			return true;
		}

		// ベンダー ID 以外のクラス定義に一致した場合
		matcher = PATTERN_CLASS.matcher(line);
		if(matcher.matches()){
			String kind = matcher.group(1);
			String id = matcher.group(2);
			String label = matcher.group(3);
			if(kind.equals("C")){
				writeStartElement(0, "class", id, label, false);
				return true;
			}
			if(kind.equals("L")){
				String locale = getLocale(label);
				writeStartElement(0, "lang", id, label, locale, false);
				return true;
			}
			return false;
		}

		// サブクラス ID に一致した場合
		matcher = PATTERN_SUBCLASS.matcher(line);
		if(matcher.matches() && ! stack.isEmpty()){
			String id = matcher.group(1);
			String label = matcher.group(2);
			if(stack.peek().name.equals("vendor")){
				writeStartElement(1, "product", id, label, false);
				return true;
			}
			if(stack.peek().name.equals("class")){
				writeStartElement(1, "subclass", id, label, false);
				return true;
			}
			if(stack.peek().name.equals("lang")){
				String locale = getLocale(stack.peek().label, label);
				writeStartElement(1, "dialect", id, label, locale, false);
				return true;
			}
			return false;
		}

		// プロトコル ID に一致した場合
		matcher = PATTERN_PROTOCOL.matcher(line);
		if(matcher.matches() && ! stack.isEmpty()){
			String id = matcher.group(1);
			String label = matcher.group(2);
			if(stack.peek().name.equals("productr")){
				writeStartElement(2, "interface", id, label, false);
				return true;
			}
			if(stack.peek().name.equals("subclass")){
				writeStartElement(2, "protocol", id, label, false);
				return true;
			}
			return false;
		}

		return false;
	}

	// ======================================================================
	// 内部バッファへの追加
	// ======================================================================
	/**
	 * 指定された文字列を内部バッファに追加します。
	 * <p>
	 * @param text 内部バッファに追加する文字列
	*/
	private void write(String text){
		buffer.append(text);
		return;
	}

	// ======================================================================
	// 要素の開始
	// ======================================================================
	/**
	 * 指定された要素を出力します。
	 * <p>
	 * @param depth 階層のインデックス (0 以上)
	 * @param tagName 開始する要素名
	 * @param id ID
	 * @param label ラベル
	 * @param empty 空要素を出力する場合 true
	*/
	private void writeStartElement(int depth, String tagName, String id, String label, boolean empty){
		writeStartElement(depth, tagName, id, label, null, empty);
		return;
	}

	// ======================================================================
	// 要素の開始
	// ======================================================================
	/**
	 * 指定された要素を出力します。
	 * <p>
	 * @param depth 階層のインデックス (0 以上)
	 * @param tagName 開始する要素名
	 * @param id ID
	 * @param label ラベル
	 * @param locale ロケール
	 * @param empty 空要素を出力する場合 true
	*/
	private void writeStartElement(int depth, String tagName, String id, String label, String locale, boolean empty){

		// スタックを使用して要素を閉じる
		writeEndElement(depth);

		if(stack.size() > 0){
			if(stack.peek().child == 0){
				write(">");
			}
			stack.peek().child ++;
		}
		if(! empty){
			stack.push(new Entry(tagName, id, label));
		}

		// 要素の出力
		id = escape(id);
		label = escape(label);
		write("\n");
		for(int i=0; i<=depth; i++){
			write("\t");
		}
		write("<" + tagName + " id=\"" + id.toUpperCase() + "\" label=\"" + label + "\"");
		if(locale != null){
			write(" locale=\"" + locale + "\"");
		}
		if(empty){
			write("/>");
		}
		return;
	}

	// ======================================================================
	// 要素の終了
	// ======================================================================
	/**
	 * 指定された深さまで要素を終了します。
	 * <p>
	 * @param depth 階層のインデックス (0 以上)
	*/
	private void writeEndElement(int depth){

		// スタックを使用して要素を閉じる
		assert(stack.size() >= depth);
		while(stack.size() > depth){
			Entry entry = stack.pop();
			if(entry.child == 0){
				write("/>");
			} else {
				write("\n");
				for(int i=0; i<=stack.size(); i++){
					write("\t");
				}
				write("</" + entry.name + ">");
			}
		}
		return;
	}

	// ======================================================================
	// ロケールの参照
	// ======================================================================
	/**
	 * 指定されたラベルを表示名に持つロケールを参照します。
	 * <p>
	 * @param label ラベル
	 * @return ロケールの文字列
	*/
	private static String getLocale(String label){
		label = label.trim();
		for(int i=0; i<LANGUAGE.length; i++){
			Locale l = new Locale(LANGUAGE[i]);
			if(l.getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(label)){
				return l.getLanguage();
			}
			if(l.getDisplayLanguage(l).equalsIgnoreCase(label)){
				return l.getLanguage();
			}
		}
		return null;
	}

	// ======================================================================
	// ロケールの参照
	// ======================================================================
	/**
	 * 指定された言語と国ラベルを持つロケールを参照します。
	 * <p>
	 * @param lang 言語
	 * @param cont 国
	 * @return ロケールの文字列
	*/
	private static String getLocale(String lang, String cont){
		String locale = getLocale(lang);
		if(locale == null){
			return null;
		}
		for(int i=0; i<COUNTRY.length; i++){
			Locale l = new Locale(locale, COUNTRY[i]);
			if(l.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(cont)){
				return locale + "_" + l.getCountry();
			}
			if(l.getDisplayCountry(l).equalsIgnoreCase(cont)){
				return locale + "_" + l.getCountry();
			}
		}
		return locale;
	}

	// ======================================================================
	// 文字列の XML エスケープ
	// ======================================================================
	/**
	 * 指定された文字列を XML エスケープして返します。
	 * <p>
	 * @param text エスケープする文字列
	 * @return エスケープした文字列
	*/
	private static String escape(String text){
		StringBuilder buffer = new StringBuilder();
		for(int i=0; i<text.length(); i++){
			char ch = text.charAt(i);
			switch(ch){
			case '<':	buffer.append("&lt;");	break;
			case '>':	buffer.append("&gt;");	break;
			case '&':	buffer.append("&amp;");	break;
			case '\"':	buffer.append("&quot;");	break;
			default:
				if(! Character.isDefined(ch)){
					buffer.append("&#" + ((int)ch) + ";");
				} else {
					buffer.append(ch);
				}
				break;
			}
		}
		return buffer.toString();
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Entry: エントリ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * スタックに保存されるエントリです。
	 * <p>
	 */
	private static class Entry{
		/** 要素名です。 */
		public final String name;
		/** ID です。*/
		public final String id;
		/** ラベル */
		public final String label;
		/** 子要素の数です。 */
		public int child = 0;
		/**
		 * 要素名を指定して構築を行います。
		 * @param name 要素名
		 */
		public Entry(String name){
			this.name = name;
			this.id = null;
			this.label = null;
			return;
		}
		/**
		 * 要素名を指定して構築を行います。
		 * @param name 要素名
		 * @param id ID
		 * @param label ラベル
		 */
		public Entry(String name, String id, String label){
			this.name = name;
			this.id = id;
			this.label = label;
			return;
		}
	}

}
