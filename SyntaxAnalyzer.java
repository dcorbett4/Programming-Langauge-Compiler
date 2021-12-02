import java.io.File;
import java.util.List;

public class SyntaxAnalyzer //all of the statements in this file use Java's syntax rules
{
    static int nextToken;
    static List<Integer> tokens;
    public static void main(String[] args) throws Exception
    {
        File f = new File("test.txt"); //this file contains multiple test cases to prove all of the statements work

        LexicalAnalyzer lex = new LexicalAnalyzer(f); //creates a lexical analyzer for the file
        
        tokens = lex.getTokens(); //get the tokenized file

        getNextToken(); //gets first token

        program(); //runs the syntax checks

        System.out.println("all good"); //prints to show the whole file has the correct syntax
    }

    public static void getNextToken() //this function gets the next token and removes it from the list of tokens
    {
        if(!tokens.isEmpty()) nextToken = tokens.get(0); //if the list tokens isn't empty get the next token 
        if(!tokens.isEmpty()) tokens.remove(0); //if the list of tokens isn't empty remove the token recieved
    }

    //this function checks if the program has correct syntax for the "program" rule in the pdf
    public static void program() throws Exception
    {
        if(nextToken != 48) { //check if nextToken is "void"
            throw new Exception("expecting \"void\" (program)");
        } else {
            getNextToken();
            if(nextToken != 49) { //checks if nextToken is "main"
                throw new Exception("expecting \"main\" (program)");
            } else {
                getNextToken();
                if(nextToken != 25) { //checks if nextToken is '('
                    throw new Exception("expecting ( (program)");
                } else { 
                    getNextToken();
                    if(nextToken != 26) { //checks if nextToken is ')'
                        throw new Exception("expecting ) (program)");
                    } else {
                        getNextToken();
                        block(); //calls block function to follow rules in pdf
                    }
                }
            }
        }
    }

    //this function checks the correctness of the syntax of the block portions of the program according to the rules in the pdf
    public static void block() throws Exception
    {   
        if(nextToken != 44) { //checks if the nextToken is a '{'
            throw new Exception("expecting { (block)");
        } else {
            getNextToken();
            
            while(nextToken != 45) //countiously loops while the nextToken != '}', this allows for multiple statements to be inside of the block
            {
                stmt(); //calls the stmt function
                if(nextToken != 43 && nextToken != 45 && nextToken != 44) { //if the nextToken isn't a ';, }, {' there is an error
                    throw new Exception("expected ; (block)");
                }

                if(nextToken == 45 && !tokens.isEmpty() && tokens.get(0) != 32) { //if the nextToken is a '{' but there are more tokens keep going
                    getNextToken();                                                 // this was added to help aid with reading if else statements
                } else if(nextToken != 45) { //get nextToken if the block isn't over
                    getNextToken();
                }

                if(tokens.isEmpty() && nextToken != 45) { //if the block is over and the tokens are empty that means the block never ended and there is an error
                    throw new Exception("ran out of tokens (block)");
                }
            }

            if(nextToken != 45) { //if the block ends and there isn't a '}' as nextToken there is an error 
                throw new Exception("expected } (block)");
            }
        }
    }
    
    //this function checks which stmt rules need to be applied to the current stmt
    public static void stmt() throws Exception 
    {
        switch(nextToken)
        {
            case 1:
            case 35:
            case 36:
                assignment(); //user defined identifiers, int-code, and float-code all go to assignment
                break;
            case 30:
                forstmt(); //for-code goes to for statement
                break;
            case 31:
                ifstmt(); //if-code goes to if statement
                break;
            case 33:
                whilestmt(); //while-code goes to while statement
                break;
            case 34:
                dowhilestmt(); //do-code goes to do while statement
                break;
            case 37:
                switchstmt(); //switch-code goes to switch statement
                break;
            case 46:
                returnstmt(); //return-code goes to return statement
                break;
            default:
                throw new Exception("no valid token (stmt)"); //if a rouge token gets in its caught here
        }
    }

