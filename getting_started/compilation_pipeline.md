# The Compilation Pipeline

To get from a String of source code to an executable program object we create a pipeline in which data is transformed step by step. The pipeline consists of four blocks.
1. The [`Scanner`](../vvhile/basic/frontend/Scanner.java) invoked via `startScan(buffer)`
2. The [`Parser`](../vvhile/basic/frontend/Parser.java) invoked via `startParsing(startSymbol, buffer)`
3. The [`ParseTree`](../vvhile/basic/frontend/ParseTree.java) invoked via `startRetranslation(buffer)`
4. The [`ASTCompiler`](../vvhile/basic/intrep/ASTCompiler.java) invoked via `startCompilation(buffer)`
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

The scanner traverses an input string and searches for registered Tokens.

### Parser

The parser takes the tokens of a scanner and matches them against a grammar.

### Parse Tree

A parse tree is the result of the parsing process. The parse tree usually contains rules that are not part of the original grammar. These so-called recursion and rewrite rules have to be retranslated.

### AST Compiler

AST stands for Abstract Syntax Tree which is the intermediate representation of the program we are able to execute. The AST compiler transforms the data of a parse tree into an abstract syntax tree.

[Back to Overview](README.md)
