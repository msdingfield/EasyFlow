EasyFlow
========

An easy framework for non-blocking concurrent execution of a DAG of tasks.

This is a prototype and proof of concept which I wouldn't consider using
in a production system without quite a bit more testing and refinement.

EasyFlow is designed for easily constructing and evaluating a DAG of dependent operations using a minimal number of threads.  By default all evaluations share a pool of threads equal in size to the number of available processors.

In order to be used effectively, all operations must be non-blocking. EasyFlow makes this easier by automatically extracting values from ListenableFutures before invoking consumers.  This means producing operations can return a ListenableFuture and let the framework wait for the value to become available and then invoking consuming operations.  A producer can even return a collection of ListenableFutures and the framework will take care of waiting for all values to be ready before invoking consumers.

In addition, it can automatically fork on a collection of values.  After each item is processed, the results are collected back together.

The graph is defined statically in Java code using annotations.  A class defines a node with inputs and outputs describing the connections among nodes.

Example

@Activity(graph = "calculate")
class ModuloDivision {

    @Input
    public int numerator;

    @Input
    public int denominator;

    @Output
    public int quotient;

    @Output
    public int remainder;

    @Operation
    public void enact() {
        quotient = numerator / denominator;
        remainder = numerator % denominator;
    }
}

The class ModuloDivision above define a node in a graph named "calculate." The node has two incoming edges named "numerator" and "denominator" and two outgoing edges named "quotient" and "remainder."  The node computes the quotient and remainder of the input.

Now a constant could be feed into the denominator with

@Activity(graph = "calculate")
class DenominatorConstant {

    @Output
    public int denominator;

    @Operation
    public void enact() {
        denominator = 2;
    }
}

finally we can add a node to check if the numerator is even or odd.

@Activity(graph = "calculate")
class IsEvenOrOdd {

    @Input
    public int denominator;

    @Input
    public int remainder;

    @Output
    public String evenness;

    @Operation
    public void enact() {
        if (denominator == 2) {
            if (Math.abs(remainder) == 0) {
                evenness = "even";
            } else {
                evenness = "odd";
            }
        } else {
            evenness = "unknown";
        }
    }
}

To use the graph above, simply evaluate it with the following.

final FlowGraph graph = EasyFlow.loadFlowGraph("com.mycompany.sillyexample", "calculate");
final Map<String, Object> params = Maps.newHashMap();
params.put("numerator", 7);
final FlowEvaluation evaluation = graph.evaluate(params);
if (evaluation.isSuccessful()) {
    final Map<String, Object> output = task.getOutputs();
    System.out.printf("The number %d is %s.\n", output.get("numerator"), output.get("evenness"));
} else {
    System.err.printf("Calculation failed with %s\n", evaluation.getErrors());
}

Asynchronous IO Operations Example

Working with asynchronous operations which return ListenableFutures can be called like this

@Activity(graph = "asyncio")
class InvokeService {

    @Input
    public ServiceClient client;

    @Input
    public String query;

    @Output
    public ListenableFuture<QueryResult> result;

    @Operation
    public void enact() {
        result = client.doAsyncQuery(query);
    }
}

@Activity(graph = "asyncio")
class ConsumeResult {

    @Input
    public QueryResult result;

    @Operation
    public void processResult() {
        // Do something with query result
    }
}

Multiple invocations to the remote service can even be done in parallel.

@Activity(graph = "parallelop")
class InvokeService {

    @Input
    public ServiceClient client;

    @ForkOn // Indicate we expect a collection which should run in parallel
    @Input(connectedEdgeName = "queries") // Provide name of collection
    public String query;

    @Output(connectedEdgeName = "results") // Name of results collection
    public ListenableFuture<QueryResult> result;

    @Operation
    public void enact() {
        result = client.doAsyncQuery(query);
    }
}

@Activity(graph = "parallelop")
class ConsumeResult {

    @Input
    public Collection<QueryResult> results;

    @Operation
    public void enact() {
        // do something with query results
    }
}

In the example above, the input consists of a collection of query strings. An instance of InvokeService is created and run for each one to run in parallel.  The framework then waits for all of the results to be available and collects them into a collection named "results" which is given to the ConsumeResult operation.

It is possible to explicitly invoke parallel operations like this

@Activity(graph = "morestuff")
public class ExplicitParallel {

    @Output
    public volatile int valueOne;

    @Output
    public volatile int valueTwo;

    // Explicitly run two expensive operations in parallel
    @Operation
    public void enact() {
        Task.fork( new Runnable() {
            @Override public void run() {
                valueOne = expensiveOperation();
            }
        } );

        Task.fork( new Runnable() {
            @Override public void run() {
                valueTwo = expensiveOperation();
        } );
    }

}

Now, any operations which consume "valueOne" or "valueTwo" will not begin until both of the "expensive operations" in the task have completed.  A small variation is to invoke a Runnable when a ListenableFuture<> completes. This is similar to a normal callback on a ListenableFuture<> except that the framework will not start any downstream tasks until the future is done and the callback executes.

@Activity(graph = "future")
public class ForkOnFuture {

    @Output
    public volatile String result;

    @Operation
    public void enact() {
        final ListenableFuture<String> future = callRemoteService();
        Task.fork(future, new Runnable() {
            result = future.get(); // We know this won't block
        });
    }

}

