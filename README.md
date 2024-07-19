# DNA Pointer-based B<sup>+</sup>-tree
This is the code project of the paper entitled "Implementing the B<sup>+</sup>-tree on DNA storage systems using DNA pointers". It contains the code implementation of the first B<sup>+</sup>-tree encoding on DNA, called the `DNA Pointer-based B+-tree` or `BPB B+-tree` for short, accelerating searches on DNA while reducing costs significantly.

The corresponding paper is currently submitted to a journal, and we will insert the link to it once it is published.

## Installation
Make sure you have [Java 22+](https://www.oracle.com/de/java/technologies/downloads/) and [Maven](https://maven.apache.org/download.cgi) installed.

## Usage
There is a fully working and customizable example located at `src/test/java/DPBAndContainerTreesTest.java`. The file builds the BPB B+-tree for a default setting, and further simulates the execution of a default range query on DNA. Moreover, the mirrored implementation on [DNAContainer](https://github.com/alexelshaikh/DNAContainer) is also used and the same query is executed there as well.

## Contact
    Name:   Alex El-Shaikh
    Email:  elshaika@mathematik.uni-marburg.de