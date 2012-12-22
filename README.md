# nlp-graph-edit-distance

A thesis project focusing on the usage of dependency graphs as a representation of natural language text. 
Sentences are represented as graph structures, tagged with part-of-speech tags and relations between words.
This representation is used as a measure of similarity between two sentences.

Below is an example of how the the edit operations between the two sentences:
 * Mary was kissed by Bob
 * Bob kissed Mary

![My image](https://raw.github.com/haakondr/nlp-graph-edit-distance/master/img/edit_operations.png)
--------------------------------

# Dependencies
 * java7
 * maven

# Quick start
From the command line in the project root directory:

    mvn install
    mvn exec:java

Enter two sentences, and the graph edit distance will be printed
