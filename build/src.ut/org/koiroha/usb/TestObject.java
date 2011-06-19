/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: TestObject.java,v 1.4 2009/05/14 02:22:35 torao Exp $
*/
package org.koiroha.usb;

import java.io.*;
import java.util.Date;
import java.util.logging.*;

import org.junit.BeforeClass;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// TestObject:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 02:22:35 $
 * @author torao
 * @since 2009/04/25 Java2 SE 5.0
 */
public class TestObject {

	// ======================================================================
	// ログ出力の初期化
	// ======================================================================
	/**
	 * ログ出力を初期化します。
	 * <p>
	 */
	@BeforeClass
	public static void initLog(){

		// ログ出力の設定
		Logger logger = Logger.getLogger("");
		for(Handler h: logger.getHandlers()){
			logger.removeHandler(h);
		}
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter(){
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
				buffer.append(String.format("[%1$tY-%1$tm-%1$td %1$tT] %2$7s %3$-" + len + "s - %4$s%n",
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
		});
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);
		handler.setLevel(Level.ALL);
		return;
	}

}
