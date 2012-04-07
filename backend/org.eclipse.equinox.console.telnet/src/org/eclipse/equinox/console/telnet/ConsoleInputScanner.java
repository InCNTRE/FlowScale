package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class performs the processing of the input command and special characters, and updates respectively what is displayed in the output
 * when line editing is performed in the telnet console. 
 * 
 *
 */
public class ConsoleInputScanner {
	/*
	The NVT printer has an unspecified carriage width and page length
	 and can produce representations of all 95 USASCII graphics (codes
	 32 through 126).  Of the 33 USASCII control codes (0 through 31
	 and 127), and the 128 uncovered codes (128 through 255), the
	 following have specified meaning to the NVT printer:
	 */

	private static final int BS = 8; // Moves the print head one character position towards the left margin.
	private static final int LF = 10; // Moves the printer to the next print line, keeping the same horizontal position.
	private static final int CR = 13; //Moves the printer to the left margin of the current line.
	private static final int ESC = 27;
	private static final int SPACE = 32;
	private static final int DELL = 127;
	private static final int COMMAND = 255;

	private boolean isEsc = false;
	private boolean isCR = false;
	private boolean isCommand = false;

	private boolean replace = false;

	private final SimpleByteBuffer buffer;
	private final HistoryHolder history;
	private final ConsoleInputStream toShell;
	private final ConsoleOutputStream toTelnet;

	public ConsoleInputScanner(ConsoleInputStream toShell, ConsoleOutputStream toTelnet) {
		buffer = new SimpleByteBuffer();
		history = new HistoryHolder();
		this.toShell = toShell;
		this.toTelnet = toTelnet;
	}

	public void scan(int b) throws IOException {
		b &= 0xFF;
		if (isCR) {
			isCR = false;
			if (b == LF) {
				return;
			}
		}
		if (isEsc) {
			scanEsc(b);
		} else if (isCommand) {
			scanCommand(b);
		} else {
			switch (b) {
				case BS :
					backSpace();
					break;
				case CR :
					isCR = true;
				case LF :
					processData();
					break;
				case ESC :
					startEsc();
					break;
				case DELL :
					delete();
					break;
				case COMMAND :
					startCommand();
					break;
				default :
					if (b >= SPACE && b < DELL) {
						newChar(b);
					}
			}
		}
	}

	private void delete() throws IOException {
		clearLine();
		buffer.delete();
		echoBuff();
		flush();
	}

	private void backSpace() throws IOException {
		clearLine();
		buffer.backSpace();
		echoBuff();
		flush();
	}

	private void newChar(int b) throws IOException {
		clearLine();
		if (replace) {
			buffer.replace(b);
		} else {
			buffer.insert(b);
		}
		echoBuff();
		flush();
	}

	private void echoBuff() throws IOException {
		byte[] data = buffer.copyCurrentData();
		for (int i = 0; i < data.length; i++) {
			echo(data[i]);
		}
		int pos = buffer.getPos();
		for (int i = data.length; i > pos; i--) {
			echo(BS);
		}
	}

	private void clearLine() throws IOException {
		int size = buffer.getSize();
		int pos = buffer.getPos();
		for (int i = size - pos; i < size; i++) {
			echo(BS);
		}
		for (int i = 0; i < size; i++) {
			echo(SPACE);
		}
		for (int i = 0; i < size; i++) {
			echo(BS);
		}
	}

	private void processData() throws IOException {
		buffer.add(CR);
		buffer.add(LF);
		echo(CR);
		echo(LF);
		flush();
		byte[] curr = buffer.getCurrentData();
		history.add(curr);
		toShell.add(curr);
	}

	private void echo(int b) throws IOException {
		toTelnet.write(b);
	}

	private void flush() throws IOException {
		toTelnet.flush();
	}

