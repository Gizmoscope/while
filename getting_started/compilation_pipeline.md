# The Compilation Pipeline

To get from a String of source code to an executable program object we create a pipeline in which data is transformed step by step. The pipeline consists of four blocks.
1. The [`Scanner`](../vvhile/frontend/Scanner.java) invoked via `startScan(buffer)`
2. The [`Parser`](../vvhile/frontend/Parser.java) invoked via `startParsing(startSymbol, buffer)`
3. The [`ParseTree`](../vvhile/frontend/ParseTree.java) invoked via `startRetranslation(buffer)`
4. The [`ASTCompiler`](../vvhile/intrep/ASTCompiler.java) invoked via `startCompilation(buffer)`
Instances of these classes are connected via [`BoundedBuffer`](../vvhile/util/BoundedBuffer.java) objects.

The invokation of each block starts a new thread. Wait for the threads to end via the `join()` methods. A complete compilation pipeline is build up as follows.

```java
// Start scanner
Scanner scanner = defaultScanner();
BoundedBuffer<Scanner.ScanObject> scannerToParser = new BoundedBuffer<>(32);
scanner.startScan(scannerToParser);

// Start parser
Parser parser = new Parser(scannerToParser, ruleSet, BasicRuleSet.STM);
BoundedBuffer<Parser.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
parser.startParsing(BasicRuleSet.STM, parserToParseTree);

// Start parse tree
ParseTree parseTree = new ParseTree(parserToParseTree);
BoundedBuffer<Parser.ParseObject> parseTreeToASTCompiler = new BoundedBuffer<>(32);
parseTree.startRetranslation(parseTreeToASTCompiler);

// start AST compiler
ASTCompiler astCompiler = new ASTCompiler(parseTreeToASTCompiler, ruleSet);
BoundedBuffer<Parser.ParseObject> astCompilerToInterpreter = new BoundedBuffer<>(32);
astCompiler.startCompilation(astCompilerToInterpreter);

// wait for the started threads
scanner.join();
parser.join();
parseTree.join();
astCompiler.join();
```

What happens in each of those stages?

### Scanner

The scanner traverses an input string and searches for registered Symbols.

#### Customizing the scanner

To register new symbols use the method `addSymbol(symbol)`. This method is also called internally when using a constructor asking for a rule set. A symbol can be a single character like "[" or "]" but also a string like "try", "catch" or "throw".

Everything that is not parsed as a symbol will be parsed as an identifier. The scan of an identifier can be terminated by characters that can be registered via `addTerminator(char)`. By default all whitespaces are terminators.

To add other types of symbols such as strings one can add so-called subscanners via `addSubscanner(entry, scanner)`. For instance, to be able to parse Strings one can do the following.
```java
addSubscanner("\"", new StringScanner());
```
By default every digit invokes an [`IntegerScanner`](../vvhile/basic/language/IntegerScanner.java).

### Parser

The parser takes the tokens of a scanner and matches them against a grammar.

#### Creating a parser

It is very easy to create a new parser. The constructor asks for a set of rules, a corresponding starting symbol and a buffer from which the parser takes its tokens. This buffer usually is the output of a scanner. Internally the constructor transforms the given grammar into a parsing table. Since parsing tables can only be created for LL(1)-grammars, the grammar is transformed in a prior step and therefore the parser matches the tokens against the transformed grammar. However, in the next stage this transformation is reversed. In practice these details can be ignored unless the internal transformation fails for some reason.

### Parse Tree

A parse tree is the result of the parsing process. The parse tree usually contains rules that are not part of the original grammar. These so-called recursion and rewrite rules have to be retranslated. The parse tree can simply be created as in the example above. After starting the retranslation the parse tree produces writes the rules and tokens into the given buffer that describe the scanned word in the language given by the grammar.

A this point one can use the information as one prefers. Usually the next step is to further transform the resulting data into an intermediate representaion as described in the next section.

### AST Compiler

AST stands for Abstract Syntax Tree which is the intermediate representation of the program we are able to execute. The AST compiler transforms the data of a parse tree into an abstract syntax tree.

[Back to Overview](README.md)