    //this function is the expression portion of the subrecursive program for mathematical expressions 
    public static void expr() throws Exception
    { 

        term(); //goes to term

        while (nextToken == 21 || nextToken == 22) //while the next token is a '+' or '-'
        {
            if (nextToken == 21) { //if the next token is a '+', this if statemnet is to accept things like i++ and i += 3
                getNextToken();
                if(nextToken != 21 && nextToken != 20) { //if next token isn't a '+' or a '='
                    term();
                } else if(nextToken != 21) { //if next token isn't a '+' means its a '='
                    getNextToken();
                    term();
                } else {
                    getNextToken();
                }
            } else { //if the next token is a '-', this if statemnet is to accept things like i-- and i -= 3
                getNextToken();
                if(nextToken != 22 && nextToken != 20) { //if next token isn't a '-' or a '='
                    term();
                } else if(nextToken != 22) { //if next token isn't a '-' means its a '-'
                    getNextToken();
                    term();
                } else {
                    getNextToken();
                }
            }
        }
    }

    //this function is the term portion of the subrecursive program for mathematical expressions 
    public static void term() throws Exception
    {
        factor();

        while (nextToken == 23 || nextToken == 24 || nextToken == 27) //while next the token is a '*', '/', or '%'
        {
            getNextToken();
            if(nextToken == 20) { //if next token is a '=', allows for "*=", "/=", and "%="
                getNextToken();
                factor();
            } else {
                factor();
            }
        }
    }

    //this function is the factor portion of the subrecursive program for mathematical expressions 
    public static void factor() throws Exception
    {
        if(nextToken == 1 || nextToken == 2 || nextToken == 3) { //if the next token is a user definined identifier, integer literal, or floating point literal
            getNextToken();
            
            if (nextToken == 25) { //if the next token is a '(', this accounts for nested parentheses
                getNextToken();
                expr(); //get the expr inside of the parentheses
                if (nextToken == 26) { //if the next token is a ')', makes sure the parentheses close
                    getNextToken();
                } else {
                    throw new Exception("Error: parentheses never close");
                }
            }

        } else {
            if (nextToken == 25) { //if the next token is a '('
                getNextToken();
                expr(); //get the expr inside of the parentheses
                if (nextToken == 26) { //if the next token is a ')', makes sure the parentheses close
                    getNextToken();
                } else {
                    throw new Exception("Error: parentheses never close");
                }
            } else { //if the expression contains an invalid token
                throw new Exception("Error: invalid token");
            }
        }
    }

    //this function is the subrecursive program for the EBNF rule of the if statement for question #3
    public static void ifstmt() throws Exception
    {
        System.out.println("ifstmt"); 
        if(nextToken != 31) { //makes sure the first token is the "if" token code
            throw new Exception("\"if\" keyword expected");
        } else { 
            getNextToken();
            if(nextToken != 25) { //if the next token isn't a '('
                throw new Exception("( expected (ifstmt)");
            } else {
                getNextToken();

                do { //this loop accounts for multiple boolexpr using || and &&
                    boolexpr(); //get the boolean expression inside of the
                } while ((nextToken == 39 || nextToken == 40));

                if(nextToken != 26) { //if the next token isn't a ')', makes sure the parenthese close
                    throw new Exception(") expected (ifstmt)");
                } else {
                    getNextToken();
                    block(); //chceks the block of the statement

                    getNextToken();
                    if(nextToken == 32) { //if the next token is the "else" token code
                        getNextToken();
                        block(); //get the expression inside (this accounts for else if, since keyword checking is built into my expr() function)
                    }
                }
            }
        }
    }

