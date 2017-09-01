/* Generated By:JavaCC: Do not edit this line. ATSParser.java */
package util.parser.ats;

import automata.IBuchiWa;
import automata.IStateWa;
import automata.BuchiWa;

import util.parser.ats.ATSFileParser;

public class ATSParser implements ATSParserConstants {

  private IBuchiWa buchi;

  private ATSFileParser parser ;

  public void parse(ATSFileParser parser)
  {
    this.parser = parser;
    try
    {
      parseFile();
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
  }

  final private void parseFile() throws ParseException {
  IBuchiWa fst, snd;
    label_1:
    while (true) {
      buchi = null;
      parser.clearStateMap();
      parseAutomaton();
      fst = buchi;
      buchi = null;
      parser.clearStateMap();
      parseAutomaton();
      snd = buchi;
      parser.addPair(fst, snd);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AUTOMATON:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
    }
  }

  final private void parseAutomaton() throws ParseException {
  Token nameStr, labelStr, sourceStr, targetStr;
    jj_consume_token(AUTOMATON);
    jj_consume_token(STRING);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTPARA);
    parseAlphabet();
    buchi = new BuchiWa(parser.getAlphabetSize());
    parseStates();
    parseInitStates();
    parseFinalStates();
    parseTransitions();
    jj_consume_token(RIGHTPARA);
    jj_consume_token(SEMICOLON);
  }

  final private void parseAlphabet() throws ParseException {
  Token labelStr;
    jj_consume_token(ALPHABET);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTCURLY);
    label_2:
    while (true) {
      labelStr = jj_consume_token(STRING);
      String label = labelStr.toString();
//      int num = parser.getAlphabetSize();
//      parser.putLetter(label, num);
      parser.addLetter(label);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
    }
    jj_consume_token(RIGHTCURLY);
    jj_consume_token(COMMA);
  }

  final private void parseStates() throws ParseException {
  Token stateStr;
    jj_consume_token(STATES);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTCURLY);
    label_3:
    while (true) {
      stateStr = jj_consume_token(STRING);
      String state = stateStr.toString();
      IStateWa st = buchi.addState();
      parser.putState(state, st.getId());
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
    }
    jj_consume_token(RIGHTCURLY);
    jj_consume_token(COMMA);
  }

  final private void parseInitStates() throws ParseException {
  Token stateStr;
    jj_consume_token(INITSTATES);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTCURLY);
    label_4:
    while (true) {
      stateStr = jj_consume_token(STRING);
      int id = parser.getStateId(stateStr.toString());
      buchi.setInitial(id);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_4;
      }
    }
    jj_consume_token(RIGHTCURLY);
    jj_consume_token(COMMA);
  }

  final private void parseFinalStates() throws ParseException {
  Token stateStr;
    jj_consume_token(FINALSTATES);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTCURLY);
    label_5:
    while (true) {
      stateStr = jj_consume_token(STRING);
      int id = parser.getStateId(stateStr.toString());
      buchi.setFinal(id);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_5;
      }
    }
    jj_consume_token(RIGHTCURLY);
    jj_consume_token(COMMA);
  }

  final private void parseTransitions() throws ParseException {
  Token sourceStr, labelStr, targetStr;
  int sourceId, letter, targetId;
    jj_consume_token(TRANSITIONS);
    jj_consume_token(ASSIGNMENT);
    jj_consume_token(LEFTCURLY);
    label_6:
    while (true) {
      jj_consume_token(LEFTPARA);
      sourceStr = jj_consume_token(STRING);
      sourceId = parser.getStateId(sourceStr.toString());
      labelStr = jj_consume_token(STRING);
      letter = parser.getLetterId(labelStr.toString());
      targetStr = jj_consume_token(STRING);
      targetId = parser.getStateId(targetStr.toString());
      IStateWa state = buchi.getState(sourceId);
      state.addSuccessor(letter, targetId);
      jj_consume_token(RIGHTPARA);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LEFTPARA:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_6;
      }
    }
    jj_consume_token(RIGHTCURLY);
  }

  /** Generated Token Manager. */
  public ATSParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[6];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x800,0x100000,0x100000,0x100000,0x100000,0x80,};
   }

  /** Constructor with InputStream. */
  public ATSParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ATSParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ATSParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public ATSParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ATSParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public ATSParser(ATSParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ATSParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[21];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 6; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 21; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
