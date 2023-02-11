# Toy application for Spring Batch partitioning

## Background

Related to https://stackoverflow.com/questions/55227133/why-do-these-threads-read-the-entire-file-instead-of-maxitemcounter

## Output

The assignment of partition to thread is random and changes with every execution.

### With `RangePartitioner` from SO question

```
Hilo : 1
Index Inicial : 1
Index Final : 5


Hilo : 2
Index Inicial : 6
Index Final : 10


Hilo : 3
Index Inicial : 11
Index Final : 16

SimpleAsyncTaskExecutor-1 writing items = foo1, foo2, foo3, foo4, foo5
SimpleAsyncTaskExecutor-2 writing items = foo11, foo12, foo13, foo14, foo15
SimpleAsyncTaskExecutor-3 writing items = foo6, foo7, foo8, foo9, foo10, foo11, foo12, foo13, foo14, foo15
```

### With `FixedRangePartitioner`

```
Hilo : 1
Index Inicial : 1
Index Final : 5


Hilo : 2
Index Inicial : 6
Index Final : 10


Hilo : 3
Index Inicial : 11
Index Final : 16

SimpleAsyncTaskExecutor-2 writing items = foo6, foo7, foo8, foo9, foo10
SimpleAsyncTaskExecutor-1 writing items = foo11, foo12, foo13, foo14, foo15
SimpleAsyncTaskExecutor-3 writing items = foo1, foo2, foo3, foo4, foo5
```