	/*
	 NAME               CODE              MEANING

	 SE                  240    End of subnegotiation parameters.
	 NOP                 241    No operation.
	 Data Mark           242    The data stream portion of a Synch.
	                            This should always be accompanied
	                            by a TCP Urgent notification.
	 Break               243    NVT character BRK.
	 Interrupt Process   244    The function IP.
	 Abort output        245    The function AO.
	 Are You There       246    The function AYT.
	 Erase character     247    The function EC.
	 Erase Line          248    The function EL.
	 Go ahead            249    The GA signal.
	 SB                  250    Indicates that what follows is
	                            subnegotiation of the indicated
	                            option.
	 WILL (option code)  251    Indicates the desire to begin
	                            performing, or confirmation that
	                            you are now performing, the
	                            indicated option.
	 WON'T (option code) 252    Indicates the refusal to perform,
	                            or continue performing, the
	                            indicated option.
	 DO (option code)    253    Indicates the request that the
	                            other party perform, or
	                            confirmation that you are expecting
	                            the other party to perform, the
	                            indicated option.
	 DON'T (option code) 254    Indicates the demand that the
	                            other party stop performing,
	                            or confirmation that you are no
	                            longer expecting the other party
	                            to perform, the indicated option.
	 IAC                 255    Data Byte 255.

	 */
	private static final int SE = 240;
	private static final int EC = 247;
	private static final int EL = 248;
	private static final int SB = 250;
	private static final int WILL = 251;
	private static final int WILL_NOT = 252;
	private static final int DO = 253;
	private static final int DO_NOT = 254;

	private boolean isNegotiation;
	private boolean isOption;

	private void scanCommand(final int b) throws IOException {
		if (isNegotiation) {
			scanNegotiation(b);
		} else if (isOption) {
			isOption = false;
			isCommand = false;
		} else {
			switch (b) {
				case WILL :
				case WILL_NOT :
				case DO :
				case DO_NOT :
					isOption = true;
					break;
				case SB :
					isNegotiation = true;
					break;
				case EC :
					eraseChar();
					isCommand = false;
					break;
				case EL :
					eraseLine();
					isCommand = false;
					break;
				default :
					isCommand = false;
					break;
			}
		}
	}

	private void eraseLine() throws IOException {
		clearLine();
		buffer.delAll();
		echoBuff();
		flush();
	}

	private void eraseChar() throws IOException {
		backSpace();
	}

	private void scanNegotiation(final int b) {
		if (b == SE) {
			isNegotiation = false;
			isCommand = false;
		}
	}

	public void resetHistory() {
		history.reset();
	}

	private static interface KEYS {
		public int UP = 0;
		public int DOWN = 1;
		public int RIGHT = 2;
		public int LEFT = 3;
		public int CENTER = 4;
		public int HOME = 5;
		public int END = 6;
		public int PGUP = 7;
		public int PGDN = 8;
		public int INS = 9;
		public int DEL = 10;
		public int UNFINISHED = 11;
		public int UNKNOWN = 12;
	}

	//	private static final Map<String, KEYS> escapesToKey;
	private static final Map escapesToKey;
	private static final String[] escapes;

	static {
		//		escapesToKey = new HashMap<String, KEYS>();
		escapesToKey = new HashMap();
		escapesToKey.put("[A", new Integer(KEYS.UP)); //$NON-NLS-1$
		escapesToKey.put("[B", new Integer(KEYS.DOWN)); //$NON-NLS-1$
		escapesToKey.put("[C", new Integer(KEYS.RIGHT)); //$NON-NLS-1$
		escapesToKey.put("[D", new Integer(KEYS.LEFT)); //$NON-NLS-1$
		escapesToKey.put("[G", new Integer(KEYS.CENTER)); //$NON-NLS-1$
		escapesToKey.put("[1~", new Integer(KEYS.HOME)); //$NON-NLS-1$
		escapesToKey.put("[4~", new Integer(KEYS.END)); //$NON-NLS-1$
		escapesToKey.put("[5~", new Integer(KEYS.PGUP)); //$NON-NLS-1$
		escapesToKey.put("[6~", new Integer(KEYS.PGDN)); //$NON-NLS-1$
		escapesToKey.put("[2~", new Integer(KEYS.INS)); //$NON-NLS-1$
		escapesToKey.put("[3~", new Integer(KEYS.DEL)); //$NON-NLS-1$
		escapes = new String[escapesToKey.size()];
		Object[] temp = escapesToKey.keySet().toArray();
		for (int i = 0; i < escapes.length; i++) {
			escapes[i] = (String) temp[i];
		}
		//		escapes = escapesToKey.keySet().toArray(new String[escapesToKey.size()]);
	}

