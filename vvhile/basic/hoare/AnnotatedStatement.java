package vvhile.basic.hoare;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import vvhile.intrep.Configuration;
import vvhile.intrep.Expression;
import vvhile.intrep.State;
import vvhile.intrep.Statement;

/**
 *
 * @author markus
 */
public class AnnotatedStatement implements Statement {

    public static final int PREFIX_POSITION = 0;
    public static final int INFIX_POSITION = 1;
    public static final int POSTFIX_POSITION = 2;

    private final Statement statement;
    private final List<Annotation> prefixes;
    private final List<Annotation> infixes;
    private final List<Annotation> postfixes;

    public AnnotatedStatement(Statement statement, List<Annotation> prefixes, List<Annotation> infixes, List<Annotation> postfixes) {
        this.statement = statement;
        this.prefixes = prefixes;
        this.infixes = infixes;
        this.postfixes = postfixes;
    }

    public AnnotatedStatement(Statement statement, Annotation prefix, Annotation infix, Annotation postfix) {
        this(
                statement,
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>()
        );
        if (prefix != null) {
            prefixes.add(prefix);
        }
        if (infix != null) {
            infixes.add(infix);
        }
        if (postfix != null) {
            postfixes.add(postfix);
        }
    }

    public List<Annotation> getPrefixes() {
        return new LinkedList<>(prefixes);
    }

    public List<Annotation> getInfixes() {
        return infixes;
    }

    public List<Annotation> getPostfixes() {
        return new LinkedList<>(postfixes);
    }

    public AnnotatedStatement reduce() {
        List<Annotation> newPrefixes = new LinkedList<>();
        List<Annotation> newInfixes = new LinkedList<>();
        List<Annotation> newPostfixes = new LinkedList<>();
        if (statement instanceof AnnotatedStatement) {
            AnnotatedStatement annotated = (AnnotatedStatement) statement;
            Statement reduced = reduce(annotated.statement);
            newPrefixes.addAll(prefixes);
            newPrefixes.addAll(annotated.prefixes);
            newPrefixes.addAll(pre(reduced));
            newPostfixes.addAll(post(reduced));
            newPostfixes.addAll(annotated.getPostfixes());
            newPostfixes.addAll(postfixes);
            return new AnnotatedStatement(
                    trim(reduced),
                    reduce(newPrefixes),
                    reduce(newInfixes),
                    reduce(newPostfixes)
            );
        } else if (statement instanceof Composition) {
            Composition composition = (Composition) statement;
            Statement reduced1 = reduce(composition.getFirstStatement());
            Statement reduced2 = reduce(composition.getSecondStatement());
            newPrefixes.addAll(prefixes);
            newPrefixes.addAll(pre(reduced1));
            newInfixes.addAll(post(reduced1));
            newInfixes.addAll(pre(reduced2));
            newPostfixes.addAll(post(reduced2));
            newPostfixes.addAll(postfixes);
            return new AnnotatedStatement(
                    new Composition(trim(reduced1), trim(reduced2)),
                    reduce(newPrefixes),
                    reduce(newInfixes),
                    reduce(newPostfixes)
            );
        } else if (statement instanceof If) {
            If ite = (If) statement;
            Statement reduced1 = reduce(ite.getIfStatement());
            Statement reduced2 = reduce(ite.getElseStatement());
            return new AnnotatedStatement(
                    new If(ite.getCondition(), reduced1, reduced2),
                    prefixes,
                    infixes,
                    postfixes
            );
        } else if (statement instanceof While) {
            While vvhile = (While) statement;
            Statement reduced = reduce(vvhile.getStatement());
            return new AnnotatedStatement(
                    new While(vvhile.getCondition(), reduced),
                    prefixes,
                    infixes,
                    postfixes
            );
        } else {
            return this;
        }
    }

