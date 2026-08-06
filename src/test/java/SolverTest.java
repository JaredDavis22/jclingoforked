import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.potassco.clingo.AnswerSet;
import org.potassco.clingo.Solver;
import org.potassco.clingo.configuration.args.NumModels;

public class SolverTest {

    @Test
    public void testSolveProgram() {
        List<AnswerSet> answers = new Solver().solve("1 {a; b} 1.", NumModels.all());
        Assert.assertEquals(List.of("a", "b"), symbols(answers));
    }

    /**
     * Instances and encoding are separate programs, so a trailing line comment in the instances must not comment out the
     * first line of the encoding.
     */
    @Test
    public void testSolveInstancesEndingInComment() {
        List<AnswerSet> answers = new Solver().solve("b :- a.", "a. % the fact above is the instance", NumModels.all());
        Assert.assertEquals(List.of("a b"), symbols(answers));
    }

    private static List<String> symbols(List<AnswerSet> answers) {
        return answers.stream()
                .map(answer -> answer.getSymbols().stream()
                        .map(Object::toString)
                        .sorted()
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
    }
}
