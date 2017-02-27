/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.pentaho.di.sdk.samples.steps.ruby;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

class ScriptValuesHighlight implements LineStyleListener {

	JavaScanner scanner = new JavaScanner();
	int[] tokenColors;
	Color[] colors;

	public static final int EOF = -1;
	public static final int EOL = 10;

	final int COLOR_BLACK = 0;
	final int COLOR_GREENISH = 1;
	final int COLOR_BLUE = 2;
	final int COLOR_ORANGE = 3;
	final int COLOR_RED = 4;
	final int COLOR_GREEN = 5;
	final int COLOR_GRAY = 6;
	final int COLOR_SKY = 7;

	final int TOKEN_DEFAULT = 0;
	final int TOKEN_STRING = 1;
	final int TOKEN_SYMBOL = 2;
	final int TOKEN_KEYWORD = 3;
	final int TOKEN_GLOBAL_FUNCTION = 4;
	final int TOKEN_STANDARD_METHOD = 5;
	final int TOKEN_GLOBAL_VARIABLE = 6;
	final int TOKEN_COMMENT = 7;
	final int TOKEN_CONSTANT = 8;
	final int TOKEN_VARIABLE = 9;

	public static final int MAXIMUM_TOKEN = 10;

	public ScriptValuesHighlight() {
		initializeColors();
		scanner = new JavaScanner();
	}

	Color getColor( int type ) {
		if ( type < 0 || type >= tokenColors.length ) {
			return null;
		}
		return colors[tokenColors[type]];
	}

	void initializeColors() {
		Display display = Display.getDefault();
		colors = new Color[] {
				new Color(display, new RGB(0, 0, 0)), 		// black
				new Color(display, new RGB(63, 127, 95)), 	// Greenish 
				new Color(display, new RGB(0, 0, 192)), 	// Blue
				new Color(display, new RGB(255, 102, 0)), 	// Orange	
				new Color(display, new RGB(225, 0, 0)), 	// Red
				new Color(display, new RGB(0, 128, 0)), 	// Green
				new Color(display, new RGB(128, 128, 128)), // Gray
				new Color(display, new RGB(0,191,255)),		// Sky Blue
				new Color(display, new RGB(127, 0, 85)) 	// -- not used --
		};
	}

	void disposeColors() {
		for ( int i = 0; i < colors.length; i++ ) {
			colors[i].dispose();
		}
	}

	StyleRange createStyleRange(int style, int start, int end){
		switch(style){
		case TOKEN_DEFAULT:
		default:
			return new StyleRange(start, end, colors[COLOR_BLACK], null, SWT.NORMAL);
		case TOKEN_STRING:
			return new StyleRange(start, end, colors[COLOR_GREEN], null, SWT.ITALIC);
		case TOKEN_SYMBOL:
			return new StyleRange(start, end, colors[COLOR_GREEN], null, SWT.NORMAL);
		case TOKEN_KEYWORD:
			return new StyleRange(start, end, colors[COLOR_BLUE], null, SWT.NORMAL);
		case TOKEN_GLOBAL_VARIABLE:
			return new StyleRange(start, end, colors[COLOR_GRAY], null, SWT.NORMAL);
		case TOKEN_COMMENT:
			return new StyleRange(start, end, colors[COLOR_GREENISH], null, SWT.ITALIC);
		case TOKEN_CONSTANT:
			return new StyleRange(start, end, colors[COLOR_GREEN], null, SWT.NORMAL);
		case TOKEN_VARIABLE:
			return new StyleRange(start, end, colors[COLOR_BLACK], null, SWT.NORMAL);
		}
	}


	/**
	 * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles Enumeration of
	 * StyleRanges, need to be in order. (output) LineStyleEvent.background line background color (output)
	 */
	public void lineGetStyle( LineStyleEvent event ) {
		Vector<StyleRange> styles = new Vector<StyleRange>();
		int token;
		int start;
		StyleRange lastStyle;

		scanner.setRange( event.lineText );
		token = scanner.nextToken();
		while ( token != EOF ) {
			if(!styles.isEmpty()){
				lastStyle = styles.lastElement();
				lastStyle.length += scanner.getLength();
			}
			start = scanner.getStartOffset() + event.lineOffset;
			styles.addElement(createStyleRange(token, start, scanner.getLength()));
			token = scanner.nextToken();
		}
		event.styles = new StyleRange[styles.size()];
		styles.copyInto( event.styles );
	}