    //this function is the subrecursive program for the EBNF rule of the while statement for question #3
    public static void whilestmt() throws Exception
    {
        System.out.println("whilestmt");
        if(nextToken != 33) { //makes sure the first token is the "while" token code
            throw new Exception("\"while\" expected");
        } else {
            getNextToken();
            if (nextToken != 25) { //if the next token isn't a '('
                throw new Exception("( expected whilestmt)");
            } else {
                getNextToken();
                
                do { //this loop accounts for multiple boolexpr using || and &&
                    boolexpr(); //get the boolean expression inside of the
                } while ((nextToken == 39 || nextToken == 40));

                if (nextToken != 26) { //if the next token isn't a ')', makes sure the parentheses close
                    throw new Exception(") expected (whilestmt)");
                } else {
                    getNextToken();
                    block(); //gets the expression inside
                }
            }
        }
    }

    //this function is the subrecursive program for the EBNF rule of the while statement for question #3
    public static void forstmt() throws Exception
    {
        System.out.println("forstmt");
        if(nextToken != 30) { //makes sure the first token is the "for" token code
            throw new Exception("\"for\" expected");
        } else {
            getNextToken();
            if (nextToken != 25) { //if the next token isn't a '('
                throw new Exception("( expected (forstmt)");
            } else {

                //the below block of 7 lines checks if there is a ':', which would indicate a foreach loop
                int check = 0;
                int i = 0;
                while(i < 3 && check != 47)
                {
                    check = tokens.get(i);
                    i++;
                }

                if(check == 47) { //if there is a ':' it is a foreach loop and therefore goes by different rules
                    for(int j = 0; j < i; j++) //gets nextToken up to the ':' for the for each loop
                    {
                        getNextToken();
                    }
                    getNextToken();
                    foreach();
                } else {
                    //the next 5 lines check for the expression inside the parenthese of a for loop
                    getNextToken(); 
                    intialization(); //checks for the initialization
                    boolexpr(); //checks for the boolean expression
    
                    if (nextToken != 43) { //makes sure there is a semicolon after the boolean expression
                        throw new Exception("expected a ;");
                    } else {
                        getNextToken();
                        if (nextToken != 26) { //this checks if the incrementation expression is empty
                            expr(); //checks for the incrementation expression
                        }
                    }
                    
                    if (nextToken != 26) { //if the next token isn't a ')', makes sure the parentheses close
                        throw new Exception(") expected (forstmt)");
                    } else {
                        getNextToken();
                        block(); //get the expression inside of the for loop
                    }
    
                }
            }
        }
    }

    //this function checks the syntax of an intialization
    public static void intialization() throws Exception
    {
        if(nextToken != 1 && nextToken != 35 && nextToken != 36 && nextToken != 43) { //if the next token isn't a user definied identifier, int code, float code, or a semicolon
            throw new Exception("expecting intialization");
        } else {
            if(nextToken == 35 || nextToken == 36) { //if the next token is the "int" or "float" token code, allows for intialization of new variables
                getNextToken();
                if(nextToken != 1) { //makes sure the variable is named
                    throw new Exception("expecting user definied identifier");
                }
                getNextToken();
            } else if (nextToken != 43) { //makes sure the next token also isn't a ";", to allow the intialization expr to be empty
                getNextToken();
            }
            
            if(nextToken != 20 && nextToken != 43) { //if the next token isn't "=", makes sure their is an assignment
                throw new Exception("expecting \"=\"");
            } else { 
                if(nextToken != 43) getNextToken(); //makes sure the next token also isn't a ";", to allow the intialization expr to be empty
                
                if(nextToken != 1 && nextToken != 2 && nextToken != 3 && nextToken != 43) { //if the next token isn't assigned to a user definied identifier, int literal, float literal, or semi colon
                    throw new Exception("expected a number");
                } else { 
                    if(nextToken != 43) getNextToken(); //makes sure the next token also isn't a ";", to allow the intialization expr to be empty

                    if(nextToken != 43) { //make sure its closed with a semicolon
                        throw new Exception("expected a ;");
                    } else {
                        getNextToken();
                    }
                }
            }
        }
    }

    //this function checks the syntax of a foreach loop according to java's rules
    public static void foreach() throws Exception
    {
        if(nextToken != 1) {
            throw new Exception("expecting a 2nd value in foreach");
        } else {
            getNextToken();
            if(nextToken != 26) {
                throw new Exception("expecting a \')\' (foreach)");
            } else {
                getNextToken();
                block(); //change to block
            }
        }
    }

