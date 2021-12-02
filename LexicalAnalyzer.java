import java.io.*;
import java.util.*;

class LexicalAnalyzer 
{
    String currentToken;
    static List<String> keywords;
    static List<Integer> tokens = new ArrayList<>();

    LexicalAnalyzer(File f) throws Exception
    {
        keywords = getKeyWords();

        tokenize(f);

        System.out.println(tokens.toString());
    }

    public static List<Integer> getTokens() //returns the list of tokens
    {
        return tokens;
    }

    //this function takes a file F and breaks the enitre file into tokens
    public static void tokenize(File f) throws Exception
    {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        int i = 0;
        String token = "";
        int tokenType = 0;
        
        if((i = br.read()) != -1 && i != 32 && i != 57) //starts the tokenizing but makes sure it doesn't start on a blank space
        {
            token += (char)i;
            tokenType = getTokenType(token);
        }

        while((i = br.read()) != -1) //reads and process every char until the loop ends
        {
            if((i == 32 && !token.equals(" ") && !token.equals("")) || (i == 10 && !token.equals(" ") && !token.equals(""))) { //if the current char is a white space
                tokens.add(getTokenType(token));
                token = ""; //reset token
                tokenType = 0;
            } else if (i == 32 || i == 10) {
                //dead space to kill white spaces that get through
            } else {
                token += (char)i; //add the current char to token

                if(!checkTokenType(token, tokenType)) { //if token doesn't match the current tokenType assigned
                    String temp = "" + (char)i; //create a temp string holding the new char
                    if(i == 46 && tokenType != 2) { //if the current char is a '.' and current tokenType isn't "Integer"
                        char j = (char)br.read(); //read next char 
                        temp += j; //add next char to temp
                        if(checkRegex(temp, "\\Q.\\E[0-9]")) { //check if the next char was an int, to see if '.' is beginning of a float
                            token = temp;
                            tokenType = 3;
                        } else { //if its not the start of a float
                            token = token.substring(0, token.length() - 1); //remove added '.'
                            if(token.length() != 0) tokens.add(getTokenType(token)); //if token is not empty
                            String specChar = "" + (char)i;
                            tokens.add(getTokenType(specChar));
                            token = (j != ' ') ? "" + j : ""; //if the next char was a white space reset token, if not add it to the start of the next token
                            tokenType = 0; //reset type
                        }
                    } else if (checkRegex(temp, "\\W") && !temp.equals(".") && i != 32 && !token.matches("(\\|\\||&&|<=|>=|<|>|\\||&|=|==)")) { //if next char is a special symbol and not a "." or a space
                        token = token.substring(0, token.indexOf((char)i)); //remove added char from token
                        if(token.length() != 0) tokens.add(getTokenType(token)); //if token is not empty
                        if(i != 13) tokens.add(getTokenType(temp)); //if special symbol is not being misread as a blank space
                        token = ""; //reset token
                        tokenType = 0;
                    } else if (checkRegex(temp, "[0-9()\\+\\*-/]+") && token.length() > 1 && !temp.equals(".")) { //special rule for math expr
                        token = token.substring(0, token.indexOf((char)i));
                        tokens.add(getTokenType(token));
                        token = temp;
                        tokenType = getTokenType(token);
                    } else{ //if all else above fail get new token type
                        tokenType = getTokenType(token);
                    }
                }
            }
        }

        if(!token.equals(" ") && token.length() >= 1) tokens.add(getTokenType(token)); //print last token in file as long as its not empty
    }

