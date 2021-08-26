import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Lexer {
    File file;
    StringBuilder buffer = new StringBuilder();
    ArrayList<Token> tokens;
    StatusHelper statusHelper;

    public Lexer(String filename) {
        file = new File(filename);
        tokens = new ArrayList<>();
        statusHelper = new StatusHelper();
    }

    public ArrayList<Token> tokenize() throws IOException {

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            int characterCode = bufferedInputStream.read();
            while (characterCode != -1) {
                char character = (char) characterCode;
                analyzeChar(character);

                characterCode = bufferedInputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    private void analyzeChar(char input) {
        if ( isSlash(input) ) {
            processSlash();
        } else if ( isStar(input) ) {
            processStar();
        } else if ( isEndline(input) ) {
            processEndline(input);
        } else {
            processCharacter(input);
        }
    }


    private void processSlash() {
        Status currentStatus = statusHelper.getCurrenStatus();
        if ( isComment() && currentStatus != Status.STAR ) {
            buffer.append('/');
        } else if ( currentStatus == Status.SLASH ) {
            statusHelper.setCurrenStatus(Status.INLINE_COMMENT);
            initBuffer();
            buffer.append("//");
        } else if ( currentStatus == Status.STAR && statusHelper.getPreviousStatus() == Status.MULTILINE_COMMENT ) {
            statusHelper.setCurrenStatus(Status.END_COMMENT);
            buffer.append("*/");
            tokens.add(createToken(TokenType.COMMENT, buffer.toString()));
        } else {
            statusHelper.setCurrenStatus(Status.SLASH);
        }
    }

    private void processStar() {
        Status currentStatus = statusHelper.getCurrenStatus();

        if ( isComment() ) {
            if ( currentStatus == Status.MULTILINE_COMMENT )
                statusHelper.setCurrenStatus(Status.STAR);
            else buffer.append('*');
        } else if ( currentStatus ==  Status.SLASH ) {
            statusHelper.setCurrenStatus(Status.MULTILINE_COMMENT);
            initBuffer();
            buffer.append("/*");
        }
    }

    private void processEndline(char input) {
        Status currentStatus = statusHelper.getCurrenStatus();

        if ( currentStatus == Status.INLINE_COMMENT ) {
            tokens.add(createToken(TokenType.COMMENT, buffer.toString()));
            statusHelper.setCurrenStatus(Status.END_COMMENT);
        } else if ( isComment() ){
            buffer.append(input);
        }
    }

    private void processCharacter(char input) {
        if ( isComment() ) {
            buffer.append(input);
        }
    }

    private boolean isSlash(char input) {
        return input == '/';
    }

    private boolean isStar(char input) {
        return input == '*';
    }

    private boolean isEndline(char input) {
        return input == '\r' || input == '\n';
    }


    private boolean isComment() {
        return (statusHelper.getCurrenStatus() == Status.INLINE_COMMENT
                || statusHelper.getPreviousStatus() == Status.INLINE_COMMENT
                || statusHelper.getCurrenStatus() == Status.MULTILINE_COMMENT
                || statusHelper.getPreviousStatus() == Status.MULTILINE_COMMENT)
                && statusHelper.getCurrenStatus() != Status.END_COMMENT;
    }




    public void initBuffer() {
        buffer = new StringBuilder();
    }

    private Token createToken(TokenType type, String value) {
        return new Token(type, value);
    }

    public void printSortedTokens() {
        Set<Token> tokens1 = new HashSet<>(tokens);
        tokens1.stream()
                .sorted((o1, o2) -> {
                    {
                        if (o1.getType().ordinal() == o2.getType().ordinal()) {
                            return 0;
                        } else if (o1.getType().ordinal() > o2.getType().ordinal()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                })
                .forEach(System.out::println);
    }
}