    //this function checks the syntax of a boolean expression
    public static void boolexpr() throws Exception
    {
        if(nextToken == 39 || nextToken == 40) getNextToken(); //if the nextToken is '||' or '&&' get the one after
        
        expr(); //checks if first expression is correct

        if(!compareCheck(nextToken)) { //checks if the nextToken is a compare operator
            throw new Exception("expecting a comparison symbol");
        } else {
            getNextToken();
            expr(); //gets the second expression
        }
    }

    //this function checks the syntax of an assignment
    public static void assignment() throws Exception
    {
        if(nextToken != 1 && nextToken != 35 && nextToken != 36) { //if the token is not one of the valid ones in the Exception below there is an error
            throw new Exception("expecting either a user defined indentifier, a int code, or float code (assignment)");
        } else {
            if(nextToken == 35 || nextToken == 36) getNextToken(); //if the token is an int or float code get nextToken
            
            if(nextToken != 1) { //if next token is not a user defined identifier there is an error
                throw new Exception("expecting a user defined identifier (assignment)");
            } else {
                getNextToken();
                if(nextToken == 21 || nextToken == 22) getNextToken(); //if the next token is a + or - get next token, this is to account for += and -=
                
                if(nextToken != 20) { //if the next token isn't a = there is an error
                    throw new Exception("expecting = (assignment)");
                } else {
                    getNextToken();
                    expr(); //call expr to make sure we are assigning to valid value
                }
            }
        }
    }

    //this function checks the syntax of a do while statement
    public static void dowhilestmt() throws Exception
    {
        System.out.println("dowhilestmt");

        if(nextToken != 34) { //check if nextToken is a do-code
            throw new Exception("expected \"do\"");
        } else {
            getNextToken();
            block(); //checks the syntax of the block for the do-code

            getNextToken();
            if(nextToken != 33) { //checks if the block is followed by a while
                throw new Exception("expected \"while\"");
            } else {
                getNextToken();
                if(nextToken != 25) { //checks if nextToken is a '('
                    throw new Exception("expected ( (dowhile)");
                } else {
                    getNextToken();

                    do { //this loop allows for multiple boolean expressions joined by && or ||
                        boolexpr();
                    } while (nextToken == 39 || nextToken == 40);

                    if(nextToken != 26) { //checks if the boolean expression(s) are closed with a ')'
                        throw new Exception("expected ) (dowhile)");
                    } else {
                        getNextToken();
                    }
                }
            }
        }
    }