	/**
	 * A simple fuzzy scanner for Java
	 */
	public class JavaScanner {

		protected Map<String, Integer> keywordsKeys = null;
		protected Map<?, ?> keywords = null;

		protected StringBuilder fBuffer = new StringBuilder();
		protected String fDoc;
		protected int fPos;
		protected int fEnd;
		protected int fStartToken;
		protected boolean fEofSeen = false;

		String[] KEYWORDS = { "attr_accessor", "attr_reader", "attr_writer", "include", "lambda", "load", "proc", "loop", "private", "protected", "public", "raise", "catch",
				"java_import", "require", "import", "include_package", "class", "end", "def", "do", "while", "if", "else", "elsif", "switch", "nil"
		};

		public JavaScanner() {
			initialize();
		}

		/**
		 * Returns the ending location of the current token in the document.
		 */
		public final int getLength() {
			return fPos - fStartToken;
		}

		/**
		 * Initialize the lookup table.
		 */
		void initialize() {
			this.keywordsKeys = new Hashtable<String, Integer>();
			for ( int i = 0; i < KEYWORDS.length; i++ ) {
				this.keywordsKeys.put( KEYWORDS[i], i );
			}
		}

		/**
		 * Returns the starting location of the current token in the document.
		 */
		public final int getStartOffset() {
			return fStartToken;
		}

		/**
		 * Returns the next lexical token in the document.
		 */
		public int nextToken() {
			int c;
			fStartToken = fPos;
			while ( true ) {
				switch ( c = read() ) {
				case EOF:
					return EOF;
				case '#': // comment
					while ( true ) {
						c = read();
						if ( ( c == EOF ) || ( c == EOL ) ) {
							unread( c );
							return TOKEN_COMMENT;
						}
					}
				case '\'': // string single quote
					for ( ;; ) {
						c = read();
						switch ( c ) {
						case '\'':
							return TOKEN_STRING;
						case EOF:
							unread( c );
							return TOKEN_STRING;
						case '\\':
							c = read();
							break;
						default:
							break;
						}
					}
				case '"': // string double quote
					for ( ;; ) {
						c = read();
						switch ( c ) {
						case '"':
							return TOKEN_STRING;
						case EOF:
							unread( c );
							return TOKEN_STRING;
						case '\\':
							c = read();
							break;
						default:
							break;
						}
					}
					
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				do {
					c = read();
				} while ( Character.isDigit( (char) c ) );
				unread( c );
				return TOKEN_CONSTANT; //number
			default:
				if ( Character.isWhitespace( (char) c ) ) {
					do {
						c = read();
					} while ( Character.isWhitespace( (char) c ) );
					unread( c );
					return TOKEN_DEFAULT;
				}
				char startChar = (char) c;
				if(startChar == ':') {
					c = read();
				}
				if ( Character.isJavaIdentifierStart( (char) c ) ) {
					fBuffer.setLength( 0 );
					do {
						fBuffer.append( (char) c );
						c = read();
					} while ( Character.isJavaIdentifierPart( (char) c ) );
					unread( c );
					Integer i = keywordsKeys.get( fBuffer.toString() );
					if ( i != null ) { return TOKEN_KEYWORD; }
				}
				return getToken(startChar);
			}
		}
	}
		
	private int getToken(char c){
		if(  c == '$') {
			return TOKEN_GLOBAL_VARIABLE;
		}
		if( c == ':') {
			return TOKEN_SYMBOL;
		}
		if(Character.isUpperCase(c)) {
			return TOKEN_CONSTANT;
		}
		return TOKEN_VARIABLE;
	}

	/**
	 * Returns next character.
	 */
	protected int read() {
		if ( fPos <= fEnd ) {
			return fDoc.charAt( fPos++ );
		}
		return EOF;
	}

	public void setRange( String text ) {
		fDoc = text;
		fPos = 0;
		fEnd = fDoc.length() - 1;
	}

	protected void unread( int c ) {
		if ( c != EOF ) {
			fPos--;
		}
	}
}
}