    public static boolean checkTokenType(String token, int tokenType) //this function checks if the parameter token matches the parameter tokenType with the appropriate regex
    {
        switch(tokenType)
        {
            case 1:
                return checkRegex(token, "[_a-zA-Z]+[_a-zA-Z0-9]*");
            case 2:
                return checkRegex(token, "([0-9]|[1-9]([0-9]{0,9}))");
            case 6:
                return checkRegex(token, "0x([0-9a-fA-F]{1,8})");
            case 7:
                return checkRegex(token, "0([1-7]{1,10})");
            case 3: 
                return checkRegex(token, "[0-9]*\\Q.\\E[0-9]*");
            case 8:
                return checkRegex(token, "[0-1]*\\Q.\\E([0-9a-fA-F]{1,10})");
            case 4:
                return checkRegex(token, "^[0-9a-zA-Z_]");
            case 20:
                return token.equals("=");
            case 21:
                return token.equals("+");
            case 22:
                return token.equals("-");
            case 23:
                return token.equals("*");
            case 24:
                return token.equals("/");
            case 25:
                return token.equals("(");
            case 26:
                return token.equals(")");
            case 27:
                return token.equals("%");
            case 28:
                return token.equals("<=");
            case 29:
                return token.equals(">=");
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 46:
            case 48:
            case 49:
                return keywords.contains(token);
            case 38:
                return token.equals("==");
            case 39:
                return token.equals("||");
            case 40:
                return token.equals("&&");
            case 41:
                return token.equals("<");
            case 42:
                return token.equals(">");
            case 43: 
                return token.equals(";");
            case 44:
                return token.equals("{");
            case 45:
                return token.equals("}");
            case 47:
                return token.equals(":");
            default:
                return false;
        }
    }

    public static boolean checkRegex(String token, String regex) //this is a helper function to check if a parameter token matches parameter regex 
    {
        if(token.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }

    public static int getTokenType(String token) //this method takes a token as the parameter and checks it with various different regex and assigns the token type
    {
        if(keywords.contains(token)) {
            return getTokenTypeKeyword(token);
        } else if(token.matches("[_a-zA-Z]+[_a-zA-Z0-9]*")) { //User Definied Identifier 
            return 1;
        } else if (token.matches("([0-9]|[1-9]([0-9]{0,9}))")) { //Integer
            return 2;
        } else if (token.matches("0x([0-9a-fA-F]{1,8})")) { //Integer (Hex)
            return 6;
        } else if (token.matches("0([1-7]{1,10})")) { //Integer (Octal)
            return 7;
        } else if (token.equals(".")) { //Period
            return 5;
        } else if (token.matches("[0-9]*\\Q.\\E[0-9]*")) { //Floating Point
            return 3;
        } else if (token.matches("[0-1]*\\Q.\\E([0-9a-fA-F]{1,10})")) { //Floating Point (Hex)
            return 8;
        } else if (token.equals("<=")) {
            return 28;
        } else if (token.equals(">=")) {
            return 29;
        } else if (token.equals("==")) {
            return 38;
        } else if (token.equals("||")) {
            return 39;
        } else if (token.equals("&&")) {
            return 40;
        } else if(token.equals("=")) {
            return 20;
        } else if(token.equals("+")) {
            return 21;
        } else if(token.equals("-")) {
            return 22;
        } else if(token.equals("*")) {
            return 23;
        } else if(token.equals("/")) {
            return 24;
        } else if(token.equals("(")) {
            return 25;
        } else if(token.equals(")")) {
            return 26;
        } else if(token.equals("%")) {
            return 27;
        } else if(token.equals("<")) {
            return 41;
        } else if(token.equals(">")) {
            return 42;
        } else if(token.equals(";")) {
            return 43;
        } else if(token.equals("{")) {
            return 44;
        } else if (token.equals("}")) {
            return 45;
        } else if (token.equals(":")) { //48 & 49 taken
            return 47;
        } else if (token.matches("\\W")) { //Special Character
            return 4;
        } else {
            return 99;
        }
    }

    //this method reads from a keyword.txt file and returns an array list containing them all of Java's keywords
    public static List<String> getKeyWords()
    {
        List<String> temp = new ArrayList<>();
        temp.add("for");
        temp.add("if");
        temp.add("else");
        temp.add("while");
        temp.add("do");
        temp.add("int");
        temp.add("float");
        temp.add("switch");
        temp.add("return");
        temp.add("void");
        temp.add("main");
        temp.add("case");
        temp.add("default");
        temp.add("break");
        return temp;
    }

    public static int getTokenTypeKeyword(String token) //this function gets the tokenType for keywords
    {
        switch(token)
        {
            case "for":
                return 30;
            case "if":
                return 31;
            case "else":
                return 32;
            case "while":
                return 33;
            case "do":
                return 34;
            case "int":
                return 35;
            case "float":
                return 36;
            case "switch":
                return 37;
            case "return":
                return 46;
            case "void":
                return 48;
            case "main":
                return 49;
            case "case":
                return 50;
            case "default":
                return 51;
            case "break":
                return 52;
            default:
                return getTokenType(token);
        }
    }
}