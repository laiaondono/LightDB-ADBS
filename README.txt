README

EXTRACTING THE JOIN CONDITIONS
To extract the join predicates from the where clause, we first call the method splitExpressions to split the conjunction
of expressions that form the where clause into a list of expressions, where each of these expressions has the form
A op B where op can be =, !=, <, >, <=, >=. Once we have all conditions split, we see how many tables that expression
involves. If it involves 2 tables, we have a join condition, if it involves 1 or 0, it is considered a select condition.
Each of these conditions are mapped to a hashmap (auxSelectionConds or auxJoinConds) according to the number of tables involved.
These hashmaps link every table to all the single conditions it is involved, for which I decided to use the structure
HashMap<String, List<Expression>>. After assigning all expressions to the corresponding hashmap, we create the "final"
hashmap (HashMap<String, Expression>), where the only change is the value, which is now an expression of the form
conjunction of expressions, that includes all the expressions in the List<Expression> of the hashmap from the same table.

You can find the code, with comments explaining it as well, in the constructor method of the class SelectStatement,
starting in line 75.

GETNEXTUPLE METHOD
Given that SortOperator (and consequently DuplicateEliminationOperator) need to read all of the output from its child
in order to sort and eliminate duplicates, respectively, this classes will not have any call made to their getNextTuple
method, which is why there is no implementation for them.

RESET METHOD
The reset method is used to reset the buffer reader so that a subsequent call will return the first tuple. This method
is only called by the join operator when performing the join (SNLJ), which is why operator created afterwards in the
query plan, such as SortOperator and DuplicateEliminationOperator will not have their reset method called. Therefore,
the reset method of these two classes are empty and there is no implementation.