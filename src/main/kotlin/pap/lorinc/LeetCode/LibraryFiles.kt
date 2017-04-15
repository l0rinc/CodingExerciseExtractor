package pap.lorinc.LeetCode;

val library = mapOf<String, String>(
        "TreeNode" to """
        >package leetcode;
        >
        >public class TreeNode {
        >    public int val;
        >    public TreeNode left, right;
        >
        >    public TreeNode(int val) { this.val = val; }
        >}""",

        "ListNode" to """
        >package leetcode;
        >
        >public class ListNode {
        >    public int val;
        >    public ListNode next;
        >    public ListNode(int val) { this.val = val; }
        >}""",

        "Interval" to """
        >package leetcode;
        >
        >public class Interval {
        >    public int start, end;
        >
        >    public Interval() {}
        >    public Interval(int start, int end) {
        >        this.start = start;
        >        this.end = end;
        >    }
        >}""",

        "GuessGame" to """
        >package leetcode;
        >
        >import java.util.concurrent.ThreadLocalRandom;
        >
        >/**
        > * -1 : My number is lower
        > *  1 : My number is higher
        > *  0 : Congrats! You got it!
        > **/
        >public class GuessGame {
        >    private Integer guess = ThreadLocalRandom.current().nextInt();
        >
        >    public int guess(int num) { return guess.compareTo(num); }
        >}""",

        "VersionControl" to """
        >package leetcode;
        >
        >public class VersionControl {
        >    public boolean isBadVersion(int version) { throw new IllegalStateException(); }
        >}"""
).mapValues { it.value.trimMargin(">").replace("\n", "\\n") }