    //this function checks the syntax of a switch statement
    public static void switchstmt() throws Exception
    {
        System.out.println("switchstmt");

        if(nextToken != 37) { //makes sure its starts with a switch-code
            throw new Exception("expected \"switch\"");
        } else {
            getNextToken();
            if(nextToken != 25) { //checks for the following '('
                throw new Exception("expected ( (switchstmt)");
            } else {
                getNextToken();
                expr(); //checks the expression for the switch-code
                
                if(nextToken != 26) { //makes sure the expression is closed with a ')'
                    throw new Exception("expected ) (switchstmt)");
                } else {
                    getNextToken();
                    if(nextToken != 44) { //checks for the '{' to start the switch's block
                        throw new Exception("expected { (switchstmt)");
                    } else {
                        getNextToken();
                        if(nextToken != 50 && nextToken != 51) { //if there is no case-code or default-code there is an error
                            throw new Exception("expected a case");
                        } else {
                            do{ //this loop allows for multiple case statements, before the potential default statement
                                casestmt(); //checks syntax of every case
                                getNextToken();
                            } while (nextToken == 50); //while nextToken is still case-code

                            if(nextToken == 51) { //if nextToken is a default-code check its syntax
                                defaultcase();
                            } else {
                                if(nextToken != 45) getNextToken(); //if nextToken isn't a } get next token, this is to account for empty cases

                                if(nextToken != 45) { //if there is still no '}' there is an error
                                    throw new Exception("expected } (switchstmt)");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //this function checks the syntax of a case statement for a switch statement
    public static void casestmt() throws Exception
    {
        if(nextToken != 50) { //makes sure its starts with the case-code token
            throw new Exception("expected \"case\"");
        } else {
            getNextToken();
            if(nextToken != 1 && nextToken != 2 && nextToken != 3) { //with the current rules of our analyzer it checks if the case is has a valid value-
                throw new Exception("expected a value for case");     //-of either a user defined indentifier, int literal, or float literal
            } else {
                getNextToken();
                if(nextToken != 47) { //checks that a ":" comes after the case value
                    throw new Exception("expected a : (casestmt)");
                } else {
                    getNextToken();

                    if(nextToken == 50) { //this allows for empty cases
                        casestmt();
                    } else {
                        do { //this do while allows for multiple statements underneath a case
                            stmt();
                            if(nextToken != 43) { //makes sure theres a ';' after each statement
                                throw new Exception("expected a ; (casestmt)");
                            } else {
                                getNextToken();
                            } //loops while the nextToken at the end isnt case-code, default-code, return-code, or break-code. Essentially until it ends.
                        } while(nextToken != 46 && nextToken != 50 && nextToken != 51 && nextToken != 52);  

                        if(nextToken == 46) { //if nextToken is return-code
                            returnstmt();
                        } else if (nextToken == 50) { //if nextToken is case-code
                            casestmt();
                        } else if (nextToken == 51) { //if nextToken is default-code
                            defaultcase();
                        } else if (nextToken == 52) { //if nextToken is a break-code
                            getNextToken();
                            if(nextToken != 43) { //makes sure it ends with a ';'
                                throw new Exception("expected a ; (casestmt)");
                            }
                        }
                    }
                }
            }
        }
    }

    //this function checks the syntax a default case of a switch statement
    public static void defaultcase() throws Exception
    {
        if(nextToken != 51) { //makes sure its starts with a default-code
            throw new Exception("expecting \"default\"");
        } else {
            getNextToken();
            if(nextToken != 47) { //makes sure it is followed by a ':'
                throw new Exception("expected : (defaultcase)");
            } else {
                getNextToken();

                do { //this loop allows for multiple statements for the case
                    stmt(); //checks syntax of statement
                    if(nextToken != 43) getNextToken(); //if nextToken isn't ';' get nextToken
                    if(nextToken != 43) { //if nextToken still isn't ';' throw error
                        throw new Exception("expected a ; (casestmt)");
                    } else {
                        getNextToken();
                    }
                } while(nextToken != 46 && nextToken != 52); //loop while nextToken isn't a return-code or break-code

                if(nextToken == 46) { //if return code
                    returnstmt();
                } else if (nextToken == 52) { //if break-code
                    getNextToken();
                    if(nextToken != 43) { //makes sure break is followed by ';'
                        throw new Exception("expected ; (defaultcase)");
                    }
                }
            }
        }
    }

    //this function checks the syntax of a return statement
    public static void returnstmt() throws Exception 
    {
        if(nextToken != 46) { //makes sure it starts with a return-code
            throw new Exception("expected \"return\"");
        } else {
            getNextToken();

            //the below loop checks if the return is gonna hold either an expression or a boolean expression
            int i = nextToken;
            int check = 0;
            while(check != 43 && !compareCheck(check) && i < tokens.size()) { //loop until a ';' token, compare operator, or we run out of tokens
                check = tokens.get(i);
                i++;
            }

            if(nextToken == 43) {
                //deadspace to catch empty returns
            } else if(compareCheck(check)) { //if there is a compare operator check syntax for boolean expression
                boolexpr();
            } else { //else check syntax for expression
                expr();
            }
        }
    }

    public static boolean compareCheck(int token) //this function checks if nextToken is a compare operator
    {
        switch(token)
        {
            case 28: //<=
            case 29: //>=
            case 38: //==
            case 41: //<
            case 42: //>
                return true;
            default:
                return false;
        }
    }
}