    private static List<Annotation> pre(Statement statement) {
        if (statement instanceof AnnotatedStatement) {
            AnnotatedStatement annotated = (AnnotatedStatement) statement;
            return annotated.getPrefixes();
        } else {
            return new LinkedList<>();
        }
    }

    private static List<Annotation> post(Statement statement) {
        if (statement instanceof AnnotatedStatement) {
            AnnotatedStatement annotated = (AnnotatedStatement) statement;
            return annotated.getPostfixes();
        } else {
            return new LinkedList<>();
        }
    }

    private static Statement trim(Statement statement) {
        if (statement instanceof AnnotatedStatement) {
            AnnotatedStatement annotated = (AnnotatedStatement) statement;
            if (annotated.statement instanceof Composition) {
                return new AnnotatedStatement(
                        annotated.statement,
                        new LinkedList<>(),
                        annotated.infixes,
                        new LinkedList<>()
                );
            } else {
                return annotated.getStatement();
            }
        } else {
            return statement;
        }
    }

    private static Statement reduce(Statement statement) {
        if (statement instanceof AnnotatedStatement) {
            AnnotatedStatement annotated = (AnnotatedStatement) statement;
            return annotated.reduce();
        } else if (statement instanceof Composition) {
            Composition composition = (Composition) statement;
            Statement reduced1 = reduce(composition.getFirstStatement());
            Statement reduced2 = reduce(composition.getSecondStatement());
            return new AnnotatedStatement(
                    new Composition(trim(reduced1), trim(reduced2)),
                    reduce(pre(reduced1)),
                    reduce(pre(reduced2)),
                    reduce(post(reduced2))
            );
        } else if (statement instanceof If) {
            If ite = (If) statement;
            Statement reduced1 = reduce(ite.getIfStatement());
            Statement reduced2 = reduce(ite.getElseStatement());
            return new If(ite.getCondition(), reduced1, reduced2);
        } else if (statement instanceof While) {
            While vvhile = (While) statement;
            Statement reduced = reduce(vvhile.getStatement());
            return new While(vvhile.getCondition(), reduced);
        } else {
            return statement;
        }
    }

    private static List<Annotation> reduce(List<Annotation> annotations) {
        ListIterator<Annotation> it = annotations.listIterator();
        if (it.hasNext()) {
            Annotation prev = it.next();
            while (it.hasNext()) {
                Annotation next = it.next();
                if (prev.getAnnotation().equals(next.getAnnotation())) {
                    if (next.isIsInvariant()) {
                        it.previous();
                        it.previous();
                        it.remove();
                        it.next();
                    } else {
                        it.remove();
                    }
                } else {
                    prev = next;
                }
            }
        }
        return annotations;
    }

    @Override
    public Configuration run(State state) {
        return statement.run(state);
    }

    @Override
    public Set<Expression.Variable> variables() {
        return statement.variables();
    }

    @Override
    public Set<Expression.Variable> writtenVariables() {
        return statement.writtenVariables();
    }

    @Override
    public Set<Expression.Variable> readVariables() {
        return statement.readVariables();
    }

    @Override
    public Set<Statement> subStatements() {
        return statement.subStatements();
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public String toString(boolean latex) {
        StringBuilder pres = new StringBuilder();
        StringBuilder posts = new StringBuilder();
        prefixes.forEach((annotation) -> {
            pres.append(annotation.toString(latex));
        });
        postfixes.forEach((annotation) -> {
            posts.append(annotation.toString(latex));
        });
        return (latex ? "\\textcolor{blue}{$" : "")
                + pres
                + (latex ? "$} " : " ")
                + statement.toString(latex)
                + (latex ? "\\textcolor{blue}{$" : " ")
                + posts
                + (latex ? "$}" : "");
    }

    @Override
    public String toString() {
        return toString(false);
    }

    private AnnotatedStatement skipAnnotations() {
        return new AnnotatedStatement(statement, Collections.EMPTY_LIST, infixes, Collections.EMPTY_LIST);
    }

}