	private static int checkEscape(String possibleEsc) {
		if (escapesToKey.get(possibleEsc) != null) {
			int key = ((Integer) escapesToKey.get(possibleEsc)).intValue();
			return key;
		}

		for (int i = 0; i < escapes.length; i++) {
			if (escapes[i].startsWith(possibleEsc)) {
				return KEYS.UNFINISHED;
			}
		}
		return KEYS.UNKNOWN;

	}

	private String esc;

	private void scanEsc(final int b) throws IOException {
		esc += (char) b;
		int key = checkEscape(esc);
		if (key == KEYS.UNFINISHED) {
			return;
		}
		if (key == KEYS.UNKNOWN) {
			isEsc = false;
			scan(b);
			return;
		}
		isEsc = false;
		switch (key) {
			case KEYS.UP :
				processUpArrow();
				break;
			case KEYS.DOWN :
				processDownArrow();
				break;
			case KEYS.RIGHT :
				processRightArrow();
				break;
			case KEYS.LEFT :
				processLeftArrow();
				break;
			case KEYS.HOME :
				processHome();
				break;
			case KEYS.END :
				processEnd();
				break;
			case KEYS.PGUP :
				processPgUp();
				break;
			case KEYS.PGDN :
				processPgDn();
				break;
			case KEYS.INS :
				processIns();
				break;
			case KEYS.DEL :
				delete();
				break;
			default : //CENTER
				break;
		}
	}

	private static final byte[] INVERSE_ON = {ESC, '[', '7', 'm'};
	private static final byte[] INVERSE_OFF = {ESC, '[', '2', '7', 'm'};

	private void echo(byte[] data) throws IOException {
		for (int i = 0; i < data.length; i++) {
			echo(data[i]);
		}
	}

	private void processIns() throws IOException {
		replace = !replace;
		int b = buffer.getCurrentChar();
		echo(INVERSE_ON);
		echo(replace ? 'R' : 'I');
		flush();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			//do not care $JL-EXC$
		}
		echo(INVERSE_OFF);
		echo(BS);
		echo(b == -1 ? SPACE : b);
		echo(BS);
		flush();
	}

	private void processPgDn() throws IOException {
		byte[] last = history.last();
		if (last != null) {
			clearLine();
			buffer.set(last);
			echoBuff();
			flush();
		}
	}

	private void processPgUp() throws IOException {
		byte[] first = history.first();
		if (first != null) {
			clearLine();
			buffer.set(first);
			echoBuff();
			flush();
		}
	}

	private void processHome() throws IOException {
		int pos = buffer.resetPos();
		if (pos > 0) {
			for (int i = 0; i < pos; i++) {
				echo(BS);
			}
			flush();
		}
	}

	private void processEnd() throws IOException {
		int b;
		while ((b = buffer.goRight()) != -1) {
			echo(b);
		}
		flush();
	}

	private void processLeftArrow() throws IOException {
		if (buffer.goLeft()) {
			echo(BS);
			flush();
		}
	}

	private void processRightArrow() throws IOException {
		int b = buffer.goRight();
		if (b != -1) {
			echo(b);
			flush();
		}
	}

	private void processDownArrow() throws IOException {
		byte[] next = history.next();
		if (next != null) {
			clearLine();
			buffer.set(next);
			echoBuff();
			flush();
		}
	}

	private void processUpArrow() throws IOException {
		clearLine();
		byte[] prev = history.prev();
		buffer.set(prev);
		echoBuff();
		flush();
	}

	private void startCommand() {
		isCommand = true;
		isNegotiation = false;
		isOption = false;
	}

	private void startEsc() {
		isEsc = true;
		esc = "";
	}
}
